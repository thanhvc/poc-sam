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
package org.exoplatform.social.core.storage.activity;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.social.core.listeners.Callback;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 12, 2014  
 */
public class DataModel extends Version {
  
  public static String ACTIVITY_BODY = "activity_body";
  public static String ACTIVITY_TITLE = "activity_title";
  /** uses the revision of the target as handle*/
  private final String handle;
  /** uses the revision of the parent's target as handle*/
  private final String parentId;
  private final boolean isLazyCreated;
  
  private final Callback callback;
  
  private Map<String, String> temporaryParams;

  public static Builder init(long revision, String handle, String parentId, Callback callback) {
    return new Builder(revision, handle, parentId, callback);
  }
  
  public static Builder init(long revision, String handle, Callback callback) {
    return init(revision, handle, null, callback);
  }
  
  public static Builder init(long revision, String handle) {
    return init(revision, handle, null, null);
  }

  public DataModel(Builder builder) {
    super(builder.revision);
    this.handle = builder.handle;
    this.parentId = builder.parentId;
    this.callback = builder.callback;
    this.isLazyCreated = builder.isLazyCreated;
    
  }
  
  /**
   * returns the callback of data model
   * @return
   */
  public Callback getCallback() {
    return callback;
  }
  
  public String getHandle() {
    return handle;
  }

  public String getParentId() {
    return parentId;
  }
  /**
   * returns the value to know the activity is lazy created or not
   * @return
   */
  public boolean isLazyCreated() {
    return isLazyCreated;
  }
  /**
   * keeps the parameters with data model
   * @param key
   * @param value
   */
  public void param(String key, String value) {
    if (temporaryParams == null) {
      temporaryParams = new HashMap<String, String>();
    }
    
    this.temporaryParams.put(key, value);
  }
  
  public boolean contains(String key) {
    return temporaryParams != null ? this.temporaryParams.containsKey(key) : false;
  }
  
  public String paramValue(String key) {
    return temporaryParams != null ? this.temporaryParams.get(key) : null;
  }
  
  public static class Builder {
    public final String handle;
    public final String parentId;
    public final Callback callback;
    public boolean isLazyCreated = false;
    public final Long revision;
    
    public Builder(Long revision, String handle, String parentId, Callback callback) {
      this.handle = handle;
      this.parentId = parentId;
      this.callback = callback;
      this.revision = revision;
    }
    
    public Builder lazyCreated(boolean isLazyCreated) {
      this.isLazyCreated = isLazyCreated;
      return this;
    }
    
    public DataModel build() {
      return new DataModel(this);
    }
  }
}
