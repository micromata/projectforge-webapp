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
 * Displays an image with a tooltip.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ImageTag extends JspTag
{
  private static final long serialVersionUID = 9194109054063085469L;

  private String src;

  private String tooltip;
  
  private String tooltipText;

  private String width;

  private String height;

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer buf = new StringBuffer();
      String s = null;
      if (StringUtils.isNotEmpty(tooltip) == true) {
        s = resolveMessage(tooltip);
      } else if (StringUtils.isNotEmpty(tooltipText) == true) {
        s = tooltipText;
      }
      htmlHelper.appendImageTag(pageContext, buf, src, width, height, s);
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
    src = tooltip = null;
  }

  /**
   * Image source path (will be build a la c:url automatically).
   * @param src
   */
  public void setSrc(String src)
  {
    this.src = src;
  }

  /**
   * The i18n key of the tool tip to show.
   * 
   * @param tooltip
   */
  public void setTooltip(String tooltip)
  {
    this.tooltip = tooltip;
  }
  
  /**
   * The tool tip text (not i18n key). Will only used, if only if tooltip is not given.
   * @param tooltipText
   */
  public void setTooltipText(String tooltipText)
  {
    this.tooltipText = tooltipText;
  }

  public void setHeight(String height)
  {
    this.height = height;
  }

  public void setWidth(String width)
  {
    this.width = width;
  }
}
