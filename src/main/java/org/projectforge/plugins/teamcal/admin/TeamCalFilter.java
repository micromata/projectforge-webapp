/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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

  private boolean own, fullAccess, readOnlyAccess, minimalAccess;

  public TeamCalFilter(){
    own = fullAccess = readOnlyAccess = minimalAccess = true;
  }

  public TeamCalFilter(final BaseSearchFilter filter)
  {
    super(filter);
    own = fullAccess = readOnlyAccess = minimalAccess = true;
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
  public void setOwn(final boolean own)
  {
    this.own = own;
  }

  /**
   * @return the readOnlyAccess
   */
  public boolean isReadOnlyAccess()
  {
    return readOnlyAccess;
  }

  /**
   * @param readOnlyAccess the readOnlyAccess to set
   * @return this for chaining.
   */
  public void setReadOnlyAccess(final boolean readOnlyAccess)
  {
    this.readOnlyAccess = readOnlyAccess;
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
  public void setMinimalAccess(final boolean minimalAccess)
  {
    this.minimalAccess = minimalAccess;
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
  public void setFullAccess(final boolean fullAccess)
  {
    this.fullAccess = fullAccess;
  }

}
