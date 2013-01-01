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

package org.projectforge.calendar;

import java.text.ParseException;
import java.util.Date;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Dates;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.DateFormats;
import org.projectforge.common.DateHelper;
import org.projectforge.common.RecurrenceFrequency;
import org.projectforge.user.PFUserContext;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ICal4JUtils
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ICal4JUtils.class);

  private static TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();

  /**
   * @return The timeZone (ical4j) built of the default java timeZone of the user.
   * @see PFUserContext#getTimeZone()
   */
  public static TimeZone getUserTimeZone()
  {
    return registry.getTimeZone(PFUserContext.getTimeZone().getID());
  }

  /**
   * @return The timeZone (ical4j) built of the default java timeZone of the user.
   * @see PFUserContext#getTimeZone()
   */
  public static TimeZone getTimeZone(final java.util.TimeZone timeZone)
  {
    return registry.getTimeZone(timeZone.getID());
  }

  public static VEvent createVEvent(final Date startDate, final Date endDate, final String uid, final String summary)
  {
    return createVEvent(startDate, endDate, uid, summary, false);
  }

  public static VEvent createVEvent(final Date startDate, final Date endDate, final String uid, final String summary, final boolean allDay)
  {
    final TimeZone timezone = getUserTimeZone();
    return createVEvent(startDate, endDate, uid, summary, allDay, timezone);
  }

  public static VEvent createVEvent(final Date startDate, final Date endDate, final String uid, final String summary, final boolean allDay,
      final TimeZone timezone)
  {
    VEvent vEvent;
    if (allDay == true) {
      final Date startUtc = CalendarUtils.getUTCMidnightDate(startDate);
      final Date endUtc = CalendarUtils.getUTCMidnightDate(endDate);
      final net.fortuna.ical4j.model.Date fortunaStartDate = new net.fortuna.ical4j.model.Date(startUtc);
      final org.joda.time.DateTime jodaTime = new org.joda.time.DateTime(endUtc);
      // requires plus 1 because one day will be omitted by calendar.
      final net.fortuna.ical4j.model.Date fortunaEndDate = new net.fortuna.ical4j.model.Date(jodaTime.plusDays(1).toDate());
      vEvent = new VEvent(fortunaStartDate, fortunaEndDate, summary);
    } else {
      final net.fortuna.ical4j.model.DateTime fortunaStartDate = new net.fortuna.ical4j.model.DateTime(startDate);
      fortunaStartDate.setTimeZone(timezone);
      final net.fortuna.ical4j.model.DateTime fortunaEndDate = new net.fortuna.ical4j.model.DateTime(endDate);
      fortunaEndDate.setTimeZone(timezone);
      vEvent = new VEvent(fortunaStartDate, fortunaEndDate, summary);
      vEvent.getProperties().add(timezone.getVTimeZone().getTimeZoneId());
    }
    vEvent.getProperties().add(new Uid(uid));
    return vEvent;
  }

  /**
   * 
   * @param rruleString
   * @return null if rruleString is empty, otherwise new RRule object.
   */
  public static RRule calculateRecurrenceRule(final String rruleString)
  {
    if (StringUtils.isBlank(rruleString) == true) {
      return null;
    }
    try {
      final RRule rule = new RRule(rruleString);
      return rule;
    } catch (final ParseException ex) {
      log.error("Exception encountered while parsing rrule '" + rruleString + "': " + ex.getMessage(), ex);
      return null;
    }
  }

  /**
   * @param rruleString
   * @see ICal4JUtils#calculateRecurrenceRule(String)
   * @see RRule#getRecur()
   */
  public static Recur calculateRecurrence(final String rruleString)
  {
    final RRule rule = calculateRecurrenceRule(rruleString);
    return rule != null ? rule.getRecur() : null;
  }

  public static Date calculateRecurrenceUntil(final String rruleString)
  {
    if (StringUtils.isBlank(rruleString) == true) {
      return null;
    }
    final Recur recur = calculateRecurrence(rruleString);
    if (recur == null) {
      return null;
    }
    return recur.getUntil();
  }

  public static String getCal4JFrequencyString(final RecurrenceFrequency interval)
  {
    if (interval == RecurrenceFrequency.DAILY) {
      return Recur.DAILY;
    } else if (interval == RecurrenceFrequency.WEEKLY) {
      return Recur.WEEKLY;
    } else if (interval == RecurrenceFrequency.MONTHLY) {
      return Recur.MONTHLY;
    } else if (interval == RecurrenceFrequency.YEARLY) {
      return Recur.YEARLY;
    }
    return null;
  }

  /**
   * @param recur
   * @return
   */
  public static RecurrenceFrequency getFrequency(final Recur recur)
  {
    if (recur == null) {
      return null;
    }
    final String freq = recur.getFrequency();
    if (Recur.WEEKLY.equals(freq) == true) {
      return RecurrenceFrequency.WEEKLY;
    } else if (Recur.MONTHLY.equals(freq) == true) {
      return RecurrenceFrequency.MONTHLY;
    } else if (Recur.DAILY.equals(freq) == true) {
      return RecurrenceFrequency.DAILY;
    } else if (Recur.YEARLY.equals(freq) == true) {
      return RecurrenceFrequency.YEARLY;
    }
    return null;
  }

  /**
   * @param recur
   * @return
   */
  public static String getFrequency(final RecurrenceFrequency interval)
  {
    if (interval == null) {
      return null;
    }
    if (interval == RecurrenceFrequency.WEEKLY) {
      return Recur.WEEKLY;
    } else if (interval == RecurrenceFrequency.DAILY) {
      return Recur.DAILY;
    } else if (interval == RecurrenceFrequency.MONTHLY) {
      return Recur.MONTHLY;
    } else if (interval == RecurrenceFrequency.YEARLY) {
      return Recur.YEARLY;
    }
    return null;
  }

  public static java.sql.Date getSqlDate(final net.fortuna.ical4j.model.Date ical4jDate)
  {
    if (ical4jDate == null) {
      return null;
    }
    return new java.sql.Date(ical4jDate.getTime());
  }

  public static net.fortuna.ical4j.model.DateTime getICal4jDateTime(final java.util.Date javaDate, final java.util.TimeZone timeZone)
  {
    if (javaDate == null) {
      return null;
    }
    final String dateString = DateHelper.formatIsoTimestamp(javaDate, timeZone);
    final String pattern = DateFormats.ISO_TIMESTAMP_SECONDS;
    try {
      final net.fortuna.ical4j.model.DateTime dateTime = new net.fortuna.ical4j.model.DateTime(dateString, pattern, getTimeZone(timeZone));
      return dateTime;
    } catch (final ParseException ex) {
      log.error("Can't parse date '" + dateString + "' with pattern '" + pattern + "': " + ex.getMessage(), ex);
      return null;
    }
  }

  public static net.fortuna.ical4j.model.Date getICal4jDate(final java.util.Date javaDate, final java.util.TimeZone timeZone)
  {
    if (javaDate == null) {
      return null;
    }
    return new MyIcal4JDate(javaDate, timeZone);
  }

  private static class MyIcal4JDate extends net.fortuna.ical4j.model.Date
  {
    private static final long serialVersionUID = 341788808291157447L;

    MyIcal4JDate(final java.util.Date javaDate, final java.util.TimeZone timeZone)
    {
      super(javaDate.getTime(), Dates.PRECISION_DAY, timeZone);
    }
  }
}
