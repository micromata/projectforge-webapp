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

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.web.core.JspTag;

public class TaskTag extends JspTag
{
  private static final long serialVersionUID = -6673098509945872473L;

  private TaskFormatter taskFormatter;

  private TaskTree taskTree;

  private Integer taskId;

  private String select;

  private boolean showPath = false;

  private boolean nullable = false;

  private boolean enableLinks = false;

  private boolean lineThroughDeletedTasks = true;

  private boolean showPathAsTooltip = true;

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskPathTag.class);

  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer buf = new StringBuffer();
      if (showPath == true) {
        String taskPath = taskFormatter.getTaskPath(pageContext, taskId, enableLinks, lineThroughDeletedTasks);
        if (taskPath != null) {
          buf.append(taskPath);
        } else if (nullable == false) {
          buf.append(resolveMessage("task.path.pleaseSelectTask"));
        }
        buf.append(" ");
      } else {
        TaskDO task = taskTree.getTaskById(taskId);
        taskFormatter.appendFormattedTask(buf, pageContext, task, enableLinks, showPathAsTooltip, lineThroughDeletedTasks);
      }
      if (StringUtils.isNotEmpty(select)) {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(buf, "submitEvent", "select." + select);
        htmlHelper.appendImageTag(pageContext, buf, "/images/button_selectTask.png", resolveMessage("tooltip.selectTask"));
        htmlHelper.appendAncorEndTag(buf);
      }
      if (nullable == true) {
        buf.append(" ");
        htmlHelper.appendAncorOnClickSubmitEventStartTag(buf, "submitEvent", "unselect." + select);
        htmlHelper.appendImageTag(pageContext, buf, "/images/button_unselectTask.png", resolveMessage("tooltip.unselectTask"));
        htmlHelper.appendAncorEndTag(buf);
      }
      pageContext.getOut().write(buf.toString());
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return SKIP_BODY;
  }

  @Override
  public void release()
  {
    super.release();
    taskId = null;
    nullable = false;
    select = null;
  }

  public void setTaskId(Integer taskId)
  {
    this.taskId = taskId;
  }

  /**
   * If true, the path of the task will be shown.
   * @param showPath (default = false)
   */
  public void setShowPath(boolean showPath)
  {
    this.showPath = showPath;
  }

  /**
   * If true, then the links to the TaskEdit.action for the task titles will be enabled.
   * @param enableLinks (default = true)
   */
  public void setEnableLinks(boolean enableLinks)
  {
    this.enableLinks = enableLinks;
  }

  /**
   * If true, the task titles of deleted tasks will be decorated with a line through.
   * @param lineThroughDeletedTasks (default = true)
   */
  public void setLineThroughDeletedTasks(boolean lineThroughDeletedTasks)
  {
    this.lineThroughDeletedTasks = lineThroughDeletedTasks;
  }

  /**
   * If true, the task path of the task will be shown via tooltip and an info icon. This variable has no effect, if showPath is true.
   * @param lineThroughDeletedTasks (default = true)
   */
  public void setShowPathAsTooltip(boolean showPathAsTooltip)
  {
    this.showPathAsTooltip = showPathAsTooltip;
  }

  /**
   * If set to true, an unset button will be shown for deselecting user.
   * @param nullable (default = false)
   */
  public void setNullable(boolean nullable)
  {
    this.nullable = nullable;
  }

  /**
   * The parameter to select (e. g. "taskId", "userId", "birthday" or "parentTaskId") via browse button.
   * @param select (default = false)
   */
  public void setSelect(String select)
  {
    this.select = select;
  }

  public void setTaskFormatter(TaskFormatter taskFormatter)
  {
    this.taskFormatter = taskFormatter;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }
}
