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

package org.projectforge.plugins.teamcal.admin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.projectforge.common.AbstractCache;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRights;

/**
 * Caches all calendars.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TeamCalCache extends AbstractCache
{
  private static Logger log = Logger.getLogger(TeamCalCache.class);

  private static TeamCalCache instance = new TeamCalCache();

  private transient TeamCalDao teamCalDao;

  private transient TeamCalRight teamCalRight;

  private Map<Integer, TeamCalDO> calendarMap;

  public static TeamCalCache getInstance()
  {
    return instance;
  }

  public TeamCalDO getCalendar(final Integer calendarId)
  {
    checkRefresh();
    return calendarMap.get(calendarId);
  }

  /**
   * Get ordered calendars (by title and id).
   * @return All accessible calendars of the context user (as owner or with full, read-only or minimal access).
   */
  public Collection<TeamCalDO> getAllAccessibleCalendars()
  {
    checkRefresh();
    final Set<TeamCalDO> set = new TreeSet<TeamCalDO>(new TeamCalsComparator());
    final PFUserDO loggedInUser = PFUserContext.getUser();
    for (final TeamCalDO cal : calendarMap.values()) {
      if (teamCalRight.hasSelectAccess(loggedInUser, cal) == true && cal.isDeleted() == false) {
        set.add(cal);
      }
    }
    return set;
  }

  /**
   * Get ordered calendars (by title and id).
   * @return All accessible calendars of the context user (as owner or with full, read-only or minimal access).
   */
  public Collection<TeamCalDO> getAllOwnCalendars()
  {
    checkRefresh();
    final Set<TeamCalDO> set = new TreeSet<TeamCalDO>(new TeamCalsComparator());
    final Integer loggedInUserId = PFUserContext.getUserId();
    for (final TeamCalDO cal : calendarMap.values()) {
      if (teamCalRight.isOwner(loggedInUserId, cal) == true) {
        set.add(cal);
      }
    }
    return set;
  }

  public Collection<TeamCalDO> getCalendars(final Collection<Integer> calIds)
  {
    final Set<TeamCalDO> set = new TreeSet<TeamCalDO>(new TeamCalsComparator());
    if (calIds != null) {
      for (final Integer calId : calIds) {
        final TeamCalDO cal = getCalendar(calId);
        if (cal == null) {
          log.warn("Calendar with id " + calId + " not found in cache.");
          continue;
        }
        if (teamCalRight.hasSelectAccess(PFUserContext.getUser()) == true) {
          set.add(cal);
        }
      }
    }
    return set;
  }

  /**
   * This method will be called by CacheHelper and is synchronized via getData();
   */
  @Override
  protected void refresh()
  {
    log.info("Initializing TeamCalCache ...");
    if (teamCalDao == null || teamCalRight == null) {
      teamCalDao = Registry.instance().getDao(TeamCalDao.class);
      teamCalRight = (TeamCalRight) UserRights.instance().getRight(TeamCalDao.USER_RIGHT_ID);
    }
    // This method must not be synchronized because it works with a new copy of maps.
    final Map<Integer, TeamCalDO> map = new HashMap<Integer, TeamCalDO>();
    final List<TeamCalDO> list = teamCalDao.internalLoadAll();
    for (final TeamCalDO cal : list) {
      map.put(cal.getId(), cal);
    }
    this.calendarMap = map;
    log.info("Initializing of TeamCalCache done.");
  }
}
