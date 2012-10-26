/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Name;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.commons.lang.StringUtils;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.plugins.teamcal.event.TeamEventFilter;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.calendar.CalendarFeedHook;

/**
 * Hook for TeamCal feeds
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 */
public class TeamCalCalendarFeedHook implements CalendarFeedHook
{

  private String[] teamCalIds;

  /**
   * @see org.projectforge.web.calendar.CalendarFeedHook#onInit()
   */
  @Override
  public void onInit(final HttpServletRequest req)
  {
    final String teamCals = req.getParameter("teamCals");
    if (teamCals != null) {
      teamCalIds = StringUtils.split(teamCals, ";");
    }
  }

  /**
   * @see org.projectforge.web.calendar.CalendarFeedHook#getEvents(net.fortuna.ical4j.model.TimeZone, java.util.Calendar)
   */
  @Override
  public List<VEvent> getEvents(final PFUserDO user, final TimeZone timezone, final Calendar cal)
  {
    {
      final List<VEvent> events = new LinkedList<VEvent>();
      if (teamCalIds != null) {
        final TeamEventDao teamEventDao = (TeamEventDao) Registry.instance().getDao(TeamEventDao.class);
        final TeamEventFilter eventFilter = new TeamEventFilter();
        eventFilter.setUser(user);
        eventFilter.setDeleted(false);
        eventFilter.setEndDate(cal.getTime());
        for (int i = 0; i < teamCalIds.length; i++) {
          final Integer id = Integer.valueOf(teamCalIds[i]);
          eventFilter.setTeamCalId(id);

          final List<TeamEventDO> teamEvents = teamEventDao.getUnlimitedList(eventFilter);
          if (teamEvents != null && teamEvents.size() > 0) {
            for (final TeamEventDO teamEvent : teamEvents) {
              final Date date = new Date(teamEvent.getStartDate().getTime());
              final VEvent vEvent;
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
                if (teamEvents.size() > 1) {
                  calendarName = " ("
                      + teamEvent.getCalendar().getTitle()
                      + ")";
                }
                vEvent = new VEvent(fortunaStartDate, fortunaEndDate, teamEvent.getSubject()
                    + calendarName);
                vEvent.getProperties().add(new Uid(fortunaStartDate.toString()));
              } else {
                cal.setTime(date);
                final DateTime startTime = getCalTime(timezone, cal);

                date.setTime(teamEvent.getEndDate().getTime());
                cal.setTime(date);
                final DateTime stopTime = getCalTime(timezone, cal);
                vEvent = new VEvent(startTime, stopTime, teamEvent.getSubject() + " (" + teamEvent.getCalendar().getTitle() + ")");
                vEvent.getProperties().add(new Uid(startTime.toString()));
              }

              // TODO Sichtbarkeit
              vEvent.getProperties().add(new Location(teamEvent.getLocation()));
              vEvent.getProperties().add(new Name(teamEvent.getCalendar().getTitle()));
              // vEvent.getProperties().add(new Note(teamEvent.getNote()));

              events.add(vEvent);
            }
          }
        }
      }
      return events;
    }
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