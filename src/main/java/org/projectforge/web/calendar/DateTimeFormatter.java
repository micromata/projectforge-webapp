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

package org.projectforge.web.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.projectforge.calendar.DayHolder;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.DateFormats;
import org.projectforge.common.DateHelper;
import org.projectforge.renderer.RenderType;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.core.AbstractFormatter;

public class DateTimeFormatter extends AbstractFormatter
{
  private static DateTimeFormatter instance = new DateTimeFormatter();

  public static final DateTimeFormatter instance()
  {
    return instance;
  }

  /** Used by getPrettyFormattedDuration */
  public static final int DEFAULT_HOURS_OF_DAY = 8;

  /** Used by getPrettyFormattedDuration */
  public static final int DEFAULT_MIN_HOURS4DAY_SEPARATION = 24;

  /**
   * @param date
   * @return Return the as two digits formatted week of year. If given date is null then "" is returned.
   * @see DateHelper#getWeekOfYear(Date)
   */
  public static String formatWeekOfYear(final Date date)
  {
    if (date == null) {
      return "";
    }
    final int weekOfYear = DateHelper.getWeekOfYear(date);
    return weekOfYear < 10 ? "0" + weekOfYear : String.valueOf(weekOfYear);
  }

  private HtmlHelper htmlHelper;

  private int durationOfWorkingDay = 8;

  /**
   * Uses patternKey SHORT_DATE_FORMAT
   * @param dateTime
   * @see #getFormattedDateTime(Object, String, Locale, TimeZone)
   */
  public String getFormattedDate(final Object date)
  {
    return getFormattedDate(date, PFUserContext.getLocale(), PFUserContext.getTimeZone());
  }

  /**
   * Uses patternKey SHORT_DATE_FORMAT
   * @param dateTime
   * @see #getFormattedDateTime(Object, String)
   */
  public String getFormattedDate(final Object date, final Locale locale, final TimeZone timeZone)
  {
    return getFormattedDate(date, DateFormats.getFormatString(org.projectforge.common.DateFormatType.DATE), locale, timeZone);
  }

  /**
   * Gets the formatted date (without time of day) with the context user's time zone and the internationalized pattern.
   * @param date
   * @param patternKey i18n key of the pattern
   */
  public String getFormattedDate(final Object date, final String pattern)
  {
    return getFormattedDate(date, pattern, PFUserContext.getLocale(), PFUserContext.getTimeZone());
  }

  /**
   * Gets the formatted date (without time of day) with the context user's time zone and the internationalized pattern.
   * @param date
   * @param patternKey i18n key of the pattern
   */
  public String getFormattedDate(final Object date, final String pattern, final Locale locale, final TimeZone timeZone)
  {
    if (date == null) {
      return "";
    }
    final DateFormat format = locale != null ? new SimpleDateFormat(pattern, locale) : new SimpleDateFormat(pattern);
    if (timeZone != null) {
      format.setTimeZone(timeZone);
    }
    return format.format(date);
  }

  /**
   * Uses patternKey SHORT_TIMESTAMP_FORMAT_WITH_MINUTES
   * @param dateTime
   * @see #getFormattedDateTime(Date, String)
   */
  public String getFormattedDateTime(final Date dateTime)
  {
    return getFormattedDateTime(dateTime, DateFormats.getFormatString(org.projectforge.common.DateFormatType.TIMESTAMP_SHORT_MINUTES));
  }

  /**
   * Uses patternKey SHORT_TIMESTAMP_FORMAT_WITH_MINUTES
   * @param dateTime
   * @see #getFormattedDateTime(Date, String)
   */
  public String getFormattedDateTime(final Date dateTime, final Locale locale, final TimeZone timeZone)
  {
    return getFormattedDateTime(dateTime, DateFormats.getFormatString(org.projectforge.common.DateFormatType.TIMESTAMP_SHORT_MINUTES),
        PFUserContext.getLocale(), PFUserContext.getTimeZone());
  }

