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

package org.projectforge.fibu;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserContext;
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
public class ProjektRight extends UserRightAccessCheck<ProjektDO>
{
  private static final long serialVersionUID = -3712738266564403670L;

  public ProjektRight()
  {
    super(UserRightId.PM_PROJECT, UserRightCategory.PM, UserRights.FALSE_READONLY_READWRITE);
    initializeUserGroupsRight(UserRights.FALSE_READONLY_READWRITE, UserRights.FIBU_ORGA_PM_GROUPS)
    // All project managers have read only access:
        .setAvailableGroupRightValues(ProjectForgeGroup.PROJECT_MANAGER, UserRightValue.READONLY)
        // All project assistants have no, read or read-only access:
        .setAvailableGroupRightValues(ProjectForgeGroup.PROJECT_ASSISTANT, UserRightValue.READONLY)
        // Read only access for controlling users:
        .setReadOnlyForControlling();
  }

  /**
   * @return True, if {@link UserRightId#PM_PROJECT} is potentially available for the user (independent from the configured value).
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(org.projectforge.access.AccessChecker, org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user)
  {
    return UserRights.getAccessChecker().isAvailable(user, UserRightId.PM_PROJECT);
  }

  @Override
  public boolean hasSelectAccess(final PFUserDO user, final ProjektDO obj)
  {
    if (obj == null) {
      return true;
    }
    if (UserRights.getAccessChecker().isUserMemberOfGroup(user, ProjectForgeGroup.CONTROLLING_GROUP) == true) {
      return true;
    }
    if (UserRights.getAccessChecker().isUserMemberOfGroup(user, ProjectForgeGroup.PROJECT_MANAGER, ProjectForgeGroup.PROJECT_ASSISTANT) == true) {
      if (obj.getProjektManagerGroup() != null
          && UserRights.getUserGroupCache().isUserMemberOfGroup(PFUserContext.getUserId(), obj.getProjektManagerGroupId()) == true) {
        if ((obj.getStatus() == null || obj.getStatus().isIn(ProjektStatus.ENDED) == false) && obj.isDeleted() == false) {
          // Ein Projektleiter sieht keine nicht aktiven oder gelöschten Projekte.
          return true;
        }
      }
      if (UserRights.getAccessChecker().isUserMemberOfGroup(user, ProjectForgeGroup.ORGA_TEAM, ProjectForgeGroup.FINANCE_GROUP) == true) {
        return UserRights.getAccessChecker().hasReadAccess(user, getId(), false) == true;
      }
      return false;
    } else {
      return UserRights.getAccessChecker().hasReadAccess(user, getId(), false) == true;
    }
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final ProjektDO obj, final ProjektDO oldObj, final OperationType operationType)
  {
    return UserRights.getAccessChecker().hasRight(user, getId(), UserRightValue.READWRITE);
  }

  /**
   * History access only allowed for users with read and/or write access.
   * @see org.projectforge.user.UserRightAccessCheck#hasHistoryAccess(java.lang.Object)
   */
  @Override
  public boolean hasHistoryAccess(final PFUserDO user, final ProjektDO obj)
  {
    if (UserRights.getAccessChecker().isUserMemberOfGroup(user, ProjectForgeGroup.CONTROLLING_GROUP) == true) {
      return true;
    }
    return UserRights.getAccessChecker().hasRight(user, getId(), UserRightValue.READWRITE);
  }
}
