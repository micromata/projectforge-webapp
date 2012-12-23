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
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimeZone;

import junit.framework.Assert;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.common.DateHelper;
import org.projectforge.common.RecurrenceFrequency;
import org.projectforge.plugins.teamcal.TeamCalConfig;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventRecurrenceData;

public class ICal4JUtilsTest
{
  @BeforeClass
  public static void setUp()
  {
    TeamCalConfig.__internalSetConfig(new TeamCalConfig().setDomain("projectforge.priv"));
  }

  @Test
  public void testSqlDate()
  {
    final net.fortuna.ical4j.model.Date date = ICal4JUtils.getICal4jDate(DateHelper.parseIsoDate("2012-12-22", DateHelper.EUROPE_BERLIN),
        DateHelper.EUROPE_BERLIN);
    Assert.assertEquals("20121222", date.toString());
  }

  @Test
  public void testRRule()
  {
    testRRule(DateHelper.EUROPE_BERLIN);
    testRRule(DateHelper.UTC);
    testRRule(TimeZone.getTimeZone("America/Los_Angeles"));
  }

  private void testRRule(final TimeZone timeZone)
  {
    TeamEventDO event = createEvent(timeZone, "2012-12-21 8:30:00.0", "2012-12-21 9:00:00.0", null, 1, null);
    Assert.assertNull(event.getRecurrenceObject());
    event = createEvent(timeZone, "2012-12-21 8:30:00.0", "2012-12-21 9:00:00.0", RecurrenceFrequency.WEEKLY, 1, null);
    Assert.assertEquals("FREQ=WEEKLY", event.getRecurrenceRule());
    event = createEvent(timeZone, "2012-12-21 8:30:00.0", "2012-12-21 9:00:00.0", RecurrenceFrequency.WEEKLY, 2, "2013-01-31");
    Assert.assertEquals("FREQ=WEEKLY;UNTIL=20130131;INTERVAL=2", event.getRecurrenceRule());
    final Collection<VEvent> events = getRecurrenceDates("2012-12-01", "2013-01-31", timeZone, event);
    assertEvents(events, 60, "2012-12-21 08:30:00", "2013-01-04 08:30:00", "2013-01184 08:30:00");
  }

  private TeamEventDO createEvent(final TimeZone timeZone, final String startDate, final String endDate,
      final RecurrenceFrequency frequency, final int interval, final String recurrenceUntil)
  {
    final Timestamp startTimestamp = new Timestamp(DateHelper.parseIsoTimestamp(startDate, timeZone).getTime());
    final Timestamp endTimestamp = new Timestamp(DateHelper.parseIsoTimestamp(endDate, timeZone).getTime());
    final TeamEventDO event = new TeamEventDO();
    event.setStartDate(startTimestamp).setEndDate(endTimestamp);
    final TeamEventRecurrenceData recurData = new TeamEventRecurrenceData(timeZone);
    recurData.setFrequency(frequency);
    recurData.setInterval(interval);
    if (recurrenceUntil != null) {
      final java.sql.Date recurrenceUntilDate = new java.sql.Date(DateHelper.parseIsoDate(recurrenceUntil, timeZone).getTime());
      recurData.setUntil(recurrenceUntilDate);
    }
    event.setRecurrence(recurData);
    assertRecurrence(event, timeZone, frequency, interval, recurrenceUntil);
    return event;
  }

  private void assertRecurrence(final TeamEventDO event, final TimeZone timeZone, final RecurrenceFrequency frequency, final int interval,
      final String utcRecurrenceUntil)
  {
    final Recur recur = event.getRecurrenceObject();
    if (frequency == null) {
      Assert.assertNull(recur);
      Assert.assertNull(event.getRecurrenceUntil());
      return;
    }
    Assert.assertEquals(frequency, ICal4JUtils.getFrequency(recur));
    if (recur.getInterval() > 1) {
      Assert.assertEquals(interval, recur.getInterval());
    } else {
      Assert.assertEquals(-1, recur.getInterval());
    }
    if (utcRecurrenceUntil == null) {
      Assert.assertNull(event.getRecurrenceUntil());
    } else {
      final String utcString = DateHelper.formatIsoDate(event.getRecurrenceUntil(), DateHelper.UTC);
      Assert.assertEquals(utcRecurrenceUntil, utcString);
    }
  }

  private Collection<VEvent> getRecurrenceDates(final String startDateString, final String endDateString, final TimeZone timeZone,
      final TeamEventDO... events)
      {
    final java.util.Date startDate = DateHelper.parseIsoDate(startDateString, timeZone);
    final java.util.Date endDate = DateHelper.parseIsoDate(endDateString, timeZone);
    final Collection<TeamEventDO> col = new ArrayList<TeamEventDO>();
    for (final TeamEventDO event : events) {
      col.add(event);
    }
    return ICal4JUtils.getRecurrenceDates(startDate, endDate, col, timeZone);
      }

  private void assertEvents(final Collection<VEvent> events, final long duration, final String... startDates)
  {
    Assert.assertEquals(startDates.length, events.size());
    int i = 0;
    for (final VEvent event : events) {
      final String startDate = startDates[i];
      Assert.assertEquals(startDate, event.getStartDate().toString());
      ++i;
    }
  }
}
