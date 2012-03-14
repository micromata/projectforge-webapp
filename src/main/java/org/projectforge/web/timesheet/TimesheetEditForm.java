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

package org.projectforge.web.timesheet;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.hibernate.Hibernate;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.core.SystemInfoCache;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.Kost2DO;
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
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.ConsumptionBarPanel;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DropDownChoicePanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;

public class TimesheetEditForm extends AbstractEditForm<TimesheetDO, TimesheetEditPage>
{
  private static final long serialVersionUID = 3150725003240437752L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimesheetEditForm.class);

  private static final String RECENT_SHEETS_DIALOG_ID = "recentSheetsModalWindow";

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userPrefDao")
  private UserPrefDao userPrefDao;

  UserSelectPanel userSelectPanel;

  PFAutoCompleteMaxLengthTextField locationTextField;

  TextArea<String> descriptionArea;

  DropDownChoicePanel<Integer> cost2ChoicePanel;

  private FieldsetPanel cost2ChoiceFieldset;

  private ConsumptionBarPanel consumptionBarPanel;

  private List<Kost2DO> cost2List;

  protected Boolean saveAsTemplate;

  @SuppressWarnings("unused")
  private Integer stopHourOfDay, stopMinute;

  @SuppressWarnings("unused")
  private String templateName, consumptionBarId;

  // Components for form validation.
  private final FormComponent< ? >[] dependentFormComponents = new FormComponent[4];

  private ModalWindow recentSheetsModalWindow;

  public TimesheetEditForm(final TimesheetEditPage parentPage, final TimesheetDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new IFormValidator() {
      @Override
      public FormComponent< ? >[] getDependentFormComponents()
      {
        return dependentFormComponents;
      }

      @SuppressWarnings("unchecked")
      @Override
      public void validate(final Form< ? > form)
      {
        final DateTimePanel startDateTimePanel = (DateTimePanel) dependentFormComponents[0];
        final DropDownChoice<Integer> stopHourOfDayDropDownChoice = (DropDownChoice<Integer>) dependentFormComponents[1];
        final DropDownChoice<Integer> stopMinuteDropDownChoice = (DropDownChoice<Integer>) dependentFormComponents[2];
        final DropDownChoice<Integer> cost2Choice = (DropDownChoice<Integer>) dependentFormComponents[3];
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
        if (data.getDuration() < 60000) {
          // Duration is less than 60 seconds.
          stopMinuteDropDownChoice.error(getString("timesheet.error.zeroDuration"));
        } else if (data.getDuration() > TimesheetDao.MAXIMUM_DURATION) {
          stopMinuteDropDownChoice.error(getString("timesheet.error.maximumDurationExceeded"));
        }
        if (cost2Choice != null && cost2Choice.getConvertedInput() == null) {
          // cost2Choice is always != null (but may-be invisible) if cost2 entries does exist in the system.
          // Kost2 is not available for current task.
          final TaskNode taskNode = taskTree.getTaskNodeById(data.getTaskId());
          if (taskNode != null) {
            final List<Integer> descendents = taskNode.getDescendantIds();
            for (final Integer taskId : descendents) {
              if (CollectionUtils.isNotEmpty(taskTree.getKost2List(taskId)) == true) {
                // But Kost2 is available for sub task, so user should book his time sheet
                // on a sub task with kost2s.
                if (cost2Choice.isVisible()) {
                  cost2Choice.error(getString("timesheet.error.kost2NeededChooseSubTask"));
                } else {
                  error(getString("timesheet.error.kost2NeededChooseSubTask"));
                }
                break;
              }
            }
          }
        }
      }
    });
    parentPage.preInit();
    gridBuilder.newGrid16();
    if (isNew() == true) {
      addTemplatesRow();
    } else {
      add(new Label(RECENT_SHEETS_DIALOG_ID, "invisible").setVisible(false));
    }
    {
      // Task
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("task"));
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(fs.newChildId(), new PropertyModel<TaskDO>(data, "task"), parentPage,
          "taskId") {
        @Override
        protected void selectTask(final TaskDO task)
        {
          super.selectTask(task);
          refresh(); // Task was changed. Therefore update the kost2 list.
        }
      };
      fs.add(taskSelectPanel);
      taskSelectPanel.init();
      taskSelectPanel.setRequired(true);
    }
    {
    }
    if (SystemInfoCache.instance().isCost2EntriesExists() == true) {
      // Cost 2 entries does exist in the data-base.
      cost2ChoiceFieldset = gridBuilder.newFieldset(getString("fibu.kost2"));
      cost2List = taskTree.getKost2List(data.getTaskId());
      final LabelValueChoiceRenderer<Integer> cost2ChoiceRenderer = getCost2LabelValueChoiceRenderer(parentPage.getBaseDao(), cost2List,
          data, null);
      final DropDownChoice<Integer> cost2Choice = createCost2ChoiceRenderer(cost2ChoiceFieldset.getDropDownChoiceId(), parentPage.getBaseDao(),
          taskTree, cost2ChoiceRenderer, data, cost2List);
      cost2Choice.setRequired(true);
      cost2ChoicePanel = cost2ChoiceFieldset.add(cost2Choice);
      dependentFormComponents[3] = cost2Choice;
      updateCost2ChoiceVisibility();
    }
    {
      // User
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("user"));
      PFUserDO user = data.getUser();
      if (Hibernate.isInitialized(user) == false) {
        user = userGroupCache.getUser(user.getId());
        data.setUser(user);
      }
      userSelectPanel = new UserSelectPanel(fs.newChildId(), new PropertyModel<PFUserDO>(data, "user"), parentPage, "userId");
      userSelectPanel.setRequired(true);
      fs.add(userSelectPanel);
      userSelectPanel.init();
    }
    {
      // Time period
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("timePeriod"), true);
      final DateTimePanel startDateTimePanel = new DateTimePanel(fs.newChildId(), new PropertyModel<Date>(data, "startTime"),
          (DateTimePanelSettings) DateTimePanelSettings.get().withSelectStartStopTime(true).withTargetType(java.sql.Timestamp.class)
          .withRequired(true), DatePrecision.MINUTE_15);
      dependentFormComponents[0] = startDateTimePanel;
      fs.add(startDateTimePanel);
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
      fs.add(new DivTextPanel(fs.newChildId(), getString("until")));
      // Stop time
      final DropDownChoice<Integer> stopHourOfDayDropDownChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(),
          new PropertyModel<Integer>(this, "stopHourOfDay"), DateTimePanel.getHourOfDayRenderer().getValues(),
          DateTimePanel.getHourOfDayRenderer());
      stopHourOfDayDropDownChoice.setNullValid(false);
      stopHourOfDayDropDownChoice.setRequired(true);
      fs.add(stopHourOfDayDropDownChoice);
      dependentFormComponents[1] = stopHourOfDayDropDownChoice;
      final DropDownChoice<Integer> stopMinuteDropDownChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(),
          new PropertyModel<Integer>(this, "stopMinute"), DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15).getValues(),
          DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15));
      stopMinuteDropDownChoice.setNullValid(false);
      stopMinuteDropDownChoice.setRequired(true);
      fs.add(stopMinuteDropDownChoice);
      dependentFormComponents[2] = stopMinuteDropDownChoice;
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("task.consumption"), true).setNoLabelFor();
      consumptionBarId = fs.newChildId();
      fs.add(getConsumptionBar());
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        /**
         * @see org.apache.wicket.model.Model#getObject()
         */
        @Override
        public String getObject()
        {
          return consumptionBarPanel.getTooltip();
        }
      }));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("timesheet.location"), true);
      locationTextField = new PFAutoCompleteMaxLengthTextField(InputPanel.WICKET_ID, new PropertyModel<String>(data, "location")) {
        @Override
        protected List<String> getChoices(final String input)
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
      fs.add(locationTextField);
      fs.addKeyboardHelpIcon(getString("tooltip.autocomplete.withDblClickFunction"));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("timesheet.description"), true);
      final IModel<String> model = new PropertyModel<String>(data, "description");
      fs.add(descriptionArea = new MaxLengthTextArea(TextAreaPanel.WICKET_ID, model));
      fs.addJIRAField(model);
    }
    {
      // Save as template checkbox:
      final FieldsetPanel fs = gridBuilder.newFieldset("").setNoLabelFor();
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.addCheckBox(new PropertyModel<Boolean>(this, "saveAsTemplate"), getString("user.pref.saveAsTemplate"));
    }
    if (isNew() == false) {
      // Clone button for existing and not deleted invoices:
      final Button cloneButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("clone")) {
        @Override
        public final void onSubmit()
        {
          parentPage.cloneTimesheet();
        }
      };
      cloneButton.setDefaultFormProcessing(false);
      final SingleButtonPanel cloneButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), cloneButton, getString("clone")) {
        /**
         * @see org.apache.wicket.Component#isVisible()
         */
        @Override
        public boolean isVisible()
        {
          return isNew() == false;
        }
      };
      actionButtons.add(2, cloneButtonPanel);
    }
  }

  @SuppressWarnings("serial")
  private void addTemplatesRow()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("templates"), true);
    final String[] templateNames = userPrefDao.getPrefNames(UserPrefArea.TIMESHEET_TEMPLATE);
    if (templateNames != null && templateNames.length > 0) {
      // DropDownChoice templates
      final String label = getString("user.pref.template.select");
      final LabelValueChoiceRenderer<String> templateNamesChoiceRenderer = new LabelValueChoiceRenderer<String>();
      templateNamesChoiceRenderer.addValue("", label);
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

        /**
         * @see org.apache.wicket.markup.html.form.AbstractSingleSelectChoice#getDefaultChoice(java.lang.String)
         */
        @Override
        protected CharSequence getDefaultChoice(final String selectedValue)
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
            if (cost2ChoicePanel != null) {
              cost2ChoicePanel.getDropDownChoice().modelChanged();
            }
            locationTextField.modelChanged();
            descriptionArea.modelChanged();
            refresh();
          }
        }
      };
      templateNamesChoice.setNullValid(true);
      fs.add(templateNamesChoice);
    }
    final AjaxSubmitLink link = new AjaxSubmitLink(IconLinkPanel.LINK_ID) {
      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        final TimesheetEditSelectRecentDialogPanel dialog = new TimesheetEditSelectRecentDialogPanel(recentSheetsModalWindow,
            getString("timesheet.recent.select"), parentPage, TimesheetEditForm.this, cost2ChoiceFieldset != null, timesheetDao, taskTree,
            userFormatter);
        dialog.show(target);
      }

      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
      }
    };
    link.setDefaultFormProcessing(false);
    fs.add(new IconLinkPanel(fs.newChildId(), IconType.FOLDER_OPEN, getString("timesheet.recent.select"), link));
    recentSheetsModalWindow = new ModalWindow(RECENT_SHEETS_DIALOG_ID);
    add(recentSheetsModalWindow);
  }

  private void updateCost2ChoiceVisibility()
  {
    final boolean cost2Visible = CollectionUtils.isNotEmpty(cost2List);
    cost2ChoiceFieldset.setVisible(cost2Visible);
  }

  @SuppressWarnings("serial")
  protected static DropDownChoice<Integer> createCost2ChoiceRenderer(final String id, final TimesheetDao timesheetDao,
      final TaskTree taskTree, final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer, final TimesheetDO data,
      final List<Kost2DO> kost2List)
      {
    final DropDownChoice<Integer> choice = new DropDownChoice<Integer>(id, new Model<Integer>() {
      @Override
      public Integer getObject()
      {
        return data.getKost2Id();
      }

      @Override
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
      protected void onValidate(final IValidatable<Integer> validatable)
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

  /**
   * Used also by TimesheetMassUpdateForm.
   * @param timesheetDao
   * @param kost2List
   * @param data
   * @param kost2Choice
   * @return
   */
  protected static LabelValueChoiceRenderer<Integer> getCost2LabelValueChoiceRenderer(final TimesheetDao timesheetDao,
      final List<Kost2DO> kost2List, final TimesheetDO data, final DropDownChoice<Integer> kost2Choice)
      {
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    if (kost2List != null && kost2List.size() == 1) {
      // Es ist genau ein Eintrag. Deshalb selektieren wir diesen auch:
      final Integer kost2Id = kost2List.get(0).getId();
      timesheetDao.setKost2(data, kost2Id);
      if (kost2Choice != null) {
        kost2Choice.modelChanged();
      }
    }
    if (CollectionUtils.isEmpty(kost2List) == true) {
      data.setKost2(null); // No kost2 list given, therefore set also kost2 to null.
    } else {
      for (final Kost2DO kost2 : kost2List) {
        kost2ChoiceRenderer.addValue(kost2.getId(), KostFormatter.formatForSelection(kost2));
      }
    }
    return kost2ChoiceRenderer;
      }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    final DateHolder stopDateHolder = new DateHolder(data.getStopTime(), DatePrecision.MINUTE_15);
    stopHourOfDay = stopDateHolder.getHourOfDay();
    stopMinute = stopDateHolder.getMinute();
  }

  protected void refresh()
  {
    if (cost2ChoicePanel != null) {
      cost2List = taskTree.getKost2List(data.getTaskId());
      final LabelValueChoiceRenderer<Integer> cost2ChoiceRenderer = getCost2LabelValueChoiceRenderer(parentPage.getBaseDao(), cost2List,
          data, null);
      cost2ChoicePanel.getDropDownChoice().setChoiceRenderer(cost2ChoiceRenderer);
      cost2ChoicePanel.getDropDownChoice().setChoices(cost2ChoiceRenderer.getValues());
      updateCost2ChoiceVisibility();
    }
    consumptionBarPanel.replaceWith(getConsumptionBar());
  }

  protected ConsumptionBarPanel getConsumptionBar()
  {
    final Integer taskId = data.getTaskId();
    TaskNode node = taskId != null ? taskTree.getTaskNodeById(taskId) : null;
    if (node != null) {
      final TaskNode personDaysNode = taskTree.getPersonDaysNode(node);
      if (personDaysNode != null) {
        node = personDaysNode;
      }
    }
    consumptionBarPanel = TaskListPage.getConsumptionBarPanel(this.parentPage, consumptionBarId, taskTree, false, node);
    consumptionBarPanel.setRenderBodyOnly(true);
    return consumptionBarPanel;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
