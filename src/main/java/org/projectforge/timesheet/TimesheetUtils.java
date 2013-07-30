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

import java.sql.Timestamp;
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
  public static Timestamp getBegin(final Collection<TimesheetDO> timesheets, final Date date, final Integer userId)
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
  public static Timestamp getEnd(final Collection<TimesheetDO> timesheets, final Date date, final Integer userId)
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
  public static Timestamp getBeginEnd(final Collection<TimesheetDO> timesheets, final Date date, final Integer userId, final boolean begin)
  {
    if (timesheets == null || timesheets.size() == 0) {
      return null;
    }
    Timestamp result = null;
    final DateHolder dh = new DateHolder(date);
    for (final TimesheetDO timesheet : timesheets) {
      final Timestamp startDate = timesheet.getStartTime();
      final Timestamp stopDate = timesheet.getStopTime();
      if (timesheet.getStartTime() == null || stopDate == null || userId != timesheet.getUserId()) {
        continue;
      }
      if (begin == true) {
        if (dh.isSameDay(startDate) == true) {
          if (result == null || result.after(startDate) == true) {
            result = startDate;
          }
        } else if (dh.isSameDay(stopDate) == true) {
          return dh.setBeginOfDay().getTimestamp();
        }
      } else {
        if (dh.isSameDay(stopDate) == true) {
          if (result == null || result.before(stopDate) == true) {
            result = stopDate;
          }
        } else if (dh.isSameDay(startDate) == true) {
          return dh.setEndOfDay().getTimestamp();
        }
      }
    }
    return result;
  }
}