  /**
   * Gets the formatted time stamp with the context user's time zone and the internationalized pattern.
   * @param dateTime
   * @param patternKey i18n key of the pattern
   */
  public String getFormattedDateTime(final Date dateTime, final String pattern)
  {
    return getFormattedDateTime(dateTime, pattern, PFUserContext.getLocale(), PFUserContext.getTimeZone());
  }

  /**
   * Gets the formatted time stamp with the context user's time zone and the internationalized pattern.
   * @param dateTime
   * @param patternKey i18n key of the pattern
   */
  public String getFormattedDateTime(final Date dateTime, final String pattern, final Locale locale, final TimeZone timeZone)
  {
    if (dateTime == null) {
      return "";
    }
    final DateFormat format = locale != null ? new SimpleDateFormat(pattern, locale) : new SimpleDateFormat(pattern);
    if (timeZone != null) {
      format.setTimeZone(PFUserContext.getTimeZone());
    }
    return format.format(dateTime);
  }

  /**
   * Uses patternKey TIMEOFDAY_FORMAT
   * @param dateTime
   * @see #getFormattedTime(Date, String)
   */
  public String getFormattedTime(final Date time)
  {
    return getFormattedTime(time, DateFormats.getFormatString(org.projectforge.common.DateFormatType.TIME_OF_DAY_MINUTES));
  }

  /**
   * Gets the formatted time of day with the context user's time zone and the internationalized pattern.
   * @param time
   * @param patternKey i18n key of the pattern
   */
  public String getFormattedTime(final Date time, final String pattern)
  {
    if (time == null) {
      return "";
    }
    final DateFormat format = new SimpleDateFormat(pattern, PFUserContext.getLocale());
    format.setTimeZone(PFUserContext.getTimeZone());
    return format.format(time);
  }

  public String getFormattedDuration(final TimePeriod timePeriod)
  {
    return getFormattedDuration(timePeriod.getDuration());
  }

  /**
   * Calls getFormattedDuration with hoursOfDay = this.durationOfWorkingDay and minHours4DaySeparation = 24.
   * @param millis
   * @return
   * @see #getFormattedDuration(long, int, int)
   */
  public String getFormattedDuration(final long millis)
  {
    return getFormattedDuration(millis, durationOfWorkingDay, 24);
  }

  /**
   * Calls getPrettyFormattedDuration with DEFAULT_HOURS_OF_DAY and DEFAULT_MIN_HOURS4DAY_SEPARATION.
   * @param millis
   * @return
   * @see #getPrettyFormattedDuration(long, int, int)
   */
  public String getPrettyFormattedDuration(final long millis)
  {
    return getPrettyFormattedDuration(millis, DEFAULT_HOURS_OF_DAY, DEFAULT_MIN_HOURS4DAY_SEPARATION);
  }

  /**
   * Examples: 12d 1:00h (97:00h), 9:00h
   * @param millis
   * @param hoursOfDay
   * @param minHours4DaySeparation
   * @return
   */
  public String getPrettyFormattedDuration(final long millis, final int hoursOfDay, final int minHours4DaySeparation)
  {
    final StringBuffer buf = new StringBuffer();
    final String str1 = getFormattedDuration(millis, hoursOfDay, minHours4DaySeparation);
    final String str2 = getFormattedDuration(millis, hoursOfDay, -1);
    buf.append(str1);
    if (str1.equals(str2) == false) {
      buf.append(" (").append(str2).append(")");
    }
    return buf.toString();
  }

