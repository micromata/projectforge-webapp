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

package org.projectforge.web.task;

import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;
import static org.projectforge.web.wicket.layout.LayoutLength.DOUBLE;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.LayoutLength.QUART;
import static org.projectforge.web.wicket.layout.LayoutLength.THREEQUART;
import static org.projectforge.web.wicket.layout.SelectLPanel.WICKET_ID_SELECT_PANEL;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.hibernate.Hibernate;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Priority;
import org.projectforge.core.ConfigXml;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.gantt.GanttObjectType;
import org.projectforge.gantt.GanttRelationType;
import org.projectforge.jira.JiraUtils;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskStatus;
import org.projectforge.task.TimesheetBookingStatus;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.fibu.Kost2ListPage;
import org.projectforge.web.fibu.Kost2SelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.DropDownChoicePanel;
import org.projectforge.web.wicket.components.LabelPanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.TextFieldOrLabelPanel;
import org.projectforge.web.wicket.converter.IntegerPercentConverter;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.LabelLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class TaskFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = -5074437615848025965L;

  private final TaskDao taskDao;

  private TaskDO data;

  private TaskEditPage taskEditPage;

  protected UserGroupCache userGroupCache;

  protected TextField<BigDecimal> durationField;

  protected DatePanel startDatePanel, endDatePanel, protectTimesheetsUntilPanel;

  protected MaxLengthTextField kost2BlackWhiteTextField;

  private Integer kost2Id;

  public TaskFormRenderer(final TaskEditPage taskEditPage, final MarkupContainer container, final LayoutContext layoutContext,
      final TaskDao taskDao, final TaskDO data)
  {
    super(container, layoutContext);
    this.taskEditPage = taskEditPage;
    this.data = data;
    this.taskDao = taskDao;
  }

  protected void validation()
  {
  }

  @SuppressWarnings( { "unchecked", "serial"})
  @Override
  public void add()
  {
    if (isNew() == false && data.getId() != null) {
      // Add title of task as title of field set:
      doPanel.newFieldSetPanel(data.getTitle());
      // Add path as sub-title:
      // doPanel.addLabel(TaskFormatter.instance().getTaskPath(data.getId(), false, OutputType.PLAIN), DOUBLE);
    }
    {
      final TaskSelectPanel parentTaskSelectPanel = new TaskSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<TaskDO>(data,
          "parentTask"), taskEditPage, "parentTaskId");
      parentTaskSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
      doPanel.addSelectPanel(getString("task.parentTask"), HALF, parentTaskSelectPanel, DOUBLE);
      parentTaskSelectPanel.init();
      if (taskEditPage.getBaseDao().getTaskTree().isRootNode(data) == false) {
        parentTaskSelectPanel.setRequired(true);
      } else {
        parentTaskSelectPanel.setVisible(false);
      }
      parentTaskSelectPanel.setRequired(true);
    }
    doPanel.addTextField(data, "title", getString("task.title"), HALF, FULL).setStrong().setRequired().setFocus();
    doPanel.addTextField(data, "reference", getString("task.reference"), HALF, FULL);
    {
      // Status drop down box:
      final LabelValueChoiceRenderer<TaskStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<TaskStatus>(container, TaskStatus
          .values());
      final DropDownChoice statusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "status"),
          statusChoiceRenderer.getValues(), statusChoiceRenderer);
      statusChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(data, "status", getString("status"), HALF, statusChoice, THREEQUART);
    }
    {
      // Priority drop down box:
      final LabelValueChoiceRenderer<Priority> priorityChoiceRenderer = new LabelValueChoiceRenderer<Priority>(container, Priority.values());
      final DropDownChoice priorityChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "priority"), priorityChoiceRenderer
          .getValues(), priorityChoiceRenderer);
      priorityChoice.setNullValid(true);
      doPanel.addDropDownChoice(data, "priority", getString("priority"), HALF, priorityChoice, THREEQUART);
    }
    final boolean jiraSupport = ConfigXml.getInstance().isJIRAConfigured();
    {
      final String jiraFootnoteMark = jiraSupport ? "*" : "";
      doPanel.addTextField(data, "shortDescription", getString("shortDescription") + jiraFootnoteMark, HALF, FULL);
      if (jiraSupport == true && JiraUtils.hasJiraIssues(data.getShortDescription()) == true) {
        doPanel.addLabel("", HALF).setBreakBefore();
        doPanel.addJiraIssuesPanel(DOUBLE, data.getShortDescription());
      }
      doPanel.addTextArea(data, "description", getString("description") + jiraFootnoteMark, HALF, DOUBLE, false).setCssStyle(
          "height: 20em;");
      if (jiraSupport == true && JiraUtils.hasJiraIssues(data.getDescription()) == true) {
        doPanel.addLabel("", HALF).setBreakBefore();
        doPanel.addJiraIssuesPanel(DOUBLE, data.getDescription());
      }
    }
    {
      PFUserDO responsibleUser = data.getResponsibleUser();
      if (Hibernate.isInitialized(responsibleUser) == false) {
        responsibleUser = userGroupCache.getUser(responsibleUser.getId());
        data.setResponsibleUser(responsibleUser);
      }
      final UserSelectPanel responsibleUserSelectPanel = new UserSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<PFUserDO>(data,
          "responsibleUser"), taskEditPage, "responsibleUserId");
      doPanel.addSelectPanel(getString("task.assignedUser"), HALF, responsibleUserSelectPanel, FULL).setStrong();
      responsibleUserSelectPanel.init();
    }
    {
      final MinMaxNumberField<Integer> maxNumberField = new MinMaxNumberField<Integer>(TextFieldLPanel.INPUT_ID,
          new PropertyModel<Integer>(data, "maxHours"), 0, 9999);
      final String maxHoursString = getString("task.maxHours");
      final LabelLPanel label = doPanel.addLabel(maxHoursString, HALF);
      label.setLabelFor(maxNumberField).setBreakBefore();
      doPanel.addTextField(maxHoursString, maxNumberField, QUART);
      if (isNew() == false && taskDao.getTaskTree().hasOrderPositions(data.getId(), true) == true) {
        WicketUtils.setWarningTooltip(maxNumberField);
        WicketUtils.addTooltip(maxNumberField, getString("task.edit.maxHoursIngoredDueToAssignedOrders"));
      }
    }
    if (jiraSupport == true) {
      // Add help text:
      doPanel.addLabel("", HALF).setBreakBefore();
      doPanel.addHelpLabel("*) " + getString("tooltip.jiraSupport.field"), DOUBLE);
    }

    // New field set
    doPanel.newFieldSetPanel(null);
    doPanel.newGroupPanel(getString("task.gantt.settings"));
    {
      // Gantt object type:
      final LabelValueChoiceRenderer<GanttObjectType> objectTypeChoiceRenderer = new LabelValueChoiceRenderer<GanttObjectType>(container,
          GanttObjectType.values());
      final DropDownChoice<GanttObjectType> objectTypeChoice = new DropDownChoice<GanttObjectType>(SELECT_ID, new PropertyModel(data,
          "ganttObjectType"), objectTypeChoiceRenderer.getValues(), objectTypeChoiceRenderer);
      objectTypeChoice.setNullValid(true);
      doPanel.addDropDownChoice(data, "ganttObjectType", getString("gantt.objectType"), HALF, objectTypeChoice, THREEQUART);
    }
    {
      // Progress
      final MinMaxNumberField<Integer> progressField = new MinMaxNumberField<Integer>(TextFieldLPanel.INPUT_ID, new PropertyModel<Integer>(
          data, "progress"), 0, 100) {
        @Override
        public IConverter getConverter(Class< ? > type)
        {
          return new IntegerPercentConverter();
        }
      };
      final String progressString = WicketUtils.getLabelWithUnit(getString("task.progress"), "%");
      final LabelLPanel label = doPanel.addLabel(progressString, HALF);
      label.setLabelFor(progressField).setBreakBefore();
      doPanel.addTextField(progressString, progressField, QUART);
    }
    {
      // Gantt: start date
      startDatePanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "startDate"), DatePanelSettings.get()
          .withCallerPage(taskEditPage).withTargetType(java.sql.Date.class).withSelectProperty("startDate"));
      doPanel.addDateFieldPanel(data, "startDate", getString("gantt.startDate"), HALF, startDatePanel, FULL);
    }
    {
      // Gantt: duration
      durationField = new MinMaxNumberField<BigDecimal>(TextFieldLPanel.INPUT_ID, new PropertyModel<BigDecimal>(data, "duration"),
          BigDecimal.ZERO, TaskEditForm.MAX_DURATION_DAYS);
      doPanel.addTextField(getString("gantt.duration"), HALF, durationField, QUART);
    }
    {
      // Gantt: end date
      endDatePanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "endDate"), DatePanelSettings.get()
          .withCallerPage(taskEditPage).withTargetType(java.sql.Date.class).withSelectProperty("endDate"));
      doPanel.addDateFieldPanel(data, "endDate", getString("gantt.endDate"), HALF, endDatePanel, FULL);
    }
    {
      // Gantt relation type:
      final LabelValueChoiceRenderer<GanttRelationType> relationTypeChoiceRenderer = new LabelValueChoiceRenderer<GanttRelationType>(
          container, GanttRelationType.values());
      final DropDownChoice<GanttRelationType> relationTypeChoice = new DropDownChoice<GanttRelationType>(SELECT_ID, new PropertyModel(data,
          "ganttRelationType"), relationTypeChoiceRenderer.getValues(), relationTypeChoiceRenderer);
      relationTypeChoice.setNullValid(true);
      doPanel.addDropDownChoice(data, "ganttRelationType", getString("gantt.relationType"), HALF, relationTypeChoice, THREEQUART);
    }
    {
      // Gantt: predecessor offset
      final MinMaxNumberField<Integer> ganttPredecessorField = new MinMaxNumberField<Integer>(TextFieldLPanel.INPUT_ID,
          new PropertyModel<Integer>(data, "ganttPredecessorOffset"), Integer.MIN_VALUE, Integer.MAX_VALUE);
      final String ganttPredecessorString = WicketUtils.getLabelWithUnit(getString("gantt.predecessorOffset"), getString("days"));
      final LabelLPanel label = doPanel.addLabel(ganttPredecessorString, HALF);
      label.setLabelFor(ganttPredecessorField).setBreakBefore();
      doPanel.addTextField(ganttPredecessorString, ganttPredecessorField, QUART);
    }
    {
      // Gantt: predecessor
      final TaskSelectPanel ganttPredecessorSelectPanel = new TaskSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<TaskDO>(data,
          "ganttPredecessor"), taskEditPage, "ganttPredecessorId");
      ganttPredecessorSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
      doPanel.addSelectPanel(getString("gantt.predecessor"), HALF, ganttPredecessorSelectPanel, DOUBLE);
      ganttPredecessorSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
      ganttPredecessorSelectPanel.setShowFavorites(true);
      ganttPredecessorSelectPanel.init();
    }

    // Finance administration group panel
    doPanel.newGroupPanel(getString("financeAdministration"));
    final boolean hasKost2AndTimesheetBookingAccess = taskDao.hasAccessForKost2AndTimesheetBookingStatus(PFUserContext.getUser(), data);
    {
      final String kost2LabelString = getString("fibu.kost2");
      final LabelLPanel label = doPanel.addLabel(kost2LabelString, HALF);
      final PropertyModel<String> model = new PropertyModel<String>(data, "kost2BlackWhiteList");
      kost2BlackWhiteTextField = new MaxLengthTextField(TextFieldOrLabelPanel.INPUT_FIELD_WICKET_ID, kost2LabelString, model);
      final RepeatingView repeatingView = doPanel.addRepeater(LayoutLength.DOUBLE).getRepeatingView();
      final TextFieldOrLabelPanel<String> field = new TextFieldOrLabelPanel<String>(repeatingView.newChildId(), model,
          kost2BlackWhiteTextField, false);
      label.setLabelFor(kost2BlackWhiteTextField).setBreakBefore();
      final ProjektDO projekt = taskDao.getTaskTree().getProjekt(data.getId());
      if (projekt != null) {
        final LabelPanel projektKostLabel = new LabelPanel(repeatingView.newChildId(), projekt.getKost() + ".*");
        WicketUtils.addTooltip(projektKostLabel.getLabel(), new Model<String>() {
          public String getObject()
          {
            final List<Kost2DO> kost2DOs = taskDao.getTaskTree().getKost2List(projekt, data, data.getKost2BlackWhiteItems(),
                data.isKost2IsBlackList());
            final String[] kost2s = TaskListPage.getKost2s(kost2DOs);
            if (kost2s == null || kost2s.length == 0) {
              return " - (-)";
            }
            return " - " + StringHelper.listToString("<br/>", kost2s);
          };
        });
        repeatingView.add(projektKostLabel);
      }
      repeatingView.add(field);
      final Kost2SelectPanel kost2SelectPanel = new Kost2SelectPanel(repeatingView.newChildId(),
          new PropertyModel<Kost2DO>(this, "kost2Id"), taskEditPage, "kost2Id") {
        @Override
        protected void beforeSelectPage(PageParameters parameters)
        {
          super.beforeSelectPage(parameters);
          if (projekt != null) {
            parameters.put(Kost2ListPage.PARAMETER_KEY_STORE_FILTER, false);
            parameters.put(Kost2ListPage.PARAMETER_KEY_SEARCH_STRING, "nummer:" + projekt.getKost() + ".*");
          }
        }
      };
      repeatingView.add(kost2SelectPanel);
      kost2SelectPanel.init();
      final LabelValueChoiceRenderer<Boolean> kost2listTypeChoiceRenderer = new LabelValueChoiceRenderer<Boolean>() //
          .addValue(Boolean.FALSE, getString("task.kost2list.whiteList")) //
          .addValue(Boolean.TRUE, getString("task.kost2list.blackList"));

      final DropDownChoicePanel<Boolean> kost2listTypeChoicePanel = new DropDownChoicePanel<Boolean>(repeatingView.newChildId(),
          new PropertyModel<Boolean>(data, "kost2IsBlackList"), kost2listTypeChoiceRenderer.getValues(), kost2listTypeChoiceRenderer);
      final DropDownChoice<Boolean> kost2listTypeChoice = kost2listTypeChoicePanel.getDropDownChoice();
      kost2listTypeChoice.setNullValid(false);
      repeatingView.add(kost2listTypeChoicePanel);
      if (hasKost2AndTimesheetBookingAccess == false) {
        kost2listTypeChoice.setEnabled(false);
        kost2BlackWhiteTextField.setEnabled(false);
      }
    }
    {
      protectTimesheetsUntilPanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "protectTimesheetsUntil"),
          DatePanelSettings.get().withCallerPage(taskEditPage).withTargetType(java.sql.Date.class).withSelectProperty("protectTimesheetsUntil"));
      doPanel.addDateFieldPanel(data, "protectTimesheetsUntil", getString("task.protectTimesheetsUntil"), HALF,
          protectTimesheetsUntilPanel, FULL);
      if (userGroupCache.isUserMemberOfFinanceGroup() == false) {
        protectTimesheetsUntilPanel.setEnabled(false);
      }
    }
    {
      // Time sheet booking status drop down box:
      final LabelValueChoiceRenderer<TimesheetBookingStatus> timesheetBookingStatusChoiceRenderer = new LabelValueChoiceRenderer<TimesheetBookingStatus>(
          container, TimesheetBookingStatus.values());
      final DropDownChoice<TimesheetBookingStatus> timesheetBookingStatusChoice = new DropDownChoice<TimesheetBookingStatus>(SELECT_ID,
          new PropertyModel(data, "timesheetBookingStatus"), timesheetBookingStatusChoiceRenderer.getValues(),
          timesheetBookingStatusChoiceRenderer);
      timesheetBookingStatusChoice.setNullValid(false);
      doPanel.addDropDownChoice(data, "timesheetBookingStatus", getString("task.timesheetBooking"), HALF, timesheetBookingStatusChoice,
          THREEQUART);
      if (hasKost2AndTimesheetBookingAccess == false) {
        timesheetBookingStatusChoice.setEnabled(false);
      }
    }
  }

  public Integer getKost2Id()
  {
    return kost2Id;
  }

  public void setKost2Id(Integer kost2Id)
  {
    this.kost2Id = kost2Id;
  }
}
