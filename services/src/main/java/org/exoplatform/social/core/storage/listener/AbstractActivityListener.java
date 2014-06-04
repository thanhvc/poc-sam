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

import org.exoplatform.social.core.listeners.Callback;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 14, 2014  
 */
public abstract class AbstractActivityListener<M> implements StreamFixedSizeListener{

  /**
   * Handles when add new an activity.
   * @param activity the activity will be created
   */
  public abstract void onAddActivity(M activity);
  
  /**
   * Handles when add new an activity.
   * @param activity the activity will be created
   * @param callback the callback has been invoked when persist the activity to storage
   */
  public abstract void onAddActivity(M activity, Callback callback);
  
  /**
   * Handles when add new a comment
   * @param activity the activity will be contained the new comment
   * @param comment the comment will be created
   */
  public abstract void onAddComment(M activity, M comment);
  
  /**
   * Handles when add new a comment
   * @param activity the activity will be contained the new comment
   * @param comment the comment will be created
   */
  public abstract void onAddComment(M activity, M comment, Callback callback);
  
  /**
   * Handles when updates the activity
   * 
   * @param activity the updated activity
   */
  public abstract void onUpdateActivity(M activity);
  
  /**
   * Handles when likes the activity
   * 
   * @param activity the updated activity
   */
  public abstract void onLikeActivity(M activity);
  
  /**
   * Handles when unlikes the activity
   * 
   * @param activity the updated activity
   */
  public abstract void onUnlikeActivity(M activity);
  
  /**
   * Handles when removes the activity
   * @param activity the deleted activity
   */
  public abstract void onRemoveActivity(M activity);
  
  /**
   * Handle when removes the comment
   * @param activity the activity will be removed the comment 
   * @param comment the deleted comment 
   */
  public abstract void onRemoveComment(M activity, M comment);
}
