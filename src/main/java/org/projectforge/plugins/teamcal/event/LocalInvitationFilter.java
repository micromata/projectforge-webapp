/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

import org.projectforge.core.BaseSearchFilter;

/**
 * 
 * @author Werner Feder (w.feder.extern@micromata.de)
 * 
 */
public class LocalInvitationFilter extends BaseSearchFilter implements Serializable
{
  private static final long serialVersionUID = 917210372774680290L;

  private Integer userId, teamEventId;

  public LocalInvitationFilter()
  {
  }

  public LocalInvitationFilter(final BaseSearchFilter filter)
  {
    super(filter);
  }

  public Integer getUserId()
  {
    return userId;
  }

  public LocalInvitationFilter setUserId(final Integer userId)
  {
    this.userId = userId;
    return this;
  }

  public Integer getTeamEventId()
  {
    return teamEventId;
  }

  public LocalInvitationFilter setTeamEventId(final Integer teamEventId)
  {
    this.teamEventId = teamEventId;
    return this;
  }

}
