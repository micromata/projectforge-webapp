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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.RRule;

import org.projectforge.calendar.CalendarUtils;
import org.projectforge.calendar.ICal4JUtils;
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
    final net.fortuna.ical4j.model.Date ical4jStartDate = ICal4JUtils.getICal4jDate(startDate, timeZone);
    final net.fortuna.ical4j.model.Date ical4jEndDate = ICal4JUtils.getICal4jDate(endDate, timeZone);
    final net.fortuna.ical4j.model.Date seed = ICal4JUtils.getICal4jDate(event.getStartDate(), timeZone);
    if (ical4jStartDate == null || ical4jEndDate == null || seed == null) {
      log.error("Can't get recurrence events of event "
          + event.getId()
          + ". Not all three dates are given: startDate="
          + ical4jStartDate
          + ", endDate="
          + ical4jEndDate
          + ", seed="
          + seed);
      return null;
    }
    final DateList dateList = recur.getDates(seed, ical4jStartDate, ical4jEndDate, Value.TIME);
    final Collection<TeamEvent> col = new ArrayList<TeamEvent>();
    if (dateList != null) {
      for (final Object obj : dateList) {
        final DateTime dateTime = (DateTime) obj;
        final Calendar startDay = Calendar.getInstance(timeZone);
        startDay.setTime(dateTime);
        final Calendar masterStartDate = Calendar.getInstance(timeZone);
        masterStartDate.setTime(event.getStartDate());
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

  public static RecurrenceFrequency[] getSupportedRecurrenceIntervals()
  {
    return SUPPORTED_INTERVALS;
  }
}
