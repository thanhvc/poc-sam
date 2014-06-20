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
package org.exoplatform.social.core.storage.streams;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.common.graph.GraphContext.Scope;
import org.exoplatform.social.common.graph.Vertex;
import org.exoplatform.social.common.graph.simple.SimpleUndirectGraph;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.cache.model.data.ActivitiesFixedListData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityType;
import org.exoplatform.social.core.storage.cache.model.key.ListActivitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.NewListActivitiesKey;
import org.exoplatform.social.core.storage.listener.StreamFixedSizeListener;
import org.exoplatform.social.core.storage.streams.ActivityRefContext.PostType;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 12, 2014  
 */
public abstract class AStream implements StreamFixedSizeListener {
  
  protected SOCContext socContext; 
  
  final List<ActivityRefKey> keys;
  
  public AStream() {
    keys = new ArrayList<ActivityRefKey>(5);
  }
  
  public AStream context(SOCContext socContext) {
    this.socContext = socContext;
    return this;
  }
  
  public AStream feed(Builder builder) {
    this.keys.add(builder.feedRefKey());
    return this;
  }
  
  public AStream owner(Builder builder) {
    if (builder.isUserOwner) {
      this.keys.add(builder.ownerRefKey());
    }
    
    return this;
  }
  
  public AStream connection(Builder builder) {
    if (builder.isUserConnection) {
      this.keys.add(builder.connectionsRefKey());
    }
    
    return this;
  }
  
  public AStream myspaces(Builder builder) {
    if (!builder.isUserOwner) {
      this.keys.add(builder.mySpacesRefKey());
    }
    
    return this;
  }
  
  public AStream space(Builder builder) {
    if (!builder.isUserOwner) {
      this.keys.add(builder.spaceRefKey());
    }
    
    return this;
  }
  
  public List<ActivityRefKey> getKeys() {
    return keys;
  }

  @Override
  public void update(String inVertexId, String outVertexId) {
    SimpleUndirectGraph graph = this.socContext.getActivityCacheGraph();
    Vertex<Object> inVertex = null;
    if (graph.contains(inVertexId)) {
      inVertex = graph.getVertex(inVertexId);
    } 
    
    Vertex<Object> outVertex = null;
    if (graph.contains(outVertexId)) {
      outVertex = graph.getVertex(outVertexId);
    }
    
    if (inVertex != null && outVertex != null) {
      graph.removeEdge(inVertex, outVertex);
    }
  }
  
  public abstract void doExecute();
  
  public static class WHATS_HOT extends AStream {
    
    public static AStream init() {
      return new WHATS_HOT();
    }
    @Override
    public void doExecute() {
      ExoCache<NewListActivitiesKey, ActivitiesFixedListData> activitiesCache = this.socContext.getActivitiesGraphCache();
      //
      for(ActivityRefKey refKey : keys) {
        NewListActivitiesKey cacheKey = refKey.listActivitiesKey();
        ActivitiesFixedListData data = activitiesCache.get(cacheKey);
        if (data == null) {
          data = new ActivitiesFixedListData(cacheKey.handle());
          activitiesCache.put(cacheKey, data);
        }
        data.insertFirst(refKey.handle, this);
      }
    }
    
  };
  
  public static class GRAPH extends AStream {
    
    public static AStream init() {
      return new GRAPH();
    }
    @Override
    public void doExecute() {
      SimpleUndirectGraph graph = this.socContext.getActivityCacheGraph();
      
      Vertex<Object> inVertex = null;
      Vertex<Object> outVertex = null;
      boolean shouldCreateEdge = false;
      //
      for (ActivityRefKey refKey : keys) {
        NewListActivitiesKey cacheKey = refKey.listActivitiesKey();
         //in vertex create or not
        if (!graph.contains(refKey.handle)) {
          inVertex = graph.addVertex(refKey.handle);
          shouldCreateEdge = true;
        } else {
          inVertex = graph.getVertex(refKey.handle);
        }
        //out vertex create or not
        if (!graph.contains(cacheKey)) {
          outVertex = graph.addVertex(cacheKey);
          shouldCreateEdge = true;
        } else {
          outVertex = graph.getVertex(cacheKey);
        }
        
        if (shouldCreateEdge) {
          graph.addEdge(refKey.edgeHandle(), inVertex, outVertex);
        }
        
      }
    }
    
  };
  
