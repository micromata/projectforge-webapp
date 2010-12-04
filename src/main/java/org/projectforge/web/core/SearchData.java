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

import java.io.Serializable;
import java.util.Date;

import org.projectforge.user.PFUserDO;

public class SearchData implements Serializable
{
  private static final long serialVersionUID = 2056162179686892853L;

  private Date modifiedStartDate, modifiedStopDate;

  private int lastDays;

  private String searchString;

  private PFUserDO modifiedByUser;

  private String area;

  public Date getModifiedStartDate()
  {
    return modifiedStartDate;
  }

  public void setModifiedStartDate(Date modifiedStartDate)
  {
    this.modifiedStartDate = modifiedStartDate;
  }

  public Date getModifiedStopDate()
  {
    return modifiedStopDate;
  }

  public void setModifiedStopDate(Date modifiedStopDate)
  {
    this.modifiedStopDate = modifiedStopDate;
  }

  public int getLastDays()
  {
    return lastDays;
  }

  public void setLastDays(int lastDays)
  {
    this.lastDays = lastDays;
  }

  public String getSearchString()
  {
    return searchString;
  }

  public void setSearchString(String searchString)
  {
    this.searchString = searchString;
  }

  public PFUserDO getModifiedByUser()
  {
    return modifiedByUser;
  }

  public void setModifiedByUser(PFUserDO modifiedByUser)
  {
    this.modifiedByUser = modifiedByUser;
  }

  public String getArea()
  {
    return area;
  }

  public void setArea(String area)
  {
    this.area = area;
  }
}
