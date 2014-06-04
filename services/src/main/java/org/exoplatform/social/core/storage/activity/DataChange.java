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
public abstract class DataChange<M> {
  final M target;
  
  private DataChange(M target) throws IllegalArgumentException {
    if (target == null) {
      throw new IllegalArgumentException("The target must not be null.");
    }
    
    this.target = target;
  }
  
  /**
   * Dispatch the data change to the listener for handling
   * 
   * @param listener the listener handling
   */
  protected abstract void dispatch(DataChangeListener<M> listener);
  
  public static final class AddActivity<M> extends DataChange<M> {
    
    public AddActivity(M model) {
      super(model);
    }

    @Override
    protected void dispatch(DataChangeListener<M> listener) {
      listener.onAddActivity(this.target);
    }
    
    @Override
    public String toString() {
      return "DataChange.AddActivity[target:" + target + "]";
    }
    
  }
  
  public static final class AddComment<M> extends DataChange<M> {
    final M parent;
    
    public AddComment(M parent, M comment) {
      super(comment);
      this.parent = parent;
    }

    @Override
    protected void dispatch(DataChangeListener<M> listener) {
      listener.onAddComment(this.parent, this.target);
    }
    
    @Override
    public String toString() {
      return "DataChange.AddComment[parent: " + parent + " comment: " + target + " ]";
    }
    
  }
  
  public static final class Like<M> extends DataChange<M> {

    public Like(M model) {
      super(model);
    }

    @Override
    protected void dispatch(DataChangeListener<M> listener) {
      listener.onLike(this.target);
    }

    @Override
    public String toString() {
      return "DataChange.Like[target: " + target + " ]";
    }
  }
  
  public static final class Unlike<M> extends DataChange<M> {

    public Unlike(M model) {
      super(model);
    }

    @Override
    protected void dispatch(DataChangeListener<M> listener) {
      listener.onLike(this.target);
    }

    @Override
    public String toString() {
      return "DataChange.Unlike[target: " + target + " ]";
    }
  }
  
  public static final class RemoveActivity<M> extends DataChange<M> {

    public RemoveActivity(M model) {
      super(model);
    }

    @Override
    protected void dispatch(DataChangeListener<M> listener) {
      listener.onRemoveActivity(this.target);
    }

    @Override
    public String toString() {
      return "DataChange.Remove[target: " + target + " ]";
    }

  }
  
  public static final class RemoveComment<M> extends DataChange<M> {

    final M parent;
    public RemoveComment(M parent, M comment) {
      super(comment);
      this.parent = parent;
    }

    @Override
    protected void dispatch(DataChangeListener<M> listener) {
      listener.onRemoveComment(this.parent, this.target);
    }

    @Override
    public String toString() {
      return "DataChange.RemoveComment[parent: " + parent + ", target " + target + " ]";
    }

  }
  
  public static final class Update<M> extends DataChange<M> {

    public Update(M model) {
      super(model);
    }

    @Override
    protected void dispatch(DataChangeListener<M> listener) {
      listener.onUpdate(this.target);
    }

    @Override
    public String toString() {
      return "DataChange.Update[target:" + target + "]";
    }
  }

}
