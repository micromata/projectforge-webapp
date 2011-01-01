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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Priority;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.gantt.GanttRelationType;
import org.projectforge.gantt.GanttObjectType;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskStatus;
import org.projectforge.task.TaskTree;
import org.projectforge.task.TimesheetBookingStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.fibu.Kost2ListPage;
import org.projectforge.web.fibu.Kost2SelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.JiraIssuesPanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.TooltipImage;
import org.projectforge.web.wicket.converter.IntegerPercentConverter;

import de.micromata.genome.gwiki.utils.StringUtils;

public class TaskEditForm extends AbstractEditForm<TaskDO, TaskEditPage>
{
  private static final long serialVersionUID = -3784956996856970327L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskEditForm.class);

  public static final BigDecimal MAX_DURATION_DAYS = new BigDecimal(10000);

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  // Unused
  private Integer kost2Id;

  protected DatePanel startDatePanel;

  protected DatePanel endDatePanel;
  
  private TextField<BigDecimal> durationField;

  protected DatePanel protectTimesheetsUntilPanel;

  protected MaxLengthTextField kost2BlackWhiteTextField;

  public TaskEditForm(TaskEditPage parentPage, TaskDO data)
  {
    super(parentPage, data);
    this.colspan = 6;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    final TaskSelectPanel parentTaskSelectPanel = new TaskSelectPanel("parentTask", new PropertyModel<TaskDO>(data, "parentTask"),
        parentPage, "parentTaskId");
    add(parentTaskSelectPanel);
    parentTaskSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
    parentTaskSelectPanel.setShowFavorites(false);
    parentTaskSelectPanel.init();
    if (parentPage.getBaseDao().getTaskTree().isRootNode(getData()) == false) {
      parentTaskSelectPanel.setRequired(true);
    } else {
      parentTaskSelectPanel.setVisible(false);
    }
    final RequiredMaxLengthTextField titleTextField = new RequiredMaxLengthTextField("title", new PropertyModel<String>(data, "title"));
    titleTextField.add(new FocusOnLoadBehavior());
    add(titleTextField);
    add(new MaxLengthTextField("reference", new PropertyModel<String>(data, "reference")));
    {
      // Status drop down box:
      final LabelValueChoiceRenderer<TaskStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<TaskStatus>(this, TaskStatus.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(data, "status"), statusChoiceRenderer.getValues(),
          statusChoiceRenderer);
      statusChoice.setNullValid(false);
      add(statusChoice);
    }
    {
      // Priority drop down box:
      final LabelValueChoiceRenderer<Priority> priorityChoiceRenderer = new LabelValueChoiceRenderer<Priority>(this, Priority.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice priorityChoice = new DropDownChoice("priority", new PropertyModel(data, "priority"), priorityChoiceRenderer
          .getValues(), priorityChoiceRenderer);
      priorityChoice.setNullValid(true);
      add(priorityChoice);
    }
    add(new MaxLengthTextField("shortDescription", new PropertyModel<String>(data, "shortDescription")));
    add(new MaxLengthTextArea("description", new PropertyModel<String>(data, "description")));
    add(WicketUtils.getJIRASupportTooltipImage("jiraSupportTooltipImage1", getResponse(), this));
    add(WicketUtils.getJIRASupportTooltipImage("jiraSupportTooltipImage2", getResponse(), this));
    add(new JiraIssuesPanel("jiraIssues1", data.getShortDescription()));
    add(new JiraIssuesPanel("jiraIssues2", data.getDescription()));
    final MinMaxNumberField<Integer> progressField = new MinMaxNumberField<Integer>("progress",
        new PropertyModel<Integer>(data, "progress"), 0, 100) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerPercentConverter();
      }
    };
    add(progressField);
    add(new MinMaxNumberField<Integer>("maxHours", new PropertyModel<Integer>(data, "maxHours"), 0, 9999));
    final TooltipImage maxHoursExclamationImage = new TooltipImage("maxHoursExclamation", getResponse(), WebConstants.IMAGE_EXCLAMATION,
        getString("task.edit.maxHoursIngoredDueToAssignedOrders"));
    if (isNew() == true || parentPage.getBaseDao().getTaskTree().hasOrderPositions(data.getId(), true) == false) {
      maxHoursExclamationImage.setVisible(false);
    }
    add(maxHoursExclamationImage);
    {
      UserSelectPanel responsibleUserSelectPanel = new UserSelectPanel("responsibleUser", new PropertyModel<PFUserDO>(this,
          "responsibleUser"), parentPage, "responsibleUserId");
      add(responsibleUserSelectPanel);
      responsibleUserSelectPanel.init();
    }
    kost2BlackWhiteTextField = new MaxLengthTextField("kost2BlackWhiteList", new PropertyModel<String>(data, "kost2BlackWhiteList"));
    add(kost2BlackWhiteTextField);
    final ProjektDO projekt = taskTree.getProjekt(data.getId());
    if (projekt != null) {
      final Label projektKostLabel = new Label("projektKost", projekt.getKost() + ".*");
      final TaskDO task = getData();
      WicketUtils.addTooltip(projektKostLabel, new Model<String>() {
        public String getObject()
        {
          final List<Kost2DO> kost2DOs = taskTree.getKost2List(projekt, task, task.getKost2BlackWhiteItems(), task.isKost2IsBlackList());
          final String[] kost2s = TaskListPage.getKost2s(kost2DOs);
          if (kost2s == null || kost2s.length == 0) {
            return " - (-)";
          }
          return " - " + StringHelper.listToString("<br/>", kost2s);
        };
      });
      add(projektKostLabel);
    } else {
      add(createInvisibleDummyComponent("projektKost"));
    }
    final Kost2SelectPanel kost2SelectPanel = new Kost2SelectPanel("kost2", new PropertyModel<Kost2DO>(this, "kost2Id"), parentPage,
        "kost2Id") {
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
    add(kost2SelectPanel);
    kost2SelectPanel.init();
    final LabelValueChoiceRenderer<Boolean> kost2listTypeChoiceRenderer = new LabelValueChoiceRenderer<Boolean>() //
        .addValue(Boolean.FALSE, getString("task.kost2list.whiteList")) //
        .addValue(Boolean.TRUE, getString("task.kost2list.blackList"));
    final boolean hasKost2AndTimesheetBookingAccess = taskDao.hasAccessForKost2AndTimesheetBookingStatus(getData());
    @SuppressWarnings("unchecked")
    final DropDownChoice kost2listTypeChoice = new DropDownChoice("kost2ListType", new PropertyModel(data, "kost2IsBlackList"),
        kost2listTypeChoiceRenderer.getValues(), kost2listTypeChoiceRenderer);
    kost2listTypeChoice.setNullValid(false);
    add(kost2listTypeChoice);
    if (hasKost2AndTimesheetBookingAccess == false) {
      kost2listTypeChoice.setEnabled(false);
      kost2BlackWhiteTextField.setEnabled(false);
    }
    protectTimesheetsUntilPanel = new DatePanel("protectTimesheetsUntil", new PropertyModel<Date>(data, "protectTimesheetsUntil"),
        DatePanelSettings.get().withCallerPage(parentPage).withTargetType(java.sql.Date.class));
    add(protectTimesheetsUntilPanel);
    if (userGroupCache.isUserMemberOfFinanceGroup() == false) {
      protectTimesheetsUntilPanel.setEnabled(false);
    }
    {
      // Booking status drop down box:
      final LabelValueChoiceRenderer<TimesheetBookingStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<TimesheetBookingStatus>(
          this, TimesheetBookingStatus.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice statusChoice = new DropDownChoice("timesheetBookingStatus", new PropertyModel(data, "timesheetBookingStatus"),
          statusChoiceRenderer.getValues(), statusChoiceRenderer);
      statusChoice.setNullValid(false);
      add(statusChoice);
      if (hasKost2AndTimesheetBookingAccess == false) {
        statusChoice.setEnabled(false);
      }
    }
    {
      // Gantt object type:
      final LabelValueChoiceRenderer<GanttObjectType> objectTypeChoiceRenderer = new LabelValueChoiceRenderer<GanttObjectType>(this,
          GanttObjectType.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice objectTypeChoice = new DropDownChoice("ganttObjectType", new PropertyModel(data, "ganttObjectType"),
          objectTypeChoiceRenderer.getValues(), objectTypeChoiceRenderer);
      objectTypeChoice.setNullValid(true);
      add(objectTypeChoice);
    }
    startDatePanel = new DatePanel("startDate", new PropertyModel<Date>(data, "startDate"), DatePanelSettings.get().withCallerPage(
        parentPage));
    add(startDatePanel);
    endDatePanel = new DatePanel("endDate", new PropertyModel<Date>(data, "endDate"), DatePanelSettings.get().withCallerPage(parentPage)
        .withTargetType(java.sql.Date.class));
    add(endDatePanel);
    durationField = new MinMaxNumberField<BigDecimal>("duration", new PropertyModel<BigDecimal>(data, "duration"), BigDecimal.ZERO, MAX_DURATION_DAYS);
    add(durationField);
    {
      // Gantt relation type:
      final LabelValueChoiceRenderer<GanttRelationType> relationTypeChoiceRenderer = new LabelValueChoiceRenderer<GanttRelationType>(
          this, GanttRelationType.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice relationTypeChoice = new DropDownChoice("ganttRelationType", new PropertyModel(data, "ganttRelationType"),
          relationTypeChoiceRenderer.getValues(), relationTypeChoiceRenderer);
      relationTypeChoice.setNullValid(true);
      add(relationTypeChoice);
    }
    add(new MinMaxNumberField<Integer>("ganttPredecessorOffset", new PropertyModel<Integer>(data, "ganttPredecessorOffset"),
        Integer.MIN_VALUE, Integer.MAX_VALUE));
    final TaskSelectPanel ganttPredecessorSelectPanel = new TaskSelectPanel("ganttPredecessor", new PropertyModel<TaskDO>(data,
        "ganttPredecessor"), parentPage, "ganttPredecessorId");
    add(ganttPredecessorSelectPanel);
    ganttPredecessorSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
    ganttPredecessorSelectPanel.setShowFavorites(true);
    ganttPredecessorSelectPanel.init();
  }

  @Override
  protected void validation()
  {
    if (StringUtils.isNotBlank(durationField.getInput()) == true && StringUtils.isNotBlank(endDatePanel.getInput()) == true) {
      addError("gantt.error.durationAndEndDateAreMutuallyExclusive");
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

  public PFUserDO getResponsibleUser()
  {
    return userGroupCache.getUser(data.getResponsibleUserId());
  }

  public void setResponsibleUser(PFUserDO user)
  {
    data.setResponsibleUser(user);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
