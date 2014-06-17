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
package org.exoplatform.social.common.graph;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 22, 2014  
 */
@SuppressWarnings("serial")
public class VertexContext<H, V extends Vertex<H>, E extends Edge<H, V>> extends ConcurrentHashMap<H, VertexContext<H, V, E>> {
  /** */
  final H handle;
  
  /** the node is wrapped by vertex context*/
  final V vertex;
  
  final Class<?> keyType;
  
  /** */
  final GraphContext<H, V, E> graph;
  
  public VertexContext(GraphContext<H, V, E> graph, V vertex) {
    this.handle = vertex.getHandle();
    this.keyType = this.handle.getClass();
    this.graph = graph;
    this.vertex = vertex;
  }
  
  public VertexContext(GraphContext<H, V, E> graph, H handle) {
    this.keyType = handle.getClass();
    this.handle = handle;
    this.graph = graph;
    this.vertex = graph.vertexModel.create(this);
  }
  
  public H getHandle() {
    return this.handle;
  }
  
  /**
   * Inserts the NodeData into the last position
   * @param data
   * @return
   */
  public VertexContext<H, V, E> insert(V vertex) {
    if (vertex == null) {
      throw new NullPointerException("Vertex must not be null.");
    }
    
    //
    VertexContext<H, V, E> context = new VertexContext<H, V, E>(graph, vertex);
    put(vertex.handle, context);
    return context;
  }
  
  /**
   * Gets number of vertex
   * @return
   */
  public int getCount() {
    return size();
  }
  /**
   * Gets the vertex by handle return NULL if it's not existing
   * @param handle
   * @return
   * @throws NullPointerException
   */
  public <T> V getVertex(H handle) throws NullPointerException {
    VertexContext<H, V, E> found = this.get(handle);
    return found == null ? null : found.vertex;
  }
  
  /**
   * Remove the node by specified handle
   * @param name
   * @return
   * @throws NullPointerException
   * @throws IllegalArgumentException
   * @throws IllegalStateException
   */
  public <T> boolean removeVertex(Object name) throws NullPointerException, IllegalArgumentException, IllegalStateException {
    VertexContext<H, V, E> vertex = get(name);
    if(vertex == null) {
      return false;
    }
    
    return removeVertex(vertex);
  }
  
  /**
   * Removes the node
   * @return
   * @throws IllegalStateException
   */
  public boolean removeVertex(VertexContext<H, V, E> vertex) throws IllegalStateException {
    try {
      this.remove(vertex.getHandle());
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }
  
  /**
   * Adds the vertex given the name
   * @param handle the handle of the vertex
   * 
   * @return the context
   * 
   * @throws NullPointerException
   * @throws IndexOutOfBoundsException
   * @throws IllegalStateException
   */
  public <T> VertexContext<H, V, E> add(H handle) throws NullPointerException, IndexOutOfBoundsException, IllegalStateException {
    if (handle == null) {
      throw new NullPointerException("No null name accepted");
    }
    
    VertexContext<H, V, E> context = new VertexContext<H, V, E>(graph, handle);
    //
    put(handle, context);  
    return context;
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VertexContext)) {
      return false;
    }
    
    VertexContext<?,?,?> that = (VertexContext<?, ?, ?>) o;

    if (handle != null ? !handle.equals(that.handle) : that.handle != null) {
      return false;
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + (handle != null ? handle.hashCode() : 0);
    return result;
  }
  
  @Override
  public String toString() {
    return "VertexContext[handle: " + keyType.cast(handle) + " ]";
  }
  
}
