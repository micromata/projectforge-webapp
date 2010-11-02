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
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.core.JspTag;


public class Kost2Tag extends JspTag
{
  private static final long serialVersionUID = 5471210529647719418L;

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Kost2Tag.class);

  private Kost2Dao kost2Dao;

  private KostCache kostCache;

  private Integer kost2Id;

  private String select;

  private boolean nullable = false;

  private boolean ignoreAccess = false;

  /**
   * @see KostFormatter#format(Kost2DO)
   * @see org.projectforge.web.core.JspTag#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer sb = new StringBuffer();
      String title = null;
      String tooltip = null;
      boolean hasAccess = kost2Dao.hasSelectAccess(false);
      if (kost2Id != null && kost2Id > 0) {
        Kost2DO kost2 = kostCache.getKost2(kost2Id);
        hasAccess = kost2Dao.hasSelectAccess(kost2, false);
        if (hasAccess == false) {
          if (kost2Id != null && kost2 != null) {
            if (ignoreAccess == true) {
              kost2.setDescription("");
              title = KostFormatter.format(kost2);
              // User has no access, show only the name of the kost2Art:
              tooltip = kost2.getKost2Art().getName();
            } else {
              // User has no access and access is not ignored, show nothing:
              title = getNotVisibleString();
            }
          }
        } else if (kost2 != null) {
          title = KostFormatter.format(kost2);
          tooltip = HtmlHelper.escapeXml(kost2.getToolTip());
        } else if (nullable == false) {
          title = resolveMessage("pleaseChoose");
        }
        if (tooltip != null) {
          sb.append("<span title=\"").append(tooltip).append("\">").append(title).append("</span>");
        } else {
          sb.append(title);
        }
        sb.append(" ");
      }
      if (hasAccess == true && StringUtils.isNotEmpty(select)) {
        htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "select." + select);
        htmlHelper.appendImageTag(pageContext, sb, "/images/coins_add.png", resolveMessage("fibu.tooltip.selectKost2"));
        htmlHelper.appendAncorEndTag(sb);
        if (nullable == true && kost2Id != null && kost2Id != 0) {
          htmlHelper.appendAncorOnClickSubmitEventStartTag(sb, "submitEvent", "unselect." + select);
          htmlHelper.appendImageTag(pageContext, sb, "/images/coins_delete.png", resolveMessage("fibu.tooltip.unselectKost2"));
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
    kost2Id = null;
    nullable = false;
    select = null;
  }

  public void setKost2Id(Integer kost2Id)
  {
    this.kost2Id = kost2Id;
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
   * If true, then the user can see the number of the kost2 object (not more).
   */
  public void setIgnoreAccess(boolean ignoreAccess)
  {
    this.ignoreAccess = ignoreAccess;
  }

  /**
   * The parameter to select (e. g. "kost2Id").
   * @param select
   */
  public void setSelect(String select)
  {
    this.select = select;
  }

  public void setKost2Dao(Kost2Dao kost2Dao)
  {
    this.kost2Dao = kost2Dao;
  }

  public void setKostCache(KostCache kostCache)
  {
    this.kostCache = kostCache;
  }
}
