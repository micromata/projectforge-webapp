/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.ExceptionHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ProjectForgeApp;
import org.projectforge.database.MyDatabaseUpdateDao;
import org.projectforge.plugins.core.PluginsRegistry;
import org.projectforge.registry.Registry;
import org.projectforge.user.Login;
import org.projectforge.user.LoginDefaultHandler;
import org.projectforge.user.LoginHandler;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserDao;
import org.projectforge.web.UserFilter;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.registry.WebRegistry;
import org.projectforge.web.wicket.converter.MyDateConverter;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import de.micromata.less.LessWicketApplicationInstantiator;
import de.micromata.wicket.request.mapper.PageParameterAwareMountedMapper;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see org.projectforge.web.AbstractStartHelper.demo.Start#main(String[])
 */
public class WicketApplication extends WebApplication implements WicketApplicationInterface
{
  // If you change this you have to change this also in PFApplication. This is used for updating the hsqldb.
  // private static final String SYSTEM_PROPERTY_HSQLDB_18_UPDATE = "hsqldb18Update";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WicketApplication.class);

  public static final String RESOURCE_BUNDLE_NAME = "I18nResources";

  static Class< ? extends WebPage> DEFAULT_PAGE = CalendarPage.class;

  private static Boolean developmentMode;

  private static Boolean stripWicketTags;

  private static String alertMessage;

  private static Map<Class< ? extends Page>, String> mountedPages = new HashMap<Class< ? extends Page>, String>();

  @SpringBean(name = "wicketApplicationFilter")
  private WicketApplicationFilter wicketApplicationFilter;

  private ProjectForgeApp projectForgeApp;

  /**
   * At application start the flag developmentMode is perhaps not already set. If possible please use {@link #isDevelopmentSystem()}
   * instead.<br/>
   * Please use {@link WebConfiguration#isDevelopmentMode()}.
   */
  public static Boolean internalIsDevelopmentMode()
  {
    return developmentMode;
  }

  /**
   * @return true if the application is running and is full available, false e. g. if ProjectForge runs in maintenance mode or is in
   *         start-up phase.
   */
  public static boolean isUpAndRunning()
  {
    return ProjectForgeApp.getInstance().isUpAndRunning();
  }

  /**
   * This method should only be called in test cases!
   * @param upAndRunning the upAndRunning to set
   */
  public static void internalSetUpAndRunning(final boolean upAndRunning)
  {
    ProjectForgeApp.getInstance().internalSetUpAndRunning(upAndRunning);
  }

  /**
   * Please don't use this method, use {@link WicketUtils#getDefaultPage()} instead.
   * @return
   */
  public static Class< ? extends WebPage> internalGetDefaultPage()
  {
    return DEFAULT_PAGE;
  }

  /**
   * Use this method only if you want to change the default page (if no other is defined in config.xml).
   * @param defaultPage
   */
  public static void setDefaultPage(final Class< ? extends WebPage> defaultPage)
  {
    DEFAULT_PAGE = defaultPage;
  }

  public static String getBookmarkableMountPath(final Class< ? extends Page> pageClass)
  {
    return mountedPages.get(pageClass);
  }

  public void setWicketApplicationFilter(final WicketApplicationFilter wicketApplicationFilter)
  {
    this.wicketApplicationFilter = wicketApplicationFilter;
  }

  /**
   * Returns the alert message, if exists. The alert message will be displayed on every screen (red on top) and is edit-able via
   * Administration -> System.
   */
  public static String getAlertMessage()
  {
    if (UserFilter.isUpdateRequiredFirst() == true) {
      return "Maintenance mode: Please restart ProjectForge after finishing." + (alertMessage != null ? " " + alertMessage : "");
    } else {
      return alertMessage;
    }
  }

  /**
   * @param alertMessage
   * @see #getAlertMessage()
   */
  public static void setAlertMessage(final String alertMessage)
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
   */
  @Override
  public RuntimeConfigurationType getConfigurationType()
  {
    if (isDevelopmentSystem() == true) {
      return RuntimeConfigurationType.DEVELOPMENT;
    }
    return RuntimeConfigurationType.DEPLOYMENT;
  }

