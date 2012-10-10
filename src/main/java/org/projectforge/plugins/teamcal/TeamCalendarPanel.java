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

package org.projectforge.plugins.teamcal;

import java.sql.Timestamp;

import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.EventSource;
import net.ftlines.wicket.fullcalendar.callback.CalendarDropMode;
import net.ftlines.wicket.fullcalendar.callback.ClickedEvent;
import net.ftlines.wicket.fullcalendar.callback.DroppedEvent;
import net.ftlines.wicket.fullcalendar.callback.ResizedEvent;
import net.ftlines.wicket.fullcalendar.callback.SelectedRange;
import net.ftlines.wicket.fullcalendar.callback.View;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.projectforge.access.AccessChecker;
import org.projectforge.address.AddressDao;
import org.projectforge.common.DateHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.humanresources.HRPlanningDao;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.address.AddressViewPage;
import org.projectforge.web.address.BirthdayEventsProvider;
import org.projectforge.web.calendar.HolidayEventsProvider;
import org.projectforge.web.calendar.MyFullCalendar;
import org.projectforge.web.calendar.MyFullCalendarConfig;
import org.projectforge.web.humanresources.HRPlanningEventsProvider;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.timesheet.TimesheetEventsProvider;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.components.DatePickerUtils;
import org.projectforge.web.wicket.components.JodaDatePanel;

