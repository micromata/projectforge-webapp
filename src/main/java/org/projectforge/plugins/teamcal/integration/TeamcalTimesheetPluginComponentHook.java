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

package org.projectforge.plugins.teamcal.integration;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventEditPage;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.web.timesheet.TimesheetEditForm;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.timesheet.TimesheetPluginComponentHook;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * Adds the switch to team event page button to the {@link TimesheetEditPage}
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class TeamcalTimesheetPluginComponentHook implements TimesheetPluginComponentHook, Serializable
{
  private static final long serialVersionUID = -8986533310341503141L;

  /**
   * @see org.projectforge.web.timesheet.TimesheetPluginComponentHook#renderComponentsToTimesheetEditForm(org.projectforge.web.timesheet.TimesheetEditForm,
   *      org.projectforge.timesheet.TimesheetDO)
   */
  @Override
  public void renderComponentsToTimesheetEditForm(final TimesheetEditForm form, final TimesheetDO timesheet)
  {
    if (timesheet == null || timesheet.getId() != null) {
      // Show button only for new timesheets.
      return;
    }
    if (form.getReturnToPage() instanceof TeamCalCalendarPage == false) {
      // May be the add button of time sheet list page was used.
      return;
    }
    final FieldsetPanel fs = form.getTemplatesRow();
    if (fs == null) {
      // May-be clone button was pressed:
      return;
    }
    final Button switchButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("switch")) {
      private static final long serialVersionUID = 7264556996228603021L;

      @Override
      public final void onSubmit()
      {
        final TeamEventDO event = new TeamEventDO();
        if (timesheet != null) {
          event.setStartDate(timesheet.getStartTime());
          event.setEndDate(timesheet.getStopTime());
        }
        setResponsePage(new TeamEventEditPage(new PageParameters(), event));
      }
    };
    switchButton.setDefaultFormProcessing(false);
    final SingleButtonPanel switchButtonPanel = new SingleButtonPanel(fs.newChildId(), switchButton, new ResourceModel(
        "plugins.teamcal.switchToTeamEventButton"), SingleButtonPanel.GREY);
    fs.add(switchButtonPanel);
  }
}
