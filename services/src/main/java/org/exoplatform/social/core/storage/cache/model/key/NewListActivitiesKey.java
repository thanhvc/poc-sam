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
package org.exoplatform.social.core.storage.cache.model.key;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 15, 2014  
 */
public class NewListActivitiesKey extends ScopeCacheKey {

  /**  */
  private String id;
  
  /** */
  private ActivityType type;
  
  /**
   * The id is IdentityId for Stream or ActivityId for comment cache key
   * @param id
   * @return
   */
  public static Builder init(String id) {
    return new Builder(id);
  }
  
  private NewListActivitiesKey(Builder builder, ActivityType type) {
    this.id = builder.id;
    this.type = type;
  }
  
  /**
   * builds label of vertex in graph
   * 
   * @return
   */
  public String handle() {
    return id + "_" + this.type.name();
  }
  
  public static class Builder {
    public final String id;
    public Builder(String id) {
      this.id = id;
    }

    public NewListActivitiesKey key(ActivityType type) {
      return new NewListActivitiesKey(this, type);
    }
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NewListActivitiesKey)) {
      return false;
    }
    
    NewListActivitiesKey that = (NewListActivitiesKey) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    
    if (type != null ? !type.equals(that.type) : that.type != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
  
  @Override
  public String toString() {
    return "NewListActivitiesKey[id: " + this.id + ",type: " + this.type.toString() + " ]";
  }
}
