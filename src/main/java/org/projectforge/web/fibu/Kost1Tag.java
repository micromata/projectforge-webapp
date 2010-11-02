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
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.web.core.JspTag;


public class Kost1Tag extends JspTag
{
  private static final long serialVersionUID = 5471210529647719418L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Kost1Tag.class);

  private Kost1Dao kost1Dao;
  
  private KostCache kostCache;

  private Integer kost1Id;

  private String select;

  private boolean nullable = false;

  /**
   * @see KostFormatter#format(Kost1DO)
   * @see org.projectforge.web.core.JspTag#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer sb = new StringBuffer();
      Kost1DO kost1 = null;
      boolean hasAccess = kost1Dao.hasSelectAccess(false);
      if (hasAccess == true) {
        try {
          kost1 = kostCache.getKost1(kost1Id);
        } catch (AccessException ex) {
          log.info(ex.getMessage());
          hasAccess = false;
        }
      }
      if (hasAccess == false) {
        String str = kost1Dao.getKostString(kost1Id);
        if (str != null) {
          sb.append(str);
        } else {
          appendNotVisible(sb);
        }
      } else if (kost1 != null) {
        sb.append(KostFormatter.format(kost1));
      } else if (nullable == false && StringUtils.isNotEmpty(select) == true) {
        sb.append(resolveMessage("pleaseChoose"));
      }
      sb.append(" ");
      if (hasAccess == true && StringUtils.isNotEmpty(select) == true) {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "select." + select);
        htmlHelper.appendImageTag(pageContext, sb, "/images/coins_add.png", resolveMessage("fibu.tooltip.selectKost1"));
        htmlHelper.appendAncorEndTag(sb);
        if (nullable == true && kost1Id != null && kost1Id != 0) {
          htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "unselect." + select);
          htmlHelper.appendImageTag(pageContext, sb, "/images/coins_delete.png", resolveMessage("fibu.tooltip.unselectKost1"));
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
    kost1Id = null;
    nullable = false;
    select = null;
  }

  public void setKost1Id(Integer kost1Id)
  {
    this.kost1Id = kost1Id;
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
   * The parameter to select (e. g. "kost1Id").
   * @param select
   */
  public void setSelect(String select)
  {
    this.select = select;
  }

  public void setKost1Dao(Kost1Dao kost1Dao)
  {
    this.kost1Dao = kost1Dao;
  }
  
  public void setKostCache(KostCache kostCache)
  {
    this.kostCache = kostCache;
  }
}
