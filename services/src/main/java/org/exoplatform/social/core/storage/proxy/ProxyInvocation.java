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
package org.exoplatform.social.core.storage.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.storage.SOCContext;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.DataStatus;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.memory.ActivityUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 28, 2014  
 */
public class ProxyInvocation implements InvocationHandler {
  /** */
  final static String GET_ID = "getId";
  /** */
  private final Class<?> targetClass;
  /** */
  private final ExoSocialActivity activity;

  public ProxyInvocation(Class<?> targetClass, ExoSocialActivity activity) {
    this.targetClass = targetClass;
    this.activity = activity;
  }
  
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    if (GET_ID.equals(methodName)) {
      String id = this.activity.getId();
      ActivityKey key = new ActivityKey(id);
      SOCContext context = SOCContext.instance();
      ActivityData data = context.getActivityCache().get(key);
      if (data != null) {
        return activity.getId();
      } else {
        key = new ActivityKey(this.activity.getHandle());
        data = context.getActivityCache().get(key);
        if (ActivityUtils.getDataStatus(data).equals(DataStatus.TRANSIENT)) {
          return this.activity.getHandle();
        } else {
          return data.getId();
        }
        
      }
    }
    return method.invoke(this.activity, args);
  }
  
  public Class<?> getTargetClass() {
    return targetClass;
  }

}
