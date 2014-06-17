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

import java.lang.ref.SoftReference;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.storage.activity.DataModel;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.DataStatus;
import org.exoplatform.social.core.storage.cache.model.data.InMemoryActivityData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 6, 2014  
 */
public class ActivityUtils {
  private final static long DAY_MILISECONDS = 86400000;//a day = 24h x 60m x 60s x 1000 milisecond.
  
  /**
   * Gets the ActivityKey what use as cache key for activity
   * @param activity the given activity
   * @return the Activity key
   */
  public static ActivityKey key(ExoSocialActivity activity) {
    String handle = activity.isLazyCreated() ? activity.getHandle() : activity.getId();
    return new ActivityKey(handle);
  }
  
  /**
   * Gets the Handle what use as graph for activity and stream
   * @param activity the given activity
   * @return the handle
   */
  public static String handle(ExoSocialActivity activity) {
    return activity.isLazyCreated() ? activity.getHandle() : activity.getId();
  }
  
  /**
   * Gets the revision of the activity
   * @param activity the given activity
   * @return the revision
   */
  public static long revision(ExoSocialActivity activity) {
    return activity.getUpdated() != null ? activity.getUpdated().getTime() : activity.getPostedTime();
  }
  /**
   * Compares oldDate and newDate.
   * 
   * return TRUE if given newDate the after one day or more the given oldDate
   * @param oldDate
   * @param newDate
   * @return TRUE: the day after oldDate
   */
  public static boolean afterDayOrMore(long oldDate, long newDate) {
    long diffValue = newDate - oldDate;
    return diffValue >= DAY_MILISECONDS;
  }
  
  /**
   * wrap the object into soft reference.
   * @param value
   * @return
   */
  public static <T> SoftReference<T> softReference(T value) {
    return new SoftReference<T>(value);
  }
  
  /**
   * Build DataModel
   * @param activityId
   * @param parentId
   * @return
   */
  public static DataModel buildModel(long revision, String activityId, String parentId) {
    return DataModel.init(revision, activityId, parentId, null).build();
  }
  
  /**
   * Gets DataStatus from the ActivityData
   * 
   * @param data the ActivityData
   * @return The dataa status
   */
  public static DataStatus getDataStatus(ActivityData data) {
    if (data != null && data instanceof InMemoryActivityData) {
      InMemoryActivityData memoryData = (InMemoryActivityData) data;
      return memoryData.getStatus();
    }
    
    return DataStatus.PERSISTENTED;
  }
  
  /**
   * Sets the DataStatus to the ActivityData
   * 
   * @param data
   * @param status
   */
  public static void setDataStatus(ActivityData data, DataStatus status) {
    if (data != null && data instanceof InMemoryActivityData) {
      InMemoryActivityData memoryData = (InMemoryActivityData) data;
      memoryData.setStatus(status);
    }
  }

}
