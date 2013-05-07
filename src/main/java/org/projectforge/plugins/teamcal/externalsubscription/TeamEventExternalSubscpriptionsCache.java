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

package org.projectforge.plugins.teamcal.externalsubscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.projectforge.core.QueryFilter;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventFilter;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class TeamEventExternalSubscpriptionsCache
{
  private static TeamEventExternalSubscpriptionsCache instance = new TeamEventExternalSubscpriptionsCache();

  private final Map<Integer, TeamEventSubscription> subscriptions;

  private final List<Integer> subscribedTamCalIds;

  private static final Long SUBSCRIPTION_UPDATE_TIME = 5L * 60 * 1000; // 5 min

  private TeamEventExternalSubscpriptionsCache()
  {
    subscriptions = new HashMap<Integer, TeamEventSubscription>();
    this.subscribedTamCalIds = new ArrayList<Integer>();
  }

  public static TeamEventExternalSubscpriptionsCache instance()
  {
    return instance;
  }

  public void updateCache(final TeamCalDao dao)
  {
    final QueryFilter filter = new QueryFilter();
    filter.add(Restrictions.eq("externalSubscription", true));
    // internalGetList is valid at this point, because we are calling this method in an asyn thread
    final List<TeamCalDO> subscribedCalendars = dao.internalGetList(filter);
    subscribedTamCalIds.clear();
    subscriptions.clear();

    for (final TeamCalDO calendar : subscribedCalendars) {
      updateCache(dao, calendar);
    }
  }

  public void updateCache(final TeamCalDao dao, final TeamCalDO calendar)
  {
    if (subscribedTamCalIds.contains(calendar.getId()) == false) {
      subscribedTamCalIds.add(calendar.getId());
    }
    final TeamEventSubscription compareSubscription = subscriptions.get(calendar.getId());
    final Long now = System.currentTimeMillis();
    final Long addedTime = calendar.getExternalSubscriptionUpdateInterval() == null ? SUBSCRIPTION_UPDATE_TIME : 1000L * calendar.getExternalSubscriptionUpdateInterval();
    if (compareSubscription == null) {
      // create the calendar
      final TeamEventSubscription teamEventSubscription = new TeamEventSubscription(dao, calendar);
      subscriptions.put(calendar.getId(), teamEventSubscription);

    } else if (compareSubscription.getLastUpdated() == null || compareSubscription.getLastUpdated() + addedTime <= now) {
      // update the calendar
      compareSubscription.initOrUpdate(calendar);
    }
  }

  public boolean isExternalSubscribedCalendar(final Integer calendarId)
  {
    return subscribedTamCalIds.contains(calendarId) == true;
  }

  public List<TeamEventDO> getEvents(final Integer calendarId, final Long startTime, final Long endTime)
  {
    final TeamEventSubscription eventSubscription = subscriptions.get(calendarId);
    if (eventSubscription == null) {
      return null;
    }
    return eventSubscription.getEvents(startTime, endTime);
  }

  public List<TeamEventDO> getRecurrenceEvents(final TeamEventFilter filter)
  {
    final List<TeamEventDO> result = new ArrayList<TeamEventDO>();
    // precondition: existing teamcals ins filter
    if (filter.getTeamCals() != null) {
        for (final Integer calendarId : filter.getTeamCals()) {
          final TeamEventSubscription eventSubscription = subscriptions.get(calendarId);
          if (eventSubscription != null) {
            List<TeamEventDO> recurrenceEvents = eventSubscription.getRecurrenceEvents();
            if (recurrenceEvents != null && recurrenceEvents.size() > 0) {
              result.addAll(recurrenceEvents);
            }
          }
        }
    }
    return result;
  }

}
