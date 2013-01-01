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

package org.projectforge.web.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.registry.Registry;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

public class GroupsProvider extends TextChoiceProvider<GroupDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupsProvider.class);

  private static final long serialVersionUID = 6228672635966093252L;

  transient UserGroupCache userGroupCache;

  transient GroupDao groupDao;

  private int pageSize = 20;

  private final GroupsComparator groupsComparator = new GroupsComparator();

  private Collection<GroupDO> sortedGroups;

  /**
   * @param groupIds
   * @return
   */
  public List<String> getGroupNames(final String groupIds)
  {
    if (StringUtils.isEmpty(groupIds) == true) {
      return null;
    }
    final int[] ids = StringHelper.splitToInts(groupIds, ",", false);
    final List<String> list = new ArrayList<String>();
    for (final int id : ids) {
      final GroupDO group = getUserGroupCache().getGroup(id);
      if (group != null) {
        list.add(group.getName());
      } else {
        log.warn("Group with id '" + id + "' not found in UserGroupCache. groupIds string was: " + groupIds);
      }
    }
    return list;
  }

  /**
   * 
   * @param groupIds
   * @return
   */
  public Collection<GroupDO> getSortedGroups(final String groupIds)
  {
    if (StringUtils.isEmpty(groupIds) == true) {
      return null;
    }
    sortedGroups = new TreeSet<GroupDO>(groupsComparator);
    final int[] ids = StringHelper.splitToInts(groupIds, ",", false);
    for (final int id : ids) {
      final GroupDO group = getUserGroupCache().getGroup(id);
      if (group != null) {
        sortedGroups.add(group);
      } else {
        log.warn("Group with id '" + id + "' not found in UserGroupCache. groupIds string was: " + groupIds);
      }
    }
    return sortedGroups;
  }

  public String getGroupIds(final Collection<GroupDO> groups)
  {
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    for (final GroupDO group : groups) {
      if (group.getId() != null) {
        first = StringHelper.append(buf, first, String.valueOf(group.getId()), ",");
      }
    }
    return buf.toString();
  }

  public Collection<GroupDO> getSortedGroups()
  {
    if (sortedGroups == null) {
      final Collection<GroupDO> allGroups = getUserGroupCache().getAllGroups();
      sortedGroups = new TreeSet<GroupDO>(groupsComparator);
      final PFUserDO loggedInUser = PFUserContext.getUser();
      for (final GroupDO group : allGroups) {
        if (group.isDeleted() == false && getGroupDao().hasSelectAccess(loggedInUser, group, false) == true) {
          sortedGroups.add(group);
        }
      }
    }
    return sortedGroups;
  }

  /**
   * @param pageSize the pageSize to set
   * @return this for chaining.
   */
  public GroupsProvider setPageSize(final int pageSize)
  {
    this.pageSize = pageSize;
    return this;
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getDisplayText(java.lang.Object)
   */
  @Override
  protected String getDisplayText(final GroupDO choice)
  {
    return choice.getName();
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getId(java.lang.Object)
   */
  @Override
  protected Object getId(final GroupDO choice)
  {
    return choice.getId();
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#query(java.lang.String, int, com.vaynberg.wicket.select2.Response)
   */
  @Override
  public void query(String term, final int page, final Response<GroupDO> response)
  {
    final Collection<GroupDO> sortedGroups = getSortedGroups();
    final List<GroupDO> result = new ArrayList<GroupDO>();
    term = term.toLowerCase();

    final int offset = page * pageSize;

    int matched = 0;
    boolean hasMore = false;
    for (final GroupDO group : sortedGroups) {
      if (result.size() == pageSize) {
        hasMore = true;
        break;
      }
      if (group.getName().toLowerCase().contains(term) == true) {
        matched++;
        if (matched > offset) {
          result.add(group);
        }
      }
    }
    response.addAll(result);
    response.setHasMore(hasMore);
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#toChoices(java.util.Collection)
   */
  @Override
  public Collection<GroupDO> toChoices(final Collection<String> ids)
  {
    final List<GroupDO> list = new ArrayList<GroupDO>();
    if (ids == null) {
      return list;
    }
    for (final String str : ids) {
      final Integer groupId = NumberHelper.parseInteger(str);
      if (groupId == null) {
        continue;
      }
      final GroupDO group = getUserGroupCache().getGroup(groupId);
      if (group != null) {
        list.add(group);
      }
    }
    return list;
  }

  /**
   * @return the userGroupCache
   */
  private UserGroupCache getUserGroupCache()
  {
    if (userGroupCache == null) {
      userGroupCache = Registry.instance().getUserGroupCache();
    }
    return userGroupCache;
  }

  /**
   * @return the groupDao
   */
  private GroupDao getGroupDao()
  {
    if (groupDao == null) {
      groupDao = Registry.instance().getDao(GroupDao.class);
    }
    return groupDao;
  }
}
