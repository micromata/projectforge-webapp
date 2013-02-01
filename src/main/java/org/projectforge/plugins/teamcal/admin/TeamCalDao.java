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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.StringHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.DisplayHistoryEntry;
import org.projectforge.core.QueryFilter;
import org.projectforge.plugins.teamcal.admin.TeamCalFilter.OwnerType;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRightId;
import org.projectforge.web.user.GroupsProvider;
import org.projectforge.web.user.UsersProvider;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "usersgroups", "owner.username", "owner.firstname",
  "owner.lastname"};

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

  @Override
  public TeamCalDO newInstance()
  {
    return new TeamCalDO();
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  @Override
  public List<TeamCalDO> getList(final BaseSearchFilter filter)
  {
    TeamCalFilter myFilter;
    if (filter instanceof TeamCalFilter)
      myFilter = (TeamCalFilter) filter;
    else {
      myFilter = new TeamCalFilter(filter);
    }
    final QueryFilter queryFilter = new QueryFilter(myFilter);
    final List<TeamCalDO> list = getList(queryFilter);
    if (myFilter.isDeleted() == true) {
      // No further filtering, show all deleted calendars.
      return list;
    }
    final List<TeamCalDO> result = new ArrayList<TeamCalDO>();
    final TeamCalRight right = (TeamCalRight) getUserRight();
    final PFUserDO user = PFUserContext.getUser();
    final Integer userId = user.getId();
    for (final TeamCalDO cal : list) {
      final boolean isOwn = right.isOwner(user, cal);
      if (isOwn == true) {
        // User is owner.
        if (myFilter.isAll() == true || myFilter.isOwn() == true) {
          // Calendar matches the filter:
          result.add(cal);
        }
      } else {
        // User is not owner.
        if (myFilter.isAll() == true || myFilter.isOthers() == true) {
          if ((myFilter.isFullAccess() == true && right.hasFullAccess(cal, userId) == true)
              || (myFilter.isReadonlyAccess() == true && right.hasReadonlyAccess(cal, userId) == true)
              || (myFilter.isMinimalAccess() == true && right.hasMinimalAccess(cal, userId) == true)) {
            // Calendar matches the filter:
            result.add(cal);
          }
        }
      }
    }
    return result;
  }

  /**
   * Gets a list of all calendars with full access of the current logged-in user as well as the calendars owned by the current logged-in
   * user.
   * @return
   */
  public List<TeamCalDO> getAllCalendarsWithFullAccess()
  {
    final TeamCalFilter filter = new TeamCalFilter();
    filter.setOwnerType(OwnerType.ALL);
    filter.setFullAccess(true);
    return getList(filter);
  }

  /**
   * Please note: Only the string group.fullAccessGroupIds will be modified (but not be saved)!
   * @param calendar
   * @param fullAccessGroups
   */
  public void setFullAccessGroups(final TeamCalDO calendar, final Collection<GroupDO> fullAccessGroups)
  {
    calendar.setFullAccessGroupIds(new GroupsProvider().getGroupIds(fullAccessGroups));
  }

  public Collection<GroupDO> getSortedFullAccessGroups(final TeamCalDO calendar)
  {
    return new GroupsProvider().getSortedGroups(calendar.getFullAccessGroupIds());
  }

  /**
   * Please note: Only the string group.fullAccessGroupIds will be modified (but not be saved)!
   * @param calendar
   * @param fullAccessUsers
   */
  public void setFullAccessUsers(final TeamCalDO calendar, final Collection<PFUserDO> fullAccessUsers)
  {
    calendar.setFullAccessUserIds(new UsersProvider().getUserIds(fullAccessUsers));
  }

  public Collection<PFUserDO> getSortedFullAccessUsers(final TeamCalDO calendar)
  {
    return new UsersProvider().getSortedUsers(calendar.getFullAccessUserIds());
  }

  /**
   * Please note: Only the string group.readonlyAccessGroupIds will be modified (but not be saved)!
   * @param calendar
   * @param readonlyAccessGroups
   */
  public void setReadonlyAccessGroups(final TeamCalDO calendar, final Collection<GroupDO> readonlyAccessGroups)
  {
    calendar.setReadonlyAccessGroupIds(new GroupsProvider().getGroupIds(readonlyAccessGroups));
  }

  public Collection<GroupDO> getSortedReadonlyAccessGroups(final TeamCalDO calendar)
  {
    return new GroupsProvider().getSortedGroups(calendar.getReadonlyAccessGroupIds());
  }

  /**
   * Please note: Only the string group.readonlyAccessGroupIds will be modified (but not be saved)!
   * @param calendar
   * @param readonlyAccessUsers
   */
  public void setReadonlyAccessUsers(final TeamCalDO calendar, final Collection<PFUserDO> readonlyAccessUsers)
  {
    calendar.setReadonlyAccessUserIds(new UsersProvider().getUserIds(readonlyAccessUsers));
  }

  public Collection<PFUserDO> getSortedReadonlyAccessUsers(final TeamCalDO calendar)
  {
    return new UsersProvider().getSortedUsers(calendar.getReadonlyAccessUserIds());
  }

  /**
   * Please note: Only the string group.minimalAccessGroupIds will be modified (but not be saved)!
   * @param calendar
   * @param minimalAccessGroups
   */
  public void setMinimalAccessGroups(final TeamCalDO calendar, final Collection<GroupDO> minimalAccessGroups)
  {
    calendar.setMinimalAccessGroupIds(new GroupsProvider().getGroupIds(minimalAccessGroups));
  }

  public Collection<GroupDO> getSortedMinimalAccessGroups(final TeamCalDO calendar)
  {
    return new GroupsProvider().getSortedGroups(calendar.getMinimalAccessGroupIds());
  }

  /**
   * Please note: Only the string group.minimalAccessGroupIds will be modified (but not be saved)!
   * @param calendar
   * @param minimalAccessUsers
   */
  public void setMinimalAccessUsers(final TeamCalDO calendar, final Collection<PFUserDO> minimalAccessUsers)
  {
    calendar.setMinimalAccessUserIds(new UsersProvider().getUserIds(minimalAccessUsers));
  }

  public Collection<PFUserDO> getSortedMinimalAccessUsers(final TeamCalDO calendar)
  {
    return new UsersProvider().getSortedUsers(calendar.getMinimalAccessUserIds());
  }

  /**
   * @see org.projectforge.core.BaseDao#getDisplayHistoryEntries(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  public List<DisplayHistoryEntry> getDisplayHistoryEntries(final TeamCalDO obj)
  {
    final List<DisplayHistoryEntry> list = super.getDisplayHistoryEntries(obj);
    if (CollectionUtils.isEmpty(list) == true) {
      return list;
    }
    for (final DisplayHistoryEntry entry : list) {
      if (entry.getPropertyName() == null) {
        continue;
      } else if (entry.getPropertyName().endsWith("GroupIds") == true) {
        final GroupsProvider gp = new GroupsProvider();
        final String oldValue = entry.getOldValue();
        if (StringUtils.isNotBlank(oldValue) == true && "null".equals(oldValue) == false) {
          final List<String> oldGroupNames = gp.getGroupNames(oldValue);
          entry.setOldValue(StringHelper.listToString(oldGroupNames, ", ", true));
        }
        final String newValue = entry.getNewValue();
        if (StringUtils.isNotBlank(newValue) == true && "null".equals(newValue) == false) {
          final List<String> newGroupNames = gp.getGroupNames(newValue);
          entry.setNewValue(StringHelper.listToString(newGroupNames, ", ", true));
        }
      } else if (entry.getPropertyName().endsWith("UserIds") == true) {
        final UsersProvider up = new UsersProvider();
        final String oldValue = entry.getOldValue();
        if (StringUtils.isNotBlank(oldValue) == true && "null".equals(oldValue) == false) {
          final List<String> oldGroupNames = up.getUserNames(oldValue);
          entry.setOldValue(StringHelper.listToString(oldGroupNames, ", ", true));
        }
        final String newValue = entry.getNewValue();
        if (StringUtils.isNotBlank(newValue) == true && "null".equals(newValue) == false) {
          final List<String> newGroupNames = up.getUserNames(newValue);
          entry.setNewValue(StringHelper.listToString(newGroupNames, ", ", true));
        }
      }
    }
    return list;
  }

  /**
   * Calls {@link TeamCalCache#setExpired()}.
   * @see org.projectforge.core.BaseDao#afterSaveOrModify(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void afterSaveOrModify(final TeamCalDO obj)
  {
    super.afterSaveOrModify(obj);
    TeamCalCache.getInstance().setExpired();
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
