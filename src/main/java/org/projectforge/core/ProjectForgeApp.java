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

package org.projectforge.core;

import java.util.TimeZone;

import net.fortuna.ical4j.util.CompatibilityHints;

import org.projectforge.AppVersion;
import org.projectforge.common.Logger;
import org.projectforge.common.LoggerBridgeLog4j;
import org.projectforge.continuousdb.DatabaseSupport;
import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.database.DatabaseCoreInitial;
import org.projectforge.database.HibernateUtils;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.database.MyDatabaseUpdateDao;
import org.projectforge.database.MyDatabaseUpdater;
import org.projectforge.export.MyXlsExportContext;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.core.PluginsRegistry;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.storage.StorageClient;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserXmlPreferencesCache;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Doing some initialization stuff and stuff on shutdown (planned). Most stuff is yet done by WicketApplication.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ProjectForgeApp
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjectForgeApp.class);

  private static ProjectForgeApp instance;

  private boolean upAndRunning;

  private final long startTime = System.currentTimeMillis();

  private Configuration configuration;

  private ConfigurationDao configurationDao;

  private ConfigXml configXml;

  private CronSetup cronSetup;

  private DaoRegistry daoRegistry;

  private InitDatabaseDao initDatabaseDao;

  private MyDatabaseUpdater myDatabaseUpdater;

  private UserXmlPreferencesCache userXmlPreferencesCache;

  private SystemInfoCache systemInfoCache;

  private PluginsRegistry pluginsRegistry;

  public synchronized static ProjectForgeApp init(final ConfigurableListableBeanFactory beanFactory,
      final org.hibernate.cfg.Configuration hibernateConfiguration)
  {
    if (instance != null) {
      log.warn("ProjectForge is already initialized!");
      return instance;
    }
    instance = new ProjectForgeApp();
    if (beanFactory != null) {
      instance.internalInit(beanFactory, hibernateConfiguration);
    }
    return instance;
  }

  public static ProjectForgeApp getInstance()
  {
    return instance;
  }

  public static void shutdown()
  {
    if (instance == null) {
      log.error("ProjectForge isn't initialized, can't excecute shutdown!");
      return;
    }
    instance.internalShutdown();
  }

  /**
   * Should be called on start-up (e. g. by WicketApplication) if all start-up stuff is done and all the services and login should be
   * started. <br>
   * Flag upAndRunning will be set to true.
   */
  public void finalizeInitialization()
  {
    cronSetup.initialize();
    log.info("system cronJobs are initialized.");
    pluginsRegistry.registerCronJobs(cronSetup);
    log.info("plugin cronJobs are initialized.");
    log.info(AppVersion.APP_ID + " " + AppVersion.NUMBER + " (" + AppVersion.RELEASE_TIMESTAMP + ") initialized.");
    try {
      StorageClient.getInstance(); // Initialize storage
    } catch (final Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    // initialize ical4j to be more "relaxed"
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
    this.upAndRunning = true;
    log.info("ProjectForge is now available (up and running).");
  }

  private void internalInit(final ConfigurableListableBeanFactory beanFactory, final org.hibernate.cfg.Configuration hibernateConfiguration)
  {
    log.info("Initializing...");
    beanFactory.autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    // Log4j for ProjectForge modules: common, excel and continuous-db.
    Logger.setLoggerBridge(new LoggerBridgeLog4j());

    // Time zone
    log.info("Default TimeZone is: " + TimeZone.getDefault());
    if ("UTC".equals(TimeZone.getDefault().getID()) == false) {
      for (final String str : UTC_RECOMMENDED) {
        log.fatal(str);
      }
      for (final String str : UTC_RECOMMENDED) {
        System.err.println(str);
      }
    }
    log.info("user.timezone is: " + System.getProperty("user.timezone"));

    // Initialize Excel extensions:
    new MyXlsExportContext();

    HibernateUtils.setConfiguration(hibernateConfiguration);

    if (DatabaseSupport.getInstance() == null) {
      DatabaseSupport.setInstance(new DatabaseSupport(HibernateUtils.getDialect()));
    }

    if (configuration.getBeanFactory() == null) {
      configuration.setBeanFactory(beanFactory);
    }

    final boolean missingDatabaseSchema = initDatabaseDao.isEmpty();
    if (missingDatabaseSchema == true) {
      try {
        PFUserContext.setUser(MyDatabaseUpdateDao.__internalGetSystemAdminPseudoUser());
        final UpdateEntry updateEntry = DatabaseCoreInitial.getInitializationUpdateEntry(myDatabaseUpdater);
        updateEntry.runUpdate();
      } finally {
        PFUserContext.setUser(null);
      }
    }

    daoRegistry.init();

    pluginsRegistry = PluginsRegistry.instance();
    pluginsRegistry.set(beanFactory);
    pluginsRegistry.set(myDatabaseUpdater.getSystemUpdater());
    pluginsRegistry.initialize();
    if (missingDatabaseSchema == true) {
      try {
        PFUserContext.setUser(MyDatabaseUpdateDao.__internalGetSystemAdminPseudoUser()); // Logon admin user.
        for (final AbstractPlugin plugin : pluginsRegistry.getPlugins()) {
          final UpdateEntry updateEntry = plugin.getInitializationUpdateEntry();
          if (updateEntry != null) {
            updateEntry.runUpdate();
          }
        }
      } finally {
        PFUserContext.setUser(null);
      }
    }
    UserXmlPreferencesCache.setInternalInstance(userXmlPreferencesCache);

    configuration.setConfigurationDao(configurationDao);
    SystemInfoCache.internalInitialize(systemInfoCache);

  }

  private void internalShutdown()
  {
    log.info("Shutdown...");
    upAndRunning = false;
    log.info("Syncing all user preferences to database.");
    userXmlPreferencesCache.forceReload();
    cronSetup.shutdown();
    try {
      PFUserContext.setUser(MyDatabaseUpdateDao.__internalGetSystemAdminPseudoUser());
      myDatabaseUpdater.getDatabaseUpdateDao().shutdownDatabase();
    } finally {
      PFUserContext.setUser(null);
    }
    log.info("Shutdown completed.");
  }

  public void setConfiguration(final Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setUserXmlPreferencesCache(final UserXmlPreferencesCache userXmlPreferencesCache)
  {
    this.userXmlPreferencesCache = userXmlPreferencesCache;
  }

  /**
   * @return the configXml
   */
  public ConfigXml getConfigXml()
  {
    return configXml;
  }

  public void setConfigXml(final ConfigXml configXml)
  {
    this.configXml = configXml;
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

  public void setInitDatabaseDao(final InitDatabaseDao initDatabaseDao)
  {
    this.initDatabaseDao = initDatabaseDao;
  }

  /**
   * @return the myDatabaseUpdater
   */
  public MyDatabaseUpdater getMyDatabaseUpdater()
  {
    return myDatabaseUpdater;
  }

  /**
   * @param myDatabaseUpdater the myDatabaseUpdater to set
   */
  public void setMyDatabaseUpdater(final MyDatabaseUpdater myDatabaseUpdater)
  {
    this.myDatabaseUpdater = myDatabaseUpdater;
  }

  public void setSystemInfoCache(final SystemInfoCache systemInfoCache)
  {
    this.systemInfoCache = systemInfoCache;
  }

  /**
   * @return the startTime
   */
  public long getStartTime()
  {
    return startTime;
  }

  /**
   * @return the upAndRunning
   */
  public boolean isUpAndRunning()
  {
    return upAndRunning;
  }

  /**
   * This method should only be called in test cases!
   * @param upAndRunning the upAndRunning to set
   */
  public void internalSetUpAndRunning(final boolean upAndRunning)
  {
    log.warn("This method should only be called in test cases!");
    this.upAndRunning = upAndRunning;
  }

  private static final String[] UTC_RECOMMENDED = { //
    "**********************************************************", //
    "***                                                    ***", //
    "*** It's highly recommended to start ProjectForge      ***", //
    "*** with TimeZone UTC. This default TimeZone has to be ***", //
    "*** set before any initialization of Hibernate!!!!     ***", //
    "*** You can do this e. g. in JAVA_OPTS etc.            ***", //
    "***                                                    ***", //
  "**********************************************************"};
}
