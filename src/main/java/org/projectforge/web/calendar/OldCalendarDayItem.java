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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.address.AddressDO;
import org.projectforge.address.BirthdayAddress;
import org.projectforge.calendar.DayHolder;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.common.DateHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.timesheet.OldDisplayTimesheet;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.PlainLabel;

@Deprecated
public class OldCalendarDayItem extends WebMarkupContainer
{
  private static final long serialVersionUID = 6140762327534002674L;

  private final DayHolder day;

  private final OldCalendarPage page;

  public OldCalendarDayItem(final OldCalendarPage page, final String id, final DayHolder day)
  {
    super(id);
    this.page = page;
    this.day = day;
  }

  @SuppressWarnings("serial")
  void init()
  {
    String cssClass;
    if (day.isToday() == true) {
      cssClass = "today";
    } else if (day.isMarker() == true) {
      cssClass = "other-month";
    } else if (day.isWorkingDay() == false) {
      cssClass = "non-working-day";
    } else if (day.isHoliday() == true) {
      cssClass = "holiday-working-day";
    } else {
      cssClass = "normal";
    }
    add(AttributeModifier.replace("class", cssClass));
    final Long duration = (Long) day.getObject("duration");
    final String durationString = duration != null ? formatDuration(duration) : "";
    final Label durationLabel = new Label("duration", durationString);
    if (NumberHelper.greaterZero(duration) == false) {
      durationLabel.setVisible(false);
    }
    add(durationLabel);

    final DateHolder dayDefaultStart = new DateHolder(day.getDate());
    dayDefaultStart.setHourOfDay(8);
    dayDefaultStart.setMinute(0);
    final Link< ? > addTimesheetLink = new Link<Void>("add") {
      @Override
      public void onClick()
      {
        final PageParameters parameters = new PageParameters();
        parameters.add(TimesheetEditPage.PARAMETER_KEY_START_DATE_IN_MILLIS, dayDefaultStart.getTimeInMillis());
        if (page.getFilter().getUserId() != null) {
          parameters.add(TimesheetEditPage.PARAMETER_KEY_USER, page.getFilter().getUserId());
        }
        final TimesheetEditPage timesheetEditPage = new TimesheetEditPage(parameters);
        timesheetEditPage.setReturnToPage(page);
        setResponsePage(timesheetEditPage);
      }
    };
    add(addTimesheetLink);
    addTimesheetLink.add(new PresizedImage("addImage", getResponse(), WebConstants.EMBATS_PLUS));
    if (isSelectMode() == true) {
      addTimesheetLink.setVisible(false);
    }

    final Link< ? > selectDayButton = new Link<Void>("selectDay") {
      @Override
      public void onClick()
      {
        if (page.targetType != null && ClassUtils.isAssignable(page.targetType, java.sql.Date.class) == true) {
          page.onSelectDay(day.getSQLDate());
        } else {
          page.onSelectDay(day.getDate());
        }
      };
    };
    add(selectDayButton);
    selectDayButton.add(new Label("dayOfMonth", String.valueOf(day.getDayOfMonth())));
    final Label dayOfMonthLabel = new Label("dayOfMonth", String.valueOf(day.getDayOfMonth()));
    add(dayOfMonthLabel);
    if (isSelectMode() == true) {
      dayOfMonthLabel.setVisible(false);
    } else {
      selectDayButton.setVisible(false);
    }
    Label holidayInfoLabel;
    if (day.isHoliday() == true) {
      final String holidayInfo = day.getHolidayInfo();
      if (holidayInfo != null && holidayInfo.startsWith("calendar.holiday.") == true) {
        holidayInfoLabel = new Label("holidayInfo", getString(holidayInfo));
      } else {
        holidayInfoLabel = new Label("holidayInfo", holidayInfo);

      }
    } else {
      holidayInfoLabel = new Label("holidayInfo", "[not visible]");
      holidayInfoLabel.setVisible(false);
    }
    add(holidayInfoLabel);
    addTimesheets();
    addBirthdays(day);
  }

  protected String formatDuration(final long millis)
  {
    return page.formatDuration(millis);
  }

  private void addTimesheets()
  {
    final RepeatingView timesheetRepeater = new RepeatingView("sheets");
    add(timesheetRepeater);
    if (page.getFilter().getUserId() == null) {
      return;
    }
    @SuppressWarnings("unchecked")
    final List<OldDisplayTimesheet> timesheets = (List<OldDisplayTimesheet>) day.getObject("timesheets");
    if (timesheets == null) {
      return;
    }
    for (final OldDisplayTimesheet timesheet : timesheets) {
      final WebMarkupContainer item = new WebMarkupContainer(timesheetRepeater.newChildId());
      timesheetRepeater.add(item);
      timesheetRepeater.setRenderBodyOnly(true);
      addTimesheet(item, timesheet);
    }
  }

