/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.integration;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Name;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.commons.lang.StringUtils;
import org.projectforge.plugins.teamcal.TeamCalConfig;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.plugins.teamcal.event.TeamEventFilter;
import org.projectforge.registry.Registry;
import org.projectforge.web.calendar.CalendarFeed;
import org.projectforge.web.calendar.CalendarFeedHook;

/**
 * Hook for TeamCal feeds
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 */
public class TeamCalCalendarFeedHook implements CalendarFeedHook
{
  // Don't change this, otherwise the synchronization with older entries may fail.
  private static final String UID_PREFIX = "pf-event";

  public static final String getUrl(final String teamCalIds)
  {
    return CalendarFeed.getUrl("&teamCals=" + teamCalIds);
  }

  /**
   * @see org.projectforge.web.calendar.CalendarFeedHook#getEvents(net.fortuna.ical4j.model.TimeZone, java.util.Calendar)
   */
  @Override
  public List<VEvent> getEvents(final HttpServletRequest req, final TimeZone timezone, final Calendar cal)
  {
    final String teamCals = req.getParameter("teamCals");
    if (teamCals == null) {
      return null;
    }
    final String[] teamCalIds = StringUtils.split(teamCals, ";");
    if (teamCalIds == null) {
      return null;
    }
    final List<VEvent> events = new LinkedList<VEvent>();
    final TeamEventDao teamEventDao = Registry.instance().getDao(TeamEventDao.class);
    final TeamEventFilter eventFilter = new TeamEventFilter();
    // Does this make any sense? TODO: eventFilter.setUser(user);
    eventFilter.setDeleted(false);
    eventFilter.setEndDate(cal.getTime());
    for (int i = 0; i < teamCalIds.length; i++) {
      final Integer id = Integer.valueOf(teamCalIds[i]);
      eventFilter.setTeamCalId(id);

      final List<TeamEventDO> teamEvents = teamEventDao.getIcsExportList(eventFilter);
      if (teamEvents != null && teamEvents.size() > 0) {
        for (final TeamEventDO teamEvent : teamEvents) {
          final Date date = new Date(teamEvent.getStartDate().getTime());
          final VEvent vEvent;
          final String uid = TeamCalConfig.get().createUid(UID_PREFIX, teamEvent.getId());
          if (teamEvent.isAllDay() == true) {
            final DtStart dtStart = new DtStart(timezone);
            final net.fortuna.ical4j.model.Date fortunaStartDate = new net.fortuna.ical4j.model.Date(date);
            dtStart.setDate(fortunaStartDate);

            final DtEnd dtEnd = new DtEnd(timezone);
            final org.joda.time.DateTime jodaTime = new org.joda.time.DateTime(teamEvent.getEndDate().getTime());

            // requires plus 1 because one day will be omitted by calendar.
            final net.fortuna.ical4j.model.Date fortunaEndDate = new net.fortuna.ical4j.model.Date(jodaTime.plusDays(1).getMillis());
            dtEnd.setDate(fortunaEndDate);
            String calendarName = "";
            if (teamCalIds.length > 1) {
              calendarName = " (" + teamEvent.getCalendar().getTitle() + ")";
            }
            vEvent = new VEvent(fortunaStartDate, fortunaEndDate, teamEvent.getSubject() + calendarName);
            vEvent.getProperties().add(new Uid(uid));
          } else {
            cal.setTime(date);
            final DateTime startTime = getCalTime(timezone, cal);

            date.setTime(teamEvent.getEndDate().getTime());
            cal.setTime(date);
            final DateTime stopTime = getCalTime(timezone, cal);
            String calendarName = "";
            if (teamCalIds.length > 1) {
              calendarName = " (" + teamEvent.getCalendar().getTitle() + ")";
            }
            vEvent = new VEvent(startTime, stopTime, teamEvent.getSubject() + calendarName);
            vEvent.getProperties().add(new Uid(uid));
          }
          if (StringUtils.isNotBlank(teamEvent.getLocation()) == true) {
            vEvent.getProperties().add(new Location(teamEvent.getLocation()));
          }
          vEvent.getProperties().add(new Name(teamEvent.getCalendar().getTitle()));
          if (StringUtils.isNotBlank(teamEvent.getNote()) == true) {
            vEvent.getProperties().add(new Description(teamEvent.getNote()));
          }
          events.add(vEvent);
        }
      }
    }
    return events;
  }

  /**
   * 
   * @param timezone
   * @param cal
   * @return relevant DateTime for a calendar, using user timezone
   */
  private DateTime getCalTime(final TimeZone timezone, final java.util.Calendar cal)
  {
    final DateTime startTime = new DateTime(cal.getTime());
    startTime.setTimeZone(timezone);
    return startTime;
  }

}
