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
import java.util.Collection;
import java.util.Date;

import org.projectforge.core.BaseSearchFilter;
import org.projectforge.user.PFUserDO;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamEventFilter extends BaseSearchFilter implements Serializable
{
  private static final long serialVersionUID = 2554610661216573080L;

  private PFUserDO user;

  private Integer teamCalId;

  private Collection<TeamCalDO> teamCals;

  private Date startDate;

  private Date endDate;

  /**
   * @param filter
   */
  public TeamEventFilter(final BaseSearchFilter filter)
  {
    super(filter);
  }

  public TeamEventFilter() {
  }

  /**
   * @return the user
   */
  public PFUserDO getUser()
  {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(final PFUserDO user)
  {
    this.user = user;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate()
  {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   * @return this for chaining.
   */
  public void setStartDate(final Date startDate)
  {
    this.startDate = startDate;
  }

  /**
   * @return the endDate
   */
  public Date getEndDate()
  {
    return endDate;
  }

  /**
   * @param date the endDate to set
   * @return this for chaining.
   */
  public void setEndDate(final Date date)
  {
    this.endDate = date;
  }

  /**
   * @return the teamCalId
   */
  public Integer getTeamCalId()
  {
    return teamCalId;
  }

  /**
   * @param teamCalId the teamCalId to set
   * @return this for chaining.
   */
  public void setTeamCalId(final Integer teamCalId)
  {
    this.teamCalId = teamCalId;
  }

  /**
   * @return the teamCals
   */
  public Collection<TeamCalDO> getTeamCals()
  {
    return teamCals;
  }

  /**
   * @param teamCals the teamCals to set
   */
  public void setTeamCals(final Collection<TeamCalDO> teamCals)
  {
    this.teamCals = teamCals;
  }


}
