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
import java.io.StringReader;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public abstract class DropFileContainer extends Panel
{
  private static final long serialVersionUID = 3622467918922963503L;

  private static final Logger log = Logger.getLogger(DropFileContainer.class);

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
    hiddenForm.add(new TextField<String>("importIcs"));
    hiddenForm.add(new AjaxSubmitLink("submitButton") {
      private static final long serialVersionUID = 6140567784494429257L;

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        final CalendarBuilder builder = new CalendarBuilder();
        try {
          // TODO ju: following "import" does not work correctly
          final Calendar calendar = builder.build(new StringReader(hiddenForm.getModel().getObject().importIcs));
          onIcsImport(target, calendar);
        } catch (final Exception ex) {
          // TODO ju: error handling
          log.fatal(ex);
        }
      }

      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
        // TODO ju: error handling
      }

    });
  }

  protected abstract void onIcsImport(final AjaxRequestTarget target, final Calendar calendar);

  /**
   * Just the form model
   * 
   */
  private class FormBean implements Serializable
  {
    private static final long serialVersionUID = 4250094235574838882L;

    private String importIcs;
  }
}
