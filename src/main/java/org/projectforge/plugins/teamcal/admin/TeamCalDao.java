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

package org.projectforge.plugins.teamcal.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserContext;
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

  private static final int ACCESS_GROUP_COUNT = 3;

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "owner.username", "owner.firstname", "owner.lastname",
    "fullAccessGroup.name", "readOnlyAccessGroup.name", "minimalAccessGroup.name"};

  private GroupDao groupDao;

  private UserDao userDao;

  private final TeamCalRight right;

  private boolean allCals;

  public TeamCalDao()
  {
    super(TeamCalDO.class);
    userRightId = USER_RIGHT_ID;
    right = new TeamCalRight();
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
  public List<TeamCalDO> getTeamCalsByAccess(final PFUserDO user, final boolean own, final String... accessGroup) {
    final QueryFilter queryFilter = new QueryFilter();

    if (accessGroup != null && user != null) {
      final Collection<Integer> groupIdsRequestedUser = userGroupCache.getUserGroups(user);
      final Iterator<Integer> iteratorRequestedGroups = groupIdsRequestedUser.iterator();

      // get teamCals where current user has access
      queryFilter.add(Restrictions.disjunction());
      final Disjunction disjunction = Restrictions.disjunction();
      while (iteratorRequestedGroups.hasNext()) {
        final GroupDO g = userGroupCache.getGroup(iteratorRequestedGroups.next());
        if (g != null)
          for (int i = 0; i < accessGroup.length; i++) {
            if (accessGroup[i] != null)
              disjunction.add(Restrictions.eq(accessGroup[i], g));
          }
      }
      if (own == true) {
        disjunction.add(Restrictions.eq("owner", user));
      }
      queryFilter.add(disjunction);
    } else {
      if (user != null) {
        // get teamCals where user is owner
        queryFilter.add(Restrictions.eq("owner", user));
      } else
        return new ArrayList<TeamCalDO>();
    }
    return getListFilteredByAccess(getList(queryFilter));
  }

  @Override
  public List<TeamCalDO> getList(final BaseSearchFilter filter) {
    TeamCalFilter tFilter;
    if (filter instanceof TeamCalFilter)
      tFilter = (TeamCalFilter) filter;
    else {
      tFilter = new TeamCalFilter(filter);
    }

    if (tFilter.getOwnerId() == null)
      return new ArrayList<TeamCalDO>();
    else {
      final PFUserDO user = userGroupCache.getUser(tFilter.getOwnerId());
      final String accessGroups[] = new String[ACCESS_GROUP_COUNT];
      boolean ownTeamCals = false;
      if (tFilter.isFullAccess() == true) {
        accessGroups[0] = FULL_ACCESS_GROUP;
      }
      if (tFilter.isReadOnlyAccess() == true) {
        accessGroups[1] = READONLY_ACCESS_GROUP;
      }
      if (tFilter.isMinimalAccess() == true) {
        accessGroups[2] = MINIMAL_ACCESS_GROUP;
      }
      if (tFilter.isOwn() == true) {
        ownTeamCals = true;
      }

      return getTeamCalsByAccess(user, ownTeamCals, accessGroups);
    }
  }

  /**
   * hide information
   * 
   * @param list
   * @return
   */
  private List<TeamCalDO> hideByAccess(final List<TeamCalDO> list) {
    final PFUserDO user = PFUserContext.getUser();
    for (final TeamCalDO teamCal : list) {
      if (right.isOwner(user, teamCal) == true
          || right.hasAccessGroup(teamCal.getFullAccessGroup(), userGroupCache, user) == true
          || right.hasAccessGroup(teamCal.getReadOnlyAccessGroup(), userGroupCache, user) == true) {
        // do nothing
      } else
        if (right.hasAccessGroup(teamCal.getMinimalAccessGroup(), userGroupCache, user) == true) {
          teamCal.setDescription("");
        } else
          list.remove(teamCal);
    }
    return list;
  }

  /**
   * filter by access where current user has access.
   * 
   * @param list
   * @return
   */
  private List<TeamCalDO> getListFilteredByAccess(final List<TeamCalDO> list) {
    final PFUserDO user = PFUserContext.getUser();
    final List<TeamCalDO> grantedList = new ArrayList<TeamCalDO>();
    for (final TeamCalDO teamcal : list) {
      if (right.hasSelectAccess(user, teamcal) == true)
        grantedList.add(teamcal);
    }
    return hideByAccess(grantedList);
  }

  /**
   * @return the allCals
   */
  public boolean isAllCals()
  {
    return allCals;
  }

  /**
   * @param allCals the allCals to set
   * @return this for chaining.
   */
  public void setAllCals(final boolean allCals)
  {
    this.allCals = allCals;
  }
}
