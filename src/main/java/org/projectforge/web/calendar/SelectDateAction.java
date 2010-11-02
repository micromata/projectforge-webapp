/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.projectforge.address.AddressDao;
import org.projectforge.address.BirthdayAddress;
import org.projectforge.calendar.DayHolder;
import org.projectforge.calendar.MonthHolder;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.calendar.WeekHolder;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.common.StringHelper;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.OrderDirection;
import org.projectforge.core.UserException;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseListActionBean;
import org.projectforge.web.core.FlowScope;
import org.projectforge.web.timesheet.DisplayTimesheet;


/**
 * Calendar view for selecting dates or time periods.
 */
@UrlBinding("/secure/calendar/SelectDate.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/calendar/selectDate.jsp")
public class SelectDateAction extends BaseListActionBean<BaseSearchFilter, TimesheetDao, TimesheetDO>
{
  public static final String PARAM_SHOW_TIMESHEETS = "showTimesheets";
  
  public static final String PARAM_SHOW_BIRTHDAYS = "showBirthdays";

  private static final Logger log = Logger.getLogger(SelectDateAction.class);

  private MonthHolder monthHolder;

  private SelectDateActionSettings settings;

  private SelectDateActionPreferences preferences;

  private TimesheetDao timesheetDao;

  private AddressDao addressDao;

  private String formattedMonthDuration;

  private String USERPREF_KEY = "SelectDate.userPrefs";

  /**
   * The key under which the time period property will be stored in the flow scope by the caller.
   */
  public static final String SETTINGS_KEY = SelectDateAction.class + "__settings";

  /**
   * /** Unused: returns null.
   */
  @Override
  protected List<TimesheetDO> buildList()
  {
    // Do nothing
    return null;
  }

  @Override
  @DefaultHandler
  public Resolution execute()
  {
    String event = getEventKey();
    getSettings();
    String s = getContext().getRequestParameter(PARAM_SHOW_TIMESHEETS);
    if (StringUtils.equals(s, "true") == true) {
      settings.setShowTimesheets(true);
    }
    s = getContext().getRequestParameter(PARAM_SHOW_BIRTHDAYS);
    if (StringUtils.equals(s, "true") == true) {
      settings.setShowBirthdays(true);
      settings.setCurrent(getContext().now());
    }
    if (StringHelper.startsWith(getEventKey(), "event.") == true) {
      DateHolder date = new DateHolder(settings.getCurrent(), DatePrecision.DAY);
      if ("event.previous".equals(event) == true) {
        date.add(Calendar.MONTH, -1);
      } else if ("event.next".equals(event) == true) {
        date.add(Calendar.MONTH, +1);
      }
      settings.setCurrent(date.getDate());
    } else if ("today".equals(getEventKey()) == true) {
      settings.setCurrent(getContext().now());
    } else if ("selectMonth".equals(getEventKey()) == true) {
      // User has clicked on the month title and therefore wants to select the whole month
      // as time period.
      DateHolder date = new DateHolder(settings.getCurrent(), DatePrecision.DAY);
      date.setBeginOfMonth();
      Date startTime = date.getDate();
      date.setEndOfMonth();
      Date stopTime = date.getDate();
      return buildResolution(String.valueOf(startTime.getTime()), String.valueOf(stopTime.getTime()));
    } else if ("selectWeek".equals(getEventKey()) == true) {
      // User has clicked on a week title and therefore wants to select the whole week
      // as time period.
      Date selectedDate = DateHelper.parseMillis(getSelectedValue());
      if (selectedDate != null) {
        DateHolder date = new DateHolder(selectedDate, DatePrecision.DAY);
        date.setBeginOfWeek();
        Date startTime = date.getDate();
        date.setEndOfWeek();
        Date stopTime = date.getDate();
        return buildResolution(String.valueOf(startTime.getTime()), String.valueOf(stopTime.getTime()));
      }
    } else if ("selectTimeperiod".equals(getEventKey()) == true) {
      // User has clicked on a break with start and stop time. Start and stop time is separated by an semicolon:
      String[] period = StringUtils.split(getSelectedValue(), ';');
      Validate.isTrue(period.length == 2);
      return buildResolution(period[0], period[1]);
    } else if ("selectStartTime".equals(getEventKey()) == true) {
      // User has clicked on a stop time which will be used as start time for the current time sheet.
      return getCallerResolution(getSelectedValue());
    } else if ("selectStopTime".equals(getEventKey()) == true) {
      // User has clicked on a stop time which will be used as start time for the current time sheet.
      Date time = DateHelper.parseMillis(getSelectedValue());
      // Note: start time only as date for preserving time.
      return buildResolution(String.valueOf(time.getTime()), String.valueOf(time.getTime()));
    } else if (isSelectMode() == true) {
      persistUserPrefs();
      return processSelectMode();
    }
    persistUserPrefs();
    return getInputPage();
  }

