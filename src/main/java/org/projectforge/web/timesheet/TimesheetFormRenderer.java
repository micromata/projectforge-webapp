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

package org.projectforge.web.timesheet;

import static org.projectforge.web.wicket.layout.LayoutLength.DOUBLE;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.SelectLPanel.WICKET_ID_SELECT_PANEL;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.hibernate.Hibernate;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.core.Configuration;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.jira.JiraUtils;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserPrefArea;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefDao;
import org.projectforge.web.task.TaskListPage;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.CheckBoxPanel;
import org.projectforge.web.wicket.components.ConsumptionBarPanel;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.DropDownChoicePanel;
import org.projectforge.web.wicket.components.LabelPanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.PlainLabel;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.ContainerLPanel;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.IField;
import org.projectforge.web.wicket.layout.LabelLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.RepeatingViewLPanel;
import org.projectforge.web.wicket.layout.TextAreaLPanel;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class TimesheetFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = -9175062586210446142L;

  protected TaskTree taskTree;

  protected UserGroupCache userGroupCache;

  protected UserPrefDao userPrefDao;

  protected UserFormatter userFormatter;

  private TimesheetDO data;

  private TimesheetEditPage parentPage;

  private PFAutoCompleteMaxLengthTextField locationTextField;

  private TextArea< ? > descriptionArea;

  protected DateTimePanel startDateTimePanel;

  protected DropDownChoice<Integer> stopHourOfDayDropDownChoice;

  protected Integer stopHourOfDay;

  protected DropDownChoice<Integer> stopMinuteDropDownChoice;

  protected Integer stopMinute;

  private List<Kost2DO> cost2List;

  protected DropDownChoiceLPanel cost2ChoicePanel;

  protected LabelLPanel cost2ChoiceLabel;

  private DropDownChoice<Integer> cost2Choice;

  protected LabelLPanel templatesLabel;

  protected RepeatingViewLPanel templatesPanel;

  protected ContainerLPanel consumptionBarPanel;

  protected Boolean saveAsTemplate;

  @SuppressWarnings("unused")
  private String templateName;

  public TimesheetFormRenderer(final TimesheetEditPage timesheetEditPage, final MarkupContainer container,
      final LayoutContext layoutContext, final TimesheetDO data)
  {
    super(container, layoutContext);
    this.parentPage = timesheetEditPage;
    this.data = data;
  }

  protected void validation()
  {
    updateStopDate();
    if (data.getDuration() < 60000) {
      // Duration is less than 60 seconds.
      startDateTimePanel.error(getString("timesheet.error.zeroDuration"));
    } else if (data.getDuration() > TimesheetDao.MAXIMUM_DURATION) {
      startDateTimePanel.error(getString("timesheet.error.maximumDurationExceeded"));
    }
    if (isCost2Visible() == false && data.getKost2Id() == null) {
      // Kost2 is not available for current task.
      final TaskNode taskNode = taskTree.getTaskNodeById(data.getTaskId());
      if (taskNode != null) {
        final List<Integer> descendents = taskNode.getDescendantIds();
        for (final Integer taskId : descendents) {
          if (CollectionUtils.isNotEmpty(taskTree.getKost2List(taskId)) == true) {
            // But Kost2 is available for sub task, so user should book his time sheet
            // on a sub task with kost2s.
            cost2Choice.error(getString("timesheet.error.kost2NeededChooseSubTask"));
            break;
          }
        }
      }
    }
  }

  private boolean isCost2Visible()
  {
    return CollectionUtils.isNotEmpty(cost2List);
  }

  protected void updateStopDate()
  {
    startDateTimePanel.validate();
    stopHourOfDayDropDownChoice.validate();
    stopMinuteDropDownChoice.validate();
    final DateHolder startDate = new DateHolder(startDateTimePanel.getConvertedInput());
    final DateHolder stopDate = new DateHolder(startDate.getTimestamp());
    stopDate.setHourOfDay(stopHourOfDayDropDownChoice.getConvertedInput());
    stopDate.setMinute(stopMinuteDropDownChoice.getConvertedInput());
    if (stopDate.getTimeOfDay() < startDate.getTimeOfDay()) { // Stop time is
      // before start time. Assuming next day for stop time:
      stopDate.add(Calendar.DAY_OF_MONTH, 1);
    }
    data.setStartTime(startDate.getTimestamp());
    data.setStopTime(stopDate.getTimestamp());
  }

  protected void onBeforeRender()
  {
    final DateHolder stopDateHolder = new DateHolder(data.getStopTime(), DatePrecision.MINUTE_15);
    stopHourOfDay = stopDateHolder.getHourOfDay();
    stopMinute = stopDateHolder.getMinute();
  }

  @SuppressWarnings("serial")
  @Override
  public void add()
  {
    addTemplatesRow();
    {
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<TaskDO>(data, "task"),
          parentPage, "taskId") {
        @Override
        protected void selectTask(final TaskDO task)
        {
          super.selectTask(task);
          refresh(); // Task was changed. Therefore update the kost2 list.
        }
      };
      taskSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
      taskSelectPanel.setTabIndex(1);
      doPanel.addSelectPanel(getString("task"), HALF, taskSelectPanel, DOUBLE);
      taskSelectPanel.init();
      taskSelectPanel.setRequired(true);
    }
    {
      final String label = getString("fibu.kost2");
      cost2ChoiceLabel = doPanel.addLabel(label, HALF);
      cost2ChoiceLabel.setBreakBefore();
      final IField field = doPanel.addDropDownChoice(data, "kost2", label, null, FULL);
      if (field instanceof DropDownChoiceLPanel) {
        cost2ChoicePanel = (DropDownChoiceLPanel) field;
      }
    }
    {
      PFUserDO user = data.getUser();
      if (Hibernate.isInitialized(user) == false) {
        user = userGroupCache.getUser(user.getId());
        data.setUser(user);
      }
      final UserSelectPanel userSelectPanel = new UserSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<PFUserDO>(data, "user"),
          parentPage, "userId");
      userSelectPanel.setRequired(true);
      doPanel.addSelectPanel(getString("user"), HALF, userSelectPanel, FULL).setStrong();
      userSelectPanel.init();
    }
    {
      final String timePeriodLabel = getString("timePeriod");
      final LabelLPanel label = doPanel.addLabel(timePeriodLabel, HALF);
      final RepeatingView repeatingView = doPanel.addRepeater(LayoutLength.DOUBLE).getRepeatingView();
      // Start time
      startDateTimePanel = new DateTimePanel(repeatingView.newChildId(), new PropertyModel<Date>(data, "startTime"),
          (DateTimePanelSettings) DateTimePanelSettings.get().withTabIndex(4).withSelectStartStopTime(true).withCallerPage(parentPage)
              .withTargetType(java.sql.Timestamp.class).withRequired(true), DatePrecision.MINUTE_15);
      repeatingView.add(startDateTimePanel);
      label.setLabelFor(startDateTimePanel.getDateField()).setBreakBefore();
      WicketUtils.addTooltip(startDateTimePanel.getDateField(), new Model<String>() {
        @Override
        public String getObject()
        {
          final StringBuffer buf = new StringBuffer();
          if (data.getStartTime() != null) {
            buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(data.getStartTime()));
            if (data.getStopTime() != null) {
              buf.append(" - ");
            }
          }
          if (data.getStopTime() != null) {
            buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(data.getStopTime()));
          }
          return buf.toString();
        }
      });
      repeatingView.add(new LabelPanel(repeatingView.newChildId(), getString("until")));
      // Stop time
      final DropDownChoicePanel<Integer> stopHourOfDayDropDownChoicePanel = new DropDownChoicePanel<Integer>(repeatingView.newChildId(),
          new PropertyModel<Integer>(this, "stopHourOfDay"), DateTimePanel.getHourOfDayRenderer().getValues(), DateTimePanel
              .getHourOfDayRenderer());
      stopHourOfDayDropDownChoice = stopHourOfDayDropDownChoicePanel.getDropDownChoice();
      stopHourOfDayDropDownChoice.setNullValid(false);
      stopHourOfDayDropDownChoice.setRequired(true);
      repeatingView.add(stopHourOfDayDropDownChoicePanel);
      repeatingView.add(new LabelPanel(repeatingView.newChildId(), ":"));
      final DropDownChoicePanel<Integer> stopMinuteDropDownChoicePanel = new DropDownChoicePanel<Integer>(repeatingView.newChildId(),
          new PropertyModel<Integer>(this, "stopMinute"), DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15).getValues(),
          DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15));
      stopMinuteDropDownChoice = stopMinuteDropDownChoicePanel.getDropDownChoice();
      stopMinuteDropDownChoice.setNullValid(false);
      stopMinuteDropDownChoice.setRequired(true);
      repeatingView.add(stopMinuteDropDownChoicePanel);
    }
    {
      final WebMarkupContainer dummy = (WebMarkupContainer) new WebMarkupContainer(ContainerLPanel.WICKET_ID).setVisible(false);
      consumptionBarPanel = doPanel.addContainer(getString("task.consumption"), HALF, dummy, FULL);
    }
    {
      locationTextField = new PFAutoCompleteMaxLengthTextField(TextFieldLPanel.INPUT_ID, new PropertyModel<String>(data, "location")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return parentPage.getBaseDao().getLocationAutocompletion(input);
        }

        @Override
        protected List<String> getFavorites()
        {
          return parentPage.getRecentLocations();
        }
      };
      locationTextField.withMatchContains(true).withMinChars(2).withFocus(true);
      WicketUtils.addTooltip(locationTextField, getString("tooltip.autocomplete.withDblClickFunction"));
      doPanel.addTextField(getString("timesheet.location"), HALF, locationTextField, DOUBLE);
    }
    final boolean jiraSupport = Configuration.getInstance().isJIRAConfigured();
    {
      final String jiraFootnoteMark = jiraSupport ? "*" : "";
      final IField field = doPanel.addTextArea(data, "description", getString("timesheet.description") + jiraFootnoteMark, HALF, DOUBLE,
          false).setCssStyle("height: 20em;");
      if (field instanceof TextAreaLPanel) {
        descriptionArea = ((TextAreaLPanel) field).getTextArea();
      }
      if (jiraSupport == true && JiraUtils.hasJiraIssues(data.getDescription()) == true) {
        doPanel.addLabel("", HALF).setBreakBefore();
        doPanel.addJiraIssuesPanel(DOUBLE, data.getDescription());
      }
    }
    {
      // Save as template checkbox:
      doPanel.addLabel("", HALF).setBreakBefore();
      final RepeatingView repeatingView = doPanel.addRepeater(LayoutLength.DOUBLE).getRepeatingView();
      final CheckBoxPanel checkBoxPanel = new CheckBoxPanel(repeatingView.newChildId(), new PropertyModel<Boolean>(this, "saveAsTemplate"));
      final PlainLabel label = new PlainLabel(repeatingView.newChildId(), getString("user.pref.saveAsTemplate"));
      WicketUtils.setLabel(checkBoxPanel.getCheckBox(), label);
      repeatingView.add(label);
      repeatingView.add(checkBoxPanel);
    }

    if (jiraSupport == true) {
      // Add help text:
      doPanel.addLabel("", HALF).setBreakBefore();
      doPanel.addHelpLabel("*) " + getString("tooltip.jiraSupport.field"), DOUBLE);
    }
  }

  @SuppressWarnings("serial")
  private void addTemplatesRow()
  {
    templatesLabel = doPanel.addLabel(getString("templates"), HALF);
    templatesPanel = doPanel.addRepeater(DOUBLE);
    if (isNew() == false) {
      templatesLabel.setVisible(false);
      templatesPanel.setVisible(false);
      return;
    }
    final RepeatingView repeatingView = templatesPanel.getRepeatingView();
    final String[] templateNames = userPrefDao.getPrefNames(UserPrefArea.TIMESHEET_TEMPLATE);
    if (templateNames != null && templateNames.length > 0) {
      // DropDownChoice templates
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
        protected CharSequence getDefaultChoice(Object selected)
        {
          return "";
        }

        @Override
        protected void onSelectionChanged(final String newSelection)
        {
          if (StringUtils.isNotEmpty(newSelection) == true) {
            // Fill fields with selected template values:
            final UserPrefDO userPref = userPrefDao.getUserPref(UserPrefArea.TIMESHEET_TEMPLATE, newSelection);
            if (userPref != null) {
              userPrefDao.fillFromUserPrefParameters(userPref, data);
            }
            templateName = "";
            cost2Choice.modelChanged();
            locationTextField.modelChanged();
            descriptionArea.modelChanged();
            refresh();
          }
        }
      };
      templateNamesChoice.setNullValid(true);
      repeatingView.add(new DropDownChoicePanel<String>(repeatingView.newChildId(), templateNamesChoice));
    }

  }

  protected void refresh()
  {
    addConsumptionBar();
    cost2List = taskTree.getKost2List(data.getTaskId());
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
    cost2Choice.setChoiceRenderer(kost2ChoiceRenderer);
    cost2Choice.setChoices(kost2ChoiceRenderer.getValues());
    cost2ChoicePanel.replaceWithDropDownChoice(cost2Choice);
    final boolean cost2Visible = isCost2Visible();
    cost2ChoiceLabel.setVisible(cost2Visible);
    cost2ChoicePanel.setVisible(cost2Visible);
  }

  protected void addKost2Row()
  {
    cost2List = taskTree.getKost2List(data.getTaskId());
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
    cost2Choice = createKost2ChoiceRenderer(DropDownChoiceLPanel.SELECT_ID, parentPage.getBaseDao(), taskTree, kost2ChoiceRenderer, data,
        cost2List);
    cost2Choice.setRequired(true);
    cost2ChoicePanel.replaceWithDropDownChoice(cost2Choice);
  }

  @SuppressWarnings("serial")
  protected static DropDownChoice<Integer> createKost2ChoiceRenderer(final String id, final TimesheetDao timesheetDao,
      final TaskTree taskTree, final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer, final TimesheetDO data,
      final List<Kost2DO> kost2List)
  {
    final DropDownChoice<Integer> choice = new DropDownChoice<Integer>(id, new Model<Integer>() {
      public Integer getObject()
      {
        return data.getKost2Id();
      }

      public void setObject(final Integer kost2Id)
      {
        if (kost2Id != null) {
          timesheetDao.setKost2(data, kost2Id);
        } else {
          data.setKost2(null);
        }
      }
    }, kost2ChoiceRenderer.getValues(), kost2ChoiceRenderer);
    choice.setNullValid(true);
    choice.add(new AbstractValidator<Integer>() {
      @Override
      protected void onValidate(IValidatable<Integer> validatable)
      {
        final Integer value = validatable.getValue();
        if (value != null && value >= 0) {
          return;
        }
        if (CollectionUtils.isNotEmpty(kost2List) == true) {
          // Kost2 available but not selected.
          error(validatable);
        }
      }

      @Override
      protected String resourceKey()
      {
        return "timesheet.error.kost2Required";
      }
    });
    return choice;
  }

  protected void addConsumptionBar()
  {
    final Integer taskId = data.getTaskId();
    TaskNode node = taskId != null ? taskTree.getTaskNodeById(taskId) : null;
    if (node != null) {
      final TaskNode personDaysNode = taskTree.getPersonDaysNode(node);
      if (personDaysNode != null) {
        node = personDaysNode;
      }
    }
    final ConsumptionBarPanel consumptionBar = TaskListPage.getConsumptionBarPanel(this.parentPage, ContainerLPanel.WICKET_ID, taskTree,
        false, node);
    consumptionBar.setRenderBodyOnly(true);
    consumptionBarPanel.replaceWithContainer(consumptionBar);
  }

  private LabelValueChoiceRenderer<Integer> getKost2LabelValueChoiceRenderer()
  {
    return getKost2LabelValueChoiceRenderer(parentPage.getBaseDao(), cost2List, data, cost2Choice);
  }

  protected static LabelValueChoiceRenderer<Integer> getKost2LabelValueChoiceRenderer(final TimesheetDao timesheetDao,
      final List<Kost2DO> kost2List, final TimesheetDO data, final DropDownChoice<Integer> kost2Choice)
  {
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    if (kost2List != null && kost2List.size() == 1) {
      // Es ist genau ein Eintrag. Deshalb selektieren wir diesen auch:
      Integer kost2Id = kost2List.get(0).getId();
      timesheetDao.setKost2(data, kost2Id);
      if (kost2Choice != null) {
        kost2Choice.modelChanged();
      }
    }
    if (CollectionUtils.isEmpty(kost2List) == true) {
      data.setKost2(null); // No kost2 list given, therefore set also kost2 to null.
    } else {
      for (Kost2DO kost2 : kost2List) {
        kost2ChoiceRenderer.addValue(kost2.getId(), KostFormatter.formatForSelection(kost2));
      }
    }
    return kost2ChoiceRenderer;
  }
}
