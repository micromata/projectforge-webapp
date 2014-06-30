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
import org.projectforge.plugins.teamcal.event.LocalInvitationDao;
import org.projectforge.user.PFUserContext;

// Represents the counter for displaying the local invitations for current user as white number in red bubble beside the local invitations menu entry.
public class MenuCounterEventNeedsAction extends Model<Integer>
{
  private static final long serialVersionUID = -6203867255786689166L;

  @SpringBean(name = "localInvitationDao")
  private LocalInvitationDao localInvitationDao;

  @Override
  public Integer getObject()
  {
    if (localInvitationDao == null) {
      Injector.get().inject(this);
    }
    final Integer id = PFUserContext.getUserId();
    return localInvitationDao.getLocalInvitations(id);
  }

  public void setLocalInvitationDao(final LocalInvitationDao localInvitationDao)
  {
    this.localInvitationDao = localInvitationDao;
  }
}
