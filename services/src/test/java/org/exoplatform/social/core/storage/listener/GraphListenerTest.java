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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.graph.simple.SimpleUndirectGraph;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.data.ActivityDataBuilder;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.InMemoryActivityManagerTest;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.NewListActivitiesKey;
import org.exoplatform.social.core.storage.memory.ActivityUtils;
import org.exoplatform.social.core.storage.memory.InMemoryActivityStorage;
import org.exoplatform.social.core.storage.streams.AStream;
import org.exoplatform.social.core.storage.streams.AStream.GRAPH;
import org.exoplatform.social.core.storage.streams.ActivityRefKey;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 20, 2014  
 */
public class GraphListenerTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(InMemoryActivityManagerTest.class);
  private List<ExoSocialActivity> tearDownActivityList;
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;
  
  /** */
  private GraphListener<ExoSocialActivity> graphListener;

  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private InMemoryActivityStorage activityStorage;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    SOCContext.instance().switchActivityMemory(true);
    
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    activityStorage = (InMemoryActivityStorage) getContainer().getComponentInstanceOfType(InMemoryActivityStorage.class);
    graphListener = new GraphListener<ExoSocialActivity>(SOCContext.instance());
    
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);
  }

  @Override
  public void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    
    graphListener = null;
    super.tearDown();
  }
  
  public void testOnAddActivity() throws Exception {
    ExoSocialActivity a = ActivityDataBuilder.initOne(maryIdentity).createOne();
    activityStorage.saveActivity(maryIdentity, a);
    graphListener.onAddActivity(a);
    
    
    
    AStream.Builder builder = AStream.initActivity(a);
    List<ActivityRefKey> keys = GRAPH.init()
                                     .context(SOCContext.instance())
                                     .feed(builder)
                                     .connection(builder)
                                     .owner(builder)
                                     .myspaces(builder)
                                     .space(builder)
                                     .getKeys();
    
    assertEquals(2, keys.size());
    
    SimpleUndirectGraph graph = SOCContext.instance().getActivityCacheGraph();
    
    for (ActivityRefKey refKey : keys) {
      NewListActivitiesKey cacheKey = refKey.listActivitiesKey();
      assertTrue(graph.contains(ActivityUtils.handle(a)));
      assertTrue(graph.contains(cacheKey));
    }
  }
  
  public void testOnRemoveActivity() throws Exception {
    ExoSocialActivity a = ActivityDataBuilder.initOne(maryIdentity).injectOne();
    activityStorage.deleteActivity(a.getId());
    graphListener.onRemoveActivity(a);
    
    AStream.Builder builder = AStream.initActivity(a);
    List<ActivityRefKey> keys = GRAPH.init()
                                     .context(SOCContext.instance())
                                     .feed(builder)
                                     .connection(builder)
                                     .owner(builder)
                                     .myspaces(builder)
                                     .space(builder)
                                     .getKeys();
    
    assertEquals(2, keys.size());
    
    SimpleUndirectGraph graph = SOCContext.instance().getActivityCacheGraph();
    
    for (ActivityRefKey refKey : keys) {
      NewListActivitiesKey cacheKey = refKey.listActivitiesKey();
      assertFalse(graph.contains(ActivityUtils.handle(a)));
      assertFalse(graph.contains(cacheKey));
    }
  }
}
