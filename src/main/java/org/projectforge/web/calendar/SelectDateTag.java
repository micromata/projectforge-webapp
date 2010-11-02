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

package org.projectforge.web.calendar;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.projectforge.web.core.JspTag;


public class SelectDateTag extends JspTag
{
  private static final long serialVersionUID = -6673098509945872473L;

  private String select;

  private boolean period;
  
  private String type;

  private boolean nullable = false;

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskPathTag.class);

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer buf = new StringBuffer();
      buf.append(" ");
      htmlHelper.appendAncorOnClickSubmitEventStartTag(buf, "submitEvent", "select." + select + "");
      if ("birthday".equals(type) == true) {
        htmlHelper.appendImageTag(pageContext, buf, "/images/button_selectBirthday.png", resolveMessage("tooltip.selectBirthday"));
      } else {
        if (period == true) {
          htmlHelper.appendImageTag(pageContext, buf, "/images/button_selectDate.png", resolveMessage("tooltip.selectDateOrPeriod"));
        } else {
          htmlHelper.appendImageTag(pageContext, buf, "/images/button_selectDate.png", resolveMessage("tooltip.selectDate"));
        }
      }
      htmlHelper.appendAncorEndTag(buf);
      if (nullable == true) {
        buf.append("Unselect date not yet supported.");
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
    select = null;
  }

  /**
   * The parameter to select (e. g. "taskId", "userId", "birthday" or "parentTaskId").
   * @param select
   */
  public void setSelect(String select)
  {
    this.select = select;
  }
  
  /**
   * If set to true, the tooltipp 'tooltipp.selectDateOrPeriod' will be shown, otherwise 'tooltipp.selectDate'.
   * @param period
   */
  public void setPeriod(boolean period)
  {
    this.period = period;
  }
  
  /**
   * Type of date. Currently supported: "birthday"
   * @param type
   */
  public void setType(String type)
  {
    this.type = type;
  }
  
  /**
   * @param nullable If true, unselect will be supported.
   */
  public void setNullable(boolean nullable)
  {
    this.nullable = nullable;
  }
}
