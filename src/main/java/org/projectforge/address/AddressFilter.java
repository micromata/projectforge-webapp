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

package org.projectforge.address;

import java.io.Serializable;

import org.projectforge.core.BaseSearchFilter;


/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class AddressFilter extends BaseSearchFilter implements Serializable
{
  private static final long serialVersionUID = -4397408137924906520L;

  private boolean uptodate = true;

  private boolean outdated = false;

  private boolean leaved = false;

  private boolean active = true;

  private boolean nonActive = false;

  private boolean uninteresting = false;

  private boolean personaIngrata = false;

  private boolean departed = false;

  // Needs to migrate the user preferences before removing.
  @Deprecated
  private boolean myFavorites;

  // Needs to migrate the user preferences before removing.
  @Deprecated
  private boolean newest;

  private String listType = "filter";

  public boolean isUptodate()
  {
    return uptodate;
  }

  public void setUptodate(boolean uptodate)
  {
    this.uptodate = uptodate;
  }

  public boolean isOutdated()
  {
    return outdated;
  }

  public void setOutdated(boolean outdated)
  {
    this.outdated = outdated;
  }

  public boolean isLeaved()
  {
    return leaved;
  }

  public void setLeaved(boolean leaved)
  {
    this.leaved = leaved;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(boolean active)
  {
    this.active = active;
  }

  public boolean isNonActive()
  {
    return nonActive;
  }

  public void setNonActive(boolean nonActive)
  {
    this.nonActive = nonActive;
  }

  public boolean isUninteresting()
  {
    return uninteresting;
  }

  public void setUninteresting(boolean uninteresting)
  {
    this.uninteresting = uninteresting;
  }

  public boolean isPersonaIngrata()
  {
    return personaIngrata;
  }

  public void setPersonaIngrata(boolean personaIngrata)
  {
    this.personaIngrata = personaIngrata;
  }

  public boolean isDeparted()
  {
    return departed;
  }

  public void setDeparted(boolean departed)
  {
    this.departed = departed;
  }

  /**
   * Standard means to consider options: current, departed, uninteresting, personaIngrata, ...
   * @return
   */
  public boolean isFilter()
  {
    return "filter".equals(listType);
  }

  public void setFilter()
  {
    listType = "filter";
  }

  /**
   * If set, only addresses are filtered, the user has marked.
   * @return
   * @see PersonalAddressDO#isFavorite()
   */
  public boolean isMyFavorites()
  {
    return "myFavorites".equals(listType);
  }

  public void setMyFavorites()
  {
    listType = "myFavorites";
  }

  /**
   * If set, the 50 (configurable in addressDao) newest address will be shown.
   * @return
   */
  public boolean isNewest()
  {
    return "newest".equals(listType);
  }

  public void setNewest()
  {
    listType = "newest";
  }

  public String getListType()
  {
    return listType;
  }

  public void setListType(String listType)
  {
    this.listType = listType;
  }

  @Override
  public boolean isDeleted()
  {
    return "deleted".equals(listType);
  }

  @Override
  public void setDeleted(boolean deleted)
  {
    super.setDeleted(deleted);
    listType = "deleted";
  }
}
