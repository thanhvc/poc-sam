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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.common.graph.simple.SimpleUndirectGraph;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.activity.DataChangeListener;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.storage.streams.AStream;
import org.exoplatform.social.core.storage.streams.AStream.MENTIONER;
import org.exoplatform.social.core.storage.streams.AStream.REMOVER;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 16, 2014  
 */
public class MentionerActivityListener<M extends ExoSocialActivity> extends DataChangeListener.Base<M> {

  /** */
  final SimpleUndirectGraph graph;
  /** */
  final SOCContext socContext;
  /** */
  private RelationshipStorage relationship;
  /** */
  private IdentityStorage identityStorage;
  
  public MentionerActivityListener(SOCContext socContext) {
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
    List<String> mentioners = StorageUtils.getIdentityIds(target.getMentionedIds());
    AStream.Builder builder;
    for(String identityId : mentioners) {
      Identity identity = getIdentity().findIdentityById(identityId);
      
      builder = AStream.initActivity(identity.getId(), target);
      MENTIONER.init().context(socContext)
                     .feed(builder)
                     .owner(builder)
                     .doExecute();
    }
  }

  @Override
  public void onRemoveActivity(M target) {
    List<String> mentioners = StorageUtils.getIdentityIds(target.getMentionedIds());
    List<String> parentMentioners = new ArrayList<String>(1);
    ExoSocialActivity parent = null;
    if (target.isComment()) {
      ActivityKey parentKey = new ActivityKey(target.getParentId());
      parent = socContext.getActivityCache().get(parentKey).build();
      parentMentioners = StorageUtils.getIdentityIds(parent.getMentionedIds());
    }

    AStream.Builder builder;
    for (String identityId : mentioners) {

      Identity identity = getIdentity().findIdentityById(identityId);
      //you still have been mentioned by the parent >> don't remove 
      if (parentMentioners.contains(identityId)) {
        continue;
      }
      
      //you have relationship with streamOwner, don't remove
      if (target.isComment()) {
        Identity streamOwner = getIdentity().findIdentityById(parent.getUserId());
        if (getRelationship().getRelationship(identity, streamOwner) != null) {
          continue;
        }
        
        builder = AStream.initActivity(identity.getId(), parent);
      } else {
        builder = AStream.initActivity(identity.getId(), target);
      }

      REMOVER.init()
             .context(socContext)
             .feed(builder)
             .connection(builder)
             .owner(builder)
             .doExecute();
    }

  }

  @Override
  public void onUpdate(M target) {
    
  }
}
