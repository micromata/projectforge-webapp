/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.SelectLPanel.WICKET_ID_SELECT_PANEL;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormVisitorParticipant;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
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
import org.projectforge.web.wicket.components.CheckBoxLabelPanel;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.DropDownChoicePanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.RepeatingViewLPanel;

public class ToDoFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = -9175062586210446142L;

  private final ToDoDO data;

  private final ToDoEditPage toDoEditPage;

  private final UserGroupCache userGroupCache;

  private final UserPrefDao userPrefDao;

  protected DatePanel dueDatePanel;

  protected boolean sendNotification = true;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.ONEHALF;

  @SuppressWarnings("unused")
  private String templateName;

  protected Boolean saveAsTemplate;

  public ToDoFormRenderer(final ToDoEditPage toDoEditPage, final MarkupContainer container, final LayoutContext layoutContext,
      final ToDoDO data, final UserGroupCache userGroupCache, final UserPrefDao userPrefDao)
  {
    super(container, layoutContext);
    this.toDoEditPage = toDoEditPage;
    this.data = data;
    this.userGroupCache = userGroupCache;
    this.userPrefDao = userPrefDao;
  }

  @SuppressWarnings("serial")
  @Override
  public void add()
  {
    doPanel.newFieldSetPanel(getString("plugins.todo.todo"));
    if (isNew() == true) {
      // DropDownChoice favorites
      final String[] templateNames = userPrefDao.getPrefNames(ToDoPlugin.USER_PREF_AREA);
      if (templateNames != null && templateNames.length > 0) {
        // DropDownChoice templates
        doPanel.addLabel(getString("templates"), new PanelContext(HALF));
        final RepeatingViewLPanel templatesPanel = doPanel.addRepeater(new PanelContext(VALUE_LENGTH));
        final RepeatingView repeatingView = templatesPanel.getRepeatingView();
        final String label = getString("user.pref.template.select");
        final LabelValueChoiceRenderer<String> templateNamesChoiceRenderer = new LabelValueChoiceRenderer<String>();
        templateNamesChoiceRenderer.addValue("", label);
        for (final String name : templateNames) {
          templateNamesChoiceRenderer.addValue(name, name);
        }
        final DropDownChoice<String> templateNamesChoice = new DropDownChoice<String>(DropDownChoicePanel.WICKET_ID,
            new PropertyModel<String>(this, "templateName"), templateNamesChoiceRenderer.getValues(), templateNamesChoiceRenderer) {
          @Override
          protected boolean wantOnSelectionChangedNotifications()
          {
            return true;
          }

          @Override
          protected CharSequence getDefaultChoice(final Object selected)
          {
            return "";
          }

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
              toDoEditPage.getForm().visitFormComponents(new FormComponent.IVisitor() {
                public Object formComponent(final IFormVisitorParticipant formComponent)
                {
                  final FormComponent< ? > fc = (FormComponent< ? >) formComponent;
                  fc.modelChanged();
                  return Component.IVisitor.CONTINUE_TRAVERSAL;
                }
              });
            }
          }
        };
        templateNamesChoice.setNullValid(true);
        repeatingView.add(new DropDownChoicePanel<String>(repeatingView.newChildId(), templateNamesChoice));
      }
    }

    doPanel.addTextField(new PanelContext(data, "subject", VALUE_LENGTH, getString("plugins.todo.subject"), LABEL_LENGTH).setRequired()
        .setStrong().setFocus());
    {
      final LabelValueChoiceRenderer<ToDoType> typeChoiceRenderer = new LabelValueChoiceRenderer<ToDoType>(container, ToDoType.values());
      final DropDownChoice<ToDoType> typeChoice = new DropDownChoice<ToDoType>(DropDownChoiceLPanel.SELECT_ID, new PropertyModel<ToDoType>(
          data, "type"), typeChoiceRenderer.getValues(), typeChoiceRenderer);
      typeChoice.setNullValid(true);
      doPanel.addDropDownChoice(typeChoice, new PanelContext(LayoutLength.THREEQUART, getString("plugins.todo.type"), LABEL_LENGTH));
    }
    {
      // Priority drop down box:
      final LabelValueChoiceRenderer<Priority> priorityChoiceRenderer = new LabelValueChoiceRenderer<Priority>(container, Priority.values());
      final DropDownChoice<Priority> priorityChoice = new DropDownChoice<Priority>(SELECT_ID,
          new PropertyModel<Priority>(data, "priority"), priorityChoiceRenderer.getValues(), priorityChoiceRenderer);
      priorityChoice.setNullValid(true);
      doPanel.addDropDownChoice(priorityChoice, new PanelContext(LayoutLength.THREEQUART, getString("priority"), LABEL_LENGTH));
    }
    {
      // Status drop down box:
      final LabelValueChoiceRenderer<ToDoStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<ToDoStatus>(container, ToDoStatus
          .values());
      final DropDownChoice<ToDoStatus> statusChoice = new DropDownChoice<ToDoStatus>(SELECT_ID, new PropertyModel<ToDoStatus>(data,
      "status"), statusChoiceRenderer.getValues(), statusChoiceRenderer);
      statusChoice.setNullValid(true);
      doPanel.addDropDownChoice(statusChoice, new PanelContext(LayoutLength.THREEQUART, getString("plugins.todo.status"), LABEL_LENGTH));
    }
    {
      PFUserDO assignee = data.getAssignee();
      if (Hibernate.isInitialized(assignee) == false) {
        assignee = userGroupCache.getUser(assignee.getId());
        data.setAssignee(assignee);
      }
      final UserSelectPanel assigneeUserSelectPanel = new UserSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<PFUserDO>(data,
      "assignee"), toDoEditPage, "assigneeId");
      doPanel.addSelectPanel(assigneeUserSelectPanel, new PanelContext(VALUE_LENGTH, getString("plugins.todo.assignee"), LABEL_LENGTH)
      .setStrong());
      assigneeUserSelectPanel.setRequired(true);
      assigneeUserSelectPanel.init();
    }
    {
      PFUserDO reporter = data.getReporter();
      if (Hibernate.isInitialized(reporter) == false) {
        reporter = userGroupCache.getUser(reporter.getId());
        data.setReporter(reporter);
      }
      final UserSelectPanel reporterUserSelectPanel = new UserSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<PFUserDO>(data,
      "reporter"), toDoEditPage, "reporterId");
      doPanel.addSelectPanel(reporterUserSelectPanel, new PanelContext(VALUE_LENGTH, getString("plugins.todo.reporter"), LABEL_LENGTH));
      reporterUserSelectPanel.init();
    }
    {
      // Due date
      dueDatePanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "dueDate"), DatePanelSettings.get()
          .withCallerPage(toDoEditPage).withTargetType(java.sql.Date.class).withSelectProperty("dueDate"));
      doPanel.addDateFieldPanel(dueDatePanel, new PanelContext(data, "dueDate", FULL, getString("dueDate"), HALF));
    }
    {
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<TaskDO>(data, "task"),
          toDoEditPage, "taskId");
      taskSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
      doPanel.addSelectPanel(taskSelectPanel, new PanelContext(VALUE_LENGTH, getString("task"), LABEL_LENGTH)
      .setTooltip(getString("plugins.todo.task.tooltip")));
      taskSelectPanel.init();
    }
    {
      final GroupSelectPanel groupSelectPanel = new GroupSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<GroupDO>(data, "group"),
          toDoEditPage, "groupId");
      doPanel.addSelectPanel(groupSelectPanel, new PanelContext(VALUE_LENGTH, getString("group"), LABEL_LENGTH)
      .setTooltip(getString("plugins.todo.group.tooltip")));
      groupSelectPanel.init();
    }
    doPanel.addTextArea(new PanelContext(data, "description", VALUE_LENGTH, getString("description"), LABEL_LENGTH)
    .setCssStyle("height: 10em;"));
    doPanel.addTextArea(new PanelContext(data, "comment", VALUE_LENGTH, getString("comment"), LABEL_LENGTH).setCssStyle("height: 10em;"));
    if (ConfigXml.getInstance().isSendMailConfigured() == true) {
      doPanel.addLabel("", new PanelContext(LABEL_LENGTH).setBreakBefore(true));
      final RepeatingView repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
      final CheckBoxLabelPanel checkBoxLabelPanel = new CheckBoxLabelPanel(repeatingView.newChildId(), new PropertyModel<Boolean>(this,
      "sendNotification"), getString("label.sendEMailNotification"));
      repeatingView.add(checkBoxLabelPanel);
      checkBoxLabelPanel.setTooltip(getString("plugins.todo.notification.tooltip"));
    }
    //    if (ConfigXml.getInstance().isSmsConfigured() == true) {
    //      doPanel.addLabel("", new PanelContext(LABEL_LENGTH).setBreakBefore(true));
    //      final RepeatingView repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
    //      final CheckBox checkBox = new CheckBox(CheckBoxLabelPanel.WICKET_ID, new PropertyModel<Boolean>(this, "sendShortMessage")) {
    //        @Override
    //        public boolean isEnabled()
    //        {
    //          return data.getAssignee() != null && data.getAssignee().getPersonalMebMobileNumbers(); // Außerdem: Beobachter (standardmäßig die letzten Bearbeiter/Reporter.
    //        }
    //      };
    //      final CheckBoxLabelPanel checkBoxLabelPanel = new CheckBoxLabelPanel(repeatingView.newChildId(), checkBox,
    //          getString("label.sendShortMessage"));
    //      repeatingView.add(checkBoxLabelPanel);
    //    }

    {
      // Save as template checkbox:
      doPanel.addLabel("", new PanelContext(LABEL_LENGTH).setBreakBefore(true));
      final RepeatingView repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
      final CheckBoxLabelPanel checkBoxLabelPanel = new CheckBoxLabelPanel(repeatingView.newChildId(), new PropertyModel<Boolean>(this,
      "saveAsTemplate"), getString("user.pref.saveAsTemplate"));
      repeatingView.add(checkBoxLabelPanel);
    }

    // @Field(index = Index.UN_TOKENIZED)
    // @DateBridge(resolution = Resolution.DAY)
    // private Date resubmission;

  }
}
