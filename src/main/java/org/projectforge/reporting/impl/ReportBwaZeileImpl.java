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

package org.projectforge.reporting.impl;

import java.math.BigDecimal;

import org.projectforge.core.Priority;
import org.projectforge.fibu.kost.BwaZeile;
import org.projectforge.reporting.ReportBwaZeile;


public class ReportBwaZeileImpl implements ReportBwaZeile
{
  private BwaZeile bwaZeile;

  public ReportBwaZeileImpl(BwaZeile bwaZeile)
  {
    this.bwaZeile = bwaZeile;
  }

  public String getBezeichnung()
  {
    return bwaZeile.getBezeichnung();
  }

  public int getIndent()
  {
    return bwaZeile.getIndent();
  }

  public Priority getPriority()
  {
    return bwaZeile.getPriority();
  }

  public int getZeile()
  {
    return bwaZeile.getZeile();
  }
  
  public BigDecimal getBwaWert()
  {
    return bwaZeile.getBwaWert();
  }
}
