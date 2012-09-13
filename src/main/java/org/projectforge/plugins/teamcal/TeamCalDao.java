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

package org.projectforge.plugins.teamcal;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRightId;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TeamCalDao extends BaseDao<TeamCalDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_CALENDAR", "plugin15", "plugins.teamcal.calendar");;

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "owner.username", "owner.firstname", "owner.lastname",
    "fullAccessGroup.name", "readOnlyAccessGroup.name", "minimalAccessGroup.name"};

  private GroupDao groupDao;

  private UserDao userDao;

  public TeamCalDao()
  {
    super(TeamCalDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  public void setOwner(final TeamCalDO calendar, final Integer userId)
  {
    final PFUserDO user = userDao.getOrLoad(userId);
    calendar.setOwner(user);
  }

  public void setFullAccessGroup(final TeamCalDO calendar, final Integer groupId)
  {
    final GroupDO group = groupDao.getOrLoad(groupId);
    calendar.setFullAccessGroup(group);
  }

  public void setReadOnlyAccessGroup(final TeamCalDO calendar, final Integer groupId)
  {
    final GroupDO group = groupDao.getOrLoad(groupId);
    calendar.setReadOnlyAccessGroup(group);
  }

  public void setMinimalAccessGroup(final TeamCalDO calendar, final Integer groupId)
  {
    final GroupDO group = groupDao.getOrLoad(groupId);
    calendar.setMinimalAccessGroup(group);
  }

  @Override
  public TeamCalDO newInstance()
  {
    return new TeamCalDO();
  }

  public void setGroupDao(final GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  /**
   * Get list of teamCals where user has full access.
   */
  public List<TeamCalDO> getFullAccessTeamCals(final PFUserDO user) {
    final QueryFilter queryFilter = new QueryFilter();
    // TODO more groups
    queryFilter.add(Restrictions.eq("owner", user));
    return getList(queryFilter);
  }

  /**
   * @see org.projectforge.core.BaseDao#getList(org.projectforge.core.BaseSearchFilter)
   */
  @Override
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<TeamCalDO> getList(final BaseSearchFilter filter)
  {
    final TeamCalFilter teamFilter;
    if (filter instanceof TeamCalFilter) {
      teamFilter = (TeamCalFilter) filter;
    } else {
      teamFilter = new TeamCalFilter(filter);
      teamFilter.setOwn(true);
    }

    final QueryFilter queryFilter = new QueryFilter(teamFilter);

    if (teamFilter.isOwn() == false) {
      return getList(queryFilter);
    } else {
      final PFUserDO user = new PFUserDO();
      user.setId(teamFilter.getOwnerId());
      queryFilter.add(Restrictions.eq("owner", user));
      return getList(queryFilter);
    }
  }
}
