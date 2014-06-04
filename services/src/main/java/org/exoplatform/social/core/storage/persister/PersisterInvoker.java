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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;
import org.exoplatform.social.core.storage.activity.DataChangeListener;
import org.exoplatform.social.core.storage.activity.DataChangeQueue;
import org.exoplatform.social.core.storage.activity.DataModel;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.storage.streams.SocialChromatticAsyncProcessor;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 29, 2014  
 */
public class PersisterInvoker {

private static final Log LOG = ExoLogger.getLogger(PersisterInvoker.class);
  /**
   * Invokes to records the activity to Stream
   * 
   * @param owner
   * @param entity
   * @param mentioners NULL is empty mentioner.
   * @return
   */
  public static void persist(DataChangeListener<DataModel> jcrPersisterListener,
                             DataChangeQueue<DataModel> changes) {
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    PersisterProcessContext processCtx = PersisterProcessContext.getIntance(PersisterProcessContext.ACTIVITY_PERSISTER_PROCESS, ctx);
    try {
      ctx.getServiceExecutor().async(doPersist(jcrPersisterListener, changes), processCtx);
    } finally {
      if (ctx.isTraced()) {
        LOG.debug(processCtx.getTraceLog());
      }
      
    }
  }
  
  private static SocialChromatticAsyncProcessor doPersist(final DataChangeListener<DataModel> jcrPersisterListener,
                                                          final DataChangeQueue<DataModel> changes) {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        try {
          StorageUtils.persistJCR(false);
          if (changes != null && changes.size() > 0) {
            changes.broadcast(jcrPersisterListener);
          }
        } finally {
          StorageUtils.persistJCR(false);
          StorageUtils.endRequest();
        }
        return processContext;
      }

    };
  }
}
