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

import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.common.graph.simple.SimpleUndirectGraph;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.activity.DataChangeListener;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.streams.AStream;
import org.exoplatform.social.core.storage.streams.AStream.RELATIONSHIP;
import org.exoplatform.social.core.storage.streams.AStream.WHATS_HOT;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 16, 2014  
 */
public class RelationshipActivityListener<M extends ExoSocialActivity> extends DataChangeListener.Base<M> {

  /** */
  final SimpleUndirectGraph graph;
  /** */
  final SOCContext socContext;
  /** */
  private RelationshipStorage relationship;
  /** */
  private IdentityStorage identityStorage;
  
  public RelationshipActivityListener(SOCContext socContext) {
    this.socContext = socContext;
    this.graph = socContext.getActivityCacheGraph();
  }
  
  private RelationshipStorage getRelationship() {
    if (relationship == null) {
      relationship = CommonsUtils.getService(RelationshipStorage.class);
    }
    return relationship;
  }
  
  private IdentityStorage getIdentity() {
    if (identityStorage == null) {
      identityStorage = CommonsUtils.getService(IdentityStorage.class);
    }
    return identityStorage;
  }
  
  @Override
  public void onAddActivity(M target) {
    if (ActivityStream.Type.SPACE.equals(target.getActivityStream().getType())) {
      return;
    }
    Identity poster = getIdentity().findIdentityById(target.getPosterId());
    List<Identity> relationships = getRelationship().getConnections(poster);
    
    AStream.Builder builder;
    for(Identity identity : relationships) {
      builder = AStream.initActivity(identity.getId(), target);
      RELATIONSHIP.init().context(socContext)
                     .feed(builder)
                     .connection(builder).doExecute();
    }
  }
  
  public void onAddComment(M activity, M comment) {
    if (ActivityStream.Type.SPACE.equals(activity.getActivityStream().getType())) {
      return;
    }
    Identity poster = getIdentity().findIdentityById(activity.getPosterId());
    List<Identity> relationships = getRelationship().getConnections(poster);
    
    AStream.Builder builder;
    for(Identity identity : relationships) {
      builder = AStream.initActivity(identity.getId(), activity);
      WHATS_HOT.init().context(socContext)
                     .feed(builder)
                     .connection(builder).doExecute();
    }
  }

  @Override
  public void onRemoveActivity(M target) {
    
  }

  @Override
  public void onUpdate(M target) {
    
  }

}
