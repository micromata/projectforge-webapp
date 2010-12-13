/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.core;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;

public class SearchFilter implements Serializable
{
  private static final long serialVersionUID = 5850672386075331163L;

  private String searchString;

  private Date modifiedStartTime, modifiedStopTime;

  private PFUserDO modifiedByUser;

  private TaskDO task;

  private Integer maxRows;

  /**
   * @return the startTime
   */
  public Date getModifiedStartTime()
  {
    return this.modifiedStartTime;
  }

  /**
   * @param modifiedStartTime the startTime to set
   */
  public void setModifiedStartTime(Date modifiedStartTime)
  {
    final DateHolder dh = new DateHolder(modifiedStartTime, DatePrecision.MILLISECOND);
    dh.setBeginOfDay();
    this.modifiedStartTime = dh.getDate();
  }

  /**
   * @return the stopTime
   */
  public Date getModifiedStopTime()
  {
    return this.modifiedStopTime;
  }

  /**
   * @param modifiedStopTime the stopTime to set
   */
  public void setModifiedStopTime(Date modifiedStopTime)
  {
    final DateHolder dh = new DateHolder(modifiedStopTime, DatePrecision.MILLISECOND);
    dh.setEndOfDay();
    this.modifiedStopTime = dh.getDate();
  }

  /**
   * The user who has done the modifications.
   */
  public PFUserDO getModifiedByUser()
  {
    return modifiedByUser;
  }

  public Integer getModifiedByUserId()
  {
    return modifiedByUser != null ? modifiedByUser.getId() : null;
  }

  public void setModifiedByUser(final PFUserDO modifiedByUser)
  {
    this.modifiedByUser = modifiedByUser;
  }

  public TaskDO getTask()
  {
    return task;
  }

  public void setTask(TaskDO task)
  {
    this.task = task;
  }

  public Integer getMaxRows()
  {
    return maxRows;
  }

  public void setMaxRows(Integer maxRows)
  {
    this.maxRows = maxRows;
  }

  public String getSearchString()
  {
    return searchString;
  }

  public void setSearchString(String searchString)
  {
    this.searchString = searchString;
  }

  @Override
  public String toString()
  {
    ToStringBuilder sb = new ToStringBuilder(this);
    sb.append("user", getModifiedByUserId());
    if (modifiedStartTime != null) {
      sb.append("modifiedStartTime", DateHelper.formatAsUTC(modifiedStartTime));
    }
    if (modifiedStopTime != null) {
      sb.append("modifiedStopTime", DateHelper.formatAsUTC(modifiedStopTime));
    }
    sb.append("maxRows", getMaxRows());
    return sb.toString();
  }
}