  public static class COUNTER extends AStream {
    
    public static AStream init() {
      return new COUNTER();
    }
    @Override
    public void doExecute() {
      //
      for (ActivityRefKey refKey : keys) {
        ActivityCountKey countKey = refKey.activityCountKey();
        IntegerData integerData = this.socContext.getActivitiesCountCache().get(countKey);
        if (integerData != null) {

          IntegerData updatedIntegerData = new IntegerData(integerData.build() + 1);
          this.socContext.getActivitiesCountCache().put(countKey, updatedIntegerData);
        }
      }
    }
    
  };
  
  public static class RELATIONSHIP extends AStream {
    
    public static AStream init() {
      return new RELATIONSHIP();
    }
    @Override
    public void doExecute() {
      ExoCache<NewListActivitiesKey, ActivitiesFixedListData> activitiesCache = this.socContext.getActivitiesGraphCache();
      SimpleUndirectGraph graph = this.socContext.getActivityCacheGraph();
      
      //
      for(ActivityRefKey refKey : keys) {
        NewListActivitiesKey cacheKey = refKey.listActivitiesKey();
        ActivitiesFixedListData data = activitiesCache.get(cacheKey);
        
        if (data != null) {
          data.insertFirst(refKey.handle, this);
          
          Vertex<Object> inVertex = graph.addVertex(refKey.handle);
          Vertex<Object> outVertex = graph.addVertex(cacheKey);
          graph.addEdge(cacheKey.handle(), inVertex, outVertex);
        }
      }
    }
    
  };
  
  public static class MENTIONER extends AStream {
    
    public static AStream init() {
      return new MENTIONER();
    }
    @Override
    public void doExecute() {
      ExoCache<NewListActivitiesKey, ActivitiesFixedListData> activitiesCache = this.socContext.getActivitiesGraphCache();
      SimpleUndirectGraph graph = this.socContext.getActivityCacheGraph();
      
      //
      for(ActivityRefKey refKey : keys) {
        NewListActivitiesKey cacheKey = refKey.listActivitiesKey();
        ActivitiesFixedListData data = activitiesCache.get(cacheKey);
        
        if (data != null) {
          data.insertFirst(refKey.handle, this);
          
          Vertex<Object> inVertex = graph.addVertex(refKey.handle);
          Vertex<Object> outVertex = graph.addVertex(cacheKey);
          graph.addEdge(cacheKey.handle(), inVertex, outVertex);
        }
      }
    }
    
  };
  
  public static class REMOVER extends AStream {
    
    public static AStream init() {
      return new REMOVER();
    }
    
    
    @Override
    public void doExecute() {
      ExoCache<NewListActivitiesKey, ActivitiesFixedListData> activitiesCache = this.socContext.getActivitiesGraphCache();
      SimpleUndirectGraph graph = this.socContext.getActivityCacheGraph();
      
      //
      for(ActivityRefKey refKey : keys) {
        NewListActivitiesKey cacheKey = refKey.listActivitiesKey();
        ActivitiesFixedListData data = activitiesCache.get(cacheKey);
        
          if (data != null) {
            //this.activityGraph.removeEdge(key.label())
            //.removeVertex(String.class, target.getId())
            //.removeVertex(ListActivitiesKey.class, key)
            graph.removeEdge(cacheKey.handle());
            //graph.removeVertex(String.class, refKey.activityId, Scope.ALL);
            graph.removeVertex(ListActivitiesKey.class, cacheKey.handle(), Scope.SINGLE);
            data.remove(refKey.handle);
        }
        
      }
    }
    
  };
  
