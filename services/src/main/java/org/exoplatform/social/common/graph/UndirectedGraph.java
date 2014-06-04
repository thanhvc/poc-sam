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
 * Mar 24, 2014  
 */
public class UndirectedGraph<H, V extends Vertex<H>, E extends Edge<H, V>> extends GraphContext<H, V, E> {

  public UndirectedGraph(VertexModel<H, V, E> vertexModel, H handle) {
    super(vertexModel, handle);
  }

  @Override
  public Edge<H, V> addEdge(H label, V inVertex, V outVertex) {
    Edge<H, V> e = new Edge<H, V>(label, inVertex, outVertex);
    this.addEdge(e);
    return e;
  }
}
