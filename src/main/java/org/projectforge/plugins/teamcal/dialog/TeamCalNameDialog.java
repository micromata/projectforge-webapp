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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.projectforge.web.dialog.ModalDialog;
import org.projectforge.web.wicket.flowlayout.DivPanel;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public abstract class TeamCalNameDialog extends ModalDialog
{
  private static final long serialVersionUID = 8687197318833240410L;

  private final IModel<String> nameModel;

  /**
   * @param id
   * @param titleModel
   * @param filter
   */
  public TeamCalNameDialog(final String id, final IModel<String> titleModel, final IModel<String> nameModel)
  {
    super(id);
    this.nameModel = nameModel;
    setTitle(titleModel);
  }

  /**
   * @see org.projectforge.web.dialog.ModalDialog#init()
   */
  @Override
  public void init()
  {
    init(new Form<String>(getFormId()));
    setCloseButtonLabel(getString("save"));
  }

  /**
   * @see org.projectforge.web.dialog.ModalDialog#onCloseButtonSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
   */
  @Override
  protected void onCloseButtonSubmit(final AjaxRequestTarget target)
  {
    onConfirm(target);
  }

  /**
   * @param target
   */
  protected abstract void onConfirm(AjaxRequestTarget target);

  public TeamCalNameDialog redraw()
  {
    clearContent();
    final DivPanel panel = gridBuilder.getPanel();
    final Content content = new Content(panel.newChildId());
    panel.add(content);
    return this;
  }

  /**
   * Inner class to represent the actual dialog content
   * 
   */
  private class Content extends Panel
  {
    private static final long serialVersionUID = -135497846745050310L;

    /**
     * @param id
     */
    public Content(final String id)
    {
      super(id);
    }

    /**
     * @see org.apache.wicket.Component#onInitialize()
     */
    @Override
    protected void onInitialize()
    {
      super.onInitialize();
      final TextField<String> nameField = new TextField<String>("name", nameModel);
      nameField.setRequired(true);
      add(nameField);
    }

  }

}
