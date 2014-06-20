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
package org.exoplatform.social.core.storage.memory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.IllegalClassException;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CacheType;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.core.storage.cache.FutureExoCache;
import org.exoplatform.social.core.storage.cache.FutureStreamExoCache;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.storage.cache.StreamCacheType;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.ActivitiesFixedListData;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.DataStatus;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityType;
import org.exoplatform.social.core.storage.cache.model.key.NewListActivitiesKey;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.proxy.ActivityProxyBuilder;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 23, 2014  
 */
public class InMemoryActivityStorage extends CachedActivityStorage implements ActivityStorage {
  
  private static final Log LOG = ExoLogger.getLogger(InMemoryActivityStorage.class);
  
  /** */
  private final ExoCache<ActivityKey, ActivityData> exoActivityCache;
  /** */
  private final FutureExoCache<ActivityKey, ActivityData, ServiceContext<ActivityData>> activityCache;
  /** */
  private final ExoCache<NewListActivitiesKey, ActivitiesFixedListData> exoActivitiesGraphCache;
  /** */
  protected final FutureStreamExoCache<String, NewListActivitiesKey, ActivitiesFixedListData, ServiceContext<ActivitiesFixedListData>> activitiesGraphCache;
  /** */
  private final ActivityStorageImpl storage;
  

  public InMemoryActivityStorage(final InMemoryActivityStorageImpl storage, final SocialStorageCacheService socialCacheService) {
    super(storage, socialCacheService);
    this.storage = storage;
    this.exoActivitiesGraphCache = SOCContext.instance().getActivitiesGraphCache();
    this.activitiesGraphCache = StreamCacheType.ACTIVITIES_GRAPH.createFutureStreamCache(exoActivitiesGraphCache);
    this.exoActivityCache = socialCacheService.getActivityCache();
    this.activityCache = CacheType.ACTIVITY.createFutureCache(exoActivityCache);
  }
  
  public InMemoryActivityStorageImpl getStorageImpl() {
    if (this.storage instanceof InMemoryActivityStorageImpl) {
      return (InMemoryActivityStorageImpl) storage;
    } else {
      throw new IllegalClassException("Wrong the InMemoryActivityStorageImpl class");
    }
  }
  
  @Override
  public ExoSocialActivity getParentActivity(final ExoSocialActivity comment) throws ActivityStorageException {
    String commentId = ActivityUtils.handle(comment);
    return getActivity(getActivity(commentId).getParentId());
  }
  
