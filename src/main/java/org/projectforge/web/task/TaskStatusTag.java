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

import org.projectforge.task.TaskStatus;
import org.projectforge.web.core.JspTag;


public class TaskStatusTag extends JspTag
{
  private static final long serialVersionUID = -6673098509945872473L;

  private TaskFormatter taskFormatter;
  
  private String status;

  public int doStartTag() throws JspException
  {
    init();
    try {
      String s = taskFormatter.appendFormattedTaskStatus(pageContext, TaskStatus.getTaskStatus(status));
      pageContext.getOut().write(s);
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return SKIP_BODY;
  }
  
  public void setStatus(String status)
  {
    this.status = status;
  }

  public void setTaskFormatter(TaskFormatter taskFormatter)
  {
    this.taskFormatter = taskFormatter;
  }
}
