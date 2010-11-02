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

package org.projectforge.web.common;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.projectforge.web.core.JspTag;


/**
 * For displaying boolean values. String.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class BooleanTag extends JspTag
{
  private static final long serialVersionUID = 6568916969039178448L;

  private boolean value;

  private boolean showFalse;

  private String displayFormat;

  public BooleanTag()
  {
    super();
  }

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer buf = new StringBuffer();
      if (value == true) {
        if (isTicker() == true) {
          htmlHelper.appendImageTag(pageContext, buf, "/images/accept.png");
        } else {
          buf.append('X');
        }
      } else if (showFalse == true) {
        if (isTicker() == true) {
          htmlHelper.appendImageTag(pageContext, buf, "/images/deny.png");
        } else {
          buf.append("-");
        }
      }
      pageContext.getOut().write(buf.toString());
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return SKIP_BODY;
  }

  private boolean isTicker()
  {
    return "ticker".equals(displayFormat);
  }

  @Override
  public void release()
  {
    super.release();
    value = false;
    showFalse = false;
    displayFormat = "normal";
  }

  public void setValue(boolean value)
  {
    this.value = value;
  }

  /**
   * If true then the false value will be displayed, otherwise false results in an empty string.
   * @param showFalse
   */
  public void setShowFalse(boolean showFalse)
  {
    this.showFalse = showFalse;
  }

  /**
   * Supported values:<br/> ticker - Ticker will be used as image for displaying true values, red minus for displaying false values.<br/>
   * Default (if no value given): Character X for true and - for false.
   * @param displayFormat
   */
  public void setDisplayFormat(String displayFormat)
  {
    if ("ticker".equals(displayFormat) == true) {
      this.displayFormat = displayFormat;
    } else {
      throw new UnsupportedOperationException("DisplayFormat '" + displayFormat + "' not supported.");
    }
  }
}
