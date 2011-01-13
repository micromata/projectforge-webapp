/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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
  private static final String I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF = "access.violation.userNotMemberOf";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessChecker.class);

  private TaskTree taskTree;

  private UserGroupCache userGroupCache;

  private UserRights userRights = UserRights.initialize(this);

  /**
   * Tests for every group the user is assigned to, if the given permission is given.
   * @return true, if the user owns the required permission, otherwise false.
   */
  public boolean hasPermission(final Integer taskId, final AccessType accessType, final OperationType operationType,
      final boolean throwException)
  {
    final PFUserDO user = PFUserContext.getUser();
    Validate.notNull(user);
    if (userGroupCache.isUserMemberOfAdminGroup(user.getId()) == true) {
      // A user group "Admin" has always access.
      return true;
    }
    TaskNode node = taskTree.getTaskNodeById(taskId);
    if (node == null) {
      log.error("Task with " + taskId + " not found.");
      if (throwException == true) {
        throw new AccessException(taskId, accessType, operationType);
      }
      return false;
    }
    Collection<Integer> groupIds = userGroupCache.getUserGroups(user);
    if (groupIds == null) {
      // No groups are assigned to this user.
      if (throwException == true) {
        throw new AccessException(taskId, accessType, operationType);
      }
      return false;
    }
    for (Integer groupId : groupIds) {
      if (node.hasPermission(groupId, accessType, operationType) == true) {
        return true;
      }
    }
    if (throwException == true) {
      throw new AccessException(taskId, accessType, operationType);
    }
    return false;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public TaskTree getTaskTree()
  {
    return taskTree;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
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
  public void checkIsUserMemberOfAdminGroup()
  {
    checkIsUserMemberOfGroup(ProjectForgeGroup.ADMIN_GROUP);
  }

  /**
   * @param userId
   * @return
   * @see org.projectforge.user.UserGroupCache#isUserMemberOfAdminGroup(java.lang.Integer)
   */
  public boolean isUserMemberOfAdminGroup()
  {
    return isUserMemberOfGroup(ProjectForgeGroup.ADMIN_GROUP);
  }

  /**
   * @param userId
   * @return
   * @see org.projectforge.user.UserGroupCache#isUserMemberOfAdminGroup(java.lang.Integer)
   */
  public boolean isUserMemberOfAdminGroup(boolean throwException)
  {
    return isUserMemberOfGroup(throwException, ProjectForgeGroup.ADMIN_GROUP);
  }

  /**
   * Checks if the user is in one of the given groups. If not, an AccessException will be thrown.
   * @see #isUserMemberOfGroup(ProjectForgeGroup...)
   */
  public void checkIsUserMemberOfGroup(ProjectForgeGroup... groups)
  {
    if (isUserMemberOfGroup(groups) == false) {
      throw getUserNotMemberOfException(groups);
    }
  }

  /**
   * Checks if the user of the PFUserContext (logged in user) is member at least of one of the given groups.
   * 
   * @param groups
   * @see #isUserMemberOfGroup(boolean, ProjectForgeGroup...)
   */
  public boolean isUserMemberOfGroup(ProjectForgeGroup... groups)
  {
    return isUserMemberOfGroup(false, groups);
  }

  /**
   * Checks if the user of the PFUserContext (logged in user) is member at least of one of the given groups.
   * 
   * @param throwException default false.
   * @param groups
   * @see #isUserMemberOfGroup(PFUserDO, ProjectForgeGroup...)
   */
  public boolean isUserMemberOfGroup(boolean throwException, ProjectForgeGroup... groups)
  {
    Validate.notNull(groups);
    final PFUserDO user = PFUserContext.getUser();
    if (user == null) {
      // Before user is logged in.
      if (throwException == true) {
        throw getUserNotMemberOfException(groups);
      }
      return false;
    }
    if (throwException == false) {
      return isUserMemberOfGroup(user, groups);
    } else if (isUserMemberOfGroup(user, groups) == true) {
      return true;
    } else {
      throw getUserNotMemberOfException(groups);
    }
  }

  private AccessException getUserNotMemberOfException(ProjectForgeGroup... groups)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < groups.length; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      buf.append(groups[i].toString());
    }
    String str = buf.toString();
    log.error(I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF + ": " + str);
    return new AccessException(I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF, str);
  }

  /**
   * Checks if the given user is at least member of one of the given groups.
   * @param user
   * @param groups
   */
  public boolean isUserMemberOfGroup(PFUserDO user, ProjectForgeGroup... groups)
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
  public boolean isContextUserInSameGroup(final PFUserDO user)
  {
    Collection<Integer> userGroups = userGroupCache.getUserGroups(user);
    if (userGroups == null) {
      // No groups found.
      return false;
    }
    Collection<Integer> currentUserGroups = userGroupCache.getUserGroups(PFUserContext.getUser());
    if (currentUserGroups == null) {
      // User has now associated groups.
      return false;
    }
    for (Integer id : currentUserGroups) {
      if (userGroups.contains(id) == true) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param rightId
   * @param obj
   * @param oldObj
   * @param operationType
   * @param throwException
   */
  @SuppressWarnings("unchecked")
  public boolean hasAccess(final UserRightId rightId, final Object obj, final Object oldObj, final OperationType operationType,
      final boolean throwException)
  {
    final UserRight right = userRights.getRight(rightId);
    Validate.notNull(right);
    boolean result;
    if (right instanceof UserRightAccessCheck< ? >) {
      final PFUserDO user = PFUserContext.getUser();
      Validate.notNull(user);
      switch (operationType) {
        case SELECT:
          if (obj != null) {
            result = ((UserRightAccessCheck) right).hasSelectAccess(obj);
          } else {
            result = ((UserRightAccessCheck) right).hasSelectAccess();
          }
          break;
        case INSERT:
          if (obj != null) {
            result = ((UserRightAccessCheck) right).hasInsertAccess(obj);
          } else {
            result = ((UserRightAccessCheck) right).hasInsertAccess();
          }
          break;
        case UPDATE:
          result = ((UserRightAccessCheck) right).hasUpdateAccess(obj, oldObj);
          break;
        case DELETE:
          result = ((UserRightAccessCheck) right).hasDeleteAccess(obj);
          break;
        default:
          throw new UnsupportedOperationException("Oups, value not supported for OperationType: " + operationType);
      }
      if (result == false && throwException == true) {
        throw new AccessException("access.exception.userHasNotRight", rightId, operationType);
      }
      return result;
    }
    if (operationType == OperationType.SELECT) {
      return hasRight(rightId, throwException, UserRightValue.READONLY, UserRightValue.READWRITE);
    } else {
      return hasRight(rightId, throwException, UserRightValue.READWRITE);
    }
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#SELECT} and both Objects as
   * null.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasSelectAccess(final UserRightId rightId, final boolean throwException)
  {
    return hasAccess(rightId, null, null, OperationType.SELECT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#SELECT}.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasSelectAccess(final UserRightId rightId, final Object obj, final boolean throwException)
  {
    return hasAccess(rightId, obj, null, OperationType.SELECT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#INSERT}.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasInsertAccess(final UserRightId rightId, final Object obj, final boolean throwException)
  {
    return hasAccess(rightId, obj, null, OperationType.INSERT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#INSERT}.
   * @param rightId
   * @param obj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasInsertAccess(final UserRightId rightId, final boolean throwException)
  {
    return hasAccess(rightId, null, null, OperationType.INSERT, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#UPDATE}.
   * @param rightId
   * @param obj
   * @param oldObj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasUpdateAccess(final UserRightId rightId, final Object obj, final Object oldObj, final boolean throwException)
  {
    return hasAccess(rightId, obj, oldObj, OperationType.UPDATE, throwException);
  }

  /**
   * Calls {@link #hasAccess(UserRightId, Object, Object, OperationType, boolean)} with {@link OperationType#DELETE}.
   * @param rightId
   * @param obj
   * @param oldObj
   * @param throwException
   * @see #hasAccess(UserRightId, Object, Object, OperationType, boolean)
   */
  public boolean hasDeleteAccess(final UserRightId rightId, final Object oldObj, final Object obj, final boolean throwException)
  {
    return hasAccess(rightId, obj, oldObj, OperationType.DELETE, throwException);
  }

  /**
   * Throws now exception if the right check fails.
   * @see #hasRight(UserRightId, boolean, UserRightValue...)
   */
  public boolean hasRight(final UserRightId rightId, final UserRightValue... values)
  {
    return hasRight(rightId, false, values);
  }

  /**
   * Checks the availability and the demanded value of the right for the context user. The right will be checked itself on required
   * constraints, e. g. if assigned groups required.
   * @param rightId
   * @param values At least one of the values should match.
   * @param throwException
   * @return
   */
  public boolean hasRight(final UserRightId rightId, final boolean throwException, final UserRightValue... values)
  {
    final PFUserDO user = PFUserContext.getUser();
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
  public boolean hasReadAccess(final UserRightId rightId, boolean throwException)
  {
    return hasRight(rightId, throwException, UserRightValue.READONLY, UserRightValue.READWRITE);
  }

  /**
   * Calls {@link #hasReadAccess(UserRightId, boolean)} with throwException = true.
   * @param rightId
   * @param value
   * @see #hasReadAccess(UserRightId, boolean)
   */
  public boolean checkReadAccess(final UserRightId rightId)
  {
    return hasReadAccess(rightId, true);
  }

  /**
   * @param rightId
   * @param throwException
   * @see #hasRight(UserRightId, boolean, UserRightValue...)
   */
  public boolean hasWriteAccess(final UserRightId rightId, boolean throwException)
  {
    return hasRight(rightId, throwException, UserRightValue.READWRITE);
  }

  /**
   * Calls {@link #hasWriteAccess(UserRightId, boolean)} with throwException = true.
   * @param rightId
   * @param value
   * @see #hasWriteAccess(UserRightId, boolean)
   */
  public boolean checkWriteAccess(final UserRightId rightId)
  {
    return hasWriteAccess(rightId, true);
  }

  @SuppressWarnings("unchecked")
  public boolean hasHistoryAccess(final UserRightId rightId, final Object obj, final boolean throwException)
  {
    final UserRight right = userRights.getRight(rightId);
    Validate.notNull(right);
    if (right instanceof UserRightAccessCheck< ? >) {
      final PFUserDO user = PFUserContext.getUser();
      Validate.notNull(user);
      if (((UserRightAccessCheck) right).hasHistoryAccess(obj) == true) {
        return true;
      } else if (throwException == true) {
        throw new AccessException("access.exception.userHasNotRight", rightId, "history");
      } else {
        return false;
      }
    } else {
      return hasRight(rightId, throwException, UserRightValue.READONLY, UserRightValue.READWRITE);
    }
  }

  /**
   * Calls {@link #hasRight(UserRightId, UserRightValue, boolean)} with throwException = true.
   * @param rightId
   * @param value
   * @see #hasRight(UserRightId, UserRightValue, boolean)
   */
  public boolean checkRight(final UserRightId rightId, final UserRightValue... values)
  {
    return hasRight(rightId, true, values);
  }

  /**
   * Gets the UserRight and calls {@link UserRight#isAvailable(UserGroupCache, PFUserDO)}.
   * @param rightId
   * @return
   */
  public boolean isAvailable(final UserRightId rightId)
  {
    final UserRight right = userRights.getRight(rightId);
    final PFUserDO user = PFUserContext.getUser();
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
    Validate.notNull(user);
    if ("demo".equals(user.getUsername()) == false) {
      return false;
    }
    return true;
  }

  /**
   * Throws an exception if the current logged-in user is a demo user.
   */
  public void checkDemoUser()
  {
    if (isDemoUser() == true) {
      throw new AccessException("access.exception.demoUserHasNoAccess");
    }
  }
}
