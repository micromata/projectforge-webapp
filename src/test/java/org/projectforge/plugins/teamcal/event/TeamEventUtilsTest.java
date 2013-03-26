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

package org.projectforge.plugins.teamcal.event;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.TimeZone;

import junit.framework.Assert;
import net.fortuna.ical4j.model.Recur;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.calendar.ICal4JUtils;
import org.projectforge.common.DateFormats;
import org.projectforge.common.DateHelper;
import org.projectforge.common.RecurrenceFrequency;
import org.projectforge.plugins.teamcal.TeamCalConfig;

public class TeamEventUtilsTest
{
  @BeforeClass
  public static void setUp()
  {
    TeamCalConfig.__internalSetConfig(new TeamCalConfig().setDomain("projectforge.priv"));
  }

  @Test
  public void testRRule()
  {
    testRRule(DateHelper.EUROPE_BERLIN);
    testRRule(DateHelper.UTC);
    testRRule(TimeZone.getTimeZone("America/Los_Angeles"));
  }

  @Test
  public void recurrenceEvents()
  {
    final TimeZone timeZone = DateHelper.EUROPE_BERLIN;
    {
      final TeamEventDO event = createEvent(timeZone, "2011-06-06 11:00", "2011-06-06 12:00", RecurrenceFrequency.WEEKLY, 1, "2013-12-31");
      final Collection<TeamEvent> col = TeamEventUtils.getRecurrenceEvents(getDate("2013-10-20", timeZone),
          getDate("2013-10-29", timeZone), event, timeZone);
      Assert.assertEquals(2, col.size());
      final Iterator<TeamEvent> it = col.iterator();
      Assert.assertEquals(DateHelper.formatAsUTC(DateHelper.parseIsoTimestamp("2013-10-21 11:00:00.0", timeZone)),
          DateHelper.formatAsUTC(it.next().getStartDate()));
      Assert.assertEquals(DateHelper.formatAsUTC(DateHelper.parseIsoTimestamp("2013-10-28 11:00:00.0", timeZone)),
          DateHelper.formatAsUTC(it.next().getStartDate()));
    }
    {
      final TeamEventDO event = createEvent(timeZone, "2011-03-03 00:00", "2011-03-03 00:00", RecurrenceFrequency.WEEKLY, 2, "2011-04-30")
          .setAllDay(true);
      final Collection<TeamEvent> col = TeamEventUtils.getRecurrenceEvents(getDate("2011-03-01", timeZone),
          getDate("2011-03-31", timeZone), event, timeZone);
      Assert.assertEquals(2, col.size());
      final Iterator<TeamEvent> it = col.iterator();
      Assert.assertEquals(DateHelper.formatAsUTC(DateHelper.parseIsoTimestamp("2011-03-03 00:00:00.0", timeZone)),
          DateHelper.formatAsUTC(it.next().getStartDate()));
      Assert.assertEquals(DateHelper.formatAsUTC(DateHelper.parseIsoTimestamp("2011-03-17 00:00:00.0", timeZone)),
          DateHelper.formatAsUTC(it.next().getStartDate()));
    }
  }

  @Test
  public void exDates()
  {
    testExDates(DateHelper.EUROPE_BERLIN);
    testExDates(TimeZone.getTimeZone("Europe/London"));
    testExDates(TimeZone.getTimeZone("America/Los_Angeles"));
  }

  private void testExDates(final TimeZone timeZone)
  {
    {
      final TeamEventDO event = createEvent(timeZone, "2013-03-21 20:00", "2013-03-21 21:30", RecurrenceFrequency.WEEKLY, 1, null);
      event.addRecurrenceExDate(parseDateTime("2013-03-28 20:00", timeZone), timeZone);
      final Collection<TeamEvent> col = TeamEventUtils.getRecurrenceEvents(getDate("2013-03-01", timeZone),
          getDate("2013-04-05", timeZone), event, timeZone);
      Assert.assertEquals(2, col.size());
      final Iterator<TeamEvent> it = col.iterator();
      Assert.assertEquals(DateHelper.formatAsUTC(DateHelper.parseIsoTimestamp("2013-03-21 20:00:00.0", timeZone)),
          DateHelper.formatAsUTC(it.next().getStartDate()));
      Assert.assertEquals(DateHelper.formatAsUTC(DateHelper.parseIsoTimestamp("2013-04-04 20:00:00.0", timeZone)),
          DateHelper.formatAsUTC(it.next().getStartDate()));
    }
    {
      final TeamEventDO event = createEvent(timeZone, "2013-03-21 00:00", "2013-03-21 00:00", RecurrenceFrequency.WEEKLY, 1, null)
          .setAllDay(true);
      event.addRecurrenceExDate(parseDate("2013-03-28", timeZone), timeZone);
      final Collection<TeamEvent> col = TeamEventUtils.getRecurrenceEvents(getDate("2013-03-01", timeZone),
          getDate("2013-04-05", timeZone), event, timeZone);
      Assert.assertEquals(2, col.size());
      final Iterator<TeamEvent> it = col.iterator();
      TeamEvent e = it.next();
      Assert.assertEquals("2013-03-21 00:00:00.000", DateHelper.formatIsoTimestamp(e.getStartDate(), timeZone));
      Assert.assertTrue(e instanceof TeamEventDO);
      e = it.next();
      Assert.assertEquals("2013-04-04 00:00:00.000", DateHelper.formatIsoTimestamp(e.getStartDate(), timeZone));
      Assert.assertFalse(e instanceof TeamEventDO);
    }
  }

