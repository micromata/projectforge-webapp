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

import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.EventSource;
import net.ftlines.wicket.fullcalendar.callback.ClickedEvent;
import net.ftlines.wicket.fullcalendar.callback.DroppedEvent;
import net.ftlines.wicket.fullcalendar.callback.ResizedEvent;
import net.ftlines.wicket.fullcalendar.callback.SelectedRange;
import net.ftlines.wicket.fullcalendar.callback.View;
import net.ftlines.wicket.fullcalendar.selector.EventSourceSelector;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateMidnight;
import org.projectforge.address.AddressDao;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.address.BirthdayEventsProvider;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.timesheet.TimesheetEventsProvider;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class CalendarPage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final long serialVersionUID = 8710165041912824126L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CalendarPage.class);

  private static final String USERPREF_KEY = "CalendarPage.userPrefs";

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  private CalendarForm form;

  protected final PageParameters pageParameters;

  private BirthdayEventsProvider birthdayEventsProvider;

  private HolidayEventsProvider holidayEventsProvider;

  private TimesheetEventsProvider timesheetEventsProvider;

  public CalendarPage(final PageParameters parameters)
  {
    super(parameters);
    this.pageParameters = parameters;
    init();
  }

  @SuppressWarnings("serial")
  public void init()
  {
    final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback") {
      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return false;
      }
    };
    feedbackPanel.setOutputMarkupId(true);
    body.add(feedbackPanel);

    final MyFullCalendarConfig config = new MyFullCalendarConfig(this);
    config.setSelectable(true);
    config.setSelectHelper(true);

    config.setLoading("function(bool) { if (bool) $(\"#loading\").show(); else $(\"#loading\").hide(); }");

    // config.setMinTime(new LocalTime(6, 30));
    // config.setMaxTime(new LocalTime(17, 30));
    config.setAllDaySlot(true);
    final MyFullCalendar calendar = new MyFullCalendar("cal", config) {
      @Override
      protected void onDateRangeSelected(final SelectedRange range, final CalendarResponse response)
      {
        log.info("Selected region: " + range.getStart() + " - " + range.getEnd() + " / allDay: " + range.isAllDay());
        // response.getTarget().add(feedbackPanel);
      }

      @Override
      protected boolean onEventDropped(final DroppedEvent event, final CalendarResponse response)
      {
        log.info("Event drop. eventId: "
            + event.getEvent().getId()
            + " sourceId: "
            + event.getSource().getUuid()
            + " dayDelta: "
            + event.getDaysDelta()
            + " minuteDelta: "
            + event.getMinutesDelta()
            + " allDay: "
            + event.isAllDay());
        log.info("Original start time: " + event.getEvent().getStart() + ", original end time: " + event.getEvent().getEnd());
        log.info("New start time: " + event.getNewStartTime() + ", new end time: " + event.getNewEndTime());
        // response.getTarget().add(feedbackPanel);
        return false;
      }

      @Override
      protected boolean onEventResized(final ResizedEvent event, final CalendarResponse response)
      {
        log.info("Event resized. eventId: "
            + event.getEvent().getId()
            + " sourceId: "
            + event.getSource().getUuid()
            + " dayDelta: "
            + event.getDaysDelta()
            + " minuteDelta: "
            + event.getMinutesDelta());
        // response.getTarget().add(feedbackPanel);
        return false;
      }

      @Override
      protected void onEventClicked(final ClickedEvent event, final CalendarResponse response)
      {
        log.info("Event clicked. eventId: " + event.getEvent().getId() + ", sourceId: " + event.getSource().getUuid());
        response.refetchEvents();
        // response.getTarget().add(feedbackPanel);
      }

      @Override
      protected void onViewDisplayed(final View view, final CalendarResponse response)
      {
        if (log.isDebugEnabled() == true) {
          log.debug("View displayed. viewType: " + view.getType().name() + ", start: " + view.getStart() + ", end: " + view.getEnd());
        }
        response.refetchEvents();
        final CalendarFilter filter = form.getFilter();
        filter.setStartDate(view.getVisibleStart());
        filter.setViewType(view.getType());
        // response.getTarget().add(feedbackPanel);
      }
    };
    calendar.setMarkupId("calendar");
    body.add(calendar);
    body.add(new EventSourceSelector("selector", calendar));

    form = new CalendarForm(this);

    // body.add(form);
    CalendarFilter filter = (CalendarFilter) getUserPrefEntry(USERPREF_KEY);
    if (filter == null) {
      filter = new CalendarFilter();
      putUserPrefEntry(USERPREF_KEY, filter, true);
    }
    form.setFilter(filter);
    form.init();

    if (pageParameters != null) {
      if (pageParameters.get("showTimesheets") != null) {
        form.getFilter().setUserId(getUserId());
      }
      if (pageParameters.get("showBirthdays") != null) {
        form.getFilter().setShowBirthdays(true);
      }
    }
    final DateMidnight startDate = form.getFilter().getStartDate();
    if (startDate != null) {
      config.setYear(startDate.getYear());
      config.setMonth(startDate.getMonthOfYear() - 1);
      config.setDate(startDate.getDayOfMonth());
    }
    config.setDefaultView(filter.getViewType().getCode());

    EventSource reservations = new EventSource();
    timesheetEventsProvider = new TimesheetEventsProvider(this, timesheetDao, form.getFilter());
    reservations.setEventsProvider(timesheetEventsProvider);
    reservations.setEditable(true);
    config.add(reservations);
    reservations = new EventSource();
    birthdayEventsProvider = new BirthdayEventsProvider(this, addressDao, accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP) == false);
    reservations.setEventsProvider(birthdayEventsProvider);
    reservations.setEditable(false);
    reservations.setBackgroundColor("#EEEEEE");
    reservations.setColor("#EEEEEE");
    reservations.setTextColor("#222222");
    config.add(reservations);
    reservations = new EventSource();
    holidayEventsProvider = new HolidayEventsProvider(this);
    reservations.setEventsProvider(holidayEventsProvider);
    reservations.setEditable(false);
    config.add(reservations);
  }

  @Override
  protected String getTitle()
  {
    return getString("calendar.title");
  }

  CalendarFilter getFilter()
  {
    return form.getFilter();
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
