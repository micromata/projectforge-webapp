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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.components.SingleButtonPanel;

import de.micromata.wicket.ajax.AjaxFormSubmitCallback;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public abstract class TeamCalNameDialog extends PFDialog
{
  private static final long serialVersionUID = 8687197318833240410L;

  private final IModel<String> nameModel;

  private Form<Void> form;

  private TextField<String> nameField;

  /**
   * @param id
   * @param titleModel
   * @param filter
   */
  public TeamCalNameDialog(final String id, final IModel<String> titleModel, final IModel<String> nameModel)
  {
    super(id, titleModel);
    this.nameModel = nameModel;
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    final AjaxFormSubmitCallback okCallback = new AjaxFormSubmitCallback() {
      private static final long serialVersionUID = 7224559508934430123L;

      /**
       * @see de.micromata.wicket.ajax.AjaxFormSubmitCallback#onError(org.apache.wicket.ajax.AjaxRequestTarget,
       *      org.apache.wicket.markup.html.form.Form)
       */
      @Override
      public void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
        TeamCalNameDialog.this.onError(target);
      }

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        onConfirm(target);
      }
    };
    appendNewAjaxActionButton(okCallback, getString("save"), form, SingleButtonPanel.DEFAULT_SUBMIT);
  }

  /**
   * @param target
   */
  protected abstract void onConfirm(AjaxRequestTarget target);

  /**
   * @param target
   */
  protected abstract void onError(AjaxRequestTarget target);

  /**
   * @see org.projectforge.web.dialog.PFDialog#open(org.apache.wicket.ajax.AjaxRequestTarget)
   */
  @Override
  public void open(final AjaxRequestTarget target)
  {
    nameField.setRequired(true);
    target.add(form);
    super.open(target);
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#getDialogContent(java.lang.String)
   */
  @Override
  protected Component getDialogContent(final String wicketId)
  {
    return new Content(wicketId);
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
      form = new Form<Void>("form");
      form.setOutputMarkupId(true);
      add(form);
      nameField = new TextField<String>("name", nameModel);
      form.add(nameField);
    }

  }

}
