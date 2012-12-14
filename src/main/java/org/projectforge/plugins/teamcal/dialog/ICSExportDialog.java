/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.dialog;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFeedHook;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class ICSExportDialog extends PFDialog
{
  private static final long serialVersionUID = -3840971062603541903L;

  private final TeamCalDO teamCal;

  /**
   * @param id
   * @param titleModel
   */
  public ICSExportDialog(String id, IModel<String> titleModel, TeamCalDO teamCal)
  {
    super(id, titleModel);
    this.teamCal = teamCal;
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#getDialogContent(java.lang.String)
   */
  @Override
  protected Component getDialogContent(String wicketId)
  {
    RepeatingView mainContainer = new RepeatingView(wicketId);

    mainContainer.add(new Label(mainContainer.newChildId(), "Download URL"));

    String iCalTarget = TeamCalCalendarFeedHook.getUrl(String.valueOf(teamCal.getId()));
    mainContainer.add(new Label(mainContainer.newChildId(), RequestCycle.get().getUrlRenderer()
        .renderFullUrl(Url.parse(urlFor(getPage().getClass(), null).toString()))
        + iCalTarget));

    final ExternalLink iCalExportLink = new ExternalLink(IconLinkPanel.LINK_ID, iCalTarget);
    IconLinkPanel link = new IconLinkPanel(mainContainer.newChildId(), IconType.SUBSCRIPTION, iCalExportLink);
    mainContainer.add(link);

    return mainContainer;
  }
}
