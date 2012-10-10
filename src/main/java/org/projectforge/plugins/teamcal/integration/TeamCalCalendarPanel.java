/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.EventSource;
import net.ftlines.wicket.fullcalendar.callback.CalendarDropMode;
import net.ftlines.wicket.fullcalendar.callback.ClickedEvent;
import net.ftlines.wicket.fullcalendar.callback.View;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateTime;
import org.projectforge.common.NumberHelper;
import org.projectforge.plugins.teamcal.TeamCalEventProvider;
import org.projectforge.plugins.teamcal.TeamEventDao;
import org.projectforge.plugins.teamcal.TeamEventEditPage;
import org.projectforge.plugins.teamcal.TeamEventRight;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.calendar.CalendarFilter;
import org.projectforge.web.calendar.CalendarPanel;
import org.projectforge.web.calendar.MyFullCalendarConfig;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.components.JodaDatePanel;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 */
public class TeamCalCalendarPanel extends CalendarPanel
{
  private static final long serialVersionUID = 5462271308502345885L;

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private TeamCalEventProvider eventProvider;

  /**
   * @param id
   * @param currentDatePanel
   */
  public TeamCalCalendarPanel(final String id, final JodaDatePanel currentDatePanel)
  {
    super(id, currentDatePanel);
  }

  /**
   * @see org.projectforge.web.calendar.CalendarPanel#onEventClickedHook(net.ftlines.wicket.fullcalendar.callback.ClickedEvent,
   *      net.ftlines.wicket.fullcalendar.CalendarResponse, net.ftlines.wicket.fullcalendar.Event, java.lang.String, java.lang.String)
   */
  @Override
  protected void onEventClickedHook(final ClickedEvent clickedEvent, final CalendarResponse response, final Event event,
      final String eventId, final String eventClassName)
  {
    // User clicked on teamEvent
    final Integer id = NumberHelper.parseInteger(event.getId());
    if (new TeamEventRight().hasUpdateAccess(PFUserContext.getUser(), teamEventDao.getById(id), null)) {
      final PageParameters parameters = new PageParameters();
      parameters.add(AbstractEditPage.PARAMETER_KEY_ID, id);
      final TeamEventEditPage teamEventPage = new TeamEventEditPage(parameters);
      setResponsePage(teamEventPage);
      teamEventPage.setReturnToPage((WebPage) getPage());
      return;
    }
  }

  /**
   * @see org.projectforge.web.calendar.CalendarPanel#onModifyEventHook(net.ftlines.wicket.fullcalendar.Event, org.joda.time.DateTime,
   *      org.joda.time.DateTime, net.ftlines.wicket.fullcalendar.callback.CalendarDropMode,
   *      net.ftlines.wicket.fullcalendar.CalendarResponse)
   */
  @Override
  protected void onModifyEventHook(final Event event, final DateTime newStartTime, final DateTime newEndTime, final CalendarDropMode dropMode,
      final CalendarResponse response)
  {
    // TODO Max do something reasonable
  }

  /**
   * @see org.projectforge.web.calendar.CalendarPanel#onRegisterEventSourceHook(org.projectforge.web.calendar.MyFullCalendarConfig)
   */
  @Override
  protected void onRegisterEventSourceHook(final MyFullCalendarConfig config, final CalendarFilter filter)
  {
    if(filter instanceof TeamCalCalendarFilter) {
      // TODO Max bitte konfigurierbar machen dann bzw. den User auswählen lassen.
      final EventSource eventSource = new EventSource();
      eventProvider = new TeamCalEventProvider(this, teamEventDao, userGroupCache, (TeamCalCalendarFilter) filter);
      eventSource.setEventsProvider(eventProvider);
      eventSource.setBackgroundColor("#1AA118");
      eventSource.setColor("#000000");
      eventSource.setTextColor("#222222");
      config.add(eventSource);
    }

  }
  /**
   * @see org.projectforge.web.calendar.CalendarPanel#onCallGetEventsHook()
   */
  @Override
  protected void onCallGetEventsHook(final View view, final CalendarResponse response)
  {
    final TeamCalCalendarForm tempForm = (TeamCalCalendarForm) ((TeamCalCalendarPage) getPage()).getForm();
    if (tempForm != null && tempForm.getMultipleTeamCalList() != null && tempForm.getMultipleTeamCalList().getAssignedItems() == null)
      eventProvider.getEvents(view.getVisibleStart().toDateTime(), view.getVisibleEnd().toDateTime());
  }
}
