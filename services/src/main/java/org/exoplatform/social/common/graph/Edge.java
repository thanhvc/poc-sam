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

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 23, 2014  
 */
public class Edge<H, V extends Vertex<H>> implements Element {
  
  /** */
  final H handle;
  
  /** */
  final Class<?> keyType;
  
  /** */
  final H label;
  
  /** */
  final ElementType type;
  
  /** */
  final V inVertex;
  
  /** */
  final V outVertex;
  
  public Edge(H label, V inVertex, V outVertex) {
    this.handle = label;
    this.keyType = this.handle.getClass();
    this.label = label;
    this.inVertex = inVertex;
    this.outVertex = outVertex;
    this.type = ElementType.EDGE;
  }
  
  public Edge(H handle, H label, V inVertex, V outVertex) {
    this.handle = handle;
    this.keyType = this.handle.getClass();
    this.label = label;
    this.inVertex = inVertex;
    this.outVertex = outVertex;
    this.type = ElementType.EDGE;
  }

  @Override
  public Object getHandle() {
    return this.handle;
  }
  
  @Override
  public Class<?> getHandleType() {
    return null;
  }
  
  /**
   * Gets the label of the edge
   * @return
   */
  public H getLabel() {
    return this.label;
  }

  @Override
  public ElementType getType() {
    return this.type;
  }
  
  /**
   * Gets the vertex what connected by given the vertex
   * and connected by this edge
   * 
   * @param inVertex the given vertex
   * @return
   */
  public V getOutVertex(Object name) {
    return name.equals(this.inVertex.getHandle()) ? this.outVertex : this.inVertex;
  }
  
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Edge)) {
      return false;
    }

    Edge<?, ?> that = (Edge<?, ?>) o;

    if (label != null ? !label.equals(that.label) : that.label != null) {
      return false;
    }
    return true;
  }
  
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (label != null ? label.hashCode() : 0);
    return result;
  }
  
  @Override
  public String toString() {
    return "Edge[label= " + this.label + ", inVertex =" + this.inVertex.getHandle() + ", outVertex =" + this.outVertex.getHandle() +"]";
  }

  

}
