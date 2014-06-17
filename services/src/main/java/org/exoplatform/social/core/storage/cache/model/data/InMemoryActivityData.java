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
package org.exoplatform.social.core.storage.cache.model.data;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 9, 2014  
 */
@SuppressWarnings("serial")
public class InMemoryActivityData extends ActivityData {
  private DataStatus status;

  public InMemoryActivityData(ExoSocialActivity activity) {
    super(activity);
  }
  
  public InMemoryActivityData(final ExoSocialActivity activity, DataStatus status) {
    super(activity);
    this.status = status;
  }
  
  public DataStatus getStatus() {
    return status;
  }

  public void setStatus(DataStatus status) {
    this.status = status;
  }
}
