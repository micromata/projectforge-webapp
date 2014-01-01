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

package org.projectforge.web.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.book.BookStatus;
import org.projectforge.common.DateHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.core.HibernateSearchReindexer;
import org.projectforge.core.ReindexSettings;
import org.projectforge.core.SystemDao;
import org.projectforge.database.MyDatabaseUpdater;
import org.projectforge.database.XmlDump;
import org.projectforge.meb.MebMailClient;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.core.PluginsRegistry;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.user.UserXmlPreferencesMigrationDao;
import org.projectforge.web.MenuBuilder;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractStandardFormPage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.MessagePage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketApplication;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

public class AdminPage extends AbstractStandardFormPage implements ISelectCallerPage
{
  private static final long serialVersionUID = 8345068133036236305L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AdminPage.class);

  static final int NUMBER_OF_TEST_OBJECTS_TO_CREATE = 100;

  @SpringBean(name = "bookDao")
  private BookDao bookDao;

  @SpringBean(name = "xmlDump")
  private XmlDump xmlDump;

  @SpringBean(name = "systemDao")
  private SystemDao systemDao;

  @SpringBean(name = "myDatabaseUpdater")
  private MyDatabaseUpdater myDatabaseUpdater;

  @SpringBean(name = "hibernateSearchReindexer")
  private HibernateSearchReindexer hibernateSearchReindexer;

  @SpringBean(name = "mebMailClient")
  private MebMailClient mebMailClient;

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "userXmlPreferencesCache")
  private UserXmlPreferencesCache userXmlPreferencesCache;

  @SpringBean(name = "userXmlPreferencesMigrationDao")
  private UserXmlPreferencesMigrationDao userXmlPreferencesMigrationDao;

  private final AdminForm form;

  public AdminPage(final PageParameters parameters)
  {
    super(parameters);
    form = new AdminForm(this);
    body.add(form);
    form.init();

    addDatabaseActionsMenu();
    addCachesMenu();
    addConfigurationMenu();
    addMEBMenu();
    addMiscMenu();
    addDevelopmentMenu();
  }

  @SuppressWarnings("serial")
  protected void addConfigurationMenu()
  {
    // Configuration
    final ContentMenuEntryPanel configurationMenu = new ContentMenuEntryPanel(getNewContentMenuChildId(),
        getString("system.admin.group.title.systemChecksAndFunctionality.configuration"));
    addContentMenuEntry(configurationMenu);
    // Check re-read configuration
    final Link<Void> rereadConfigurationLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        rereadConfiguration();
      }
    };
    final ContentMenuEntryPanel rereadConfigurationLinkMenuItem = new ContentMenuEntryPanel(configurationMenu.newSubMenuChildId(),
        rereadConfigurationLink, getString("system.admin.button.rereadConfiguration"))
    .setTooltip(getString("system.admin.button.rereadConfiguration.tooltip"));
    configurationMenu.addSubMenuEntry(rereadConfigurationLinkMenuItem);

    // Export configuration.
    final Link<Void> exportConfigurationLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        exportConfiguration();
      }
    };
    final ContentMenuEntryPanel exportConfigurationLinkMenuItem = new ContentMenuEntryPanel(configurationMenu.newSubMenuChildId(),
        exportConfigurationLink, getString("system.admin.button.exportConfiguration"))
    .setTooltip(getString("system.admin.button.exportConfiguration.tooltip"));
    configurationMenu.addSubMenuEntry(exportConfigurationLinkMenuItem);
  }

  @SuppressWarnings("serial")
  protected void addCachesMenu()
  {
    // Caches
    final ContentMenuEntryPanel cachesMenu = new ContentMenuEntryPanel(getNewContentMenuChildId(),
        getString("system.admin.group.title.systemChecksAndFunctionality.caches"));
    addContentMenuEntry(cachesMenu);
    // Refresh caches.
    final Link<Void> refreshCachesLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        refreshCaches();
      }
    };
    final ContentMenuEntryPanel refreshCachesLinkMenuItem = new ContentMenuEntryPanel(cachesMenu.newSubMenuChildId(), refreshCachesLink,
        getString("system.admin.button.refreshCaches")).setTooltip(getString("system.admin.button.refreshCaches.tooltip"));
    cachesMenu.addSubMenuEntry(refreshCachesLinkMenuItem);
  }

  @SuppressWarnings("serial")
  protected void addDatabaseActionsMenu()
  {
    // Data-base actions
    final ContentMenuEntryPanel databaseActionsMenu = new ContentMenuEntryPanel(getNewContentMenuChildId(),
        getString("system.admin.group.title.databaseActions"));
    addContentMenuEntry(databaseActionsMenu);
    // Update all user preferences
    final Link<Void> updateUserPrefsLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        updateUserPrefs();
      }
    };
    final ContentMenuEntryPanel updateUserPrefsLinkMenuItem = new ContentMenuEntryPanel(databaseActionsMenu.newSubMenuChildId(),
        updateUserPrefsLink, getString("system.admin.button.updateUserPrefs"))
    .setTooltip(getString("system.admin.button.updateUserPrefs.tooltip"));
    databaseActionsMenu.addSubMenuEntry(updateUserPrefsLinkMenuItem);

    // Create missing data-base indices.
    final Link<Void> createMissingDatabaseIndicesLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        createMissingDatabaseIndices();
      }
    };
    final ContentMenuEntryPanel createMissingDatabaseIndicesLinkMenuItem = new ContentMenuEntryPanel(
        databaseActionsMenu.newSubMenuChildId(), createMissingDatabaseIndicesLink,
        getString("system.admin.button.createMissingDatabaseIndices"))
    .setTooltip(getString("system.admin.button.createMissingDatabaseIndices.tooltip"));
    databaseActionsMenu.addSubMenuEntry(createMissingDatabaseIndicesLinkMenuItem);
    // Fix data-base history entries.
    final Link<Void> fixDBHistoryEntriesLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        fixDBHistoryEntries();
      }
    };
    final ContentMenuEntryPanel fixDBHistoryEntriesLinkMenuItem = new ContentMenuEntryPanel(databaseActionsMenu.newSubMenuChildId(),
        fixDBHistoryEntriesLink, getString("system.admin.button.fixDBHistoryEntries"))
    .setTooltip(getString("system.admin.button.fixDBHistoryEntries.tooltip"));
    databaseActionsMenu.addSubMenuEntry(fixDBHistoryEntriesLinkMenuItem);
    {
      // Dump data-base.
      final Link<Void> dumpDatabaseLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
        @Override
        public void onClick()
        {
          dump();
        }
      };
      final ContentMenuEntryPanel dumpDatabaseLinkMenuItem = new ContentMenuEntryPanel(databaseActionsMenu.newSubMenuChildId(),
          dumpDatabaseLink, getString("system.admin.button.dump")).setTooltip(getString("system.admin.button.dump.tooltip"));
      databaseActionsMenu.addSubMenuEntry(dumpDatabaseLinkMenuItem);
      dumpDatabaseLink.add(WicketUtils.javaScriptConfirmDialogOnClick(getString("system.admin.button.dump.question")));
    }
    {
      // Schema export.
      final Link<Void> schemaExportLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
        @Override
        public void onClick()
        {
          schemaExport();
        }
      };
      final ContentMenuEntryPanel schemaExportLinkMenuItem = new ContentMenuEntryPanel(databaseActionsMenu.newSubMenuChildId(),
          schemaExportLink, getString("system.admin.button.schemaExport"))
      .setTooltip(getString("system.admin.button.schemaExport.tooltip"));
      databaseActionsMenu.addSubMenuEntry(schemaExportLinkMenuItem);
    }
  }

  @SuppressWarnings("serial")
  protected void addMiscMenu()
  {
    // Misc checks
    final ContentMenuEntryPanel miscChecksMenu = new ContentMenuEntryPanel(getNewContentMenuChildId(),
        getString("system.admin.group.title.systemChecksAndFunctionality.miscChecks"));
    addContentMenuEntry(miscChecksMenu);
    // Check system integrity
    final Link<Void> checkSystemIntegrityLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        checkSystemIntegrity();
      }
    };
    final ContentMenuEntryPanel checkSystemIntegrityLinkMenuItem = new ContentMenuEntryPanel(miscChecksMenu.newSubMenuChildId(),
        checkSystemIntegrityLink, getString("system.admin.button.checkSystemIntegrity"))
    .setTooltip(getString("system.admin.button.checkSystemIntegrity.tooltip"));
    miscChecksMenu.addSubMenuEntry(checkSystemIntegrityLinkMenuItem);
  }

  @SuppressWarnings("serial")
  protected void addMEBMenu()
  {
    if (Configuration.getInstance().isMebConfigured() == false) {
      // Do nothing.
      return;
    }
    // Mobile enterprise blogging
    final ContentMenuEntryPanel mebMenu = new ContentMenuEntryPanel(getNewContentMenuChildId(), getString("meb.title.heading"));
    addContentMenuEntry(mebMenu);
    // Check unseen meb mails
    final Link<Void> checkUnseenMebMailsLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        checkUnseenMebMails();
      }
    };
    final ContentMenuEntryPanel checkUnseenMebMailsLinkMenuItem = new ContentMenuEntryPanel(mebMenu.newSubMenuChildId(),
        checkUnseenMebMailsLink, getString("system.admin.button.checkUnseenMebMails"))
    .setTooltip(getString("system.admin.button.checkUnseenMebMails.tooltip"));
    mebMenu.addSubMenuEntry(checkUnseenMebMailsLinkMenuItem);

    // Import all meb mails.
    final Link<Void> importAllMebMailsLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        importAllMebMails();
      }
    };
    final ContentMenuEntryPanel importAllMebMailsLinkMenuItem = new ContentMenuEntryPanel(mebMenu.newSubMenuChildId(),
        importAllMebMailsLink, getString("system.admin.button.importAllMebMails"))
    .setTooltip(getString("system.admin.button.importAllMebMails.tooltip"));
    mebMenu.addSubMenuEntry(importAllMebMailsLinkMenuItem);
  }

  @SuppressWarnings("serial")
  protected void addDevelopmentMenu()
  {
    if (WebConfiguration.isDevelopmentMode() == false) {
      // Do nothing.
      return;
    }
    // Development actions
    final ContentMenuEntryPanel developmentMenu = new ContentMenuEntryPanel(getNewContentMenuChildId(), "Development");
    addContentMenuEntry(developmentMenu);
    // Check I18n properties.
    final Link<Void> checkI18nPropertiesLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        checkI18nProperties();
      }
    };
    final ContentMenuEntryPanel checkI18nPropertiesLinkMenuItem = new ContentMenuEntryPanel(developmentMenu.newSubMenuChildId(),
        checkI18nPropertiesLink, getString("system.admin.button.checkI18nProperties"))
    .setTooltip(getString("system.admin.button.checkI18nProperties.tooltip"));
    developmentMenu.addSubMenuEntry(checkI18nPropertiesLinkMenuItem);
    // Create test objects
    final Link<Void> createTestObjectsLink = new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
      @Override
      public void onClick()
      {
        createTestBooks();
      }
    };
    createTestObjectsLink.add(WicketUtils.javaScriptConfirmDialogOnClick(getLocalizedMessage(
        "system.admin.development.testObjectsCreationQuestion", AdminPage.NUMBER_OF_TEST_OBJECTS_TO_CREATE, "BookDO")));
    final ContentMenuEntryPanel createTestObjectsLinkMenuItem = new ContentMenuEntryPanel(developmentMenu.newSubMenuChildId(),
        createTestObjectsLink, "BookDO").setTooltip("Creates 100 books of type BookDO for testing.");
    developmentMenu.addSubMenuEntry(createTestObjectsLinkMenuItem);
  }

  @Override
  protected String getTitle()
  {
    return getString("system.admin.title");
  }

  protected void checkUnseenMebMails()
  {
    log.info("Administration: check for new MEB mails.");
    checkAccess();
    final int counter = mebMailClient.getNewMessages(true, true);
    setResponsePage(new MessagePage("message.successfullCompleted", "check for new MEB mails, " + counter + " new messages imported."));
  }

  protected void importAllMebMails()
  {
    log.info("Administration: import all MEB mails.");
    checkAccess();
    final int counter = mebMailClient.getNewMessages(false, false);
    setResponsePage(new MessagePage("message.successfullCompleted", "import all MEB mails, " + counter + " new messages imported."));
  }

  protected void checkSystemIntegrity()
  {
    log.info("Administration: check integrity of tasks.");
    checkAccess();
    final String result = systemDao.checkSystemIntegrity();
    final String filename = "projectforge_check_report" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".txt";
    DownloadUtils.setDownloadTarget(result.getBytes(), filename);
  }

  protected void refreshCaches()
  {
    log.info("Administration: refresh of caches.");
    checkAccess();
    String refreshedCaches = systemDao.refreshCaches();
    userXmlPreferencesCache.forceReload();
    refreshedCaches += ", UserXmlPreferencesCache";
    menuBuilder.refreshAllMenus();
    refreshedCaches += ", MenuCache";
    setResponsePage(new MessagePage("administration.refreshCachesDone", refreshedCaches));
  }

  protected void rereadConfiguration()
  {
    log.info("Administration: reread configuration file config.xml.");
    checkAccess();
    String result = ConfigXml.getInstance().readConfiguration();
    if (result != null) {
      result = result.replaceAll("\n", "<br/>\n");
    }
    setResponsePage(new MessagePage("administration.rereadConfiguration", result));
  }

  protected void exportConfiguration()
  {
    log.info("Administration: export configuration file config.xml.");
    checkAccess();
    final String xml = ConfigXml.getInstance().exportConfiguration();
    final String filename = "config-" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".xml";
    DownloadUtils.setUTF8CharacterEncoding(getResponse());
    DownloadUtils.setDownloadTarget(xml.getBytes(), filename);
  }

  protected void checkI18nProperties()
  {
    log.info("Administration: check i18n properties.");
    checkAccess();
    final StringBuffer buf = new StringBuffer();
    final Properties props = new Properties();
    final Properties props_en = new Properties();
    final Properties props_de = new Properties();
    final Properties propsFound = new Properties();
    final ClassLoader cLoader = this.getClass().getClassLoader();
    try {
      load(props, "");
      load(props_en, "_en");
      load(props_de, "_de");
      final InputStream is = cLoader.getResourceAsStream(WebConstants.FILE_I18N_KEYS);
      propsFound.load(is);
    } catch (final IOException ex) {
      log.error("Could not load i18n properties: " + ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
    buf.append("Checking the differences between the " + PFUserContext.BUNDLE_NAME + " properties (default and _de)\n\n");
    buf.append("Found " + props.size() + " entries in default property file (en).\n\n");
    buf.append("Missing in _de:\n");
    buf.append("---------------\n");
    List<String> keys = new ArrayList<String>();
    for (final Object key : props.keySet()) {
      if (props_de.containsKey(key) == false) {
        keys.add(String.valueOf(key));
      }
    }
    Collections.sort(keys);
    for (final String key : keys) {
      buf.append(key + "=" + props.getProperty(key) + "\n");
    }
    buf.append("\n\nOnly in _de (not in _en):\n");
    buf.append("-------------------------\n");
    keys = new ArrayList<String>();
    for (final Object key : props_de.keySet()) {
      if (props.containsKey(key) == false) {
        keys.add(String.valueOf(key));
      }
    }
    Collections.sort(keys);
    for (final String key : keys) {
      buf.append(key + "=" + props_de.getProperty(key) + "\n");
    }
    if (WebConfiguration.isDevelopmentMode() == true) {
      buf.append("\n\nMaybe not defined but used (found in java, jsp or Wicket's html code):\n");
      buf.append("----------------------------------------------------------------------\n");
      keys = new ArrayList<String>();
      for (final Object key : propsFound.keySet()) {
        if (props.containsKey(key) == false && props_de.containsKey(key) == false && props.containsKey(key) == false) {
          keys.add(String.valueOf(key) + "=" + propsFound.getProperty((String) key));
        }
      }
      Collections.sort(keys);
      for (final String key : keys) {
        buf.append(key + "\n");
      }
      buf.append("\n\nExperimental (in progress): Maybe unused (not found in java, jsp or Wicket's html code):\n");
      buf.append("----------------------------------------------------------------------------------------\n");
      final Set<String> all = new TreeSet<String>();
      CollectionUtils.addAll(all, props.keys());
      CollectionUtils.addAll(all, props_en.keys());
      CollectionUtils.addAll(all, props_de.keys());
      keys = new ArrayList<String>();
      for (final String key : all) {
        if (propsFound.containsKey(key) == false) {
          keys.add(String.valueOf(key));
        }
      }
      Collections.sort(keys);
      for (final String key : keys) {
        String value = props_de.getProperty(key);
        if (value == null) {
          value = props_en.getProperty(key);
        }
        if (value == null) {
          value = props.getProperty(key);
        }
        buf.append(key + "=" + value + "\n");
      }
    }
    final String result = buf.toString();
    final String filename = "projectforge_i18n_check" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".txt";
    DownloadUtils.setDownloadTarget(result.getBytes(), filename);
  }

  protected void dump()
  {
    log.info("Administration: Database dump.");
    checkAccess();
    // Fix the data-base history entries first:
    myDatabaseUpdater.getDatabaseUpdateDao().fixDBHistoryEntries();
    final String ts = DateHelper.getTimestampAsFilenameSuffix(new Date());
    final String filename = "projectforgedump_" + ts + ".xml.gz";
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    xmlDump.dumpDatabase(filename, out);
    DownloadUtils.setDownloadTarget(out.toByteArray(), filename);
  }

  protected void reindex()
  {
    log.info("Administration: re-index.");
    checkAccess();
    final ReindexSettings settings = new ReindexSettings(form.reindexFromDate, form.reindexNewestNEntries);
    final String tables = hibernateSearchReindexer.rebuildDatabaseSearchIndices(settings);
    setResponsePage(new MessagePage("administration.databaseSearchIndicesRebuild", tables));
  }

  protected void schemaExport()
  {
    log.info("Administration: schema export.");
    checkAccess();
    final String result = systemDao.exportSchema();
    final String filename = "projectforge_schema" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".sql";
    DownloadUtils.setDownloadTarget(result.getBytes(), filename);
  }

  @Override
  public void cancelSelection(final String property)
  {
  }

  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("reindexFromDate".equals(property) == true) {
      // Date selected.
      final Date date = (Date) selectedValue;
      form.reindexFromDate = date;
      form.reindexFromDatePanel.markModelAsChanged();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  @Override
  public void unselect(final String property)
  {
    if ("reindexFromDate".equals(property) == true) {
      form.reindexFromDate = null;
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  protected void formatLogEntries()
  {
    log.info("Administration: formatLogEntries");
    checkAccess();
    if (form.logEntries == null) {
      form.formattedLogEntries = "";
      return;
    }
    int indent = 0;
    final StringBuffer buf = new StringBuffer();
    for (int i = 0; i < form.logEntries.length(); i++) {
      final char c = form.logEntries.charAt(i);
      buf.append(c);
      if (c == ',') {
        buf.append("<br/>");
        for (int j = 0; j < indent; j++) {
          buf.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
      } else if (c == '[') {
        indent++;
        buf.append("<br/>");
        for (int j = 0; j < indent; j++) {
          buf.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
      } else if (c == ']') {
        indent--;
        buf.append("<br/>");
      }
    }
    form.formattedLogEntries = buf.toString();
  }

  protected void setAlertMessage()
  {
    log.info("Admin user has set the alert message: \"" + form.alertMessage + "\"");
    checkAccess();
    WicketApplication.setAlertMessage(form.alertMessage);
  }

  protected void clearAlertMessage()
  {
    log.info("Admin user has cleared the alert message.");
    checkAccess();
    form.alertMessage = null;
    WicketApplication.setAlertMessage(form.alertMessage);
  }

  protected void updateUserPrefs()
  {
    checkAccess();
    log.info("Administration: updateUserPrefs");
    final String output = userXmlPreferencesMigrationDao.migrateAllUserPrefs();
    final byte[] content = output.getBytes();
    final String ts = DateHelper.getTimestampAsFilenameSuffix(new Date());
    final String filename = "projectforge_updateUserPrefs_" + ts + ".txt";
    DownloadUtils.setDownloadTarget(content, filename);
  }

  protected void createMissingDatabaseIndices()
  {
    log.info("Administration: create missing data base indices.");
    accessChecker.checkRestrictedOrDemoUser();
    final int counter = myDatabaseUpdater.getDatabaseUpdateDao().createMissingIndices();
    setResponsePage(new MessagePage("administration.missingDatabaseIndicesCreated", String.valueOf(counter)));
  }

  /**
   * There is a bug for Hibernate history with Javassist: Sometimes the data base objects are serialized with the default toString() method
   * instead of using the plain id. This method fixes all wrong data base history entries.
   */
  protected void fixDBHistoryEntries()
  {
    log.info("Administration: fix data base history entries.");
    accessChecker.checkRestrictedOrDemoUser();
    final int counter = myDatabaseUpdater.getDatabaseUpdateDao().fixDBHistoryEntries();
    setResponsePage(new MessagePage("system.admin.button.fixDBHistoryEntries.result", String.valueOf(counter)));
  }

  private void checkAccess()
  {
    accessChecker.checkIsLoggedInUserMemberOfAdminGroup();
    accessChecker.checkRestrictedOrDemoUser();
  }

  public void createTestBooks()
  {
    accessChecker.checkIsLoggedInUserMemberOfAdminGroup();
    accessChecker.checkRestrictedOrDemoUser();
    final TaskDO task = taskTree.getTaskById(Configuration.getInstance().getTaskIdValue(ConfigurationParam.DEFAULT_TASK_ID_4_BOOKS));
    final List<BookDO> list = new ArrayList<BookDO>();
    int number = 1;
    while (myDatabaseUpdater.getDatabaseUpdateDao().queryForInt("select count(*) from t_book where title like 'title." + number + ".%'") > 0) {
      number++;
    }
    for (int i = 1; i <= NUMBER_OF_TEST_OBJECTS_TO_CREATE; i++) {
      list.add(new BookDO().setTitle(get("title", number, i)).setAbstractText(get("abstractText", number, i))
          .setAuthors(get("authors", number, i)).setComment(get("comment", number, i)).setEditor(get("editor", number, i))
          .setIsbn(get("isbn", number, i)).setKeywords(get("keywords", number, i)).setPublisher(get("publisher", number, i))
          .setSignature(get("signature", number, i)).setStatus(BookStatus.PRESENT).setTask(task).setYearOfPublishing("2001"));
    }
    bookDao.save(list);
    setResponsePage(new MessagePage("system.admin.development.testObjectsCreated", String.valueOf(NUMBER_OF_TEST_OBJECTS_TO_CREATE),
        "BookDO"));
  }

  private String get(final String basename, final int number, final int counter)
  {
    return basename + "." + number + "." + counter;
  }

  private void load(final Properties properties, final String locale) throws IOException
  {
    final ClassLoader cLoader = this.getClass().getClassLoader();
    InputStream is = cLoader.getResourceAsStream(PFUserContext.BUNDLE_NAME + locale + ".properties");
    properties.load(is);
    for (final AbstractPlugin plugin : PluginsRegistry.instance().getPlugins()) {
      if (plugin.getResourceBundleName() == null) {
        continue;
      }
      final String basePath = plugin.getResourceBundleName().replace('.', '/');
      is = cLoader.getResourceAsStream(basePath + locale + ".properties");
      if (is != null) {
        properties.load(is);
      }
    }
  }
}
