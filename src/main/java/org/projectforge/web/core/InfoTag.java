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

/**
 * Displays a info icon with a tooltip.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class InfoTag extends JspTag
{
  private static final long serialVersionUID = 4276610851104596402L;

  private String key;

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskPathTag.class);

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer buf = new StringBuffer();
      htmlHelper.appendImageTag(pageContext, buf, htmlHelper.getInfoImage(), resolveMessage(key));
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
    key = null;
  }
  
  /**
   * The i18n key of the tooltipp to show.
   * @param key
   */
  public void setKey(String key)
  {
    this.key = key;
  }
}