  @SuppressWarnings("serial")
  private void addTimesheet(final WebMarkupContainer item, final OldDisplayTimesheet timesheet)
  {
    final WebMarkupContainer displayTimesheet = new WebMarkupContainer("sheet");
    item.add(displayTimesheet);
    final Link< ? > startTimeLink = new Link<Void>("start") {
      @Override
      public void onClick()
      {
        final ISelectCallerPage caller = page.caller;
        if (caller != null) {
          WicketUtils.setResponsePage(this, caller);
          final TimePeriod timePeriod = new TimePeriod(null, timesheet.getStartTime()); // Start time as stop time of caller object.
          caller.select(page.selectProperty, timePeriod);
        } else {
          final PageParameters parameters = new PageParameters();
          parameters.add(TimesheetEditPage.PARAMETER_KEY_STOP_DATE_IN_MILLIS, timesheet.getStartTime().getTime());
          if (page.getFilter().getUserId() != null) {
            parameters.add(TimesheetEditPage.PARAMETER_KEY_USER, page.getFilter().getUserId());
          }
          final TimesheetEditPage timesheetEditPage = new TimesheetEditPage(parameters);
          timesheetEditPage.setReturnToPage(page);
          setResponsePage(timesheetEditPage);
        }
      }
    };
    displayTimesheet.add(startTimeLink);
    final String startTimeString = getFormattedTimeOfDay(timesheet.getStartTime());
    startTimeLink.add(new PlainLabel("startTime", startTimeString));
    final Label startTimeLabel = new PlainLabel("startTime", startTimeString);
    displayTimesheet.add(startTimeLabel);
    if (timesheet.isStartTimeLinkEnabled() == true && (isSelectMode() == false || page.isSelectStartStopTime() == true)) {
      startTimeLabel.setVisible(false);
    } else {
      startTimeLink.setVisible(false);
    }
    final Link< ? > stopTimeLink = new Link<Void>("stop") {
      @Override
      public void onClick()
      {
        final ISelectCallerPage caller = page.caller;
        if (caller != null) {
          WicketUtils.setResponsePage(this, caller);
          final TimePeriod timePeriod = new TimePeriod(timesheet.getStopTime(), null); // Stop time as start time of caller object.
          caller.select(page.selectProperty, timePeriod);
        } else {
          final PageParameters parameters = new PageParameters();
          parameters.add(TimesheetEditPage.PARAMETER_KEY_START_DATE_IN_MILLIS, timesheet.getStopTime().getTime());
          if (page.getFilter().getUserId() != null) {
            parameters.add(TimesheetEditPage.PARAMETER_KEY_USER, page.getFilter().getUserId());
          }
          final TimesheetEditPage timesheetEditPage = new TimesheetEditPage(parameters);
          timesheetEditPage.setReturnToPage(page);
          setResponsePage(timesheetEditPage);
        }
      }
    };
    displayTimesheet.add(stopTimeLink);
    final String stopTimeString = getFormattedTimeOfDay(timesheet.getStopTime());
    stopTimeLink.add(new PlainLabel("stopTime", stopTimeString));
    final Label stopTimeLabel = new PlainLabel("stopTime", stopTimeString);
    displayTimesheet.add(stopTimeLabel);

    final WebMarkupContainer displayBreak = new WebMarkupContainer("break");
    item.add(displayBreak);
    final Link< ? > timePeriodLink = new Link<Void>("period") {
      @Override
      public void onClick()
      {
        final ISelectCallerPage caller = page.caller;
        if (caller != null) {
          WicketUtils.setResponsePage(this, caller);
          final TimePeriod timePeriod = new TimePeriod(timesheet.getStartTime(), timesheet.getStopTime());
          caller.select(page.selectProperty, timePeriod);
        } else {
          final PageParameters parameters = new PageParameters();
          parameters.add(TimesheetEditPage.PARAMETER_KEY_START_DATE_IN_MILLIS, timesheet.getStartTime().getTime());
          parameters.add(TimesheetEditPage.PARAMETER_KEY_STOP_DATE_IN_MILLIS, timesheet.getStopTime().getTime());
          if (page.getFilter().getUserId() != null) {
            parameters.add(TimesheetEditPage.PARAMETER_KEY_USER, page.getFilter().getUserId());
          }
          final TimesheetEditPage timesheetEditPage = new TimesheetEditPage(parameters);
          timesheetEditPage.setReturnToPage(page);
          setResponsePage(timesheetEditPage);
        }
      }
    };
    displayBreak.add(timePeriodLink);
    final String timePeriodString = getFormattedTimeOfDay(timesheet.getStartTime())
        + " - "
        + getFormattedTimeOfDay(timesheet.getStopTime());
    timePeriodLink.add(new PlainLabel("timePeriod", timePeriodString));
    final Label timePeriodLabel = new PlainLabel("timePeriod", timePeriodString);
    displayBreak.add(timePeriodLabel);

    if (timesheet.isBreak() == true) {
      displayTimesheet.setVisible(false);
      if (timesheet.isStopTimeLinkEnabled() == true && (isSelectMode() == false || page.isSelectStartStopTime() == true)) {
        timePeriodLabel.setVisible(false);
      } else {
        timePeriodLink.setVisible(false);
      }
    } else {
      displayBreak.setVisible(false);
      if (timesheet.isStopTimeLinkEnabled() == true && (isSelectMode() == false || page.isSelectStartStopTime() == true)) {
        stopTimeLabel.setVisible(false);
      } else {
        stopTimeLink.setVisible(false);
      }
    }
    final Link< ? > selectTimesheetLink = new Link<Void>("select") {
      @Override
      public void onClick()
      {
        final PageParameters parameters = new PageParameters();
        parameters.add(AbstractEditPage.PARAMETER_KEY_ID, timesheet.getId());
        final TimesheetEditPage timesheetEditPage = new TimesheetEditPage(parameters);
        timesheetEditPage.setReturnToPage(page);
        setResponsePage(timesheetEditPage);
      }
    };
    displayTimesheet.add(selectTimesheetLink);
    final String tooltip = timesheet.getToolTip();
    final Label showTimesheetLabel1 = new PlainLabel("showTimesheet1", timesheet.getFormatted4Calendar());
    showTimesheetLabel1.setEscapeModelStrings(false);
    WicketUtils.addTooltip(selectTimesheetLink, tooltip);
    selectTimesheetLink.add(showTimesheetLabel1);

    final Label showTimesheetLabel2 = new Label("showTimesheet2", timesheet.getFormatted4Calendar());
    WicketUtils.addTooltip(showTimesheetLabel2, tooltip);
    displayTimesheet.add(showTimesheetLabel2);
    showTimesheetLabel2.setEscapeModelStrings(false);
    if (isSelectMode() == true) {
      selectTimesheetLink.setVisible(false);
    } else {
      showTimesheetLabel2.setVisible(false);
    }
  }

