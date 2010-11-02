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
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.web.core.JspTag;


public class KundeTag extends JspTag
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(KundeTag.class);

  private static final long serialVersionUID = 1071711439479620917L;

  private KundeDao kundeDao;

  private Integer kundeId;

  private String select;

  private boolean nullable = false;

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer sb = new StringBuffer();
      KundeDO kunde = null;
      boolean hasAccess = kundeDao.hasSelectAccess(false);
      if (hasAccess == true) {
        try {
          kunde = kundeDao.getById(kundeId);
        } catch (AccessException ex) {
          log.info(ex.getMessage());
          hasAccess = false;
        }
      }
      if (hasAccess == false) {
        appendNotVisible(sb);
      } else if (kunde != null) {
        sb.append(KostFormatter.formatKunde(kunde));
      } else if (nullable == false) {
        sb.append(resolveMessage("fibu.kunde.pleaseSelectKunde"));
      }
      sb.append(" ");
      if (hasAccess == true && StringUtils.isNotEmpty(select)) {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "select." + select);
        htmlHelper.appendImageTag(pageContext, sb, "/images/button_selectCustomer.png", resolveMessage("fibu.tooltip.selectKunde"));
        htmlHelper.appendAncorEndTag(sb);
        if (nullable == true && kundeId != null && kundeId != 0) {
          htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "unselect." + select);
          htmlHelper.appendImageTag(pageContext, sb, "/images/button_unselectCustomer.png", resolveMessage("fibu.tooltip.unselectKunde"));
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
    kundeId = null;
    nullable = false;
    select = null;
  }

  public void setKundeId(Integer kundeId)
  {
    this.kundeId = kundeId;
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

  public void setKundeDao(KundeDao kundeDao)
  {
    this.kundeDao = kundeDao;
  }
}
