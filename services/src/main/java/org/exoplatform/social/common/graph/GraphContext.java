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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class GraphContext<H, V extends Vertex<H>, E extends Edge<H, V>> {
  public enum Scope {
    ALL,
    SINGLE
  }
  /** */
  final EdgeContext<H, V, E> edgeContext;
  
  /** */
  final VertexContext<H, V, E> rootVertex;
  
  /** */
  final VertexModel<H, V, E> vertexModel;

  public GraphContext(VertexModel<H, V, E> vertexModel, H handle) {
    this.vertexModel = vertexModel;
    this.edgeContext = new EdgeContext<H, V, E>();
    this.rootVertex = new VertexContext<H, V, E>(this, handle);
    
  }
  
  public abstract Edge<H, V> addEdge(H label, V inVertex, V outVertex);
  
  /**
   * Adds new the vertex into the graph at the last positions
   * 
   * @param vertex the vertex
   * @return the context
   */
  public VertexContext<H, V, E> addVertex(V vertex) {
    return this.rootVertex.insert(vertex);
  }
  
  /**
   * Adds the vertex given the handle
   * @param handle
   * @return
   */
  public V addVertex(H handle) {
    return this.rootVertex.add(handle).vertex;
  }
  
  /**
   * Removes the vertex given the handle
   * @param handle the removed vertex's handle
   * @param scope ALL: remove it and all of its edges
   *            SINGLE: remove it if its's edges size == zero  
   */
  public void removeVertex(Class<?> type, H handle, Scope scope) {
    switch(scope) {
    case ALL:
      if (this.rootVertex.removeVertex(handle)) {
        List<Edge<H, V>> list = this.getEdges(handle);
        for (Edge<H, V> e : list) {
          this.removeEdge(e.getLabel());
        }
      }
      break;
    case SINGLE:
      List<Edge<H, V>> list = this.getEdges(handle);
      if (list.size() == 0) {
        this.rootVertex.removeVertex(handle);
      }
      break;
    };
    
  }

  
  /**
   * 
   * @param e
   */
  public void addEdge(Edge<H, V> e) {
    this.edgeContext.add(e);
  }
  
  /**
   * 
   * @param label
   * @return
   */
  public Edge<H, V> getEdge(String label) {
    return edgeContext.get(label);
  }
  
  
  /**
   * Removes the edge by label
   * 
   * @param name the edge's label or vertex's name
   * @param label the label of edge
   */
  public void removeEdge(H name) {
    edgeContext.remove(name);
  }
  
  /**
   * Removes the edge by given inVertex and outVertex
   * 
   * @param inVertex the in vertex
   * @param outVertex the out vertex
   */
  public void removeEdge(V inVertex, V outVertex) {
    edgeContext.remove(inVertex.getHandle(), outVertex.getHandle());
  }
  
  /**
   * Gets the vertex by give name
   * 
   * @param name the vertex's name
   * @return the vertex
   */
  public <T> V getVertex(H name) {
    return this.rootVertex.getVertex(name);
  }
  
  /**
   * Returns TRUE if the vertex has been added in the graph
   * @param name the specified handle name
   * @return TRUE/FALSE
   */
  public boolean contains(H name) {
    return this.rootVertex.contains(name);
  }
  
  /**
   * Gets the number of the vertices in the graph
   * 
   * @return the number of vertex
   */
  public int getVertexSize() {
    return this.rootVertex.getCount();
  }
  
  /**
   * Gets these adjacent of the give vertex
   * @param vertex the vertex
   * @return these adjacent of the vertex 
   */
  public Iterator<V> getAdjacents(V vertex) {
    return getAdjacents(vertex.getHandle()).iterator();
  }
  
  /**
   * Gets these adjacent size of the give vertex
   * @param vertex the vertex
   * @return the size of these adjacent the vertex
   */
  public int getAdjacentsSize(V vertex) {
    return getAdjacentsSize(vertex.getHandle());
  }
  
  /**
   * Gets these adjacent size of the give vertex's name
   * @param vertexName the vertex's name
   * @return the size of these adjacent the vertex
   */
  public int getAdjacentsSize(Object vertexName) {
    
    Collection<V> list = this.edgeContext.getAdjacents(vertexName);
    return list != null ? list.size() : 0;
  }
  
  /**
   * Gets these adjacent of the give vertex's name
   * 
   * @param vertexName the vertex's name
   * @return these adjacent of the vertex 
   */
  public List<V> getAdjacents(Object vertexName) {
    return this.edgeContext.getAdjacents(vertexName);
  }
  
  /**
   * Gets all of edges what connected the given vertex's name to others
   * 
   * @param vertexName the vertex's name
   * @return the list of edges
   */
  public List<Edge<H, V>> getEdges(Object vertexName) {
    return this.edgeContext.getEdges(vertexName);
  }
  
  /**
   * Gets all of edges what connected the given vertex to others
   * 
   * @param vertex the vertex
   * @return the list of edges
   */
  public List<Edge<H, V>> getEdges(V vertex) {
    return this.getEdges(vertex.getHandle());
  }
  
  /**
   * Gets the number of the edges in the graph.
   * @return the number of edges
   */
  public int getEdgeSize() {
    return this.edgeContext.getEdgeSize();
  }
  
  public ListIterator<E> getListIterator() {
    //return this.edgeContext.ge
    return null;
  }

  /**
   * Clear all of things
   */
  public void clear() {
    this.rootVertex.clear();
    this.edgeContext.clear();

  }
  
  
}
