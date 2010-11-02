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

package org.projectforge.web.core;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;

/**
 * Creates a javascript submit anchor: &lt;a onclick="javascript:submitSelectedEvent('selectWeek', '${week.days[0]}')" href="#"&gt; or &lt;a
 * onclick="javascript:submitEvent('selectMonth')" href="#"&gt;
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SubmitTag extends JspTag
{
  private static final long serialVersionUID = 2411753487425131651L;

  private String event;

  private String select;
  
  private String button;

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer sb = new StringBuffer();
      if (StringUtils.isNotEmpty(select) == true) {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitSelectedEvent", event, select);
      } else if (StringUtils.isNotEmpty(button) == true) {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitButton", button);
      } else {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", event);
      }
      pageContext.getOut().write(sb.toString());
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doEndTag() throws JspException
  {
    try {
      pageContext.getOut().write("</a>");
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return EVAL_PAGE;
  }

  @Override
  public void release()
  {
    super.release();
    event = select = null;
  }

  /**
   * Produce anchor with onclick method: submitEvent(event).
   * @param event
   */
  public void setEvent(String event)
  {
    this.event = event;
  }

  /**
   * Produce anchor with onclick method: submitSelectedEvent(event, select).
   * @param select
   */
  public void setSelect(String select)
  {
    this.select = select;
  }

  /**
   * Produce anchor with onclick method: submitButton(button).
   * @param button
   */
  public void setButton(String button)
  {
    this.button = button;
  }
}
