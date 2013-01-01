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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.projectforge.user.GroupDO;
import org.projectforge.user.UserGroupCache;
import org.springframework.util.CollectionUtils;

public class GroupsProviderTest
{
  @Test
  public void convertGroupIds()
  {
    final GroupsProvider groupsProvider = new GroupsProvider();
    groupsProvider.userGroupCache = Mockito.mock(UserGroupCache.class);
    Mockito.when(groupsProvider.userGroupCache.getGroup(1)).thenReturn(cg("1", 1));
    Mockito.when(groupsProvider.userGroupCache.getGroup(2)).thenReturn(cg("2", 2));
    Mockito.when(groupsProvider.userGroupCache.getGroup(3)).thenReturn(cg("3", 3));
    Mockito.when(groupsProvider.userGroupCache.getGroup(4)).thenReturn(cg("4", 4));

    Assert.assertEquals("", groupsProvider.getGroupIds(createGroupsCol()));
    Assert.assertEquals("1", groupsProvider.getGroupIds(createGroupsCol(1)));
    Assert.assertEquals("1,2", groupsProvider.getGroupIds(createGroupsCol(1, 2)));
    Assert.assertEquals("1,2,3", groupsProvider.getGroupIds(createGroupsCol(3, 1, 2)));

    assertGroupSet(groupsProvider.getSortedGroups(""));
    assertGroupSet(groupsProvider.getSortedGroups(" ,, ,"));
    assertGroupSet(groupsProvider.getSortedGroups("1"), 1);
    assertGroupSet(groupsProvider.getSortedGroups("3,1"), 1, 3);
    assertGroupSet(groupsProvider.getSortedGroups("3,1,2,4"), 1, 2, 3, 4);
  }

  /**
   * Creates a group with the given name and id.
   * @param name
   * @param id
   */
  private GroupDO cg(final String name, final int id)
  {
    final GroupDO group = new GroupDO();
    group.setName(name).setId(id);
    return group;
  }

  private Collection<GroupDO> createGroupsCol(final int... groupIds)
  {
    final Collection<GroupDO> col = new TreeSet<GroupDO>(new GroupsComparator());
    for (final int id : groupIds) {
      col.add(cg(String.valueOf(id), id));
    }
    return col;
  }

  private void assertGroupSet(final Collection<GroupDO> actualGroupSet, final int... expectedIds)
  {
    if (expectedIds == null || expectedIds.length == 0) {
      Assert.assertTrue(CollectionUtils.isEmpty(actualGroupSet));
      return;
    }
    Assert.assertEquals(expectedIds.length, actualGroupSet.size());
    final Set<Integer> actualIdSet = new HashSet<Integer>();
    for (final GroupDO actualGroup : actualGroupSet) {
      actualIdSet.add(actualGroup.getId());
    }
    for (final int expectedId : expectedIds) {
      Assert.assertTrue(actualIdSet.contains(expectedId));
    }
  }
}
