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

package org.projectforge.web;

import java.util.Date;

import org.projectforge.core.SearchFilter;
import org.projectforge.web.calendar.SelectDate;
import org.projectforge.web.core.Select;
import org.projectforge.web.task.TaskTreeAction;
import org.projectforge.web.user.UserListAction;


/**
 */
public class SearchActionFilter extends SearchFilter
{
  // private static final Logger log = Logger.getLogger(TimesheetFilter.class);

  /**
   * 
   */
  private static final long serialVersionUID = -6525578651055914232L;

  /**
   * Only for annotating the super method.
   * @return the startTime
   */
  @SelectDate(periodStop = "stopTime")
  public Date getStartTime()
  {
    return super.getStartTime();
  }

  /**
   * Only for annotating the super method.
   * @return the stopTime
   */
  @SelectDate(periodStart = "startTime")
  public Date getStopTime()
  {
    return super.getStopTime();
  }

  /**
   * Only for annotating the super method.
   * @return the userId
   */
  @Select(selectAction = UserListAction.class)
  public Integer getModifiedByUserId()
  {
    return super.getModifiedByUserId();
  }

  /**
   * Only for annotating the super method.
   * @return the taskId
   */
  @Select(selectAction = TaskTreeAction.class)
  public Integer getTaskId()
  {
    return super.getTaskId();
  }
}
