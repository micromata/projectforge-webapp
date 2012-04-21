/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import net.ftlines.wicket.fullcalendar.Config;

import org.apache.wicket.Component;

public class MyFullCalendarConfig extends Config
{
  private static final long serialVersionUID = 6825903475480593064L;

  private int firstDay = 1;

  private String weekMode = "liquid";

  private String allDayText;

  private boolean theme = true;

  private Integer year, month, dayOfMonth;

  private final Component parent;

  /**
   * @param parent Used for localization.
   * @see Component#getString(String)
   */
  public MyFullCalendarConfig(final Component parent)
  {
    this.parent = parent;
    // setAspectRatio(1.5f);
    setSlotMinutes(15);
    setFirstHour(8);
    getHeader().setLeft("prev,next today");
    getHeader().setCenter("title");
    getHeader().setRight("month,agendaWeek,agendaDay");

    getButtonText().setToday(getString("calendar.today"));
    getButtonText().setWeek(getString("calendar.week"));
    getButtonText().setMonth(getString("calendar.month"));
    getButtonText().setDay(getString("calendar.day"));
    setAllDayText(getString("calendar.allday"));
  }

  /**
   * @return the firstDay
   */
  public int getFirstDay()
  {
    return firstDay;
  }

  /**
   * @param firstDay the firstDay to set
   * @return this for chaining.
   */
  public MyFullCalendarConfig setFirstDay(final int firstDay)
  {
    this.firstDay = firstDay;
    return this;
  }

  /**
   * @return the weekMode
   */
  public String getWeekMode()
  {
    return weekMode;
  }

  /**
   * @param weekMode the weekMode to set
   * @return this for chaining.
   */
  public MyFullCalendarConfig setWeekMode(final String weekMode)
  {
    this.weekMode = weekMode;
    return this;
  }

  /**
   * @return the theme
   */
  public boolean isTheme()
  {
    return theme;
  }

  /**
   * @param theme the theme to set
   * @return this for chaining.
   */
  public MyFullCalendarConfig setTheme(final boolean theme)
  {
    this.theme = theme;
    return this;
  }

  /**
   * @return the year
   */
  public Integer getYear()
  {
    return year;
  }

  /**
   * @param year the year to set
   * @return this for chaining.
   */
  public MyFullCalendarConfig setYear(final Integer year)
  {
    this.year = year;
    return this;
  }

  /**
   * @return the month
   */
  public Integer getMonth()
  {
    return month;
  }

  /**
   * @param month the month to set
   * @return this for chaining.
   */
  public MyFullCalendarConfig setMonth(final Integer month)
  {
    this.month = month;
    return this;
  }

  /**
   * @return the dayOfMonth
   */
  public Integer getDayOfMonth()
  {
    return dayOfMonth;
  }

  /**
   * @param dayOfMonth the dayOfMonth to set
   * @return this for chaining.
   */
  public MyFullCalendarConfig setDayOfMonth(final Integer dayOfMonth)
  {
    this.dayOfMonth = dayOfMonth;
    return this;
  }

  private String getString(final String key)
  {
    return parent.getString(key);
  }

  /**
   * @return the allDayText
   */
  public String getAllDayText()
  {
    return allDayText;
  }

  /**
   * @param allDayText the allDayText to set
   * @return this for chaining.
   */
  public MyFullCalendarConfig setAllDayText(final String allDayText)
  {
    this.allDayText = allDayText;
    return this;
  }
}
