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

package org.projectforge.plugins.teamcal.event;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.projectforge.core.BaseSearchFilter;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
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
