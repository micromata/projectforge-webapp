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

package org.projectforge.web.dialog;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.projectforge.web.wicket.AbstractUnsecureBasePage;
import org.projectforge.web.wicket.bootstrap.GridBuilder;

/**
 * Base component for the ProjectForge modal dialogs.<br/>
 * This dialog is modal.<br/>
 * 
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class ModalDialog extends Panel
{
  private static final long serialVersionUID = 4235521713603821639L;

  protected GridBuilder gridBuilder;

  private final WebMarkupContainer mainContainer;

  /**
   * @param id
   */
  public ModalDialog(final String id)
  {
    super(id);
    mainContainer = new WebMarkupContainer("mainContainer");
    add(mainContainer.setOutputMarkupId(true));
  }

  /**
   * Appends css class big-modal.
   */
  public ModalDialog setBigWindow()
  {
    mainContainer.add(AttributeModifier.append("class", "big-modal"));
    return this;
  }

  @SuppressWarnings("serial")
  public ModalDialog wantsNotificationOnClose() {
    mainContainer.add(new AjaxEventBehavior("hidden") {
      @Override
      protected void onEvent(final AjaxRequestTarget target)
      {
        handleCloseEvent(target);
      }
    });
    return this;
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    final String script = "$('#" + getMarkupId(true) + "').modal({keyboard: true, show: false })";
    response.render(OnDomReadyHeaderItem.forScript(script));
  }

  public String getOpenJavaScript()
  {
    return "$('#" + mainContainer.getMarkupId() + "').modal('show');";
  }

  public abstract void init();

  public void setTitle(final String title)
  {
    mainContainer.add(new Label("title", title));
  }

  protected void init(final Form< ? > form)
  {
    mainContainer.add(form);
    final AbstractUnsecureBasePage page = (AbstractUnsecureBasePage) getPage();
    gridBuilder = new GridBuilder(form, "flowform", page.getMySession());
  }

  protected void handleCloseEvent(final AjaxRequestTarget target)
  {
  }

  public String getFormId()
  {
    return "form";
  }
}