  private String getFormattedTimeOfDay(final Date date)
  {
    return DateTimeFormatter.instance().getFormattedDateTime(date, DateFormats.getFormatString(DateFormatType.TIME_OF_DAY_MINUTES));
  }

  private void addBirthdays(final DayHolder day)
  {
    final RepeatingView birthdayRepeater = new RepeatingView("birthday");
    add(birthdayRepeater);
    birthdayRepeater.setRenderBodyOnly(true);
    @SuppressWarnings("unchecked")
    final Collection<BirthdayAddress> birthdays = (Collection<BirthdayAddress>) day.getObject("birthdays");
    if (birthdays == null) {
      return;
    }
    for (final BirthdayAddress birthdayAddress : birthdays) {
      final WebMarkupContainer item = new WebMarkupContainer(birthdayRepeater.newChildId());
      birthdayRepeater.add(item);
      addBirthday(item, birthdayAddress);
    }
  }

  private void addBirthday(final WebMarkupContainer item, final BirthdayAddress birthdayAddress)
  {
    if (page.getFilter().isShowBirthdays() == false) {
      return;
    }
    final StringBuffer buf = new StringBuffer();
    final AddressDO address = birthdayAddress.getAddress();
    if (birthdayAddress.getAge() > 0) {
      // Birthday is not visible for all users (age == 0).
      buf.append(
          DateTimeFormatter.instance().getFormattedDate(address.getBirthday(), DateFormats.getFormatString(DateFormatType.DATE_SHORT)))
          .append(" ");
    }
    buf.append(HtmlHelper.escapeXml(address.getFirstName())).append(" ").append(HtmlHelper.escapeXml(address.getName()));
    if (birthdayAddress.getAge() > 0) {
      // Birthday is not visible for all users (age == 0).
      buf.append(" (").append(birthdayAddress.getAge()).append(" ").append(getString("address.age.short")).append(")");
    }
    final Label label = new Label("birthday", buf.toString());
    if (birthdayAddress.isFavorite() == true) {
      label.add(AttributeModifier.replace("class", "birthday favourite"));
    } else {
      label.add(AttributeModifier.replace("class", "birthday others"));
    }
    label.setEscapeModelStrings(false);
    item.add(label);
  }

  private boolean isSelectMode()
  {
    return page.isSelectMode();
  }
}
