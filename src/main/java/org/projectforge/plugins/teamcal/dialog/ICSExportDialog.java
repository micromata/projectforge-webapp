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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFeedHook;
import org.projectforge.web.dialog.ModalDialog;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class ICSExportDialog extends ModalDialog
{
  private static final long serialVersionUID = -3840971062603541903L;

  /**
   * @param id
   * @param titleModel
   */
  public ICSExportDialog(final String id, final IModel<String> titleModel)
  {
    super(id);
    setTitle(titleModel);
    setBigWindow();
  }

  public void redraw(final TeamCalDO teamCal)
  {
    clearContent();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("calendar.abonnement.url")).setLabelSide(false);
      final String iCalTarget = TeamCalCalendarFeedHook.getUrl(teamCal.getId());
      final String url = WicketUtils.getAbsoluteContextPath() + iCalTarget;
      final TextArea<String> textArea = new TextArea<String>(fs.getTextAreaId(), Model.of(url));
      fs.add(textArea);
      textArea.add(AttributeModifier.replace("onClick", "$(this).select();"));
    }
  }

  @Override
  public void init()
  {
    init(new Form<String>(getFormId()));
    gridBuilder.newFormHeading("invisible");
  }
}
