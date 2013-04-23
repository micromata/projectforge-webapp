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
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
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

  private final RangeMap<Long, TeamEventDO> eventDuractionAccess;

  public TeamEventAbo(TeamCalDO teamCalDo)
  {
    this.teamCalId = teamCalDo.getId();
    this.eventDuractionAccess = TreeRangeMap.create();
    initOrUpdate(teamCalDo);
  }

  public void initOrUpdate(TeamCalDO teamCalDo)
  {

    if (teamCalDo.isAbo() == true && StringUtils.isNotEmpty(teamCalDo.getAboUrl())) {
      final CalendarBuilder builder = new CalendarBuilder();
      byte[] bytes = null;
      try {
        bytes = IOUtils.toByteArray(new URL(teamCalDo.getAboUrl()));
        MessageDigest md = MessageDigest.getInstance("MD5");
        String md5 = md.digest(bytes).toString();
        if (StringUtils.equals(md5, teamCalDo.getAboHash()) == false) {
          teamCalDo.setAboHash(md5);
          teamCalDo.setAboCalendarBinary(bytes);
        }
      } catch (Exception e) {
        bytes = teamCalDo.getAboCalendarBinary();
        log.error("Unable to gather abo calendar information, using database.", e);
      }
      try {
        final Calendar calendar = builder.build(new ByteArrayInputStream(bytes));
        final List<Component> list = calendar.getComponents(Component.VEVENT);
        // Temporary not used, because multiple events are not supported.
        final List<VEvent> vEvents = new ArrayList<VEvent>();
        for (final Component c : list) {
          final VEvent event = (VEvent) c;
          if (StringUtils.equals(event.getSummary().getValue(), CalendarFeed.SETUP_EVENT) == true) {
            // skip setup event!
            continue;
          }
          vEvents.add(event);
        }
        for (VEvent event : vEvents) {
          final TeamEventDO teamEvent = TeamEventUtils.createTeamEventDO(event);
          teamEvent.setCalendar(teamCalDo);

          Long endTime = null;
          if (teamEvent.hasRecurrence() == true && teamEvent.getEndDate() == null) {
            // special treatment for recurrence events with no end date
            endTime = Long.MAX_VALUE;
          } else {
            endTime = teamEvent.getEndDate().getTime();
          }
          eventDuractionAccess.put(Range.closed(teamEvent.getStartDate().getTime(), endTime), teamEvent);
        }
      } catch (Exception e) {
        log.error("Unable to instantiate team event list.", e);
      }
    }

  }

  public List<TeamEventDO> getEvents(Long startTime, Long endTime)
  {

    final RangeMap<Long, TeamEventDO> rangeMap = eventDuractionAccess.subRangeMap(Range.atMost(endTime)).subRangeMap(
        Range.atLeast(startTime));
    return new ArrayList<TeamEventDO>(rangeMap.asMapOfRanges().values());
  }

  public Integer getTeamCalId()
  {
    return teamCalId;
  }
}
