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
package org.exoplatform.social.core.storage.streams;

import java.util.Set;

import org.exoplatform.social.core.storage.activity.VersionChangeContext;
import org.exoplatform.social.core.storage.streams.AStream.Builder;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 11, 2014  
 */
public class AStreamVersion {

  /** */
  private final VersionChangeContext<ActivityRefKey> versionContext;
  
  public AStreamVersion() {
    versionContext = new VersionChangeContext<ActivityRefKey>();
  }
  
  public void clearAll() {
    versionContext.clearChanges();
  }
  
  public VersionChangeContext<ActivityRefKey> getVersionContext() {
    return versionContext;
  }
  
  public void owner(Builder builder) {
    if (builder.isUserOwner) {
      versionContext.add(builder.feedRefKey(), builder.ownerRefKey());
    } else {
      versionContext.add(builder.feedRefKey(), builder.mySpacesRefKey());
    }
  }
  
  public void connecter(Builder builder) {
    if (builder.isUserOwner) {
      versionContext.add(builder.feedRefKey(), builder.connectionsRefKey());
    }
  }
  
  public void remove(Builder builder) {
    if (builder.isUserOwner) {
      versionContext.remove(builder.feedRefKey(), builder.ownerRefKey(), builder.connectionsRefKey());
    } else {
      versionContext.remove(builder.feedRefKey(), builder.mySpacesRefKey());
    }
  }
  
  public int getChangesSize() {
    return versionContext.getChangesSize();
  }
  
  
  public Set<ActivityRefKey> popChanges() {
    return versionContext.popChanges();
  }
  
}
