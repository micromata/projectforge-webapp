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

package org.projectforge.web.user;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.core.JspTag;


@Deprecated
public class UserTag extends JspTag
{
  private static final long serialVersionUID = -6673098509945872473L;

  private UserFormatter userFormatter;

  private Integer userId;

  private String select;

  private boolean nullable = false;

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskPathTag.class);

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer sb = new StringBuffer();
      userFormatter.appendFormattedUser(sb, userId);
      if (StringUtils.isNotEmpty(select)) {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "select." + select);
        htmlHelper.appendImageTag(pageContext, sb, "/images/button_selectUser.png", resolveMessage("tooltip.selectUser"));
        htmlHelper.appendAncorEndTag(sb);
        if (nullable == true) {
          htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "unselect." + select);
          htmlHelper.appendImageTag(pageContext, sb, "/images/button_unselectUser.png", resolveMessage("tooltip.unselectUser"));
          htmlHelper.appendAncorEndTag(sb);
        }
        if (userId == null || userId.equals(PFUserContext.getUser().getId()) == false) {
          // Show "select me" if current logged in user is not already selecte.
          htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "selectDefault." + select);
          htmlHelper.appendImageTag(pageContext, sb, "/images/button_selectMe.png", resolveMessage("tooltip.selectMe"));
          htmlHelper.appendAncorEndTag(sb);
        }
      }
      pageContext.getOut().write(sb.toString());
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return SKIP_BODY;
  }

  @Override
  public void release()
  {
    super.release();
    userId = null;
    nullable = false;
    select = null;
  }

  public void setUserId(Integer userId)
  {
    this.userId = userId;
  }

  /**
   * If set to true, an unset button will be shown for deselecting user.
   * @param nullable
   */
  public void setNullable(boolean nullable)
  {
    this.nullable = nullable;
  }

  public void setUserFormatter(UserFormatter userFormatter)
  {
    this.userFormatter = userFormatter;
  }

  /**
   * The parameter to select (e. g. "taskId", "userId", "birthday" or "parentTaskId").
   * @param select
   */
  public void setSelect(String select)
  {
    this.select = select;
  }
}