  @Override
  public ExoSocialActivity getActivity(final String activityId) throws ActivityStorageException {

    if (activityId == null || activityId.length() == 0) {
      return ActivityData.NULL.build();
    }
    //
    ActivityKey key = new ActivityKey(activityId);

    //
    ActivityData data = activityCache.get(
        new ServiceContext<ActivityData>() {
          public ActivityData execute() {
            try {
              ExoSocialActivity got = storage.getActivity(activityId);
              if (got != null) {
                return new ActivityData(got);
              }
              else {
                return ActivityData.NULL;
              }
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        },
        key);

    return DataStatus.REMOVED.equals(ActivityUtils.getDataStatus(data)) ? null : data.build();
  }
  
  @Override
  public ExoSocialActivity saveActivity(final Identity owner, final ExoSocialActivity activity) throws ActivityStorageException {
    List<String> mentioners = new ArrayList<String>();
    if(activity.isLazyCreated()) {
      this.getStorageImpl().createActivityInMemory(owner, activity, mentioners);
    } else {
      this.getStorageImpl().createActivity(owner, activity, mentioners);
    }
    return activity;
  }
  
  @Override
  public void saveComment(final ExoSocialActivity activity, final ExoSocialActivity comment) throws ActivityStorageException {
    if(comment.isLazyCreated()) {
      this.getStorageImpl().createCommentInMemory(activity, comment);
    } else {
      this.getStorageImpl().createComment(activity, comment);
    }
  }
  
  public void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {
    this.getStorageImpl().removeComment(activity, comment);
  }
  
  @Override
  public void deleteComment(final String activityId, final String commentId) throws ActivityStorageException {
    this.getStorageImpl().removeComment(getActivity(activityId), getActivity(commentId));
  }
  
  @Override
  public List<ExoSocialActivity> getComments(final ExoSocialActivity existingActivity, final int offset, final int limit) {
    return storage.getComments(existingActivity, offset, limit);
  }
  
  @Override
  public List<ExoSocialActivity> getActivityFeed(final Identity ownerIdentity, final int offset, final int limit) {
    final NewListActivitiesKey streamKey = NewListActivitiesKey.init(ownerIdentity.getId()).key(ActivityType.FEED);
    //
    ActivitiesFixedListData keys = activitiesGraphCache.get(
        new ServiceContext<ActivitiesFixedListData>() {
          public ActivitiesFixedListData execute() {
            List<ExoSocialActivity> got = storage.getActivityFeed(ownerIdentity, offset, limit);
            return buildStreamDataIds(streamKey, got);
          }
        },
        streamKey, offset, limit);

    //
    return buildActivitiesFromStream(keys, offset, limit);

  }
  
  @Override
  public List<ExoSocialActivity> getActivitiesOfConnections(final Identity ownerIdentity, final int offset, final int limit) {
    final NewListActivitiesKey streamKey = NewListActivitiesKey.init(ownerIdentity.getId()).key(ActivityType.CONNECTION);
    //
    ActivitiesFixedListData keys = activitiesGraphCache.get(
        new ServiceContext<ActivitiesFixedListData>() {
          public ActivitiesFixedListData execute() {
            List<ExoSocialActivity> got = storage.getActivitiesOfConnections(ownerIdentity, offset, limit);
            return buildStreamDataIds(streamKey, got);
          }
        },
        streamKey, offset, limit);

    //
    return buildActivitiesFromStream(keys, offset, limit);
    
  }
  
  @Override
  public List<ExoSocialActivity> getUserActivities(final Identity owner, final long offset, final long limit)
      throws ActivityStorageException {
    final NewListActivitiesKey streamKey = NewListActivitiesKey.init(owner.getId()).key(ActivityType.USER);
    //
    ActivitiesFixedListData keys = activitiesGraphCache.get(new ServiceContext<ActivitiesFixedListData>() {
      public ActivitiesFixedListData execute() {
        List<ExoSocialActivity> got = storage.getUserActivities(owner, offset, limit);
        return buildStreamDataIds(streamKey, got);
      }
   }, streamKey, (int)offset, (int)limit);
    
   return buildActivitiesFromStream(keys, (int)offset, (int)limit);
  }
  
  @Override
  public List<ExoSocialActivity> getUserSpacesActivities(final Identity ownerIdentity, final int offset, final int limit) {
    final NewListActivitiesKey streamKey = NewListActivitiesKey.init(ownerIdentity.getId()).key(ActivityType.SPACES);
    //
    ActivitiesFixedListData keys = activitiesGraphCache.get(new ServiceContext<ActivitiesFixedListData>() {
        public ActivitiesFixedListData execute() {
          List<ExoSocialActivity> got = storage.getUserSpacesActivities(ownerIdentity, offset, limit);
          return buildStreamDataIds(streamKey, got);
        }
     }, streamKey, (int)offset, (int)limit);
    
   return buildActivitiesFromStream(keys, (int)offset, (int)limit);
  }
  
  @Override
  public List<ExoSocialActivity> getSpaceActivities(final Identity ownerIdentity, final int offset, final int limit) {
    final NewListActivitiesKey streamKey = NewListActivitiesKey.init(ownerIdentity.getId()).key(ActivityType.SPACE);
    //
    ActivitiesFixedListData keys = activitiesGraphCache.get(new ServiceContext<ActivitiesFixedListData>() {
        public ActivitiesFixedListData execute() {
          List<ExoSocialActivity> got = storage.getSpaceActivities(ownerIdentity, offset, limit);
          return buildStreamDataIds(streamKey, got);
        }
     }, streamKey, (int)offset, (int)limit);
    
    return buildActivitiesFromStream(keys, (int)offset, (int)limit);
  }
  
  /**
   * Build the activity list from the caches Ids.
   *
   * @param data ids
   * @return activities
   */
  private List<ExoSocialActivity> buildActivitiesFromStream(ActivitiesFixedListData data, int offset, int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    List<String> ids = data.subList(offset, limit);
    for (String id : ids) {
      ExoSocialActivity a = getActivity(id);
      if (a != null) {
        if (a.isLazyCreated()) {
          activities.add(ActivityProxyBuilder.of(ExoSocialActivity.class, a));
        } else {
          activities.add(a);
        }
      }
    }
    return activities;

  }
  
  /**
   * Build the ids from the activity list.
   *
   * @return remoteId
   * @param activities activities
   * 
   */
  private ActivitiesFixedListData buildStreamDataIds(NewListActivitiesKey key, List<ExoSocialActivity> activities) {

    ActivitiesFixedListData data = this.exoActivitiesGraphCache.get(key);
    if (data == null) {
      data = new ActivitiesFixedListData(key.handle());
    }
    
    //
    for(int i = 0, len = activities.size(); i < len; i++) {
      ExoSocialActivity a = activities.get(i);
      //handle the activity is NULL
      if (a == null) {
        continue;
      }
      //
      if (!data.contains(a.getId()) && !data.contains(a.getHandle())) {
        data.insertLast(a.getId());
      }
    }
    return data;

  }
  
  @Override
  public int getNumberOfOlderOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {
    return storage.getNumberOfOlderOnUserActivities(ownerIdentity, baseActivity);
  }
  
  @Override
  public int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity) {
    NewListActivitiesKey streamKey = NewListActivitiesKey.init(ownerIdentity.getId()).key(ActivityType.FEED);
    //
    ActivitiesFixedListData data = exoActivitiesGraphCache.get(streamKey);
    
    int actualSize = super.getNumberOfActivitesOnActivityFeed(ownerIdentity);
    if (data != null) {
      return Math.max(data.size(), actualSize);
    } else {
      return actualSize;
    }
  }
  
  @Override
  public int getNumberOfUserActivities(Identity owner) throws ActivityStorageException {
    NewListActivitiesKey streamKey = NewListActivitiesKey.init(owner.getId()).key(ActivityType.USER);
    //
    ActivitiesFixedListData data = exoActivitiesGraphCache.get(streamKey);
    
    int actualSize = super.getNumberOfUserActivities(owner);
    if (data != null) {
      return Math.max(data.size(), actualSize);
    } else {
      return actualSize;
    }
  }

}
