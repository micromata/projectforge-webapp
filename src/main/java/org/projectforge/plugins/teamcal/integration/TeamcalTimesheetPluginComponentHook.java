/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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
    final FieldsetPanel fs = form.getTemplatesRow();
    final SingleButtonPanel switchButtonPanel = new SingleButtonPanel(fs.newChildId(), switchButton, new ResourceModel(
        "plugins.teamcal.switchToTeamEventButton"), SingleButtonPanel.GREY);
    fs.add(switchButtonPanel);
  }
}
