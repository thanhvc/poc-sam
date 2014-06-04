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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.exoplatform.social.core.storage.listener.StreamFixedSizeListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 15, 2014  
 */
@SuppressWarnings("serial")
public abstract class AbstractFixedSizeListData<T> implements Serializable {

  /**
   * Returns the sub list from this list extract between 
   * the specified offset and limit
   * 
   * @param offset the offset
   * @param limit the limit
   * @return the sub list
   */
  public abstract List<T> subList(int offset, int limit);
  
  /**
   * Checks the key whether contained in this or not
   * @param key
   * @return
   */
  public abstract boolean contains(T key);
  
  /**
   * Insert the key at the top of list
   * @param key the key
   * @param listener the listener
   */
  public abstract void insertFirst(T key, StreamFixedSizeListener listener);
  
  /**
   * Inserts the keys on the top of this list
   * @param keys
   */
  public abstract void insertFirst(Collection<T> keys);
  
  /**
   * Insert the keys at the bottom of list
   * @param keys the keys
   * @param listener the listener
   */
  public abstract void insertLast(Collection<T> keys);
  /**
   * Insert the key at the bottom of list
   * @param key the key id
   * @param listener the listener
   */
  public abstract void insertLast(T key);
  
  /**
   * Determine can add more activity id on this
   * makes sure it is not over fixed size
   * 
   * @return TRUE can add more Otherwise FALSE
   */
  public abstract boolean canAddMore();
  /**
   * returns the size of list
   * @return
   */
  public abstract int size();
}
