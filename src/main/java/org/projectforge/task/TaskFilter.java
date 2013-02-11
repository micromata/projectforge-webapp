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

package org.projectforge.task;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("TaskFilter")
public class TaskFilter extends BaseSearchFilter
{
  // private static final Logger log = Logger.getLogger(TimesheetFilter.class);

  private static final long serialVersionUID = 5783675334284869722L;

  @XStreamAsAttribute
  private boolean notOpened = true;

  @XStreamAsAttribute
  private boolean opened = true;

  @XStreamAsAttribute
  private boolean closed;

  @XStreamAsAttribute
  private boolean deleted;

  /**
   * Used by match filter for avoiding multiple traversing of the tree. Should be empty before building a task node list!
   */
  private transient HashMap<Integer, Boolean> taskVisibility;

  /**
   * Used by match filter for storing those tasks which matches the search string. Should be empty before building a task node list!
   */
  private transient HashSet<Integer> tasksMatched;

  public TaskFilter()
  {
  }

  public TaskFilter(final BaseSearchFilter filter)
  {
    super(filter);
  }

  public boolean isClosed()
  {
    return closed;
  }

  public void setClosed(final boolean closed)
  {
    this.closed = closed;
  }

  @Override
  public boolean isDeleted()
  {
    return deleted;
  }

  @Override
  public void setDeleted(final boolean deleted)
  {
    this.deleted = deleted;
  }

  public boolean isNotOpened()
  {
    return notOpened;
  }

  public void setNotOpened(final boolean notOpened)
  {
    this.notOpened = notOpened;
  }

  public boolean isOpened()
  {
    return opened;
  }

  public void setOpened(final boolean opened)
  {
    this.opened = opened;
  }

  @Override
  public void reset()
  {
    super.reset();
    notOpened = opened = true;
    closed = deleted = false;
    searchString = "";
  }

  public void resetMatch()
  {
    taskVisibility = new HashMap<Integer, Boolean>();
    tasksMatched = new HashSet<Integer>();
  }

  /**
   * Needed by TaskTreeTable to show and hide nodes.<br/>
   * Don't forget to call resetMatch before!
   * @see org.projectforge.web.tree.TreeTableFilter#match(org.projectforge.web.tree.TreeTableNode)
   */
  public boolean match(final TaskNode node)
  {
    Validate.notNull(node);
    Validate.notNull(node.getTask());
    if (taskVisibility == null) {
      resetMatch();
    }
    final TaskDO task = node.getTask();
    if (StringUtils.isBlank(this.searchString) == true) {
      return isVisibleByStatus(node, task) || node.isRootNode() == true;
    } else {
      if (isVisibleBySearchString(node, task) == true) {
        return isVisibleByStatus(node, task) || node.isRootNode() == true;
      } else {
        if (node.getParent() != null && node.getParent().isRootNode() == false && isAncestorVisibleBySearchString(node.getParent()) == true) {
          // Otherwise the node is only visible by his status if the parent node is visible:
          return isVisibleByStatus(node, task);
        } else {
          return false;
        }
      }
    }
  }

  private boolean isAncestorVisibleBySearchString(final TaskNode node)
  {
    if (tasksMatched.contains(node.getId()) == true) {
      return true;
    } else if (node.getParent() != null) {
      return isAncestorVisibleBySearchString(node.getParent());
    }
    return false;
  }

  /**
   * @param node
   * @param task
   * @return true if the search string matches at least one field of the task of if this methods returns true for any descendant.
   */
  private boolean isVisibleBySearchString(final TaskNode node, final TaskDO task)
  {
    final Boolean cachedVisibility = taskVisibility.get(task.getId());
    if (cachedVisibility != null) {
      return cachedVisibility;
    }
    if (isVisibleByStatus(node, task) == false && node.isRootNode() == false) {
      taskVisibility.put(task.getId(), false);
      return false;
    }
    final PFUserDO user = Registry.instance().getUserGroupCache().getUser(task.getResponsibleUserId());
    final String username = user != null ? user.getFullname() + " " + user.getUsername() : null;
    if (StringUtils.containsIgnoreCase(task.getTitle(), this.searchString) == true
        || StringUtils.containsIgnoreCase(task.getReference(), this.searchString) == true
        || StringUtils.containsIgnoreCase(task.getDescription(), this.searchString) == true
        || StringUtils.containsIgnoreCase(task.getShortDisplayName(), this.searchString) == true
        || StringUtils.containsIgnoreCase(username, this.searchString) == true
        || StringUtils.containsIgnoreCase(task.getWorkpackageCode(), this.searchString) == true) {
      taskVisibility.put(task.getId(), true);
      tasksMatched.add(task.getId());
      return true;
    } else if (node.hasChilds() == true && node.isRootNode() == false) {
      for (final TaskNode childNode : node.getChilds()) {
        final TaskDO childTask = childNode.getTask();
        if (isVisibleBySearchString(childNode, childTask) == true) {
          taskVisibility.put(childTask.getId(), true);
          return true;
        }
      }
    }
    taskVisibility.put(task.getId(), false);
    return false;
  }

  private boolean isVisibleByStatus(final TaskNode node, final TaskDO task)
  {
    if (isDeleted() == false && task.isDeleted() == true) {
      return false;
    }
    if (task.getStatus() == TaskStatus.N) {
      return isNotOpened();
    } else if (task.getStatus() == TaskStatus.O) {
      return isOpened();
    } else if (task.getStatus() == TaskStatus.C) {
      return isClosed();
    }
    return node.isDeleted() == isDeleted();
  }
}
