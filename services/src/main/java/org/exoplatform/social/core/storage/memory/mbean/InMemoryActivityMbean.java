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
package org.exoplatform.social.core.storage.memory.mbean;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.social.core.storage.SOCContext;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 9, 2014  
 */
@Managed
@NameTemplate({@Property(key = "service", value = "social"), @Property(key = "view", value = "activity") })
@ManagedDescription("Social activity service.")
public class InMemoryActivityMbean implements Startable {
  private long maxFixedSize = 0;
  private long intervalPersistThreshold = 0;
  private boolean activityMemoryStatus = true;
  
  public InMemoryActivityMbean(SOCContext socContext) {
    this.maxFixedSize = socContext.getLimitPersistThreshold();
    this.intervalPersistThreshold = socContext.getIntervalPersistThreshold();
  }
  
  @Managed
  @ManagedDescription("Set the max fixed size threshold to call persistence")
  public void setMaxFixedSize(long maxFixedSize) {
    this.maxFixedSize = maxFixedSize;
    SOCContext.instance().setLimitPersistThreshold(this.maxFixedSize);
  }
  
  @Managed
  @ManagedDescription("Get the max fixed size threshold number")
  public long getMaxFixedSize() {
    return this.maxFixedSize;
  }
  
  @Managed
  @ManagedDescription("Set the interval persistence threshold as miliseconds value. for ex: 5000ms")
  public void setIntervalPersistThreshold(long intervalPersistThreshold) {
    this.intervalPersistThreshold = intervalPersistThreshold;
    SOCContext.instance().setIntervalPersistThreshold(this.intervalPersistThreshold);
  }
  
  @Managed
  @ManagedDescription("Get the interval persistence threshold as miliseconds value.")
  public long getIntervalPersistThreshold() {
    return this.intervalPersistThreshold;
  }
  
  @Managed
  @ManagedDescription("Activate(True) | Deactivate(False) the activity memory processing")
  public void setActivityMemoryStatus(boolean status) {
    this.activityMemoryStatus = status;
    SOCContext.instance().switchActivityMemory(this.activityMemoryStatus);
  }
  
  @Managed
  @ManagedDescription("Get the activity memory processing status")
  public boolean getActivityMemoryStatus() {
    return this.activityMemoryStatus;
  }

  @Override
  public void start() {
    
  }

  @Override
  public void stop() {
    
  }

}
