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
import org.projectforge.web.core.JspTag;


/**
 * For displaying project and customer. Both are optional.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ProjektKundeTag extends JspTag
{
  private static final long serialVersionUID = 361571726158880691L;

  private String kundeText;

  private String kundeName;

  private String projektName;

  private String projektKundeName;

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      StringBuffer sb = new StringBuffer();
      String s = null;
      if (StringUtils.isNotBlank(kundeName) == true) {
        s = kundeName;
      }
      if (StringUtils.isNotBlank(kundeText) == true) {
        if (s == null) {
        s = kundeText;
        } else {
          s = kundeText + "; " + s;
        }
      }
      if (StringUtils.isNotBlank(s) == true) {
        sb.append(s);
      }
      if (StringUtils.isNotBlank(projektName) == true && StringUtils.equals(s, projektKundeName) == false) {
        if (StringUtils.isNotBlank(s) == true) {
          sb.append("; ");
        }
        sb.append(projektKundeName);
      }
      if (StringUtils.isNotBlank(projektName) == true) {
        sb.append(" - ");
        sb.append(projektName);
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
    kundeText = kundeName = projektName = projektKundeName = null;
  }

  /**
   * Alternativ zum Kunde, falls dieser z. B. noch nicht angelegt wurde. Wird nur verwendet, falls von projekt.kunde.name abweichend.
   * @param kundeText
   */
  public void setKundeText(String kundeText)
  {
    this.kundeText = kundeText;
  }

  /**
   * Wenn Kunde schon vorhanden ist, muss nicht über kundeId er nochmal geladen werden. Wird nur verwendet, falls von projekt.kunde.name
   * abweichend.
   * 
   * @param kundeName
   */
  public void setKundeName(String kundeName)
  {
    this.kundeName = kundeName;
  }

  /**
   * Wenn Projekt schon vorhanden ist, muss nicht über projektId nochmal geladen werden. Wird nur verwendet, falls projektId nicht gesetzt
   * ist.
   * 
   * @param projektName
   */
  public void setProjektName(String projektName)
  {
    this.projektName = projektName;
  }

  /**
   * Wenn Projekt schon vorhanden ist, muss nicht über projektId nochmal geladen werden. Wird nur verwendet, falls projektId nicht gesetzt
   * ist.
   * 
   * @param projektKundeName
   */
  public void setProjektKundeName(String projektKundeName)
  {
    this.projektKundeName = projektKundeName;
  }
}
