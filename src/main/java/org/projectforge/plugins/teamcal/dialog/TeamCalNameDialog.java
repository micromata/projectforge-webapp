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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.components.SingleButtonPanel;

import de.micromata.wicket.ajax.AjaxCallback;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public abstract class TeamCalNameDialog extends PFDialog
{
  private static final long serialVersionUID = 8687197318833240410L;

  private final IModel<String> nameModel;

  private Form<Void> form;

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
    final AjaxCallback okCallback = new AjaxCallback() {
      private static final long serialVersionUID = 7224559508934430123L;

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
   * @see org.projectforge.web.dialog.PFDialog#open(org.apache.wicket.ajax.AjaxRequestTarget)
   */
  @Override
  public void open(final AjaxRequestTarget target)
  {
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
      form.add(new TextField<String>("name", nameModel));
    }

  }

}
