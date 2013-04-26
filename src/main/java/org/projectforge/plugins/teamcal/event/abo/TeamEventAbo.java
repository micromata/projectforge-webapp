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

package org.projectforge.plugins.teamcal.event.abo;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventUtils;
import org.projectforge.web.calendar.CalendarFeed;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class TeamEventAbo implements Serializable
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventAbo.class);


  private final Integer teamCalId;

  private final RangeMap<Long, TeamEventDO> eventDurationAccess;

  private final List<TeamEventDO> recurrenceEvents;

  private final TeamCalDao teamCalDao;

  private Long lastUpdated;

  private final HttpClient client;

  public TeamEventAbo(TeamCalDao teamCalDao, TeamCalDO teamCalDo)
  {
    this.teamCalDao = teamCalDao;
    this.teamCalId = teamCalDo.getId();
    eventDurationAccess = TreeRangeMap.create();
    recurrenceEvents = new ArrayList<TeamEventDO>();
    client = new HttpClient();
    initOrUpdate(teamCalDo);
  }

  public void initOrUpdate(TeamCalDO teamCalDo)
  {

    if (teamCalDo.isAbo() == true && StringUtils.isNotEmpty(teamCalDo.getAboUrl())) {
      final CalendarBuilder builder = new CalendarBuilder();
      byte[] bytes = null;
      try {

        // Create a method instance.
        GetMethod method = new GetMethod(teamCalDo.getAboUrl());

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
          // TODO
        }

        MessageDigest md =  MessageDigest.getInstance("MD5");

        // Read the response body.
        bytes = method.getResponseBody();

        String md5 = new String(md.digest(bytes));
        if (StringUtils.equals(md5, teamCalDo.getAboHash()) == false) {
          teamCalDo.setAboHash(md5);
          teamCalDo.setAboCalendarBinary(bytes);
          teamCalDao.update(teamCalDo);
        }
      } catch (Exception e) {
        bytes = teamCalDo.getAboCalendarBinary();
        log.error("Unable to gather abo calendar information, using database.", e);
      }
      if (bytes == null) {
        log.error("Unable to use database abo calendar information, quit.");
        return;
      }
      try {
        final Calendar calendar = builder.build(new ByteArrayInputStream(bytes));
        final List<Component> list = calendar.getComponents(Component.VEVENT);
        final List<VEvent> vEvents = new ArrayList<VEvent>();
        for (final Component c : list) {
          final VEvent event = (VEvent) c;
          if (StringUtils.equals(event.getSummary().getValue(), CalendarFeed.SETUP_EVENT) == true) {
            // skip setup event!
            continue;
          }
          vEvents.add(event);
        }
        // clear
        eventDurationAccess.clear();

        // the event id must (!) be negative and decrementing (different on each event)
        Integer startId = -1;
        for (VEvent event : vEvents) {
          final TeamEventDO teamEvent = TeamEventUtils.createTeamEventDO(event);
          teamEvent.setId(startId);
          teamEvent.setCalendar(teamCalDo);

          if (teamEvent.hasRecurrence() == true) {
            // special treatment for recurrence events ..
            recurrenceEvents.add(teamEvent);
          } else {
            eventDurationAccess.put(Range.closed(teamEvent.getStartDate().getTime(), teamEvent.getEndDate().getTime()), teamEvent);
          }

          startId--;
        }
        lastUpdated = System.currentTimeMillis();
      } catch (Exception e) {
        log.error("Unable to instantiate team event list for abo.", e);
      }
    }

  }

  public List<TeamEventDO> getEvents(Long startTime, Long endTime)
  {
    // first: gather all "normal" events
    final RangeMap<Long, TeamEventDO> rangeMap = eventDurationAccess.subRangeMap(Range.closed(startTime, endTime));
    // then gather
    return new ArrayList<TeamEventDO>(rangeMap.asMapOfRanges().values());
  }

  public Integer getTeamCalId()
  {
    return teamCalId;
  }

  public Long getLastUpdated()
  {
    return lastUpdated;
  }

  public List<TeamEventDO> getRecurrenceEvents() {
    return recurrenceEvents;
  }
}
