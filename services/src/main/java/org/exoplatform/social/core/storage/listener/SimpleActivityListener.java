/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.storage.listener;

import java.util.concurrent.TimeUnit;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.graph.Vertex;
import org.exoplatform.social.common.graph.simple.SimpleUndirectGraph;
import org.exoplatform.social.common.service.utils.LogWatch;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.listeners.Callback;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.activity.DataChangeListener;
import org.exoplatform.social.core.storage.activity.DataChangeQueue;
import org.exoplatform.social.core.storage.activity.DataContext;
import org.exoplatform.social.core.storage.activity.DataModel;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.selector.ScopeCacheSelector;
import org.exoplatform.social.core.storage.memory.ActivityUtils;
import org.exoplatform.social.core.storage.memory.InMemoryActivityStorageImpl;
import org.exoplatform.social.core.storage.persister.Persister;
import org.exoplatform.social.core.storage.persister.PersisterTask;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 14, 2014  
 */
public class SimpleActivityListener<M extends ExoSocialActivity> extends AbstractActivityListener<M> implements Persister {
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(SimpleActivityListener.class);
  /** */
  final SOCContext socContext;
  /** */
  final DataChangeListener<M> cachedListener;
  /** */
  final GraphListener<M> graphListener;
  /** */
  final RelationshipActivityListener<M> relationshipListener;
  /** */
  final MentionerActivityListener<M> mentionerListener;
  /** */
  final CacheCounterListener<M> cacheCounterListener;
  /** */
  final StreamUpdateListener<M> streamUpdateListener;
  /** */
  final SimpleUndirectGraph graph;
  /** */
  final DataChangeListener<DataModel> jcrPersisterListener;
  /** */
  final PersisterTask timerTask;
  /** */
  final DataContext<DataModel> dataContext;
  /** */
  final LogWatch logWatch;
  
  public SimpleActivityListener(InMemoryActivityStorageImpl storage, SOCContext socContext) {
    this.socContext = socContext;
    this.dataContext = this.socContext.getDataContext();
    this.graph = socContext.getActivityCacheGraph();
    this.cachedListener = new CachedListener<M>(socContext);
    this.graphListener = new GraphListener<M>(socContext);
    this.relationshipListener = new RelationshipActivityListener<M>(socContext);
    this.mentionerListener = new MentionerActivityListener<M>(socContext);
    this.cacheCounterListener = new CacheCounterListener<M>(socContext);
    this.streamUpdateListener = new StreamUpdateListener<M>(socContext);
    this.jcrPersisterListener = new PersisterListener(storage, socContext);
    timerTask = PersisterTask.init()
                             .persister(this)
                             .wakeup(socContext.getIntervalPersistThreshold())
                             .timeUnit(TimeUnit.MILLISECONDS)
                             .build();
    timerTask.start();
    this.socContext.setPersisterTask(timerTask);
    logWatch = new LogWatch("persister to storage.");
  }
  
  @Override
  public void onAddActivity(M activity) {
    cachedListener.onAddActivity(activity);
    graphListener.onAddActivity(activity);
    streamUpdateListener.onAddActivity(activity);
    relationshipListener.onAddActivity(activity);
    mentionerListener.onAddActivity(activity);
    cacheCounterListener.onAddActivity(activity);
    //
    String handle = ActivityUtils.handle(activity);
    this.dataContext.addActivity(DataModel.init(handle).build());
    commit(false);
  }
  
  @Override
  public void onAddActivity(M activity, Callback callback) {
    cachedListener.onAddActivity(activity);
    graphListener.onAddActivity(activity);
    streamUpdateListener.onAddActivity(activity);
    relationshipListener.onAddActivity(activity);
    mentionerListener.onAddActivity(activity);
    cacheCounterListener.onAddActivity(activity);
    //
    String handle = ActivityUtils.handle(activity);
    this.dataContext.addActivity(DataModel.init(handle, callback).lazyCreated(activity.isLazyCreated()).build());
    commit(false);
  }

  @Override
  public void onAddComment(M activity, M comment) {
    cachedListener.onAddComment(activity, comment);
    streamUpdateListener.onUpdate(activity);
    relationshipListener.onAddComment(activity, comment);
    //
    String handle = ActivityUtils.handle(comment);
    this.dataContext.addComment(DataModel.init(activity.getId()).build(),
                                DataModel.init(handle).lazyCreated(comment.isLazyCreated()).build());
    commit(false);
  }
  
  @Override
  public void onAddComment(M activity, M comment, Callback callback) {
    cachedListener.onAddComment(activity, comment);
    streamUpdateListener.onUpdate(activity);
    //
    String handle = ActivityUtils.handle(comment);
    this.dataContext.addComment(DataModel.init(activity.getId()).build(),
                                DataModel.init(handle, callback).build());
    
    commit(false);
  }

  @Override
  public void onUpdateActivity(M activity) {
    cachedListener.onUpdate(activity);
    this.dataContext.update(DataModel.init(activity.getId()).build());
    commit(false);
  }
  
  @Override
  public void onLikeActivity(M activity) {
    cachedListener.onUpdate(activity);
    this.dataContext.like(DataModel.init(activity.getId()).build());
    commit(false);
  }
  
  @Override
  public void onUnlikeActivity(M activity) {
    cachedListener.onUpdate(activity);
    this.dataContext.unLike(DataModel.init(activity.getId()).build());
    commit(false);
  }

  @Override
  public void onRemoveActivity(M activity) {
    cachedListener.onRemoveActivity(activity);
    graphListener.onRemoveActivity(activity);
    this.dataContext.removeActivity(DataModel.init(activity.getId()).build());
    commit(false);
    
  }

  @Override
  public void onRemoveComment(M activity, M comment) {
    //cachedListener.onUpdate(activity);
    cachedListener.onRemoveComment(activity, comment);
    //mentionerListener.onRemoveComment(activity, comment);
    this.dataContext.removeComment(DataModel.init(activity.getId()).build(),
                                   DataModel.init(comment.getId()).build());
    
    commit(false);
  }
  
  @Override
  public void commit(boolean forceCommit) {
    persistFixedSize(forceCommit);
  }
  
  private void persistFixedSize(boolean forcePersist) {
    DataContext<DataModel> context = this.socContext.getDataContext();
    if (timerTask.shoudldPersist(context.getChangesSize()) || forcePersist) {
      DataChangeQueue<DataModel> changes = context.popChanges();
      if (changes != null && changes.size() > 0) {
        logWatch.start();
        LOG.info("start persist size = " + changes.size());
        changes.broadcast(this.jcrPersisterListener);
        logWatch.stop();
        LOG.info("persist size = " + changes.size() + " consume time : " + logWatch.getElapsedTime() + "ms");
        //TODO find the better way don't clear caching
        clearNewCache();
      }
    }
  }
  
  private void clearNewCache() {
    try {
      this.socContext.getActivitiesCountCache().select(new ScopeCacheSelector<ActivityCountKey, IntegerData>());
    }
    catch (Exception e) {
      LOG.error(e);
    }
  }


  @Override
  public void update(String inVertexId, String outVertexId) {
    Vertex<Object> inVertex = this.graph.getVertex(inVertexId);
    Vertex<Object> outVertex = this.graph.getVertex(outVertexId);
    
    if (inVertex != null && outVertex != null) {
      this.graph.removeEdge(inVertex, outVertex);
    }
  }
}
