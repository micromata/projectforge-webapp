/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.skillmatrix;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.projectforge.access.OperationType;
import org.projectforge.registry.Registry;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;
import org.projectforge.web.user.GroupsProvider;

/**
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillRight extends UserRightAccessCheck<SkillDO>
{
  private static final long serialVersionUID = 6346078004388197890L;

  private transient UserGroupCache userGroupCache;

  /**
   * @param id
   * @param category
   * @param rightValues
   */
  public SkillRight()
  {
    super(SkillDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  private UserGroupCache getUserGroupCache()
  {
    if (userGroupCache == null) {
      userGroupCache = Registry.instance().getUserGroupCache();
    }
    return userGroupCache;
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final SkillDO obj, final SkillDO oldObj, final OperationType operationType)
  {

    if (UserRights.getAccessChecker().isUserMemberOfAdminGroup(user) == true) {
      return true;
    }

    final SkillDO skill = (oldObj != null) ? oldObj : obj;

    if (skill == null) {
      return true;
    }
    if (operationType == OperationType.SELECT) {
      return (hasFullAccess(skill, user.getId()) == true) || (hasReadOnlyAccess(skill, user.getId()) == true);
    }
    return hasFullAccess(skill, user.getId());
  }

  public boolean hasFullAccess(final SkillDO skill, final Integer userId)
  {
    final Integer[] groupIds = getFullAccessGroupIds(skill);
    return hasAccess(groupIds, userId);
  }

  public boolean hasReadOnlyAccess(final SkillDO skill, final Integer userId)
  {
    final Integer[] groupIds = getReadOnlyAccessGroupIds(skill);
    return hasAccess(groupIds, userId);
  }

  public boolean hasTrainingAccess(final SkillDO skill, final Integer userId)
  {
    final Integer[] groupIds = getTrainingAccessGroupIds(skill);
    return hasAccess(groupIds, userId);
  }

  private boolean hasAccess(final Integer[] groupIds, final Integer userId)
  {
    if (getUserGroupCache().isUserMemberOfAtLeastOneGroup(userId, groupIds) == true) {
      return true;
    }
    return false;
  }

  public Integer[] getFullAccessGroupIds(final SkillDO skill)
  {
    final Set<Integer> result = new HashSet<Integer>();
    getFullAccessGroupIds(result, skill);
    return result.toArray(new Integer[0]);
  }

  private void getFullAccessGroupIds(final Set<Integer> groupIds, final SkillDO skill)
  {
    if (StringUtils.isNotBlank(skill.getFullAccessGroupIds()) == true) {
      final Collection<GroupDO> groups = new GroupsProvider().getSortedGroups(skill.getFullAccessGroupIds());
      if (groups != null) {
        for (final GroupDO group : groups) {
          groupIds.add(group.getId());
        }
      }
    }
    if (skill.getParent() == null) {
      return;
    }
    getFullAccessGroupIds(groupIds, skill.getParent());
  }

  public Integer[] getReadOnlyAccessGroupIds(final SkillDO skill)
  {
    final Set<Integer> result = new HashSet<Integer>();
    getReadOnlyAccessGroupIds(result, skill);
    return result.toArray(new Integer[0]);
  }

  private void getReadOnlyAccessGroupIds(final Set<Integer> groupIds, final SkillDO skill)
  {
    if (StringUtils.isNotBlank(skill.getReadOnlyAccessGroupIds()) == true) {
      final Collection<GroupDO> groups = new GroupsProvider().getSortedGroups(skill.getReadOnlyAccessGroupIds());
      if (groups != null) {
        for (final GroupDO group : groups) {
          groupIds.add(group.getId());
        }
      }
    }
    if (skill.getParent() == null) {
      return;
    }
    getReadOnlyAccessGroupIds(groupIds, skill.getParent());
  }

  public Integer[] getTrainingAccessGroupIds(final SkillDO skill)
  {
    final Set<Integer> result = new HashSet<Integer>();
    getTrainingAccessGroupIds(result, skill);
    return result.toArray(new Integer[0]);
  }

  private void getTrainingAccessGroupIds(final Set<Integer> groupIds, final SkillDO skill)
  {
    if (StringUtils.isNotBlank(skill.getTrainingAccessGroupIds()) == true) {
      final Collection<GroupDO> groups = new GroupsProvider().getSortedGroups(skill.getTrainingAccessGroupIds());
      if (groups != null) {
        for (final GroupDO group : groups) {
          groupIds.add(group.getId());
        }
      }
    }
    if (skill.getParent() == null) {
      return;
    }
    getTrainingAccessGroupIds(groupIds, skill.getParent());
  }
}
