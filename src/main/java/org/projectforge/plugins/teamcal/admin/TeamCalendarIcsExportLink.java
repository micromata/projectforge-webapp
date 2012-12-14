/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.admin;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.projectforge.plugins.teamcal.dialog.ICSExportDialog;
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
    }, getString("close"), SingleButtonPanel.CANCEL);
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
    dialogOpenLink.setLight();
    add(dialogOpenLink);
  }

}
