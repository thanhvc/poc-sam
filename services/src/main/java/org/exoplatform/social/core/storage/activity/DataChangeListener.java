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

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 12, 2014  
 */
public interface DataChangeListener<M> {
  /**
   * An activity is added
   * @param activity the added model
   */
  void onAddActivity(M activity);
  
  /**
   * A comment is added
   * 
   * @param activity the parent activity
   * @param comment the added comment
   */
  void onAddComment(M activity, M comment);
  
  /**
   * An activity is liked
   * @param activity the liked model
   */
  void onLike(M activity);
  
  /**
   * An activity is disliked
   * @param activity the disliked model
   */
  void onUnlike(M activity);
  
  /**
   * A model is removed
   * @param activity
   */
  void onRemoveActivity(M activity);
  
  /**
   * A comment is removed
   * @param activity
   * @param comment
   */
  void onRemoveComment(M activity, M comment);
  
  /**
   * A model is updated
   * @param target
   */
  void onUpdate(M target);
  
  public class Base<M> implements DataChangeListener<M> {

    @Override
    public void onAddActivity(M target) {
      
    }

    @Override
    public void onRemoveActivity(M target) {
      
    }

    @Override
    public void onUpdate(M target) {
      
    }

    @Override
    public void onAddComment(M activity, M comment) {}

    @Override
    public void onLike(M activity) {}

    @Override
    public void onUnlike(M activity) {}

    @Override
    public void onRemoveComment(M activity, M comment) {}
  }
  

}
