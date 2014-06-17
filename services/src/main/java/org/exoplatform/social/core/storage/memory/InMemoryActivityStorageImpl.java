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
package org.exoplatform.social.core.storage.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.chromattic.api.ChromatticException;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityListEntity;
import org.exoplatform.social.core.chromattic.entity.HidableEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.LockableEntity;
import org.exoplatform.social.core.chromattic.utils.ActivityList;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.storage.streams.StreamInvocationHelper;
import org.exoplatform.social.core.storage.synchronization.SynchronizedActivityStorage;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 29, 2014  
 */
public class InMemoryActivityStorageImpl extends SynchronizedActivityStorage {

  private static final Log LOG = ExoLogger.getLogger(InMemoryActivityStorageImpl.class);
  
  public InMemoryActivityStorageImpl(RelationshipStorage relationshipStorage,
                                     IdentityStorage identityStorage,
                                     SpaceStorage spaceStorage,
                                     ActivityStreamStorage streamStorage) {
    super(relationshipStorage, identityStorage, spaceStorage, streamStorage);
  }

  @Override
  public int getNumberOfComments(ExoSocialActivity existingActivity) {
    String[] commentIds = existingActivity.getReplyToId();
    if (commentIds == null) {
      return 0;
    }
    
    int size = 0;

    ExoSocialActivity comment = null;
    //
    for(String commentId : commentIds) {
      comment = getStorage().getActivity(commentId);
      if (comment != null && !comment.isHidden()) {
        size++;
      }
    }

    //
    return size;
  }
  
  @Override
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {
    //
    List<String> commentIds = Arrays.asList(existingActivity.getReplyToId());
    int baseIndex = commentIds.indexOf(ActivityUtils.handle(baseComment));
    if (baseIndex > commentIds.size()) {
      baseIndex = commentIds.size();
    }

    int size = 0;
    //
    for(String commentId : commentIds.subList(0, baseIndex)) {
      ExoSocialActivity comment = getStorage().getActivity(commentId);
      if (!comment.isHidden())
        size++;
    }

    //
    return size;
  }
  @Override
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(existingActivity.getReplyToId());
    int baseIndex = commentIds.indexOf(ActivityUtils.handle(baseComment));

    //
    for(String commentId : commentIds.subList(baseIndex + 1, commentIds.size())) {
      ExoSocialActivity comment = getStorage().getActivity(commentId);
      if (!comment.isHidden())
        activities.add(getStorage().getActivity(commentId));
    }

