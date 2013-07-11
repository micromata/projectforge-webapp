/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.resourceworkload;

import java.awt.Color;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.DateMidnight;
import org.joda.time.Duration;
import org.projectforge.core.QueryFilter;
import org.projectforge.fibu.EmployeeDao;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.CyclicDependencyException;
import org.projectforge.plugins.chimney.gantt.GanttScheduledActivity;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.gantt.IllegalTransientActivityException;
import org.projectforge.plugins.chimney.gantt.MissingFixedBeginDateException;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDao;
import org.projectforge.plugins.chimney.resourceworkload.DefaultEmployeeWorkdayProvider;
import org.projectforge.plugins.chimney.resourceworkload.ResourceAssignmentWorkload;
import org.projectforge.plugins.chimney.resourceworkload.RessourceNotAvailableInScheduledTimeException;
import org.projectforge.plugins.chimney.resourceworkload.TimesheetWorkload;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.PlanningStatus;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.DetachableChangeableDOModel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreeTabsPanel;
import org.projectforge.plugins.chimney.web.resourceworkload.model.IWorkloadData;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.JFreeChartImage;
import org.projectforge.web.wicket.components.DatePickerUtils;
import org.projectforge.web.wicket.components.JodaDateField;
import org.projectforge.web.wicket.converter.JodaDateConverter;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;

