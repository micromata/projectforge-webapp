/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket.components;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * The panel which includes the drop behavior for several files. If the dropped file (string) was sucessfully importet, the hook method
 * {@link #onStringImport(AjaxRequestTarget, String)} is called.
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public abstract class DropFileContainer extends Panel
{
  private static final long serialVersionUID = 3622467918922963503L;

  /**
   * @param id
   */
  public DropFileContainer(final String id)
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
    final Form<FormBean> hiddenForm = new Form<FormBean>("hiddenForm", new CompoundPropertyModel<FormBean>(new FormBean()));
    add(hiddenForm);
    hiddenForm.add(new TextArea<String>("importString"));
    hiddenForm.add(new AjaxSubmitLink("submitButton") {
      private static final long serialVersionUID = 6140567784494429257L;

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        onStringImport(target, hiddenForm.getModel().getObject().importString);
      }

      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
        // nothing to do here
      }

    });
  }

  protected abstract void onStringImport(final AjaxRequestTarget target, final String string);

  /**
   * Just the form model
   * 
   */
  private class FormBean implements Serializable
  {
    private static final long serialVersionUID = 4250094235574838882L;

    private String importString;
  }
}
