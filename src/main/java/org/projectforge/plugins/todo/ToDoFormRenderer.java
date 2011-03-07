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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.hibernate.Hibernate;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Priority;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.components.CheckBoxPanel;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelForPanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;

public class ToDoFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = -9175062586210446142L;

  private ToDoDO data;

  private ToDoEditPage toDoEditPage;

  private UserGroupCache userGroupCache;

  protected DatePanel dueDatePanel;

  protected boolean sendNotification = true;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.ONEHALF;

  @SuppressWarnings("unused")
  private String templateName;

  public ToDoFormRenderer(final ToDoEditPage toDoEditPage, final MarkupContainer container, final LayoutContext layoutContext,
      final ToDoDO data, final UserGroupCache userGroupCache)
  {
    super(container, layoutContext);
    this.toDoEditPage = toDoEditPage;
    this.data = data;
    this.userGroupCache = userGroupCache;
  }

  @Override
  public void add()
  {
    doPanel.newFieldSetPanel(getString("plugins.todo.todo"));
    if (isNew() == true) {
      // DropDownChoice favorites
      // final FavoritesChoicePanel<ToDoDO, ToDoDO> favoritesPanel = new FavoritesChoicePanel<ToDoDO, ToDoDO>("favorites",
      // UserPrefArea.TASK_FAVORITE, tabIndex, "full text") {
      // @Override
      // protected void select(final TaskFavorite favorite)
      // {
      // if (favorite.getTask() != null) {
      // TaskSelectPanel.this.selectTask(favorite.getTask());
      // }
      // }
      //
      // @Override
      // protected TaskDO getCurrentObject()
      // {
      // return TaskSelectPanel.this.getModelObject();
      // }
      //
      // @Override
      // protected TaskFavorite newFavoriteInstance(final TaskDO currentObject)
      // {
      // final TaskFavorite favorite = new TaskFavorite();
      // favorite.setTask(currentObject);
      // return favorite;
      // }
      // };
      // divContainer.add(favoritesPanel);
      // favoritesPanel.init();
      // if (showFavorites == false) {
      // favoritesPanel.setVisible(false);
      // }
    }

    doPanel.addTextField(new PanelContext(data, "subject", VALUE_LENGTH, getString("plugins.todo.subject"), LABEL_LENGTH).setRequired()
        .setStrong());
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
      doPanel.addSelectPanel(getString("plugins.todo.assignee"), LABEL_LENGTH, assigneeUserSelectPanel, VALUE_LENGTH).setStrong();
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
      doPanel.addSelectPanel(getString("plugins.todo.reporter"), LABEL_LENGTH, reporterUserSelectPanel, VALUE_LENGTH);
      reporterUserSelectPanel.init();
    }
    {
      // Due date
      dueDatePanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "dueDate"), DatePanelSettings.get()
          .withCallerPage(toDoEditPage).withTargetType(java.sql.Date.class).withSelectProperty("dueDate"));
      doPanel.addDateFieldPanel(data, "dueDate", getString("dueDate"), HALF, dueDatePanel, FULL);
    }
    {
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<TaskDO>(data, "task"),
          toDoEditPage, "taskId");
      taskSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
      doPanel.addSelectPanel(getString("task"), LABEL_LENGTH, taskSelectPanel, VALUE_LENGTH);
      taskSelectPanel.init();
    }
    doPanel.addTextArea(new PanelContext(data, "description", VALUE_LENGTH, getString("description"), LABEL_LENGTH)
        .setCssStyle("height: 10em;"));
    doPanel.addTextArea(new PanelContext(data, "comment", VALUE_LENGTH, getString("comment"), LABEL_LENGTH).setCssStyle("height: 10em;"));
    if (ConfigXml.getInstance().isSendMailConfigured() == true) {
      doPanel.addCheckBox(new PanelContext(this, "sendNotification", VALUE_LENGTH, getString("label.sendEMailNotification"), LABEL_LENGTH))
          .setTooltip(getString("plugins.todo.notification.tooltip"));
    }

    {
      // Save as template checkbox:
      doPanel.addLabel("", LABEL_LENGTH).setBreakBefore();
      final RepeatingView repeatingView = doPanel.addRepeater(VALUE_LENGTH).getRepeatingView();
      final CheckBoxPanel checkBoxPanel = new CheckBoxPanel(repeatingView.newChildId(), new PropertyModel<Boolean>(this, "saveAsTemplate"));
      repeatingView.add(checkBoxPanel);
      final LabelForPanel label = new LabelForPanel(repeatingView.newChildId(), checkBoxPanel.getCheckBox(),
          getString("user.pref.saveAsTemplate"));
      repeatingView.add(label);
    }

    // @Field(index = Index.UN_TOKENIZED)
    // @DateBridge(resolution = Resolution.DAY)
    // private Date resubmission;

  }
}