/**
 * This pages views the planned and real effort (accumulated timesheets and accumulated resource assignements) of a single project.
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public class ProjectWorkloadPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = 1L;

  /**
   * ID of this page (for mounting in wicket)
   */
  public static final String PAGE_ID = "projectWorkloadView";

  // @SpringBean(name="userDao")
  // private UserDao userDao;

  @SpringBean(name = "employeeDao")
  private EmployeeDao employeeDao;

  @SpringBean(name = "projectDao")
  private ProjectDao projectDao;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name = "resourceAssignmentDao")
  private ResourceAssignmentDao resourceAssignmentDao;

  @SpringBean(name = "wbsActivityDao")
  private WbsActivityDao wbsActivityDao;

  @SpringBean
  private IScheduler scheduler;

  private static final Color realResourceWorkloadColor = new Color(230, 50, 200);
  private static final Color plannedResourceWorkloadColor = new Color(100, 100, 250);
  private static final int defaultChartWidth = 920;
  private static final int defaultChartHeight = 360;
  private static final boolean drawWarnSymbolAtOverPlanning = true;

  private Float chartScaleFactor = 1.0f;
  private Form<Void> form;
  private ProjectDO project;
  private DateMidnight beginDay, endDay;
  private boolean projectChoosen = true;

  // private static final Logger log = Logger.getLogger(ResourceWorkloadPage.class);

  /**
   * constructor for showing a project selector (only one project can be visualized, thus it must be selected first)
   */
  public ProjectWorkloadPage()
  {
    super(new PageParameters());
    projectChoosen = false;
    addTabs(null);
    init();
  }

  /**
   * constructor for showing the workload of the specified project in between the scheduled time interval of the whole project
   * @param project project of which the workload is shown
   */
  public ProjectWorkloadPage(final ProjectDO project)
  {
    super(new PageParameters());
    projectChoosen = true;
    addTabs(project == null ? null : new Integer[] { project.getId()});
    init(project);
  }

  /**
   * constructor for showing the workload of the specified project in between the specified time interval
   * @param project visualized project
   * @param beginDay start day of the time interval to show
   * @param endDay end day of the time interval to show
   */
  public ProjectWorkloadPage(final ProjectDO project, final DateMidnight beginDay, final DateMidnight endDay)
  {
    super(new PageParameters());
    projectChoosen = true;
    addTabs(project == null ? null : new Integer[] { project.getId()});
    init(project, beginDay, endDay);
  }

  /**
   * constructor for showing the workload of the specified project in between the specified time interval. the chart size is linearly scaled
   * by the given scale factor
   * @param project visualized project
   * @param beginDay start day of the time interval to show
   * @param endDay end day of the time interval to show
   * @param chartScaleFactor factor to scale the size of the chart
   */
  public ProjectWorkloadPage(final ProjectDO project, final DateMidnight beginDay, final DateMidnight endDay, final float chartScaleFactor)
  {
    super(new PageParameters());
    projectChoosen = true;
    addTabs(project == null ? null : new Integer[] { project.getId()});
    init(project, beginDay, endDay, chartScaleFactor);
  }

  /**
   * constructor for compatibility with the tabs view (@see org.projectforge.plugins.chimney.web.projecttree.ProjectTreeTabsPanel) if there
   * was no project id or multiple project ids a project chooser will be shown to select one project. otherwise (so if one project id is
   * given) this constructor visualizes the project specified by this single id
   * @param projectIds ids of projects to show in the tabs view
   */
  public ProjectWorkloadPage(final Integer... projectIds)
  {
    super(new PageParameters());
    addTabs(projectIds);
    if (projectIds == null || projectIds.length == 0) {
      projectChoosen = false;
      init();
    } else {
      projectChoosen = projectIds.length == 1;
      final ProjectDO p = projectDao.getById(projectIds[0]);
      init(p);
    }
  }

  /**
   * create the page with the first project in the database
   */
  private void init()
  {
    final QueryFilter filter = new QueryFilter();
    final List<ProjectDO> allProjects = projectDao.getList(filter);
    if (allProjects.isEmpty()) {
      warn(getString("plugins.chimney.projectworkload.error.noprojectfound"));
      init(null);
    } else {
      init(allProjects.get(0));
    }
  }

  /**
   * create the page for the specified project in scheduled time interval
   * @param project visualized project
   */
  private void init(final ProjectDO project)
  {

    DateMidnight beginDay = null;
    DateMidnight endDay = null;

    boolean projectIntervalScheduled = false;
    try {
      if (project != null) {
        // schedule project to figure out begin and end day
        final WbsActivityDO projectActivity = wbsActivityDao.getByOrCreateFor(project);
        scheduler.schedule(projectActivity);
        final GanttScheduledActivity ganttActivity = scheduler.getResult(projectActivity);
        beginDay = new DateMidnight(ganttActivity.getBegin());
        endDay = new DateMidnight(ganttActivity.getEnd());
        projectIntervalScheduled = true;
      }
    } catch (final MissingFixedBeginDateException mfbde) {
      warn(getLocalizedMessage(mfbde.getI18nKey(), mfbde.getParams()));
    } catch (final IllegalTransientActivityException itae) {
      warn(getLocalizedMessage(itae.getI18nKey(), itae.getParams()));
    } catch (final CyclicDependencyException cde) {
      warn(getLocalizedMessage(cde.getI18nKey(), cde.getParams()));
    }
    if (!projectIntervalScheduled) {
      // take current month as interval
      final DateMidnight today = new DateMidnight();
      final int lastDayOfCurrentMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
      beginDay = new DateMidnight(today.getYear(), today.getMonthOfYear(), 1);
      endDay = new DateMidnight(today.getYear(), today.getMonthOfYear(), lastDayOfCurrentMonth);

    }
    init(project, beginDay, endDay);
  }

  /**
   * creates the page for the specified project in the specified time interval
   * @param project project to visualize
   * @param beginDay start day of the time interval to show
   * @param endDay end day of the time interval to show
   */
  private void init(final ProjectDO project, final DateMidnight beginDay, final DateMidnight endDay)
  {
    init(project, beginDay, endDay, 1.0f);
  }

  /**
   * creates the page for the specified project in the specified time interval
   * @param project project to visualize
   * @param beginDay start day of the time interval to show
   * @param endDay end day of the time interval to show
   */
  private void init(final ProjectDO project, final DateMidnight beginDay, final DateMidnight endDay, final float chartScaleFactor)
  {
    this.project = project;
    this.beginDay = beginDay;
    this.endDay = endDay;
    this.chartScaleFactor = chartScaleFactor;
  }

  /**
   * adds all components to the page
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    body.add(new Label("heading", getString("plugins.chimney.projectworkload.heading")));

    addFeedbackPanel();

    if (endDay.isBefore(beginDay)) {
      warn(getLocalizedMessage("plugins.chimney.errors.endbeforebegindate"));
      this.endDay = beginDay;
    }

    addProjectChoiceForm();
    addControls();

    addProjectWorkloadChart();
  }

  private void addTabs(final Integer[] projectIds)
  {
    body.add(new ProjectTreeTabsPanel("tabs", projectIds, this));
  }

  /**
   * adds the form to choose a project (only visible if none is yet choosed)
   */
  private void addProjectChoiceForm()
  {
    final Select<ProjectDO> select = new Select<ProjectDO>("projectSelect", Model.of(project));

    final RepeatingView rv = new RepeatingView("projectOptgroupRepeatingView");
    select.add(rv);

    for (final PlanningStatus groupName : PlanningStatus.values()) {

      final WebMarkupContainer overOptGroup = new WebMarkupContainer(rv.newChildId());
      rv.add(overOptGroup);

      final WebMarkupContainer optGroup = new WebMarkupContainer("projectStatusOptgroup");
      overOptGroup.add(optGroup);
      optGroup.add(AttributeModifier.replace("label", Model.of(getString(groupName.getI18nKey()))));

      final QueryFilter filter = new QueryFilter();
      filter.add(Restrictions.eq("planningStatus", groupName));
      final List<ProjectDO> projectsInGroup = projectDao.getList(filter);

      Collections.sort(projectsInGroup, new Comparator<ProjectDO>() {
        @Override
        public int compare(final ProjectDO p1, final ProjectDO p2)
        {
          if (p1.getTitle() == null)
            return 1;
          if (p2.getTitle() == null)
            return -1;
          return p1.getTitle().compareTo(p2.getTitle());
        }

      });
      optGroup.add(new SelectOptions<ProjectDO>("optgroupProjects", projectsInGroup, new IOptionRenderer<ProjectDO>() {
        private static final long serialVersionUID = 1L;

        @Override
        public String getDisplayValue(final ProjectDO p)
        {
          return p.getTitle();
        }

        @Override
        public IModel<ProjectDO> getModel(final ProjectDO p)
        {
          return new DetachableChangeableDOModel<ProjectDO, ProjectDao>(p, projectDao);
        }
      }));

      final Form<Void> form = new Form<Void>("projectChoiceForm") {
        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit()
        {
          setResponsePage(new ProjectWorkloadPage(select.getModelObject()));
        }
      };
      form.setVisible(!projectChoosen);
      form.add(select);
      form.add(new ButtonPanel("submitProjectButton", getString("plugins.chimney.resourceworkload.submitbuttonlabel"),
          new Button("button"), ButtonType.DEFAULT_SUBMIT));
      body.addOrReplace(form);
    }
  }

  /**
   * adds controls to select resource (user) and time interval
   */
  private void addControls()
  {
    final JodaDateField beginDateField = createDateField("beginDateField", beginDay, false);
    final JodaDateField endDateField = createDateField("endDateField", endDay, false);

    final Select<Float> chartSizes = new Select<Float>("chartSizeSelect", Model.of(chartScaleFactor));
    // chartSizes.add(new SelectOption<Float>("optionSize10", new Model<Float>(0.10f)));
    // chartSizes.add(new SelectOption<Float>("optionSize25", new Model<Float>(0.25f)));
    chartSizes.add(new SelectOption<Float>("optionSize50", new Model<Float>(0.50f)));
    chartSizes.add(new SelectOption<Float>("optionSize75", new Model<Float>(0.75f)));
    chartSizes.add(new SelectOption<Float>("optionSize100", new Model<Float>(1.00f)));
    chartSizes.add(new SelectOption<Float>("optionSize150", new Model<Float>(1.50f)));
    chartSizes.add(new SelectOption<Float>("optionSize200", new Model<Float>(2.00f)));
    chartSizes.add(new SelectOption<Float>("optionSize300", new Model<Float>(3.00f)));

    form = new Form<Void>("projectWorkloadForm") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onSubmit()
      {
        setResponsePage(new ProjectWorkloadPage(project, beginDateField.getModelObject(), endDateField.getModelObject(),
            chartSizes.getModelObject()));
      }
    };

    form.setVisible(projectChoosen);

    form.add(new Label("projectSelect", new PropertyModel<String>(project, "title")));
    form.add(beginDateField);
    form.add(endDateField);
    form.add(chartSizes);

    form.add(new ButtonPanel("submitButton", getString("plugins.chimney.resourceworkload.submitbuttonlabel"), new Button("button"),
        ButtonType.DEFAULT_SUBMIT));

    body.add(form);
  }

  /**
   * creates a date field to select date
   * @param wicketId id of the component
   * @param day date used for the text field model and which is initially selected
   * @return a JodaDateField ready to use
   */
  private JodaDateField createDateField(final String wicketId, final DateMidnight day, final boolean autoSubmit)
  {
    final IModel<DateMidnight> dateModel = Model.of(day);
    final JodaDateField dateField = new JodaDateField(wicketId, dateModel) {
      private static final long serialVersionUID = 1L;

      @Override
      public void renderHead(final IHeaderResponse response)
      {
        super.renderHead(response);
        DatePickerUtils.renderHead(response, getLocale(), getMarkupId(), autoSubmit);
      }
    };
    dateField.add(AttributeModifier.replace("size", "10"));
    dateField.setOutputMarkupId(true);
    return dateField;
  }

  /**
   * adds the project workload chart to the page
   */
  private void addProjectWorkloadChart()
  {

    addChartLabels();
    // create dataset factory for the all graphs (real, planned effort)
    final TimeSeriesCollection dataset = createDataset(project, beginDay, endDay);

    // create the chart
    final JFreeChart chart = createProjectWorkloadChart(dataset, beginDay, endDay);
    // createResourceWorkloadChart(dataset, beginDay, endDay);

    // setup painting colors for the three resource workload series (max, real, planned)
    final XYPlot plot = chart.getXYPlot();
    plot.setForegroundAlpha(0.5f);

    final XYItemRenderer renderer = plot.getRenderer();
    renderer.setSeriesPaint(0, realResourceWorkloadColor);
    renderer.setSeriesPaint(1, plannedResourceWorkloadColor);

    // create image containing the chart for visualization on the wicket page
    final Image chartImg = new JFreeChartImage("chartimage", chart, (int) (chartScaleFactor * defaultChartWidth),
        (int) (chartScaleFactor * defaultChartHeight));

    chartImg.setVisible(projectChoosen);
    body.add(chartImg);
  }

  /**
   * adds labels above the chart image to tell what the chart is currently showing
   */
  private void addChartLabels()
  {
    final WebMarkupContainer chartInfoPanel = new WebMarkupContainer("chartInfoPanel");
    chartInfoPanel.setVisible(projectChoosen);
    body.add(chartInfoPanel);

    chartInfoPanel.add(new Label("currentSettingsLabel", getString("plugins.chimney.resourceworkload.currentsettingslabel") + ":"));

    // body.add(new Label("selectedResourceLabel", getString("plugins.chimney.resourceworkload.resourcelabel")+": ") );
    chartInfoPanel.add(new Label("selectedProject", project == null ? "-" : project.getTitle()));

    final JodaDateConverter converter = new JodaDateConverter();
    // body.add(new Label("selectedFromDateLabel", getString("plugins.chimney.resourceworkload.fromdatelabel")+": ") );
    chartInfoPanel.add(new Label("selectedFromDate", converter.convertToString(beginDay, PFUserContext.getLocale())));

    // body.add(new Label("selectedTillDateLabel", getString("plugins.chimney.resourceworkload.tilldatelabel")+": ") );
    chartInfoPanel.add(new Label("selectedTillDate", converter.convertToString(endDay, PFUserContext.getLocale())));

  }

  /**
   * prepares the chart dataset for the graphs: maximim, planned and real resource workload for the specified user
   * @param project
   * @param beginDay start of the time interval
   * @param endDay end of the time interval
   * @return dataset (TimeSeriesCollection) for a JFreeChart
   */
  private TimeSeriesCollection createDataset(final ProjectDO project, final DateMidnight beginDay, final DateMidnight endDay)
  {
    final TimeSeriesCollection dataset = new TimeSeriesCollection();

    if (project == null)
      return dataset;

    QueryFilter filter;

    Timestamp beginStamp, endStamp;
    beginStamp = new Timestamp(beginDay.getMillis());
    endStamp = new Timestamp(endDay.getMillis());

    final List<AbstractWbsNodeDO> allProjectNodesList = new ArrayList<AbstractWbsNodeDO>();
    getAllNodesInSubTree(project, allProjectNodesList);

    // filter all timesheets of all project nodes in the specified time interval
    filter = new QueryFilter();
    Junction taskDisjunction = Restrictions.disjunction();
    for (final AbstractWbsNodeDO node : allProjectNodesList) {
      taskDisjunction = taskDisjunction.add(Restrictions.eq("task", node.getTaskDo()));
    }
    filter.add(taskDisjunction);
    filter.add(Restrictions.between("startTime", beginStamp, endStamp));
    filter.add(Restrictions.between("stopTime", beginStamp, endStamp));
    final List<TimesheetDO> userTimesheets = timesheetDao.getList(filter);
    // create real resource workload data
    final IWorkloadData realResourceWorkload = new TimesheetWorkload(userTimesheets);
    dataset.addSeries(WorkloadSeriesFactory.createTimeSeries(getString("plugins.chimney.projectworkload.realeffort"), realResourceWorkload,
        beginDay, endDay));

    String exceptionMsg = null;
    try {
      filter = new QueryFilter();
      Junction wbsNodeDisjunction = Restrictions.disjunction();
      for (final AbstractWbsNodeDO node : allProjectNodesList) {
        wbsNodeDisjunction = wbsNodeDisjunction.add(Restrictions.eq("wbsNode", node));
      }
      filter.add(wbsNodeDisjunction);
      final List<ResourceAssignmentDO> userAssignements = resourceAssignmentDao.getList(filter);
      final IWorkloadData plannedResourceWorkload = new ResourceAssignmentWorkload(userAssignements, wbsActivityDao, scheduler,
          employeeDao, new DefaultEmployeeWorkdayProvider());
      plannedResourceWorkload.getWorkloadHoursPerDay(beginDay, endDay); // just to trigger the exception, if any
      dataset.addSeries(WorkloadSeriesFactory.createTimeSeries(getString("plugins.chimney.projectworkload.plannedeffort"),
          plannedResourceWorkload, beginDay, endDay));
    } catch (final RessourceNotAvailableInScheduledTimeException rnaiste) {
      exceptionMsg = getLocalizedMessage(rnaiste.getI18nKey(), rnaiste.getParams());
    } catch (final MissingFixedBeginDateException mfbde) {
      exceptionMsg = getLocalizedMessage(mfbde.getI18nKey(), mfbde.getParams());
    } catch (final IllegalTransientActivityException itae) {
      exceptionMsg = getLocalizedMessage(itae.getI18nKey(), itae.getParams());
    } catch (final CyclicDependencyException cde) {
      exceptionMsg = getLocalizedMessage(cde.getI18nKey(), cde.getParams());
    }
    if (exceptionMsg != null) {
      warn(
          exceptionMsg + " "
              + getString("plugins.chimney.projectworkload.error.cantcalculateplannedeffort")
          );
    }
    return dataset;
  }

  /**
   * retrieves all nodes in the subtree of the given node and adds them to the list
   * @param node root of the subtree
   * @param list list where all nodes of the subtree are added
   */
  private void getAllNodesInSubTree(final AbstractWbsNodeDO node, final List<AbstractWbsNodeDO> list)
  {
    if (node == null)
      return;
    list.add(node);
    final int childrenCount = node.childrenCount();
    for (int i = 0; i < childrenCount; ++i) {
      getAllNodesInSubTree(node.getChild(i), list);
    }
  }

  /**
   * create a resource workload chart for the given resource workload graph data to visualize in the specified time interval
   * @param dataSetGen resource workload dataset generator
   * @param beginDay begin of the interval to show
   * @param endDay end of the interval to show
   * @return a JFreeChart for visualizing the resource workload
   */
  private JFreeChart createProjectWorkloadChart(final TimeSeriesCollection dataset, final DateMidnight beginDay, final DateMidnight endDay)
  {
    // configure x-axis label and tick units depending on the time interval size
    final int durationInDays = new Duration(beginDay, endDay).toStandardDays().getDays();
    String timeAxisLabel = getString("plugins.chimney.resourceworkload.timeaxislabel") + " ";
    DateTickUnit dateTickUnit = null;
    if (durationInDays < 70) {
      dateTickUnit = new DateTickUnit(DateTickUnitType.DAY, 1, new SimpleDateFormat("d"));
      timeAxisLabel += "(" + getString("plugins.chimney.resourceworkload.days") + ")";
    } else if (durationInDays < 365) {
      dateTickUnit = new DateTickUnit(DateTickUnitType.DAY, 7, new SimpleDateFormat("w"));
      timeAxisLabel += "(" + getString("plugins.chimney.resourceworkload.weeks") + ")";
    } else if (durationInDays <= 365 * 4) {
      dateTickUnit = new DateTickUnit(DateTickUnitType.MONTH, 1, new SimpleDateFormat("M"));
      timeAxisLabel += "(" + getString("plugins.chimney.resourceworkload.months") + ")";
    } else if (durationInDays <= 365 * 20) {
      dateTickUnit = new DateTickUnit(DateTickUnitType.YEAR, 1, new SimpleDateFormat("yyyy"));
      timeAxisLabel += "(" + getString("plugins.chimney.resourceworkload.years") + ")";
    } else {
      dateTickUnit = new DateTickUnit(DateTickUnitType.YEAR, 1, new SimpleDateFormat("yy"));
      timeAxisLabel += "(" + getString("plugins.chimney.resourceworkload.years") + ")";
    }

    /*
     * // create the chart final JFreeChart chart = ChartFactory.createXYAreaChart(
     * getString("plugins.chimney.resourceworkload.charttitle"), timeAxisLabel,
     * getString("plugins.chimney.resourceworkload.workloadaxislabel")+" ("+getString("plugins.chimney.resourceworkload.hours")+")",
     * dataSetGen.createDataset(beginDay, endDay), PlotOrientation.VERTICAL, true, true, false );
     */

    final XYAreaRenderer renderer = new XYAreaRenderer(XYAreaRenderer.AREA);

    // configure time axis
    final DateAxis timeAxis = new DateAxis(timeAxisLabel);
    timeAxis.setTickUnit(dateTickUnit);
    timeAxis.setLowerMargin(0.0);
    timeAxis.setUpperMargin(0.0);

    final NumberAxis yAxis = new NumberAxis(getString("plugins.chimney.projectworkload.workloadaxislabel")
        + " ("
        + getString("plugins.chimney.resourceworkload.hours")
        + ")");

    if (drawWarnSymbolAtOverPlanning) {
      yAxis.setUpperMargin(0.2);
    } else {
      yAxis.setUpperMargin(0.1);
    }
    final XYPlot plot = new XYPlot(dataset, timeAxis, yAxis, renderer);
    plot.setOrientation(PlotOrientation.VERTICAL);
    plot.setForegroundAlpha(0.5f);

    final JFreeChart chart = new JFreeChart(getString("plugins.chimney.projectworkload.charttitle") + ": " + project == null ? "-"
        : project.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, plot, true);

    ChartFactory.getChartTheme().apply(chart);
    // return chart;
    // configure additional visual properties
    chart.setBackgroundPaint(Color.white);
    // chart.setBorderPaint(Color.magenta);

    // configure time axis
    plot.setBackgroundPaint(new Color(250, 240, 250));
    plot.setDomainGridlinePaint(Color.gray);
    plot.setRangeGridlinePaint(Color.gray);

    // XYItemRenderer renderer = plot.getRenderer(0);

    return chart;
  }

  /**
   * 
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.projectworkload.heading");
  }

  /**
   * 
   * @see org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage#getNavigationBarName()
   */
  @Override
  protected String getNavigationBarName()
  {
    return NavigationConstants.MAIN;
  }

  /**
   * 
   * @see org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage#insertBreadcrumbItems(java.util.List)
   */
  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    items.add(BreadcrumbConstants.PROJECT_WORKLOAD_VIEW);
  }

}
