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

import java.lang.reflect.Proxy;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 28, 2014  
 */
public class ActivityProxyBuilder {
  
  @SuppressWarnings("unchecked")
  static <T> T of(ProxyInvocation invocation) {
    Class<?> targetClass = invocation.getTargetClass();
    //1. loader - the class loader to define the proxy class
    //2. the list of interfaces for the proxy class to implement
    //3. the invocation handler to dispatch method invocations to
    return (T) Proxy.newProxyInstance(targetClass.getClassLoader(),
                                      new Class<?>[]{targetClass}, invocation);
    
  }
  
  public static <T> T of(Class<T> aClass, ExoSocialActivity activity) {
    return of(new ProxyInvocation(aClass, activity));
  }

}
