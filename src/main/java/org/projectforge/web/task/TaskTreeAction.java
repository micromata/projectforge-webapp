/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.core.QueryFilter;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskStatus;
import org.projectforge.task.TaskTree;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseListAction;
import org.projectforge.web.core.BaseListActionBean;


/**
 * For browsing and selecting tasks.
 */
@Deprecated
@UrlBinding("/secure/task/TaskTree.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/task/taskTree.jsp")
@BaseListAction(flowSope = true)
public class TaskTreeAction extends BaseListActionBean<TaskFilter, TaskDao, TaskDO>
{
  private static final Logger log = Logger.getLogger(TaskTreeAction.class);

  private static final String SESSION_KEY_TASK_TREE_TABLE = "TaskTreeTable";

  private static final String USER_PREFS_KEY_OPEN_TASKS = "openTasks";

  private TaskTree taskTree;

  private boolean searchMode = false;

  private List<TaskDO> result;

  public boolean isSearchMode()
  {
    return searchMode;
  }

  /**
   * return always true.
   * @see org.projectforge.web.core.BaseListActionBean#isShowResultInstantly()
   */
  @Override
  protected boolean isShowResultInstantly()
  {
    return true;
  }

  @Override
  protected Resolution onExecute()
  {
    if (getPreselectedValue() != null) {
      Integer taskId = (Integer) getPreselectedValue(); // Put in scope in ExtendedActionBean.processEvent for selectTask.
      if (taskId != null) {
        // scope.remove("taskId");
        getTaskTreeTable().openNode(taskId.toString());
        // Do not open again, if the user closes this task manually.
        clearPreselectedValue();
      }
    }
    return null;
  }

  /** Unused: returns null. */
  @Override
  protected List<TaskDO> buildList()
  {
    // Do nothing
    return null;
  }

  /**
   * Should be used in tree table view. The current task tree will be returned for tree navigation.
   * @return
   */
  public List<TaskTreeTableNode> getTaskTreeList()
  {
    List<TaskTreeTableNode> list = getTaskTreeTable().getNodeList(actionFilter);
    return list;
  }

  /**
   * Should be used in search mode. Only a list of matching tasks will be returned (list view).
   * @return
   */
  public List<TaskDO> getList()
  {
    if (actionFilter != null && result == null) {
      QueryFilter queryFilter = new QueryFilter(actionFilter);
      Collection<TaskStatus> col = new ArrayList<TaskStatus>(4);
      if (actionFilter.isNotOpened() == true) {
        col.add(TaskStatus.N);
      }
      if (actionFilter.isOpened() == true) {
        col.add(TaskStatus.O);
      }
      if (actionFilter.isClosed() == true) {
        col.add(TaskStatus.C);
      }
      if (col.size() > 0) {
        queryFilter.add(Restrictions.in("status", col));
      } else {
        // Note: Result set should be empty, because every task should has one of the following status values.
        queryFilter.add(Restrictions.not(Restrictions.in("status", new TaskStatus[] { TaskStatus.N, TaskStatus.O, TaskStatus.C})));
      }
      queryFilter.addOrder(Order.asc("title"));
      if (log.isDebugEnabled() == true) {
        log.debug(actionFilter.toString());
      }
      result = baseDao.getList(queryFilter);
    }
    return result;
  }

  public Integer getRootNodeId()
  {
    return getRootNode().getId();
  }

  public TaskNode getRootNode()
  {
    return taskTree.getRootTaskNode();
  }

  @Override
  public Resolution search()
  {
    getLogger().debug("search");
    if (actionFilter.isSearchNotEmpty() == true) {
      searchMode = true;
    }
    return super.search();
  }

  @Override
  protected Resolution afterExecute()
  {
    if (StringHelper.isIn(eventKey, new String[] { "open", "close", "explore", "implore"}) == true) {
      Integer id = NumberHelper.parseInteger(selectedValue);
      getTaskTreeTable().setOpenedStatusOfNode(eventKey, id);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private TaskTreeTable getTaskTreeTable()
  {
    TaskTreeTable taskTreeTable = (TaskTreeTable) getContext().getEntry(SESSION_KEY_TASK_TREE_TABLE);
    if (taskTreeTable == null) {
      taskTreeTable = new TaskTreeTable(baseDao.getTaskTree());
      getContext().putEntry(SESSION_KEY_TASK_TREE_TABLE, taskTreeTable, false);
      Set<Serializable> openTaskNodes = (Set<Serializable>) getContext().getEntry(USER_PREFS_KEY_OPEN_TASKS);
      if (openTaskNodes != null) {
        taskTreeTable.setOpenNodes(openTaskNodes);
      }
      getContext().putEntry(USER_PREFS_KEY_OPEN_TASKS, taskTreeTable.getOpenNodes(), true);
    }
    return taskTreeTable;
  }

  public void setTaskDao(TaskDao taskDao)
  {
    this.baseDao = taskDao;
  }

  /**
   * The root node will only be shown in select mode and for admin users.
   */
  public boolean isShowRootNode()
  {
    return (isSelectMode() == true && accessChecker.isUserMemberOfAdminGroup())
        || accessChecker.isUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP);
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  @Override
  protected TaskFilter createFilterInstance()
  {
    return new TaskFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
