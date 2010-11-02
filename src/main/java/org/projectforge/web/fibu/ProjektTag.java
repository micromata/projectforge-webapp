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

package org.projectforge.web.fibu;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.projectforge.access.AccessException;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.web.core.JspTag;


public class ProjektTag extends JspTag
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjektTag.class);

  private static final long serialVersionUID = -7004078818499153691L;

  private ProjektDao projektDao;

  private Integer projektId;

  private String select;

  private boolean onlyNumber = false;

  private boolean nullable = false;

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer sb = new StringBuffer();
      ProjektDO projekt = null;
      boolean hasAccess = projektDao.hasSelectAccess(false);
      if (hasAccess == true) {
        try {
          projekt = projektDao.getById(projektId);
        } catch (AccessException ex) {
          log.info(ex.getMessage());
          hasAccess = true;
        }
      }
      if (hasAccess == false) {
        if (onlyNumber == true) {
          projekt = projektDao.internalGetById(projektId);
          sb.append(KostFormatter.format(projekt));
          projekt = null; // Security paranoia.
        } else {
          appendNotVisible(sb);
        }
      } else if (projekt != null) {
        if (onlyNumber == true) {
          sb.append(KostFormatter.format(projekt));
        } else {
          sb.append(KostFormatter.formatProjekt(projekt));
        }
      } else if (nullable == false) {
        sb.append(resolveMessage("fibu.projekt.pleaseSelectProjekt"));
      }
      if (hasAccess == true && StringUtils.isNotEmpty(select)) {
        sb.append(" ");
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "select." + select);
        htmlHelper.appendImageTag(pageContext, sb, "/images/button_selectProjekt.png", resolveMessage("fibu.tooltip.selectProjekt"));
        htmlHelper.appendAncorEndTag(sb);
        if (nullable == true && projektId != null && projektId != 0) {
          htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "unselect." + select);
          htmlHelper.appendImageTag(pageContext, sb, "/images/button_unselectProjekt.png", resolveMessage("fibu.tooltip.unselectProjekt"));
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
    projektId = null;
    nullable = false;
    select = null;
  }

  public void setProjektId(Integer projektId)
  {
    this.projektId = projektId;
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

  /**
   * If true, then only the number of the project x.xxx.xx will be shown.
   * @param onlyNumber
   */
  public void setOnlyNumber(boolean onlyNumber)
  {
    this.onlyNumber = onlyNumber;
  }

  public void setProjektDao(ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }
}
