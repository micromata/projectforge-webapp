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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFeedHook;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.calendar.AbstractICSExportDialog;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalICSExportDialog extends AbstractICSExportDialog
{
  private static final long serialVersionUID = -3840971062603541903L;

  private TeamCalDO teamCal;

  private boolean exportReminders;

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
   * @see org.projectforge.web.calendar.AbstractICSExportDialog#addFormFields()
   */
  @Override
  protected void addFormFields()
  {
    if (teamCal.getOwnerId() != null && teamCal.getOwnerId().equals(PFUserContext.getUserId()) == true) {
      // Export reminders for owners as default.
      exportReminders = true;
    }
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options")).supressLabelForWarning();
    final DivPanel checkBoxesPanel = new DivPanel(fs.newChildId(), DivType.CHECKBOX);
    fs.add(checkBoxesPanel);
    @SuppressWarnings("serial")
    final AjaxCheckBox checkBox = new AjaxCheckBox(CheckBoxPanel.WICKET_ID, new PropertyModel<Boolean>(this, "exportReminders")) {
      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        target.add(urlTextArea);
      }
    };
    checkBoxesPanel.add(new CheckBoxPanel(checkBoxesPanel.newChildId(), checkBox, getString("plugins.teamcal.export.reminder.checkbox"))
    .setTooltip(getString("plugins.teamcal.export.reminder.checkbox.tooltip")));
  }

  /**
   * @see org.projectforge.web.calendar.AbstractICSExportDialog#getUrl()
   */
  @Override
  protected String getUrl()
  {
    return TeamCalCalendarFeedHook.getUrl(teamCal.getId(), "&" + TeamCalCalendarFeedHook.PARAM_EXPORT_REMINDER + "=" + exportReminders);
  }

}
