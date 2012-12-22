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

package org.projectforge.calendar;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Assert;
import net.fortuna.ical4j.model.Recur;

import org.junit.Test;
import org.projectforge.common.DateHelper;
import org.projectforge.common.RecurrenceFrequency;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventRecurrenceData;

public class ICal4JUtilsTest
{
  @Test
  public void testRRule()
  {
    TeamEventDO event = createEvent(DateHelper.EUROPE_BERLIN, "2012-12-21 8:30:00.0", "2012-12-21 9:00:00.0", null, 1, null);
    Assert.assertNull(event.getRecurrenceObject());
    event = createEvent(DateHelper.EUROPE_BERLIN, "2012-12-21 8:30:00.0", "2012-12-21 9:00:00.0", RecurrenceFrequency.WEEKLY, 1, null);
    Assert.assertEquals("FREQ=WEEKLY", event.getRecurrenceRule());
    event = createEvent(DateHelper.EUROPE_BERLIN, "2012-12-21 8:30:00.0", "2012-12-21 9:00:00.0", RecurrenceFrequency.WEEKLY, 2, null);
    Assert.assertEquals("FREQ=WEEKLY;COUNT=2", event.getRecurrenceRule());
    Assert.assertNull(event.getRecurrenceUntil());
  }

  private TeamEventDO createEvent(final TimeZone timeZone, final String startDate, final String endDate,
      final RecurrenceFrequency frequency, final int count, final String recurrenceUntil)
  {
    final Timestamp startTimestamp = new Timestamp(DateHelper.parseIsoTimestamp(startDate, timeZone).getTime());
    final Timestamp endTimestamp = new Timestamp(DateHelper.parseIsoTimestamp(endDate, timeZone).getTime());
    final TeamEventDO event = new TeamEventDO();
    event.setStartDate(startTimestamp).setEndDate(endTimestamp);
    final TeamEventRecurrenceData recurData = new TeamEventRecurrenceData();
    recurData.setFrequency(frequency);
    recurData.setCount(count);
    if (recurrenceUntil != null) {
      final Date recurrenceUntilDate = DateHelper.parseIsoDate(recurrenceUntil, timeZone);
      event.setRecurrenceUntil(recurrenceUntilDate);
    }
    event.setRecurrence(recurData);
    assertRecurrence(event, timeZone, frequency, count, recurrenceUntil);
    return event;
  }

  private void assertRecurrence(final TeamEventDO event, final TimeZone timeZone, final RecurrenceFrequency frequency, final int count,
      final String utcRecurrenceUntil)
  {
    final Recur recur = event.getRecurrenceObject();
    if (frequency == null) {
      Assert.assertNull(recur);
      Assert.assertNull(event.getRecurrenceUntil());
      return;
    }
    Assert.assertEquals(frequency, ICal4JUtils.getFrequency(recur));
    if (count == 1) {
      Assert.assertEquals(-1, recur.getCount());
    } else {
      Assert.assertEquals(count, recur.getCount());
    }
    if (utcRecurrenceUntil == null) {
      Assert.assertNull(event.getRecurrenceUntil());
    } else {
      final String utcString = DateHelper.formatIsoTimestamp(event.getRecurrenceUntil(), timeZone);
      Assert.assertEquals(utcRecurrenceUntil, utcString);
    }
  }
}
