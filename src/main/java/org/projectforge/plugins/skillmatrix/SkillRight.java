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

package org.projectforge.plugins.skillmatrix;

import org.apache.commons.lang.StringUtils;
import org.projectforge.access.OperationType;
import org.projectforge.common.StringHelper;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * @author Billy Duong (b.duong@micromata.de)
 *
 */
public class SkillRight extends UserRightAccessCheck<SkillDO>
{
  private static final long serialVersionUID = 6346078004388197890L;

  private static final String delim =",";

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

    boolean ret = false;
    switch (operationType) {
      case SELECT:
      {
        ret = ( (hasFullAccess(skill, user.getId()) == true) || (hasReadonlyAccess(skill, user.getId()) == true) );
        break;
      }
      case INSERT:
      case UPDATE:
      case DELETE:
      {
        ret = (hasFullAccess(skill, user.getId()) == true);
        break;
      }
      default:
        break;
    }
    return ret;
  }

  public boolean hasFullAccess(final SkillDO skill, final Integer userId)
  {
    final Integer[] groupIds = StringHelper.splitToIntegers(getSkillParentsFullAccessGroupIds(skill), delim);
    return hasAccess(groupIds, userId);
  }

  public boolean hasReadonlyAccess(final SkillDO skill, final Integer userId)
  {
    final Integer[] groupIds = StringHelper.splitToIntegers(getSkillParentsReadonlyAccessGroupIds(skill), delim);
    return hasAccess(groupIds, userId);
  }

  private boolean hasAccess(final Integer[] groupIds, final Integer userId)
  {
    if (getUserGroupCache().isUserMemberOfAtLeastOneGroup(userId, groupIds) == true) {
      return true;
    }
    return false;
  }

  public String getSkillParentsFullAccessGroupIds(final SkillDO skill)
  {
    String skillGroupIds = "";
    if (StringUtils.isBlank(skill.getFullAccessGroupIds()) == false) {
      skillGroupIds = skill.getFullAccessGroupIds() + delim;
    }
    SkillDO tmpSkill = skill.getParent();
    while (tmpSkill != null) {
      if (StringUtils.isBlank(tmpSkill.getFullAccessGroupIds()) == false) {
        skillGroupIds += tmpSkill.getFullAccessGroupIds() + delim;
      }
      tmpSkill = tmpSkill.getParent();
    }
    return skillGroupIds;
  }

  public String getSkillParentsReadonlyAccessGroupIds(final SkillDO skill)
  {
    String skillGroupIds = "";
    if (StringUtils.isBlank(skill.getReadonlyAccessGroupIds()) == false) {
      skillGroupIds = skill.getReadonlyAccessGroupIds() + delim;
    }
    SkillDO tmpSkill = skill.getParent();
    while (tmpSkill != null) {
      if (StringUtils.isBlank(tmpSkill.getReadonlyAccessGroupIds()) == false) {
        skillGroupIds += tmpSkill.getReadonlyAccessGroupIds() + delim;
      }
      tmpSkill = tmpSkill.getParent();
    }
    return skillGroupIds;
  }
}
