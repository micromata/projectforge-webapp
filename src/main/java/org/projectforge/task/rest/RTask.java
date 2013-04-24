/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.task.rest;

import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import org.projectforge.common.ReflectionToString;
import org.projectforge.core.Priority;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskStatus;
import org.projectforge.task.TimesheetBookingStatus;

/**
 * TaskDO object for REST,
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class RTask implements Comparable<RTask>
{
  private Collection<RTask> children;

  private final Integer id;

  private final Integer parentTaskId;

  private final boolean deleted;

  private final Date created, lastUpdate;

  private final String description, title, shortDescription, reference;

  private final Integer maxHours;

  private final Priority priority;

  private final TaskStatus status;

  private TimesheetBookingStatus timesheetBookingStatus;

  private boolean bookableForTimesheets;

  public RTask(final TaskDO task)
  {
    this.id = task.getId();
    this.parentTaskId = task.getParentTaskId();
    this.deleted = task.isDeleted();
    this.created = task.getCreated();
    this.lastUpdate = task.getLastUpdate();
    this.description = task.getDescription();
    this.reference = task.getReference();
    this.title = task.getTitle();
    this.shortDescription = task.getShortDescription();
    this.maxHours = task.getMaxHours();
    this.priority = task.getPriority();
    this.status = task.getStatus();
  }

  public Integer getId()
  {
    return id;
  }

  public boolean isDeleted()
  {
    return deleted;
  }

  public Date getCreated()
  {
    return created;
  }

  public Date getLastUpdate()
  {
    return lastUpdate;
  }

  public String getDescription()
  {
    return description;
  }

  public Integer getMaxHours()
  {
    return maxHours;
  }

  public String getTitle()
  {
    return title;
  }

  public Integer getParentTaskId()
  {
    return parentTaskId;
  }

  public String getShortDescription()
  {
    return shortDescription;
  }

  /**
   * @return the priority
   */
  public Priority getPriority()
  {
    return priority;
  }

  public TaskStatus getStatus()
  {
    return status;
  }

  public String getReference()
  {
    return reference;
  }

  public TimesheetBookingStatus getTimesheetBookingStatus()
  {
    return timesheetBookingStatus;
  }

  /**
   * @param bookableForTimesheets the bookableForTimesheets to set
   * @return this for chaining.
   */
  public void setBookableForTimesheets(final boolean bookableForTimesheets)
  {
    this.bookableForTimesheets = bookableForTimesheets;
  }

  /**
   * @return the bookableForTimesheets
   */
  public boolean isBookableForTimesheets()
  {
    return bookableForTimesheets;
  }

  /**
   * @return the children
   */
  public Collection<RTask> getChildren()
  {
    return children;
  }

  void add(final RTask child)
  {
    if (this.children == null) {
      this.children = new TreeSet<RTask>();
    }
    this.children.add(child);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return id.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (obj instanceof RTask == false) {
      return false;
    }
    return this.hashCode() == obj.hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return new ReflectionToString(this).toString();
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final RTask o)
  {
    final String title1 = this.title != null ? this.title.toLowerCase() : "";
    final String title2 = o.title != null ? o.title.toLowerCase() : "";
    return title1.compareTo(title2);
  }
}
