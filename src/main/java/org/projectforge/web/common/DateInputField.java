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

package org.projectforge.web.common;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.common.KeyValueBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;


@Deprecated
public class DateInputField implements Serializable
{
  private static final long serialVersionUID = -2683509191576166347L;

  private static final List<KeyValueBean<Integer, String>> HOURS_OF_DAY;

  private static final List<KeyValueBean<Integer, String>> MINUTES_1;

  private static final List<KeyValueBean<Integer, String>> MINUTES_15;

  private static KeyValueBean<Integer, String> get(Integer i, String s)
  {
    return new KeyValueBean<Integer, String>(i, s);
  }

  static {
    HOURS_OF_DAY = new ArrayList<KeyValueBean<Integer, String>>();
    for (int i = 0; i <= 23; i++) {
      HOURS_OF_DAY.add(get(i, StringHelper.format2DigitNumber(i)));
    }
    MINUTES_1 = new ArrayList<KeyValueBean<Integer, String>>();
    for (int i = 0; i <= 59; i++) {
      MINUTES_1.add(get(i, StringHelper.format2DigitNumber(i)));
    }
    MINUTES_15 = new ArrayList<KeyValueBean<Integer, String>>();
    MINUTES_15.add(get(0, "00"));
    MINUTES_15.add(get(15, "15"));
    MINUTES_15.add(get(30, "30"));
    MINUTES_15.add(get(45, "45"));
  }

  DateHolder dateHolder;

  public DateInputField()
  {
    dateHolder = new DateHolder();
  }

  public DateInputField(DatePrecision precision)
  {
    dateHolder = new DateHolder(precision);
  }

  public DateInputField(Date date, DatePrecision precision)
  {
    dateHolder = new DateHolder(date, precision);
  }

  /**
   * @param src
   * @see DateHolder#setDay(Calendar)
   */
  public void setDay(Calendar src)
  {
    dateHolder.setDay(src);
  }

  /**
   * @param field
   * @param amount
   * @see DateHolder#add(int, int)
   */
  public void add(int field, int amount)
  {
    dateHolder.add(field, amount);
  }

  /**
   * Gets the time of day in milliseconds since midnight. This method is used for comparing the times.
   * @return
   */
  public long getTimeOfDay()
  {
    return getHourOfDay() * 3600 + getMinute() * 60 + dateHolder.getSecond();
  }

  public Date getDate()
  {
    return dateHolder.getDate();
  }

  public Calendar getCalendar()
  {
    return dateHolder.getCalendar();
  }

  /**
   * Parses the given string as long (UTC time in millis) and creates the date.
   * @param str
   */
  public void setDateString(String str)
  {
    long utcMillis = NumberHelper.parseLong(str);
    this.dateHolder.setDate(utcMillis);
  }

  public void setDate(Date date)
  {
    this.dateHolder.setDate(date);
  }

  @Transient
  public Timestamp getTimestamp()
  {
    return this.dateHolder.getTimestamp();
  }

  public int getHourOfDay()
  {
    return dateHolder.getHourOfDay();
  }

  public void setHourOfDay(int hourOfDay)
  {
    dateHolder.setHourOfDay(hourOfDay);
  }

  public int getMinute()
  {
    return dateHolder.getMinute();
  }

  public void setMinute(int minute)
  {
    dateHolder.setMinute(minute);
  }

  public void ensurePresicion()
  {
    dateHolder.ensurePrecision();
  }

  /**
   * @return List of KeyValueBeans (0, "00"), (1, "01"), ... (23, "23").
   */
  public List<KeyValueBean<Integer, String>> getHoursOfDay()
  {
    return HOURS_OF_DAY;
  }

  /**
   * For DatePrecision.MINUTE_15: (0, "00"), (15, "15"), (30, "30"), (45, "45"), otherwise (0, "00"), (1, "01"), ...(23, "23").
   * @return List of KeyValueBeans
   */
  public List<KeyValueBean<Integer, String>> getMinutes()
  {
    if (dateHolder.getPrecision() == DatePrecision.MINUTE_15) {
      return MINUTES_15;
    }
    return MINUTES_1;
  }

  /**
   * For DatePrecision.MINUTE_15: (0, "00"), (15, "15"), (30, "30"), (45, "45").
   * @return List of KeyValueBeans
   */
  public List<KeyValueBean<Integer, String>> getMinutes_15()
  {
    return MINUTES_15;
  }

  public String toString()
  {
    return dateHolder.getDate().toString();
  }
}
