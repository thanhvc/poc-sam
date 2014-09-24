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
package org.exoplatform.social.core.storage;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.graph.Vertex;
import org.exoplatform.social.common.graph.simple.SimpleUndirectGraph;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.activity.DataContext;
import org.exoplatform.social.core.storage.activity.DataModel;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.storage.cache.StreamCacheType;
import org.exoplatform.social.core.storage.cache.model.data.ActivitiesFixedListData;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.NewListActivitiesKey;
import org.exoplatform.social.core.storage.persister.PersisterTask;
import org.exoplatform.social.core.storage.streams.AStreamVersion;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 15, 2014  
 */
public class SOCContext implements Startable {
  
  private static final Log LOG = ExoLogger.getLogger(SOCContext.class);
  
  /** */
  static final String INTERVAL_ACTIVITY_PERSIST_THRESHOLD = "exo.social.interval.activity.persist.threshold";
  /** */
  static final String ACTIVITY_LIMIT_PERSIST_THRESHOLD = "exo.social.activity.limit.persist.threshold";
  /** */
  static final long DEFAULT_INTERVAL_ACTIVITY_PERSIST_THRESHOLD = 20000; //20s  = 1000 x 20
  /** */
  static final long DEFAULT_ACTIVITY_LIMIT_PERSIST_THRESHOLD = 100; //number per persist storage
  /** */
  private static SOCContext instance;
  /** */
  final AStreamVersion streamUpdater;
  /** */
  final ExoCache<ActivityKey, ActivityData> exoActivityCache;
  /** */
  ExoCache<NewListActivitiesKey, ActivitiesFixedListData> exoActivitiesGraphCache;
  /** */
  final DataContext<DataModel> context;
  /** */
  final SimpleUndirectGraph activityCacheGraph;
  /** */
  final ExoCache<ActivityCountKey, IntegerData> exoActivitiesCountCache;
  /** */
  final SimpleUndirectGraph relationshipCacheGraph;
  /** */
  final SocialStorageCacheService cacheService;
  /** */
  private long intervalPersistThreshold;
  /** */
  private long limitPersistThreshold;
  /** */
  private PersisterTask timerTask;
  /** */
  private boolean activityMemoryStatus = true;
   
  public SOCContext(InitParams params, CacheService service, ActivityManager activityManager) {
    ValueParam intervalPersistThreshold = params.getValueParam(INTERVAL_ACTIVITY_PERSIST_THRESHOLD);
    ValueParam limitPersistThreshold = params.getValueParam(ACTIVITY_LIMIT_PERSIST_THRESHOLD);
    if (intervalPersistThreshold != null && intervalPersistThreshold.getValue() != null) {
      this.intervalPersistThreshold = Long.valueOf(intervalPersistThreshold.getValue()).longValue();
    } else {
      this.intervalPersistThreshold = DEFAULT_INTERVAL_ACTIVITY_PERSIST_THRESHOLD;
    }
    
    if (limitPersistThreshold != null && limitPersistThreshold.getValue() != null) {
      this.limitPersistThreshold = Long.valueOf(limitPersistThreshold.getValue()).longValue();
    } else {
      this.limitPersistThreshold = DEFAULT_ACTIVITY_LIMIT_PERSIST_THRESHOLD;
    }
    
    //TODO improve here to build provide by constructor injection
    this.cacheService = CommonsUtils.getService(SocialStorageCacheService.class);
    this.exoActivityCache = this.cacheService.getActivityCache();
    this.exoActivitiesGraphCache = StreamCacheType.ACTIVITIES_GRAPH.getFromService(service);
    this.exoActivitiesCountCache = this.cacheService.getActivitiesCountCache();
    this.context = new DataContext<DataModel>();
    this.streamUpdater = new AStreamVersion();
    this.activityCacheGraph = new SimpleUndirectGraph(Vertex.MODEL);
    this.relationshipCacheGraph = new SimpleUndirectGraph(Vertex.MODEL);
  }
  
