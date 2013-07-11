/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.gantt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.ChimneyGanttActivity;
import org.projectforge.plugins.chimney.gantt.CyclicDependencyException;
import org.projectforge.plugins.chimney.gantt.GanttActivityMapper;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.gantt.IllegalTransientActivityException;
import org.projectforge.plugins.chimney.gantt.MissingFixedBeginDateException;
import org.projectforge.plugins.chimney.gantt.WbsActivityNavigator;
import org.projectforge.plugins.chimney.resources.FileResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.wbs.WbsNodeUtils;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.gantt.model.IGanttActivity;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreeTabsPanel;

/**
 * Wicket page for visualization of gantt charts based on an improved version of jsgantt (http://www.jsgantt.com/).
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public class GanttPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = -4069836308854129602L;

  public static final String PAGE_ID = "ganttView";

  @SpringBean(name="projectDao")
  private ProjectDao projectDao;

  @SpringBean(name="wbsActivityDao")
  private WbsActivityDao activityDao;

  @SpringBean
  private WbsActivityNavigator wbsActivityNavigator;

  @SpringBean
  private IScheduler scheduler;

  @Transient
  final transient Integer[] projectIds;

  @Transient
  final transient List<? extends IGanttActivity> ganttActivities;

  /**
   * Default constructor which creates a page for visualization of all defined activities
   * (this constructor may be removed later)
   */
  public GanttPage() {
    super(new PageParameters());

    final BaseSearchFilter filter = new BaseSearchFilter();
    filter.setIgnoreDeleted(true);
    final List<ProjectDO> allProjectsList = projectDao.getList(filter);

    this.projectIds = null;
    this.ganttActivities = ganttMapMultipleProjects(allProjectsList);
  }

  /**
   * Constructor which creates a gantt chart page for visualization of all given projects
   * @param projectIds List of project ids which will be visualized in a gantt chart
   */
  public GanttPage(final Integer[] projectIds)
  {
    super(new PageParameters());

    final List<ProjectDO>projectsList = new ArrayList<ProjectDO>();
    for(final Integer projectId : projectIds){
      projectsList.add(projectDao.getOrLoad(projectId));
    }
    this.projectIds = projectIds;
    this.ganttActivities = ganttMapMultipleProjects(projectsList);
  }

  /**
   * Creates a gantt chart page which visualizes the given list of gantt activities
   * @param activities gantt activities which will be visualized in the gantt chart
   */
  public GanttPage(final List<? extends IGanttActivity> activities)
  {
    super(new PageParameters());
    this.projectIds = null;
    this.ganttActivities = activities;
  }


  /**
   * Creates a gantt chart page which visualizes all activities of a given project subtree with root 'wbsNode'
   * @param wbsNode root of the project subtree which will be visualized in a gantt chart
   */
  public GanttPage(final AbstractWbsNodeDO wbsNode)
  {
    super(new PageParameters());
    final ProjectDO project = WbsNodeUtils.getProject(wbsNode);
    final Integer[] projectsList = {project.getId()};
    this.projectIds = projectsList;
    ganttActivities = ganttMapSingleNode(wbsNode);
  }

  private List<ChimneyGanttActivity> ganttMapSingleNode(final AbstractWbsNodeDO wbsNode){

    final WbsActivityDO activity = activityDao.getByOrCreateFor(wbsNode);

    //final FixedProjectBeginScheduler scheduler = new FixedProjectBeginScheduler(wbsActivityNavigator, neededDurationProvider, workdayCalculator);

    final GanttActivityMapper gam = new GanttActivityMapper();
    gam.setScheduler(scheduler);
    gam.setWbsActivityNavigator(wbsActivityNavigator);
    List<ChimneyGanttActivity> recursiveMap;
    final ProjectDO project = WbsNodeUtils.getProject(wbsNode);
    final WbsActivityDO projectActivity = activityDao.getByOrCreateFor(project);

    String exceptionMsg = null;
    try {
      scheduler.schedule(projectActivity);
      recursiveMap = gam.map(activity);
    } catch (final MissingFixedBeginDateException mfbde) {
      recursiveMap = new LinkedList<ChimneyGanttActivity>(); // create empty list of gantt activities
      exceptionMsg = getLocalizedMessage(mfbde.getI18nKey(), mfbde.getParams());
    } catch (final IllegalTransientActivityException itae) {
      recursiveMap = new LinkedList<ChimneyGanttActivity>(); // create empty list of gantt activities
      exceptionMsg = getLocalizedMessage(itae.getI18nKey(), itae.getParams());
    } catch (final CyclicDependencyException cde) {
      recursiveMap = new LinkedList<ChimneyGanttActivity>(); // create empty list of gantt activities
      exceptionMsg = getLocalizedMessage(cde.getI18nKey(), cde.getParams());
    }

    if (exceptionMsg != null) {
      warn(
          exceptionMsg + " "
              + getString("plugins.chimney.gantt.error.cantcalculategantt")
          );
    }

    setAllGanttLinks(recursiveMap);
    return recursiveMap;
  }

  private List<ChimneyGanttActivity> ganttMapMultipleProjects(final List<ProjectDO> projectsList){

    //final FixedProjectBeginScheduler scheduler = new FixedProjectBeginScheduler(wbsActivityNavigator, neededDurationProvider, workdayCalculator);

    final GanttActivityMapper gam = new GanttActivityMapper();
    gam.setScheduler(scheduler);
    gam.setWbsActivityNavigator(wbsActivityNavigator);

    final List<ChimneyGanttActivity> ganttActivities = new LinkedList<ChimneyGanttActivity>();

    for (final Iterator<ProjectDO> allProjectsIt=projectsList.iterator(); allProjectsIt.hasNext(); ) {
      final ProjectDO project = allProjectsIt.next();
      ganttActivities.addAll(ganttMapSingleNode(project));
    }
    return ganttActivities;
  }
  private void setAllGanttLinks(final List<ChimneyGanttActivity> activities) {
    //AbstractSecuredPage editPage;
    String urlLink;
    for(final ChimneyGanttActivity a: activities) {
      if (a.getWbsNode() != null ) {
        try {
          //editPage = WbsNodeEditPageVisitor.createEditPageFor(a.getWbsNode());
          urlLink = "";
          if (urlLink!=null)
            a.setLinkUrl(urlLink);
        } catch (final UnsupportedOperationException uoe) {
          // no edit page available -> do nothing
        }
      }
    }
  }

  private String getUrlStringToPage(final IRequestablePage page) {
    return urlFor(new RenderPageRequestHandler( new PageProvider(page))).toString();
  }


  /**
   * adds all components to the page
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize() {
    super.onInitialize();
    addTabs(projectIds);
    initPage(ganttActivities);
  }
  /**
   * creates the gantt page for the given list of gantt activities
   * @param activities gantt activities which will be visualized in the gantt chart
   */
  protected void initPage(final List<? extends IGanttActivity> activities) {

    addFeedbackPanel();

    body.add(new Label("heading", getString("plugins.chimney.gantt.title")));

    body.add( new Label("chartScrollWidthLabel", getString("plugins.chimney.gantt.chartscrollpanelwidth") +":" ) );
    // create xml page for jsgantt xml input
    final WebPage ganttXmlPage = new GanttTaskXmlPage(activities);
    // get url to the xml page
    final String xmlUrl = getUrlStringToPage(ganttXmlPage);
    // body.add( new ExternalLink("ganttXmlLink", xmlUrl) );

    // create the config script (javascript) to configure jsgantt and I18next (https://npmjs.org/package/i18next)
    final StringBuilder ganttConfigScript = new StringBuilder();
    ganttConfigScript.append("taskXmlUrl = '"+xmlUrl+"';");
    ganttConfigScript.append('\n');
    ganttConfigScript.append("i18nRessources = "+createI18nJSON()+";");
    ganttConfigScript.append('\n');

    // embed the script into the page
    final Label jsGanttConfigScriptLabel = new Label("jsGanttConfigScript",
        "/*<![CDATA[*/"+ganttConfigScript.toString()+"/*]]>*/");
    jsGanttConfigScriptLabel.setEscapeModelStrings(false);

    body.add(jsGanttConfigScriptLabel);
  }


  private void addTabs(final Integer[] projectIds)
  {
    body.add(new ProjectTreeTabsPanel("tabs", projectIds, this));
  }


  /**
   * creates a json notation for I18n used by I18next (https://npmjs.org/package/i18next)
   * @return string representation of an I18n json object
   */
  private String createI18nJSON() {
    final StringBuilder str = new StringBuilder();
    str.append("{ dev: { translation : { jsgantt: { ");

    str.append("minutes : '");
    str.append( getString("plugins.chimney.gantt.minutes") );
    str.append("', ");

    str.append("hours : '");
    str.append( getString("plugins.chimney.gantt.hours") );
    str.append("', ");

    str.append("days : '");
    str.append( getString("plugins.chimney.gantt.days") );
    str.append("', ");

    str.append("weeks : '");
    str.append( getString("plugins.chimney.gantt.weeks") );
    str.append("', ");

    str.append("months : '");
    str.append( getString("plugins.chimney.gantt.months") );
    str.append("', ");

    str.append("quarters : '");
    str.append( getString("plugins.chimney.gantt.quarters") );
    str.append("', ");

    str.append("format : '");
    str.append( getString("plugins.chimney.gantt.format") );
    str.append("', ");

    str.append("resource : '");
    str.append( getString("plugins.chimney.gantt.resource") );
    str.append("', ");

    str.append("duration : '");
    str.append( getString("plugins.chimney.gantt.duration") );
    str.append("', ");

    str.append("progress : '");
    str.append( getString("plugins.chimney.gantt.progress") );
    str.append("', ");

    str.append("startdate : '");
    str.append( getString("plugins.chimney.gantt.startdate") );
    str.append("', ");

    str.append("enddate : '");
    str.append( getString("plugins.chimney.gantt.enddate") );
    str.append("', ");

    str.append("displaydateformat : '");
    str.append( getString("plugins.chimney.gantt.displaydateformat") );
    str.append("'");


    str.append("}}}}");

    return str.toString();
  };

  /**
   * load all needed dependencies to render the page:
   * - i18next (https://npmjs.org/package/i18next)
   * - jsgantt (http://www.jsgantt.com/)
   * 
   * @see org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
   */
  @Override
  public void renderHead(final IHeaderResponse response) {
    super.renderHead(response);
    response.render(JavaScriptHeaderItem.forReference(FileResources.I18NNEXT_JS));
    response.render(CssReferenceHeaderItem.forReference(FileResources.JSGANTT_CSS));
    response.render(JavaScriptHeaderItem.forReference(FileResources.JSGANTT_JS));
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
    items.add(BreadcrumbConstants.GANTT_VIEW);
  }

  /**
   * 
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.gantt.title");
  }

}
