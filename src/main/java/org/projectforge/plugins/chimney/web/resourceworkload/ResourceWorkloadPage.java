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
import java.util.Calendar;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
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
import org.projectforge.fibu.EmployeeDO;
import org.projectforge.fibu.EmployeeDao;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.CyclicDependencyException;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.gantt.IllegalTransientActivityException;
import org.projectforge.plugins.chimney.gantt.MissingFixedBeginDateException;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDao;
import org.projectforge.plugins.chimney.resourceworkload.DefaultEmployeeAvailabilityProvider;
import org.projectforge.plugins.chimney.resourceworkload.DefaultEmployeeWorkdayProvider;
import org.projectforge.plugins.chimney.resourceworkload.MaximumResourceWorkload;
import org.projectforge.plugins.chimney.resourceworkload.ResourceAssignmentWorkload;
import org.projectforge.plugins.chimney.resourceworkload.RessourceNotAvailableInScheduledTimeException;
import org.projectforge.plugins.chimney.resourceworkload.TimesheetWorkload;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.components.ChimneyUserSelectPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.resourceworkload.model.IWorkloadData;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.JFreeChartImage;
import org.projectforge.web.wicket.components.DatePickerUtils;
import org.projectforge.web.wicket.components.JodaDateField;
import org.projectforge.web.wicket.converter.JodaDateConverter;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;
/**
 * This pages views the planned, real and maximum workload of a single resource (employee).
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public class ResourceWorkloadPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = 1L;

  public static final String PAGE_ID = "resourceWorkloadView";

  @SpringBean(name="employeeDao")
  private EmployeeDao employeeDao;

  @SpringBean(name="timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name="resourceAssignmentDao")
  private ResourceAssignmentDao resourceAssignmentDao;

  @SpringBean(name="wbsActivityDao")
  private WbsActivityDao wbsActivityDao;

  @SpringBean
  private IScheduler scheduler;

  /**
   * Color of the maximum resource load graph
   */
  private static final Color maximumResourceWorkloadColor = new Color(255,155,155);
  /**
   * Color of the real resource load graph
   */
  private static final Color realResourceWorkloadColor = Color.green;
  /**
   * Color of the planned resource load graph
   */
  private static final Color plannedResourceWorkloadColor = Color.blue;

  /**
   * default width of the chart image
   */
  private static final int defaultChartWidth = 920;

  /**
   * default height of the chart image
   */
  private static final int defaultChartHeight = 360;

  /**
   * if true, warn symbols are shown in the chart, when a resource (employee) is planned to work overtime
   */
  private static final boolean drawWarnSymbolAtOverPlanning = true;

  /**
   * scale factor by which scales the chart image with repsect to the default chart width and height
   */
  private Float chartScaleFactor = 1.0f;

  private Form<Void> form;
  private PFUserDO user;
  private DateMidnight beginDay,endDay;

  // private static final Logger log = Logger.getLogger(ResourceWorkloadPage.class);

  /**
   * constructor for showing the resource workload of the current logged user in the current month
   */
  public ResourceWorkloadPage() {
    super(new PageParameters());
    init(PFUserContext.getUser());
  }

  /**
   * constructor for showing the resource workload of the specified user in the current month
   * @param user resource of which the workload is shown
   */
  public ResourceWorkloadPage(final PFUserDO user) {
    super(new PageParameters());
    init(user);
  }

  /**
   * constructor for showing the resource workload of the specified user in the specified time interval
   * @param user resource of which the workload is shown
   * @param beginDay start day of the time interval to show
   * @param endDay end day of the time interval to show
   */
  public ResourceWorkloadPage(final PFUserDO user, final DateMidnight beginDay, final DateMidnight endDay) {
    super(new PageParameters());
    init(user, beginDay, endDay);
  }

  public ResourceWorkloadPage(final PFUserDO user, final DateMidnight beginDay, final DateMidnight endDay, final float chartScaleFactor) {
    super(new PageParameters());
    init(user, beginDay, endDay, chartScaleFactor);
  }

  /**
   * create the page for the specified user in the current month
   * @param user resource of which the workload is shown
   */
  private void init(final PFUserDO user)
  {
    final DateMidnight today = new DateMidnight();
    final int lastDayOfCurrentMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
    final DateMidnight beginDay = new DateMidnight(today.getYear(), today.getMonthOfYear(), 1);
    final DateMidnight endDay = new DateMidnight(today.getYear(), today.getMonthOfYear(), lastDayOfCurrentMonth);
    //final DateMidnight endDay = new DateMidnight(today.getYear(), (today.getMonthOfYear() < 12? today.getMonthOfYear()+1: 1), 1);
    init(user, beginDay, endDay);
  }

  /**
   * creates the page for the specified user in the specified time interval
   * @param user resource of which the workload is shown
   * @param beginDay start day of the time interval to show
   * @param endDay end day of the time interval to show
   */
  private void init(final PFUserDO user, final DateMidnight beginDay, final DateMidnight endDay) {
    init(user, beginDay, endDay, 1.0f);
  }

  /**
   * creates the page for the specified user in the specified time interval
   * @param user resource of which the workload is shown
   * @param beginDay start day of the time interval to show
   * @param endDay end day of the time interval to show
   */
  private void init(final PFUserDO user, final DateMidnight beginDay, final DateMidnight endDay, final float chartScaleFactor)
  {
    this.user = user;
    this.beginDay = beginDay;
    this.endDay = endDay;
    this.chartScaleFactor = chartScaleFactor;
  }

  /**
   * adds the components to the page
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize() {
    super.onInitialize();
    body.add( new Label("heading", getString("plugins.chimney.resourceworkload.heading")));

    addFeedbackPanel();

    if (endDay.isBefore(beginDay)) {
      warn(getLocalizedMessage("plugins.chimney.errors.endbeforebegindate"));
      this.endDay = beginDay;
    }

    addControls();

    addResourceWorkloadChart();
  }

  /**
   * adds controls to select resource (user), time interval and chart size
   */
  private void addControls() {
    final UserSelectPanel userSelectPanel = new ChimneyUserSelectPanel("userSelect", Model.of(user));

    final JodaDateField beginDateField = createDateField("beginDateField", beginDay, false);
    final JodaDateField endDateField = createDateField("endDateField", endDay, false);



    final Select<Float> chartSizes = new Select<Float>("chartSizeSelect", Model.of(chartScaleFactor));
    //  chartSizes.add(new SelectOption<Float>("optionSize10",  new Model<Float>(0.10f)));
    //  chartSizes.add(new SelectOption<Float>("optionSize25",  new Model<Float>(0.25f)));
    chartSizes.add(new SelectOption<Float>("optionSize50",  new Model<Float>(0.50f)));
    chartSizes.add(new SelectOption<Float>("optionSize75",  new Model<Float>(0.75f)));
    chartSizes.add(new SelectOption<Float>("optionSize100", new Model<Float>(1.00f)));
    chartSizes.add(new SelectOption<Float>("optionSize150", new Model<Float>(1.50f)));
    chartSizes.add(new SelectOption<Float>("optionSize200", new Model<Float>(2.00f)));
    chartSizes.add(new SelectOption<Float>("optionSize300", new Model<Float>(3.00f)));


    form = new Form<Void>("resourceWorkloadForm") {
      private static final long serialVersionUID = 1L;
      @Override
      public void onSubmit() {
        setResponsePage(
            new ResourceWorkloadPage(
                userSelectPanel.getModelObject(),
                beginDateField.getModelObject(),
                endDateField.getModelObject(),
                chartSizes.getModelObject()
                )
            );
      }
    };

    form.add( userSelectPanel );
    userSelectPanel.init();
    //userSelectPanel.withAutoSubmit(true);
    userSelectPanel.setRequired(true);

    form.add( beginDateField );
    form.add( endDateField );

    form.add(
        new ButtonPanel(
            "submitButton",
            getString("plugins.chimney.resourceworkload.submitbuttonlabel"),
            new Button("button"),
            ButtonType.DEFAULT_SUBMIT
            )
        );

    form.add(chartSizes);

    body.add(form);
  }

  /**
   * creates a date field to select dates (whole days)
   * @param wicketId id of the component
   * @param day date used for the text field model and which is initially selected
   * @return a JodaDateField ready to use
   */
  private JodaDateField createDateField(final String wicketId, final DateMidnight day, final boolean autoSubmit) {
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
   * adds the resource workload chart to the page
   */
  private void addResourceWorkloadChart() {

    addChartLabels();
    // create dataset factory for the all graphs (real, planned, maximum worload)
    final TimeSeriesCollection dataset = createDataset(user, beginDay, endDay);

    // create the chart
    final JFreeChart chart = createResourceWorkloadChart(dataset, beginDay, endDay);
    //createResourceWorkloadChart(dataset, beginDay, endDay);

    // setup painting colors for the three resource workload series (max, real, planned)
    final XYPlot plot = chart.getXYPlot();
    plot.setForegroundAlpha(0.5f);

    if (drawWarnSymbolAtOverPlanning) {
      final EmployeeDO employee = employeeDao.getByUserId(user.getId());

      plot.setRenderer(
          new OverloadWarnXYAreaRenderer(
              new TimeSeriesCollection(
                  WorkloadSeriesFactory.createTimeSeries(
                      "compare",
                      new MaximumResourceWorkload(employee,
                          new DefaultEmployeeAvailabilityProvider(
                              new DefaultEmployeeWorkdayProvider()
                              )
                          ),
                          beginDay, endDay
                      )
                  ),
                  2,
                  XYAreaRenderer.AREA
              )
          );
    }
    final XYItemRenderer renderer = plot.getRenderer();
    renderer.setSeriesPaint(0, maximumResourceWorkloadColor);
    renderer.setSeriesPaint(1, realResourceWorkloadColor);
    renderer.setSeriesPaint(2, plannedResourceWorkloadColor);

    // create image containing the chart for visualization on the wicket page
    final Image chartImg = new JFreeChartImage(
        "chartimage",
        chart,
        (int)(chartScaleFactor * defaultChartWidth),
        (int)(chartScaleFactor * defaultChartHeight));


    body.add(chartImg);
  }


  private void addChartLabels() {

    body.add(new Label("currentSettingsLabel", getString("plugins.chimney.resourceworkload.currentsettingslabel") +":") );

    // body.add(new Label("selectedResourceLabel", getString("plugins.chimney.resourceworkload.resourcelabel")+": ") );
    body.add(new Label("selectedResource", user.getDisplayUsername()) );

    final JodaDateConverter converter = new JodaDateConverter();
    // body.add(new Label("selectedFromDateLabel", getString("plugins.chimney.resourceworkload.fromdatelabel")+": ") );
    body.add(new Label("selectedFromDate", converter.convertToString(beginDay, PFUserContext.getLocale())) );

    // body.add(new Label("selectedTillDateLabel", getString("plugins.chimney.resourceworkload.tilldatelabel")+": ") );
    body.add(new Label("selectedTillDate",  converter.convertToString(endDay, PFUserContext.getLocale())) );

  }

  /**
   * prepares the chart dataset for the graphs: maximim, planned and real resource workload for the specified user
   * @param user resource for which graph data is prepared
   * @param beginDay start of the time interval
   * @param endDay end of the time interval
   * @return dataset (TimeSeriesCollection) for a JFreeChart
   */
  private TimeSeriesCollection createDataset(final PFUserDO user, final DateMidnight beginDay, final DateMidnight endDay) {
    final TimeSeriesCollection dataset = new TimeSeriesCollection();

    QueryFilter filter;

    // create maximum resource workload data
    final EmployeeDO employee = employeeDao.getByUserId(user.getId());
    final IWorkloadData maxResourceWorkload = new MaximumResourceWorkload(employee,
        new DefaultEmployeeAvailabilityProvider(
            new DefaultEmployeeWorkdayProvider()
            )
        );
    dataset.addSeries(
        WorkloadSeriesFactory.createTimeSeries(
            getString("plugins.chimney.resourceworkload.maxresourceload"),
            maxResourceWorkload,
            beginDay,
            endDay
            )
        );

    Timestamp beginStamp, endStamp;
    beginStamp  = new Timestamp(beginDay.getMillis());
    endStamp    = new Timestamp(endDay.getMillis());

    // filter all timesheets for the user in the specified time interval
    filter = new QueryFilter();
    filter.add( Restrictions.eq("user", user) );
    filter.add( Restrictions.between("startTime", beginStamp, endStamp) );
    filter.add( Restrictions.between("stopTime",  beginStamp, endStamp) );
    final List<TimesheetDO> userTimesheets = timesheetDao.getList(filter);
    // create real resource workload data
    final IWorkloadData realResourceWorkload = new TimesheetWorkload(userTimesheets);
    dataset.addSeries(
        WorkloadSeriesFactory.createTimeSeries(
            getString("plugins.chimney.resourceworkload.realresourceload"),
            realResourceWorkload,
            beginDay,
            endDay
            )
        );

    String exceptionMsg = null;
    try {
      filter = new QueryFilter();
      filter.add( Restrictions.eq("user", user) );
      final List<ResourceAssignmentDO> userAssignements = resourceAssignmentDao.getList(filter);
      final IWorkloadData plannedResourceWorkload = new ResourceAssignmentWorkload(userAssignements, wbsActivityDao, scheduler, employeeDao, new DefaultEmployeeWorkdayProvider());
      plannedResourceWorkload.getWorkloadHoursPerDay(beginDay, endDay); // just to trigger the exception, if any
      dataset.addSeries(
          WorkloadSeriesFactory.createTimeSeries(
              getString("plugins.chimney.resourceworkload.plannedresourceload"),
              plannedResourceWorkload,
              beginDay,
              endDay
              )
          );
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
              + getString("plugins.chimney.resourceworkload.error.cantcalculateplannedresourceload")
          );
    }
    return dataset;
  }

  /**
   * create a resource workload chart for the given resource workload graph data to visualize in the specified time interval
   * @param dataSetGen resource workload dataset generator
   * @param beginDay begin of the interval to show
   * @param endDay end of the interval to show
   * @return a JFreeChart for visualizing the resource workload
   */
  private JFreeChart createResourceWorkloadChart(final TimeSeriesCollection dataset, final DateMidnight beginDay, final DateMidnight endDay) {
    // configure x-axis label and tick units depending on the time interval size
    final int durationInDays = new Duration(beginDay, endDay).toStandardDays().getDays();
    String timeAxisLabel = getString("plugins.chimney.resourceworkload.timeaxislabel")+" ";
    DateTickUnit dateTickUnit = null;
    if (durationInDays < 70) {
      dateTickUnit = new DateTickUnit(DateTickUnitType.DAY, 1, new SimpleDateFormat("d"));
      timeAxisLabel += "("+getString("plugins.chimney.resourceworkload.days")+")";
    } else if (durationInDays < 365) {
      dateTickUnit = new DateTickUnit(DateTickUnitType.DAY, 7, new SimpleDateFormat("w"));
      timeAxisLabel += "("+getString("plugins.chimney.resourceworkload.weeks")+")";
    } else if (durationInDays <= 365*4) {
      dateTickUnit = new DateTickUnit(DateTickUnitType.MONTH, 1, new SimpleDateFormat("M"));
      timeAxisLabel += "("+getString("plugins.chimney.resourceworkload.months")+")";
    } else if (durationInDays <= 365*20) {
      dateTickUnit = new DateTickUnit(DateTickUnitType.YEAR, 1, new SimpleDateFormat("yyyy"));
      timeAxisLabel += "("+getString("plugins.chimney.resourceworkload.years")+")";
    } else {
      dateTickUnit = new DateTickUnit(DateTickUnitType.YEAR, 1, new SimpleDateFormat("yy"));
      timeAxisLabel += "("+getString("plugins.chimney.resourceworkload.years")+")";
    }

    /*
    // create the chart
    final JFreeChart chart = ChartFactory.createXYAreaChart(
        getString("plugins.chimney.resourceworkload.charttitle"),
        timeAxisLabel,
        getString("plugins.chimney.resourceworkload.workloadaxislabel")+" ("+getString("plugins.chimney.resourceworkload.hours")+")",
        dataSetGen.createDataset(beginDay, endDay),
        PlotOrientation.VERTICAL,
        true, true, false
    );
     */

    final XYAreaRenderer renderer = new XYAreaRenderer(XYAreaRenderer.AREA);

    // configure time axis
    final DateAxis timeAxis = new DateAxis(timeAxisLabel);
    timeAxis.setTickUnit(dateTickUnit);
    timeAxis.setLowerMargin(0.0);
    timeAxis.setUpperMargin(0.0);

    final NumberAxis yAxis = new NumberAxis(
        getString("plugins.chimney.resourceworkload.workloadaxislabel")
        + " ("+getString("plugins.chimney.resourceworkload.hours")+")"
        );

    if (drawWarnSymbolAtOverPlanning) {
      yAxis.setUpperMargin(0.2);
    } else {
      yAxis.setUpperMargin(0.1);
    }
    final XYPlot plot = new XYPlot(
        dataset,
        timeAxis,
        yAxis,
        renderer
        );
    plot.setOrientation(PlotOrientation.VERTICAL);
    plot.setForegroundAlpha(0.5f);

    final JFreeChart chart = new JFreeChart(
        getString("plugins.chimney.resourceworkload.charttitle")+": "+user.getDisplayUsername(),
        JFreeChart.DEFAULT_TITLE_FONT,
        plot, true);

    ChartFactory.getChartTheme().apply(chart);
    //return chart;
    // configure additional visual properties
    chart.setBackgroundPaint(Color.white);
    //chart.setBorderPaint(Color.magenta);

    // configure time axis
    plot.setBackgroundPaint(new Color(250,240,250));
    plot.setDomainGridlinePaint(Color.gray);
    plot.setRangeGridlinePaint(Color.gray);

    //XYItemRenderer renderer = plot.getRenderer(0);

    return chart;
  }

  /**
   * 
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.resourceworkload.heading");
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
    items.add(BreadcrumbConstants.RESOURCE_WORKLOAD_VIEW);
  }

}
