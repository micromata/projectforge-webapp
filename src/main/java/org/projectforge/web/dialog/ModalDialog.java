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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.projectforge.web.wicket.bootstrap.GridBuilder;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.MyComponentsRepeater;

import de.micromata.wicket.ajax.AjaxCallback;
import de.micromata.wicket.ajax.AjaxFormSubmitCallback;

/**
 * Base component for the ProjectForge modal dialogs.<br/>
 * This dialog is modal.<br/>
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class ModalDialog extends Panel
{
  private static final long serialVersionUID = 4235521713603821639L;

  protected GridBuilder gridBuilder;

  private final WebMarkupContainer mainContainer;

  private boolean keyboard;

  private String closeButtonLabel;

  /**
   * List to create action buttons in the desired order before creating the RepeatingView.
   */
  protected MyComponentsRepeater<Component> actionButtons;

  /**
   * @param id
   */
  public ModalDialog(final String id)
  {
    super(id);
    actionButtons = new MyComponentsRepeater<Component>("actionButtons");
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

  /**
   * @param keyboard the keyboard to set
   * @return this for chaining.
   */
  public ModalDialog setKeyboard(final boolean keyboard)
  {
    this.keyboard = keyboard;
    return this;
  }

  /**
   * Close is used as default:
   * @param closeButtonLabel the closeButtonLabel to set
   * @return this for chaining.
   */
  public ModalDialog setCloseButtonLabel(final String closeButtonLabel)
  {
    this.closeButtonLabel = closeButtonLabel;
    return this;
  }

  @SuppressWarnings("serial")
  public ModalDialog wantsNotificationOnClose()
  {
    mainContainer.add(new AjaxEventBehavior("hidden") {
      @Override
      protected void onEvent(final AjaxRequestTarget target)
      {
        handleCloseEvent(target);
      }
    });
    return this;
  }

  public ModalDialog addAjaxEventBehavior(final AjaxEventBehavior behavior)
  {
    mainContainer.add(behavior);
    return this;
  }

  public String getMainContainerMarkupId() {
    return mainContainer.getMarkupId(true);
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    final String script = "$('#" + getMainContainerMarkupId() + "').modal({keyboard: " + keyboard + ", show: false })";
    response.render(OnDomReadyHeaderItem.forScript(script));
  }

  public String getOpenJavaScript()
  {
    return "$('#" + getMainContainerMarkupId() + "').modal('show');";
  }

  public String getCloseJavaScript()
  {
    return "$('#" + getMainContainerMarkupId() + "').modal('hide');";
  }

  public abstract void init();

  public void setTitle(final String title)
  {
    mainContainer.add(new Label("title", title));
  }

  @SuppressWarnings("serial")
  protected void init(final Form< ? > form)
  {
    mainContainer.add(form);
    appendNewAjaxActionButton(new AjaxFormSubmitCallback() {

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        onCloseButtonSubmit(target);
        target.appendJavaScript(getCloseJavaScript());
      }

      @Override
      public void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
        onError(target, form);
      }
    }, closeButtonLabel != null ? closeButtonLabel : getString("close"), SingleButtonPanel.GREY);
    form.add(actionButtons.getRepeatingView());
    gridBuilder = new GridBuilder(form, "flowform");
  }

  /**
   * Called if {@link #wantsNotificationOnClose()} was chosen and the dialog is closed (by pressing esc, clicking outside or clicking the
   * upper right cross).
   * @param target
   */
  protected void handleCloseEvent(final AjaxRequestTarget target)
  {
  }

  /**
   * Called if user hit the close button.
   * @param target
   */
  protected void onCloseButtonSubmit(final AjaxRequestTarget target)
  {
  }

  protected void onError(final AjaxRequestTarget target, final Form< ? > form)
  {
  }

  /**
   * @see org.apache.wicket.Component#onBeforeRender()
   */
  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    actionButtons.render();
  }

  public String getFormId()
  {
    return "form";
  }

  public ModalDialog appendNewAjaxActionButton(final AjaxCallback ajaxCallback, final String label, final Form< ? > form,
      final String... classnames)
  {
    final SingleButtonPanel result = addNewAjaxActionButton(ajaxCallback, label, form, classnames);
    this.actionButtons.add(result);
    return this;
  }

  public ModalDialog prependNewAjaxActionButton(final AjaxCallback ajaxCallback, final String label, final Form< ? > form,
      final String... classnames)
  {
    final SingleButtonPanel result = addNewAjaxActionButton(ajaxCallback, label, form, classnames);
    this.actionButtons.add(0, result);
    return this;
  }

  public SingleButtonPanel appendNewAjaxActionButton(final AjaxCallback ajaxCallback, final String label, final String... classnames)
  {
    final SingleButtonPanel result = addNewAjaxActionButton(ajaxCallback, label, null, classnames);
    this.actionButtons.add(result);
    return result;
  }

  public ModalDialog prependNewAjaxActionButton(final AjaxCallback ajaxCallback, final String label, final String... classnames)
  {
    final SingleButtonPanel result = addNewAjaxActionButton(ajaxCallback, label, null, classnames);
    this.actionButtons.add(0, result);
    return this;
  }

  private SingleButtonPanel addNewAjaxActionButton(final AjaxCallback ajaxCallback, final String label, final Form< ? > form,
      final String... classnames)
  {
    final AjaxButton button = new AjaxButton("button", form) {
      private static final long serialVersionUID = -5306532706450731336L;

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        ajaxCallback.callback(target);
      }

      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
        if (ajaxCallback instanceof AjaxFormSubmitCallback) {
          ((AjaxFormSubmitCallback) ajaxCallback).onError(target, form);
        }
      }
    };
    final SingleButtonPanel buttonPanel = new SingleButtonPanel(this.actionButtons.newChildId(), button, label, classnames);
    buttonPanel.add(button);
    return buttonPanel;
  }
}
