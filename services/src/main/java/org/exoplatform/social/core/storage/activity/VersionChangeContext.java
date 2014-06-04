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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 9, 2014  
 */
public final class VersionChangeContext<T extends Version> {

  /** */
  private Map<T, Long> changes;
  
  /**
   * Constructor the DataContext to update 
   * the data model what hold by caching
   * @param listener
   */
  public VersionChangeContext() {}
  
  public boolean hasChanges() {
    return changes != null && changes.size() > 0;
  }
  
  private boolean addChange(T change) {
    if (changes == null) {
      changes = Maps.newHashMap();
    }
    
    Long value = this.changes.get(change);
    
    if (value != null) {
      if (value.longValue() < change.revision) {
        this.changes.put(change, change.revision);
        return true;
      }
    } else {
      this.changes.put(change, change.revision);
    }
    
    return false;
  }
  
  private void removeChange(T change) {
    if (changes == null) {
      return;
    }
    
    this.changes.remove(change);
  }
  
  /**
   * Adds the new model
   * @param target
   */
  public void add(T target) {
    addChange(target);
  }
  
  /**
   * Adds the models
   * @param target
   */
  public void add(T...targets) { 
    for(int i = 0, len = targets.length; i< len; i++) {
      addChange(targets[i]);
    }
    
  }

  /**
   * Removes the model
   * @param target
   */
  public void remove(T target) {
    removeChange(target);
  }
  /**
   * Removes the models
   * @param target
   */
  public void remove(T...targets) {
    for(int i = 0, len = targets.length; i< len; i++) {
      removeChange(targets[i]);
    }
  }
  
  /**
   * Gets revision by given key.
   * 
   * @param key the given key
   * @return the revision
   */
  public Long getRevision(T key) {
    if (changes == null) {
      return new Long(0);
    }
    return changes.get(key);
  }
  
  @SuppressWarnings("unchecked")
  public Map<T, Long> getChanges() {
    return changes == null ? Collections.EMPTY_MAP : this.changes;
  }
  
  public int getChangesSize() {
    return changes == null ? 0 : this.changes.size();
  }
  
  public Set<T> peekChanges() {
    if (hasChanges()) {
      return changes.keySet();
    } else {
      return Collections.emptySet();
    }
  }

  public Set<T> popChanges() {
    if (hasChanges()) {
      Set<T> tmp = changes.keySet();
      changes = null;
      return tmp;
    } else {
      return Collections.emptySet();
    }
  }
  
  public void clearChanges() {
    if (changes != null) {
      changes.clear();
    }
    
  }
}