  /**
   * Returns the graph what provides relationship information 
   * between the activity and Identity's stream
   * 
   * For example: when UserA has relationship UserB, 
   * then activityA can show on their Feed stream both of them
   * 
   * @return the activity graph
   */
  public SimpleUndirectGraph getActivityCacheGraph() {
    return activityCacheGraph;
  }
  
  /**
   * Returns the graph what provides connection information
   * 
   * @return the graph connection
   */
  public SimpleUndirectGraph getRelationshipCacheGraph() {
    return relationshipCacheGraph;
  }

  public ExoCache<ActivityKey, ActivityData> getActivityCache() {
    return exoActivityCache;
  }

  public ExoCache<NewListActivitiesKey, ActivitiesFixedListData> getActivitiesGraphCache() {
    return exoActivitiesGraphCache;
  }
  
  public ExoCache<ActivityCountKey, IntegerData> getActivitiesCountCache() {
    return exoActivitiesCountCache;
  }
  
  public AStreamVersion getStreamUpdater() {
    return streamUpdater;
  }
  
  public void clear() {
    this.exoActivityCache.clearCache();
    this.exoActivitiesGraphCache.clearCache();
    this.activityCacheGraph.clear();
    this.relationshipCacheGraph.clear();
    streamUpdater.clearAll();
    this.context.clearAll();
  }
  
  public class ActivityTask implements Runnable {
    public void run() {
      try {
      } catch (Throwable t) {
      }
    }
  }

  /**
   * Gets the data context
   * @return
   */
  public DataContext<DataModel> getDataContext() {
    return this.context;
  }

  /**
   * Gets interval activity persist threshold time.
   * 
   * @return The period of time to execute a persistence call.
   */
  public long getIntervalPersistThreshold() {
    return intervalPersistThreshold;
  }

  /**
   * Sets period of time to execute a persistence call.
   * 
   * @param intervalPersistThreshold period of time (s).
   */
  public void setIntervalPersistThreshold(long intervalPersistThreshold) {
    
    if (this.intervalPersistThreshold != intervalPersistThreshold) {
      this.intervalPersistThreshold = intervalPersistThreshold;
      if (timerTask != null) {
        timerTask.resetWakeupInterval(this.intervalPersistThreshold);
      }
    }
  }
  
  /**
   * Sets the status of activity memory feature processing.
   * status = TRUE: The proxy will return InMemoryActivityManagerImpl instance
   * status = FALSE: ActivityManagerImpl instance will be returned 
   * 
   * @param status the status of activity memory processing
   */
  public void switchActivityMemory(boolean status) {
    if (status != this.activityMemoryStatus) {
      this.activityMemoryStatus = status;
      this.exoActivitiesGraphCache.clearCache();
      this.cacheService.getActivitiesCache().clearCache();
    }
    
  }
  
  /**
   * Returns the status of activity memory feature processing
   * 
   */
  public boolean getActivityMemoryStatus() {
    return this.activityMemoryStatus;
  }
  
  /**
   * Keeping the PersisterTask in the context
   * @param timerTask
   */
  public void setPersisterTask(PersisterTask timerTask) {
    this.timerTask = timerTask;
  }

  /**
   * Gets number per persist storage.
   * 
   * @return the number per persist storage.
   */
  public long getLimitPersistThreshold() {
    return limitPersistThreshold;
  }

  /**
   * Sets number per persist storage.
   * 
   * @param limitPersistThreshold the number per persist storage.
   */
  public void setLimitPersistThreshold(long limitPersistThreshold) {
    this.limitPersistThreshold = limitPersistThreshold;
  }

  public static SOCContext instance() {
    if (instance == null) {
      instance = CommonsUtils.getService(SOCContext.class);
    }
    return instance;
  }

  @Override
  public void start() {
    LOG.info("Initializing the SOC context...");
    SOCContext.instance().switchActivityMemory(true);
  }

  @SuppressWarnings("static-access")
  @Override
  public void stop() {
    LOG.info("Stopping the SOC context.");
    this.instance = null;
  }

}
