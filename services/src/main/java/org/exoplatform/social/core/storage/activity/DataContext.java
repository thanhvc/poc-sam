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

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;

import org.exoplatform.social.core.storage.memory.ActivityUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 12, 2014  
 */
public final class DataContext<M> {
  /** */
  private DataChangeQueue<M> changes;
  
  /**
   * Constructor the DataContext to update 
   * the data model what hold by caching
   * @param listener
   */
  public DataContext() {
  }
  
  public boolean hasChanges() {
    return changes != null && changes.size() > 0;
  }
  
  public int getChangesSize() {
    return changes != null ? changes.size() : 0;
  }
  
  private void addChange(DataChange<M> change) {
    if (changes == null) {
      changes = new DataChangeQueue<M>();
    }
    //
    changes.addLast(ActivityUtils.softReference(change));
  }
  
  /**
   * Adds the new activity
   * @param activity
   */
  public void addActivity(M activity) {
    addChange(new DataChange.AddActivity<M>(activity));
  }
  
  /**
   * Adds the new comment into parent
   * @param parent the activity will put the comment
   * @param comment
   */
  public void addComment(M parent, M comment) {
    addChange(new DataChange.AddComment<M>(parent, comment));
  }
  
  /**
   * Removes the activity
   * @param target
   */
  public void removeActivity(M target) {
    addChange(new DataChange.RemoveActivity<M>(target));
  }
  

  /**
   * Removes the comment
   * @param parent the activity will remove the comment
   * @param comment
   */
  public void removeComment(M parent, M comment) {
    addChange(new DataChange.RemoveComment<M>(parent, comment));
  }
  
  /**
   * Likes the model
   * @param activity
   */
  public void like(M activity) {
    addChange(new DataChange.Like<M>(activity));
  }
  
  /**
   * Unlike the model
   * @param activity
   */
  public void unLike(M activity) {
    addChange(new DataChange.Unlike<M>(activity));
  }
  
  /**
   * Updates the model
   * @param activity
   */
  public void update(M activity) {
    addChange(new DataChange.Update<M>(activity));
  }
  
  public DataChangeQueue<M> getChanges() {
    return changes == null ? new DataChangeQueue<M>() : changes;
  }
  
  public List<SoftReference<DataChange<M>>> peekChanges() {
    if (hasChanges()) {
      return changes;
    } else {
      return Collections.emptyList();
    }
  }
  
  public DataChangeQueue<M> popChanges() {
    if (hasChanges()) {
      DataChangeQueue<M> tmp = changes;
      changes = null;
      return tmp;
    } else {
      return null;
    }
  }
  
  public M pop(M activity) {
    int pos = changes.indexOf(activity);
    if (pos >= 0) {
      DataChange<M> change = changes.get(pos).get();
      if (change != null) {
        M result = change.target;
        changes.remove(pos);
        return result;
      }
    }
    return null;
  }
  
  public void clearAll() {
    if (changes != null) {
      this.changes.clear();
    }
  }

}
