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
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.user.PFUserContext;


public class SearchFilter implements Serializable
{
  private static final long serialVersionUID = 5850672386075331163L;

  private TimePeriod timePeriod;

  private Integer modifiedByUserId;

  private Integer taskId;

  private Integer maxRows;

  public void reset()
  {
    modifiedByUserId = PFUserContext.getUser().getId();
    DateHolder dh = new DateHolder(new Date(), DatePrecision.MILLISECOND, PFUserContext.getLocale());
    dh.setEndOfDay();
    if (timePeriod == null) {
      timePeriod = new TimePeriod();
    }
    timePeriod.setToDate(dh.getDate());
    dh.setBeginOfDay();
    timePeriod.setFromDate(dh.getDate());
    maxRows = 10;
  }

  /**
   * @return the startTime
   */
  public Date getStartTime()
  {
    return getTimePeriod().getFromDate();
  }

  /**
   * @param startTime the startTime to set
   */
  public void setStartTime(Date startTime)
  {
    getTimePeriod().setFromDate(startTime);
  }

  /**
   * @return the stopTime
   */
  public Date getStopTime()
  {
    return getTimePeriod().getToDate();
  }

  /**
   * @param stopTime the stopTime to set
   */
  public void setStopTime(Date stopTime)
  {
    DateHolder dh = new DateHolder(stopTime, DatePrecision.MILLISECOND, PFUserContext.getLocale());
    dh.setEndOfDay();
    getTimePeriod().setToDate(dh.getDate());
  }

  private TimePeriod getTimePeriod()
  {
    if (timePeriod == null) {
      timePeriod = new TimePeriod();
    }
    return timePeriod;
  }

  /**
   * The user who has done the represented modification if available.
   * @return
   */
  public Integer getModifiedByUserId()
  {
    return modifiedByUserId;
  }

  public void setModifiedByUserId(Integer modifiedByUserId)
  {
    this.modifiedByUserId = modifiedByUserId;
  }

  public Integer getTaskId()
  {
    return taskId;
  }

  public void setTaskId(Integer taskId)
  {
    this.taskId = taskId;
  }

  public Integer getMaxRows()
  {
    return maxRows;
  }

  public void setMaxRows(Integer maxRows)
  {
    this.maxRows = maxRows;
  }

  @Override
  public String toString()
  {
    ToStringBuilder sb = new ToStringBuilder(this);
    sb.append("user", getModifiedByUserId());
    if (timePeriod != null) {
      sb.append("from", DateHelper.formatAsUTC(timePeriod.getFromDate()));
      sb.append("to", DateHelper.formatAsUTC(timePeriod.getToDate()));
    }
    return sb.toString();
  }
}