  /**
   * Persists the current date as user preference. Next call of calendar will show the current date as default.
   */
  private void persistUserPrefs()
  {
    getPreferences().setCurrent(getSettings().getCurrent()); // Persist to user preferences.
    getContext().putEntry(USERPREF_KEY, preferences, true);
  }

  private RedirectResolution getCallerResolution(String selectedValue)
  {
    FlowScope scope = getFlowScope(false);
    if (scope == null) {
      throw new UserException(UserException.I18N_KEY_FLOWSCOPE_NOT_EXIST);
    }
    String callerUrl = (String) scope.get(getKey4CallerUrl(getClass()));
    // redirect to the caller with the result of the selected value.
    RedirectResolution resolution = getFlowResolution(new RedirectResolution(callerUrl));
    String callerParam = (String) scope.get(getKey4CallerKey(getClass()));
    resolution.addParameter(callerParam, selectedValue);
    return resolution;
  }

  /**
   * User has chosen a period, so the RedirectResolution will be build.
   * @startTime
   * @stopTime
   * @withTimeOfDay If false, the time of day will be ignored.
   * @return
   */
  private Resolution buildResolution(String startTime, String stopTime)
  {
    // redirect to the caller with the result of the selected value.
    RedirectResolution resolution = null;
    if (StringUtils.isNotEmpty(settings.getPeriodStart()) == true) {
      if (log.isDebugEnabled() == true) {
        log.debug("Adding period start: " + startTime);
      }
      resolution = getCallerResolution(stopTime);
      resolution.addParameter(settings.getPeriodStart(), startTime);
    }
    if (StringUtils.isNotEmpty(settings.getPeriodStop()) == true) {
      if (log.isDebugEnabled() == true) {
        log.debug("adding period end: " + stopTime);
      }
      resolution = getCallerResolution(startTime);
      resolution.addParameter(settings.getPeriodStop(), stopTime);
    }
    return resolution;
  }

