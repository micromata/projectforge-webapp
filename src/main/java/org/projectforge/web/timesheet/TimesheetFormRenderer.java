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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.hibernate.Hibernate;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.core.Configuration;
import org.projectforge.core.SystemInfoCache;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.jira.JiraUtils;
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
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.WicketLocalizerAndUrlBuilder;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.CheckBoxPanel;
import org.projectforge.web.wicket.components.ConsumptionBarPanel;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.DropDownChoicePanel;
import org.projectforge.web.wicket.components.ImageLinkPanel;
import org.projectforge.web.wicket.components.LabelForPanel;
import org.projectforge.web.wicket.components.LabelPanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
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

  private static final String RECENT_TIME_SHEETS_MODAL_WINDOW_ID = "recentTimesheetsModalWindow";

  protected TaskTree taskTree;

  protected TimesheetDao timesheetDao;

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

  protected ModalWindow recentTimesheetsModalWindow;

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
              container.error(getString("timesheet.error.kost2NeededChooseSubTask"));
            }
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
    if (SystemInfoCache.instance().isCost2EntriesExists() == true) {
      final String label = getString("fibu.kost2");
      cost2ChoiceLabel = doPanel.addLabel(label, HALF);
      cost2ChoiceLabel.setBreakBefore();
      final IField field = doPanel.addDropDownChoice(data, "kost2", label, null, FULL);
      if (field instanceof DropDownChoiceLPanel) {
        cost2ChoicePanel = (DropDownChoiceLPanel) field;
      } else {
        // Shouldn't occur.
        throw new UnsupportedOperationException();
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
      repeatingView.add(checkBoxPanel);
      final LabelForPanel label = new LabelForPanel(repeatingView.newChildId(), checkBoxPanel.getCheckBox(),
          getString("user.pref.saveAsTemplate"));
      repeatingView.add(label);
    }

    if (jiraSupport == true) {
      // Add help text:
      doPanel.addLabel("", HALF).setBreakBefore();
      doPanel.addHelpLabel("*) " + getString("tooltip.jiraSupport.field"), DOUBLE);
    }

    recentTimesheetsModalWindow = new ModalWindow(RECENT_TIME_SHEETS_MODAL_WINDOW_ID);
    add(recentTimesheetsModalWindow);

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
            if (cost2Choice != null) {
              cost2Choice.modelChanged();
            }
            locationTextField.modelChanged();
            descriptionArea.modelChanged();
            refresh();
          }
        }
      };
      templateNamesChoice.setNullValid(true);
      repeatingView.add(new DropDownChoicePanel<String>(repeatingView.newChildId(), templateNamesChoice));
    }
    final AjaxLink< ? > link = new AjaxLink<Void>(ImageLinkPanel.LINK_WICKET_ID) {
      @Override
      public void onClick(AjaxRequestTarget target)
      {
        showRecentTimesheetsDialog(target);
      }
    };
    final ImageLinkPanel imageLinkPanel = new ImageLinkPanel(repeatingView.newChildId(), link, parentPage.getResponse(), ImageDef.ZOOM
        .getPath(), getString("timesheet.recent.select")) {
    };
    repeatingView.add(imageLinkPanel);
  }

  protected void refresh()
  {
    addConsumptionBar();
    if (cost2Choice != null) {
      // cost2Choice does always exist if any cost2 entry does exist in the data-base (but is may be invisible).
      cost2List = taskTree.getKost2List(data.getTaskId());
      final LabelValueChoiceRenderer<Integer> cost2ChoiceRenderer = getCost2LabelValueChoiceRenderer();
      cost2Choice.setChoiceRenderer(cost2ChoiceRenderer);
      cost2Choice.setChoices(cost2ChoiceRenderer.getValues());
      updateCost2ChoiceVisibility();
    }
  }

  protected void addCost2Row()
  {
    if (cost2ChoicePanel == null) {
      // There is no cost2 entry in the data-base, so cost2 row not needed.
      return;
    }
    cost2List = taskTree.getKost2List(data.getTaskId());
    final LabelValueChoiceRenderer<Integer> cost2ChoiceRenderer = getCost2LabelValueChoiceRenderer();
    cost2Choice = createCost2ChoiceRenderer(DropDownChoiceLPanel.SELECT_ID, parentPage.getBaseDao(), taskTree, cost2ChoiceRenderer, data,
        cost2List);
    cost2Choice.setLabel(new Model<String>(getString("fibu.kost2")));
    cost2Choice.setRequired(true);
    cost2ChoicePanel.replaceWithDropDownChoice(cost2Choice);
    updateCost2ChoiceVisibility();
  }

  private void updateCost2ChoiceVisibility()
  {
    final boolean cost2Visible = CollectionUtils.isNotEmpty(cost2List);
    cost2ChoiceLabel.setVisible(cost2Visible);
    cost2ChoicePanel.setVisible(cost2Visible);
  }

  @SuppressWarnings("serial")
  protected static DropDownChoice<Integer> createCost2ChoiceRenderer(final String id, final TimesheetDao timesheetDao,
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

  private LabelValueChoiceRenderer<Integer> getCost2LabelValueChoiceRenderer()
  {
    return getCost2LabelValueChoiceRenderer(parentPage.getBaseDao(), cost2List, data, cost2Choice);
  }

  private void showRecentTimesheetsDialog(final AjaxRequestTarget target)
  {
    recentTimesheetsModalWindow.setInitialHeight(800);
    recentTimesheetsModalWindow.setInitialWidth(1000);
    recentTimesheetsModalWindow.setMinimalHeight(800);
    recentTimesheetsModalWindow.setMinimalWidth(1000);

    final Fragment content = new Fragment(recentTimesheetsModalWindow.getContentId(), "recentTimesheetsTable", parentPage);
    addRecentSheetsTable(content);

    recentTimesheetsModalWindow.setContent(content);

    recentTimesheetsModalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      private static final long serialVersionUID = 2633814101880954425L;

      public void onClose(AjaxRequestTarget target)
      {
      }

    });
    recentTimesheetsModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      private static final long serialVersionUID = 6761625465164911336L;

      public boolean onCloseButtonClicked(AjaxRequestTarget target)
      {
        return true;
      }
    });
    recentTimesheetsModalWindow.show(target);
  }

  @SuppressWarnings( { "serial"})
  private void addRecentSheetsTable(final WebMarkupContainer container)
  {
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
    if (cost2Choice != null) { // Is maybe invisible but does always exist if cost2 entries does exist in the system.
      columns
          .add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("fibu.kost2"), null, "kost2.shortDisplayName", cellItemListener) {
            @Override
            public void populateItem(Item<ICellPopulator<TimesheetDO>> item, String componentId, IModel<TimesheetDO> rowModel)
            {
              final TimesheetDO timesheet = rowModel.getObject();
              final Fragment fragment = new Fragment(componentId, "selectRecentSheet", parentPage);
              item.add(fragment);
              fragment.add(createRecentTimeSheetSelectionLink(timesheet));
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
              item.add(new AttributeAppendModifier("style", new Model<String>("white-space: nowrap;")));
              final Item< ? > row = ((Item< ? >) item.findParent(Item.class));
              WicketUtils.addRowClick(row);
              cellItemListener.populateItem(item, componentId, rowModel);
            }
          });
      columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("fibu.kunde")), null,
          "kost2.projekt.kunde.name", cellItemListener));
      columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("fibu.projekt")), null, "kost2.projekt.name",
          cellItemListener));
      columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("task")), null, "task.title",
          cellItemListener) {
        @Override
        public void populateItem(final Item<ICellPopulator<TimesheetDO>> item, final String componentId, final IModel<TimesheetDO> rowModel)
        {
          final TaskDO task = rowModel.getObject().getTask();
          StringBuffer buf = new StringBuffer();
          TaskFormatter.instance().appendFormattedTask(buf, new WicketLocalizerAndUrlBuilder(parentPage.getResponse()), task, false, true,
              false);
          final Label formattedTaskLabel = new Label(componentId, buf.toString());
          formattedTaskLabel.setEscapeModelStrings(false);
          item.add(formattedTaskLabel);
          final Item< ? > row = ((Item< ? >) item.findParent(Item.class));
          WicketUtils.addRowClick(row);
          cellItemListener.populateItem(item, componentId, rowModel);
        }
      });
    } else {
      columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(new Model<String>(getString("task")), null, "task.title",
          cellItemListener) {
        @Override
        public void populateItem(final Item<ICellPopulator<TimesheetDO>> item, final String componentId, final IModel<TimesheetDO> rowModel)
        {
          final TimesheetDO timesheet = rowModel.getObject();
          final TaskDO task = rowModel.getObject().getTask();
          final Fragment fragment = new Fragment(componentId, "selectRecentSheet", parentPage);
          item.add(fragment);
          fragment.add(createRecentTimeSheetSelectionLink(timesheet));
          fragment.add(new Label("label", new Model<String>() {
            @Override
            public String getObject()
            {
              final StringBuffer buf = new StringBuffer();
              TaskFormatter.instance().appendFormattedTask(buf, new WicketLocalizerAndUrlBuilder(parentPage.getResponse()), task, false,
                  true, false);
              return buf.toString();
            }
          }).setEscapeModelStrings(false));
          final Item< ? > row = ((Item< ? >) item.findParent(Item.class));
          WicketUtils.addRowClick(row);
          cellItemListener.populateItem(item, componentId, rowModel);
        }
      });
    }
    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("timesheet.location"), null, "location", cellItemListener) {

    });
    columns.add(new CellItemListenerPropertyColumn<TimesheetDO>(getString("timesheet.description"), null, "shortDescription",
        cellItemListener));
    @SuppressWarnings("unchecked")
    final IColumn<TimesheetDO>[] colArray = columns.toArray(new IColumn[columns.size()]);
    final IDataProvider<TimesheetDO> dataProvider = new ListDataProvider<TimesheetDO>(parentPage.getRecentTimesheets());
    final DataTable<TimesheetDO> dataTable = new DataTable<TimesheetDO>("table", colArray, dataProvider, 100) {
      @Override
      protected Item<TimesheetDO> newRowItem(String id, int index, IModel<TimesheetDO> model)
      {
        return new OddEvenItem<TimesheetDO>(id, index, model);
      }
    };
    final HeadersToolbar headersToolbar = new HeadersToolbar(dataTable, null);
    dataTable.addTopToolbar(headersToolbar);
    container.add(dataTable);
    if (isNew() == false) {
      dataTable.setVisible(false);
    }
    dataTable.add(new DataTableBehavior());
  }

  @SuppressWarnings("serial")
  private AjaxLink<Void> createRecentTimeSheetSelectionLink(final TimesheetDO timesheet)
  {
    return new AjaxLink<Void>("selectRecent") {
      @Override
      public void onClick(final AjaxRequestTarget target)
      {
        if (target != null) {
          data.setLocation(timesheet.getLocation());
          data.setDescription(timesheet.getDescription());
          parentPage.getBaseDao().setTask(data, timesheet.getTaskId());
          parentPage.getBaseDao().setUser(data, timesheet.getUserId());
          parentPage.getBaseDao().setKost2(data, timesheet.getKost2Id());
          if (cost2Choice != null) {
            cost2Choice.modelChanged();
          }
          locationTextField.modelChanged();
          descriptionArea.modelChanged();
          // updateStopDate();
          refresh();
          recentTimesheetsModalWindow.close(target);
          parentPage.setResponsePage(parentPage);
        }
      }
    };
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
}
