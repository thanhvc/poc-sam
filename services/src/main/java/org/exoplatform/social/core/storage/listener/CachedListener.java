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

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.activity.DataChangeListener;
import org.exoplatform.social.core.storage.activity.DataModel;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.DataStatus;
import org.exoplatform.social.core.storage.cache.model.data.InMemoryActivityData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.memory.ActivityUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 11, 2014  
 */
public class CachedListener<M extends ExoSocialActivity> extends DataChangeListener.Base<M> {
  /** */
  private static final Log LOG = ExoLogger.getLogger(CachedListener.class);
  /** */
  final ExoCache<ActivityKey, ActivityData> activityCache;
  /** */
  final SOCContext socContext;
  
  public CachedListener(SOCContext socContext) {
    this.socContext = socContext;
    this.activityCache = socContext.getActivityCache();
  }

  @Override
  public void onAddActivity(ExoSocialActivity activity) {
    ActivityKey key = ActivityUtils.key(activity);
    ActivityData data = new InMemoryActivityData(activity, activity.isLazyCreated() ? DataStatus.TRANSIENT : DataStatus.CHANGED);
    activityCache.put(key, data);
  }
  
  @Override
  public void onAddComment(M activity, M comment) {
    ActivityKey key = ActivityUtils.key(activity);
    ActivityData data = activityCache.get(key);
    if (data != null && DataStatus.PERSISTENTED.equals(ActivityUtils.getDataStatus(data))) {
      data = new InMemoryActivityData(activity, DataStatus.CHANGED);
    } else {
      data = new InMemoryActivityData(activity, DataStatus.TRANSIENT);
    }
    activityCache.put(key, data);
    
    //comment
    ActivityKey commentKey = ActivityUtils.key(comment);
    ActivityData commentData = new InMemoryActivityData(comment, DataStatus.TRANSIENT);
    activityCache.put(commentKey, commentData);
  }

  @Override
  public void onRemoveActivity(ExoSocialActivity activity) {
    ActivityKey key = ActivityUtils.key(activity);
    ActivityData data = activityCache.get(key);
    if (ActivityUtils.getDataStatus(data).equals(DataStatus.REMOVED)) {
      return;
    } else if (ActivityUtils.getDataStatus(data).equals(DataStatus.TRANSIENT)) {
      //in the case, if status != transient, that means it had been persisted in the past
      // it must be deleted so far
      //Otherwise, don't update its status.
      activityCache.remove(key);
    } else {
      ActivityUtils.setDataStatus(data, DataStatus.REMOVED);
    }
  }
  
  @Override
  public void onRemoveComment(ExoSocialActivity activity, ExoSocialActivity comment) {
    ActivityKey key = ActivityUtils.key(activity);
    ActivityData data = new InMemoryActivityData(activity, DataStatus.CHANGED);
    activityCache.put(key, data);
    
    // comment
    ActivityKey commentKey = ActivityUtils.key(comment);
    ActivityData commentData = activityCache.get(commentKey);
    if (ActivityUtils.getDataStatus(commentData).equals(DataStatus.REMOVED)) {
      return;
    } else {
      ActivityUtils.setDataStatus(commentData, DataStatus.REMOVED);
    }
  }

  @Override
  public void onUpdate(ExoSocialActivity activity) {
    ActivityKey key = ActivityUtils.key(activity);
    ActivityData data = activityCache.get(key);
    //handles when multi-browser delete the activity
    if (ActivityUtils.getDataStatus(data).equals(DataStatus.REMOVED)) {
      return;
    }
    
    String newBodyChanged = null;
    if (activity.getBody() != null) {
      newBodyChanged = activity.getBody();
    } else {
      activity.setBody(data.getBody());
    }
    
    String newTitleChanged = null;
    if (activity.getTitle() != null) {
      newTitleChanged = activity.getTitle();
    } else {
      activity.setTitle(data.getTitle());
    }

    //process the activity
    if (newBodyChanged != null && newTitleChanged != null) {
      CommonsUtils.getService(ActivityStorageImpl.class).processActivity(activity);
    }
    
    ActivityData updated = new InMemoryActivityData(activity, DataStatus.CHANGED);
    ExoSocialActivity newA = data.build();
    DataModel model = ActivityUtils.buildModel(ActivityUtils.revision(newA), data.getId(), data.getParentId());
    //
    model.param(DataModel.ACTIVITY_BODY, newBodyChanged);
    model.param(DataModel.ACTIVITY_TITLE, newTitleChanged);
    activityCache.put(key, updated);
  }
}