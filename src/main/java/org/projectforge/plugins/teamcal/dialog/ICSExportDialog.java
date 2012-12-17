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
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFeedHook;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.WicketUtils;

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
  public ICSExportDialog(final String id, final IModel<String> titleModel, final TeamCalDO teamCal)
  {
    super(id, titleModel);
    this.teamCal = teamCal;
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#getDialogContent(java.lang.String)
   */
  @Override
  protected Component getDialogContent(final String wicketId)
  {
    return new Content(wicketId);
  }

  private class Content extends Panel
  {
    private static final long serialVersionUID = 5506421088716142887L;

    /**
     * @param id
     */
    public Content(final String id)
    {
      super(id);
      // final ExternalLink iCalExportLink = new ExternalLink(IconLinkPanel.LINK_ID, iCalTarget);
      // IconLinkPanel link = new IconLinkPanel(mainContainer.newChildId(), IconType.SUBSCRIPTION, iCalExportLink);
      // mainContainer.add(link);
    }

    /**
     * @see org.apache.wicket.Component#onInitialize()
     */
    @Override
    protected void onInitialize()
    {
      super.onInitialize();

      final String iCalTarget = TeamCalCalendarFeedHook.getUrl(teamCal.getId());
      final String url = WicketUtils.getAbsoluteContextPath() + iCalTarget;
      add(new TextArea<String>("url", Model.of(url)));
    }
  }
}
