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

package org.projectforge.plugins.licensemanagement;

import org.apache.commons.lang.StringUtils;
import org.projectforge.access.OperationType;
import org.projectforge.common.StringHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * Every user has access to own to-do's or to-do's he's assigned to. All other users have access if the to-do is assigned to a task and the
 * user has the task access.
 * @author Kai Reinhard (k.reinhard@me.de)
 * 
 */
public class LicenseManagementRight extends UserRightAccessCheck<LicenseDO>
{
  private static final long serialVersionUID = -2928342166476350773L;

  public LicenseManagementRight()
  {
    super(LicenseDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final LicenseDO obj, final LicenseDO oldObj, final OperationType operationType)
  {
    return true;
  }

  public boolean isLicenseKeyVisible(final PFUserDO user, final LicenseDO license)
  {
    if (license == null || license.getId() == null) {
      // Visible for new objects (created by current user):
      return true;
    }
    if (UserRights.getAccessChecker().isLoggedInUserMemberOfAdminGroup() == true) {
      // Administrators have always access:
      return true;
    }
    if (StringUtils.isBlank(license.getOwnerIds()) == true) {
      // No owners defined.
      return false;
    }
    final int[] ids = StringHelper.splitToInts(license.getOwnerIds(), ",", true);
    final int userId = user.getId();
    for (final int id : ids) {
      if (id == userId) {
        // User is member of owners:
        return true;
      }
    }
    return false;

  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasHistoryAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasHistoryAccess(final PFUserDO user, final LicenseDO obj)
  {
    return isLicenseKeyVisible(user, obj);
  }
}
