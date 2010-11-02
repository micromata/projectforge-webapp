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
import java.math.BigDecimal;

import javax.servlet.jsp.JspException;

import org.projectforge.core.CurrencyFormatter;


/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class CurrencyTag extends JspTag
{
  private static final long serialVersionUID = -2588954308319770460L;

  private BigDecimal value;

  private boolean suppressOutputOfZeroAmount = false;

  public CurrencyTag()
  {
    super();
  }

  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      String out = "";
      if (value != null) {
        if (suppressOutputOfZeroAmount == false || value.compareTo(BigDecimal.ZERO) != 0)
          out = CurrencyFormatter.format(value);
      }
      pageContext.getOut().write(out);
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return SKIP_BODY;
  }

  @Override
  public void release()
  {
    super.release();
    value = null;
  }

  public void setValue(BigDecimal value)
  {
    this.value = value;
  }

  /**
   * If true, then zero amounts will be suppressed in outputs. Default: "false".
   * @param suppressOutputOfZeroAmount
   */
  public void setSuppressOutputOfZeroAmount(boolean suppressOutputOfZeroAmount)
  {
    this.suppressOutputOfZeroAmount = suppressOutputOfZeroAmount;
  }
}
