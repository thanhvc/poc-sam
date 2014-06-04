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

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.storage.streams.AStream.Builder;
/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 8, 2014  
 */
public class ActivityRefContext {
  
  public enum PostType {
    ACTIVITY,
    COMMENT
  }
  
  final boolean isUserOwner;
  final String identityId;
  final ExoSocialActivity activity;
  final ExoSocialActivity comment;
  final PostType type;
  
  
  public ActivityRefContext(Builder builder) {
    this.isUserOwner = builder.isUserOwner;
    this.identityId = builder.identityId;
    this.activity = builder.activity;
    this.comment = builder.comment;
    this.type = builder.type;
  }
}
