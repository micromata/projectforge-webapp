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

package org.projectforge.humanresources;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * 
 * @author Kai Reinhard (k.reinhard@me.de)
 * 
 */
public class HRPlanningRight extends UserRightAccessCheck<HRPlanningDO>
{
  private static final long serialVersionUID = 3318798287641861759L;

  public HRPlanningRight()
  {
    super(UserRightId.PM_HR_PLANNING, UserRightCategory.PM, UserRights.FALSE_READONLY_READWRITE);
    initializeUserGroupsRight(UserRights.FALSE_READONLY_READWRITE, UserRights.FIBU_ORGA_PM_GROUPS)
    // All project managers have read write access:
        .setAvailableGroupRightValues(ProjectForgeGroup.PROJECT_MANAGER, UserRightValue.READWRITE)
        // All project assistants have no, read or read-write access:
        .setAvailableGroupRightValues(ProjectForgeGroup.PROJECT_ASSISTANT, UserRights.FALSE_READONLY_READWRITE)
        // Read only access for controlling users:
        .setReadOnlyForControlling();
  }

  /**
   * @return true.
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(org.projectforge.access.AccessChecker, org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user)
  {
    return true;
  }

  @Override
  public boolean hasSelectAccess(final PFUserDO user, final HRPlanningDO obj)
  {
    if (UserRights.getAccessChecker().userEquals(user, obj.getUser()) == true) {
      return true;
    }
    return UserRights.getAccessChecker().hasRight(user, getId(), UserRightValue.READONLY, UserRightValue.READWRITE);
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final HRPlanningDO obj, final HRPlanningDO oldObj, final OperationType operationType)
  {
    return UserRights.getAccessChecker().hasRight(user, getId(), UserRightValue.READWRITE);
  }

  /**
   * History access only allowed for users with read and/or write access.
   * @see org.projectforge.user.UserRightAccessCheck#hasHistoryAccess(java.lang.Object)
   */
  @Override
  public boolean hasHistoryAccess(final PFUserDO user, final HRPlanningDO obj)
  {
    return UserRights.getAccessChecker().hasRight(user, getId(), UserRightValue.READONLY, UserRightValue.READWRITE);
  }
}
