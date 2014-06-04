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
package org.exoplatform.social.core.manager.memory;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.listeners.Callback;
import org.exoplatform.social.core.manager.ActivityManagerImpl;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.listener.AbstractActivityListener;
import org.exoplatform.social.core.storage.listener.SimpleActivityListener;
import org.exoplatform.social.core.storage.memory.InMemoryActivityStorage;
import org.exoplatform.social.core.storage.memory.InMemoryActivityStorageImpl;
import org.exoplatform.social.core.storage.proxy.ActivityProxyBuilder;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 23, 2014  
 */
public final class InMemoryActivityManagerImpl extends ActivityManagerImpl {
  /** Logger */
  private static final Log               LOG = ExoLogger.getLogger(InMemoryActivityManagerImpl.class);
  
  /** */
  private final SOCContext socContext;
  
  /** */
  final InMemoryActivityStorageImpl storageImpl;

  /** */
  final AbstractActivityListener<ExoSocialActivity> activityListener;
  
  public InMemoryActivityManagerImpl(InMemoryActivityStorage activityStorage, IdentityManager identityManager, SOCContext socContext) {
    super(activityStorage, identityManager);
    this.socContext = socContext;
    storageImpl = getStorageImpl(this.activityStorage);
    this.activityListener = new SimpleActivityListener<ExoSocialActivity>(storageImpl, socContext);
    
  }
  
  /**
   * Returns the activity storage implementation
   * 
   * @param activityStorage
   * @return
   */
  private InMemoryActivityStorageImpl getStorageImpl(ActivityStorage activityStorage) {
    if (activityStorage instanceof InMemoryActivityStorage) {
      InMemoryActivityStorage cached = (InMemoryActivityStorage) activityStorage;
      return cached.getStorageImpl();
    }
    
    return null;
  }
  
  @Override
  public void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity newActivity) {
    activityStorage.saveActivity(streamOwner, newActivity);
    activityListener.onAddActivity(newActivity);
    activityLifeCycle.saveActivity(getActivity(newActivity.getId()));
  }
  
  @Override
  public void saveActivityNoReturn(ExoSocialActivity newActivity) {
    Identity owner = getStreamOwner(newActivity);
    saveActivityNoReturn(owner, newActivity);
  }
  
  @Override
  public ExoSocialActivity saveActivity(Identity streamOwner, ExoSocialActivity newActivity) {
    activityStorage.saveActivity(streamOwner, newActivity);
    activityListener.onAddActivity(newActivity);
    return newActivity;
  }
  
  @Override
  public ExoSocialActivity saveActivity(Identity streamOwner,
                                        ExoSocialActivity activity,
                                        Callback callback) {
    activity.setLazyCreated(true);
    activityStorage.saveActivity(streamOwner, activity);
    activityListener.onAddActivity(activity, callback);
    return ActivityProxyBuilder.of(ExoSocialActivity.class, activity);
  }
  
  
  
  @Override
  public void saveActivity(Identity streamOwner, String activityType, String activityTitle) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setType(activityType);
    activity.setTitle(activityTitle);
    //process the activity in memory by provided callback == null
    activity = this.saveActivity(streamOwner, activity);
  }
  
  @Override
  public void updateActivity(ExoSocialActivity existingActivity) {
    activityListener.onUpdateActivity(existingActivity);
  }
  
  @Override
  public void deleteActivity(ExoSocialActivity existingActivity) {
    Validate.notNull(existingActivity.getId(), "existingActivity.getId() must not be null!");
    deleteActivity(existingActivity.getId());
  }
  
  @Override
  public void deleteActivity(String activityId) {
    activityListener.onRemoveActivity(activityStorage.getActivity(activityId));
  }
  
  @Override
  public void saveComment(ExoSocialActivity existingActivity, ExoSocialActivity newComment) throws ActivityStorageException {
    ExoSocialActivity activityProxy = ActivityProxyBuilder.of(ExoSocialActivity.class, existingActivity);
    activityStorage.saveComment(activityProxy, newComment);
    activityListener.onAddComment(activityProxy, newComment);
    activityLifeCycle.saveComment(newComment);
  }
  
  @Override
  public ExoSocialActivity saveComment(ExoSocialActivity existingActivity,
                                       ExoSocialActivity newComment,
                                       Callback callback) {
    newComment.setLazyCreated(true);
    ExoSocialActivity activityProxy = ActivityProxyBuilder.of(ExoSocialActivity.class, existingActivity);
    activityStorage.saveComment(activityProxy, newComment);
    activityListener.onAddComment(activityProxy, newComment);
    return ActivityProxyBuilder.of(ExoSocialActivity.class, newComment);
  }
  
  @Override
  public void deleteComment(String activityId, String commentId) {
    deleteComment(activityStorage.getActivity(activityId), activityStorage.getActivity(commentId));
    activityListener.onRemoveComment(activityStorage.getActivity(activityId), activityStorage.getActivity(commentId));
  }
  
  @Override
  public void deleteComment(ExoSocialActivity existingActivity, ExoSocialActivity existingComment) {
    storageImpl.removeComment(existingActivity, existingComment);
    activityListener.onRemoveComment(existingActivity, existingComment);
  }

  @Override
  public void saveLike(ExoSocialActivity existingActivity, Identity identity) {

    existingActivity.setTitle(null);
    existingActivity.setBody(null);

    String[] identityIds = existingActivity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      LOG.warn("activity is already liked by identity: " + identity);
      return;
    }
    identityIds = (String[]) ArrayUtils.add(identityIds, identity.getId());
    existingActivity.setLikeIdentityIds(identityIds);
    activityListener.onLikeActivity(existingActivity);
    activityLifeCycle.likeActivity(existingActivity);
  }
  
  @Override
  public void deleteLike(ExoSocialActivity activity, Identity identity) {
    activity.setTitle(null);
    activity.setBody(null);
    String[] identityIds = activity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      identityIds = (String[]) ArrayUtils.removeElement(identityIds, identity.getId());
      activity.setLikeIdentityIds(identityIds);
      activityListener.onLikeActivity(activity);
    } else {
      LOG.warn("activity is not liked by identity: " + identity);
    }
  }
  
  @Override
  public void addProcessorPlugin(BaseActivityProcessorPlugin plugin) {
    this.addProcessor(plugin);
  }
  
  @Override
  public void addProcessor(ActivityProcessor processor) {
    storageImpl.getActivityProcessors().add(processor);
    LOG.debug("added activity processor " + processor.getClass());
  }
}
