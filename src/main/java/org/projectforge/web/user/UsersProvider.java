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
import java.util.TreeSet;

import org.projectforge.common.NumberHelper;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;

public class UsersProvider extends TextChoiceProvider<PFUserDO>
{
  private static final long serialVersionUID = 6228672635966093252L;

  private transient UserGroupCache userGroupCache;

  private int pageSize = 20;

  private final UsersComparator usersComparator = new UsersComparator();

  private Collection<PFUserDO> sortedUsers;

  public Collection<PFUserDO> getSortedUsers()
  {
    if (sortedUsers == null) {
      sortedUsers = new TreeSet<PFUserDO>(usersComparator);
      final Collection<PFUserDO> allusers = getUserGroupCache().getAllUsers();
      for (final PFUserDO user : allusers) {
        if (user.isDeleted() == false && user.isDeactivated() == false) {
          sortedUsers.add(user);
        }
      }
    }
    return sortedUsers;
  }

  /**
   * @param pageSize the pageSize to set
   * @return this for chaining.
   */
  public UsersProvider setPageSize(final int pageSize)
  {
    this.pageSize = pageSize;
    return this;
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getDisplayText(java.lang.Object)
   */
  @Override
  protected String getDisplayText(final PFUserDO choice)
  {
    return choice.getFullname();
  }

  /**
   * @see com.vaynberg.wicket.select2.TextChoiceProvider#getId(java.lang.Object)
   */
  @Override
  protected Object getId(final PFUserDO choice)
  {
    return choice.getId();
  }

  /**
   * @see com.vaynberg.wicket.select2.ChoiceProvider#query(java.lang.String, int, com.vaynberg.wicket.select2.Response)
   */
  @Override
  public void query(String term, final int page, final Response<PFUserDO> response)
  {
    final Collection<PFUserDO> sortedUsers = getSortedUsers();
    final List<PFUserDO> result = new ArrayList<PFUserDO>();
    term = term.toLowerCase();

    final int offset = page * pageSize;

    int matched = 0;
    boolean hasMore = false;
    for (final PFUserDO user : sortedUsers) {
      if (result.size() == pageSize) {
        hasMore = true;
        break;
      }
      if (user.getFullname().toLowerCase().contains(term) == true || user.getUsername().toLowerCase().contains(term) == true) {
        matched++;
        if (matched > offset) {
          result.add(user);
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
  public Collection<PFUserDO> toChoices(final Collection<String> ids)
  {
    final List<PFUserDO> list = new ArrayList<PFUserDO>();
    if (ids == null) {
      return list;
    }
    for (final String str : ids) {
      final Integer userId = NumberHelper.parseInteger(str);
      if (userId == null) {
        continue;
      }
      final PFUserDO user = getUserGroupCache().getUser(userId);
      if (user != null) {
        list.add(user);
      }
    }
    return list;
  }

  /**
   * @return the useruserCache
   */
  private UserGroupCache getUserGroupCache()
  {
    if (userGroupCache == null) {
      userGroupCache = Registry.instance().getUserGroupCache();
    }
    return userGroupCache;
  }
}
