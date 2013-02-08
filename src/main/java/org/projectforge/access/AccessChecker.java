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

package org.projectforge.access;

import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.projectforge.common.StringHelper;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRight;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightDO;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * This class contains some helper methods for evaluation of user and group access'.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class AccessChecker
{
  public static final String I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF = "access.violation.userNotMemberOf";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessChecker.class);

  private TaskTree taskTree;

  private UserGroupCache userGroupCache;

  private final UserRights userRights = UserRights.initialize(this);

  /**
   * Tests for every group the user is assigned to, if the given permission is given.
   * @return true, if the user owns the required permission, otherwise false.
   */
  public boolean hasLoggedInUserPermission(final Integer taskId, final AccessType accessType, final OperationType operationType,
      final boolean throwException)
  {
    return hasPermission(PFUserContext.getUser(), taskId, accessType, operationType, throwException);
  }

  /**
   * Tests for every group the user is assigned to, if the given permission is given.
   * @return true, if the user owns the required permission, otherwise false.
   */
  public boolean hasPermission(final PFUserDO user, final Integer taskId, final AccessType accessType, final OperationType operationType,
      final boolean throwException)
  {
    Validate.notNull(user);
    if (userGroupCache.isUserMemberOfAdminGroup(user.getId()) == true) {
      // A user group "Admin" has always access.
      return true;
    }
    final TaskNode node = taskTree.getTaskNodeById(taskId);
    if (node == null) {
      log.error("Task with " + taskId + " not found.");
      if (throwException == true) {
        throw new AccessException(taskId, accessType, operationType);
      }
      return false;
    }
    final Collection<Integer> groupIds = userGroupCache.getUserGroups(user);
    if (groupIds == null) {
      // No groups are assigned to this user.
      if (throwException == true) {
        throw new AccessException(taskId, accessType, operationType);
      }
      return false;
    }
    for (final Integer groupId : groupIds) {
      if (node.hasPermission(groupId, accessType, operationType) == true) {
        return true;
      }
    }
    if (throwException == true) {
      throw new AccessException(taskId, accessType, operationType);
    }
    return false;
  }

  public void setTaskTree(final TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public TaskTree getTaskTree()
  {
    return taskTree;
  }

  public void setUserGroupCache(final UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  public UserGroupCache getUserGroupCache()
  {
    return userGroupCache;
  }

  /**
   * Checks if the user is an admin user (member of admin group). If not, an AccessException will be thrown.
   * @see #isUserMemberOfAdminGroup()
   */
  public void checkIsLoggedInUserMemberOfAdminGroup()
  {
    checkIsLoggedInUserMemberOfGroup(ProjectForgeGroup.ADMIN_GROUP);
  }

  /**
   * @param userId
   * @return
   * @see org.projectforge.user.UserGroupCache#isUserMemberOfAdminGroup(java.lang.Integer)
   */
  public boolean isLoggedInUserMemberOfAdminGroup()
  {
    return isUserMemberOfAdminGroup(PFUserContext.getUser());
  }

  /**
   * @param userId
   * @return
   * @see org.projectforge.user.UserGroupCache#isUserMemberOfAdminGroup(java.lang.Integer)
   */
  public boolean isUserMemberOfAdminGroup(final PFUserDO user)
  {
    return isUserMemberOfGroup(user, ProjectForgeGroup.ADMIN_GROUP);
  }

  /**
   * @param userId
   * @return
   * @see org.projectforge.user.UserGroupCache#isUserMemberOfAdminGroup(java.lang.Integer)
   */
  public boolean isLoggedInUserMemberOfAdminGroup(final boolean throwException)
  {
    return isLoggedInUserMemberOfGroup(throwException, ProjectForgeGroup.ADMIN_GROUP);
  }

  /**
   * @param userId
   * @return
   * @see org.projectforge.user.UserGroupCache#isUserMemberOfAdminGroup(java.lang.Integer)
   */
  public boolean isUserMemberOfAdminGroup(final PFUserDO user, final boolean throwException)
  {
    return isUserMemberOfGroup(user, throwException, ProjectForgeGroup.ADMIN_GROUP);
  }

  /**
   * Checks if the user is in one of the given groups. If not, an AccessException will be thrown.
   * @see #isUserMemberOfGroup(ProjectForgeGroup...)
   */
  public void checkIsLoggedInUserMemberOfGroup(final ProjectForgeGroup... groups)
  {
    checkIsUserMemberOfGroup(PFUserContext.getUser(), groups);
  }

  /**
   * Checks if the user is in one of the given groups. If not, an AccessException will be thrown.
   * @see #isUserMemberOfGroup(ProjectForgeGroup...)
   */
  public void checkIsUserMemberOfGroup(final PFUserDO user, final ProjectForgeGroup... groups)
  {
    if (isUserMemberOfGroup(user, groups) == false) {
      throw getLoggedInUserNotMemberOfException(groups);
    }
  }

  /**
   * Checks if the user of the PFUserContext (logged in user) is member at least of one of the given groups.
   * 
   * @param groups
   * @see #isUserMemberOfGroup(boolean, ProjectForgeGroup...)
   */
  public boolean isLoggedInUserMemberOfGroup(final ProjectForgeGroup... groups)
  {
    return isLoggedInUserMemberOfGroup(false, groups);
  }

  /**
   * Checks if the user of the PFUserContext (logged in user) is member at least of one of the given groups.
   * 
   * @param throwException default false.
   * @param groups
   * @see #isUserMemberOfGroup(PFUserDO, ProjectForgeGroup...)
   */
  public boolean isLoggedInUserMemberOfGroup(final boolean throwException, final ProjectForgeGroup... groups)
  {
    return isUserMemberOfGroup(PFUserContext.getUser(), throwException, groups);
  }

  /**
   * Checks if the user of the PFUserContext (logged in user) is member at least of one of the given groups.
   * 
   * @param throwException default false.
   * @param groups
   * @see #isUserMemberOfGroup(PFUserDO, ProjectForgeGroup...)
   */
  public boolean isUserMemberOfGroup(final PFUserDO user, final boolean throwException, final ProjectForgeGroup... groups)
  {
    Validate.notNull(groups);
    if (user == null) {
      // Before user is logged in.
      if (throwException == true) {
        throw getLoggedInUserNotMemberOfException(groups);
      }
      return false;
    }
    if (throwException == false) {
      return isUserMemberOfGroup(user, groups);
    } else if (isUserMemberOfGroup(user, groups) == true) {
      return true;
    } else {
      throw getLoggedInUserNotMemberOfException(groups);
    }
  }

  private AccessException getLoggedInUserNotMemberOfException(final ProjectForgeGroup... groups)
  {
    final StringBuffer buf = new StringBuffer();
    for (int i = 0; i < groups.length; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      buf.append(groups[i].toString());
    }
    final String str = buf.toString();
    log.error(I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF + ": " + str);
    return new AccessException(I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF, str);
  }

  /**
   * Checks if the given user is at least member of one of the given groups.
   * @param user
   * @param groups
   */
  public boolean isUserMemberOfGroup(final PFUserDO user, final ProjectForgeGroup... groups)
  {
    return userGroupCache.isUserMemberOfGroup(user, groups);
  }

  /**
   * Compares the two given users on equality. The pk's will be compared. If one or more user's or pk's are null, false will be returned.
   * @param u1
   * @param u2
   * @return true, if both user pk's are not null and equal.
   */
  public boolean userEquals(final PFUserDO u1, final PFUserDO u2)
  {
    if (u1 == null || u2 == null || u1.getId() == null) {
      return false;
    }
    return u1.getId().equals(u2.getId());
  }

  /**
   * Gets the user from the PFUserContext and compares the both user.
   * @param user
   * @return
   * @see AccessChecker#userEquals(PFUserDO, PFUserDO)
   */
  public boolean userEqualsToContextUser(final PFUserDO user)
  {
    return userEquals(PFUserContext.getUser(), user);
  }

  /**
   * Is the current context user in at minimum one group of the groups assigned to the given user?
   * @param user
   * @return
   */
  public boolean isLoggedInUserInSameGroup(final PFUserDO user)
  {
    return areUsersInSameGroup(PFUserContext.getUser(), user);
  }

  /**
   * Is the current context user in at minimum one group of the groups assigned to the given user?
   * @param user2
   * @return
   */
  public boolean areUsersInSameGroup(final PFUserDO user1, final PFUserDO user2)
  {
    final Collection<Integer> userGroups = userGroupCache.getUserGroups(user2);
    if (userGroups == null) {
      // No groups found.
      return false;
    }
    final Collection<Integer> currentUserGroups = userGroupCache.getUserGroups(user1);
    if (currentUserGroups == null) {
      // User has now associated groups.
      return false;
    }
    for (final Integer id : currentUserGroups) {
      if (userGroups.contains(id) == true) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param user Check the access for the given user instead of the logged-in user.
   * @param rightId
   * @param obj
   * @param oldObj
   * @param operationType
   * @param throwException
   */
  @SuppressWarnings({ "unchecked", "rawtypes"})
  public boolean hasAccess(final PFUserDO user, final UserRightId rightId, final Object obj, final Object oldObj,
      final OperationType operationType, final boolean throwException)
  {
    final UserRight right = userRights.getRight(rightId);
    Validate.notNull(right);
    boolean result;
    if (right instanceof UserRightAccessCheck< ? >) {
      Validate.notNull(user);
      switch (operationType) {
        case SELECT:
          if (obj != null) {
            result = ((UserRightAccessCheck) right).hasSelectAccess(user, obj);
          } else {
            result = ((UserRightAccessCheck) right).hasSelectAccess(user);
          }
          break;
        case INSERT:
          if (obj != null) {
            result = ((UserRightAccessCheck) right).hasInsertAccess(user, obj);
          } else {
            result = ((UserRightAccessCheck) right).hasInsertAccess(user);
          }
          break;
        case UPDATE:
          result = ((UserRightAccessCheck) right).hasUpdateAccess(user, obj, oldObj);
          break;
        case DELETE:
          result = ((UserRightAccessCheck) right).hasDeleteAccess(user, obj);
          break;
        default:
          throw new UnsupportedOperationException("Oups, value not supported for OperationType: " + operationType);
      }
      if (result == false && throwException == true) {
        throw new AccessException(user, "access.exception.userHasNotRight", rightId, operationType);
      }
      return result;
    }
    if (operationType == OperationType.SELECT) {
      return hasRight(user, rightId, throwException, UserRightValue.READONLY, UserRightValue.READWRITE);
    } else {
      return hasRight(user, rightId, throwException, UserRightValue.READWRITE);
    }
  }

  /**
   * Use context user (logged-in user).
   * @param rightId
   * @param obj
   * @param oldObj
   * @param operationType
   * @param throwException
   * @see #hasAccess(PFUserDO, UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasLoggedInUserAccess(final UserRightId rightId, final Object obj, final Object oldObj, final OperationType operationType,
      final boolean throwException)
  {
    return hasAccess(PFUserContext.getUser(), rightId, obj, oldObj, operationType, throwException);
  }

  /**
   * Use context user (logged-in user).
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasSelectAccess(PFUserDO, UserRightId, boolean)
   */
  public boolean hasLoggedInUserSelectAccess(final UserRightId rightId, final boolean throwException)
  {
    return hasSelectAccess(PFUserContext.getUser(), rightId, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#SELECT} and both Objects as
   * null.
   * @param user Check the access for the given user instead of the logged-in user.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasSelectAccess(final PFUserDO user, final UserRightId rightId, final boolean throwException)
  {
    return hasAccess(user, rightId, null, null, OperationType.SELECT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#SELECT}.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasLoggedInUserSelectAccess(final UserRightId rightId, final Object obj, final boolean throwException)
  {
    return hasLoggedInUserAccess(rightId, obj, null, OperationType.SELECT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#INSERT}.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasLoggedInUserInsertAccess(final UserRightId rightId, final Object obj, final boolean throwException)
  {
    return hasLoggedInUserAccess(rightId, obj, null, OperationType.INSERT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#INSERT}.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasLoggedInUserInsertAccess(final UserRightId rightId, final boolean throwException)
  {
    return hasLoggedInUserAccess(rightId, null, null, OperationType.INSERT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#INSERT}.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasInsertAccess(final PFUserDO user, final UserRightId rightId, final boolean throwException)
  {
    return hasAccess(user, rightId, null, null, OperationType.INSERT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#UPDATE}.
   * @param rightId
   * @param obj
   * @param oldObj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasLoggedInUserUpdateAccess(final UserRightId rightId, final Object obj, final Object oldObj, final boolean throwException)
  {
    return hasLoggedInUserAccess(rightId, obj, oldObj, OperationType.UPDATE, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#DELETE}.
   * @param rightId
   * @param obj
   * @param oldObj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasLoggedInUserDeleteAccess(final UserRightId rightId, final Object oldObj, final Object obj, final boolean throwException)
  {
    return hasLoggedInUserAccess(rightId, obj, oldObj, OperationType.DELETE, throwException);
  }

  /**
   * Throws now exception if the right check fails.
   * @see #hasRight(UserRightId, boolean, UserRightValue...)
   * @deprec
   */
  public boolean hasLoggedInUserRight(final UserRightId rightId, final UserRightValue... values)
  {
    return hasRight(PFUserContext.getUser(), rightId, false, values);
  }

  /**
   * Throws now exception if the right check fails.
   * @see #hasRight(UserRightId, boolean, UserRightValue...)
   */
  public boolean hasRight(final PFUserDO user, final UserRightId rightId, final UserRightValue... values)
  {
    return hasRight(user, rightId, false, values);
  }

  /**
   * Checks the availability and the demanded value of the right for the context user. The right will be checked itself on required
   * constraints, e. g. if assigned groups required.
   * @param rightId
   * @param values At least one of the values should match.
   * @param throwException
   */
  public boolean hasLoggedInUserRight(final UserRightId rightId, final boolean throwException, final UserRightValue... values)
  {
    return hasRight(PFUserContext.getUser(), rightId, throwException, values);
  }

  /**
   * Checks the availability and the demanded value of the right for the context user. The right will be checked itself on required
   * constraints, e. g. if assigned groups required.
   * @param user Check the access for the given user instead of the logged-in user.
   * @param rightId
   * @param values At least one of the values should match.
   * @param throwException
   * @return
   */
  public boolean hasRight(final PFUserDO user, final UserRightId rightId, final boolean throwException, final UserRightValue... values)
  {
    Validate.notNull(user);
    Validate.notNull(values);
    final UserRightDO rightDO = user.getRight(rightId);
    final UserRight right = userRights.getRight(rightId);
    for (final UserRightValue value : values) {
      if ((rightDO == null || rightDO.getValue() == null) && right.matches(userGroupCache, user, value) == true) {
        return true;
      }
      if (rightDO != null && rightDO.getValue() == value) {
        if (right != null && right.isAvailable(userGroupCache, user, value) == true) {
          return true;
        }
      }
    }
    if (throwException == true) {
      throw new AccessException("access.exception.userHasNotRight", rightId, StringHelper.listToString(", ", (Object[]) values));
    }
    return false;
  }

  /**
   * @param rightId
   * @param throwException
   * @see #hasRight(UserRightId, boolean, UserRightValue...)
   */
  public boolean hasLoggedInUserReadAccess(final UserRightId rightId, final boolean throwException)
  {
    return hasReadAccess(PFUserContext.getUser(), rightId, throwException);
  }

  /**
   * @param rightId
   * @param throwException
   * @see #hasRight(UserRightId, boolean, UserRightValue...)
   */
  public boolean hasReadAccess(final PFUserDO user, final UserRightId rightId, final boolean throwException)
  {
    return hasRight(user, rightId, throwException, UserRightValue.READONLY, UserRightValue.READWRITE);
  }

  /**
   * Calls {@link #hasReadAccess(UserRightId, boolean)} with throwException = true.
   * @param rightId
   * @param value
   * @see #hasReadAccess(UserRightId, boolean)
   */
  public boolean checkLoggedInUserReadAccess(final UserRightId rightId)
  {
    return hasLoggedInUserReadAccess(rightId, true);
  }

  /**
   * @param rightId
   * @param throwException
   * @see #hasRight(UserRightId, boolean, UserRightValue...)
   */
  public boolean hasLoggedInUserWriteAccess(final UserRightId rightId, final boolean throwException)
  {
    return hasLoggedInUserRight(rightId, throwException, UserRightValue.READWRITE);
  }

  /**
   * Calls {@link #hasWriteAccess(UserRightId, boolean)} with throwException = true.
   * @param rightId
   * @param value
   * @see #hasWriteAccess(UserRightId, boolean)
   */
  public boolean checkLoggedInUserWriteAccess(final UserRightId rightId)
  {
    return hasLoggedInUserWriteAccess(rightId, true);
  }

  public boolean hasLoggedInUserHistoryAccess(final UserRightId rightId, final Object obj, final boolean throwException)
  {
    return hasHistoryAccess(PFUserContext.getUser(), rightId, obj, throwException);
  }

  @SuppressWarnings({ "unchecked", "rawtypes"})
  public boolean hasHistoryAccess(final PFUserDO user, final UserRightId rightId, final Object obj, final boolean throwException)
  {
    final UserRight right = userRights.getRight(rightId);
    Validate.notNull(right);
    if (right instanceof UserRightAccessCheck< ? >) {
      Validate.notNull(user);
      if (((UserRightAccessCheck) right).hasHistoryAccess(user, obj) == true) {
        return true;
      } else if (throwException == true) {
        throw new AccessException("access.exception.userHasNotRight", rightId, "history");
      } else {
        return false;
      }
    } else {
      return hasRight(user, rightId, throwException, UserRightValue.READONLY, UserRightValue.READWRITE);
    }
  }

  /**
   * Calls {@link #hasRight(UserRightId, UserRightValue, boolean)} with throwException = true.
   * @param rightId
   * @param value
   * @see #hasRight(UserRightId, UserRightValue, boolean)
   */
  public boolean checkLoggedInUserRight(final UserRightId rightId, final UserRightValue... values)
  {
    return hasLoggedInUserRight(rightId, true, values);
  }

  /**
   * Calls {@link #hasRight(UserRightId, UserRightValue, boolean)} with throwException = true.
   * @param rightId
   * @param value
   * @see #hasRight(UserRightId, UserRightValue, boolean)
   */
  public boolean checkUserRight(final PFUserDO user, final UserRightId rightId, final UserRightValue... values)
  {
    return hasRight(user, rightId, true, values);
  }

  /**
   * Gets the UserRight and calls {@link UserRight#isAvailable(UserGroupCache, PFUserDO)}.
   * @param rightId
   * @return
   */
  public boolean isAvailable(final UserRightId rightId)
  {
    return isAvailable(PFUserContext.getUser(), rightId);
  }

  /**
   * Gets the UserRight and calls {@link UserRight#isAvailable(UserGroupCache, PFUserDO)}.
   * @param rightId
   * @return
   */
  public boolean isAvailable(final PFUserDO user, final UserRightId rightId)
  {
    final UserRight right = userRights.getRight(rightId);
    return right != null && right.isAvailable(userGroupCache, user) == true;
  }

  public boolean isDemoUser()
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      return false;
    }
    return isDemoUser(user);
  }

  public boolean isDemoUser(final Integer userId)
  {
    final PFUserDO user = userGroupCache.getUser(userId);
    return isDemoUser(user);
  }

  public boolean isDemoUser(final PFUserDO user)
  {
    if (user == null) {
      return false;
    }
    if ("demo".equals(user.getUsername()) == false) {
      return false;
    }
    return true;
  }

  public boolean isRestrictedUser()
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      return true;
    }
    return isRestrictedUser(user);
  }

  public boolean isRestrictedUser(final Integer userId)
  {
    final PFUserDO user = userGroupCache.getUser(userId);
    return isDemoUser(user);
  }

  public boolean isRestrictedUser(final PFUserDO user)
  {
    if (user == null) {
      return false;
    }
    return user.isRestrictedUser();
  }

  /**
   * Throws an exception if the current logged-in user is a demo user.
   */
  public void checkRestrictedUser()
  {
    if (isRestrictedUser() == true) {
      throw new AccessException("access.exception.demoUserHasNoAccess");
    }
  }

  public boolean isRestrictedOrDemoUser()
  {
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      return false;
    }
    return isRestrictedOrDemoUser(user);
  }

  public boolean isRestrictedOrDemoUser(final Integer userId)
  {
    final PFUserDO user = userGroupCache.getUser(userId);
    return isRestrictedOrDemoUser(user);
  }

  public boolean isRestrictedOrDemoUser(final PFUserDO user)
  {
    if (user == null) {
      return false;
    }
    return isRestrictedUser(user) || isDemoUser(user);
  }

  /**
   * Throws an exception if the current logged-in user is a demo user.
   */
  public void checkRestrictedOrDemoUser()
  {
    if (isDemoUser() == true) {
      throw new AccessException("access.exception.demoUserHasNoAccess");
    }
    if (isRestrictedUser() == true) {
      throw new AccessException("access.exception.demoUserHasNoAccess");
    }
  }

  /**
   * @return true if logged-in-user is member of {@link ProjectForgeGroup#FINANCE_GROUP}, {@link ProjectForgeGroup#CONTROLLING_GROUP} or
   *         {@link ProjectForgeGroup#PROJECT_MANAGER}. Returns also true if user is member of {@link ProjectForgeGroup#ORGA_TEAM} and has
   *         the
   */
  public boolean hasLoggedInUserAccessToTimesheetsOfOtherUsers()
  {
    final PFUserDO loggedInUser = PFUserContext.getUser();
    Validate.notNull(loggedInUser);
    if (isUserMemberOfGroup(loggedInUser, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
        ProjectForgeGroup.PROJECT_MANAGER) == true) {
      return true;
    }
    if (isUserMemberOfGroup(loggedInUser, ProjectForgeGroup.ORGA_TEAM) == true
        && hasRight(loggedInUser, UserRightId.PM_HR_PLANNING, UserRightValue.READONLY, UserRightValue.READWRITE)) {
      return true;
    }
    return false;
  }
}