  public static Builder initActivity(ExoSocialActivity activity) {
    return new Builder(activity);
  }
  
  public static Builder initActivity(String identityId, ExoSocialActivity activity) {
    return new Builder(identityId, activity);
  }
  
  public static Builder initComment(ExoSocialActivity activity, ExoSocialActivity comment) {
    return new Builder(activity, comment);
  }
  
  public static Builder initComment(String identityId, ExoSocialActivity activity, ExoSocialActivity comment) {
    return new Builder(identityId, activity, comment);
  }

  public static class Builder {
    public boolean isUserOwner;
    public boolean isUserConnection;
    public String identityId;
    public String streamOwnerIdentityId;
    public ExoSocialActivity activity;
    public ExoSocialActivity comment;
    public PostType type;
    public SOCContext socContext;
    
    public Builder(ExoSocialActivity activity, ExoSocialActivity comment) {
     this(activity.getPosterId(), activity, comment);
    }
    
    public Builder(String identityId, ExoSocialActivity activity, ExoSocialActivity comment) {
      this.isUserOwner = ActivityStream.Type.USER.equals(activity.getActivityStream().getType());
      this.type = comment != null ? PostType.COMMENT : PostType.ACTIVITY;
      this.activity = activity;
      this.comment = comment;
      this.identityId = identityId;
      this.streamOwnerIdentityId = activity.getStreamId();
      this.isUserConnection = (this.identityId != this.activity.getStreamId() && isUserOwner);
      
    }
    
    public Builder(ExoSocialActivity activity) {
      this(activity, null);
    }
    
    public Builder(String identityId, ExoSocialActivity activity) {
      this(identityId, activity, null);
    }
    
    public ActivityRefContext buildContext() {
      return new ActivityRefContext(this);
    }
    
    /**
     * Build the feed stream key and context
     * @return
     */
    public ActivityRefKey feedRefKey() {
      return new ActivityRefKey(this, ActivityType.FEED);
    }
    
    /**
     * Build the connection stream key and context
     * @return
     */
    public ActivityRefKey connectionsRefKey() {
      return this.isUserConnection ? new ActivityRefKey(this, ActivityType.CONNECTION) : null;
    }
    
    /**
     * Build the owner stream key and context
     * @return
     */
    public ActivityRefKey ownerRefKey() {
      return this.isUserOwner ? new ActivityRefKey(this, ActivityType.USER) : null;
    }
    /**
     * Build the my spaces stream key and context
     *
     * @return
     */
    public ActivityRefKey mySpacesRefKey() {
      //TODO return NULL Option
      return !this.isUserOwner ? new ActivityRefKey(this, ActivityType.SPACES) : null;
    }
    
    public ActivityRefKey spaceRefKey() {
      //TODO return NULL Option
      return !this.isUserOwner ? new ActivityRefKey(this, ActivityType.SPACE) : null;
    }
    
    public NewListActivitiesKey feedCacheKey() {
      return this.isUserOwner ? NewListActivitiesKey.init(identityId).key(ActivityType.FEED) : null;
    }
    
    public NewListActivitiesKey connectionCacheKey() {
      return this.isUserOwner ? NewListActivitiesKey.init(identityId).key(ActivityType.CONNECTION) : null;
    }
    
    public NewListActivitiesKey ownerCacheKey() {
      return this.isUserOwner ? NewListActivitiesKey.init(identityId).key(ActivityType.USER) : null;
    }
    
    public NewListActivitiesKey myspacesCacheKey() {
      return this.isUserOwner ? NewListActivitiesKey.init(identityId).key(ActivityType.SPACES) : null;
    }
    
    public NewListActivitiesKey spaceCacheKey() {
      return this.isUserOwner ? null : NewListActivitiesKey.init(identityId).key(ActivityType.SPACE);
    }
  }
}