  @Override
  protected void init()
  {
    super.init();
    // CryptoMapper doesn't work with FullCalendar.
    // setRootRequestMapper(new CryptoMapper(getRootRequestMapper(), this));
    final XmlWebApplicationContext webApplicationContext = (XmlWebApplicationContext) WebApplicationContextUtils
        .getWebApplicationContext(getServletContext());
    final ConfigurableListableBeanFactory beanFactory = webApplicationContext.getBeanFactory();
    beanFactory.autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    final LocalSessionFactoryBean localSessionFactoryBean = (LocalSessionFactoryBean) beanFactory.getBean("&sessionFactory");
    final org.hibernate.cfg.Configuration hibernateConfiguration = localSessionFactoryBean.getConfiguration();
    final PluginsRegistry pluginsRegistry = PluginsRegistry.instance();
    pluginsRegistry.set(getResourceSettings());
    projectForgeApp = ProjectForgeApp.init(beanFactory, hibernateConfiguration);
    // Own error page for deployment mode and UserException and AccessException.
    getRequestCycleListeners().add(new AbstractRequestCycleListener() {
      /**
       * Log only non ProjectForge exceptions.
       * @see org.apache.wicket.request.cycle.AbstractRequestCycleListener#onException(org.apache.wicket.request.cycle.RequestCycle,
       *      java.lang.Exception)
       */
      @Override
      public IRequestHandler onException(final RequestCycle cycle, final Exception ex)
      {
        // in case of expired session, please redirect to home page
        if (ex instanceof PageExpiredException) {
          return super.onException(cycle, ex);
        }
        final Throwable rootCause = ExceptionHelper.getRootCause(ex);
        // log.error(rootCause.getMessage(), ex);
        // if (rootCause instanceof ProjectForgeException == false) {
        // return super.onException(cycle, ex);
        // }
        // return null;
        if (isDevelopmentSystem() == true) {
          log.error(ex.getMessage(), ex);
          if (rootCause instanceof SQLException) {
            SQLException next = (SQLException) rootCause;
            while ((next = next.getNextException()) != null) {
              log.error(next.getMessage(), next);
            }
          }
          return super.onException(cycle, ex);
        } else {
          // Show always this error page in production mode:
          return new RenderPageRequestHandler(new PageProvider(new ErrorPage(ex)));
        }
      }
    });

    getApplicationSettings().setDefaultMaximumUploadSize(Bytes.megabytes(100));
    getMarkupSettings().setDefaultMarkupEncoding("utf-8");
    final MyAuthorizationStrategy authStrategy = new MyAuthorizationStrategy();
    getSecuritySettings().setAuthorizationStrategy(authStrategy);
    getSecuritySettings().setUnauthorizedComponentInstantiationListener(authStrategy);
    // Prepend the resource bundle for overwriting some Wicket default localizations (such as StringValidator.*)
    getResourceSettings().getStringResourceLoaders().add(new BundleStringResourceLoader(RESOURCE_BUNDLE_NAME));
    if (isDevelopmentSystem() == false) {
      getResourceSettings().setThrowExceptionOnMissingResource(false); // Don't throw MissingResourceException for
      // missing i18n keys in production mode.
    }
    getApplicationSettings().setPageExpiredErrorPage(PageExpiredPage.class); // Don't show expired page.
    // getSessionSettings().setMaxPageMaps(20); // Map up to 20 pages per session (default is 5).
    getComponentInstantiationListeners().add(new SpringComponentInjector(this));
    getApplicationSettings().setInternalErrorPage(ErrorPage.class);
    // getRequestCycleSettings().setGatherExtendedBrowserInfo(true); // For getting browser width and height.

    // Select2:
    // final ApplicationSettings select2Settings = ApplicationSettings.get();
    // select2Settings.setIncludeJavascript(false);

    // if ("true".equals(System.getProperty(SYSTEM_PROPERTY_HSQLDB_18_UPDATE)) == true) {
    // try {
    // log.info("Send SHUTDOWN COMPACT to upgrade data-base version:");
    // final DataSource dataSource = (DataSource)beanFactory.getBean("dataSource");
    // dataSource.getConnection().createStatement().execute("SHUTDOWN COMPACT");
    // log.fatal("************ PLEASE RESTART APPLICATION NOW FOR PROPER INSTALLATION !!!!!!!!!!!!!! ************");
    // return;
    // } catch (final SQLException ex) {
    // log.fatal("Data-base SHUTDOWN COMPACT failed: " + ex.getMessage());
    // }
    // }

    // Javascript Resource settings
    getJavaScriptLibrarySettings().setJQueryReference(new PackageResourceReference(WicketApplication.class, "scripts/jquery.js"));

    final ServletContext servletContext = getServletContext();
    final String configContextPath = projectForgeApp.getConfigXml().getServletContextPath();
    String contextPath;
    if (StringUtils.isBlank(configContextPath) == true) {
      contextPath = servletContext.getContextPath();
      projectForgeApp.getConfigXml().setServletContextPath(contextPath);
    } else {
      contextPath = configContextPath;
    }
    log.info("Using servlet context path: " + contextPath);
    if (this.wicketApplicationFilter != null) {
      this.wicketApplicationFilter.setApplication(this);
    } else {
      throw new RuntimeException("this.wicketApplicationFilter is null");
    }
    WicketUtils.setContextPath(contextPath);
    UserFilter.initialize(Registry.instance().getDao(UserDao.class), contextPath);

    for (final Map.Entry<String, Class< ? extends WebPage>> mountPage : WebRegistry.instance().getMountPages().entrySet()) {
      final String path = mountPage.getKey();
      final Class< ? extends WebPage> pageClass = mountPage.getValue();
      mountPageWithPageParameterAwareness(path, pageClass);
      mountedPages.put(pageClass, path);
    }
    if (isDevelopmentSystem() == true) {
      if (isStripWicketTags() == true) {
        log.info("Strip Wicket tags also in development mode at default (see context.xml).");
        Application.get().getMarkupSettings().setStripWicketTags(true);
      }
      getDebugSettings().setOutputMarkupContainerClassName(true);
    }
    try {
      PFUserContext.setUser(MyDatabaseUpdateDao.__internalGetSystemAdminPseudoUser()); // Logon admin user.
      if (projectForgeApp.getMyDatabaseUpdater().getSystemUpdater().isUpdated() == false) {
        // Force redirection to update page:
        UserFilter.setUpdateRequiredFirst(true);
      }
    } finally {
      PFUserContext.setUser(null);
    }
    LoginHandler loginHandler;
    if (StringUtils.isNotBlank(projectForgeApp.getConfigXml().getLoginHandlerClass()) == true) {
      loginHandler = (LoginHandler) BeanHelper.newInstance(projectForgeApp.getConfigXml().getLoginHandlerClass());
    } else {
      loginHandler = new LoginDefaultHandler();
    }

    // initialize styles compiler
    try {
      final LessWicketApplicationInstantiator lessInstantiator = new LessWicketApplicationInstantiator(this, "styles", "projectforge.less",
          "projectforge.css");
      lessInstantiator.instantiate();
    } catch (final Exception e) {
      log.error("Unable to instantiate wicket less compiler", e);
    }

    if (loginHandler == null) {
      log.error("Can't load login handler '" + projectForgeApp.getConfigXml().getLoginHandlerClass() + "'. No login will be possible!");
    } else {
      loginHandler.initialize();
      Login.getInstance().setLoginHandler(loginHandler);
      if (UserFilter.isUpdateRequiredFirst() == false) {
        projectForgeApp.finalizeInitialization();
      }
    }
    getPageSettings().setRecreateMountedPagesAfterExpiry(false);
  }

