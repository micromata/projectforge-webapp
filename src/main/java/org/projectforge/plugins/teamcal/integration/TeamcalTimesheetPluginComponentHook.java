/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.projectforge.plugins.teamcal.event.TeamEventEditPage;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.timesheet.TimesheetPluginComponentHook;
import org.projectforge.web.wicket.components.SingleButtonPanel;

/**
 * Adds the switch to team event page button to the {@link TimesheetEditPage}
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class TeamcalTimesheetPluginComponentHook implements TimesheetPluginComponentHook
{

  /**
   * @see org.projectforge.web.timesheet.TimesheetPluginComponentHook#renderActionButtonsToTimesheetEditPage(java.lang.String)
   */
  @Override
  public Component renderActionButtonsToTimesheetEditPage(final String wicketId)
  {
    final Button switchButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("clone")) {
      private static final long serialVersionUID = 7264556996228603021L;

      @Override
      public final void onSubmit()
      {
        setResponsePage(TeamEventEditPage.class);
      }
    };
    switchButton.setDefaultFormProcessing(false);
    final SingleButtonPanel cloneButtonPanel = new SingleButtonPanel(wicketId, switchButton, new ResourceModel(
        "plugins.teamcal.switchToTeamEventButton"));
    return cloneButtonPanel;
  }

}
