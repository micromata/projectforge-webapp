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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.projectforge.core.BaseDao;
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
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class TeamCalDao extends BaseDao<TeamCalDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_CALENDAR", "plugin15", "plugins.teamcal.calendar");

  public static final String FULL_ACCESS_GROUP = "fullAccessGroup";

  public static final String READONLY_ACCESS_GROUP = "readOnlyAccessGroup";

  public static final String MINIMAL_ACCESS_GROUP = "minimalAccessGroup";

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
   * Get list of teamCals where user is owner and has access.
   * <p>
   * access groups:<br />
   * FULL_ACCESS_GROUP<br />
   * READONLY_ACCESS_GROUP<br />
   * MINIMAL_ACCESS_GROUP
   * </p>
   * 
   * @param user - where user is owner
   * @param accessGroup - where user has given access. if accessGroup == null, only get groups, where user is owner
   */
  public List<TeamCalDO> getTeamCalsByAccess(final PFUserDO user, final String... accessGroup) {
    final QueryFilter queryFilter = new QueryFilter();

    if (accessGroup != null && user != null) {
      final Collection<Integer> groups = userGroupCache.getUserGroups(user);
      final Iterator<Integer> it = groups.iterator();

      // get teamCals where user has access
      queryFilter.add(Restrictions.disjunction());
      String query = "FROM TeamCalDO WHERE owner = " + user.getId();
      while (it.hasNext()) {
        final GroupDO g = userGroupCache.getGroup(it.next());
        for (int i = 0; i < accessGroup.length; i++) {
          query = query + " OR " + accessGroup[i] + " = " + g.getId();
        }
      }
      query = query + "";
      @SuppressWarnings("unchecked")
      final List<TeamCalDO> results = getHibernateTemplate().find(query);
      return results;
    } else {
      if (user != null) {
        // get teamCals where user is owner
        queryFilter.add(Restrictions.eq("owner", user));
      } else
        return null;
    }
    return getList(queryFilter);
  }
}
