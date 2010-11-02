/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.hibernate.Hibernate;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserPrefArea;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefDao;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.task.TaskListPage;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DatesAsUTCLabel;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketLocalizerAndUrlBuilder;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.ConsumptionBarPanel;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.JiraIssuesPanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TooltipImage;

public class TimesheetEditForm extends AbstractEditForm<TimesheetDO, TimesheetEditPage>
{
  private static final long serialVersionUID = 3150725003240437752L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimesheetEditForm.class);

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userPrefDao")
  private UserPrefDao userPrefDao;

  protected Boolean saveAsTemplate;

  @SuppressWarnings("unused")
  private String templateName;

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

  public TimesheetEditForm(final TimesheetEditPage parentPage, final TimesheetDO data)
  {
    super(parentPage, data);
    this.colspan = 4;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new TooltipImage("autocompleteDblClickHelpImage", getResponse(), WebConstants.IMAGE_HELP_KEYBOARD,
        getString("tooltip.autocomplete.withDblClickFunction")));
    final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("task", new PropertyModel<TaskDO>(data, "task"), parentPage, "taskId") {
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
    {
      PFUserDO user = data.getUser();
      if (Hibernate.isInitialized(user) == false) {
        user = userGroupCache.getUser(user.getId());
        data.setUser(user);
      }
      UserSelectPanel userSelectPanel = new UserSelectPanel("user", new PropertyModel<PFUserDO>(data, "user"), parentPage, "userId");
      userSelectPanel.setRequired(true);
      add(userSelectPanel);
      userSelectPanel.init();
    }
    {
      // DropDownChoice templates
      final String[] templateNames = userPrefDao.getPrefNames(UserPrefArea.TIMESHEET_TEMPLATE);
      final LabelValueChoiceRenderer<String> templateNamesChoiceRenderer = new LabelValueChoiceRenderer<String>();
      templateNamesChoiceRenderer.addValue("", getString("user.pref.template.select"));
      for (final String name : templateNames) {
        templateNamesChoiceRenderer.addValue(name, name);
      }
      final DropDownChoice< ? > templateNamesChoice = new DropDownChoice<String>("template",
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
      add(templateNamesChoice);
    }
    // Start time
    startDateTimePanel = new DateTimePanel("startDateTime", new PropertyModel<Date>(data, "startTime"),
        (DateTimePanelSettings) DateTimePanelSettings.get().withTabIndex(4).withSelectStartStopTime(true).withCallerPage(parentPage)
            .withTargetType(java.sql.Timestamp.class).withRequired(true), DatePrecision.MINUTE_15);
    add(startDateTimePanel);
    // Stop time
    stopHourOfDayDropDownChoice = new DropDownChoice<Integer>("stopHourOfDay", new PropertyModel<Integer>(this, "stopHourOfDay"),
        DateTimePanel.getHourOfDayRenderer().getValues(), DateTimePanel.getHourOfDayRenderer());
    stopHourOfDayDropDownChoice.setNullValid(false);
    stopHourOfDayDropDownChoice.setRequired(true);
    add(stopHourOfDayDropDownChoice);
    stopMinuteDropDownChoice = new DropDownChoice<Integer>("stopMinute", new PropertyModel<Integer>(this, "stopMinute"), DateTimePanel
        .getMinutesRenderer(DatePrecision.MINUTE_15).getValues(), DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15));
    stopMinuteDropDownChoice.setNullValid(false);
    stopMinuteDropDownChoice.setRequired(true);
    add(stopMinuteDropDownChoice);
    final Label datesAsUTCLabel = new DatesAsUTCLabel("datesAsUTC") {
      @Override
      public Date getStartTime()
      {
        return data.getStartTime();
      }

      @Override
      public Date getStopTime()
      {
        return data.getStopTime();
      }
    };
    add(datesAsUTCLabel);
    addRecentSheetsTable();
    WebMarkupContainer toggleRecentSheets = new WebMarkupContainer("toggleRecentSheets");
    toggleRecentSheets.setRenderBodyOnly(true);
    if (isNew() == false) {
      toggleRecentSheets.setVisible(false);
    }
    add(toggleRecentSheets);
    locationTextField = new PFAutoCompleteMaxLengthTextField("location", new PropertyModel<String>(data, "location")) {
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
    add(locationTextField);
    descriptionArea = new MaxLengthTextArea("description", new PropertyModel<String>(data, "description"));
    add(descriptionArea);
    add(WicketUtils.getJIRASupportTooltipImage(getResponse(), this));
    add(new JiraIssuesPanel("jiraIssues", data.getDescription()));
    add(new CheckBox("saveAsTemplate", new PropertyModel<Boolean>(this, "saveAsTemplate")));
  }

  @SuppressWarnings("serial")
  protected void addKost2Row()
  {
    kost2List = taskTree.getKost2List(data.getTaskId());
    kost2Row = new WebMarkupContainer("kost2Row") {
      @Override
      public boolean isVisible()
      {
        return CollectionUtils.isNotEmpty(kost2List);
      }
    };
    add(kost2Row);
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
    kost2Choice = createKost2ChoiceRenderer(parentPage.getBaseDao(), taskTree, kost2ChoiceRenderer, getData(), kost2List);
    kost2Choice.setRequired(true);
    kost2Row.add(kost2Choice);
  }

  @SuppressWarnings("serial")
  protected static DropDownChoice<Integer> createKost2ChoiceRenderer(final TimesheetDao timesheetDao, final TaskTree taskTree,
      final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer, final TimesheetDO data, final List<Kost2DO> kost2List)
  {
    final DropDownChoice<Integer> choice = new DropDownChoice<Integer>("kost2Id", new Model<Integer>() {
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
    final Integer taskId = getData().getTaskId();
    TaskNode node = taskId != null ? taskTree.getTaskNodeById(taskId) : null;
    if (node != null) {
      final TaskNode personDaysNode = taskTree.getPersonDaysNode(node);
      if (personDaysNode != null) {
        node = personDaysNode;
      }
    }
    ConsumptionBarPanel consumptionBarPanel = TaskListPage.getConsumptionBarPanel(this, "consumptionBar", taskTree, false, node);
    consumptionBarPanel.setRenderBodyOnly(true);
    add(consumptionBarPanel);
    consumptionBar = consumptionBarPanel;
  }

  @SuppressWarnings("serial")
  @Override
  protected void addButtonPanel()
  {
    final Fragment buttonFragment = new Fragment("buttonPanel", "buttonFragment", this);
    buttonFragment.setRenderBodyOnly(true);
    buttonCell.add(buttonFragment);
    cloneButtonPanel = new SingleButtonPanel("clone", new Button("button", new Model<String>(getString("clone"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.cloneTimesheet();
      }
    });
    if (isNew() == true || getData().isDeleted() == true) {
      // Show clone button only for existing time sheets.
      cloneButtonPanel.setVisible(false);
    }
    buttonFragment.add(cloneButtonPanel);
  }

  @Override
  public void onBeforeRender()
  {
    final DateHolder stopDateHolder = new DateHolder(data.getStopTime(), DatePrecision.MINUTE_15);
    stopHourOfDay = stopDateHolder.getHourOfDay();
    stopMinute = stopDateHolder.getMinute();
    super.onBeforeRender();
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
    getData().setStartTime(startDate.getTimestamp());
    getData().setStopTime(stopDate.getTimestamp());
  }

  @Override
  protected void validation()
  {
    updateStopDate();
    if (getData().getDuration() < 60000) {
      // Duration is less than 60 seconds.
      addError("timesheet.error.zeroDuration");
    } else if (getData().getDuration() > TimesheetDao.MAXIMUM_DURATION) {
      addError("timesheet.error.maximumDurationExceeded");
    }
    if (kost2Row.isVisible() == false && getData().getKost2Id() == null) {
      // Kost2 is not available for current task.
      final TaskNode taskNode = taskTree.getTaskNodeById(getData().getTaskId());
      if (taskNode != null) {
        final List<Integer> descendents = taskNode.getDescendantIds();
        for (final Integer taskId : descendents) {
          if (CollectionUtils.isNotEmpty(taskTree.getKost2List(taskId)) == true) {
            // But Kost2 is available for sub task, so user should book his time sheet
            // on a sub task with kost2s.
            addError("timesheet.error.kost2NeededChooseSubTask");
            break;
          }
        }
      }
    }
  }

  protected void refresh()
  {
    if (kost2Choice == null) {
      // Not yet initialized, no refresh needed.
      return;
    }
    kost2List = taskTree.getKost2List(data.getTaskId());
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
    kost2Choice.setChoiceRenderer(kost2ChoiceRenderer);
    kost2Choice.setChoices(kost2ChoiceRenderer.getValues());
    remove(consumptionBar);
    addConsumptionBar();
  }

  private LabelValueChoiceRenderer<Integer> getKost2LabelValueChoiceRenderer()
  {
    return getKost2LabelValueChoiceRenderer(parentPage.getBaseDao(), kost2List, getData(), kost2Choice);
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

  @SuppressWarnings( { "serial"})
  private void addRecentSheetsTable()
  {
    if (isNew() == false) {
      WebMarkupContainer invisible = new WebMarkupContainer("recentSheets");
      invisible.setVisible(false);
      add(invisible);
      return;
    }
    final List<IColumn<TimesheetDO>> columns = new ArrayList<IColumn<TimesheetDO>>();
    final CellItemListener<TimesheetDO> cellItemListener = new CellItemListener<TimesheetDO>() {
      public void populateItem(Item<ICellPopulator<TimesheetDO>> item, String componentId, IModel<TimesheetDO> rowModel)
      {
        final TimesheetDO timesheet = rowModel.getObject();
        final int rowIndex = ((Item< ? >) item.findParent(Item.class)).getIndex();
        String cssStyle = null;
        if (timesheet.isDeleted() == true) {
          cssStyle = "text-decoration: line-through;";
        } else if (rowIndex < TimesheetEditPage.SIZE_OF_FIRST_RECENT_BLOCK) {
          cssStyle = "font-weight: bold; color:red;";
        }

        if (cssStyle != null) {
          item.add(new AttributeAppendModifier("style", new Model<String>(cssStyle)));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("fibu.kost2"), null, "kost2.shortDisplayName", cellItemListener) {
      @Override
      public void populateItem(Item<ICellPopulator<TimesheetDO>> item, String componentId, IModel<TimesheetDO> rowModel)
      {
        final TimesheetDO timesheet = rowModel.getObject();
        final Fragment fragment = new Fragment(componentId, "selectRecentSheet", parentPage);
        item.add(fragment);
        final SubmitLink link = new SubmitLink("selectRecent") {
          public void onSubmit()
          {
            getData().setLocation(timesheet.getLocation());
            getData().setDescription(timesheet.getDescription());
            parentPage.getBaseDao().setTask(getData(), timesheet.getTaskId());
            parentPage.getBaseDao().setUser(getData(), timesheet.getUserId());
            parentPage.getBaseDao().setKost2(getData(), timesheet.getKost2Id());
            kost2Choice.modelChanged();
            locationTextField.modelChanged();
            descriptionArea.modelChanged();
            updateStopDate();
            refresh();
          };
        };
        fragment.add(link);
        link.setDefaultFormProcessing(false);
        fragment.add(new Label("label", new Model<String>() {
          @Override
          public String getObject()
          {
            final StringBuffer buf = new StringBuffer();
            if (timesheet.getKost2() != null) {
              buf.append(timesheet.getKost2().getShortDisplayName());
            }
            if (timesheet.getUserId() != null && timesheet.getUserId().equals(PFUserContext.getUserId()) == false) {
              if (timesheet.getKost2() != null) {
                buf.append(", ");
              }
              buf.append(userFormatter.getFormattedUser(timesheet.getUserId()));
            }
            return buf.toString();
          }
        }));
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("fibu.kunde")), null,
        "kost2.projekt.kunde.name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("fibu.projekt")), null, "kost2.projekt.name",
        cellItemListener));
    columns
        .add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("task")), null, "task.title", cellItemListener) {
          @Override
          public void populateItem(final Item<ICellPopulator<TimesheetDO>> item, final String componentId,
              final IModel<TimesheetDO> rowModel)
          {
            final TaskDO task = rowModel.getObject().getTask();
            StringBuffer buf = new StringBuffer();
            taskFormatter.appendFormattedTask(buf, new WicketLocalizerAndUrlBuilder(getResponse()), task, false, true, false);
            final Label formattedTaskLabel = new Label(componentId, buf.toString());
            formattedTaskLabel.setEscapeModelStrings(false);
            item.add(formattedTaskLabel);
            cellItemListener.populateItem(item, componentId, rowModel);
          }
        });
    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("timesheet.location"), null, "location", cellItemListener) {

    });
    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("timesheet.description"), null, "shortDescription",
        cellItemListener));
    @SuppressWarnings("unchecked")
    final IColumn<TimesheetDO>[] colArray = columns.toArray(new IColumn[columns.size()]);
    final IDataProvider<TimesheetDO> dataProvider = new ListDataProvider<TimesheetDO>(parentPage.getRecentTimesheets());
    final DataTable<TimesheetDO> dataTable = new DataTable<TimesheetDO>("recentSheets", colArray, dataProvider, 100) {
      @Override
      protected Item<TimesheetDO> newRowItem(String id, int index, IModel<TimesheetDO> model)
      {
        return new OddEvenItem<TimesheetDO>(id, index, model);
      }
    };
    final HeadersToolbar headersToolbar = new HeadersToolbar(dataTable, null);
    dataTable.addTopToolbar(headersToolbar);
    add(dataTable);
    if (isNew() == false) {
      dataTable.setVisible(false);
    }
    dataTable.add(new DataTableBehavior());
  }

  class DataTableBehavior extends AbstractBehavior implements IHeaderContributor
  {
    private static final long serialVersionUID = -3295144120585281383L;

    public void renderHead(IHeaderResponse response)
    {
      final String initJS = "// Mache alle Zeilen von recentSheets klickbar\n"
          + "  $(\".datatable td\").click( function() {\n"
          + "    $(this).parent().find(\"a:first\").click();\n"
          + "  });\n";
      response.renderOnDomReadyJavascript(initJS);
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
