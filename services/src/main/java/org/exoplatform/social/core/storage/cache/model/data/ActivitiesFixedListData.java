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
package org.exoplatform.social.core.storage.cache.model.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.social.core.storage.listener.StreamFixedSizeListener;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 15, 2014  
 */
@SuppressWarnings("serial")
public class ActivitiesFixedListData extends AbstractFixedSizeListData<String> {
  /** */
  final static int FIXED_SIZE = 100;
  /** */
  final int maxSize;
  /** */
  final LinkedList<String> dataKeys;
  /* **/
  final String myKey;
  
  public ActivitiesFixedListData(String key) {
    this(key, FIXED_SIZE);
  }
  
  /**
   * Constructors the empty instance.
   */
  public ActivitiesFixedListData(String key, int fixedSize) {
    dataKeys = new LinkedList<String>();
    this.maxSize = fixedSize;
    this.myKey = key;
  }
  
  
  @Override
  public boolean contains(String key) {
    return dataKeys.contains(key);
  }
  
  
  @Override
  public void insertFirst(String id, StreamFixedSizeListener listener) {
    synchronized (dataKeys) {
      int position = this.dataKeys.indexOf(id);
      if (position > 0) {
        this.dataKeys.remove(position);
        this.dataKeys.offerFirst(id);
      } else if (position == -1) {
        this.dataKeys.offerFirst(id);
      }
      
      maintainFixedSize(listener);
    }
  }
  
  /**
   * Handles the case when the activity keys size over fixed size
   * 
   * @param listener the listener to handle
   */
  private void maintainFixedSize(StreamFixedSizeListener listener) {
    if (dataKeys.size() > maxSize) {
      String outV = dataKeys.removeLast();
      if (listener != null) {
        listener.update(outV, myKey);
      }
    }
  }
 
  @Override
  public boolean canAddMore() {
    return size() < this.maxSize;
  }
  
  @Override
  public void insertFirst(Collection<String> keys) {
    for(String key : keys) {
      insertFirst(key, null);
    }
  }
  
  @Override
  public void insertLast(String id) {
    synchronized (dataKeys) {
      if (canAddMore()) {
        int position = this.dataKeys.indexOf(id);
        if (position > -1 && position < dataKeys.size() - 1) {
          this.dataKeys.remove(position);
        }
        this.dataKeys.offerLast(id);
      }
    }
  }
  
  @Override
  public void insertLast(Collection<String> keys) {
    for(String key : keys) {
      insertLast(key);
    }
  }

  @Override
  public List<String> subList(int offset, int limit) {
    synchronized (dataKeys) {
      int to = Math.min(this.dataKeys.size(), offset + limit);
      return this.dataKeys.subList(offset, to);
    }
  }
  
  /**
   * Removes the id
   */
  public void remove(String key) {
    this.dataKeys.remove(key);
  }
  
  /**
   * Returns size of the activities id
   * @return
   */
  @Override
  public int size() {
    return dataKeys.size();
  }
}
