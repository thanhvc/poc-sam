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
package org.exoplatform.social.core.storage.persister;

import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.impl.ProcessorContextImpl;
import org.exoplatform.social.core.storage.activity.DataChangeQueue;
import org.exoplatform.social.core.storage.streams.StreamProcessContext;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 29, 2014  
 */
public class PersisterProcessContext extends ProcessorContextImpl {

  /** */
  public static String ACTIVITY_PERSISTER_PROCESS = "PERSISTER_ACTIVITY";
  
  public final static String DATA_CHANGES = "DATA_CHANGES";
  
  public static PersisterProcessContext getIntance(String name, SocialServiceContext context) {
    return new PersisterProcessContext(name, context);
  }
  
  public PersisterProcessContext(String name, SocialServiceContext context) {
    super(name, context);
  }
  
  public <T> PersisterProcessContext activity(DataChangeQueue<T> changes) {
    setProperty(DATA_CHANGES, changes);
    return this;
  }
  
  public <T> DataChangeQueue<T> getChanges() {
    return getProperty(DATA_CHANGES, DataChangeQueue.class);
  }
  
  

}
