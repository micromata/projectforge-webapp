/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.dialog;

import org.apache.wicket.model.IModel;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFeedHook;
import org.projectforge.web.calendar.AbstractICSExportDialog;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalICSExportDialog extends AbstractICSExportDialog
{
  private static final long serialVersionUID = -3840971062603541903L;

  private TeamCalDO teamCal;

  /**
   * @param id
   * @param titleModel
   */
  public TeamCalICSExportDialog(final String id, final IModel<String> titleModel)
  {
    super(id, titleModel);
  }

  public void redraw(final TeamCalDO teamCal)
  {
    this.teamCal = teamCal;
    super.redraw();
  }

  /**
   * @see org.projectforge.web.calendar.AbstractICSExportDialog#getUrl()
   */
  @Override
  protected String getUrl()
  {
    return TeamCalCalendarFeedHook.getUrl(teamCal.getId());
  }

}
