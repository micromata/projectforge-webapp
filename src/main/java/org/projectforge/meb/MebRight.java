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

package org.projectforge.meb;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserGroupsRight;
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
public class MebRight extends UserRightAccessCheck<MebEntryDO>
{

  private static final long serialVersionUID = 2985751765063922520L;

  public MebRight()
  {
    super(UserRightId.MISC_MEB, UserRightCategory.MISC, UserRightValue.TRUE);
  }

  /**
   * Every user can insert new MEB entries.
   * @see org.projectforge.user.UserRightAccessCheck#hasInsertAccess()
   */
  @Override
  public boolean hasInsertAccess(final PFUserDO user)
  {
    return true;
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
  public boolean hasSelectAccess(final PFUserDO user, final MebEntryDO obj)
  {
    if (obj == null) {
      return true;
    }
    if (obj.getOwner() == null) {
      return UserRights.getAccessChecker().isUserMemberOfAdminGroup(user);
    } else {
      return UserRights.getAccessChecker().userEquals(user, obj.getOwner());
    }
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final MebEntryDO obj, final MebEntryDO oldObj, final OperationType operationType)
  {
    if (obj == null) {
      return false;
    }
    if (obj.getOwner() == null) {
      return UserRights.getAccessChecker().isUserMemberOfAdminGroup(user);
    } else {
      return UserRights.getAccessChecker().userEquals(user, obj.getOwner());
    }
  }

  @Override
  public boolean hasUpdateAccess(final PFUserDO user, MebEntryDO obj, MebEntryDO oldObj)
  {
    if (oldObj != null && UserRights.getAccessChecker().isUserMemberOfAdminGroup(user) == true && oldObj.getOwner() == null) {
      // Otherwise an admin couldn't assign unassigned entries:
      return true;
    }
    return hasAccess(user, obj, oldObj, OperationType.UPDATE);
  }

  /**
   * @param userGroupCache
   * @param user
   * @param value
   * @return Always true.
   * @see UserGroupsRight#matches(UserGroupCache, PFUserDO, UserRightValue)
   */
  @Override
  public boolean matches(final UserGroupCache userGroupCache, final PFUserDO user, final UserRightValue value)
  {
    return true;
  }
}