  private void testRRule(final TimeZone timeZone)
  {
    TeamEventDO event = createEvent(timeZone, "2012-12-21 8:30", "2012-12-21 9:00", null, 1, null);
    Assert.assertNull(event.getRecurrenceObject());

    event = createEvent(timeZone, "2012-12-21 8:30", "2012-12-21 9:00", RecurrenceFrequency.WEEKLY, 1, null);
    Assert.assertEquals("FREQ=WEEKLY", event.getRecurrenceRule());
    Collection<TeamEvent> events = getRecurrenceEvents("2012-12-01", "2013-01-31", timeZone, event);
    assertEvents(events, timeZone, "2012-12-21 08:30", "2012-12-28 08:30", "2013-01-04 08:30", "2013-01-11 08:30", "2013-01-18 08:30",
        "2013-01-25 08:30");

    event = createEvent(timeZone, "2012-12-21 18:30", "2012-12-22 9:00", RecurrenceFrequency.WEEKLY, 2, "2013-01-31");
    Assert.assertEquals("FREQ=WEEKLY;UNTIL=20130131;INTERVAL=2", event.getRecurrenceRule());
    events = getRecurrenceEvents("2012-12-01", "2013-03-31", timeZone, event);
    assertEvents(events, timeZone, "2012-12-21 18:30", "2013-01-04 18:30", "2013-01-18 18:30");
    Assert.assertTrue(events.iterator().next() instanceof TeamEventDO);
  }

  private TeamEventDO createEvent(final TimeZone timeZone, final String startDate, final String endDate,
      final RecurrenceFrequency frequency, final int interval, final String recurrenceUntil)
  {
    final Timestamp startTimestamp = new Timestamp(parseDateTime(startDate, timeZone).getTime());
    final Timestamp endTimestamp = new Timestamp(parseDateTime(endDate, timeZone).getTime());
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

  private Collection<TeamEvent> getRecurrenceEvents(final String startDateString, final String endDateString, final TimeZone timeZone,
      final TeamEventDO event)
      {
    final java.util.Date startDate = DateHelper.parseIsoDate(startDateString, timeZone);
    final java.util.Date endDate = DateHelper.parseIsoDate(endDateString, timeZone);
    return TeamEventUtils.getRecurrenceEvents(startDate, endDate, event, timeZone);
      }

  private java.util.Date parseDateTime(final String dateString, final TimeZone timeZone)
  {
    final DateFormat df = new SimpleDateFormat(DateFormats.ISO_TIMESTAMP_MINUTES);
    df.setTimeZone(timeZone);
    try {
      return df.parse(dateString);
    } catch (final ParseException ex) {
      Assert.fail("Can't parse dateString '" + dateString + "': " + ex.getMessage());
      return null;
    }
  }

  private java.util.Date parseDate(final String dateString, final TimeZone timeZone)
  {
    final DateFormat df = new SimpleDateFormat(DateFormats.ISO_DATE);
    df.setTimeZone(timeZone);
    try {
      return df.parse(dateString);
    } catch (final ParseException ex) {
      Assert.fail("Can't parse dateString '" + dateString + "': " + ex.getMessage());
      return null;
    }
  }

  private void assertEvents(final Collection<TeamEvent> events, final TimeZone timeZone, final String... startDates)
  {
    Assert.assertEquals(startDates.length, events.size());
    int i = 0;
    final DateFormat df = new SimpleDateFormat(DateFormats.ISO_TIMESTAMP_MINUTES);
    df.setTimeZone(timeZone);
    for (final TeamEvent event : events) {
      if (event instanceof TeamRecurrenceEvent) {
        final long duration = ((TeamRecurrenceEvent) event).getMaster().getDuration();
        Assert.assertEquals(duration, event.getEndDate().getTime() - event.getStartDate().getTime());
      }
      final String startDate = startDates[i];
      Assert.assertEquals(startDate, df.format(event.getStartDate()));
      ++i;
    }
  }

  net.fortuna.ical4j.model.Date getDate(final String dateString, final TimeZone timeZone)
  {
    final java.util.Date date = DateHelper.parseIsoDate(dateString, timeZone);
    return ICal4JUtils.getICal4jDate(date, timeZone);
  }
}
