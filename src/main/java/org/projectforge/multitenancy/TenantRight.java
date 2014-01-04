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

package org.projectforge.multitenancy;

import org.projectforge.access.OperationType;
import org.projectforge.core.GlobalConfiguration;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserGroupsRight;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * 
 * @author Kai Reinhard (k.reinhard@me.de)
 * 
 */
public class TenantRight extends UserRightAccessCheck<TenantDO>
{
  private static final long serialVersionUID = -558887908748357573L;

  @SuppressWarnings("serial")
  public TenantRight()
  {
    super(TenantDao.USER_RIGHT_ID, UserRightCategory.ADMIN, UserRightValue.READONLY, UserRightValue.READWRITE);
    setUserGroupsRight(new UserGroupsRight(null, null, UserRights.FALSE_READONLY_PARTLYREADWRITE_READWRITE, ProjectForgeGroup.ADMIN_GROUP) {
      /**
       * @see org.projectforge.user.UserGroupsRight#isAvailable(org.projectforge.user.UserGroupCache, org.projectforge.user.PFUserDO)
       */
      @Override
      public boolean isAvailable(final UserGroupCache userGroupCache, final PFUserDO user)
      {
        if (GlobalConfiguration.getInstance().isMultiTenancyConfigured() == false) {
          // Right should only be available if multi tenancy is configured.
          return false;
        }
        return super.isAvailable(userGroupCache, user);
      }
    });
  }

  /**
   * @return true if user is member of group FINANCE.
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(java.lang.Object)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final TenantDO obj, final TenantDO oldObj, final OperationType operationType)
  {
    if (GlobalConfiguration.getInstance().isMultiTenancyConfigured() == false) {
      return false;
    }
    if (UserRights.getAccessChecker().isUserMemberOfGroup(user, ProjectForgeGroup.ADMIN_GROUP) == false) {
      return false;
    }
    if (operationType == OperationType.SELECT) {
      return UserRights.getAccessChecker().hasRight(user, getId(), UserRightValue.READONLY, UserRightValue.READWRITE);
    }
    return UserRights.getAccessChecker().hasRight(user, getId(), UserRightValue.READWRITE);
  }
}
