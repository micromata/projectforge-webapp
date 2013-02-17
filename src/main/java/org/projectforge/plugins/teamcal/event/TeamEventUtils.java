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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.StringTokenizer;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;

import org.projectforge.calendar.CalendarUtils;
import org.projectforge.calendar.ICal4JUtils;
import org.projectforge.common.DateHelper;
import org.projectforge.common.RecurrenceFrequency;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TeamEventUtils
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventUtils.class);

  private static final RecurrenceFrequency[] SUPPORTED_INTERVALS = new RecurrenceFrequency[] { RecurrenceFrequency.NONE,
    RecurrenceFrequency.DAILY, RecurrenceFrequency.WEEKLY, RecurrenceFrequency.MONTHLY, RecurrenceFrequency.YEARLY};

  public static VEvent createVEvent(final TeamEventDO eventDO, final TimeZone timezone)
  {
    final VEvent vEvent = ICal4JUtils.createVEvent(eventDO.getStartDate(), eventDO.getEndDate(), eventDO.getUid(), eventDO.getSubject(),
        eventDO.isAllDay(), timezone);
    if (eventDO.hasRecurrence() == true) {
      final RRule rrule = eventDO.getRecurrenceRuleObject();
      vEvent.getProperties().add(rrule);
    }
    return vEvent;
  }

  public static String calculateRRule(final TeamEventRecurrenceData recurData)
  {
    if (recurData == null || recurData.getFrequency() == null || recurData.getFrequency() == RecurrenceFrequency.NONE) {
      return null;
    }
    final Recur recur = new Recur();
    final net.fortuna.ical4j.model.Date untilDate = ICal4JUtils.getICal4jDate(recurData.getUntil(), recurData.getTimeZone());
    if (untilDate != null) {
      recur.setUntil(untilDate);
    }
    recur.setInterval(recurData.getInterval());
    recur.setFrequency(ICal4JUtils.getCal4JFrequencyString(recurData.getFrequency()));
    final RRule rrule = new RRule(recur);
    return rrule.getValue();
  }

  public static Collection<TeamEvent> getRecurrenceEvents(final Date startDate, final Date endDate, final TeamEventDO event,
      final java.util.TimeZone timeZone)
      {
    if (event.hasRecurrence() == false) {
      return null;
    }
    final Recur recur = event.getRecurrenceObject();
    if (recur == null) {
      // Shouldn't happen:
      return null;
    }
    final net.fortuna.ical4j.model.DateTime ical4jStartDate = new net.fortuna.ical4j.model.DateTime(startDate);
    // ical4jStartDate.setTimeZone(ICal4JUtils.getTimeZone(timeZone));
    final net.fortuna.ical4j.model.DateTime ical4jEndDate = new net.fortuna.ical4j.model.DateTime(endDate);
    // ical4jEndDate.setTimeZone(ICal4JUtils.getTimeZone(timeZone));
    net.fortuna.ical4j.model.DateTime seedDate;
    seedDate = ICal4JUtils.getICal4jDateTime(event.getStartDate(), timeZone);
    if (ical4jStartDate == null || ical4jEndDate == null || seedDate == null) {
      log.error("Can't get recurrence events of event "
          + event.getId()
          + ". Not all three dates are given: startDate="
          + ical4jStartDate
          + ", endDate="
          + ical4jEndDate
          + ", seed="
          + seedDate);
      return null;
    }
    final Collection<Calendar> exDates = getExDates(event.getRecurrenceExDate());
    final DateList dateList = recur.getDates(seedDate, ical4jStartDate, ical4jEndDate, Value.DATE_TIME);
    final Collection<TeamEvent> col = new ArrayList<TeamEvent>();
    if (dateList != null) {
      OuterLoop: for (final Object obj : dateList) {
        final net.fortuna.ical4j.model.DateTime dateTime = (net.fortuna.ical4j.model.DateTime) obj;
        final Calendar startDay = Calendar.getInstance(timeZone);
        startDay.setTime(dateTime);
        final Calendar masterStartDate = Calendar.getInstance(timeZone);
        masterStartDate.setTime(event.getStartDate());
        if (exDates != null && exDates.size() > 0) {
          for (final Calendar exDate : exDates) {
            if (CalendarUtils.isSameDay(startDay, exDate) == true) {
              // this date is part of ex dates, so don't use it.
              continue OuterLoop;
            }
          }
        }
        if (CalendarUtils.isSameDay(startDay, masterStartDate) == true) {
          // Put event itself to the list.
          col.add(event);
        } else {
          final TeamRecurrenceEvent recurEvent = new TeamRecurrenceEvent(event, startDay);
          col.add(recurEvent);
        }
      }
    }
    return col;
      }

  public static Collection<Calendar> getExDates(final String recurrenceExdate)
  {
    final Collection<Calendar> exDates = new LinkedList<Calendar>();
    if (recurrenceExdate == null) {
      return exDates;
    }
    final StringTokenizer tokenizer = new StringTokenizer(recurrenceExdate, ",");
    while (tokenizer.hasMoreTokens() == true) {
      final String dateString = tokenizer.nextToken().trim();
      final net.fortuna.ical4j.model.Date date = ICal4JUtils.parseICal4jDate(dateString);
      if (date != null) {
        final Calendar cal = Calendar.getInstance(DateHelper.UTC);
        cal.setTime(date);
        exDates.add(cal);
      }
    }
    return exDates;
  }

  public static TeamEventDO createTeamEventDO(final VEvent event)
  {
    final TeamEventDO teamEvent = new TeamEventDO();
    final DtStart dtStart = event.getStartDate();
    final String value = dtStart.toString();
    if (value.indexOf("VALUE=DATE") >= 0) {
      teamEvent.setAllDay(true);
    }
    Timestamp timestamp = ICal4JUtils.getSqlTimestamp(dtStart.getDate());
    teamEvent.setStartDate(timestamp);
    if (teamEvent.isAllDay() == true) {
      final org.joda.time.DateTime jodaTime = new org.joda.time.DateTime(event.getEndDate().getDate());
      final net.fortuna.ical4j.model.Date fortunaEndDate = new net.fortuna.ical4j.model.Date(jodaTime.plusDays(-1).toDate());
      timestamp = new Timestamp(fortunaEndDate.getTime());
    } else {
      timestamp = ICal4JUtils.getSqlTimestamp(event.getEndDate().getDate());
    }
    teamEvent.setEndDate(timestamp);
    if (event.getUid() != null) {
      teamEvent.setExternalUid(event.getUid().getValue());
    }
    if (event.getLocation() != null) {
      teamEvent.setLocation(event.getLocation().getValue());
    }
    if (event.getDescription() != null) {
      teamEvent.setNote(event.getDescription().getValue());
    }
    if (event.getSummary() != null) {
      teamEvent.setSubject(event.getSummary().getValue());
    } else {
      teamEvent.setSubject("");
    }
    if (event.getOrganizer() != null) {
      teamEvent.setOrganizer(event.getOrganizer().getValue());
    }
    final RRule rule = (RRule) event.getProperty(Property.RRULE);
    if (rule != null) {
      teamEvent.setRecurrenceRule(rule.getValue());
    }
    final ExDate exDate = (ExDate) event.getProperty(Property.EXDATE);
    if (exDate != null) {
      teamEvent.setRecurrenceExDate(exDate.getValue());
    }
    return teamEvent;
  }

  public static RecurrenceFrequency[] getSupportedRecurrenceIntervals()
  {
    return SUPPORTED_INTERVALS;
  }
}
