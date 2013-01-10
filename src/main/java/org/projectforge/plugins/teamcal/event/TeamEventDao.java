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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.projectforge.calendar.CalendarUtils;
import org.projectforge.calendar.ICal4JUtils;
import org.projectforge.common.DateHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.plugins.teamcal.admin.TeamCalCache;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalsProvider;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserRightId;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class TeamEventDao extends BaseDao<TeamEventDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_CALENDAR_EVENT", "plugin15", "plugins.teamcalendar.event");

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventDao.class);

  private static final long ONE_DAY = 1000 * 60 * 60 * 24;

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "subject", "location", "calendar.id", "calendar.title", "note",
  "attendees"};

  public TeamEventDao()
  {
    super(TeamEventDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  /**
   * Sets midnight (UTC) of all day events.
   * @see org.projectforge.core.BaseDao#onSaveOrModify(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void onSaveOrModify(final TeamEventDO event)
  {
    super.onSaveOrModify(event);
    if (event.isAllDay() == true) {
      final Date startDate = event.getStartDate();
      if (startDate != null) {
        event.setStartDate(CalendarUtils.getUTCMidnightTimestamp(startDate));
      }
      final Date endDate = event.getEndDate();
      if (endDate != null) {
        event.setEndDate(CalendarUtils.getUTCMidnightTimestamp(endDate));
      }
    }
    // Update recurrenceUntil date (for database queries):
    final Date recurrenceUntil = ICal4JUtils.calculateRecurrenceUntil(event.getRecurrenceRule());
    event.setRecurrenceUntil(recurrenceUntil);
  }

  /**
   * Sets midnight (with user's time zone) of all day events.
   * @see org.projectforge.core.BaseDao#afterLoad(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void afterLoad(final TeamEventDO event)
  {
    super.afterLoad(event);
    if (event.isAllDay() == false) {
      return;
    } else {
      final Date startDate = event.getStartDate();
      if (startDate != null) {
        event.setStartDate(CalendarUtils.getMidnightTimestampFromUTC(startDate));
      }
      final Date endDate = event.getEndDate();
      if (endDate != null) {
        event.setEndDate(CalendarUtils.getMidnightTimestampFromUTC(endDate));
      }
    }
  }

  /**
   * @param filter
   * @return list of team events (same as {@link #getList(BaseSearchFilter)} but with all calculated and matching recurrence events.
   */
  public List<TeamEvent> getEventList(final TeamEventFilter filter)
  {
    List<TeamEventDO> list = getList(filter);
    final List<TeamEvent> result = new ArrayList<TeamEvent>();
    result.addAll(list);

    final TeamEventFilter teamEventFilter = filter.clone().setOnlyRecurrence(true);
    final QueryFilter qFilter = buildQueryFilter(teamEventFilter);
    qFilter.add(Restrictions.isNotNull("recurrenceRule"));
    list = getList(qFilter);
    final TimeZone timeZone = PFUserContext.getTimeZone();
    if (list != null) {
      for (final TeamEventDO eventDO : list) {
        if (eventDO.hasRecurrence() == false) {
          log.warn("Shouldn't occur! Please contact developer.");
          // This event was handled above.
          continue;
        }
        final Collection<TeamEvent> events = TeamEventUtils.getRecurrenceEvents(teamEventFilter.getStartDate(),
            teamEventFilter.getEndDate(), eventDO, timeZone);
        if (events == null) {
          continue;
        }
        for (final TeamEvent event : events) {
          if (event instanceof TeamEventDO) {
            // TeamEventDO objects were already added.
            continue;
          }
          if (matches(event.getStartDate(), event.getEndDate(), event.isAllDay(), teamEventFilter) == false) {
            continue;
          }
          result.add(event);
        }
      }
    }
    return result;
  }

  /**
   * @see org.projectforge.core.BaseDao#getListForSearchDao(org.projectforge.core.BaseSearchFilter)
   */
  @Override
  public List<TeamEventDO> getListForSearchDao(final BaseSearchFilter filter)
  {
    final TeamEventFilter teamEventFilter = new TeamEventFilter(filter); // May-be called by SeachPage
    final Collection<TeamCalDO> ownCalendars = TeamCalCache.getInstance().getAllOwnCalendars();
    if (CollectionUtils.isEmpty(ownCalendars) == true) {
      // No calendars accessible, nothing to search.
      return new ArrayList<TeamEventDO>();
    }
    teamEventFilter.setTeamCals(TeamCalsProvider.getCalIdList(ownCalendars));
    return getList(teamEventFilter);
  }

  /**
   * @see org.projectforge.core.BaseDao#getList(org.projectforge.core.BaseSearchFilter)
   */
  @Override
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<TeamEventDO> getList(final BaseSearchFilter filter)
  {
    final TeamEventFilter teamEventFilter;
    if (filter instanceof TeamEventFilter) {
      teamEventFilter = ((TeamEventFilter) filter).clone();
    } else {
      teamEventFilter = new TeamEventFilter(filter);
    }
    if (CollectionUtils.isEmpty(teamEventFilter.getTeamCals()) == true && teamEventFilter.getTeamCalId() == null) {
      return new ArrayList<TeamEventDO>();
    }
    final QueryFilter qFilter = buildQueryFilter(teamEventFilter);
    final List<TeamEventDO> list = getList(qFilter);
    final List<TeamEventDO> result = new ArrayList<TeamEventDO>(list.size());
    if (list != null) {
      for (final TeamEventDO event : list) {
        if (matches(event.getStartDate(), event.getEndDate(), event.isAllDay(), teamEventFilter) == true) {
          result.add(event);
        }
      }
    }
    return result;
  }

  private boolean matches(final Date eventStartDate, final Date eventEndDate, final boolean allDay, final TeamEventFilter teamEventFilter)
  {
    final Date startDate = teamEventFilter.getStartDate();
    final Date endDate = teamEventFilter.getEndDate();
    if (allDay == true) {
      // Check date match:
      final Calendar utcCal = Calendar.getInstance(DateHelper.UTC);
      utcCal.setTime(eventStartDate);
      if (startDate != null && eventEndDate.before(startDate) == true) {
        // Check same day (eventStartDate in UTC and startDate of filter in user's time zone):
        final Calendar userCal = Calendar.getInstance(PFUserContext.getTimeZone());
        userCal.setTime(startDate);
        if (CalendarUtils.isSameDay(utcCal, utcCal) == true) {
          return true;
        }
        return false;
      }
      if (endDate != null && eventStartDate.after(endDate) == true) {
        // Check same day (eventEndDate in UTC and endDate of filter in user's time zone):
        final Calendar userCal = Calendar.getInstance(PFUserContext.getTimeZone());
        userCal.setTime(endDate);
        if (CalendarUtils.isSameDay(utcCal, utcCal) == true) {
          return true;
        }
        return false;
      }
      return true;
    } else {
      // Check start and stop date due to extension of time period of buildQueryFilter:
      if (startDate != null && eventEndDate.before(startDate) == true) {
        return false;
      }
      if (endDate != null && eventStartDate.after(endDate) == true) {
        return false;
      }
    }
    return true;
  }

  /**
   * The time period of the filter will be extended by one day. This is needed due to all day events which are stored in UTC. The additional
   * events in the result list not matching the time period have to be removed by caller!
   * @param filter
   * @param allDay
   * @return
   */
  private QueryFilter buildQueryFilter(final TeamEventFilter filter)
  {
    final QueryFilter queryFilter = new QueryFilter(filter);
    final Collection<Integer> cals = filter.getTeamCals();
    if (CollectionUtils.isNotEmpty(cals) == true) {
      queryFilter.add(Restrictions.in("calendar.id", cals));
    } else if (filter.getTeamCalId() != null) {
      queryFilter.add(Restrictions.eq("calendar.id", filter.getTeamCalId()));
    }
    // Following period extension is needed due to all day events which are stored in UTC. The additional events in the result list not
    // matching the time period have to be removed by caller!
    Date startDate = filter.getStartDate();
    if (startDate != null) {
      startDate = new Date(startDate.getTime() - ONE_DAY);
    }
    Date endDate = filter.getEndDate();
    if (endDate != null) {
      endDate = new Date(endDate.getTime() + ONE_DAY);
    }
    // limit events to load to chosen date view.
    if (startDate != null && endDate != null) {
      if (filter.isOnlyRecurrence() == false) {
        queryFilter.add(Restrictions.or(
            (Restrictions.or(Restrictions.between("startDate", startDate, endDate), Restrictions.between("endDate", startDate, endDate))),
            // get events whose duration overlap with chosen duration.
            (Restrictions.and(Restrictions.le("startDate", startDate), Restrictions.ge("endDate", endDate)))));
      } else {
        queryFilter.add(
            // "startDate" < endDate && ("recurrenceUntil" == null || "recurrenceUnti" > startDate)
            (Restrictions.and(Restrictions.lt("startDate", endDate),
                Restrictions.or(Restrictions.isNull("recurrenceUntil"), Restrictions.gt("recurrenceUntil", startDate)))));
      }
    } else if (startDate != null) {
      queryFilter.add(Restrictions.ge("startDate", startDate));
    } else if (endDate != null) {
      queryFilter.add(Restrictions.le("startDate", endDate));
    }
    queryFilter.addOrder(Order.desc("startDate"));
    if (log.isDebugEnabled() == true) {
      log.debug(ToStringBuilder.reflectionToString(filter));
    }
    return queryFilter;
  }

  @Override
  public TeamEventDO newInstance()
  {
    return new TeamEventDO();
  }

  /**
   * @return the log
   */
  public Logger getLog()
  {
    return log;
  }

  /**
   * @see org.projectforge.core.BaseDao#useOwnCriteriaCacheRegion()
   */
  @Override
  protected boolean useOwnCriteriaCacheRegion()
  {
    return true;
  }
}
