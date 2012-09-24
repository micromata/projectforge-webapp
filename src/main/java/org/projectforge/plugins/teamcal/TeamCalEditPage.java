/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.joda.time.DateTime;
import org.projectforge.common.DateHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.calendar.MyFullCalendar;
import org.projectforge.web.calendar.MyFullCalendarConfig;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.components.DatePickerUtils;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@EditPage(defaultReturnPage = TeamCalListPage.class)
public class TeamCalEditPage extends AbstractEditPage<TeamCalDO, TeamCalEditForm, TeamCalDao> implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEditPage.class);

  private static final long serialVersionUID = -3352981782657771662L;

  private TeamCalEventProvider eventProvider;

  private TeamCalRight right;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  /**
   * @param parameters
   * @param i18nPrefix
   */
  public TeamCalEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.teamcal");
    init();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#init()
   */
  @Override
  protected void init()
  {
    super.init();
    right = new TeamCalRight();

    final MyFullCalendarConfig config = new MyFullCalendarConfig(this);
    if (teamCalDao.hasUpdateAccess(getUser(), getData(), null, false))
      config.setSelectable(true);
    else
      config.setSelectable(false);
    config.setSelectHelper(true);
    config.setLoading("function(bool) { if (bool) $(\"#loading\").show(); else $(\"#loading\").hide(); }");
    config.setAllDaySlot(true);
    config.setDefaultView("agendaWeek");
    @SuppressWarnings("serial")
    final MyFullCalendar myCalendar = new MyFullCalendar("calendar", config){
      @Override
      protected void onDateRangeSelected(final SelectedRange range, final CalendarResponse response)
      {
        if (right.isOwner(getUser(), getData()) == false)
          if(right.hasAccessGroup(getData().getMinimalAccessGroup(), userGroupCache, getUser()) == true)
            return;

        if (log.isDebugEnabled() == true) {
          log.debug("Selected region: " + range.getStart() + " - " + range.getEnd() + " / allDay: " + range.isAllDay());
        }
        final PageParameters parameters = new PageParameters();
        parameters.add(TeamEventEditPage.PARAMETER_KEY_START_DATE_IN_MILLIS, DateHelper.getDateTimeAsMillis(range.getStart()));
        parameters.add(TeamEventEditPage.PARAMETER_KEY_END_DATE_IN_MILLIS, DateHelper.getDateTimeAsMillis(range.getEnd()));
        parameters.add(TeamEventEditPage.PARAMETER_KEY_TEAMCALID, getData().getId());
        final TeamEventEditPage teamEventEditPage = new TeamEventEditPage(parameters);
        teamEventEditPage.setReturnToPage((WebPage) getPage());
        setResponsePage(teamEventEditPage);
      }

      /**
       * Event was moved, a new start time was chosen.
       * @see net.ftlines.wicket.fullcalendar.FullCalendar#onEventDropped(net.ftlines.wicket.fullcalendar.callback.DroppedEvent,
       *      net.ftlines.wicket.fullcalendar.CalendarResponse)
       */
      @Override
      protected boolean onEventDropped(final DroppedEvent event, final CalendarResponse response)
      {
        if (right.isOwner(getUser(), getData()) == false)
          if(right.hasAccessGroup(getData().getMinimalAccessGroup(), userGroupCache, getUser()) == true)
            return false;

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
        if (right.isOwner(getUser(), getData()) == false)
          if(right.hasAccessGroup(getData().getMinimalAccessGroup(), userGroupCache, getUser()) == true)
            return false;

        if (log.isDebugEnabled() == true) {
          log.debug("Event resized. eventId: "
              + event.getEvent().getId()
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
      protected void onEventClicked(final ClickedEvent event, final CalendarResponse response)
      {
        if (right.isOwner(getUser(), getData()) == false)
          if(right.hasAccessGroup(getData().getMinimalAccessGroup(), userGroupCache, getUser()) == true)
            return;

        if (log.isDebugEnabled() == true) {
          log.debug("Event clicked. eventId: " + event.getEvent().getId() + ", sourceId: " + event.getSource().getUuid());
        }
        final String eventId = event.getEvent().getId();
        // User clicked on an event, show the event:
        final Integer id = NumberHelper.parseInteger(eventId);
        final PageParameters parameters = new PageParameters();
        parameters.add(AbstractEditPage.PARAMETER_KEY_ID, id);
        parameters.add(TeamEventEditPage.PARAMETER_KEY_TEAMCALID, getData().getId());
        final TeamEventEditPage teamEventEditPage = new TeamEventEditPage(parameters);
        teamEventEditPage.setReturnToPage((WebPage) getPage());
        setResponsePage(teamEventEditPage);
        return;
      }

      @Override
      protected void onViewDisplayed(final View view, final CalendarResponse response)
      {
        if (right.isOwner(getUser(), getData()) == false)
          if(right.hasAccessGroup(getData().getMinimalAccessGroup(), userGroupCache, getUser()) == true)
            return;

        if (log.isDebugEnabled() == true) {
          log.debug("View displayed. viewType: " + view.getType().name() + ", start: " + view.getStart() + ", end: " + view.getEnd());
        }
        response.refetchEvents();
        // Need calling getEvents for getting correct duration label, it's not predictable what will be called first: onViewDisplayed or
        // getEvents.
        if (isNew() == false) {
          eventProvider.getEvents(view.getVisibleStart().toDateTime(), view.getVisibleEnd().toDateTime());
        }
        if (form.getDatePanel() != null) {
          form.getDatePanel().getDateField().modelChanged();
          response.getTarget().add(form.getDatePanel().getDateField());
          response.getTarget().appendJavaScript(
              DatePickerUtils.getDatePickerInitJavaScript(form.getDatePanel().getDateField().getMarkupId(), true));
        }
      }
    };

    getForm().add(myCalendar);
    myCalendar.setMarkupId("calendar");
    final EventSource eventSource = new EventSource();
    if (isNew() == false) {
      eventProvider = new TeamCalEventProvider(this, teamCalDao, teamEventDao, getData().getId());
      eventProvider.setUserGroupCache(userGroupCache);
      eventSource.setEventsProvider(eventProvider);
      eventSource.setEditable(true);
      config.add(eventSource);
    }
    else
      myCalendar.setVisible(false);

  }

  /**
   * required to find the component which called the request.
   * components are marked with ids.
   * 
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Object)
   */
  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("fullAccessGroupId".equals(property) == true) {
      teamCalDao.setFullAccessGroup(getData(), (Integer) selectedValue);
    } else if ("readOnlyAccessGroupId".equals(property)) {
      teamCalDao.setReadOnlyAccessGroup(getData(), (Integer) selectedValue);
    } else if ("minimalAccessGroupId".equals(property)) {
      teamCalDao.setMinimalAccessGroup(getData(), (Integer) selectedValue);
    }
  }

  /**
   * @see #select
   * 
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  @Override
  public void unselect(final String property)
  {
    if ("fullAccessGroupId".equals(property)) {
      getData().setFullAccessGroup(null);
    } else if ("readOnlyAccessGroupId".equals(property)) {
      getData().setReadOnlyAccessGroup(null);
    } else if ("minimalAccessGroupId".equals(property)) {
      getData().setMinimalAccessGroup(null);
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  @Override
  public void cancelSelection(final String property)
  {
    // TODO Auto-generated method stub
  }

  private void modifyEvent(final Event event, final DateTime newStartDate, final DateTime newEndDate, final CalendarDropMode dropMode, final CalendarResponse response)
  {
    final Integer id = NumberHelper.parseInteger(event.getId());
    final TeamEventDO dbTeamEvent = teamEventDao.internalGetById(id);
    if (dbTeamEvent == null) {
      return;
    }
    final Long newStartTimeMillis = newStartDate != null ? DateHelper.getDateTimeAsMillis(newStartDate) : null;
    final Long newEndTimeMillis = newEndDate != null ? DateHelper.getDateTimeAsMillis(newEndDate) : null;
    final PFUserDO loggedInUser = ((AbstractSecuredBasePage) getPage()).getUser();
    if (teamEventDao.hasUpdateAccess(loggedInUser, dbTeamEvent, dbTeamEvent, false) == false) {
      // User has no update access, therefore ignore this request...
      event.setEditable(false);
      event.setTitle("");
      return;
    }

    // update start and end time
    if(newStartDate != null) {
      dbTeamEvent.setStartDate(new Timestamp(newStartTimeMillis));
    }
    if(newEndDate != null) {
      dbTeamEvent.setEndDate(new Timestamp(newEndTimeMillis));
    }

    // clone event if mode is copy_*
    if (CalendarDropMode.COPY_EDIT.equals(dropMode) || CalendarDropMode.COPY_SAVE.equals(dropMode)) {
      dbTeamEvent.setId(null);
      dbTeamEvent.setDeleted(false);
      dbTeamEvent.setCalendar(getData());

      // and save the new event -> correct time is set already
      teamEventDao.save(dbTeamEvent);
    }

    if (dropMode == null || CalendarDropMode.MOVE_EDIT.equals(dropMode) || CalendarDropMode.COPY_EDIT.equals(dropMode)) {
      // first: "normal edit mode"
      // TODO use modal dialogs
      final PageParameters parameters = new PageParameters();
      // Add event Id
      parameters.add(AbstractEditPage.PARAMETER_KEY_ID, dbTeamEvent.getId());
      // Add teamCal id
      parameters.add(TeamEventEditPage.PARAMETER_KEY_TEAMCALID, dbTeamEvent.getCalendar().getId());
      if (newStartDate != null) {
        parameters.add(TeamEventEditPage.PARAMETER_KEY_START_DATE_IN_MILLIS, newStartTimeMillis);
      }
      if (newEndDate != null) {
        parameters.add(TeamEventEditPage.PARAMETER_KEY_END_DATE_IN_MILLIS, newEndTimeMillis);
      }
      final TeamEventEditPage teamEventEditPage = new TeamEventEditPage(parameters);
      teamEventEditPage.setReturnToPage((WebPage) getPage());
      setResponsePage(teamEventEditPage);
    } else if (CalendarDropMode.MOVE_SAVE.equals(dropMode) || CalendarDropMode.COPY_SAVE.equals(dropMode)) {
      // second mode: "quick save mode"
      if(CalendarDropMode.MOVE_SAVE.equals(dropMode)) {
        // we need update only in "move" mode, in "copy" mode it was saved a few lines above
        teamEventDao.update(dbTeamEvent);
      }
      setResponsePage(getPage());
    } else {
      // CANCEL -> should be handled through javascript now
      setResponsePage(getPage());
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected TeamCalDao getBaseDao()
  {
    return teamCalDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage,
   *      org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected TeamCalEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final TeamCalDO data)
  {
    return new TeamCalEditForm(this, data);
  }
}
