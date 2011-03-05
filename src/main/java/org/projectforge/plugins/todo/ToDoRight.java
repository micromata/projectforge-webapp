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

package org.projectforge.plugins.todo;

import org.apache.commons.lang.ObjectUtils;
import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;

/**
 * @author Kai Reinhard (k.reinhard@me.de)
 * 
 */
public class ToDoRight extends UserRightAccessCheck<ToDoDO>
{
  private static final long serialVersionUID = -2928342166476350773L;

  public ToDoRight()
  {
    super(ToDoDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  /**
   * @return true if the reporter or assignee is the logged-in user, otherwise false.
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final ToDoDO obj, final ToDoDO oldObj, final OperationType operationType)
  {
    final ToDoDO toDo = oldObj != null ? oldObj : obj;
    if (toDo == null) {
      return true; // General insert and select access given by default.
    }
    if (ObjectUtils.equals(user.getId(), toDo.getAssigneeId()) == true
        || ObjectUtils.equals(user.getId(), toDo.getReporterId()) == true) {
      return true;
    }
    return false;
  }
}
