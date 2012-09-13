/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.io.Serializable;

import org.projectforge.core.BaseSearchFilter;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalFilter extends BaseSearchFilter implements Serializable
{
  private static final long serialVersionUID = 7410573665085873058L;

  private boolean own, mySubscribedCalendars, availableCalendars, allMyCalendars, allCalendars;

  private Integer ownerId;

  public TeamCalFilter(){
  }

  public TeamCalFilter(final BaseSearchFilter filter)
  {
    super(filter);
  }

  /**
   * @return the userId
   */
  public Integer getOwnerId()
  {
    return ownerId;
  }

  public void setOwnerId(final Integer ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * @return the mySubscribedCalendars
   */
  public boolean isMySubscribedCalendars()
  {
    return mySubscribedCalendars;
  }

  /**
   * @param mySubscribedCalendars the mySubscribedCalendars to set
   */
  public void setMySubscribedCalendars(final boolean mySubscribedCalendars)
  {
    this.mySubscribedCalendars = mySubscribedCalendars;
  }

  /**
   * @return the availableCalendars
   */
  public boolean isAvailableCalendars()
  {
    return availableCalendars;
  }

  /**
   * @param availableCalendars the availableCalendars to set
   */
  public void setAvailableCalendars(final boolean availableCalendars)
  {
    this.availableCalendars = availableCalendars;
  }

  /**
   * @return the allCalendars
   */
  public boolean isAllCalendars()
  {
    return allCalendars;
  }

  /**
   * @param allCalendars the allCalendars to set
   */
  public void setAllCalendars(final boolean allCalendars)
  {
    this.allCalendars = allCalendars;
  }

  /**
   * @return the allMyCalendars
   */
  public boolean isAllMyCalendars()
  {
    return allMyCalendars;
  }

  /**
   * @param allMyCalendars the allMyCalendars to set
   */
  public void setAllMyCalendars(final boolean allMyCalendars)
  {
    this.allMyCalendars = allMyCalendars;
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

}
