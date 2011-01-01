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

package org.projectforge.task;

import org.apache.commons.lang.Validate;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.web.task.TaskTreeTableNode;
import org.projectforge.web.tree.TreeTableFilter;
import org.projectforge.web.tree.TreeTableNode;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("TaskFilter")
public class TaskFilter extends BaseSearchFilter implements TreeTableFilter<TreeTableNode>
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

  @XStreamAsAttribute
  private boolean ajaxSupport = true;

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

  public void setClosed(boolean closed)
  {
    this.closed = closed;
  }

  public boolean isDeleted()
  {
    return deleted;
  }

  public void setDeleted(boolean deleted)
  {
    this.deleted = deleted;
  }

  public boolean isNotOpened()
  {
    return notOpened;
  }

  public void setNotOpened(boolean notOpened)
  {
    this.notOpened = notOpened;
  }

  public boolean isOpened()
  {
    return opened;
  }

  public void setOpened(boolean opened)
  {
    this.opened = opened;
  }

  /**
   * If true then the explore icons will be implemented as ajax calls.
   */
  public boolean isAjaxSupport()
  {
    return ajaxSupport;
  }

  public void setAjaxSupport(boolean ajaxSupport)
  {
    this.ajaxSupport = ajaxSupport;
  }

  @Override
  public void reset()
  {
    super.reset();
    notOpened = opened = true;
    closed = deleted = false;
    searchString = "";
    ajaxSupport = true;
  }

  /**
   * Needed by TaskTreeTable to show and hide nodes.
   * @see org.projectforge.web.tree.TreeTableFilter#match(org.projectforge.web.tree.TreeTableNode)
   */
  public boolean match(TreeTableNode n)
  {
    TaskTreeTableNode node = (TaskTreeTableNode) n;
    Validate.notNull(node.getTaskNode());
    Validate.notNull(node.getTaskNode().getTask());
    TaskDO task = node.getTaskNode().getTask();
    if (isDeleted() == false && task.isDeleted() == true) {
      return false;
    }
    if (task.getStatus() == TaskStatus.N) {
      return isNotOpened();
    } else if (node.getStatus() == TaskStatus.O) {
      return isOpened();
    } else if (node.getStatus() == TaskStatus.C) {
      return isClosed();
    }
    return node.isDeleted() == isDeleted();
  }
}
