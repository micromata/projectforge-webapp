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

package org.projectforge.user;

import org.projectforge.access.AccessChecker;
import org.projectforge.access.OperationType;
import org.projectforge.humanresources.HRPlanningRight;

/**
 * These rights implement the checking of the different access types (select, insert, update, delete) itself.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class UserRightAccessCheck<O> extends UserRight
{
  private static final long serialVersionUID = 3075619933808717141L;

  protected UserGroupsRight userGroupsRight;

  public UserRightAccessCheck(final UserRightId id, final UserRightCategory category, final UserRightValue... rightValues)
  {
    super(id, category, rightValues);
  }

  /**
   * Optional for enabling user specific values stored in the data base. See {@link HRPlanningRight} as an example.
   * @param values
   * @param dependsOnGroups
   */
  protected UserGroupsRight initializeUserGroupsRight(final UserRightValue[] values, ProjectForgeGroup... dependsOnGroups)
  {
    userGroupsRight = new UserGroupsRight(null, null, values, dependsOnGroups);
    return userGroupsRight;
  }

  /**
   * The default implementation calls for {@link OperationType#SELECT} {@link AccessChecker#hasReadAccess(UserRightId, boolean)} and
   * otherwise {@link AccessChecker#hasWriteAccess(UserRightId, boolean)}.
   * @param user Check the access for the given user instead of the logged-in user.
   * @param obj null is possible for checking general insert access or general select access.
   * @param oldObj
   * @param operationType
   * @return
   */
  public boolean hasAccess(final PFUserDO user, final O obj, final O oldObj, final OperationType operationType)
  {
    if (operationType == OperationType.SELECT) {
      return UserRights.getAccessChecker().hasRight(user, this.getId(), false, UserRightValue.READONLY, UserRightValue.READWRITE);
    } else {
      return UserRights.getAccessChecker().hasRight(user, this.getId(), false, UserRightValue.READWRITE);
    }
  }

  public boolean hasSelectAccess(final PFUserDO user)
  {
    return hasAccess(user, null, null, OperationType.SELECT);
  }

  public boolean hasSelectAccess(final PFUserDO user, final O obj)
  {
    return hasAccess(user, obj, null, OperationType.SELECT);
  }

  public boolean hasInsertAccess(final PFUserDO user)
  {
    return hasAccess(user, null, null, OperationType.INSERT);
  }

  public boolean hasInsertAccess(final PFUserDO user, final O obj)
  {
    return hasAccess(user, obj, null, OperationType.INSERT);
  }

  public boolean hasUpdateAccess(final PFUserDO user, final O obj, final O oldObj)
  {
    return hasAccess(user, obj, oldObj, OperationType.UPDATE);
  }

  public boolean hasDeleteAccess(final PFUserDO user, final O obj)
  {
    return hasAccess(user, obj, null, OperationType.DELETE);
  }

  /**
   * Calls {@link #hasSelectAccess(Object)} at default.
   * @param accessChecker
   * @param user
   * @param obj
   * @return
   */
  public boolean hasHistoryAccess(final PFUserDO user, final O obj)
  {
    return hasSelectAccess(user, obj);
  }

  /**
   * @return matches of UserGroupsRight if exist, otherwise false.
   * @see UserGroupsRight#matches(UserGroupCache, PFUserDO, UserRightValue)
   */
  public boolean matches(final UserGroupCache userGroupCache, final PFUserDO user, final UserRightValue value)
  {
    if (userGroupsRight != null) {
      return userGroupsRight.matches(userGroupCache, user, value);
    }
    return false;
  }

  /**
   * If userGroupsRight is initialized then {@link UserGroupsRight#isAvailable(UserGroupCache, PFUserDO)} is called, otherwise super.
   * @see org.projectforge.user.UserRight#isAvailable(org.projectforge.user.UserGroupCache, org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean isAvailable(final UserGroupCache userGroupCache, final PFUserDO user)
  {
    if (userGroupsRight != null) {
      return userGroupsRight.isAvailable(userGroupCache, user);
    }
    return super.isAvailable(userGroupCache, user);
  }

  /**
   * If userGroupsRight is initialized then {@link UserGroupsRight#isAvailable(UserGroupCache, PFUserDO, UserRightValue)} is called,
   * otherwise super.
   * @see org.projectforge.user.UserRight#isAvailable(org.projectforge.user.UserGroupCache, org.projectforge.user.PFUserDO,
   *      org.projectforge.user.UserRightValue)
   */
  @Override
  public boolean isAvailable(final UserGroupCache userGroupCache, final PFUserDO user, final UserRightValue value)
  {
    if (userGroupsRight != null) {
      return userGroupsRight.isAvailable(userGroupCache, user, value);
    }
    return super.isAvailable(userGroupCache, user, value);
  }
}
