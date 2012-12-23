/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.projectforge.calendar.CalendarUtils;

/**
 * Represents a recurrence event (created by a master TeamEventDO with recurrence rules).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TeamRecurrenceEvent implements TeamEvent, Serializable
{
  private static final long serialVersionUID = -7523583666714303142L;

  private final TeamEventDO master;

  private final Date startDate, endDate;

  /**
   * @param master
   * @param startDay day of event (startDate and endDate will be calculated based on this day and the master).
   */
  public TeamRecurrenceEvent(final TeamEventDO master, final Calendar startDay)
  {
    this.master = master;
    final Calendar masterStartDate = (Calendar)startDay.clone(); // Clone time zone and local.
    masterStartDate.setTime(master.getStartDate());
    startDay.set(Calendar.HOUR_OF_DAY, masterStartDate.get(Calendar.HOUR_OF_DAY));
    startDay.set(Calendar.MINUTE, masterStartDate.get(Calendar.MINUTE));
    startDay.set(Calendar.SECOND, masterStartDate.get(Calendar.SECOND));
    this.startDate = startDay.getTime();
    final Calendar masterEndDate = (Calendar)startDay.clone(); // Clone time zone and local.
    masterEndDate.setTime(master.getEndDate());
    startDay.set(Calendar.HOUR_OF_DAY, masterEndDate.get(Calendar.HOUR_OF_DAY));
    startDay.set(Calendar.MINUTE, masterEndDate.get(Calendar.MINUTE));
    startDay.set(Calendar.SECOND, masterEndDate.get(Calendar.SECOND));
    final int daysBetween = CalendarUtils.daysBetween(masterStartDate, masterEndDate);
    if (daysBetween > 0) {
      // The event ends at another day:
      startDay.add(Calendar.DAY_OF_YEAR, daysBetween);
    }
    this.endDate = startDay.getTime();
  }

  /**
   * @return the master
   */
  public TeamEventDO getMaster()
  {
    return master;
  }

  /**
   * @see org.projectforge.plugins.teamcal.event.TeamEvent#getUid()
   */
  @Override
  public String getUid()
  {
    return master.getUid();
  }

  /**
   * @see org.projectforge.plugins.teamcal.event.TeamEvent#getSubject()
   */
  @Override
  public String getSubject()
  {
    return master.getSubject();
  }

  /**
   * @see org.projectforge.plugins.teamcal.event.TeamEvent#getLocation()
   */
  @Override
  public String getLocation()
  {
    return master.getLocation();
  }

  /**
   * @see org.projectforge.plugins.teamcal.event.TeamEvent#isAllDay()
   */
  @Override
  public boolean isAllDay()
  {
    return master.isAllDay();
  }

  /**
   * @see org.projectforge.plugins.teamcal.event.TeamEvent#getStartDate()
   */
  @Override
  public Date getStartDate()
  {
    return startDate;
  }

  /**
   * @see org.projectforge.plugins.teamcal.event.TeamEvent#getEndDate()
   */
  @Override
  public Date getEndDate()
  {
    return endDate;
  }

  /**
   * @see org.projectforge.plugins.teamcal.event.TeamEvent#getNote()
   */
  @Override
  public String getNote()
  {
    return master.getNote();
  }
}
