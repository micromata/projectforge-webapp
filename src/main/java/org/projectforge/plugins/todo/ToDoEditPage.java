/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.plugins.todo;

import java.sql.Date;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserPrefEditPage;
import org.projectforge.web.wicket.AbstractAutoLayoutEditPage;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.layout.DataObjectLPanel;
import org.projectforge.web.wicket.layout.FieldSetLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;

@EditPage(defaultReturnPage = ToDoListPage.class)
public class ToDoEditPage extends AbstractAutoLayoutEditPage<ToDoDO, ToDoEditForm, ToDoDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = -5058143025817192156L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToDoEditPage.class);

  private static final String CLOSE_MODAL_WINDOW_ID = "closeToDoModalWindow";

  protected ModalWindow closeToDoModalWindow;

  private boolean redirect;

  @SpringBean(name = "toDoDao")
  private ToDoDao toDoDao;

  private ToDoDO oldToDo;

  public ToDoEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.todo");
    init();
    addTopMenuPanel();
    if (isNew() == true) {
      final ToDoDO pref = getToDoPrefData(false);
      if (pref != null) {
        copyPrefValues(pref, getData());
      }
      getData().setReporter(PFUserContext.getUser());
      getData().setStatus(ToDoStatus.OPENED);
    } else {
      // Store old to-do for sending e-mail notification after major changes.
      oldToDo = new ToDoDO();
      oldToDo.copyValuesFrom(getData());
    }
    closeToDoModalWindow = new ModalWindow(CLOSE_MODAL_WINDOW_ID);
    form.add(closeToDoModalWindow);
  }

  @Override
  protected void onAfterRender()
  {
    super.onAfterRender();
    if (ObjectUtils.equals(PFUserContext.getUserId(), getData().getAssigneeId()) == true) {
      // OK, user has now seen this to-do: delete recent flag:
      if (isNew() == false && getData().isRecent() == true) {
        getData().setRecent(false);
        toDoDao.update(getData());
      }
    }
  }

  @Override
  public WebPage afterDelete()
  {
    sendNotification();
    return null;
  }

  @Override
  public WebPage afterUndelete()
  {
    sendNotification();
    return null;
  }

  private void sendNotification()
  {
    final String url = WicketUtils.getAbsoluteEditPageUrl(getRequest(), ToDoEditPage.class, getData().getId());
    toDoDao.sendNotification(form.getData(), url);
  }

  @Override
  public AbstractBasePage afterSaveOrUpdate()
  {
    // Save to-do as recent to-do
    final ToDoDO pref = getToDoPrefData(true);
    copyPrefValues(getData(), pref);
    // Does the user want to store this to-do as template?
    boolean sendNotification = false;
    if (form.renderer.sendNotification == true) {
      sendNotification = true;
    } else if (oldToDo == null) {
      // Send notification on new to-do's.
      sendNotification = true;
    } else {
      if (ObjectUtils.equals(oldToDo.getAssigneeId(), getData().getAssigneeId()) == false) {
        // Assignee was changed.
        sendNotification = true;
      } else if (oldToDo.getStatus() != getData().getStatus()) {
        // Status was changed.
        sendNotification = true;
      } else if (oldToDo.isDeleted() != getData().isDeleted()) {
        // Deletion status was changed.
        sendNotification = true;
      }
    }
    if (sendNotification == true) {
      sendNotification();
    }
    if (BooleanUtils.isTrue(form.renderer.saveAsTemplate) == true) {
      final UserPrefEditPage userPrefEditPage = new UserPrefEditPage(ToDoPlugin.USER_PREF_AREA, getData());
      userPrefEditPage.setReturnToPage(this.returnToPage);
      return userPrefEditPage;
    }
    return null;
  }

  private void copyPrefValues(final ToDoDO src, final ToDoDO dest)
  {
    dest.setPriority(src.getPriority()).setType(src.getType());
  }

  /**
   * @param force If true then a pre entry is created if not exist.
   */
  private ToDoDO getToDoPrefData(final boolean force)
  {
    ToDoDO pref = (ToDoDO) getUserPrefEntry(ToDoDO.class.getName());
    if (pref == null && force == true) {
      pref = new ToDoDO();
      putUserPrefEntry(ToDoDO.class.getName(), pref, true);
    }
    return pref;
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(final String property, final Object selectedValue)
  {
    if ("reporterId".equals(property) == true) {
      toDoDao.setReporter(getData(), (Integer) selectedValue);
    } else if ("assigneeId".equals(property) == true) {
      toDoDao.setAssignee(getData(), (Integer) selectedValue);
    } else if ("taskId".equals(property) == true) {
      toDoDao.setTask(getData(), (Integer) selectedValue);
    } else if ("groupId".equals(property) == true) {
      toDoDao.setGroup(getData(), (Integer) selectedValue);
    } else if ("dueDate".equals(property) == true) {
      final Date date = (Date) selectedValue;
      getData().setDueDate(date);
      form.renderer.dueDatePanel.markModelAsChanged();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(final String property)
  {
    if ("reporterId".equals(property) == true) {
      getData().setReporter(null);
    } else if ("assigneeId".equals(property) == true) {
      getData().setAssignee(null);
    } else if ("taskId".equals(property) == true) {
      getData().setTask(null);
    } else if ("groupId".equals(property) == true) {
      getData().setGroup(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(final String property)
  {
    // Do nothing.
  }

  @SuppressWarnings("serial")
  private void addTopMenuPanel()
  {
    if (isNew() == false && getData().getStatus() != ToDoStatus.CLOSED) {
      final AjaxSubmitLink closeLink = new AjaxSubmitLink("link", form) {
        @Override
        protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
        {
          showCloseToDoDialog(target);
        }

        @Override
        protected void onError(final AjaxRequestTarget target, final Form< ? > form)
        {
          target.addComponent(ToDoEditPage.this.form.getFeedbackPanel());
        }
      };
      final ContentMenuEntryPanel closeMenuPanel = new ContentMenuEntryPanel(getNewContentMenuChildId(), closeLink,
          getString("plugins.todo.button.close"));
      contentMenuEntries.add(closeMenuPanel);
    }
  }

  @SuppressWarnings("serial")
  private void showCloseToDoDialog(final AjaxRequestTarget target)
  {
    closeToDoModalWindow.setInitialWidth(350);
    closeToDoModalWindow.setInitialHeight(250);
    closeToDoModalWindow.setMinimalWidth(350);
    closeToDoModalWindow.setMinimalHeight(250);
    redirect = false;

    final Fragment content = new Fragment(closeToDoModalWindow.getContentId(), "closeToDoDialog", this);
    final Form<Void> ajaxForm = new Form<Void>("form");
    ajaxForm.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final LayoutContext layoutContext = new LayoutContext(false);
    final DataObjectLPanel doPanel = new DataObjectLPanel("fieldSetsPanel", layoutContext);
    final FieldSetLPanel fieldSetPanel = doPanel.newFieldSetPanel(null);
    fieldSetPanel.getFieldSetContainer().add(new SimpleAttributeModifier("style", "min-height:10em;"));
    doPanel.addTextArea(new PanelContext(form.getData(), "comment", LayoutLength.ONEHALF, getString("comment"), LayoutLength.HALF)
    .setCssStyle("height: 10em;"));
    ajaxForm.add(doPanel);

    final AjaxButton cancelButton = new AjaxButton("button", new Model<String>(getString("cancel"))) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        closeToDoModalWindow.close(target);
      }
    };
    cancelButton.add(WebConstants.BUTTON_CLASS_CANCEL);
    cancelButton.setDefaultFormProcessing(false); // No validation of the
    final SingleButtonPanel cancelButtonPanel = new SingleButtonPanel("cancel", cancelButton);
    ajaxForm.add(cancelButtonPanel);
    final AjaxButton closeButton = new AjaxButton("button", new Model<String>(getString("plugins.todo.button.close"))) {
      @Override
      public final void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        getData().setStatus(ToDoStatus.CLOSED);
        redirect = true;
        closeToDoModalWindow.close(target);
      }
    };
    closeButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    final SingleButtonPanel closeButtonPanel = new SingleButtonPanel("close", closeButton);
    ajaxForm.add(closeButtonPanel);
    content.add(ajaxForm);
    closeToDoModalWindow.setContent(content);
    closeToDoModalWindow.setTitle(getString("plugins.todo.closeDialog.heading"));

    closeToDoModalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      private static final long serialVersionUID = 2633814101880954425L;

      public void onClose(final AjaxRequestTarget target)
      {
        if (redirect == true) {
          update();
          redirect = false;
        }
      }

    });
    closeToDoModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      private static final long serialVersionUID = 6761625465164911336L;

      public boolean onCloseButtonClicked(final AjaxRequestTarget target)
      {
        return true;
      }
    });
    closeToDoModalWindow.show(target);
  }

  @Override
  protected ToDoDao getBaseDao()
  {
    return toDoDao;
  }

  @Override
  protected ToDoEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final ToDoDO data)
  {
    return new ToDoEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
