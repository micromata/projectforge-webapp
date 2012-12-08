/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.admin;

import java.io.Serializable;

import org.projectforge.core.BaseSearchFilter;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalFilter extends BaseSearchFilter implements Serializable
{
  private static final long serialVersionUID = 7410573665085873058L;

  private boolean own, foreign, fullAccess, readonlyAccess, minimalAccess;

  public TeamCalFilter()
  {
    this(null);
  }

  public TeamCalFilter(final BaseSearchFilter filter)
  {
    super(filter);
    own = foreign = fullAccess = readonlyAccess = minimalAccess = true;
  }

  /**
   * @return the own
   */
  public boolean isOwn()
  {
    return own;
  }

  /**
   * @param own the own to set
   */
  public TeamCalFilter setOwn(final boolean own)
  {
    this.own = own;
    return this;
  }

  /**
   * @return the foreign
   */
  public boolean isForeign()
  {
    return foreign;
  }

  /**
   * @param foreign the foreign to set
   * @return this for chaining.
   */
  public TeamCalFilter setForeign(final boolean foreign)
  {
    this.foreign = foreign;
    return this;
  }

  /**
   * @return the readonlyAccess
   */
  public boolean isReadonlyAccess()
  {
    return readonlyAccess;
  }

  /**
   * @param readonlyAccess the readOnlyAccess to set
   * @return this for chaining.
   */
  public TeamCalFilter setReadonlyAccess(final boolean readonlyAccess)
  {
    this.readonlyAccess = readonlyAccess;
    return this;
  }

  /**
   * @return the minimalAccess
   */
  public boolean isMinimalAccess()
  {
    return minimalAccess;
  }

  /**
   * @param minimalAccess the minimalAccess to set
   * @return this for chaining.
   */
  public TeamCalFilter setMinimalAccess(final boolean minimalAccess)
  {
    this.minimalAccess = minimalAccess;
    return this;
  }

  /**
   * @return the fullAccess
   */
  public boolean isFullAccess()
  {
    return fullAccess;
  }

  /**
   * @param fullAccess the fullAccess to set
   * @return this for chaining.
   */
  public TeamCalFilter setFullAccess(final boolean fullAccess)
  {
    this.fullAccess = fullAccess;
    return this;
  }

}
