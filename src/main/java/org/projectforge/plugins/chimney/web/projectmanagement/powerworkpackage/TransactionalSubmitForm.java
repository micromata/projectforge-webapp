/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

public class TransactionalSubmitForm<T> extends Form<T>
{
  private static final long serialVersionUID = -5054114644503562395L;

  private transient List<Runnable> submitActions = new LinkedList<Runnable>();

  protected void flushTransactionalSubmitActions() {
    if (submitActions == null)
      return;

    for (final Runnable action: submitActions) {
      action.run();
    }
  }

  public void addTransactionalSubmitAction(final Runnable action) {
    if (submitActions == null)
      submitActions = new LinkedList<Runnable>();
    submitActions.add(action);
  }

  public TransactionalSubmitForm(final String id, final IModel<T> model)
  {
    super(id, model);
  }

  public TransactionalSubmitForm(final String id)
  {
    super(id);
  }

}