  private void mountPageWithPageParameterAwareness(final String path, final Class< ? extends WebPage> pageClass)
  {
    mount(new PageParameterAwareMountedMapper(path, pageClass));
  }

  @Override
  protected void onDestroy()
  {
    ProjectForgeApp.shutdown();
  }

  /**
   * @return True if configured as servlet context param.
   */
  public boolean isDevelopmentSystem()
  {
    if (developmentMode == null) {
      final String value = getServletContext().getInitParameter("development");
      developmentMode = "true".equals(value);
      Configuration.getInstance().internalSetDevelopmentMode(developmentMode);
    }
    return developmentMode;
  }

  @Override
  public boolean isStripWicketTags()
  {
    if (stripWicketTags == null) {
      if (isDevelopmentSystem() == false) {
        stripWicketTags = true;
      } else {
        final String value = getServletContext().getInitParameter("stripWicketTags");
        stripWicketTags = "true".equals(value);
      }
    }
    return stripWicketTags;
  }

  /**
   * @see org.apache.wicket.Application#getHomePage()
   */
  @Override
  public Class< ? extends WebPage> getHomePage()
  {
    return WicketUtils.getDefaultPage();
  }

  @Override
  public Session newSession(final Request request, final Response response)
  {
    final MySession mySession = new MySession(request);
    return mySession;
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
  @Override
  protected IConverterLocator newConverterLocator()
  {
    final ConverterLocator converterLocator = new ConverterLocator();
    converterLocator.set(java.util.Date.class, new MyDateConverter());
    converterLocator.set(java.sql.Date.class, new MyDateConverter(java.sql.Date.class, "S-"));
    return converterLocator;
  }

  public static long getStartTime()
  {
    if (ProjectForgeApp.getInstance() == null) {
      // Should only occur in test cases.
      return 0;
    }
    return ProjectForgeApp.getInstance().getStartTime();
  }
}
