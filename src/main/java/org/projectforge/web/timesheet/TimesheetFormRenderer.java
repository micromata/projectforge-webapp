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

import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;
import static org.projectforge.web.wicket.layout.LayoutLength.DOUBLE;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.SelectLPanel.WICKET_ID_SELECT_PANEL;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.hibernate.Hibernate;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
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
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.DropDownChoicePanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.PlainLabel;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.LayoutAlignment;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class TimesheetFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = -9175062586210446142L;

  protected TaskTree taskTree;

  private final TimesheetDao timesheetDao;

  protected UserGroupCache userGroupCache;

  protected UserPrefDao userPrefDao;

  private TimesheetDO data;

  private TimesheetEditPage parentPage;

  private DropDownChoice<Integer> kost2Choice;

  private MarkupContainer kost2Row;

  private List<Kost2DO> kost2List;

  private PFAutoCompleteMaxLengthTextField locationTextField;

  private TextArea<String> descriptionArea;

  protected DateTimePanel startDateTimePanel;

  protected DropDownChoice<Integer> stopHourOfDayDropDownChoice;

  protected Integer stopHourOfDay;

  protected DropDownChoice<Integer> stopMinuteDropDownChoice;

  protected SingleButtonPanel cloneButtonPanel;

  protected Integer stopMinute;

  protected Component consumptionBar;

  @SuppressWarnings("unused")
  private String templateName;

  public TimesheetFormRenderer(final TimesheetEditPage timesheetEditPage, final MarkupContainer container,
      final LayoutContext layoutContext, final TimesheetDao timesheetDao, final TimesheetDO data)
  {
    super(container, layoutContext);
    this.parentPage = timesheetEditPage;
    this.data = data;
    this.timesheetDao = timesheetDao;
  }

  protected void validation()
  {
    updateStopDate();
    if (data.getDuration() < 60000) {
      // Duration is less than 60 seconds.
      startDateTimePanel.error("timesheet.error.zeroDuration");
    } else if (data.getDuration() > TimesheetDao.MAXIMUM_DURATION) {
      startDateTimePanel.error("timesheet.error.maximumDurationExceeded");
    }
    if (kost2Row.isVisible() == false && data.getKost2Id() == null) {
      // Kost2 is not available for current task.
      final TaskNode taskNode = taskTree.getTaskNodeById(data.getTaskId());
      if (taskNode != null) {
        final List<Integer> descendents = taskNode.getDescendantIds();
        for (final Integer taskId : descendents) {
          if (CollectionUtils.isNotEmpty(taskTree.getKost2List(taskId)) == true) {
            // But Kost2 is available for sub task, so user should book his time sheet
            // on a sub task with kost2s.
            kost2Choice.error("timesheet.error.kost2NeededChooseSubTask");
            break;
          }
        }
      }
    }
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
      add(taskSelectPanel);
      taskSelectPanel.setEnableLinks(isNew() == false); // Enable click-able ancestor tasks only for edit mode.
      taskSelectPanel.setTabIndex(1);
      taskSelectPanel.init();
      taskSelectPanel.setRequired(true);
      doPanel.addSelectPanel(getString("task"), HALF, taskSelectPanel, LayoutLength.DOUBLE);
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
      add(userSelectPanel);
      userSelectPanel.init();
      doPanel.addSelectPanel(getString("user"), HALF, userSelectPanel, FULL).setStrong();
    }
    {
      // DropDownChoice templates
      final String label = getString("user.pref.template.select");
      final String[] templateNames = userPrefDao.getPrefNames(UserPrefArea.TIMESHEET_TEMPLATE);
      final LabelValueChoiceRenderer<String> templateNamesChoiceRenderer = new LabelValueChoiceRenderer<String>();
      templateNamesChoiceRenderer.addValue("", label);
      for (final String name : templateNames) {
        templateNamesChoiceRenderer.addValue(name, name);
      }
      final DropDownChoice< ? > templateNamesChoice = new DropDownChoice<String>(SELECT_ID,
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
            kost2Choice.modelChanged();
            locationTextField.modelChanged();
            descriptionArea.modelChanged();
            refresh();
          }
        }
      };
      templateNamesChoice.setNullValid(true);
      if (isNew() == false) {
        templateNamesChoice.setVisible(false);
      }
      doPanel.addDropDownChoice(this, "templateName", label, templateNamesChoice, LayoutLength.FULL).setAlignment(LayoutAlignment.RIGHT);
    }
    {
      final String timePeriodLabel = getString("timePeriod");
      doPanel.addLabel(timePeriodLabel, HALF).setBreakBefore();
      final RepeatingView repeatingView = doPanel.addRepeater(LayoutLength.DOUBLE).getRepeatingView();
      // Start time
      startDateTimePanel = new DateTimePanel(repeatingView.newChildId(), new PropertyModel<Date>(data, "startTime"),
          (DateTimePanelSettings) DateTimePanelSettings.get().withTabIndex(4).withSelectStartStopTime(true).withCallerPage(parentPage)
              .withTargetType(java.sql.Timestamp.class).withRequired(true), DatePrecision.MINUTE_15);
      repeatingView.add(startDateTimePanel);
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
      repeatingView.add(new PlainLabel(repeatingView.newChildId(), getString("until")));
      // Stop time
      final DropDownChoicePanel<Integer> stopHourOfDayDropDownChoicePanel = new DropDownChoicePanel<Integer>(repeatingView.newChildId(),
          new PropertyModel<Integer>(this, "stopHourOfDay"), DateTimePanel.getHourOfDayRenderer().getValues(), DateTimePanel
              .getHourOfDayRenderer());
      stopHourOfDayDropDownChoice = stopHourOfDayDropDownChoicePanel.getDropDownChoice();
      stopHourOfDayDropDownChoice.setNullValid(false);
      stopHourOfDayDropDownChoice.setRequired(true);
      repeatingView.add(stopHourOfDayDropDownChoicePanel);
      repeatingView.add(new PlainLabel(repeatingView.newChildId(), " : "));
      final DropDownChoicePanel<Integer> stopMinuteDropDownChoicePanel = new DropDownChoicePanel<Integer>(repeatingView.newChildId(),
          new PropertyModel<Integer>(this, "stopMinute"), DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15).getValues(),
          DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15));
      stopMinuteDropDownChoice = stopMinuteDropDownChoicePanel.getDropDownChoice();
      stopMinuteDropDownChoice.setNullValid(false);
      stopMinuteDropDownChoice.setRequired(true);
      repeatingView.add(stopMinuteDropDownChoicePanel);

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
      doPanel.addTextField(getString("timesheet.location"), HALF, locationTextField, DOUBLE);
      doPanel.addTextArea(data, "description", getString("description") + " (JIRA)", HALF, DOUBLE, false).setCssStyle("height: 20em;");

    }
  }

  protected void refresh()
  {
    if (kost2Choice == null) {
      // Not yet initialized, no refresh needed.
      return;
    }
    // kost2List = taskTree.getKost2List(data.getTaskId());
    // final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
    // kost2Choice.setChoiceRenderer(kost2ChoiceRenderer);
    // kost2Choice.setChoices(kost2ChoiceRenderer.getValues());
    // remove(consumptionBar);
    // addConsumptionBar();
  }
}
