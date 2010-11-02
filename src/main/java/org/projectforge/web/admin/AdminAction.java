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

package org.projectforge.web.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.projectforge.common.DateHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ReindexSettings;
import org.projectforge.core.SystemDao;
import org.projectforge.database.DatabaseDao;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.XmlDump;
import org.projectforge.meb.MebMailClient;
import org.projectforge.user.UserXmlPreferencesMigrationDao;
import org.projectforge.web.MessageAction;
import org.projectforge.web.core.BaseActionBean;
import org.projectforge.web.core.BaseActionBeanContext;
import org.projectforge.web.core.ResponseUtils;
import org.projectforge.web.stripes.DateTypeConverter;
import org.projectforge.web.wicket.WebConstants;

/**
 */
@UrlBinding("/secure/admin/Admin.action")
public class AdminAction extends BaseActionBean
{
  public static final String I18N_PROPERTIES_BASENAME = "I18nResources";

  private static final Logger log = Logger.getLogger(AdminAction.class);

  private static final String JSP_URL = "/WEB-INF/jsp/admin/admin.jsp";

  private XmlDump xmlDump;

  private SystemDao systemDao;

  private DatabaseDao databaseDao;

  private DatabaseUpdateDao databaseUpdateDao;

  private MebMailClient mebMailClient;

  private UserXmlPreferencesMigrationDao userXmlPreferencesMigrationDao;

  private String alertMessage;

  private String logEntries;

  private String formattedLogEntries;

  private Date reindexFromDate;

  private Integer reindexNewestNEntries = 1000;

  public void setXmlDump(XmlDump xmlDump)
  {
    this.xmlDump = xmlDump;
  }

  public void setSystemDao(SystemDao systemDao)
  {
    this.systemDao = systemDao;
  }

  public void setDatabaseDao(DatabaseDao databaseDao)
  {
    this.databaseDao = databaseDao;
  }

  public void setDatabaseUpdateDao(DatabaseUpdateDao databaseUpdateDao)
  {
    this.databaseUpdateDao = databaseUpdateDao;
  }

  public void setMebMailClient(MebMailClient mebMailClient)
  {
    this.mebMailClient = mebMailClient;
  }

  public void setUserXmlPreferencesMigrationDao(UserXmlPreferencesMigrationDao userXmlPreferencesMigrationDao)
  {
    this.userXmlPreferencesMigrationDao = userXmlPreferencesMigrationDao;
  }

  /**
   * For manipulating the system wide alert message.
   * @see BaseActionBeanContext#getAlertMessage()
   */
  public String getAlertMessage()
  {
    if (alertMessage != null) {
      return alertMessage;
    } else {
      return getContext().getAlertMessage();
    }
  }

  public void setAlertMessage(String alertMessage)
  {
    this.alertMessage = alertMessage;
  }

  public String getLogEntries()
  {
    return logEntries;
  }

  public void setLogEntries(String logEntries)
  {
    this.logEntries = logEntries;
  }

  public String getFormattedLogEntries()
  {
    return formattedLogEntries;
  }

  @Validate(converter = DateTypeConverter.class)
  public Date getReindexFromDate()
  {
    return reindexFromDate;
  }

  public void setReindexFromDate(Date reindexFromDate)
  {
    this.reindexFromDate = reindexFromDate;
  }

  public Integer getReindexNewestNEntries()
  {
    return reindexNewestNEntries;
  }

  public void setReindexNewestNEntries(Integer reindexNewestNEntries)
  {
    this.reindexNewestNEntries = reindexNewestNEntries;
  }

