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
 * Displays a help icon with a tooltip.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class HelpTag extends JspTag
{
  private static final long serialVersionUID = 4276610851104596402L;

  private static final String LUCENE_HELP_LINK = "/secure/doc/Handbuch.html#label_volltextsuche";

  public static final String I18N_KEY_LUCENE_LINK = "tooltip.lucene.link";

  private String key;

  private String param;

  private String image;

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskPathTag.class);

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer buf = new StringBuffer();
      String message;
      if (I18N_KEY_LUCENE_LINK.equals(key) == true) {
        // Lucene help image:
        buf.append("<a href=\"" + LUCENE_HELP_LINK + "\" target=\"_blank\">");
        message = resolveMessage(I18N_KEY_LUCENE_LINK);
      } else {
        if (StringUtils.isEmpty(param) == true) {
          message = resolveMessage(key);
        } else {
          message = resolveMessage(key, param);
        }
      }
      if (image != null) {
        htmlHelper.appendImageTag(pageContext, buf, "/images/" + image, message);
      } else {
        htmlHelper.appendImageTag(pageContext, buf, "/images/help.png", message);
      }
      if (I18N_KEY_LUCENE_LINK.equals(key) == true) {
        // Lucene help image:
        buf.append("</a>");
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
    key = null;
  }

  /**
   * The i18n key of the tool-tip to show. If "tooltip.lucene.link" then a link to the Lucene query syntax know who will be generated with a
   * short tool tip.
   * @param key
   */
  public void setKey(String key)
  {
    this.key = key;
  }

  /**
   * If set then the given image will be used, otherwise default image. Image inclusive path should be relative to src/main/webapp/images.
   * @param image
   */
  public void setImage(String image)
  {
    this.image = image;
  }

  /**
   * The param of the message if needed.
   * @param param
   */
  public void setParam(String param)
  {
    this.param = param;
  }
}
