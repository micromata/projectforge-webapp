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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFeedHook;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

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

    final String iCalTarget = TeamCalCalendarFeedHook.getUrl(String.valueOf(teamCal.getId()));
    final ExternalLink iCalExportLink = new ExternalLink(IconLinkPanel.LINK_ID, iCalTarget);
    final IconLinkPanel exportICalButtonPanel = new IconLinkPanel("exportLink", IconType.SUBSCRIPTION,
        getString("plugins.teamcal.subscribe"), iCalExportLink).setLight();
    add(exportICalButtonPanel);
    if (cssStyle != null && cssStyle.length() > 0) {
      add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
    }
    final PFDialog dialog = new PFDialog("dialog", new ResourceModel("plugins.teamcal.calendar")) {
      private static final long serialVersionUID = -8509068698727168517L;

      @Override
      protected Component getDialogContent(final String wicketId)
      {
        // TODO Kai: here we go
        return new Label(wicketId, "dialog content");
      }
    };
    add(dialog);
    final AjaxIconButtonPanel dialogOpenLink = new AjaxIconButtonPanel("exportLink2", IconType.SUBSCRIPTION) {
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
