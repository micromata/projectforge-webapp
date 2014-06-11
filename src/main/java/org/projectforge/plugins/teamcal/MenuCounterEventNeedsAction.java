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

package org.projectforge.plugins.teamcal;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.user.PFUserContext;

// Represents the counter for displaying the open to-do entries as white number in red bubble beside the to-do menu entry.
public class MenuCounterEventNeedsAction extends Model<Integer>
{
  private static final long serialVersionUID = -6203867255786689166L;

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  @Override
  public Integer getObject()
  {
    if (teamEventDao == null) {
      Injector.get().inject(this);
    }
    final Integer id = PFUserContext.getUserId();
    return teamEventDao.getNeedsActionEvents(id);
  }

  public void setTeamEventDao(final TeamEventDao teamEventDao)
  {
    this.teamEventDao = teamEventDao;
  }
}
