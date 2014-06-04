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

import org.exoplatform.social.common.graph.simple.SimpleUndirectGraph;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.activity.DataChangeListener;
import org.exoplatform.social.core.storage.streams.AStream;
import org.exoplatform.social.core.storage.streams.AStream.WHATS_HOT;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 18, 2014  
 */
public class StreamUpdateListener<M extends ExoSocialActivity> extends DataChangeListener.Base<M> {
  /** */
  final SimpleUndirectGraph graph;
  /** */
  final SOCContext socContext;

  public StreamUpdateListener(SOCContext socContext) {
    this.socContext = socContext;
    this.graph = socContext.getActivityCacheGraph();
  }

  @Override
  public void onAddActivity(M target) {
    AStream.Builder builder = AStream.initActivity(target);
    WHATS_HOT.init().context(socContext)
                   .feed(builder)
                   .connection(builder)
                   .owner(builder)
                   .myspaces(builder)
                   .space(builder).doExecute();
  }

  @Override
  public void onRemoveActivity(M target) {
    
  }

  @Override
  public void onUpdate(M target) {
    AStream.Builder builder = AStream.initActivity(target);
    WHATS_HOT.init().context(socContext)
                   .feed(builder)
                   .connection(builder)
                   .owner(builder)
                   .myspaces(builder)
                   .space(builder).doExecute();
  }
  
  
}
