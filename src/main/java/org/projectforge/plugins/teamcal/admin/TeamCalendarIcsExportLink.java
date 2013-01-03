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

package org.projectforge.plugins.teamcal.admin;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.projectforge.plugins.teamcal.dialog.ICSExportDialog;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

import de.micromata.wicket.ajax.AjaxCallback;

/**
 * Representation for the subscribtion icon export link for our {@link TeamCalDO} ics exports
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class TeamCalendarIcsExportLink extends Panel
{
  private static final long serialVersionUID = 7062414325369734614L;

  /**
   * 
   * @param id
   * @param teamCal
   * @param cssStyle
   */
  public TeamCalendarIcsExportLink(final String id, final TeamCalDO teamCal)
  {
    this(id, teamCal, null);
  }

  /**
   * 
   * @param id
   * @param teamCal
   * @param cssStyle
   */
  public TeamCalendarIcsExportLink(final String id, final TeamCalDO teamCal, final String cssStyle)
  {
    super(id);

    if (cssStyle != null && cssStyle.length() > 0) {
      add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
    }
    final PFDialog dialog = new ICSExportDialog("dialog", new ResourceModel("plugins.teamcal.calendar"), teamCal);
    add(dialog);
    dialog.appendNewAjaxActionButton(new AjaxCallback() {
      private static final long serialVersionUID = 2079351698403799220L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        dialog.close(target);
      }
    }, PFUserContext.getLocalizedString("close"), SingleButtonPanel.CANCEL);
    final AjaxIconButtonPanel dialogOpenLink = new AjaxIconButtonPanel("exportLink", IconType.SUBSCRIPTION) {
      private static final long serialVersionUID = 3541449118975289501L;

      /**
       * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        dialog.open(target);
      }
    };
    add(dialogOpenLink);
  }

}