  /**
   * Examples of output (localized units): 9:00h, 12d 1:00h
   * @param millis
   * @param hoursOfDay
   * @param minHours4DaySeparation
   * @return
   */
  public String getFormattedDuration(final long millis, final int hoursOfDay, final int minHours4DaySeparation)
  {
    final int[] fields = TimePeriod.getDurationFields(millis, hoursOfDay, minHours4DaySeparation);
    final StringBuffer buf = new StringBuffer();
    if (fields[0] > 0) { // days
      buf.append(fields[0]).append(getI18nMessage("calendar.unit.day")).append(" ");
    }
    buf.append(fields[1]).append(":"); // hours
    formatNumber(buf, fields[2]); // minutes
    buf.append(getI18nMessage("calendar.unit.hour"));
    return buf.toString();
  }

  private void formatNumber(final StringBuffer buf, final long number)
  {
    if (number < 10) {
      buf.append("0");
    }
    buf.append(number);
  }

  public String getFormattedTimePeriod(final TimePeriod timePeriod)
  {
    return getFormattedTimePeriod(timePeriod, RenderType.HTML, true);
  }

  /**
   * 
   * @param timePeriod
   * @param renderType Default is HTML
   * @param multiLines If true, &gt;br/&lt; tags will be used for a multi line output.
   * @return
   */
  public String getFormattedTimePeriod(final TimePeriod timePeriod, final RenderType renderType, final boolean multiLines)
  {
    if (timePeriod == null) {
      return "";
    }
    final StringBuffer buf = new StringBuffer();
    if (timePeriod.getMarker() == true) {
      // Time collision is marked!
      buf.append("<span");
      if (renderType == RenderType.FOP) {
        htmlHelper.attribute(buf, "use-font", "bold");
      } else {
        htmlHelper.attribute(buf, "class", "tp_collision");
      }
      buf.append(">").append("***");
    }
    if (timePeriod.getFromDate() != null) {
      appendCSSDate(buf, timePeriod.getFromDate(), renderType);
      if (timePeriod.getMarker() == true) {
        buf.append("***</span>");
      }
      buf.append(" ");
      appendCSSTime(buf, timePeriod.getFromDate(), renderType);
      if (timePeriod.getToDate() != null) {
        buf.append("-");
        appendCSSTime(buf, timePeriod.getToDate(), renderType);
        final DayHolder day = new DayHolder(timePeriod.getFromDate());
        if (day.isSameDay(timePeriod.getToDate()) == false) {
          if (multiLines == true) {
            buf.append("<br/>(");
          } else {
            buf.append(" (");
          }
          buf.append(getFormattedDate(timePeriod.getToDate())).append(")");
        }
      }
    } else {
      if (timePeriod.getToDate() != null) {
        buf.append(getFormattedDateTime(timePeriod.getFromDate()));
      }
      if (timePeriod.getMarker() == true) {
        buf.append("***</span>");
      }
    }
    return buf.toString();
  }

  private void appendCSSDate(final StringBuffer buf, final Date date, final RenderType renderType)
  {
    buf.append("<span");
    if (renderType == RenderType.FOP) {
      htmlHelper.attribute(buf, "use-font", "bold");
    } else {
      htmlHelper.attribute(buf, "class", "tp_date");
    }
    buf.append(">");
    buf.append(getFormattedDate(date)).append("</span>");
  }

  private void appendCSSTime(final StringBuffer buf, final Date date, final RenderType renderType)
  {
    if (renderType == RenderType.FOP) {
      buf.append(getFormattedTime(date));
    } else {
      buf.append("<span");
      htmlHelper.attribute(buf, "class", "tp_time");
      buf.append(">").append(getFormattedTime(date)).append("</span>");
    }
  }

  public void setHtmlHelper(final HtmlHelper htmlHelper)
  {
    this.htmlHelper = htmlHelper;
  }

  /**
   * Set the default duration of working day (8 at default).
   * @param durationOfWorkingDay
   */
  public void setDurationOfWorkingDay(final int durationOfWorkingDay)
  {
    this.durationOfWorkingDay = durationOfWorkingDay;
  }

  public int getDurationOfWorkingDay()
  {
    return durationOfWorkingDay;
  }
}
