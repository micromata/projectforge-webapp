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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.MyComponentsRepeater;

import de.micromata.wicket.ajax.AjaxCallback;
import de.micromata.wicket.ajax.AjaxFormSubmitCallback;
import de.micromata.wicket.ajax.MDefaultAjaxBehavior;

/**
 * Base component for the ProjectForge modal dialogs.<br/>
 * This dialog is modal, not resizable/draggable and has auto size and position.<br/>
 * 
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public abstract class PFDialog extends Panel
{
  private static final long serialVersionUID = -2542957111789393810L;

  private static final String WID_CONTENT = "dialogContent";

  protected WebMarkupContainer dialogContainer;

  private final IModel<String> titleModel;

  private Form<String> buttonForm;

  /**
   * List to create action buttons in the desired order before creating the RepeatingView.
   */
  protected MyComponentsRepeater<Component> actionButtons;

  private AjaxCallback onCloseCallback;

  private MDefaultAjaxBehavior onCloseBehavior;

  /**
   * 
   * @param id the wicket:id
   * @param titleModel the model of the dialog title
   */
  public PFDialog(final String id, final IModel<String> titleModel)
  {
    super(id);
    this.titleModel = titleModel;
    actionButtons = new MyComponentsRepeater<Component>("actionButtons");
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    // container
    dialogContainer = new WebMarkupContainer("dialogContainer");
    dialogContainer.setOutputMarkupId(true);
    dialogContainer.add(new AttributeAppender("title", titleModel));
    add(dialogContainer);
    if (isRefreshedOnOpen() == true) {
      dialogContainer.add(new EmptyPanel(WID_CONTENT));
    } else {
      dialogContainer.add(getDialogContent(WID_CONTENT));
    }
    // feedback
    final ContainerFeedbackMessageFilter containerFeedbackMessageFilter = new ContainerFeedbackMessageFilter(this);
    final WebMarkupContainer feedbackContainer = new WebMarkupContainer("feedbackContainer") {
      private static final long serialVersionUID = -2676548030393266940L;

      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return MySession.get().getFeedbackMessages().hasMessage(containerFeedbackMessageFilter);
      }
    };
    dialogContainer.add(feedbackContainer);
    feedbackContainer.add(new FeedbackPanel("feedback", containerFeedbackMessageFilter));
    // lower button form
    buttonForm = new Form<String>("buttonForm", Model.of("")) {
      private static final long serialVersionUID = 4536735016945915848L;

      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return actionButtons.getList() != null && actionButtons.getList().size() > 0;
      }
    };
    dialogContainer.add(buttonForm);
    buttonForm.setOutputMarkupId(true);
    buttonForm.add(actionButtons.getRepeatingView());
    this.onCloseBehavior = new MDefaultAjaxBehavior() {

      private static final long serialVersionUID = -3696760085641009801L;

      /**
       * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void respond(final AjaxRequestTarget target)
      {
        if (PFDialog.this.onCloseCallback != null) {
          PFDialog.this.onCloseCallback.callback(target);
        }
      }
    };
    add(this.onCloseBehavior);
  }

  /**
   * @return the buttonForm
   */
  public Form<String> getButtonForm()
  {
    return buttonForm;
  }

  /**
   * Opens the dialog
   * 
   * @param target
   */
  public void open(final AjaxRequestTarget target)
  {
    if (isRefreshedOnOpen() == true) {
      this.dialogContainer.replace(getDialogContent(WID_CONTENT));
      target.add(this.dialogContainer);
    }
    String jsFunction = "";
    if (this.onCloseCallback != null) {
      jsFunction = "function() { Wicket.Ajax.get({'u': '" + onCloseBehavior.getCallbackUrl() + "'}); }";
    } else {
      jsFunction = "function() {}";
    }
    target.appendJavaScript("openDialog('" + dialogContainer.getMarkupId() + "', " + jsFunction + ");");
  }

  /**
   * Indicated if the dialog should be refreshed each open(..) call.
   * 
   * @return
   */
  protected boolean isRefreshedOnOpen()
  {
    return false;
  }

  /**
   * Closes the dialog
   * 
   * @param target
   */
  public void close(final AjaxRequestTarget target)
  {
    target.appendJavaScript("$('#" + dialogContainer.getMarkupId() + "').dialog('close');");
  }

  public PFDialog appendNewAjaxActionButton(final AjaxCallback ajaxCallback, final String label, final Form< ? > form,
      final String... classnames)
  {
    final SingleButtonPanel result = addNewAjaxActionButton(ajaxCallback, label, form, classnames);
    this.actionButtons.add(result);
    return this;
  }

  public PFDialog prependNewAjaxActionButton(final AjaxCallback ajaxCallback, final String label, final Form< ? > form,
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

  public PFDialog prependNewAjaxActionButton(final AjaxCallback ajaxCallback, final String label, final String... classnames)
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

  /**
   * Sets the callback which is executed when dialog was closed
   * 
   * @param onCloseCallback the onCloseCallback to set
   * @return this for chaining.
   */
  public PFDialog setOnCloseCallback(final AjaxCallback onCloseCallback)
  {
    this.onCloseCallback = onCloseCallback;
    return this;
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

  /**
   * Hook method which should represent the content of the dialog
   * 
   * @param wicketId
   * @return
   */
  protected abstract Component getDialogContent(String wicketId);

}
