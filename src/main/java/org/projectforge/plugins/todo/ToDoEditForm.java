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

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.hibernate.Hibernate;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Priority;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefDao;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DialogPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;

public class ToDoEditForm extends AbstractEditForm<ToDoDO, ToDoEditPage>
{
  private static final long serialVersionUID = -6208809585214296635L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToDoEditForm.class);

  private static final String CLOSE_DIALOG_ID = "closeToDoModalWindow";

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userPrefDao")
  private UserPrefDao userPrefDao;

  protected boolean saveAsTemplate, sendNotification = true, sendShortMessage;

  private boolean redirectAfterCloseDialog;

  @SuppressWarnings("unused")
  private String templateName; // Used by Wicket

  private ModalWindow closeModalWindow;

  public ToDoEditForm(final ToDoEditPage parentPage, final ToDoDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    if (isNew() == false
        && getData().getStatus() != ToDoStatus.CLOSED
        && getData().isDeleted() == false
        && getBaseDao().hasLoggedInUserUpdateAccess(getData(), getData(), false)) {
      // Close button:
      final AjaxButton closeButton = new AjaxButton(ButtonPanel.BUTTON_ID, this) {
        @Override
        protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
        {
          showCloseModalWindow(target);
        }

        @Override
        protected void onError(final AjaxRequestTarget target, final Form< ? > form)
        {
          target.add(((ToDoEditForm) form).getFeedbackPanel());
        }
      };
      final SingleButtonPanel closeButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), closeButton,
          getString("plugins.todo.button.close"));
      actionButtons.add(2, closeButtonPanel);
      closeModalWindow = new ModalWindow(CLOSE_DIALOG_ID);
      add(closeModalWindow);
    } else {
      add(new WebMarkupContainer(CLOSE_DIALOG_ID).setVisible(false));
    }

    /* GRID16 - BLOCK */
    gridBuilder.newGrid16();
    if (isNew() == true) {
      // Favorites
      final String[] templateNames = userPrefDao.getPrefNames(ToDoPlugin.USER_PREF_AREA);
      if (templateNames != null && templateNames.length > 0) {
        // DropDownChoice templates
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("templates"));
        final LabelValueChoiceRenderer<String> templateNamesChoiceRenderer = new LabelValueChoiceRenderer<String>();
        templateNamesChoiceRenderer.addValue("", getString("userPref.template.select"));
        for (final String name : templateNames) {
          templateNamesChoiceRenderer.addValue(name, name);
        }
        final DropDownChoice<String> templateNamesChoice = new DropDownChoice<String>(fs.getDropDownChoiceId(),
            new PropertyModel<String>(this, "templateName"), templateNamesChoiceRenderer.getValues(), templateNamesChoiceRenderer) {
          @Override
          protected boolean wantOnSelectionChangedNotifications()
          {
            return true;
          }

          @Override
          protected CharSequence getDefaultChoice(final String selected)
          {
            return "";
          }

          @SuppressWarnings({ "unchecked", "rawtypes"})
          @Override
          protected void onSelectionChanged(final String newSelection)
          {
            if (StringUtils.isNotEmpty(newSelection) == true) {
              // Fill fields with selected template values:
              final UserPrefDO userPref = userPrefDao.getUserPref(ToDoPlugin.USER_PREF_AREA, newSelection);
              if (userPref != null) {
                userPrefDao.fillFromUserPrefParameters(userPref, data);
              }
              templateName = "";
              // Mark all form components as model changed.
              visitFormComponents(new IVisitor() {
                @Override
                public void component(final Object object, final IVisit visit)
                {
                  final FormComponent< ? > fc = (FormComponent< ? >) object;
                  fc.modelChanged();
                  visit.dontGoDeeper();
                }
              });
            }
          }
        };
        templateNamesChoice.setNullValid(true);
        fs.add(templateNamesChoice);
      }
    }

    {
      // Subject
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.todo.subject"));
      final RequiredMaxLengthTextField subject = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "subject"));
      if (isNew() == true) {
        // Only focus for new to-do's:
        subject.add(WicketUtils.setFocus());
      }
      fs.add(subject);
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // ToDo type
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.todo.type"));
      final LabelValueChoiceRenderer<ToDoType> typeChoiceRenderer = new LabelValueChoiceRenderer<ToDoType>(this, ToDoType.values());
      fs.addDropDownChoice(new PropertyModel<ToDoType>(data, "type"), typeChoiceRenderer.getValues(), typeChoiceRenderer)
      .setNullValid(true);
    }
    {
      // Status
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.todo.status"));
      final LabelValueChoiceRenderer<ToDoStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<ToDoStatus>(this, ToDoStatus.values());
      fs.addDropDownChoice(new PropertyModel<ToDoStatus>(data, "status"), statusChoiceRenderer.getValues(), statusChoiceRenderer)
      .setNullValid(true);
    }
    {
      // Due date
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("dueDate"));
      fs.add(new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "dueDate"), DatePanelSettings.get().withTargetType(
          java.sql.Date.class)));
    }

    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Priority
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("priority"));
      final LabelValueChoiceRenderer<Priority> priorityChoiceRenderer = new LabelValueChoiceRenderer<Priority>(this, Priority.values());
      fs.addDropDownChoice(new PropertyModel<Priority>(data, "priority"), priorityChoiceRenderer.getValues(), priorityChoiceRenderer)
      .setNullValid(true);
    }
    {
      // Assignee
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.todo.assignee"));
      PFUserDO assignee = data.getAssignee();
      if (Hibernate.isInitialized(assignee) == false) {
        assignee = userGroupCache.getUser(assignee.getId());
        data.setAssignee(assignee);
      }
      final UserSelectPanel assigneeUserSelectPanel = new UserSelectPanel(fs.newChildId(), new PropertyModel<PFUserDO>(data, "assignee"),
          parentPage, "assigneeId");
      fs.add(assigneeUserSelectPanel);
      assigneeUserSelectPanel.setRequired(true);
      assigneeUserSelectPanel.init();
    }
    {
      // Reporter
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.todo.reporter"));
      PFUserDO reporter = data.getReporter();
      if (Hibernate.isInitialized(reporter) == false) {
        reporter = userGroupCache.getUser(reporter.getId());
        data.setReporter(reporter);
      }
      final UserSelectPanel reporterUserSelectPanel = new UserSelectPanel(fs.newChildId(), new PropertyModel<PFUserDO>(data, "reporter"),
          parentPage, "reporterId");
      fs.add(reporterUserSelectPanel);
      reporterUserSelectPanel.init();
    }
    gridBuilder.newBlockPanel();
    {
      // Task
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("task"), true);
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(fs.newChildId(), new PropertyModel<TaskDO>(data, "task"), parentPage,
          "taskId");
      fs.add(taskSelectPanel);
      taskSelectPanel.init();
      fs.addHelpIcon(getString("plugins.todo.task.tooltip"));
    }
    {
      // Group
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("group"), true);
      final GroupSelectPanel groupSelectPanel = new GroupSelectPanel(fs.newChildId(), new PropertyModel<GroupDO>(data, "group"),
          parentPage, "groupId");
      fs.add(groupSelectPanel);
      fs.setLabelFor(groupSelectPanel);
      fs.addHelpIcon(getString("plugins.todo.group.tooltip"));
      groupSelectPanel.init();
    }
    {
      // Description
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("description"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
    }
    {
      // Comment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("comment"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "comment"))).setAutogrow();
    }
    {
      // Options
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true).setNoLabelFor();
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      if (ConfigXml.getInstance().isSendMailConfigured() == true) {
        checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(this, "sendNotification"),
            getString("label.sendEMailNotification")).setTooltip(getString("plugins.todo.notification.tooltip")));
      }
      // if (ConfigXml.getInstance().isSmsConfigured() == true) {
      // checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(this, "sendShortMessage"),
      // getString("label.sendShortMessage")));
      // }
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(this, "saveAsTemplate"),
          getString("userPref.saveAsTemplate")));
    }
  }

  protected void showCloseModalWindow(final AjaxRequestTarget target)
  {
    redirectAfterCloseDialog = false;
    // Close dialog
    final DialogPanel closeDialog = new DialogPanel(closeModalWindow, getString("plugins.todo.closeDialog.heading"));
    closeModalWindow.setContent(closeDialog);

    final DivPanel content = new DivPanel(closeDialog.newChildId());
    closeDialog.add(content);
    final FieldsetPanel fs = new FieldsetPanel(content, getString("comment"));
    final TextArea<String> comment = new TextArea<String>(TextAreaPanel.WICKET_ID, new PropertyModel<String>(getData(), "comment"));
    WicketUtils.setHeight(comment, 40);
    WicketUtils.setFocus(comment);
    fs.add(new TextAreaPanel(fs.newChildId(), comment).setAutogrow());

    @SuppressWarnings("serial")
    final AjaxButton cancelButton = new AjaxButton(SingleButtonPanel.WICKET_ID, new Model<String>("cancel")) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        closeModalWindow.close(target);
      }

      /**
       * @see org.apache.wicket.ajax.markup.html.form.AjaxButton#onError(org.apache.wicket.ajax.AjaxRequestTarget,
       *      org.apache.wicket.markup.html.form.Form)
       */
      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
      }
    };
    cancelButton.setDefaultFormProcessing(false); // No validation
    final SingleButtonPanel cancelButtonPanel = new SingleButtonPanel(closeDialog.newButtonChildId(), cancelButton, getString("cancel"),
        SingleButtonPanel.CANCEL);
    closeDialog.addButton(cancelButtonPanel);

    @SuppressWarnings("serial")
    final AjaxButton closeButton = new AjaxButton(SingleButtonPanel.WICKET_ID, new Model<String>("close")) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        getData().setStatus(ToDoStatus.CLOSED);
        redirectAfterCloseDialog = true;
        closeModalWindow.close(target);
      }

      /**
       * @see org.apache.wicket.ajax.markup.html.form.AjaxButton#onError(org.apache.wicket.ajax.AjaxRequestTarget,
       *      org.apache.wicket.markup.html.form.Form)
       */
      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
      }
    };
    final SingleButtonPanel closeButtonPanel = new SingleButtonPanel(closeDialog.newButtonChildId(), closeButton,
        getString("plugins.todo.button.close"), SingleButtonPanel.DEFAULT_SUBMIT);
    closeDialog.addButton(closeButtonPanel);

    closeModalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      private static final long serialVersionUID = 2633814101880954425L;

      public void onClose(final AjaxRequestTarget target)
      {
        if (redirectAfterCloseDialog == true) {
          parentPage.updateAndClose();
          redirectAfterCloseDialog = false;
        }
      }

    });
    closeModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      private static final long serialVersionUID = 6761625465164911336L;

      public boolean onCloseButtonClicked(final AjaxRequestTarget target)
      {
        return true;
      }
    });
    closeModalWindow.show(target);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
