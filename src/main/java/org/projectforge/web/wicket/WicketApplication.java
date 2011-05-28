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
import org.apache.wicket.markup.html.WebPage;
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
import org.hibernate.cfg.AnnotationConfiguration;
import org.projectforge.AppVersion;
import org.projectforge.admin.SystemUpdater;
import org.projectforge.common.ExceptionHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.core.CronSetup;
import org.projectforge.core.ProjectForgeException;
import org.projectforge.core.SystemInfoCache;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.HibernateUtils;
import org.projectforge.plugins.core.PluginsRegistry;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserXmlPreferencesCache;
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

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see org.wicket.demo.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WicketApplication.class);

  public static final String RESOURCE_BUNDLE_NAME = "I18nResources";

  public static final Class<? extends WebPage> DEFAULT_PAGE = CalendarPage.class;

  private static Boolean developmentMode;

  private static Boolean stripWicketTags;

  private static String alertMessage;

  private static Map<Class< ? extends Page>, String> mountedPages = new HashMap<Class< ? extends Page>, String>();

  @SpringBean(name = "wicketApplicationFilter")
  private WicketApplicationFilter wicketApplicationFilter;

  private UserXmlPreferencesCache userXmlPreferencesCache;

  @SpringBean(name = "configurationDao")
  private ConfigurationDao configurationDao;

  private ConfigXml configXml;

  private Configuration configuration;

  @SpringBean(name = "cronSetup")
  private CronSetup cronSetup;

  @SpringBean(name = "daoRegistry")
  private DaoRegistry daoRegistry;

  @SpringBean(name = "systemUpdater")
  private SystemUpdater systemUpdater;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SpringBean(name = "systemInfoCache")
  private SystemInfoCache systemInfoCache;

  /**
   * At application start the flag developmentMode is perhaps not already set. If possible please use {@link #isDevelopmentSystem()} instead.<br/>
   * Please use {@link WebConfiguration#isDevelopmentMode()}.
   */
  public static Boolean internalIsDevelopmentMode()
  {
    return developmentMode;
  }

  public static String getBookmarkableMountPath(final Class< ? extends Page> pageClass)
  {
    return mountedPages.get(pageClass);
  }

  public void setWicketApplicationFilter(final WicketApplicationFilter wicketApplicationFilter)
  {
    this.wicketApplicationFilter = wicketApplicationFilter;
  }

  public void setUserXmlPreferencesCache(final UserXmlPreferencesCache userXmlPreferencesCache)
  {
    this.userXmlPreferencesCache = userXmlPreferencesCache;
  }

  public void setConfigXml(final ConfigXml configXml)
  {
    this.configXml = configXml;
  }

  public void setConfiguration(final Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setConfigurationDao(final ConfigurationDao configurationDao)
  {
    this.configurationDao = configurationDao;
  }

  public void setCronSetup(final CronSetup cronSetup)
  {
    this.cronSetup = cronSetup;
  }

  public void setDaoRegistry(final DaoRegistry daoRegistry)
  {
    this.daoRegistry = daoRegistry;
  }

  public void setSystemUpdater(final SystemUpdater systemUpdater)
  {
    this.systemUpdater = systemUpdater;
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setSystemInfoCache(final SystemInfoCache systemInfoCache)
  {
    this.systemInfoCache = systemInfoCache;
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
   * @param msg
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
    final MyAuthorizationStrategy authStrategy = new MyAuthorizationStrategy();
    getSecuritySettings().setAuthorizationStrategy(authStrategy);
    // getSecuritySettings().setUnauthorizedComponentInstantiationListener(authStrategy);
    getResourceSettings().setResourceStreamLocator(new MyResourceStreamLocator());
    // Prepend the resource bundle for overwriting some Wicket default localizations (such as StringValidator.*)
    getResourceSettings().addStringResourceLoader(0, new BundleStringResourceLoader(RESOURCE_BUNDLE_NAME));
    getResourceSettings().setThrowExceptionOnMissingResource(false); // Don't throw MissingResourceException for missing i18n keys.
    getApplicationSettings().setPageExpiredErrorPage(PageExpiredPage.class); // Don't show expired page.
    getSessionSettings().setMaxPageMaps(20); // Map up to 20 pages per session (default is 5).
    addComponentInstantiationListener(new SpringComponentInjector(this));
    getApplicationSettings().setInternalErrorPage(ErrorPage.class);

    final XmlWebApplicationContext webApplicationContext = (XmlWebApplicationContext) WebApplicationContextUtils
    .getWebApplicationContext(getServletContext());
    final ConfigurableListableBeanFactory beanFactory = webApplicationContext.getBeanFactory();
    beanFactory.autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    final LocalSessionFactoryBean localSessionFactoryBean = (LocalSessionFactoryBean) beanFactory.getBean("&sessionFactory");
    final AnnotationConfiguration hibernateConfiguration = (AnnotationConfiguration) localSessionFactoryBean.getConfiguration();
    HibernateUtils.setConfiguration(hibernateConfiguration);
    final ServletContext servletContext = getServletContext();
    final String configContextPath = configXml.getServletContextPath();
    String contextPath;
    if (StringUtils.isBlank(configContextPath) == true) {
      contextPath = servletContext.getContextPath();
      configXml.setServletContextPath(contextPath);
    } else {
      contextPath = configContextPath;
    }
    log.info("Using servlet context path: " + contextPath);
    if (configuration.getBeanFactory() == null) {
      configuration.setBeanFactory(beanFactory);
    }
    configuration.setConfigurationDao(configurationDao);
    SystemInfoCache.internalInitialize(systemInfoCache);
    WicketUtils.setContextPath(contextPath);
    UserFilter.initialize(userDao, contextPath);
    if (this.wicketApplicationFilter != null) {
      this.wicketApplicationFilter.setApplication(this);
    } else {
      throw new RuntimeException("this.wicketApplicationFilter is null");
    }
    daoRegistry.init();

    final PluginsRegistry pluginsRegistry = PluginsRegistry.instance();
    pluginsRegistry.set(systemUpdater);
    pluginsRegistry.set(beanFactory, getResourceSettings());
    pluginsRegistry.initialize();

    for (final Map.Entry<String, Class< ? extends WebPage>> mountPage : WebRegistry.instance().getMountPages().entrySet()) {
      mountPage(mountPage.getKey(), mountPage.getValue());
    }
    if (isDevelopmentSystem() == true && isStripWicketTags() == true) {
      log.info("Strip Wicket tags also in development mode at default (see context.xml).");
      Application.get().getMarkupSettings().setStripWicketTags(true);
    }
    getResourceSettings().setLocalizer(new MyLocalizer("edit/StandardI18n"));
    log.info("Default TimeZone is: " + TimeZone.getDefault());
    log.info("user.timezone is: " + System.getProperty("user.timezone"));
    cronSetup.initialize();
    log.info(AppVersion.APP_ID + " " + AppVersion.NUMBER + " (" + AppVersion.RELEASE_TIMESTAMP + ") initialized.");

    PFUserContext.setUser(DatabaseUpdateDao.__internalGetSystemAdminPseudoUser()); // Logon admin user.
    if (systemUpdater.isUpdated() == false) {
      // Force redirection to update page:
      UserFilter.setUpdateRequiredFirst(true);
    }
    PFUserContext.setUser(null);
  }

  @Override
  protected void onDestroy()
  {
    log.info("Syncing all user preferences to database.");
    userXmlPreferencesCache.forceReload();
    cronSetup.shutdown();
    log.info("Destroyed");
  }

  public boolean isDevelopmentSystem()
  {
    if (developmentMode == null) {
      final String value = getServletContext().getInitParameter("development");
      developmentMode = "true".equals(value);
    }
    return developmentMode;
  }

  public boolean isStripWicketTags()
  {
    if (stripWicketTags == null) {
      final String value = getServletContext().getInitParameter("stripWicketTargets");
      stripWicketTags = "true".equals(value);
    }
    return stripWicketTags;
  }

  /**
   * @see org.apache.wicket.Application#getHomePage()
   */
  @Override
  public Class<? extends WebPage> getHomePage()
  {
    return DEFAULT_PAGE;
  }

  @Override
  public Session newSession(final Request request, final Response response)
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
  @Override
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
      protected Page onRuntimeException(final Page page, final RuntimeException e)
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
      protected void logRuntimeException(final RuntimeException e)
      {
        final Throwable rootCause = ExceptionHelper.getRootCause(e);
        if (rootCause instanceof ProjectForgeException == false) {
          super.logRuntimeException(e);
        }
      }
    };
  }
}
