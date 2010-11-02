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
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.core.JspTag;


public class GroupTag extends JspTag
{
  private static final long serialVersionUID = -8728087459303362961L;

  private UserGroupCache userGroupCache;

  private Integer groupId;

  private String select;

  private boolean nullable = false;

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer sb = new StringBuffer();
      String name = userGroupCache.getGroupname(groupId);
      if (name != null) {
        sb.append(name);
      } else if (nullable == false) {
        sb.append(resolveMessage("group.pleaseSelectGroup"));
      }
      sb.append(" ");
      if (StringUtils.isNotEmpty(select)) {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "select." + select);
        htmlHelper.appendImageTag(pageContext, sb, "/images/button_selectGroup.png", resolveMessage("tooltip.selectGroup"));
        htmlHelper.appendAncorEndTag(sb);
        if (nullable == true) {
          htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "unselect." + select);
          htmlHelper.appendImageTag(pageContext, sb, "/images/button_unselectGroup.png", resolveMessage("tooltip.unselectGroup"));
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
    groupId = null;
    nullable = false;
    select = null;
  }

  public void setGroupId(Integer groupId)
  {
    this.groupId = groupId;
  }

  /**
   * If set to true, an unset button will be shown for deselecting group.
   * @param nullable
   */
  public void setNullable(boolean nullable)
  {
    this.nullable = nullable;
  }

  /**
   * The parameter to select (e. g. "groupId").
   * @param select
   */
  public void setSelect(String select)
  {
    this.select = select;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }
}
