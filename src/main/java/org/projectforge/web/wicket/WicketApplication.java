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

package org.projectforge.web.wicket;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.convert.ConverterLocator;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.common.ExceptionHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.core.CronSetup;
import org.projectforge.core.ProjectForgeException;
import org.projectforge.database.HibernateUtils;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.access.AccessListPage;
import org.projectforge.web.address.AddressListPage;
import org.projectforge.web.address.AddressViewPage;
import org.projectforge.web.address.SendSmsPage;
import org.projectforge.web.admin.AdminPage;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.admin.UpdatePage;
import org.projectforge.web.book.BookListPage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.core.ConfigurationListPage;
import org.projectforge.web.core.LoginFilter;
import org.projectforge.web.doc.TutorialPage;
import org.projectforge.web.fibu.AuftragEditPage;
import org.projectforge.web.fibu.AuftragListPage;
import org.projectforge.web.fibu.BankAccountListPage;
import org.projectforge.web.fibu.EingangsrechnungListPage;
import org.projectforge.web.fibu.Kost1ListPage;
import org.projectforge.web.fibu.Kost2ArtListPage;
import org.projectforge.web.fibu.Kost2ListPage;
import org.projectforge.web.fibu.ProjektListPage;
import org.projectforge.web.fibu.RechnungListPage;
import org.projectforge.web.gantt.GanttChartEditPage;
import org.projectforge.web.gantt.GanttChartListPage;
import org.projectforge.web.gwiki.GWikiContainerPage;
import org.projectforge.web.gwiki.GWikiLocalizer;
import org.projectforge.web.humanresources.HRListPage;
import org.projectforge.web.humanresources.HRPlanningListPage;
import org.projectforge.web.imagecropper.ImageCropperPage;
import org.projectforge.web.meb.MebListPage;
import org.projectforge.web.orga.ContractListPage;
import org.projectforge.web.orga.PostausgangListPage;
import org.projectforge.web.orga.PosteingangListPage;
import org.projectforge.web.scripting.ScriptListPage;
import org.projectforge.web.statistics.PersonalStatisticsPage;
import org.projectforge.web.statistics.SystemStatisticsPage;
import org.projectforge.web.task.TaskEditPage;
import org.projectforge.web.task.TaskListPage;
import org.projectforge.web.task.TaskTreePage;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.user.GroupListPage;
import org.projectforge.web.user.MyAccountEditPage;
import org.projectforge.web.user.UserListPage;
import org.projectforge.web.user.UserPrefListPage;
import org.projectforge.web.wicket.converter.MyDateConverter;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see org.wicket.demo.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WicketApplication.class);

  public static final String RESOURCE_BUNDLE_NAME = "I18nResources";

  private static final String BOOKMARK_ACCESS_LIST = "accessList";

  private static final String BOOKMARK_ADMIN = "admin";

  private static final String BOOKMARK_ADDRESS_LIST = "addressList";

  private static final String BOOKMARK_ADDRESS_VIEW = "addressView";

  private static final String BOOKMARK_AUFTRAG_EDIT = "editAuftrag";

  private static final String BOOKMARK_AUFTRAG_LIST = "auftragList";

  private static final String BOOKMARK_BANK_ACCOUNT_LIST = "bankAccountList";

  private static final String BOOKMARK_BOOK_LIST = "bookList";

  private static final String BOOKMARK_CALENDAR_PAGE = "calendarPage";

  private static final String BOOKMARK_CONFIGURATION_PAGE = "configurationPage";

  private static final String BOOKMARK_CONTRACT_LIST = "contractList";

  private static final String BOOKMARK_EINGANGS_RECHNUNG_LIST = "eingangsRechnungList";

  private static final String BOOKMARK_ERROR_PAGE = "errorPage";

  private static final String BOOKMARK_FEEDBACK_PAGE = "feedback";

  private static final String BOOKMARK_GANTT_EDIT = "ganttEdit";

  private static final String BOOKMARK_GANTT_LIST = "ganttList";

  private static final String BOOKMARK_GROUP_LIST = "groupList";

  private static final String BOOKMARK_GWIKI_CONTAINER_PAGE = "gwikiContainer";

  private static final String BOOKMARK_HR_PLANNING_LIST = "hrPlanningList";

  private static final String BOOKMARK_HR_LIST = "hrList";

  private static final String BOOKMARK_IMAGECROPPER = "imageCropper";

  private static final String BOOKMARK_KOST1_LIST = "kost1List";

  private static final String BOOKMARK_KOST2_LIST = "kost2List";

  private static final String BOOKMARK_KOST2ART_LIST = "kost2ArtList";

  private static final String BOOKMARK_MEB_LIST = "mebList";

  private static final String BOOKMARK_MY_ACCOUNT = "myAccount";

  private static final String BOOKMARK_PERSONAL_STATISTICS = "personalStatistics";

  private static final String BOOKMARK_POSTAUSGANG_LIST = "postausgangList";

  private static final String BOOKMARK_POSTEINGANG_LIST = "posteingangList";

  private static final String BOOKMARK_PROJEKT_LIST = "projektList";

  private static final String BOOKMARK_RECHNUNG_LIST = "rechnungList";

  private static final String BOOKMARK_SCRIPT_LIST = "scriptList";

  private static final String BOOKMARK_SEND_SMS = "sendSms";

  private static final String BOOKMARK_SETUP_PAGE = "setup";

  private static final String BOOKMARK_SYSTEM_STATISTICS = "systemStatistics";

  private static final String BOOKMARK_SYSTEM_UPDATE = "systemUpdate";

  private static final String BOOKMARK_TASK_EDIT = "taskEdit";

  private static final String BOOKMARK_TASK_TREE = "taskTree";

  private static final String BOOKMARK_TASK_LIST = "taskList";

  private static final String BOOKMARK_TIMESHEET_LIST = "timesheetList";

  private static final String BOOKMARK_TIMESHEET_EDIT = "timesheetEdit";

  private static final String BOOKMARK_TUTORIAL = "tutorial";

  private static final String BOOKMARK_USER_LIST = "userList";

  private static final String BOOKMARK_USER_PREF_LIST = "userPrefList";

  private static final String BOOKMARK_VIEW_ADDRESS = "viewAddress";

  private static Boolean developmentModus;

  private static String alertMessage;

  private static Map<Class< ? extends Page>, String> mountedPages = new HashMap<Class< ? extends Page>, String>();

  @SpringBean(name = "wicketApplicationFilter")
  private WicketApplicationFilter wicketApplicationFilter;

  private UserXmlPreferencesCache userXmlPreferencesCache;

  @SpringBean(name = "configurationDao")
  private ConfigurationDao configurationDao;

  private Configuration configuration;

  @SpringBean(name = "cronSetup")
  private CronSetup cronSetup;

  /**
   * At application start the flag developmentModus is perhaps not already set. If possible please use isDevelopmentSystem() instead.
   * @return
   */
  public static Boolean isDevelopmentModus()
  {
    return developmentModus;
  }

  public static String getBookmarkableMountPath(final Class< ? extends Page> pageClass)
  {
    return mountedPages.get(pageClass);
  }

  public void setWicketApplicationFilter(WicketApplicationFilter wicketApplicationFilter)
  {
    this.wicketApplicationFilter = wicketApplicationFilter;
  }

  public void setUserXmlPreferencesCache(UserXmlPreferencesCache userXmlPreferencesCache)
  {
    this.userXmlPreferencesCache = userXmlPreferencesCache;
  }

  public void setConfiguration(Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setConfigurationDao(ConfigurationDao configurationDao)
  {
    this.configurationDao = configurationDao;
  }

  public void setCronSetup(CronSetup cronSetup)
  {
    this.cronSetup = cronSetup;
  }

  /**
   * Returns the alert message, if exists. The alert message will be displayed on every screen (red on top) and is edit-able via
   * Administration -> System.
   */
  public static String getAlertMessage()
  {
    return WicketApplication.alertMessage;
  }

  /**
   * @param msg
   * @see #getAlertMessage()
   */
  public static void setAlertMessage(String alertMessage)
  {
    WicketApplication.alertMessage = alertMessage;
  }

  /**
   * Constructor
   */
  public WicketApplication()
  {
    super();
  }

  /**
   * Own solution: uses development parameter of servlet context init parameter (see context.xml or server.xml).
   * @return DEVELOPMENT, if development variable of servlet context is set to "true" otherwise DEPLOYMENT.
   * @see org.apache.wicket.protocol.http.WebApplication#getConfigurationType()
   * @see Application#DEPLOYMENT
   * @see Application#DEVELOPMENT
   */
  @Override
  public String getConfigurationType()
  {
    if (isDevelopmentSystem() == true) {
      return DEVELOPMENT;
    }
    return DEPLOYMENT;
  }

  private void mountPage(final String path, final Class< ? extends Page> pageClass)
  {
    mountBookmarkablePage(path, pageClass);
    mountedPages.put(pageClass, path);
  }

  @Override
  protected void init()
  {
    super.init();
    getApplicationSettings().setDefaultMaximumUploadSize(Bytes.megabytes(100));
    getMarkupSettings().setDefaultMarkupEncoding("utf-8");
    MyAuthorizationStrategy authStrategy = new MyAuthorizationStrategy();
    getSecuritySettings().setAuthorizationStrategy(authStrategy);
    // getSecuritySettings().setUnauthorizedComponentInstantiationListener(authStrategy);
    getResourceSettings().setResourceStreamLocator(new MyResourceStreamLocator());
    getResourceSettings().addStringResourceLoader(new BundleStringResourceLoader(RESOURCE_BUNDLE_NAME));
    getResourceSettings().setThrowExceptionOnMissingResource(false); // Don't throw MissingResourceException for missing i18n keys.
    getApplicationSettings().setPageExpiredErrorPage(PageExpiredPage.class); // Don't show expired page.
    getSessionSettings().setMaxPageMaps(20); // Map up to 20 pages per session (default is 5).
    addComponentInstantiationListener(new SpringComponentInjector(this));
    // mountPage("login", LoginPage.class);
    mountPage(BOOKMARK_IMAGECROPPER, ImageCropperPage.class);
    mountPage(BOOKMARK_ACCESS_LIST, AccessListPage.class);
    mountPage(BOOKMARK_ADMIN, AdminPage.class);
    mountPage(BOOKMARK_ADDRESS_LIST, AddressListPage.class);
    mountPage(BOOKMARK_ADDRESS_VIEW, AddressViewPage.class);
    mountPage(BOOKMARK_AUFTRAG_LIST, AuftragListPage.class);
    mountPage(BOOKMARK_AUFTRAG_EDIT, AuftragEditPage.class);
    mountPage(BOOKMARK_BANK_ACCOUNT_LIST, BankAccountListPage.class);
    mountPage(BOOKMARK_BOOK_LIST, BookListPage.class);
    mountPage(BOOKMARK_CALENDAR_PAGE, CalendarPage.class);
    mountPage(BOOKMARK_CONFIGURATION_PAGE, ConfigurationListPage.class);
    mountPage(BOOKMARK_CONTRACT_LIST, ContractListPage.class);
    mountPage(BOOKMARK_EINGANGS_RECHNUNG_LIST, EingangsrechnungListPage.class);
    mountPage(BOOKMARK_ERROR_PAGE, ErrorPage.class);
    mountPage(BOOKMARK_FEEDBACK_PAGE, FeedbackPage.class);
    mountPage(BOOKMARK_GANTT_EDIT, GanttChartEditPage.class);
    mountPage(BOOKMARK_GANTT_LIST, GanttChartListPage.class);
    mountPage(BOOKMARK_GROUP_LIST, GroupListPage.class);
    mountPage(BOOKMARK_GWIKI_CONTAINER_PAGE, GWikiContainerPage.class);
    mountPage(BOOKMARK_KOST1_LIST, Kost1ListPage.class);
    mountPage(BOOKMARK_KOST2_LIST, Kost2ListPage.class);
    mountPage(BOOKMARK_KOST2ART_LIST, Kost2ArtListPage.class);
    mountPage(BOOKMARK_PROJEKT_LIST, ProjektListPage.class);
    mountPage(BOOKMARK_RECHNUNG_LIST, RechnungListPage.class);
    mountPage(BOOKMARK_MEB_LIST, MebListPage.class);
    mountPage(BOOKMARK_MY_ACCOUNT, MyAccountEditPage.class);
    mountPage(BOOKMARK_PERSONAL_STATISTICS, PersonalStatisticsPage.class);
    mountPage(BOOKMARK_POSTAUSGANG_LIST, PostausgangListPage.class);
    mountPage(BOOKMARK_POSTEINGANG_LIST, PosteingangListPage.class);
    mountPage(BOOKMARK_HR_PLANNING_LIST, HRPlanningListPage.class);
    mountPage(BOOKMARK_HR_LIST, HRListPage.class);
    mountPage(BOOKMARK_SCRIPT_LIST, ScriptListPage.class);
    mountPage(BOOKMARK_SEND_SMS, SendSmsPage.class);
    mountPage(BOOKMARK_SETUP_PAGE, SetupPage.class);
    mountPage(BOOKMARK_SYSTEM_STATISTICS, SystemStatisticsPage.class);
    mountPage(BOOKMARK_SYSTEM_UPDATE, UpdatePage.class);
    mountPage(BOOKMARK_TASK_EDIT, TaskEditPage.class);
    mountPage(BOOKMARK_TASK_TREE, TaskTreePage.class);
    mountPage(BOOKMARK_TASK_LIST, TaskListPage.class);
    mountPage(BOOKMARK_TIMESHEET_LIST, TimesheetListPage.class);
    mountPage(BOOKMARK_TIMESHEET_EDIT, TimesheetEditPage.class);
    mountPage(BOOKMARK_TUTORIAL, TutorialPage.class);
    mountPage(BOOKMARK_USER_LIST, UserListPage.class);
    mountPage(BOOKMARK_USER_PREF_LIST, UserPrefListPage.class);
    mountPage(BOOKMARK_VIEW_ADDRESS, AddressViewPage.class);
    getApplicationSettings().setInternalErrorPage(ErrorPage.class);

    final WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {}, webApplicationContext);
    ctx.getBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    final LocalSessionFactoryBean localSessionFactoryBean = (LocalSessionFactoryBean) ctx.getBean("&sessionFactory");
    org.hibernate.cfg.Configuration hibernateConfiguration = localSessionFactoryBean.getConfiguration();
    HibernateUtils.setConfiguration(hibernateConfiguration);
    final ServletContext servletContext = getServletContext();
    final String configContextPath = configuration.getServletContextPath();
    String contextPath;
    if (StringUtils.isBlank(configContextPath) == true) {
      contextPath = servletContext.getContextPath();
      configuration.setServletContextPath(contextPath);
    } else {
      contextPath = configContextPath;
    }
    log.info("Using servlet context path: " + contextPath);
    if (configuration.getApplicationContext() == null) {
      configuration.setApplicationContext(ctx);
    }
    configuration.setConfigurationDao(configurationDao);
    WicketUtils.setContextPath(contextPath);
    LoginFilter.setServletContextPath(contextPath);
    if (this.wicketApplicationFilter != null) {
      this.wicketApplicationFilter.setApplication(this);
    } else {
      throw new RuntimeException("this.wicketApplicationFilter is null");
    }
    getResourceSettings().setLocalizer(new GWikiLocalizer("edit/StandardI18n"));
    log.info("Default TimeZone is: " + TimeZone.getDefault());
    log.info("user.timezone is: " + System.getProperty("user.timezone"));
    cronSetup.initialize();
    log.fatal("Initialized.");
  }

  @Override
  protected void onDestroy()
  {
    log.info("Syncing all user preferences to database.");
    userXmlPreferencesCache.forceReload();
    cronSetup.shutdown();
    log.fatal("Destroyed");
  }

  public boolean isDevelopmentSystem()
  {
    if (developmentModus == null) {
      final String value = getServletContext().getInitParameter("development");
      developmentModus = "true".equals(value);
    }
    return developmentModus;
  }

  /**
   * @see org.apache.wicket.Application#getHomePage()
   */
  public Class<CalendarPage> getHomePage()
  {
    return CalendarPage.class;
  }

  @Override
  public Session newSession(Request request, Response response)
  {
    return new MySession(request);
  }

  /**
   * From http://www.danwalmsley.com/2009/04/08/apache-wicket-on-google-app-engine-for-java/<br/>
   * Override the newSessionStore() method to return HttpSessionStore, because the default second level session store uses java.io.File,
   * which is sometimes not allowed.
   * @see org.apache.wicket.Application#newSessionStore()
   */
  /*
   * @Override protected ISessionStore newSessionStore() { return new org.apache.wicket.protocol.http.HttpSessionStore(this); }
   */

  /**
   * 
   */
  protected IConverterLocator newConverterLocator()
  {
    final ConverterLocator converterLocator = new ConverterLocator();
    converterLocator.set(java.util.Date.class, new MyDateConverter());
    converterLocator.set(java.sql.Date.class, new MyDateConverter(java.sql.Date.class, "S-"));
    return converterLocator;
  }

  /**
   * Own error page for deployment mode and UserException and AccessException.
   * @see org.apache.wicket.protocol.http.WebApplication#newRequestCycleProcessor()
   */
  @Override
  protected IRequestCycleProcessor newRequestCycleProcessor()
  {
    return new WebRequestCycleProcessor() {
      @Override
      protected Page onRuntimeException(Page page, RuntimeException e)
      {
        final Throwable rootCause = ExceptionHelper.getRootCause(e);
        if (page != null && page instanceof AbstractSecuredPage && rootCause instanceof ProjectForgeException) {
          // Show exception message as error message in feedback panel.
          final AbstractSecuredPage securedPage = (AbstractSecuredPage) page;
          final String msg = ErrorPage.getExceptionMessage(securedPage, (ProjectForgeException) rootCause, true);
          page.error(msg);
          return page;
        }
        if (isDevelopmentSystem() == true) {
          return super.onRuntimeException(page, e);
        } else {
          // Show always this error page in production mode:
          return new ErrorPage(e);
        }
      }
    };
  }

  /**
   * Log only non ProjectForge exceptions.
   * @see org.apache.wicket.protocol.http.WebApplication#newRequestCycle(org.apache.wicket.Request, org.apache.wicket.Response)
   */
  @Override
  public RequestCycle newRequestCycle(final Request request, final Response response)
  {
    return new WebRequestCycle(this, (WebRequest) request, (WebResponse) response) {
      @Override
      protected void logRuntimeException(RuntimeException e)
      {
        final Throwable rootCause = ExceptionHelper.getRootCause(e);
        if (rootCause instanceof ProjectForgeException == false) {
          super.logRuntimeException(e);
        }
      }
    };
  }
}
