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

  private boolean myCalendars, mySubscribedCalendars, availableCalendars, allMyCalendars, allCalendars;

  //  private Integer taskId;

  public TeamCalFilter(){
    // seems to do nothing...
  }

  public TeamCalFilter(final BaseSearchFilter filter)
  {
    super(filter);
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
   * @return this for chaining.
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
   * @return this for chaining.
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
   * @return this for chaining.
   */
  public void setAllCalendars(final boolean allCalendars)
  {
    this.allCalendars = allCalendars;
  }

  /**
   * @return the myCalendars
   */
  public boolean isMyCalendars()
  {
    return myCalendars;
  }

  /**
   * @param myCalendars the myCalendars to set
   * @return this for chaining.
   */
  public void setMyCalendars(final boolean myCalendars)
  {
    this.myCalendars = myCalendars;
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
   * @return this for chaining.
   */
  public void setAllMyCalendars(final boolean allMyCalendars)
  {
    this.allMyCalendars = allMyCalendars;
  }
}
