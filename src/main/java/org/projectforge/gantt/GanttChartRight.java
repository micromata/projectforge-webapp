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

package org.projectforge.gantt;

import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRights;

/**
 * 
 * @author Kai Reinhard (k.reinhard@me.de)
 * 
 */
public class GanttChartRight extends UserRightAccessCheck<GanttChartDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GanttChartRight.class);

  private static final long serialVersionUID = -1711148447929915434L;

  public GanttChartRight()
  {
    super(UserRightId.PM_GANTT, UserRightCategory.PM);
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

  /**
   * If the user is owner of the GanttChartDO he has access, otherwise he needs at least select access to the root task. For project
   * managers the user must be additional of the group of the project manager group (assigned to this task) or if no project manager group
   * is available for this task the user should be a member of {@link ProjectForgeGroup#PROJECT_MANAGER}.
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(java.lang.Object)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final GanttChartDO obj)
  {
    if (obj == null) {
      return false;
    }
    return hasAccess(user, obj, obj.getReadAccess());
  }

  /**
   * If the user is owner of the GanttChartDO he has access, otherwise he needs at least select access to the root task. For project
   * managers the user must be additional of the group of the project manager group (assigned to this task) or if no project manager group
   * is available for this task the user should be a member of {@link ProjectForgeGroup#PROJECT_MANAGER}.
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(java.lang.Object)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final GanttChartDO obj, final GanttChartDO oldObj, final OperationType operationType)
  {
    if (obj == null) {
      return false;
    }
    final GanttChartDO gc = oldObj != null ? oldObj : obj;
    return hasAccess(user, gc, gc.getWriteAccess());
  }

  @Override
  public boolean hasInsertAccess(final PFUserDO user)
  {
    return true;
  }

  private boolean hasAccess(final PFUserDO user, final GanttChartDO obj, final GanttAccess access)
  {
    if (UserRights.getAccessChecker().userEqualsToContextUser(obj.getOwner()) == true) {
      // Owner has always access:
      return true;
    }
    if (access == null || access == GanttAccess.OWNER) {
      // No access defined, so only owner has access:
      return false;
    }
    if (access.isIn(GanttAccess.ALL, GanttAccess.PROJECT_MANAGER) == true) {
      if (obj.getTask() == null) {
        // Task needed for these GanttAccess types, so no access:
        return false;
      }
      if (UserRights.getAccessChecker().hasPermission(user, obj.getTaskId(), AccessType.TASKS, OperationType.SELECT, false) == false) {
        // User has no task access:
        return false;
      }
      if (access == GanttAccess.ALL) {
        // User has task access:
        return true;
      }
      final TaskTree taskTree = UserRights.getAccessChecker().getTaskTree();
      final ProjektDO project = taskTree.getProjekt(obj.getTaskId());
      if (project == null) {
        // Project manager group not found:
        return UserRights.getAccessChecker().isUserMemberOfGroup(user, ProjectForgeGroup.PROJECT_MANAGER);
      }
      // Users of the project manager group have access:
      return UserRights.getAccessChecker().getUserGroupCache().isUserMemberOfGroup(user, project.getProjektManagerGroupId());
    } else {
      log.error("Unsupported GanttAccess type: " + access);
    }
    return false;

  }
}
