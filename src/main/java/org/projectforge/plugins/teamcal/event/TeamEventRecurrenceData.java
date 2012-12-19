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


import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Recur;

import org.projectforge.calendar.ICal4JUtils;
import org.projectforge.common.RecurrenceInterval;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TeamEventRecurrenceData
{
  private RecurrenceInterval interval = RecurrenceInterval.NONE;

  private Date until;

  private int count;

  public TeamEventRecurrenceData()
  {
  }

  public TeamEventRecurrenceData(final Recur recur)
  {
    this.count = recur.getCount();
    this.until = recur.getUntil();
    this.interval = ICal4JUtils.getFrequency(recur);
  }

  /**
   * @return the interval
   */
  public RecurrenceInterval getInterval()
  {
    return interval;
  }

  /**
   * @param interval the interval to set
   * @return this for chaining.
   */
  public TeamEventRecurrenceData setInterval(final RecurrenceInterval interval)
  {
    this.interval = interval;
    return this;
  }

  /**
   * @return the until
   */
  public Date getUntil()
  {
    return until;
  }

  /**
   * @param until the until to set
   * @return this for chaining.
   */
  public TeamEventRecurrenceData setUntil(final Date until)
  {
    this.until = until;
    return this;
  }

  /**
   * @return the count
   */
  public int getCount()
  {
    return count;
  }

  /**
   * @param count the count to set
   * @return this for chaining.
   */
  public TeamEventRecurrenceData setCount(final int count)
  {
    this.count = count;
    return this;
  }
}