public class TeamCalendarPanel extends Panel
{
  private static final long serialVersionUID = -8491059902148238143L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalendarPanel.class);

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "hrPlanningDao")
  private HRPlanningDao hrPlanningDao;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private MyFullCalendar calendar;

  private BirthdayEventsProvider birthdayEventsProvider;

  private HolidayEventsProvider holidayEventsProvider;

  private HRPlanningEventsProvider hrPlanningEventsProvider;

  private TimesheetEventsProvider timesheetEventsProvider;

  private TeamCalEventProvider eventProvider;

  private TeamCalendarFilter filter;

  private boolean refresh;

  private final JodaDatePanel currentDatePanel;

  // private TeamCalDO teamCal;

  private MyFullCalendarConfig config;

  public TeamCalendarPanel(final String id, final JodaDatePanel datePanel)
  {
    super(id);
    this.currentDatePanel = datePanel;
  }

  @SuppressWarnings("serial")
  void init(final TeamCalendarFilter filter)
  {
    this.filter = filter;
    config = new MyFullCalendarConfig(this);
    config.setSelectable(true);
    config.setSelectHelper(true);
    config.setLoading("function(bool) { if (bool) $(\"#loading\").show(); else $(\"#loading\").hide(); }");
    // config.setMinTime(new LocalTime(6, 30));
    // config.setMaxTime(new LocalTime(17, 30));
    config.setAllDaySlot(true);
    calendar = new MyFullCalendar("cal", config) {
      @Override
      protected void onDateRangeSelected(final SelectedRange range, final CalendarResponse response)
      {
        if (log.isDebugEnabled() == true) {
          log.debug("Selected region: " + range.getStart() + " - " + range.getEnd() + " / allDay: " + range.isAllDay());
        }
        if (accessChecker.isRestrictedUser() == true) {
          return;
        }
        final PageParameters parameters = new PageParameters();
        parameters.add(TimesheetEditPage.PARAMETER_KEY_START_DATE_IN_MILLIS, DateHelper.getDateTimeAsMillis(range.getStart()));
        parameters.add(TimesheetEditPage.PARAMETER_KEY_STOP_DATE_IN_MILLIS, DateHelper.getDateTimeAsMillis(range.getEnd()));
        if (filter.getUserId() != null) {
          parameters.add(TimesheetEditPage.PARAMETER_KEY_USER, filter.getUserId());
        }

        final TimesheetEditPage timesheetEditPage = new TimesheetEditPage(parameters);
        timesheetEditPage.setReturnToPage((WebPage) getPage());
        setResponsePage(timesheetEditPage);
      }

      /**
       * Event was moved, a new start time was chosen.
       * @see net.ftlines.wicket.fullcalendar.FullCalendar#onEventDropped(net.ftlines.wicket.fullcalendar.callback.DroppedEvent,
       *      net.ftlines.wicket.fullcalendar.CalendarResponse)
       */
      @Override
      protected boolean onEventDropped(final DroppedEvent event, final CalendarResponse response)
      {
        // default mode is move and edit
        CalendarDropMode dropMode = CalendarDropMode.MOVE_EDIT;
        final StringValue parameterValue = RequestCycle.get().getRequest().getQueryParameters().getParameterValue("which");
        if (parameterValue != null) {
          try {
            dropMode = CalendarDropMode.fromAjaxTarget(parameterValue.toString());
          } catch (final Exception ex) {
            log.warn("Unable to get calendar drop mode for given value, using default mode. Given mode: " + parameterValue.toString());
          }
        }
        if (log.isDebugEnabled() == true) {
          log.debug("Event drop. eventId: "
              + event.getEvent().getId()
              + " eventClass"
              + event.getEvent().getClassName()
              + " sourceId: "
              + event.getSource().getUuid()
              + " dayDelta: "
              + event.getDaysDelta()
              + " minuteDelta: "
              + event.getMinutesDelta()
              + " allDay: "
              + event.isAllDay());
          log.debug("Original start time: " + event.getEvent().getStart() + ", original end time: " + event.getEvent().getEnd());
          log.debug("New start time: " + event.getNewStartTime() + ", new end time: " + event.getNewEndTime());
        }
        modifyEvent(event.getEvent(), event.getNewStartTime(), event.getNewEndTime(), dropMode, response);
        return false;
      }

      @Override
      protected boolean onEventResized(final ResizedEvent event, final CalendarResponse response)
      {
        if (log.isDebugEnabled() == true) {
          log.debug("Event resized. eventId: "
              + event.getEvent().getId()
              + " eventClass"
              + event.getEvent().getClassName()
              + " sourceId: "
              + event.getSource().getUuid()
              + " dayDelta: "
              + event.getDaysDelta()
              + " minuteDelta: "
              + event.getMinutesDelta());
        }
        modifyEvent(event.getEvent(), null, event.getNewEndTime(), CalendarDropMode.MOVE_EDIT, response);
        return false;
      }

      @Override
      protected void onEventClicked(final ClickedEvent clickedEvent, final CalendarResponse response)
      {
        final Event event = clickedEvent.getEvent();
        final String eventId = event != null ? event.getId() : null;
        final String eventClassName = event != null ? event.getClassName() : null;
        if (log.isDebugEnabled() == true) {
          log.debug("Event clicked. eventId: "
              + eventId
              + " eventClass"
              + event.getClassName()
              + ", sourceId: "
              + clickedEvent.getSource().getUuid());
        }
        if (eventId != null && TimesheetEventsProvider.EVENT_CLASS_NAME.equals(eventClassName) == true) {
          // User clicked on a time sheet, show the time sheet:
          final Integer id = NumberHelper.parseInteger(eventId);
          final PageParameters parameters = new PageParameters();
          parameters.add(AbstractEditPage.PARAMETER_KEY_ID, id);
          final TimesheetEditPage timesheetEditPage = new TimesheetEditPage(parameters);
          timesheetEditPage.setReturnToPage((WebPage) getPage());
          setResponsePage(timesheetEditPage);
          return;
        }
        if (eventId != null && BirthdayEventsProvider.EVENT_CLASS_NAME.equals(eventClassName) == true) {
          // User clicked on birthday, show the address:
          final Integer id = NumberHelper.parseInteger(eventId);
          final PageParameters parameters = new PageParameters();
          parameters.add(AbstractEditPage.PARAMETER_KEY_ID, id);
          final AddressViewPage addressViewPage = new AddressViewPage(parameters);
          setResponsePage(addressViewPage);
          addressViewPage.setReturnToPage((WebPage) getPage());
          return;
        }
        if (eventId != null && TeamCalEventProvider.EVENT_CLASS_NAME.equals(eventClassName) == true) {
          // User clicked on teamEvent
          final Integer id = NumberHelper.parseInteger(eventId);
          if (new TeamEventRight().hasUpdateAccess(PFUserContext.getUser(), teamEventDao.getById(id), null)) {
            final PageParameters parameters = new PageParameters();
            parameters.add(AbstractEditPage.PARAMETER_KEY_ID, id);
            final TeamEventEditPage teamEventPage = new TeamEventEditPage(parameters);
            setResponsePage(teamEventPage);
            teamEventPage.setReturnToPage((WebPage) getPage());
            return;
          }
        }
        response.refetchEvents();
      }

      @Override
      protected void onViewDisplayed(final View view, final CalendarResponse response)
      {
        if (log.isDebugEnabled() == true) {
          log.debug("View displayed. viewType: " + view.getType().name() + ", start: " + view.getStart() + ", end: " + view.getEnd());
        }
        response.refetchEvents();
        setStartDate(view.getStart());
        filter.setViewType(view.getType());

        // Need calling getEvents for getting correct duration label, it's not predictable what will be called first: onViewDisplayed or
        // getEvents.
        timesheetEventsProvider.getEvents(view.getVisibleStart().toDateTime(), view.getVisibleEnd().toDateTime());
        final TeamCalendarForm tempForm = ((TeamCalendarPage) getPage()).form;
        if (tempForm != null && tempForm.getMultipleTeamCalList() != null && tempForm.getMultipleTeamCalList().getAssignedItems() == null)
          eventProvider.getEvents(view.getVisibleStart().toDateTime(), view.getVisibleEnd().toDateTime());

        if (currentDatePanel != null) {
          currentDatePanel.getDateField().modelChanged();
          response.getTarget().add(currentDatePanel.getDateField());
          response.getTarget().appendJavaScript(
              DatePickerUtils.getDatePickerInitJavaScript(currentDatePanel.getDateField().getMarkupId(), true));
        }
        response.getTarget().add(((TeamCalendarPage) getPage()).form.durationLabel);
      }
    };
    calendar.setMarkupId("calendar");
    add(calendar);
    final DateMidnight startDate = filter.getStartDate();
    if (startDate != null) {
      config.setYear(startDate.getYear());
      config.setMonth(startDate.getMonthOfYear() - 1);
      config.setDate(startDate.getDayOfMonth());
    }
    setConfig();

    // Time sheets
    EventSource eventSource = new EventSource();
    timesheetEventsProvider = new TimesheetEventsProvider(this, timesheetDao, filter);
    eventSource.setEventsProvider(timesheetEventsProvider);
    eventSource.setEditable(true);
    config.add(eventSource);
    // Holidays:
    eventSource = new EventSource();
    holidayEventsProvider = new HolidayEventsProvider(this);
    eventSource.setEventsProvider(holidayEventsProvider);
    eventSource.setEditable(false);
    config.add(eventSource);
    // HR planning:
    eventSource = new EventSource();
    hrPlanningEventsProvider = new HRPlanningEventsProvider(this, filter, hrPlanningDao);
    eventSource.setEventsProvider(hrPlanningEventsProvider);
    eventSource.setEditable(false);
    eventSource.setBackgroundColor("#0080FF");
    eventSource.setColor("#0080FF");
    config.add(eventSource);
    // Birthdays:
    eventSource = new EventSource();
    birthdayEventsProvider = new BirthdayEventsProvider(this, filter, addressDao,
        accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP) == false);
    eventSource.setEventsProvider(birthdayEventsProvider);
    eventSource.setEditable(false);
    // The default color of birthdays (not favorites), should be gray, see BirthdayEventsProvider for colors of birthdays of favorites.
    eventSource.setBackgroundColor("#EEEEEE");
    eventSource.setColor("#EEEEEE");
    eventSource.setTextColor("#222222");
    config.add(eventSource);

    // TESTING TEAMCAL
    eventSource = new EventSource();
    eventProvider = new TeamCalEventProvider(this, teamEventDao, userGroupCache, filter);
    eventSource.setEventsProvider(eventProvider);
    eventSource.setBackgroundColor("#1AA118");
    eventSource.setColor("#000000");
    eventSource.setTextColor("#222222");
    config.add(eventSource);
  }

  private void modifyEvent(final Event event, final DateTime newStartTime, final DateTime newEndTime, final CalendarDropMode dropMode,
      final CalendarResponse response)
  {
    final String eventId = event != null ? event.getId() : null;
    final String eventClassName = event != null ? event.getClassName() : null;
    if (eventId != null && TimesheetEventsProvider.EVENT_CLASS_NAME.equals(eventClassName) == true) {
      // User clicked on a time sheet, show the time sheet:
      final Integer id = NumberHelper.parseInteger(eventId);
      final TimesheetDO dbTimesheet = timesheetDao.internalGetById(id);
      if (dbTimesheet == null) {
        return;
      }
      final TimesheetDO timesheet = new TimesheetDO();
      timesheet.copyValuesFrom(dbTimesheet);
      final Long newStartTimeMillis = newStartTime != null ? DateHelper.getDateTimeAsMillis(newStartTime) : null;
      final Long newEndTimeMillis = newEndTime != null ? DateHelper.getDateTimeAsMillis(newEndTime) : null;
      if (newStartTimeMillis != null) {
        timesheet.setStartDate(newStartTimeMillis);
      }
      if (newEndTimeMillis != null) {
        timesheet.setStopTime(new Timestamp(newEndTimeMillis));
      }
      final PFUserDO loggedInUser = ((AbstractSecuredBasePage) getPage()).getUser();
      if (timesheetDao.hasUpdateAccess(loggedInUser, timesheet, dbTimesheet, false) == false) {
        // User has no update access, therefore ignore this request...
        return;
      }

      // update start and end time
      if (newStartTime != null) {
        dbTimesheet.setStartTime(new Timestamp(newStartTime.getMillis()));
      }
      if (newEndTime != null) {
        dbTimesheet.setStopTime(new Timestamp(newEndTime.getMillis()));
      }

      // clone timesheet if mode is copy_*
      if (CalendarDropMode.COPY_EDIT.equals(dropMode) || CalendarDropMode.COPY_SAVE.equals(dropMode)) {
        dbTimesheet.setId(null);
        dbTimesheet.setDeleted(false);
        timesheetDao.setUser(dbTimesheet, loggedInUser.getId());

        // and save the new time sheet -> correct time is set already
        timesheetDao.save(dbTimesheet);
      }

      if (dropMode == null || CalendarDropMode.MOVE_EDIT.equals(dropMode) || CalendarDropMode.COPY_EDIT.equals(dropMode)) {
        // first: "normal edit mode"
        // TODO use modal dialogs
        final PageParameters parameters = new PageParameters();
        parameters.add(AbstractEditPage.PARAMETER_KEY_ID, dbTimesheet.getId());
        if (newStartTime != null) {
          parameters.add(TimesheetEditPage.PARAMETER_KEY_NEW_START_DATE, DateHelper.getDateTimeAsMillis(newStartTime));
        }
        if (newEndTime != null) {
          parameters.add(TimesheetEditPage.PARAMETER_KEY_NEW_END_DATE, DateHelper.getDateTimeAsMillis(newEndTime));
        }
        final TimesheetEditPage timesheetEditPage = new TimesheetEditPage(parameters);
        timesheetEditPage.setReturnToPage((WebPage) getPage());
        setResponsePage(timesheetEditPage);
      } else if (CalendarDropMode.MOVE_SAVE.equals(dropMode) || CalendarDropMode.COPY_SAVE.equals(dropMode)) {
        // second mode: "quick save mode"
        if (CalendarDropMode.MOVE_SAVE.equals(dropMode)) {
          // we need update only in "move" mode, in "copy" mode it was saved a few lines above
          timesheetDao.update(dbTimesheet);
        }
        setResponsePage(getPage());
      } else {
        // CANCEL -> should be handled through javascript now
        setResponsePage(getPage());
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractSecuredPage#onBeforeRender()
   */
  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    // Restore current date (e. g. on reload or on coming back from callee page).
    final MyFullCalendarConfig config = calendar.getConfig();
    final DateMidnight startDate = filter.getStartDate();
    if (startDate != null) {
      config.setYear(startDate.getYear());
      config.setMonth(startDate.getMonthOfYear() - 1);
      config.setDate(startDate.getDayOfMonth());
    }
    config.setDefaultView(filter.getViewType().getCode());
    if (refresh == true) {
      refresh = false;
      timesheetEventsProvider.forceReload();
      birthdayEventsProvider.forceReload();
      hrPlanningEventsProvider.forceReload();
      eventProvider.forceReload();
    }
  }

  private void setConfig()
  {
    final MyFullCalendarConfig config = calendar.getConfig();
    if (filter.isSlot30() == true) {
      config.setSlotMinutes(30);
    } else {
      config.setSlotMinutes(15);
    }
    if (filter.getFirstHour() != null) {
      config.setFirstHour(filter.getFirstHour());
    }
  }

  /**
   * @param startDate the startDate to set
   * @return this for chaining.
   */
  public TeamCalendarPanel setStartDate(final DateMidnight startDate)
  {
    filter.setStartDate(startDate);
    return this;
  }

  /**
   * @return the startDate
   */
  public DateMidnight getStartDate()
  {
    return filter.getStartDate();
  }

  /**
   * Forces to reloaded time sheets in onBeforeRender().
   * @param refresh the refresh to set
   * @return this for chaining.
   */
  public TeamCalendarPanel forceReload()
  {
    this.refresh = true;
    return this;
  }

  public String getTotalTimesheetDuration()
  {
    return timesheetEventsProvider.formatDuration(timesheetEventsProvider.getTotalDuration());
  }
}
