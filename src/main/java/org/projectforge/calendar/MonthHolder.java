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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;


/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MonthHolder
{
  /** Keys of the month names (e. g. needed for I18n). */
  public static final String MONTH_KEYS[] = new String[] { "january", "february", "march", "april", "may", "june", "july", "august",
      "september", "october", "november", "december"};

  private List<WeekHolder> weeks;

  private int year = -1;

  private int month = -1;

  private Calendar cal;

  private Date begin;

  private Date end;

  public MonthHolder()
  {

  }

  /**
   * 
   * @param month Can also be one month before or after if the day of the weeks of this month have an overlap to the nearby months.
   * @param dayOfMonth
   * @return null, if the demanded day is not member of the weeks of the MonthHolder.
   */
  public DayHolder getDay(int month, int dayOfMonth)
  {
    for (WeekHolder week : weeks) {
      for (DayHolder day : week.getDays()) {
        if (day.getMonth() == month && day.getDayOfMonth() == dayOfMonth) {
          return day;
        }
      }
    }
    return null;
  }

  /** Initializes month containing all days of actual month. */
  public MonthHolder(TimeZone timeZone, Locale locale)
  {
    cal = Calendar.getInstance(timeZone, locale);
    calculate();
  }

  public MonthHolder(final Date date, final TimeZone timeZone)
  {
    cal = DateHelper.getCalendar(timeZone, Locale.GERMAN);
    cal.setTime(date);
    calculate();
  }

  public MonthHolder(Date date)
  {
    cal = DateHelper.getCalendar(Locale.GERMAN);
    cal.setTime(date);
    calculate();
  }

  private void calculate()
  {
    DateHolder dateHolder = new DateHolder(cal, DatePrecision.DAY);
    year = dateHolder.getYear();
    month = dateHolder.getMonth();
    dateHolder.setBeginOfMonth();
    begin = dateHolder.getDate(); // Storing begin of month.
    dateHolder.setEndOfMonth();
    end = dateHolder.getDate(); // Storing end of month.
    dateHolder.setDate(begin); // reset to begin of month
    dateHolder.computeTime();
    dateHolder.setBeginOfWeek(); // get first week (with days of previous month)

    weeks = new ArrayList<WeekHolder>();
    do {
      WeekHolder week = new WeekHolder(dateHolder.getCalendar(), month);
      weeks.add(week);
      dateHolder.add(Calendar.WEEK_OF_YEAR, 1);
    } while (dateHolder.getMonth() == month);
  }

  public int getYear()
  {
    return year;
  }

  public int getMonth()
  {
    return month;
  }

  public String getMonthKey()
  {
    if (month < 0 || month >= MONTH_KEYS.length) {
      return "unknown";
    }
    return MONTH_KEYS[month];
  }

  public WeekHolder getFirstWeek()
  {
    return getWeeks().get(0);
  }

  public WeekHolder getLastWeek()
  {
    return weeks.get(weeks.size() - 1);
  }

  public List<WeekHolder> getWeeks()
  {
    return weeks;
  }

  public Date getBegin()
  {
    return begin;
  }

  public Date getEnd()
  {
    return end;
  }

  /**
   * Is the given day member of the current month?
   * @param day
   * @return
   */
  public boolean containsDay(DayHolder day)
  {
    return (day.getDate().before(begin) == false && day.getDate().after(end) == false);
  }

  public BigDecimal getNumberOfWorkingDays()
  {
    final DayHolder from = new DayHolder(this.begin);
    final DayHolder to = new DayHolder(this.end);
    return DayHolder.getNumberOfWorkingDays(from, to);
  }
}
