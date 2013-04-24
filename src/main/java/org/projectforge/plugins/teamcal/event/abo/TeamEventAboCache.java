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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.projectforge.core.QueryFilter;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class TeamEventAboCache
{
  private static TeamEventAboCache instance = new TeamEventAboCache();

  private final Map<Integer, TeamEventAbo> abos;

  private List<Integer> teamCalWithAbo;

  private TeamEventAboCache()
  {
    abos = new HashMap<Integer, TeamEventAbo>();
    this.teamCalWithAbo = new ArrayList<Integer>();
  }

  public static TeamEventAboCache instance()
  {
    return instance;
  }

  public void updateCache(TeamCalDao dao)
  {
    QueryFilter filter = new QueryFilter();
    filter.add(Restrictions.eq("abo", true));
    final List<TeamCalDO> aboCalendars = dao.getList(filter);
    teamCalWithAbo.clear();

    for (TeamCalDO calendar : aboCalendars) {
      teamCalWithAbo.add(calendar.getId());
      final TeamEventAbo compareAbo = abos.get(calendar.getId());
      Long now = System.currentTimeMillis();
      if (compareAbo == null) {
        // create the calendar
        final TeamEventAbo teamEventAbo = new TeamEventAbo(dao, calendar);
        abos.put(calendar.getId(), teamEventAbo);
      } else if (compareAbo.getLastUpdated() == null || compareAbo.getLastUpdated() + calendar.getAboUpdateTime() <= now) {
        // update the calendar
        compareAbo.initOrUpdate(calendar);
      }
    }
  }

  public boolean isAboCalendar(Integer calendarId)
  {
    return teamCalWithAbo.contains(calendarId) == true;
  }

  public List<TeamEventDO> getEvents(Integer calendarId, Long startTime, Long endTime)
  {
    TeamEventAbo eventAbo = abos.get(calendarId);
    if (eventAbo == null) {
      return null;
    }
    return eventAbo.getEvents(startTime, endTime);
  }
}
