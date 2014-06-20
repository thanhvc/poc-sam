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
package org.exoplatform.social.core.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.data.ActivityDataBuilder;
import org.exoplatform.social.core.data.CommentDataBuilder;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.memory.ActivityUtils;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
* Created by The eXo Platform SAS
* Author : eXoPlatform
* exo@exoplatform.com
* Apr 23, 2014
*/
public class InMemoryActivityManagerTest extends AbstractCoreTest {
  
  private final Log LOG = ExoLogger.getLogger(InMemoryActivityManagerTest.class);
  private List<ExoSocialActivity> tearDownActivityList;
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  private ActivityManager activityManager;
  private SpaceService spaceService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    SOCContext.instance().switchActivityMemory(true);
    
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
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
    super.tearDown();
  }

  /**
* Test {@link ActivityManager#saveActivityNoReturn(Identity, ExoSocialActivity)}
*
* @throws Exception
* @since 1.2.0-Beta3
*/
  public void testSaveActivityNoReturn() throws Exception {
    String activityTitle = " activity title ";
    ExoSocialActivity activity = ActivityDataBuilder.initOne(activityTitle, johnIdentity).injectOne();
    tearDownActivityList.add(activity);
    
    assertNotNull(activity);
    assertEquals(activityTitle, activity.getTitle());
    assertEquals(johnIdentity.getId(), activity.getUserId());
    Map<String, String> gotTemplateParams = activity.getTemplateParams();
    List<String> values = new ArrayList<String>(gotTemplateParams.values());
    assertEquals("value 1", values.get(0));
    assertEquals("value 2", values.get(1));
    assertEquals("value 3", values.get(2));
  }
  
  /**
   * Test {@link ActivityManager#saveActivity(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveActivityNoReturnNotStreamOwner() throws Exception {
    String activityTitle = "activity title";
    
    ExoSocialActivity activity = ActivityDataBuilder.initOne(activityTitle, johnIdentity).injectOne();
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull(activity);
    assertEquals(activityTitle, activity.getTitle());
    assertEquals(johnIdentity.getId(), activity.getUserId());
  }
  
  /**
   * Test for
   * {@link ActivityManager#saveActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * and
   * {@link ActivityManager#saveActivity(Identity, org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * 
   * @throws ActivityStorageException
   */
  public void testSaveActivity() throws ActivityStorageException {
    ExoSocialActivity malformedActivity = new ExoSocialActivityImpl();
    malformedActivity.setTitle("malform");
    try {
      activityManager.saveActivityNoReturn(malformedActivity);
      fail("Expecting IllegalArgumentException.");
    } catch (IllegalArgumentException e) {
      LOG.info("test with malfomred activity passes.");
    }
  }
  
  /**
   * Test {@link ActivityManager#saveActivity(Identity, ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveActivityWithStreamOwner() throws Exception {
    String activityTitle = "activity title";
    ExoSocialActivity activity = ActivityDataBuilder.initMore(1, activityTitle, johnIdentity, demoIdentity).injectOne();
    tearDownActivityList.add(activity);
    
    assertNotNull(activity);
    assertEquals(activityTitle, activity.getTitle());
    assertEquals(johnIdentity.getId(), activity.getUserId());
  }
  
  /**
   * Test {@link ActivityManager#getActivities(Identity, long, long)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivitiesWithOffsetLimit() throws Exception {
    tearDownActivityList = ActivityDataBuilder.initMore(10, johnIdentity).inject();
    List<ExoSocialActivity> johnActivities = activityManager.getActivities(johnIdentity, 0, 5);
    assertEquals(5, johnActivities.size());
    
    johnActivities = activityManager.getActivities(johnIdentity, 0, 10);
    assertEquals(10, johnActivities.size());
    
    johnActivities = activityManager.getActivities(johnIdentity, 0, 20);
    assertEquals(10, johnActivities.size());
  }
  
  
  /**
   * Test {@link ActivityManager#getActivity(String)}
   * 
   * @throws ActivityStorageException
   */
  public void testGetActivity() throws ActivityStorageException {
    String activityTitle = "title";
    tearDownActivityList = ActivityDataBuilder.initOne(activityTitle, rootIdentity).inject();
    List<ExoSocialActivity> rootActivities = activityManager.getActivities(rootIdentity);
    assertEquals(1, rootActivities.size());
  }
  
  
  /**
   * Tests {@link ActivityManager#getParentActivity(ExoSocialActivity)}.
   */
  public void testGetParentActivity() {
    tearDownActivityList = ActivityDataBuilder.initOne(demoIdentity).inject();
    
    //
    ExoSocialActivity demoActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    assertNotNull(demoActivity);
    assertNull(activityManager.getParentActivity(demoActivity));

    //comment
    CommentDataBuilder.initOne(demoActivity, "comment", demoIdentity).injectOne();
    ExoSocialActivity gotComment = activityManager.getCommentsWithListAccess(demoActivity).load(0, 1)[0];
    assertNotNull(gotComment);
    
    //
    ExoSocialActivity parentActivity = activityManager.getParentActivity(gotComment);
    assertNotNull(parentActivity);
    assertEquals(demoActivity.getId(), parentActivity.getId());
    assertEquals(demoActivity.getTitle(), parentActivity.getTitle());
    assertEquals(demoActivity.getUserId(), parentActivity.getUserId());
  }
  
  /**
   * Test {@link ActivityManager#updateActivity(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testUpdateActivity() throws Exception {
    
    String activityTitle = "activity title";
    tearDownActivityList = ActivityDataBuilder.initOne(activityTitle, johnIdentity).inject();
    
    ExoSocialActivity activity = activityManager.getActivity(tearDownActivityList.get(0).getId());
    assertEquals(activityTitle, activity.getTitle());
    assertEquals(johnIdentity.getId(), activity.getUserId());
    
    String newTitle = "new activity title";
    activity.setTitle(newTitle);
    activityManager.updateActivity(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull(activity);
    assertEquals(newTitle, activity.getTitle());
  }

  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * 
   * @throws Exception
   */
  public void testDeleteActivity() throws Exception {
    tearDownActivityList = ActivityDataBuilder.initOne(demoIdentity).inject();
    activityManager.deleteActivity(tearDownActivityList.get(0));
    tearDownActivityList.clear();
    //
    List<ExoSocialActivity> rootActivities = activityManager.getActivities(demoIdentity);
    assertEquals(0, rootActivities.size());
  }
  
  /**
   * Test {@link ActivityManager#deleteActivity(String)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeleteActivityWithId() throws Exception {
    tearDownActivityList = ActivityDataBuilder.initOne(demoIdentity).inject();
    activityManager.deleteActivity(tearDownActivityList.get(0).getId());
    tearDownActivityList.clear();
    //
    List<ExoSocialActivity> rootActivities = activityManager.getActivities(demoIdentity);
    assertEquals(0, rootActivities.size());
  }
  
  /**
   * Test
   * {@link ActivityManager#saveComment(ExoSocialActivity, ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveComment() throws Exception {
    String activityTitle = "activity title";
    tearDownActivityList = ActivityDataBuilder.initOne(activityTitle, johnIdentity).inject();
    ExoSocialActivity activity = tearDownActivityList.get(0);
    
    ExoSocialActivity comment = CommentDataBuilder.initOne(activity, "Comment title", demoIdentity).injectOne();
    
    List<ExoSocialActivity> demoComments = activityManager.getComments(activity);
    assertEquals(1, demoComments.size());
    
    assertEquals("Comment title", demoComments.get(0).getTitle());
    assertEquals(demoIdentity.getId(), demoComments.get(0).getUserId());

    ExoSocialActivity gotParentActivity = activityManager.getParentActivity(comment);
    assertEquals(activity.getId(), gotParentActivity.getId());
    assertEquals(1, activity.getReplyToId().length);
    assertEquals(ActivityUtils.handle(comment), activity.getReplyToId()[0]);
  }
  
  /**
   * Test {@link ActivityManager#getCommentsWithListAccess(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetCommentsWithListAccess() throws Exception {
    tearDownActivityList = ActivityDataBuilder.initOne(demoIdentity).inject();
    ExoSocialActivity activity = tearDownActivityList.get(0);
    
    List<ExoSocialActivity> comments = CommentDataBuilder.initMore(10, activity, demoIdentity).inject();
    ExoSocialActivity baseActivity = comments.get(5);
    
    RealtimeListAccess<ExoSocialActivity> demoComments = activityManager.getCommentsWithListAccess(activity);
    assertEquals(10, demoComments.getSize());
    
    assertEquals(5, demoComments.getNumberOfNewer(baseActivity));
    assertEquals(4, demoComments.getNumberOfOlder(baseActivity));
  }
  
  /**
   * Test
   * {@link ActivityManager#deleteComment(ExoSocialActivity, ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeleteComment() throws Exception {
    tearDownActivityList = ActivityDataBuilder.initOne(demoIdentity).inject();
    ExoSocialActivity activity = tearDownActivityList.get(0);
    
    ExoSocialActivity demoComment = new ExoSocialActivityImpl();
    demoComment.setTitle("demo comment");
    demoComment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, demoComment);
    
    activityManager.deleteComment(activity, demoComment);
    
    assertEquals(0, activityManager.getComments(activity).size());
  }
  
  /**
   * Test {@link ActivityManager#saveLike(ExoSocialActivity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3s
   */
  public void testSaveLike() throws Exception {
    
    String title = "&\"demo activity";
    tearDownActivityList = ActivityDataBuilder.initOne(title, demoIdentity).inject();
    ExoSocialActivity demoActivity = tearDownActivityList.get(0);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals(0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.saveLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals(1, demoActivity.getLikeIdentityIds().length);
    assertEquals("&amp;&quot;demo activity", demoActivity.getTitle());
  }
  
  /**
   * {@link ActivityManager#deleteLike(ExoSocialActivity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeleteLike() throws Exception {
    tearDownActivityList = ActivityDataBuilder.initOne(demoIdentity).inject();
    ExoSocialActivity demoActivity = tearDownActivityList.get(0);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals(0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.saveLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals(1, demoActivity.getLikeIdentityIds().length);
    
    activityManager.deleteLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals(0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.deleteLike(demoActivity, maryIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals(0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.deleteLike(demoActivity, rootIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals(0, demoActivity.getLikeIdentityIds().length);
  }
  
  /**
   * Test {@link ActivityManager#getActivitiesWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivitiesWithListAccess() throws Exception {
    
    tearDownActivityList = ActivityDataBuilder.initMore(10, demoIdentity).inject();
    
    ExoSocialActivity baseActivity = tearDownActivityList.get(5);

    RealtimeListAccess<ExoSocialActivity> demoListAccess = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertEquals(10, demoListAccess.getSize());
    assertEquals(4, demoListAccess.getNumberOfNewer(baseActivity));
    assertEquals(5, demoListAccess.getNumberOfOlder(baseActivity));
  }
  
  /**
  * Test {@link ActivityManager#getActivitiesOfConnectionsWithListAccess(Identity)}
  *
  * @throws Exception
  * @since 1.2.0-Beta3
  */

  public void testGetActivitiesOfConnectionsWithListAccess() throws Exception {
    ExoSocialActivity baseActivity = null;
    for (int i = 0; i < 10; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(johnIdentity.getId());
      activityManager.saveActivityNoReturn(johnIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == 5) {
        baseActivity = activity;
      }
    }
    
    RealtimeListAccess<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(0, demoConnectionActivities.getSize());
    
    Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoJohnRelationship);
    
    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(10, demoConnectionActivities.getSize());
    assertEquals(4, demoConnectionActivities.getNumberOfNewer(baseActivity));
    assertEquals(5, demoConnectionActivities.getNumberOfOlder(baseActivity));
    
    for (int i = 0; i < 10; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(maryIdentity.getId());
      activityManager.saveActivityNoReturn(maryIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == 5) {
        baseActivity = activity;
      }
    }
    
    Relationship demoMaryRelationship = relationshipManager.invite(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryRelationship);
    
    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(20, demoConnectionActivities.getSize());
    assertEquals(4, demoConnectionActivities.getNumberOfNewer(baseActivity));
    assertEquals(15, demoConnectionActivities.getNumberOfOlder(baseActivity));
    
    relationshipManager.remove(demoJohnRelationship);
    relationshipManager.remove(demoMaryRelationship);
  } 
  
  /**
  *
  * TODO Failed
  * Test {@link ActivityManager#getActivitiesOfUserSpacesWithListAccess(Identity)}
  *
  * @throws Exception
  * @since 1.2.0-Beta3s
  */
  public void testGetActivitiesOfUserSpacesWithListAccess() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    
    int totalNumber = 10;
    
    ExoSocialActivity baseActivity = null;
    
    //demo posts activities to space
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == 5) {
        baseActivity = activity;
      }
    }
    
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("my space 0", space.getDisplayName());
    assertEquals("add new space 0", space.getDescription());
    
    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertEquals(10, demoActivities.getSize());
    assertEquals(4, demoActivities.getNumberOfNewer(baseActivity));
    
    assertEquals(5, demoActivities.getNumberOfOlder(baseActivity));
    
    Space space2 = this.getSpaceInstance(spaceService, 1);
    Identity spaceIdentity2 = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);
    
    //demo posts activities to space2
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity2, activity);
      tearDownActivityList.add(activity);
      if (i == 5) {
        baseActivity = activity;
      }
    }
    
    space2 = spaceService.getSpaceByDisplayName(space2.getDisplayName());
    assertEquals("my space 1", space2.getDisplayName());
    assertEquals("add new space 1", space2.getDescription());
    
    demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertEquals(20, demoActivities.getSize());
    assertEquals(4, demoActivities.getNumberOfNewer(baseActivity));
    assertEquals(15, demoActivities.getNumberOfOlder(baseActivity));
    
    demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(maryIdentity);
    assertEquals(0, demoActivities.getSize());
    
    spaceService.deleteSpace(space);
    spaceService.deleteSpace(space2);
  }
  
  /**
   * Test {@link ActivityManager#getActivityFeedWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivityFeedWithListAccess() throws Exception {
    this.populateActivityMass(demoIdentity, 3);
    this.populateActivityMass(maryIdentity, 3);
    this.populateActivityMass(johnIdentity, 2);
    
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    populateActivityMass(spaceIdentity, 5);

    RealtimeListAccess<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed.getSize() must be 8", 8, demoActivityFeed.getSize());

    Relationship demoMaryConnection = relationshipManager.invite(demoIdentity, maryIdentity);
    assertEquals(8, activityManager.getActivityFeedWithListAccess(demoIdentity).getSize());

    relationshipManager.confirm(demoMaryConnection);
    RealtimeListAccess<ExoSocialActivity> demoActivityFeed2 = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed2.getSize() must return 11", 11, demoActivityFeed2.getSize());
    RealtimeListAccess<ExoSocialActivity> maryActivityFeed = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals("maryActivityFeed.getSize() must return 6", 6, maryActivityFeed.getSize());
    
    // Create demo's activity on space
    createActivityToOtherIdentity(demoIdentity, spaceIdentity, 5);

    // after that the feed of demo with have 16
    RealtimeListAccess<ExoSocialActivity> demoActivityFeed3 = activityManager
        .getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed3.getSize() must return 16", 16,
        demoActivityFeed3.getSize());

    // demo's Space feed must be be 5
    RealtimeListAccess demoActivitiesSpaceFeed = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertEquals("demoActivitiesSpaceFeed.getSize() must return 10", 10, demoActivitiesSpaceFeed.getSize());

    // the feed of mary must be the same because mary not the member of space
    RealtimeListAccess<ExoSocialActivity> maryActivityFeed2 = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals("maryActivityFeed2.getSize() must return 6", 6, maryActivityFeed2.getSize());

    // john not friend of demo but member of space
    RealtimeListAccess johnSpaceActivitiesFeed = activityManager.getActivitiesOfUserSpacesWithListAccess(johnIdentity);
    assertEquals("johnSpaceActivitiesFeed.getSize() must return 10", 10, johnSpaceActivitiesFeed.getSize());

    relationshipManager.remove(demoMaryConnection);
    spaceService.deleteSpace(space);
  }
  
  /**
   * Populates activity.
   * 
   * @param user
   * @param number
   */
  private void populateActivityMass(Identity user, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();;
      activity.setTitle("title " + i);
      activity.setUserId(user.getId());
      try {
        activityManager.saveActivityNoReturn(user, activity);
        tearDownActivityList.add(activity);
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
  }
  
  private void createActivityToOtherIdentity(Identity posterIdentity,
                                             Identity targetIdentity,
                                             int number) {

    // if(!relationshipManager.get(posterIdentity,
    // targetIdentity).getStatus().equals(Type.CONFIRMED)){
    // return;
    // }

    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();

      activity.setTitle("title " + i);
      activity.setUserId(posterIdentity.getId());
      try {
        activityManager.saveActivityNoReturn(targetIdentity, activity);
        tearDownActivityList.add(activity);
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
  }
  
  /**
   * Test {@link ActivityManager#getComments(ExoSocialActivity)}
   * 
   * @throws ActivityStorageException
   */
  public void testGetCommentWithHtmlContent() throws ActivityStorageException {
    String htmlString = "<span><strong>foo</strong>bar<script>zed</script></span>";
    String htmlRemovedString = "<span><strong>foo</strong>bar&lt;script&gt;zed&lt;/script&gt;</span>";
    
    ExoSocialActivity activity = ActivityDataBuilder.initOne(rootIdentity).injectOne();
    tearDownActivityList.add(activity);
    
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(htmlString);
    comment.setUserId(rootIdentity.getId());
    comment.setBody(htmlString);
    activityManager.saveComment(activity, comment);
    assertNotNull(comment.getId());

    List<ExoSocialActivity> comments = activityManager.getComments(activity);
    assertEquals(1, comments.size());
    assertEquals(htmlRemovedString, comments.get(0).getBody());
    assertEquals(htmlRemovedString, comments.get(0).getTitle());
  }
  
  /**
   * @throws ActivityStorageException
   */
  public void testGetComment() throws ActivityStorageException {
    ExoSocialActivity activity = ActivityDataBuilder.initOne(rootIdentity).injectOne();
    tearDownActivityList.add(activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();;
    comment.setTitle("comment blah blah");
    comment.setUserId(rootIdentity.getId());

    activityManager.saveComment(activity, comment);

    assertNotNull(comment.getId());

    String[] commentsId = activity.getReplyToId();
    assertEquals(ActivityUtils.handle(comment), commentsId[0]);
  }

  /**
   * @throws ActivityStorageException
   */
  public void testGetComments() throws ActivityStorageException {
    ExoSocialActivity activity = ActivityDataBuilder.initOne(rootIdentity).injectOne();
    tearDownActivityList.add(activity);

    List<ExoSocialActivity> comments = new ArrayList<ExoSocialActivity>();
    for (int i = 0; i < 10; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();;
      comment.setTitle("comment blah blah");
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment);
      assertNotNull("comment.getId() must not be null", comment.getId());

      comments.add(comment);
    }

    ExoSocialActivity assertActivity = activityManager.getActivity(activity.getId());
    String[] commentIds = assertActivity.getReplyToId();
    for (int i = 1; i < commentIds.length; i++) {
      assertEquals(ActivityUtils.handle(comments.get(i - 1)), commentIds[i - 1]);
    }
  }

  /**
   * TODO Failed Unit Test for:
   * <p>
   * {@link ActivityManager#deleteComment(String, String)}
   * 
   * @throws ActivityStorageException
   */
  public void testDeleteCommentWithId() throws ActivityStorageException {
    final String title = "Activity Title";
    {
      //FIXBUG: SOC-1194
      //Case: a user create an activity in his stream, then give some comments on it.
      //Delete comments and check
      ExoSocialActivity activity1 = new ExoSocialActivityImpl();;
      activity1.setUserId(demoIdentity.getId());
      activity1.setTitle(title);
      activityManager.saveActivityNoReturn(demoIdentity, activity1);

      final int numberOfComments = 10;
      final String commentTitle = "Activity Comment";
      for (int i = 0; i < numberOfComments; i++) {
        ExoSocialActivity comment = new ExoSocialActivityImpl();;
        comment.setUserId(demoIdentity.getId());
        comment.setTitle(commentTitle + i);
        activityManager.saveComment(activity1, comment);
      }

      List<ExoSocialActivity> storedCommentList = activityManager.getComments(activity1);

      assertEquals(numberOfComments, storedCommentList.size());

      //delete random 2 comments
      int index1 = new Random().nextInt(numberOfComments - 1);
      int index2 = index1;
      while (index2 == index1) {
        index2 = new Random().nextInt(numberOfComments - 1);
      }

      ExoSocialActivity tobeDeletedComment1 = storedCommentList.get(0);
      ExoSocialActivity tobeDeletedComment2 = storedCommentList.get(1);
      
      LOG.info("remove commentId1 = " + tobeDeletedComment1.getId());
      activityManager.deleteComment(activity1, tobeDeletedComment1);
      LOG.info("remove commentId2 = " + tobeDeletedComment2.getId());
      activityManager.deleteComment(activity1, tobeDeletedComment2);

      List<ExoSocialActivity> afterDeletedCommentList = activityManager.getComments(activity1);

      assertEquals(numberOfComments - 2, afterDeletedCommentList.size());
    }
  }

  /**
   * Unit Test for: {@link ActivityManager#getActivities(Identity)}
   * {@link ActivityManager#getActivities(Identity, long, long)}
   * 
   * @throws ActivityStorageException
   */
 public void testGetActivities() throws ActivityStorageException {
   tearDownActivityList = ActivityDataBuilder.initMore(30, rootIdentity).inject();
   
   List<ExoSocialActivity> activities = activityManager.getActivities(rootIdentity);
   assertEquals(20, activities.size());

   List<ExoSocialActivity> allActivities = activityManager.getActivities(rootIdentity, 0, 30);
   assertEquals(30, allActivities.size());
 }

  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#getActivitiesOfConnections(Identity)}
   * 
   * @throws Exception
   */
 public void testGetActivitiesOfConnections() throws Exception {
   tearDownActivityList = ActivityDataBuilder.initMore(10, johnIdentity).inject();
   
   List<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity);
   assertEquals(0, demoConnectionActivities.size());
   
   Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
   relationshipManager.confirm(demoJohnRelationship);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity);
   assertEquals(10, demoConnectionActivities.size());
   
   tearDownActivityList.addAll(ActivityDataBuilder.initMore(10, maryIdentity).inject());
   
   Relationship demoMaryRelationship = relationshipManager.invite(demoIdentity, maryIdentity);
   relationshipManager.confirm(demoMaryRelationship);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity);
   assertEquals(20, demoConnectionActivities.size());
   
   relationshipManager.remove(demoJohnRelationship);
   relationshipManager.remove(demoMaryRelationship);
 }
 
 /**
  * Test {@link ActivityManager#getActivitiesOfConnections(Identity, int, int)}
  *
  * @throws Exception
  * @since 1.2.0-Beta3
  */
 
 public void testGetActivitiesOfConnectionswithOffsetLimit() throws Exception {
   tearDownActivityList = ActivityDataBuilder.initMore(10, johnIdentity).inject();
   
   List<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 20);
   assertEquals(0, demoConnectionActivities.size());
   
   Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
   relationshipManager.confirm(demoJohnRelationship);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 5);
   assertEquals(5, demoConnectionActivities.size());
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 20);
   assertEquals(10, demoConnectionActivities.size());
   
   tearDownActivityList.addAll(ActivityDataBuilder.initMore(10, maryIdentity).inject());
   
   Relationship demoMaryRelationship = relationshipManager.invite(demoIdentity, maryIdentity);
   relationshipManager.confirm(demoMaryRelationship);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 10);
   assertEquals(10, demoConnectionActivities.size());
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 20);
   assertEquals(20, demoConnectionActivities.size());
   
   relationshipManager.remove(demoJohnRelationship);
   relationshipManager.remove(demoMaryRelationship);
 }


  /**
   * TODO Failed Unit Test for:
   * <p>
   * {@link ActivityManager#getActivitiesOfUserSpaces(Identity)}
   * 
   * @throws Exception
   */
   public void testGetActivitiesOfUserSpaces() throws Exception {
     Space space = this.getSpaceInstance(spaceService, 0);
     Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
     
     int totalNumber = 10;
     
     tearDownActivityList = ActivityDataBuilder.initMore(totalNumber, spaceIdentity).inject();
     
     List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfUserSpaces(demoIdentity);
     assertEquals(10, demoActivities.size());
     
     Space space2 = this.getSpaceInstance(spaceService, 1);
     Identity spaceIdentity2 = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);
     
     tearDownActivityList.addAll(ActivityDataBuilder.initMore(totalNumber, spaceIdentity2).inject());
     
     demoActivities = activityManager.getActivitiesOfUserSpaces(demoIdentity);
     assertEquals(20, demoActivities.size());
     
     demoActivities = activityManager.getActivitiesOfUserSpaces(maryIdentity);
     assertEquals(0, demoActivities.size());
     
     spaceService.deleteSpace(space);
     spaceService.deleteSpace(space2);
   }

  /**
   * Test {@link ActivityManager#getActivities(Identity, long, long)}
   * 
   * @throws ActivityStorageException
   */
  public void testGetActivitiesByPagingWithoutCreatingComments() throws ActivityStorageException {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    tearDownActivityList = ActivityDataBuilder.initMore(totalActivityCount, johnIdentity).inject();

    List<ExoSocialActivity> activities = activityManager.getActivities(johnIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());
  }

  /**
   * Test {@link ActivityManager#getActivityFeed(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  
  public void testGetActivityFeed() throws Exception {
    
    tearDownActivityList = ActivityDataBuilder.initMore(3, demoIdentity).inject();
    tearDownActivityList.addAll(ActivityDataBuilder.initMore(3, maryIdentity).inject());
    tearDownActivityList.addAll(ActivityDataBuilder.initMore(2, johnIdentity).inject());
    
    List<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeed(demoIdentity);
    assertEquals(3, demoActivityFeed.size());

    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    tearDownActivityList.addAll(ActivityDataBuilder.initMore(5, spaceIdentity).inject());

    demoActivityFeed = activityManager.getActivityFeed(demoIdentity);
    assertEquals(8, demoActivityFeed.size());

    Relationship demoMaryConnection = relationshipManager.invite(demoIdentity, maryIdentity);
    assertEquals(8, activityManager.getActivityFeedWithListAccess(demoIdentity).getSize());

    relationshipManager.confirm(demoIdentity, maryIdentity);
    List<ExoSocialActivity> demoActivityFeed2 = activityManager.getActivityFeed(demoIdentity);
    assertEquals(11, demoActivityFeed2.size());
    List<ExoSocialActivity> maryActivityFeed = activityManager.getActivityFeed(maryIdentity);
    assertEquals(6, maryActivityFeed.size());

    relationshipManager.remove(demoMaryConnection);
    spaceService.deleteSpace(space);
  }
  
  
  
  /**
   * Test {@link ActivityManager#removeLike(ExoSocialActivity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testRemoveLike() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());

    assertEquals("demoActivity.getLikeIdentityIds() must return: 0",
                 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.saveLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 1", 1, demoActivity.getLikeIdentityIds().length);
    
    activityManager.removeLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.removeLike(demoActivity, maryIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.removeLike(demoActivity, rootIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
  }
  
  /**
* Test {@link ActivityManager#getActivitiesCount(Identity)}
*
* @throws Exception
* @since 1.2.0-Beta3
*/
  public void testGetActivitiesCount() throws Exception {
    int count = activityManager.getActivitiesCount(rootIdentity);
    assertEquals("count must be: 0", 0, count);

    tearDownActivityList = ActivityDataBuilder.initMore(5, rootIdentity).inject();
    count = activityManager.getActivitiesCount(rootIdentity);
    assertEquals(5, count);
  }

  /**
   * Gets an instance of the space.
   * 
   * @param spaceService
   * @param number
   * @return
   * @throws Exception
   * @since 1.2.0-GA
   */
  private Space getSpaceInstance(SpaceService spaceService, int number)
      throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.OPEN);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId(SpaceUtils.SPACE_GROUP + "/" + space.getPrettyName());
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] { "demo", "john" };
    String[] members = new String[] { "demo", "john" };
    String[] invitedUsers = new String[] { "mary"};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.saveSpace(space, true);
    return space;
  }
}
