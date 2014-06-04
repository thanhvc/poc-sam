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
package org.exoplatform.social.common.graph.simple;

import org.exoplatform.social.common.graph.Edge;
import org.exoplatform.social.common.graph.UndirectedGraph;
import org.exoplatform.social.common.graph.Vertex;
import org.exoplatform.social.common.graph.VertexModel;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Apr 7, 2014  
 */
public class SimpleUndirectGraph extends UndirectedGraph<Object, Vertex<Object>, Edge<Object, Vertex<Object>>>{

  public SimpleUndirectGraph(VertexModel<Object, Vertex<Object>, Edge<Object, Vertex<Object>>> vertexModel) {
    super(vertexModel, "root");
  }

}
