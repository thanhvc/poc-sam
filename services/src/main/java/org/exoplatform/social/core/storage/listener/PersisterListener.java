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

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivityLifeCycle;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.listeners.Callback;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.activity.DataChangeListener;
import org.exoplatform.social.core.storage.activity.DataModel;
import org.exoplatform.social.core.storage.activity.DataStatus;
import org.exoplatform.social.core.storage.cache.model.data.ActivitiesFixedListData;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.NewListActivitiesKey;
import org.exoplatform.social.core.storage.memory.InMemoryActivityStorageImpl;
import org.exoplatform.social.core.storage.proxy.ActivityProxyBuilder;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 15, 2014  
 */
public class PersisterListener implements DataChangeListener<DataModel> {
  private static final Log LOG = ExoLogger.getLogger(PersisterListener.class);
  
  /** */
  final InMemoryActivityStorageImpl storage;
  /** */
  final ExoCache<ActivityKey, ActivityData> activityCache;
  /** */
  final ExoCache<NewListActivitiesKey, ActivitiesFixedListData> activitiesCache;
  /** */
  final SOCContext socContext;
  /** */
  final ActivityLifeCycle activityLifeCycle;
  
  public PersisterListener(InMemoryActivityStorageImpl storage, SOCContext socContext) {
    this.storage = storage;
    this.activityCache = socContext.getActivityCache();
    this.activitiesCache = socContext.getActivitiesGraphCache();
    this.socContext = socContext;
    this.activityLifeCycle = new ActivityLifeCycle();
  }

  @Override
  public void onAddActivity(DataModel target) {
    ActivityKey key = new ActivityKey(target.getHandle());
    ActivityData data = activityCache.get(key);
    if (data != null && DataStatus.TRANSIENT.equals(data.getStatus())) {
      ExoSocialActivity a = data.build();
      if (a.isLazyCreated()) {
        this.storage.persistJCRActivity(a);
      } else {
        this.storage.persistActivity(a);
      }
      //
      data = new ActivityData(a);
      data.setStatus(DataStatus.PERSISTENTED);
      activityCache.put(key, data);
      //invoke activity life-cycle to create notification or something like that.
      activityLifeCycle.saveActivity(a);
      Callback callback = target.getCallback();
      
      if (callback != null) {
        callback.done(a);
      }
    }
  }

  @Override
  public void onRemoveActivity(DataModel activity) {
    ActivityKey key = new ActivityKey(activity.getHandle());
    ActivityData data = activityCache.get(key);
    //In the case, the ActivityData is REMOVED status
    // using data.build will get NULL object and call getId() throw NPE.
    //just use data.getId() to take activityId
    if (data != null) {
      this.storage.deleteActivity(data.getId());
      this.activityCache.remove(key);
      key = new ActivityKey(data.getId());
      this.activityCache.remove(key);
    }
  }

  @Override
  public void onUpdate(DataModel activity) {
    ActivityKey key = new ActivityKey(activity.getHandle());
    ActivityData data = activityCache.get(key);
    if (data != null && DataStatus.CHANGED.equals(data.getStatus())) {
      
      ExoSocialActivity changedActivity = data.build();
      if (!changedActivity.isComment()) {
        changedActivity.setTitle(activity.paramValue(DataModel.ACTIVITY_TITLE));
        changedActivity.setBody(activity.paramValue(DataModel.ACTIVITY_BODY));
      }
     
      this.storage.updateActivity(changedActivity);
      data.setStatus(DataStatus.PERSISTENTED);
    }
  }

  @Override
  public void onAddComment(DataModel activity, DataModel comment) {
    ActivityKey key = new ActivityKey(comment.getHandle());
    ActivityData data = activityCache.get(key);
    if (data != null && DataStatus.TRANSIENT.equals(data.getStatus())) {
      ExoSocialActivity newComment = data.build();
      if (newComment.isComment()) {
        //
        //onAddActivity(activity);
        ActivityData parent =  activityCache.get(new ActivityKey(activity.getHandle()));
        if (parent != null) {
          if (newComment.isLazyCreated()) {
            ExoSocialActivity parentActivity = parent.build();
            ExoSocialActivity proxyParent = ActivityProxyBuilder.of(ExoSocialActivity.class, parentActivity);
            this.storage.persistCommentInMemory(proxyParent, newComment);
          } else {
            this.storage.persistComment(parent.build(), newComment);
          }
        }
        data = new ActivityData(newComment, DataStatus.PERSISTENTED);
        activityCache.put(key, data);
        //invoke activity life-cycle to create notification or something like that.
        activityLifeCycle.saveComment(newComment);
      }
    }
  }

  @Override
  public void onLike(DataModel activity) {
    ActivityKey key = new ActivityKey(activity.getHandle());
    ActivityData data = activityCache.get(key);
    if (data != null && DataStatus.CHANGED.equals(data.getStatus())) {
      ExoSocialActivity changedActivity = data.build();
      this.storage.persistLikeOrUnlike(changedActivity);
      data.setStatus(DataStatus.PERSISTENTED);
      //invoke activity life-cycle to create notification or something like that.
      activityLifeCycle.likeActivity(changedActivity);
    }
  }

  @Override
  public void onUnlike(DataModel activity) {
    ActivityKey key = new ActivityKey(activity.getHandle());
    ActivityData data = activityCache.get(key);
    if (data != null && DataStatus.CHANGED.equals(data.getStatus())) {
      ExoSocialActivity changedActivity = data.build();
      this.storage.persistLikeOrUnlike(changedActivity);
      data.setStatus(DataStatus.PERSISTENTED);
    }
  }

  @Override
  public void onRemoveComment(DataModel activity, DataModel comment) {
    ActivityKey commentKey = new ActivityKey(comment.getHandle());
    ActivityData commentData = activityCache.get(commentKey);
    if (commentData != null && DataStatus.REMOVED.equals(commentData.getStatus())) {
      ExoSocialActivity newComment = commentData.build();
      if (newComment.isComment()) {
        //
        ActivityData parent =  activityCache.get(new ActivityKey(activity.getHandle()));
        this.storage.deleteActivity(newComment.getId());
        this.storage.persistAddOrRemoveComment(parent.build());
        parent.setStatus(DataStatus.PERSISTENTED);
        //TODO need to update Activity here
        activityCache.remove(commentKey);
      }
    }
  }
}
