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

package org.projectforge.web.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.projectforge.common.NumberHelper;
import org.projectforge.registry.Registry;
import org.projectforge.user.GroupDO;
import org.projectforge.user.UserGroupCache;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

public class GroupsProvider extends TextChoiceProvider<GroupDO>
{
  private static final long serialVersionUID = 6228672635966093252L;

  private transient UserGroupCache userGroupCache;

  private int pageSize = 20;

  private final GroupsComparator groupsComparator = new GroupsComparator();

  public SortedSet<GroupDO> getSortedGroups()
  {
    final Collection<GroupDO> allGroups = getUserGroupCache().getAllGroups();
    final SortedSet<GroupDO> sortedGroups = new TreeSet<GroupDO>(groupsComparator);
    for (final GroupDO group : allGroups) {
      if (group.isDeleted() == false) {
        sortedGroups.add(group);
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
    final Set<GroupDO> sortedGroups = getSortedGroups();
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
      if (group.getName().toLowerCase().contains(term)) {
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
}
