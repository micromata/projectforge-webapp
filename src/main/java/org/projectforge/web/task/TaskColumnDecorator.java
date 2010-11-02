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

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;
import org.projectforge.core.Priority;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskStatus;
import org.projectforge.task.TaskTree;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.core.PriorityFormatter;


/**
 * In result lists this decorate is used for formatting tasks in columns.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TaskColumnDecorator implements DisplaytagColumnDecorator
{
  private TaskFormatter taskFormatter;

  private PriorityFormatter priorityFormatter;

  private HtmlHelper htmlHelper;

  private TaskTree taskTree;

  public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException
  {
    if (columnValue == null) {
      // For example if Priority is null;
      return "";
    }
    TaskDO task = null;
    if (columnValue instanceof TaskDO) {
      task = (TaskDO) columnValue;
    } else if (columnValue instanceof Integer) {
      task = taskTree.getTaskById((Integer) columnValue);
    } else if (columnValue instanceof Priority) {
      return decoratePriority((Priority) columnValue, pageContext, media);
    } else if (columnValue instanceof TaskStatus) {
      return decorateTaskStatus((TaskStatus) columnValue, pageContext, media);
    }
    if (task == null) {
      return "???";
    }
    StringBuffer buf = new StringBuffer();
    boolean enableLink = false, showPathAsTooltip = true, lineThroughDeletedTask = true;
    ServletRequest request = (ServletRequest) pageContext.getRequest();
    String selectedValue = request.getParameter("selectedValue");
    if (selectedValue != null) {
      // SelectForm.
      enableLink = false;
      htmlHelper.appendAncorOnClickSubmitEventStartTag(buf, "submitSelectedEvent", "select", String.valueOf(task.getId()));
    }
    taskFormatter.appendFormattedTask(buf, pageContext, task, enableLink, showPathAsTooltip, lineThroughDeletedTask);
    if (selectedValue != null) {
      // SelectForm.
      htmlHelper.appendAncorEndTag(buf);
    }
    return buf.toString();
  }

  public Object decorateTaskStatus(TaskStatus status, PageContext pageContext, MediaTypeEnum media) throws DecoratorException
  {
    return taskFormatter.appendFormattedTaskStatus(pageContext, status);
  }

  public Object decoratePriority(Priority priority, PageContext pageContext, MediaTypeEnum media) throws DecoratorException
  {
    return priorityFormatter.format(pageContext, priority);
  }

  public void setTaskFormatter(TaskFormatter taskFormatter)
  {
    this.taskFormatter = taskFormatter;
  }

  public void setPriorityFormatter(PriorityFormatter priorityFormatter)
  {
    this.priorityFormatter = priorityFormatter;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setHtmlHelper(HtmlHelper htmlHelper)
  {
    this.htmlHelper = htmlHelper;
  }
}