    //
    return activities.size();

  }
  
  @Override
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity, int offset, int limit) {
    String[] commentIds = existingActivity.getReplyToId();
    if (commentIds == null) {
      return Collections.emptyList();
    }
    
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    //
    limit = (limit > commentIds.length ? commentIds.length : limit);
    
    for (int i = offset; i < commentIds.length; i++) {
      ExoSocialActivity comment = getStorage().getActivity(commentIds[i]);
      if (comment == null || comment.isHidden()) {
        continue;
      }
      activities.add(comment);
      //
      if (activities.size() == limit) {
        break;
      }
    }
    
    
    return activities;
  }
  
  
  public void createActivity(Identity owner, ExoSocialActivity activity, List<String> mentioners) throws ActivityStorageException {

    try {
      Validate.notNull(owner, "owner must not be null.");
      Validate.notNull(activity, "activity must not be null.");
      Validate.notNull(activity.getUpdated(), "Activity.getUpdated() must not be null.");
      Validate.notNull(activity.getPostedTime(), "Activity.getPostedTime() must not be null.");
      Validate.notNull(activity.getTitle(), "Activity.getTitle() must not be null.");
    } catch (IllegalArgumentException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.ILLEGAL_ARGUMENTS, e.getMessage(), e);
    }
    
    try {
      IdentityEntity identityEntity = _findById(IdentityEntity.class, owner.getId());

      IdentityEntity posterIdentityEntity;
      if (activity.getUserId() != null) {
        posterIdentityEntity = _findById(IdentityEntity.class, activity.getUserId());
      } else {
        posterIdentityEntity = identityEntity;
      }
      
      // Get ActivityList
      ActivityListEntity activityListEntity = identityEntity.getActivityList();

      //
      Collection<ActivityEntity> entities = new ActivityList(activityListEntity);
     
      // Create activity
      long currentMillis = System.currentTimeMillis();
      long activityMillis = (activity.getPostedTime() != null ? activity.getPostedTime() : currentMillis);
      ActivityEntity activityEntity = activityListEntity.createActivity(String.valueOf(activityMillis));
      entities.add(activityEntity);
      activityEntity.setIdentity(identityEntity);
      activityEntity.setComment(Boolean.FALSE);
      activityEntity.setPostedTime(activityMillis);
      activityEntity.setLastUpdated(activityMillis);
      activityEntity.setPosterIdentity(posterIdentityEntity);

      getSession().save();
      
      // Fill activity model
      activity.setId(activityEntity.getId());
      activity.setUserId(posterIdentityEntity.getId());
      activity.setStreamOwner(identityEntity.getRemoteId());
      activity.setPostedTime(activityMillis);
      activity.setReplyToId(new String[]{});
      activity.setUpdated(activityMillis);
      
      //records activity for mention case.
      //just keep raw data for mentioner
      //process mentioners on
      activity.setMentionedIds(processMentions(activity.getMentionedIds(), activity.getTitle(), mentioners, true));
      
      //
      activity.setPosterId(activity.getUserId() != null ? activity.getUserId() : owner.getId());
      
      //
      fillStream(identityEntity, activity);
      processActivity(activity);
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, e.getMessage(), e);
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was inserted activity by another session");
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, ex.getMessage());
      }
    }
  }
  
  /**
   * Creates the comment in memory and provides the fake id
   * 
   * @param owner
   * @param activity
   * @param mentioners
   * @throws ActivityStorageException
   */
  public void createActivityInMemory(Identity owner, ExoSocialActivity activity, List<String> mentioners) throws ActivityStorageException {

    try {
      Validate.notNull(owner, "owner must not be null.");
      Validate.notNull(activity, "activity must not be null.");
      Validate.notNull(activity.getUpdated(), "Activity.getUpdated() must not be null.");
      Validate.notNull(activity.getPostedTime(), "Activity.getPostedTime() must not be null.");
      Validate.notNull(activity.getTitle(), "Activity.getTitle() must not be null.");
    } catch (IllegalArgumentException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.ILLEGAL_ARGUMENTS, e.getMessage(), e);
    }
    
    try {
      IdentityEntity identityEntity = _findById(IdentityEntity.class, owner.getId());

      IdentityEntity posterIdentityEntity;
      if (activity.getUserId() != null) {
        posterIdentityEntity = _findById(IdentityEntity.class, activity.getUserId());
      } else {
        posterIdentityEntity = identityEntity;
      }
      // Create activity
      long currentMillis = System.currentTimeMillis();
      long activityMillis = (activity.getPostedTime() != null ? activity.getPostedTime() : currentMillis);
      // Fill activity model
      activity.setId(IdGenerator.generate());
      activity.setUserId(posterIdentityEntity.getId());
      activity.setStreamOwner(identityEntity.getRemoteId());
      activity.setPostedTime(activityMillis);
      activity.setReplyToId(new String[]{});
      activity.setUpdated(activityMillis);
      
      //records activity for mention case.
      //just keep raw data for mentioner
      //process mentioners on
      activity.setMentionedIds(processMentions(activity.getMentionedIds(), activity.getTitle(), mentioners, true));
      
      //
      activity.setPosterId(activity.getUserId() != null ? activity.getUserId() : owner.getId());
      
      //
      fillStream(identityEntity, activity);
      processActivity(activity);
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, e.getMessage(), e);
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was inserted activity by another session");
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, ex.getMessage());
      }
    }
  }
  
  public void persistJCRActivity(ExoSocialActivity activity) throws ActivityStorageException {
    try {
      List<String> mentioners = StorageUtils.getIdentityIds(activity.getMentionedIds());
      IdentityEntity identityEntity = _findById(IdentityEntity.class, activity.getStreamId());
      IdentityEntity posterIdentityEntity = _findById(IdentityEntity.class, activity.getUserId());
      // Get ActivityList
      ActivityListEntity activityListEntity = identityEntity.getActivityList();
      //
      Collection<ActivityEntity> entities = new ActivityList(activityListEntity);
      // Create activity
      long currentMillis = System.currentTimeMillis();
      long activityMillis = (activity.getPostedTime() != null ? activity.getPostedTime() : currentMillis);
      ActivityEntity entity = activityListEntity.createActivity(String.valueOf(activityMillis));
      //
      entities.add(entity);
      
      entity.setIdentity(identityEntity);
      entity.setComment(Boolean.FALSE);
      entity.setPostedTime(activityMillis);
      entity.setLastUpdated(activityMillis);
      entity.setPosterIdentity(posterIdentityEntity);
      entity.setTitle(activity.getTitle());
      entity.setTitleId(activity.getTitleId());
      entity.setBody(activity.getBody());
      entity.setBodyId(activity.getBodyId());
      entity.setLikes(activity.getLikeIdentityIds());
      entity.setType(activity.getType());
      entity.setAppId(activity.getAppId());
      entity.setExternalId(activity.getExternalId());
      entity.setUrl(activity.getUrl());
      entity.setPriority(activity.getPriority());
      //
      HidableEntity hidable = _getMixin(entity, HidableEntity.class, true);
      hidable.setHidden(activity.isHidden());
      LockableEntity lockable = _getMixin(entity, LockableEntity.class, true);
      lockable.setLocked(activity.isLocked());
      entity.setMentioners(activity.getMentionedIds());
      entity.setCommenters(activity.getCommentedIds());

      
      //
      Map<String, String> params = activity.getTemplateParams();
      if (params != null) {
        entity.putParams(params);
      }
      
      
      activity.setId(entity.getId());
      
      getSession().save();
      
      Identity owner = identityStorage.findIdentityById(activity.getStreamId());
      if (mustInjectStreams) {
        StreamInvocationHelper.savePoster(owner, entity);
        StreamInvocationHelper.save(owner, activity, mentioners.toArray(new String[0]));
      }
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, e.getMessage());
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was updated activity by another session");
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_UPDATE_ACTIVITY, ex.getMessage());
      }
    }

  }

  public void persistActivity(ExoSocialActivity activity) throws ActivityStorageException {
    try {
      ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
      List<String> mentioners = StorageUtils.getIdentityIds(activity.getMentionedIds());
      
      entity.setTitle(activity.getTitle());
      entity.setTitleId(activity.getTitleId());
      entity.setBody(activity.getBody());
      entity.setBodyId(activity.getBodyId());
      entity.setLikes(activity.getLikeIdentityIds());
      entity.setType(activity.getType());
      entity.setAppId(activity.getAppId());
      entity.setExternalId(activity.getExternalId());
      entity.setUrl(activity.getUrl());
      entity.setPriority(activity.getPriority());
      //
      HidableEntity hidable = _getMixin(entity, HidableEntity.class, true);
      hidable.setHidden(activity.isHidden());
      LockableEntity lockable = _getMixin(entity, LockableEntity.class, true);
      lockable.setLocked(activity.isLocked());
      entity.setMentioners(activity.getMentionedIds());
      entity.setCommenters(activity.getCommentedIds());

      //
      Map<String, String> params = activity.getTemplateParams();
      if (params != null) {
        entity.putParams(params);
      }

      Identity owner = identityStorage.findIdentityById(activity.getStreamId());
      if (mustInjectStreams) {
        StreamInvocationHelper.savePoster(owner, entity);
        StreamInvocationHelper.save(owner, activity, mentioners.toArray(new String[0]));
      }
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, e.getMessage());
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was updated activity by another session");
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_UPDATE_ACTIVITY, ex.getMessage());
      }
    }

  }
  
  public void createCommentInMemory(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {
      long currentMillis = System.currentTimeMillis();
      long commentMillis = (comment.getPostedTime() != null ? comment.getPostedTime() : currentMillis);
      //
      List<String> mentioners = new ArrayList<String>();
      activity.setMentionedIds(processMentions(activity.getMentionedIds(), comment.getTitle(), mentioners, true));
      
      //
      List<String> commenters = new ArrayList<String>();
      activity.setCommentedIds(processCommenters(activity.getCommentedIds(), comment.getUserId(), commenters, true));
      //
      comment.setParentId(activity.getId());
      comment.setId(IdGenerator.generate());
      comment.setPosterId(comment.getUserId());
      comment.isComment(true);
      comment.setPostedTime(commentMillis);
      //
      String[] ids = activity.getReplyToId();
      List<String> listIds;
      if (ids != null) {
        listIds = new ArrayList<String>(Arrays.asList(ids));
      }
      else {
        listIds = new ArrayList<String>();
      }
      
      String handle = ActivityUtils.handle(comment);
      listIds.add(handle);
      activity.setReplyToId(listIds.toArray(new String[]{}));
      //
      activity.setUpdated(currentMillis);
      processActivity(comment);
  }
  
  /**
   * Using to create new comment 
   * @param activity
   * @param comment
   * @throws ActivityStorageException
   */
  public void createComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {
    try {

      //
      long currentMillis = System.currentTimeMillis();
      long commentMillis = (comment.getPostedTime() != null ? comment.getPostedTime() : currentMillis);
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      ActivityEntity commentEntity = activityEntity.createComment(String.valueOf(commentMillis));

      //
      List<String> mentioners = new ArrayList<String>();
      activity.setMentionedIds(processMentions(activity.getMentionedIds(), comment.getTitle(), mentioners, true));
      
      //
      List<String> commenters = new ArrayList<String>();
      activity.setCommentedIds(processCommenters(activity.getCommentedIds(), comment.getUserId(), commenters, true));
      
      //
      activityEntity.getComments().add(commentEntity);
      activityEntity.setMentioners(activity.getMentionedIds());
      activityEntity.setCommenters(activity.getCommentedIds());
      comment.setParentId(activity.getId());
      comment.setId(commentEntity.getId());
      comment.setPosterId(comment.getUserId());
      comment.isComment(true);
      
      //
      HidableEntity hidable = _getMixin(commentEntity, HidableEntity.class, true);
      hidable.setHidden(comment.isHidden());
      commentEntity.setTitle(comment.getTitle());
      commentEntity.setType(comment.getType());
      commentEntity.setTitleId(comment.getTitleId());
      commentEntity.setBody(comment.getBody());
      commentEntity.setIdentity(activityEntity.getIdentity());
      commentEntity.setPosterIdentity(_findById(IdentityEntity.class, comment.getUserId()));
      commentEntity.setComment(Boolean.TRUE);
      commentEntity.setPostedTime(commentMillis);
      commentEntity.setLastUpdated(commentMillis);
      
      
      commentEntity.setMentioners(processMentions(ArrayUtils.EMPTY_STRING_ARRAY, comment.getTitle(), new ArrayList<String>(), true));
      
      //
      String[] ids = activity.getReplyToId();
      List<String> listIds;
      if (ids != null) {
        listIds = new ArrayList<String>(Arrays.asList(ids));
      }
      else {
        listIds = new ArrayList<String>();
      }
      listIds.add(commentEntity.getId());
      activity.setReplyToId(listIds.toArray(new String[]{}));
      //
      activity.setUpdated(currentMillis);
      getSession().save();
      processActivity(comment);
    }  
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_COMMENT, e.getMessage(), e);
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was inserted activity by another session");
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, ex.getMessage());
      }
    }
    //
    LOG.debug(String.format(
        "Comment %s by %s (%s) created: comment size is == %s ",
        comment.getTitle(),
        comment.getUserId(),
        comment.getId(),
        activity.getCommentedIds().length
    ));
  }
  
  /**
   * Persist the comment in memory to storage
   * 
   * @param activity
   * @param comment
   * @throws ActivityStorageException
   */
  public void persistCommentInMemory(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {

    try {
      //
      long currentMillis = System.currentTimeMillis();
      long commentMillis = (comment.getPostedTime() != null ? comment.getPostedTime() : currentMillis);
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      ActivityEntity commentEntity = activityEntity.createComment(String.valueOf(commentMillis));
      
      //
      long oldUpdated = getLastUpdatedTime(activityEntity);
      
      activityEntity.getComments().add(commentEntity);
      activityEntity.setMentioners(activity.getMentionedIds());
      activityEntity.setCommenters(activity.getCommentedIds());
      activityEntity.setLastUpdated(commentEntity.getPostedTime());

      //
      HidableEntity hidable = _getMixin(commentEntity, HidableEntity.class, true);
      hidable.setHidden(comment.isHidden());
      commentEntity.setTitle(comment.getTitle());
      commentEntity.setType(comment.getType());
      commentEntity.setTitleId(comment.getTitleId());
      commentEntity.setBody(comment.getBody());
      commentEntity.setIdentity(activityEntity.getIdentity());
      commentEntity.setPosterIdentity(_findById(IdentityEntity.class, comment.getUserId()));
      commentEntity.setComment(Boolean.TRUE);
      commentEntity.setPostedTime(commentMillis);
      commentEntity.setLastUpdated(commentMillis);
      commentEntity.setMentioners(processMentions(ArrayUtils.EMPTY_STRING_ARRAY, comment.getTitle(), new ArrayList<String>(), true));
      
      Map<String, String> params = comment.getTemplateParams();
      if (params != null) {
        commentEntity.putParams(params);
      }
      
      comment.setParentId(activity.getId());
      comment.setId(commentEntity.getId());
      
      List<String> commenters = StorageUtils.getIdentityIds(activityEntity.getCommenters());
      List<String> mentioners = StorageUtils.getIdentityIds(commentEntity.getMentioners());
      //
      if (mustInjectStreams && ActivityUtils.afterDayOrMore(oldUpdated, currentMillis)) {
        Identity identity = identityStorage.findIdentityById(comment.getUserId());
        StreamInvocationHelper.updateCommenter(identity, activityEntity, commenters.toArray(new String[0]), oldUpdated);
        StreamInvocationHelper.update(activity, mentioners.toArray(new String[0]), oldUpdated);
      }
    }  
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_COMMENT, e.getMessage(), e);
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was inserted activity by another session", ex);
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, ex.getMessage());
      }
    }

    LOG.debug(String.format(
        "Comment %s by %s (%s) created: comment size is == %s ",
        comment.getTitle(),
        comment.getUserId(),
        comment.getId(),
        activity.getCommentedIds().length
    ));
  }
  
  /**
   * Using the case when the Persister task invokes 
   * to persist JCR storage when add new comment to the activity.
   * 
   * 1. Activity Entity: updates the lastUpdate 
   * 2. Comment Entity: add mixin type, params map
   * 3. Create Activity Ref for commenter, comment's mentioners
   * 
   * @param activity the activity
   * @param comment the new comment
   * @throws ActivityStorageException
   */
  public void persistComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {

    try {
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      ActivityEntity commentEntity = _findById(ActivityEntity.class, comment.getId());
      //
      long oldUpdated = getLastUpdatedTime(activityEntity);
      
      activityEntity.setLastUpdated(commentEntity.getPostedTime());

      HidableEntity hidable = _getMixin(commentEntity, HidableEntity.class, true);
      hidable.setHidden(comment.isHidden());

      Map<String, String> params = comment.getTemplateParams();
      if (params != null) {
        commentEntity.putParams(params);
      }
      
      List<String> commenters = StorageUtils.getIdentityIds(activityEntity.getCommenters());
      List<String> mentioners = StorageUtils.getIdentityIds(commentEntity.getMentioners());
      //
      if (mustInjectStreams && ActivityUtils.afterDayOrMore(oldUpdated, commentEntity.getPostedTime())) {
        Identity identity = identityStorage.findIdentityById(comment.getUserId());
        StreamInvocationHelper.updateCommenter(identity, activityEntity, commenters.toArray(new String[0]), oldUpdated);
        StreamInvocationHelper.update(activity, mentioners.toArray(new String[0]), oldUpdated);
      }
    }  
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_COMMENT, e.getMessage(), e);
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was inserted activity by another session");
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, ex.getMessage());
      }
    }

    LOG.debug(String.format(
        "Comment %s by %s (%s) created: comment size is == %s ",
        comment.getTitle(),
        comment.getUserId(),
        comment.getId(),
        activity.getCommentedIds().length
    ));
  }
  
  public void persistLikeOrUnlike(ExoSocialActivity activity) throws ActivityStorageException {
    try {
      ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
      entity.setLikes(activity.getLikeIdentityIds());
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_UPDATE_ACTIVITY, e.getMessage());
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was updated activity by another session");
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_UPDATE_ACTIVITY, ex.getMessage());
      }
    }
  }
  
  public void persistAddOrRemoveComment(ExoSocialActivity activity) throws ActivityStorageException {
    try {
      ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
      entity.setMentioners(activity.getMentionedIds());
      entity.setCommenters(activity.getCommentedIds());
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_UPDATE_ACTIVITY, e.getMessage());
    } catch (ChromatticException ex) {
      Throwable throwable = ex.getCause();
      if (throwable instanceof ItemExistsException || 
          throwable instanceof InvalidItemStateException) {
        LOG.warn("Probably was updated activity by another session");
        LOG.debug(ex.getMessage(), ex);
      } else {
        throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_UPDATE_ACTIVITY, ex.getMessage());
      }
    }
  }
  
  public void removeComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {
    //
    List<String> mentioners = new ArrayList<String>();
    activity.setMentionedIds(processMentions(activity.getMentionedIds(), comment.getTitle(), mentioners, false));
    
    //
    List<String> commenters = new ArrayList<String>();
    activity.setCommentedIds(processCommenters(activity.getCommentedIds(), comment.getUserId(), commenters, false));
    
    String[] replyIds = activity.getReplyToId();
    activity.setReplyToId((String[])ArrayUtils.removeElement(replyIds, comment.getId()));
    
  }

}
