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

package org.projectforge.plugins.teamcal.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.callback.EventDroppedCallbackScriptGenerator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.projectforge.plugins.teamcal.admin.TeamCalRight;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFilter;
import org.projectforge.plugins.teamcal.integration.TemplateEntry;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.calendar.MyEvent;
import org.projectforge.web.calendar.MyFullCalendarEventsProvider;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TeamCalEventProvider extends MyFullCalendarEventsProvider
{
  private static final long serialVersionUID = -5609599079385073490L;

  private final TeamEventDao teamEventDao;

  private int days;

  private final TeamCalCalendarFilter filter;

  private final Map<String, TeamEvent> teamEventMap = new HashMap<String, TeamEvent>();

  /**
   * the name of the event class.
   */
  public static final String EVENT_CLASS_NAME = "teamEvent";

  private final TeamEventRight eventRight;

  /**
   * @param parent component for i18n
   */
  public TeamCalEventProvider(final Component parent, final TeamEventDao teamEventDao, final UserGroupCache userGroupCache,
      final TeamCalCalendarFilter filter)
  {
    super(parent);
    this.filter = filter;
    this.teamEventDao = teamEventDao;
    this.eventRight = new TeamEventRight();
  }

  /**
   * @see org.projectforge.web.calendar.MyFullCalendarEventsProvider#getEvents(org.joda.time.DateTime, org.joda.time.DateTime)
   */
  @Override
  public Collection<Event> getEvents(final DateTime start, final DateTime end)
  {
    final Collection<Event> events = super.getEvents(start, end);
    return events;
  }

  /**
   * @see org.projectforge.web.calendar.MyFullCalendarEventsProvider#buildEvents(org.joda.time.DateTime, org.joda.time.DateTime)
   */
  @Override
  protected void buildEvents(final DateTime start, final DateTime end)
  {
    final TemplateEntry activeTemplateEntry = filter.getActiveTemplateEntry();
    if (activeTemplateEntry == null) {
      // Nothing to build.
      return;
    }
    final Set<Integer> visibleCalendars = activeTemplateEntry.getVisibleCalendarIds();
    if (CollectionUtils.isEmpty(visibleCalendars) == true) {
      // Nothing to build.
      return;
    }
    final TeamEventFilter eventFilter = new TeamEventFilter();
    eventFilter.setTeamCals(visibleCalendars);
    eventFilter.setStartDate(start.toDate());
    eventFilter.setEndDate(end.toDate());
    eventFilter.setUser(PFUserContext.getUser());
    final List<TeamEvent> teamEvents = teamEventDao.getEventList(eventFilter);

    boolean longFormat = false;
    days = Days.daysBetween(start, end).getDays();
    if (days < 10) {
      // Week or day view:
      longFormat = true;
    }

    final TeamCalRight right = new TeamCalRight();
    final PFUserDO user = PFUserContext.getUser();
    final TimeZone timeZone = PFUserContext.getTimeZone();
    if (CollectionUtils.isNotEmpty(teamEvents) == true) {
      for (final TeamEvent teamEvent : teamEvents) {
        final DateTime startDate = new DateTime(teamEvent.getStartDate(), PFUserContext.getDateTimeZone());
        final DateTime endDate = new DateTime(teamEvent.getEndDate(), PFUserContext.getDateTimeZone());
        final TeamEventDO eventDO;
        final TeamCalEventId id = new TeamCalEventId(teamEvent, timeZone);
        if (teamEvent instanceof TeamEventDO) {
          eventDO = (TeamEventDO) teamEvent;
        } else {
          eventDO = ((TeamRecurrenceEvent) teamEvent).getMaster();
        }
        teamEventMap.put(id.toString(), teamEvent);
        final MyEvent event = new MyEvent();
        event.setClassName(EVENT_CLASS_NAME + " " + EventDroppedCallbackScriptGenerator.NO_CONTEXTMENU_INDICATOR);
        event.setId("" + id);
        event.setColor(activeTemplateEntry.getColorCode(eventDO.getCalendarId()));

        if (eventRight.hasUpdateAccess(PFUserContext.getUser(), eventDO, null)) {
          event.setEditable(true);
        } else {
          event.setEditable(false);
        }

        if (teamEvent.isAllDay() == true) {
          event.setAllDay(true);
        }

        event.setStart(startDate);
        event.setEnd(endDate);

        event.setTooltip(eventDO.getCalendar().getTitle(), new String[][] { { eventDO.getSubject()},
          { eventDO.getLocation(), getString("timesheet.location")}, { eventDO.getNote(), getString("plugins.teamcal.event.note")}});
        final String title;
        String durationString = "";
        if (longFormat == true) {
          // String day = duration.getDays() + "";
          final Period period = new Period(startDate, endDate);
          int hourInt = period.getHours();
          if (period.getDays() > 0) {
            hourInt += period.getDays() * 24;
          }
          final String hour = hourInt < 10 ? "0" + hourInt : "" + hourInt;

          final int minuteInt = period.getMinutes();
          final String minute = minuteInt < 10 ? "0" + minuteInt : "" + minuteInt;

          if (event.isAllDay() == false) {
            durationString = "\n" + getString("plugins.teamcal.event.duration") + ": " + hour + ":" + minute;
          }
          final StringBuffer buf = new StringBuffer();
          buf.append(teamEvent.getSubject());
          if (StringUtils.isNotBlank(teamEvent.getNote()) == true) {
            buf.append("\n").append(getString("plugins.teamcal.event.note")).append(": ").append(teamEvent.getNote());
          }
          buf.append(durationString);
          title = buf.toString();
        } else {
          title = teamEvent.getSubject();
        }
        if (right.hasMinimalAccess(eventDO.getCalendar(), user.getId()) == true) {
          // for minimal access
          event.setTitle("");
          event.setEditable(false);
        } else {
          event.setTitle(title);
        }
        events.put(id + "", event);
      }
    }
  }

  public TeamEvent getTeamEvent(final String id)
  {
    return teamEventMap.get(id);
  }
}
