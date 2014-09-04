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
 * Define the access rights.
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class AttendeeRight extends UserRightAccessCheck<AttendeeDO>
{
  private static final long serialVersionUID = -3590945654632199595L;

  private transient UserGroupCache userGroupCache;

  /**
   * @param id
   * @param category
   * @param rightValues
   */
  public AttendeeRight()
  {
    super(AttendeeDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  private UserGroupCache getUserGroupCache()
  {
    if (userGroupCache == null) {
      userGroupCache = Registry.instance().getUserGroupCache();
    }
    return userGroupCache;
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final AttendeeDO obj, final AttendeeDO oldObj, final OperationType operationType)
  {
    if (UserRights.getAccessChecker().isUserMemberOfAdminGroup(user) == true) {
      return true;
    }

    final AttendeeDO attendee = (oldObj != null) ? oldObj : obj;
    if (attendee == null) {
      return true;
    }
    if (operationType == OperationType.SELECT) {
      return (hasAccess(StringHelper.splitToIntegers(attendee.getTraining().getReadOnlyAccessGroupIds(), ","), user.getId()) == true)
          || (hasAccess(StringHelper.splitToIntegers(attendee.getTraining().getFullAccessGroupIds(), ","), user.getId()) == true)
          || ( user.getId() == attendee.getAttendeeId());
    }
    return hasAccess(StringHelper.splitToIntegers(attendee.getTraining().getFullAccessGroupIds(), ","), user.getId());
  }

  private boolean hasAccess(final Integer[] groupIds, final Integer userId)
  {
    if (getUserGroupCache().isUserMemberOfAtLeastOneGroup(userId, groupIds) == true) {
      return true;
    }
    return false;
  }
}
