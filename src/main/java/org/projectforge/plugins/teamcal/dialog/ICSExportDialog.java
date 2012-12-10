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
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFeedHook;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class ICSExportDialog extends PFDialog
{
  private static final long serialVersionUID = -3840971062603541903L;

  private final String iCalTarget;

  private final Page page;

  /**
   * @param id
   * @param titleModel
   */
  public ICSExportDialog(String id, IModel<String> titleModel, Integer teamCalId, Page page)
  {
    super(id, titleModel);
    iCalTarget = TeamCalCalendarFeedHook.getUrl(String.valueOf(teamCalId));
    this.page = page;
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#getDialogContent(java.lang.String)
   */
  @Override
  protected Component getDialogContent(String wicketId)
  {
    RepeatingView mainContainer = new RepeatingView(wicketId);

    mainContainer.add(new Label(mainContainer.newChildId(), "Download URL"));

    final ExternalLink iCalExportLink = new ExternalLink(IconLinkPanel.LINK_ID, iCalTarget);
    mainContainer.add(new Label(mainContainer.newChildId(), iCalExportLink.getPageRelativePath()));

    Button link = new Button(mainContainer.newChildId(), Model.of(".ics Download")) {
      private static final long serialVersionUID = 4504857470420395891L;

      /**
       * @see org.apache.wicket.markup.html.form.Button#onSubmit()
       */
      @Override
      public void onSubmit()
      {
        super.onSubmit();
      }
    };
    link.setVisible(true);
    mainContainer.add(link);

    return mainContainer;
  }
}