  @DontValidate
  public Resolution updateUserPrefs()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: updateUserPrefs");
    final String output = userXmlPreferencesMigrationDao.migrateAllUserPrefs();
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        String ts = DateHelper.getTimestampAsFilenameSuffix(new Date());
        String filename = "projectforge_updateUserPrefs_" + ts + ".txt";
        ResponseUtils.prepareDownload(filename, response, getContext().getServletContext(), true);
        response.setCharacterEncoding("utf-8");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "utf-8"));
        pw.print(output);
        pw.flush();
      }
    };
  }

  /**
   * Reserved for patching some data base entries over admin web.
   * @return
   */
  @DontValidate
  public Resolution formatLogEntries()
  {
    log.info("Administration: formatLogEntries");
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    int indent = 0;
    final StringBuffer buf = new StringBuffer();
    for (int i = 0; i < logEntries.length(); i++) {
      final char c = logEntries.charAt(i);
      buf.append(c);
      if (c == ',') {
        buf.append("<br/>");
        for (int j = 0; j < indent; j++) {
          buf.append("&nbsp;&nbsp;");
        }
      } else if (c == '[') {
        indent++;
        buf.append("<br/>");
        for (int j = 0; j < indent; j++) {
          buf.append("&nbsp;&nbsp;");
        }
      } else if (c == ']') {
        indent--;
        buf.append("<br/>");
      }
    }
    formattedLogEntries = buf.toString();
    return getInputPage();
  }

  @DontValidate
  public Resolution dump()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: Database dump.");

    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        String ts = DateHelper.getTimestampAsFilenameSuffix(new Date());
        String filename = "projectforgedump_" + ts + ".xml.gz";
        ResponseUtils.prepareDownload(filename, response, getContext().getServletContext(), true);
        xmlDump.dumpDatabase(filename, response.getOutputStream());
        response.getOutputStream().flush();
      }
    };
  }

  @DontValidate
  public Resolution reindex()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    final ReindexSettings settings = new ReindexSettings(this.reindexFromDate, this.reindexNewestNEntries);
    String tables = databaseDao.rebuildDatabaseSearchIndices(settings);
    return MessageAction.getForwardResolution(this, "administration.databaseSearchIndicesRebuild", tables);
  }

  @DontValidate
  public Resolution createMissingDatabaseIndices()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    final int counter = databaseUpdateDao.createMissingIndices();
    return MessageAction.getForwardResolution(this, "administration.missingDatabaseIndicesCreated", String.valueOf(counter));
  }

  @DontValidate
  public Resolution schemaExport()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    final String result = systemDao.exportSchema();
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        String filename = "projectforge_schema" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".sql";
        ResponseUtils.prepareDownload(filename, response, getContext().getServletContext(), true);
        response.getOutputStream().print(result);
        response.getOutputStream().flush();
      }
    };
  }

  /**
   * @return
   * @see SystemDao#checkSystemIntegrity()
   */
  @DontValidate
  public Resolution checkSystemIntegrity()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: check integrity of tasks.");
    final String result = systemDao.checkSystemIntegrity();
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        String filename = "projectforge_check_report" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".txt";
        ResponseUtils.prepareDownload(filename, response, getContext().getServletContext(), true);
        response.getOutputStream().print(result);
        response.getOutputStream().flush();
      }
    };
  }

  /**
   * @return
   * @see SystemDao#refreshCaches()
   */
  @DontValidate
  public Resolution refreshCaches()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: refresh of caches.");
    String refreshedCaches = systemDao.refreshCaches();
    getContext().flushAllToDB();
    refreshedCaches += ", UserXmlPreferencesCache";
    return MessageAction.getForwardResolution(this, "administration.refreshCachesDone", refreshedCaches);
  }

  /**
   * Reread the config.xml.
   * @see Configuration#readConfiguration()
   */
  @DontValidate
  public Resolution rereadConfiguration()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: reread configuration file config.xml.");
    String result = Configuration.getInstance().readConfiguration();
    if (result != null) {
      result = result.replaceAll("\n", "<br/>\n");
    }
    return MessageAction.getForwardResolution(this, "administration.rereadConfiguration", result);
  }

  /**
   * Export the config.xml.
   * @see Configuration#readConfiguration()
   */
  @DontValidate
  public Resolution exportConfiguration()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: export configuration file config.xml.");
    final String xml = Configuration.getInstance().exportConfiguration();
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        String filename = "config-" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".xml";
        ResponseUtils.prepareDownload(filename, response, getContext().getServletContext(), true);
        response.setCharacterEncoding("utf-8");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "utf-8"));
        pw.print(xml);
        pw.flush();
      }
    };
  }

  /**
   * @see Configuration#isMebMailAccountConfigured()
   */
  public boolean isMebMailAccountConfigured()
  {
    return Configuration.getInstance().isMebMailAccountConfigured();
  }

  /**
   * Check for new MEB mails (Mobile Enterprise Blogging).
   * @see MebMailClient#getNewMessages(boolean)
   * @return Message
   */
  @DontValidate
  public Resolution checkUnseenMebMails()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: check for new MEB mails.");
    final int counter = mebMailClient.getNewMessages(true, true);
    return MessageAction.getForwardResolution(this, "message.successfullCompleted", "check for new MEB mails, "
        + counter
        + " new messages imported.");
  }

  /**
   * Check all MEB mails (Mobile Enterprise Blogging) and imports unkown messages.
   * @see MebMailClient#getNewMessages(boolean)
   * @return Message
   */
  @DontValidate
  public Resolution importAllMebMails()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: import all MEB mails.");
    final int counter = mebMailClient.getNewMessages(false, false);
    return MessageAction.getForwardResolution(this, "message.successfullCompleted", "import all MEB mails, "
        + counter
        + " new messages imported.");
  }

  /**
   * Checks the i18n properties and shows the differences in the localization files.
   * @return
   */
  @DontValidate
  public Resolution checkI18nProperties()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Administration: check i18n properties.");
    StringBuffer buf = new StringBuffer();
    Properties props = new Properties();
    Properties props_en = new Properties();
    Properties props_de = new Properties();
    Properties propsFound = new Properties();
    ClassLoader cLoader = this.getClass().getClassLoader();
    try {
      InputStream is = cLoader.getResourceAsStream(I18N_PROPERTIES_BASENAME + ".properties");
      props.load(is);
      is = cLoader.getResourceAsStream(I18N_PROPERTIES_BASENAME + "_en.properties");
      props_en.load(is);
      is = cLoader.getResourceAsStream(I18N_PROPERTIES_BASENAME + "_de.properties");
      props_de.load(is);
      is = cLoader.getResourceAsStream(WebConstants.FILE_I18N_KEYS);
      propsFound.load(is);
    } catch (IOException ex) {
      log.error("Could not load i18n properties: " + ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
    buf.append("Checking the differences between the " + I18N_PROPERTIES_BASENAME + " properties (default and _de)\n\n");
    buf.append("Found " + props.size() + " entries in default property file (en).\n\n");
    buf.append("Missing in _de:\n");
    buf.append("---------------\n");
    List<String> keys = new ArrayList<String>();
    for (Object key : props.keySet()) {
      if (props_de.containsKey(key) == false) {
        keys.add(String.valueOf(key));
      }
    }
    Collections.sort(keys);
    for (String key : keys) {
      buf.append(key + "=" + props.getProperty(key) + "\n");
    }
    buf.append("\n\nOnly in _de (not in _en):\n");
    buf.append("-------------------------\n");
    keys = new ArrayList<String>();
    for (Object key : props_de.keySet()) {
      if (props.containsKey(key) == false) {
        keys.add(String.valueOf(key));
      }
    }
    Collections.sort(keys);
    for (String key : keys) {
      buf.append(key + "=" + props_de.getProperty(key) + "\n");
    }
    buf.append("\n\nMaybe not defined but used (found in java, jsp or Wicket's html code):\n");
    buf.append("----------------------------------------------------------------------\n");
    keys = new ArrayList<String>();
    for (final Object key : propsFound.keySet()) {
      if (props.containsKey(key) == false && props_de.containsKey(key) == false && props.containsKey(key) == false) {
        keys.add(String.valueOf(key) + "=" + propsFound.getProperty((String) key));
      }
    }
    Collections.sort(keys);
    for (String key : keys) {
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
    for (String key : keys) {
      String value = props_de.getProperty(key);
      if (value == null) {
        value = props_en.getProperty(key);
      }
      if (value == null) {
        value = props.getProperty(key);
      }
      buf.append(key + "=" + value + "\n");
    }
    final String result = buf.toString();
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        String filename = "projectforge_i18n_check" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".txt";
        ResponseUtils.prepareDownload(filename, response, getContext().getServletContext(), true);
        response.getOutputStream().print(result);
        response.getOutputStream().flush();
      }
    };
  }

  public Resolution setAlertMessage()
  {
    accessChecker.checkIsUserMemberOfAdminGroup();
    accessChecker.checkDemoUser();
    log.info("Admin user has set the alert message: \"" + alertMessage + "\"");
    getContext().setAlertMessage(alertMessage);
    return getInputPage();
  }

  @DefaultHandler
  @DontValidate
  public Resolution cancel()
  {
    return getInputPage();
  }

  private Resolution getInputPage()
  {
    return new ForwardResolution(JSP_URL);
  }
}
