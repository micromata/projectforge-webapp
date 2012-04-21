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

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Transient;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDao;
import org.projectforge.calendar.DayHolder;
import org.projectforge.calendar.MonthHolder;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.calendar.WeekHolder;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.TooltipImage;

public class OldCalendarPage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final long serialVersionUID = 8710165041912824126L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OldCalendarPage.class);

  private static final String USERPREF_KEY = "CalendarPage.userPrefs";

  private transient OldCalendarMonthHolder monthHolder;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  private OldCalendarForm form;

  private RepeatingView weekRepeater;

  protected ISelectCallerPage caller;

  protected String selectProperty;

  protected boolean selectPeriodMode;

  protected boolean selectStartStopTime;

  protected final PageParameters pageParameters;

  protected final Date date;

  protected Class< ? > targetType;

  private boolean returnAsISODateString;

  /**
   * Don't forget to call init() after configuration.
   * @param caller
   * @param selectProperty
   * @param date
   */
  public OldCalendarPage(final ISelectCallerPage caller, final String selectProperty, final Date date)
  {
    super(new PageParameters());
    this.date = date;
    this.pageParameters = null;
    this.caller = caller;
    this.selectProperty = selectProperty;
  }

  public OldCalendarPage(final PageParameters parameters)
  {
    super(parameters);
    this.pageParameters = parameters;
    this.date = null;
    init();
  }

  public OldCalendarPage setReturnAsIsoDateString()
  {
    returnAsISODateString = true;
    return this;
  }

  /**
   * If true then the user can also choose time periods.
   */
  protected boolean isSelectPeriodMode()
  {
    return selectPeriodMode;
  }

  /**
   * @see #isSelectPeriodMode()
   * @param selectPeriodMode
   */
  public void setSelectPeriodMode(final boolean selectPeriodMode)
  {
    this.selectPeriodMode = selectPeriodMode;
  }

  public void setSelectStartStopTime(final boolean selectStartStopTime)
  {
    this.selectStartStopTime = selectStartStopTime;
  }

  /**
   * @param targetType java.sql.Date is supported.
   */
  public void setTargetType(final Class< ? > targetType)
  {
    this.targetType = targetType;
  }

  @SuppressWarnings("serial")
  public void init()
  {
    form = new OldCalendarForm(this);
    body.add(form);
    CalendarFilter filter = (CalendarFilter) getUserPrefEntry(USERPREF_KEY);
    if (filter == null) {
      filter = new CalendarFilter();
      putUserPrefEntry(USERPREF_KEY, filter, true);
    }
    form.setFilter(filter);
    if (this.date != null) {
      filter.setCurrent(date);
    }
    form.init();

    if (pageParameters != null) {
      if (pageParameters.get("showTimesheets") != null) {
        form.getFilter().setUserId(getUserId());
      }
      if (pageParameters.get("showBirthdays") != null) {
        form.getFilter().setShowBirthdays(true);
      }
    }
    final RepeatingView dayOfWeekLabelRepeater = new RepeatingView("dayOfWeekLabelRepeater");
    body.add(dayOfWeekLabelRepeater);
    for (final DayHolder day : getMonthHolder().getFirstWeek().getDays()) {
      final WebMarkupContainer item = new WebMarkupContainer(dayOfWeekLabelRepeater.newChildId());
      dayOfWeekLabelRepeater.add(item);
      item.add(new Label("dayOfWeekLabel", getString("calendar.shortday." + day.getDayKey())));
    }
    addWeeks();
    body.add(new Label("numberOfWeeksJavascript", new Model<String>() {
      @Override
      public String getObject()
      {
        return "var numberOfWeeks = " + getMonthHolder().getWeeks().size() + ";";
      }
    }).setEscapeModelStrings(false));
  }

  @SuppressWarnings("serial")
  private void addWeeks()
  {
    weekRepeater = new RepeatingView("weeks");
    body.add(weekRepeater);
    for (final WeekHolder week : getMonthHolder().getWeeks()) {
      final WebMarkupContainer item = new WebMarkupContainer(weekRepeater.newChildId());
      weekRepeater.add(item);
      final Link< ? > selectWeekButton = new Link<Void>("selectWeek") {
        @Override
        public void onClick()
        {
          onSelectPeriod(week.getFirstDay().getDate(), week.getLastDay().getDate());
        };
      };
      item.add(selectWeekButton);
      selectWeekButton.add(new TooltipImage("selectWeekHelp", getResponse(), WebConstants.IMAGE_CALENDAR_SELECT_WEEK,
          getString("calendar.tooltip.selectWeek")));
      final Label weekOfYearLabel1 = new Label("weekOfYearLabel", String.valueOf(week.getWeekOfYear()));
      selectWeekButton.add(weekOfYearLabel1);
      final Label weekOfYearLabel2 = new Label("weekOfYearLabel", String.valueOf(week.getWeekOfYear()));
      item.add(weekOfYearLabel2);
      if (isSelectMode() == true && isSelectPeriodMode() == true) {
        weekOfYearLabel2.setVisible(false);
      } else {
        selectWeekButton.setVisible(false);
      }
      // Total duration of all time sheets in current week:
      final Long duration = (Long) week.getObject("duration");
      final String durationString = duration != null ? formatDuration(duration) : "";
      final Label weekDuration = new Label("weekDuration", "<br/>" + durationString);
      weekDuration.setEscapeModelStrings(false);
      if (NumberHelper.greaterZero(duration) == false) {
        weekDuration.setVisible(false);
      }
      item.add(weekDuration);
      addDays(item, week);
    }
  }

  private void addDays(final WebMarkupContainer parent, final WeekHolder week)
  {
    final RepeatingView dayRepeater = new RepeatingView("days");
    parent.add(dayRepeater);
    for (final DayHolder day : week.getDays()) {
      final OldCalendarDayItem dayItem = new OldCalendarDayItem(this, dayRepeater.newChildId(), day);
      dayRepeater.add(dayItem);
      dayItem.init();
    }
  }

  void goToPreviousMonth()
  {
    final DateHolder date = new DateHolder(form.getFilter().getCurrent(), DatePrecision.DAY);
    date.add(Calendar.MONTH, -1);
    goToDate(date.getDate());
  }

  void goToNextMonth()
  {
    final DateHolder date = new DateHolder(form.getFilter().getCurrent(), DatePrecision.DAY);
    date.add(Calendar.MONTH, 1);
    goToDate(date.getDate());
  }

  void goToToday()
  {
    final DateHolder date = new DateHolder(DatePrecision.DAY);
    goToDate(date.getDate());
  }

  private void goToDate(final Date date)
  {
    form.getFilter().setCurrent(date);
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    response.renderCSSReference("styles/oldcalendar.css");
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    monthHolder = null;
    body.remove(weekRepeater);
    addWeeks();
  }

  @Transient
  MonthHolder getMonthHolder()
  {
    if (monthHolder == null) {
      monthHolder = new OldCalendarMonthHolder(form.getFilter());
      monthHolder.setTimesheetDao(timesheetDao);
      monthHolder.setAddressDao(addressDao);
      monthHolder.setAccessChecker(accessChecker);
      monthHolder.init();
    }
    return monthHolder;
  }

  protected String formatDuration(final long millis)
  {
    final int[] fields = TimePeriod.getDurationFields(millis, 8, 200);
    final StringBuffer buf = new StringBuffer();
    if (fields[0] > 0) {
      buf.append(fields[0]).append(getString("calendar.unit.day")).append(" ");
    }
    buf.append(fields[1]).append(":").append(StringHelper.format2DigitNumber(fields[2])).append(getString("calendar.unit.hour"));
    return buf.toString();
  }

  @Override
  protected String getTitle()
  {
    if (isSelectMode() == true) {
      if (isSelectPeriodMode() == true) {
        return getString("calendar.selectDateOrPeriod.title");
      } else {
        return getString("calendar.selectDate.title");
      }
    } else {
      return getString("calendar.title");
    }
  }

  /**
   * User has pressed the cancel button. If in selection mode then redirect to the caller.
   */
  protected void onCancel()
  {
    log.debug("onCancel");
    if (isSelectMode() == true) {
      WicketUtils.setResponsePage(this, caller);
      caller.cancelSelection(selectProperty);
    }
  }

  /**
   * User has selected one day. If in selection mode then redirect to the caller with Date or if returnAsISODateString is true date as iso
   * date string: "yyyy-mm-dd".
   */
  protected void onSelectDay(final Date date)
  {
    log.debug("onSelectDay");
    if (isSelectMode() == true) {
      WicketUtils.setResponsePage(this, caller);
      if (returnAsISODateString == true) {
        caller.select(this.selectProperty, DateHelper.formatIsoDate(date));
      } else {
        caller.select(this.selectProperty, date);
      }
    }
  }

  /**
   * User has selected a period (week or whole month). If in selection mode then redirect to the caller with TimePeriod or if
   * returnAsISODateString is true time period as iso date string: "yyyy-mm-dd:yyyy-mm-dd".
   */
  protected void onSelectPeriod(final Date fromDate, final Date toDate)
  {
    log.debug("onSelectPeriod");
    if (isSelectMode() == true) {
      WicketUtils.setResponsePage(this, caller);
      if (returnAsISODateString == true) {
        caller.select(this.selectProperty, DateHelper.formatIsoTimePeriod(fromDate, toDate));
      } else {
        caller.select(this.selectProperty, new TimePeriod(fromDate, toDate));
      }
    }
  }

  CalendarFilter getFilter()
  {
    return form.getFilter();
  }

  boolean isSelectMode()
  {
    return this.caller != null;
  }

  String getFormattedMonthDuration()
  {
    return formatDuration(monthHolder.getMonthDuration());
  }

  public boolean isSelectStartStopTime()
  {
    return selectStartStopTime;
  }

  public void cancelSelection(final String property)
  {
  }

  public void select(final String property, final Object selectedValue)
  {
    if ("userId".equals(property) == true) {
      getFilter().setUserId((Integer) selectedValue);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(final String property)
  {
    if ("userId".equals(property) == true) {
      getFilter().setUserId(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }
}
