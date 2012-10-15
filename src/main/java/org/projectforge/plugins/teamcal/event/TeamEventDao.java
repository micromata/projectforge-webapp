/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
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
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_CALENDAR_EVENT", "plugin20", "plugins.teamcalendar.event");

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventDao.class);

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "subject", "location", "calendar.id", "calendar.title",
    "note", "attendees"};

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
   * @see org.projectforge.core.BaseDao#getList(org.projectforge.core.BaseSearchFilter)
   */
  @Override
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<TeamEventDO> getList(final BaseSearchFilter filter)
  {
    final TeamEventFilter teamEventFilter;
    if (filter instanceof TeamEventFilter) {
      teamEventFilter = (TeamEventFilter) filter;
    } else {
      teamEventFilter = new TeamEventFilter(filter);
    }

    if (teamEventFilter.getTeamCals().isEmpty())
      return null;

    final QueryFilter qFilter = buildQueryFilter(teamEventFilter);

    final List<TeamEventDO> list = getList(qFilter);
    return list;
  }

  public QueryFilter buildQueryFilter(final TeamEventFilter filter)
  {
    final QueryFilter queryFilter = new QueryFilter(filter);
    final Collection<TeamCalDO> cals = filter.getTeamCals();
    if (cals != null) {
      final TeamCalDO teamCal = new TeamCalDO();
      teamCal.setId(filter.getTeamCalId());
      queryFilter.add(Restrictions.eq("calendar", teamCal));
    }
    // limit events to load to chosen date view.
    if (filter.getStartDate() != null && filter.getEndDate() != null) {
      queryFilter.add(
          Restrictions.or(
              (Restrictions.or(
                  Restrictions.between("startDate", filter.getStartDate(), filter.getEndDate()),
                  Restrictions.between("endDate", filter.getStartDate(), filter.getEndDate()))
                  ),
                  // get events whose duration overlap with chosen duration.
                  (Restrictions.and(
                      Restrictions.le("startDate", filter.getStartDate()),
                      Restrictions.ge("endDate", filter.getEndDate()))
                      ))
          );
    } else
      if (filter.getStartDate() != null) {
        queryFilter.add(Restrictions.ge("startDate", filter.getStartDate()));
      } else
        if (filter.getEndDate() != null) {
          queryFilter.add(Restrictions.le("startDate", filter.getEndDate()));
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

}
