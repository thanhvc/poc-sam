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

import org.exoplatform.social.core.storage.activity.Version;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityType;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.NewListActivitiesKey;
import org.exoplatform.social.core.storage.memory.ActivityUtils;
import org.exoplatform.social.core.storage.streams.AStream.Builder;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 8, 2014  
 */
public final class ActivityRefKey extends Version {
  final String identityId;
  final ActivityType type;
  final String handle;
  
  public ActivityRefKey(Builder builder, ActivityType type) {
    super(builder.activity.getUpdated().getTime());
    if (ActivityType.SPACE.equals(type)) {
      this.identityId = builder.streamOwnerIdentityId;
    } else {
      this.identityId = builder.identityId;
    }
    this.type = type;
    this.handle = ActivityUtils.handle(builder.activity);
  }
  
  /**
   * returns the key of activity reference
   * @return
   */
  public String edgeHandle() {
    return String.format("%s_%s_%s", this.identityId, this.identityId, this.type);
  }
  
  public NewListActivitiesKey listActivitiesKey() {
    return NewListActivitiesKey.init(this.identityId).key(this.type);
  }
  
  public ActivityCountKey activityCountKey() {
    return new ActivityCountKey(new IdentityKey(this.identityId), type);
  }
  
  @Override
  public String toString() {
    return "ActivityRefKey[identityId="+this.identityId+", activityId = " + this.handle + ", type = " + type +"]";
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ActivityRefKey)) {
      return false;
    }

    ActivityRefKey that = (ActivityRefKey) o;

    if (identityId != null ? !identityId.equals(that.identityId) : that.identityId != null) {
      return false;
    }
    
    if (type != null ? !type.equals(that.type) : that.type != null) {
      return false;
    }
    
    if (handle != null ? !handle.equals(that.handle) : that.handle != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + (identityId != null ? identityId.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (handle != null ? handle.hashCode() : 0);
    return result;
  }

}