  @SuppressWarnings("unchecked")
  @Transient
  public MonthHolder getMonthHolder()
  {
    if (monthHolder == null) {
      monthHolder = new MonthHolder(getSettings().getCurrent());
      // Now get all time sheets for the current user for this month:
      TimesheetFilter filter = new TimesheetFilter();
      filter.setUserId(getContext().getUser().getId());
      DayHolder firstDay = monthHolder.getFirstWeek().getFirstDay(); // Is often the last sunday / monday of the previous month.
      DayHolder lastDay = monthHolder.getLastWeek().getLastDay(); // Is often the first monday / sunday of the following month.
      filter.setStartTime(firstDay.getDate());
      filter.setStopTime(lastDay.getDate());
      filter.setOrderType(OrderDirection.ASC);
      if (settings.isShowTimesheets() == true) {
        List<TimesheetDO> timesheets = timesheetDao.getList(filter);
        long monthDuration = 0;
        if (CollectionUtils.isNotEmpty(timesheets) == true) {
          Iterator<TimesheetDO> it = timesheets.iterator();
          TimesheetDO timesheet = it.next(); // Start with first time sheet
          Date lastStoptime = null;
          DisplayTimesheet last = null;
          for (WeekHolder week : monthHolder.getWeeks()) {
            long weekDuration = 0;
            for (DayHolder day : week.getDays()) {
              long dayDuration = 0;
              Collection<DisplayTimesheet> col = null;
              while (day.isSameDay(timesheet.getStartTime()) == true) {
                if (col == null) {
                  col = new ArrayList<DisplayTimesheet>();
                  day.addObject("timesheets", col);
                }
                DisplayTimesheet cur = new DisplayTimesheet(timesheet);
                if (lastStoptime != null) {
                  DayHolder d = new DayHolder(lastStoptime);
                  if (d.isSameDay(timesheet.getStartTime()) == true) {
                    if (lastStoptime.before(timesheet.getStartTime()) == true) {
                      // Create empty entry (may be pause):
                      col.add(DisplayTimesheet.createBreak(lastStoptime, timesheet.getStartTime()));
                    } else {
                      // The stop time of the last time sheet is equals to start time of current, so do not display link for this time stamp
                      // (the user
                      // can't have a third time sheet without overlap with this time stamp).
                      cur.setStartTimeLinkEnabled(false);
                      last.setStopTimeLinkEnabled(false);
                    }
                  }
                }
                col.add(cur);
                dayDuration += timesheet.getDuration();
                if (it.hasNext() == false) {
                  break;
                }
                lastStoptime = timesheet.getStopTime();
                last = cur;
                timesheet = it.next();
              }
              day.addObject("duration", formatDuration(dayDuration));
              weekDuration += dayDuration;
              if (monthHolder.containsDay(day) == true) {
                monthDuration += dayDuration;
              }
            }
            if (weekDuration > 0) {
              week.addObject("duration", formatDuration(weekDuration));
            }
          }
        }
        if (monthDuration > 0) {
          formattedMonthDuration = formatDuration(monthDuration);
        }
      } // if (settings.isShowTimesheets() == true)
      if (settings.isShowBirthdays() == true) {
        // February, 29th 2010 fix:
        Date from = firstDay.getDate();
        if (firstDay.getMonth() == Calendar.MARCH && firstDay.getDayOfMonth() == 1) {
          DateHolder dh = new DateHolder(firstDay.getDate());
          dh.add(Calendar.DAY_OF_MONTH, -1); // Take birthday from February 29th into March, 1st.
          from = dh.getDate();
        }
        Set<BirthdayAddress> set = addressDao.getBirthdays(from, lastDay.getDate(), 100, true);
        if (CollectionUtils.isNotEmpty(set) == true) {
          Collection<BirthdayAddress> col = null;
          DayHolder day;
          for (BirthdayAddress ba : set) {
            day = monthHolder.getDay(ba.getMonth(), ba.getDayOfMonth());
            if (day == null) { // February, 29th fix:
              if (ba.getMonth() == Calendar.FEBRUARY && ba.getDayOfMonth() == 29) {
                day = monthHolder.getDay(Calendar.MARCH, 1);
              } else {
                log.warn("Oups, day not found in MonthHolder: " + ba.getCompareString());
              }
            }
            if (day == null) {
              continue;
            }
            col = (Collection<BirthdayAddress>) day.getObject("birthdays");
            if (col == null) {
              col = new ArrayList<BirthdayAddress>();
              day.addObject("birthdays", col);
            }
            if (accessChecker.isUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP) == true) {
              ba.setAge(day.getDate());
            }
            col.add(ba);
          }
        }
      }
    }
    return monthHolder;
  }

  protected String formatDuration(long millis)
  {
    int[] fields = TimePeriod.getDurationFields(millis, 8, 200);
    StringBuffer buf = new StringBuffer();
    if (fields[0] > 0) {
      buf.append(fields[0]).append(getLocalizedString("calendar.unit.day")).append(" ");
    }
    buf.append(fields[1]).append(":").append(StringHelper.format2DigitNumber(fields[2])).append(getLocalizedString("calendar.unit.hour"));
    return buf.toString();
  }

  public SelectDateActionSettings getSettings()
  {
    if (settings == null) {
      FlowScope scope = getFlowScope(false);
      if (scope != null) {
        settings = (SelectDateActionSettings) scope.get(SETTINGS_KEY);
      }
      if (settings == null) {
        setSettings(new SelectDateActionSettings());
        settings.setCurrent(getPreferences().getCurrent());
      }
    }
    return settings;
  }

  public SelectDateActionPreferences getPreferences()
  {
    if (preferences == null) {
      preferences = (SelectDateActionPreferences) getContext().getEntry(USERPREF_KEY);
      if (preferences == null) {
        preferences = new SelectDateActionPreferences();
        getContext().putEntry(USERPREF_KEY, preferences, true);
      }
    }
    return preferences;
  }

  public void setSettings(SelectDateActionSettings settings)
  {
    this.settings = settings;
  }

  public boolean isPeriod()
  {
    return StringUtils.isNotEmpty(getSettings().getPeriodStart()) == true || StringUtils.isNotEmpty(settings.getPeriodStop()) == true;
  }

  public String getMonthDuration()
  {
    return formattedMonthDuration;
  }

  @Override
  protected BaseSearchFilter createFilterInstance()
  {
    return new BaseSearchFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  public void setTimesheetDao(TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }

  public void setAddressDao(AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }
}
