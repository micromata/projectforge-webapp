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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.criterion.Order;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.book.BookFilter;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ToDoDao extends BaseDao<ToDoDO>
{
  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "reporter.username", "reporter.firstname", "reporter.lastname",
      "assignee.username", "assignee.firstname", "assignee.lastname", "task.title", "task.taskpath"};

  private UserDao userDao;

  private TaskTree taskTree;

  public ToDoDao()
  {
    super(ToDoDO.class);
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  @Override
  public List<ToDoDO> getList(BaseSearchFilter filter)
  {
    final ToDoFilter myFilter;
    if (filter instanceof BookFilter) {
      myFilter = (ToDoFilter) filter;
    } else {
      myFilter = new ToDoFilter(filter);
    }
    final QueryFilter queryFilter = new QueryFilter(myFilter);
    if (StringUtils.isBlank(myFilter.getSearchString()) == true) {
      // To be done.
    }
    queryFilter.addOrder(Order.desc("created"));
    return getList(queryFilter);
  }

  public void setAssignee(final ToDoDO todo, final Integer userId)
  {
    final PFUserDO user = userDao.getOrLoad(userId);
    todo.setAssignee(user);
  }

  public void setReporter(final ToDoDO todo, final Integer userId)
  {
    final PFUserDO user = userDao.getOrLoad(userId);
    todo.setReporter(user);
  }

  public void setTask(final ToDoDO todo, final Integer taskId)
  {
    final TaskDO task = taskTree.getTaskById(taskId);
    todo.setTask(task);
  }

  /**
   * return Always true, no generic select access needed for book objects.
   * @see org.projectforge.core.BaseDao#hasSelectAccess()
   */
  @Override
  public boolean hasSelectAccess(boolean throwException)
  {
    return true;
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(ToDoDO obj, ToDoDO oldObj, OperationType operationType, boolean throwException)
  {
    // Reporter and Assignee have full access, otherwise task access is used if a task is given.
    return accessChecker.hasPermission(obj.getTaskId(), AccessType.TASKS, operationType, throwException);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasUpdateAccess(Object, Object)
   */
  @Override
  public boolean hasUpdateAccess(ToDoDO obj, ToDoDO dbObj, boolean throwException)
  {
    Validate.notNull(dbObj);
    Validate.notNull(obj);
    Validate.notNull(dbObj.getTaskId());
    Validate.notNull(obj.getTaskId());
    if (accessChecker.hasPermission(obj.getTaskId(), AccessType.TASKS, OperationType.UPDATE, throwException) == false) {
      return false;
    }
    if (dbObj.getTaskId().equals(obj.getTaskId()) == false) {
      // User moves the object to another task:
      if (accessChecker.hasPermission(obj.getTaskId(), AccessType.TASKS, OperationType.INSERT, throwException) == false) {
        // Inserting of object under new task not allowed.
        return false;
      }
      if (accessChecker.hasPermission(dbObj.getTaskId(), AccessType.TASKS, OperationType.DELETE, throwException) == false) {
        // Deleting of object under old task not allowed.
        return false;
      }
    }
    return true;
  }

  @Override
  public ToDoDO newInstance()
  {
    return new ToDoDO();
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setTaskTree(final TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }
}
