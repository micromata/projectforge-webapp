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

package org.projectforge.timesheet;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.projectforge.common.DateHolder;

/**
 * Provides some helper methods.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TimesheetUtils
{
  /**
   * @param timesheets
   * @param date
   * @param userId
   * @return The start time of the earliest time-sheet for the given user for the given date of the given list of time sheets. Returns null
   *         if no such time sheet is given.
   */
  public static Date getBegin(final Collection<TimesheetDO> timesheets, final Date date, final Integer userId)
  {
    return getBeginEnd(timesheets, date, userId, true);
  }

  /**
   * @param timesheets
   * @param date
   * @param userId
   * @return The stop time of the latest time-sheet for the given user for the given date of the given list of time sheets. Returns null if
   *         no such time sheet is given.
   */
  public static Date getEnd(final Collection<TimesheetDO> timesheets, final Date date, final Integer userId)
  {
    return getBeginEnd(timesheets, date, userId, false);
  }

  /**
   * @param timesheets
   * @param date
   * @param userId
   * @param begin If true then the start time of the earliest time-sheet is returned otherwise the latest stop-time.
   * @see #getBegin(Collection, Date, Integer)
   * @see #getEnd(Collection, Date, Integer)
   */
  public static Date getBeginEnd(final Collection<TimesheetDO> timesheets, final Date date, final Integer userId, final boolean begin)
  {
    final TimesheetStats stats = getStats(timesheets, date, userId);
    if (stats == null) {
      return null;
    }
    if (begin == true) {
      return stats.getEarliestStartDate();
    } else {
      return stats.getLatestStopDate();
    }
  }

  public static TimesheetStats getStats(final Collection<TimesheetDO> timesheets, final Date day, final Integer userId)
  {
    final DateHolder dh = new DateHolder(day).setBeginOfDay();
    final Date startDate = dh.getDate();
    final Date stopDate = dh.add(Calendar.DAY_OF_MONTH, 1).getDate();
    return calculateStats(timesheets, startDate, stopDate, userId);
  }

  public static TimesheetStats calculateStats(final Collection<TimesheetDO> timesheets, final Date from, final Date to, final Integer userId)
  {
    if (timesheets == null || timesheets.size() == 0) {
      return null;
    }
    final TimesheetStats stats = new TimesheetStats(from, to);
    for (final TimesheetDO timesheet : timesheets) {
      if (userId != timesheet.getUserId()) {
        continue;
      }
      stats.add(timesheet);
    }
    return stats;
  }
}
