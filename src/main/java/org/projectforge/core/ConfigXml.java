/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.projectforge.AppVersion;
import org.projectforge.calendar.ConfigureHoliday;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.FileHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.common.TimeNotation;
import org.projectforge.fibu.kost.AccountingConfig;
import org.projectforge.jira.JiraConfig;
import org.projectforge.jira.JiraIssueType;
import org.projectforge.ldap.LdapConfig;
import org.projectforge.mail.MailAccountConfig;
import org.projectforge.mail.SendMailConfig;
import org.projectforge.orga.ContractType;
import org.projectforge.storage.StorageConfig;
import org.projectforge.user.LoginDefaultHandler;
import org.projectforge.web.MenuEntryConfig;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.WebConfig;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlField;
import org.projectforge.xml.stream.XmlHelper;
import org.projectforge.xml.stream.XmlObject;
import org.projectforge.xml.stream.XmlObjectReader;
import org.projectforge.xml.stream.XmlObjectWriter;
import org.projectforge.xml.stream.XmlOmitField;

/**
 * Configure ProjectForge via config.xml in the application's base dir.<br/>
 * The config.xml will never re-read automatically. Please call the web admin page to force a re-read.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@XmlObject(alias = "config")
public class ConfigXml
{
  // If change this, please change it also in EmbeddedJetty. If true then no log4j is initialized.
  private static final String SYSTEM_PROPERTY_STANDALONE = "ProjectForge.standalone";

  private static transient final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConfigXml.class);

  private static transient final Set<String> nonExistingResources = new HashSet<String>();

  private static transient final Set<String> existingResources = new HashSet<String>();

  private static transient ConfigXml instance;

  private static final String LOG4J_PROPERTY_FILE = "log4j.properties";

  private static final String LOG4J_PROPERTY_SOURCE_FILE = "appHomeDir-log4j.properties";

  private transient final List<ConfigurationListener> listeners = new ArrayList<ConfigurationListener>();

  @XmlOmitField
  private String applicationHomeDir;

  @XmlOmitField
  private String applicationsResourcePath;

  private String resourceDir;

  private JiraConfig jiraConfig;

  private String jiraBrowseBaseUrl;

  private StorageConfig storageConfig;

  private String telephoneSystemUrl;

  private String telephoneSystemNumber;

  private String telephoneSystemOperatorPanelUrl;

  private String smsUrl;

  private String receiveSmsKey;

  private String phoneLookupKey;

  private MailAccountConfig mebMailAccount = new MailAccountConfig();

  private String currencySymbol;

  @XmlField(asElement = true)
  private Locale defaultLocale;

  @XmlField(asElement = true)
  private TimeNotation defaultTimeNotation;

  @XmlField(asElement = true)
  private int firstDayOfWeek = Calendar.MONDAY;

  private String excelDefaultPaperSize;

  private List<ConfigureHoliday> holidays;

  private List<ContractType> contractTypes;

  private transient short excelDefaultPaperSizeValue = -42;

  private transient File configFile;

  private String databaseDirectory;

  private String loggingDirectory;

  private String fontsDirectory;

  private String workingDirectory;

  private String tempDirectory;

  private String servletContextPath;

  private String domain;

  private String logoFile;

  private String keystoreFile;

  private String keystorePassphrase;

  private String cronExpressionHourlyJob;

  private String cronExpressionNightlyJob;

  private String cronExpressionMebPollingJob;

  private MenuEntryConfig menuConfig;

  private WebConfig webConfig;

  private boolean portletMode;

  private AccountingConfig accountingConfig;

  private LdapConfig ldapConfig;

  private String loginHandlerClass;

  /**
   * Separated list of main classes (separated by white chars and or ',').
   */
  String pluginMainClasses;

  // Please note: If you change the name of this member field don't forget to change the PLUGIN_CONFIGS_FIELD_NAME below.
  private List<ConfigurationData> plugins;

  private static final String PLUGIN_CONFIGS_FIELD_NAME = "plugins";

  private transient SSLSocketFactory usersSSLSocketFactory;

  @XmlField(alias = "sendMail")
  private SendMailConfig sendMailConfiguration;

  public static ConfigXml getInstance()
  {
    if (instance == null) {
      throw new IllegalStateException("Configuration is not yet configured");
    }
    return instance;
  }

  public static boolean isInitialized()
  {
    return instance != null;
  }

  private void reset()
  {
    resourceDir = "resources";
    jiraConfig = null;
    jiraBrowseBaseUrl = null;
    telephoneSystemUrl = null;
    telephoneSystemNumber = null;
    telephoneSystemOperatorPanelUrl = null;
    smsUrl = null;
    receiveSmsKey = null;
    phoneLookupKey = null;
    mebMailAccount = new MailAccountConfig();
    currencySymbol = "€";
    defaultLocale = Locale.ENGLISH;
    defaultTimeNotation = null;
    firstDayOfWeek = Calendar.MONDAY;
    excelDefaultPaperSize = "DINA4";
    holidays = null;
    contractTypes = null;
    databaseDirectory = "database";
    loggingDirectory = "logs";
    workingDirectory = "work";
    fontsDirectory = resourceDir + File.separator + "fonts";
    tempDirectory = "tmp";
    servletContextPath = null;
    domain = null;
    logoFile = null;
    keystoreFile = null;
    keystorePassphrase = null;
    cronExpressionHourlyJob = null;
    cronExpressionNightlyJob = null;
    cronExpressionMebPollingJob = null;
    menuConfig = null;
    webConfig = null;
    sendMailConfiguration = new SendMailConfig();
    accountingConfig = new AccountingConfig();
    accountingConfig.reset();
    ldapConfig = new LdapConfig();
  }

  protected ConfigXml()
  {
    reset();
  }

  private boolean ensureDir(final File dir)
  {
    if (dir.exists() == false) {
      log.info("Creating directory " + dir);
      dir.mkdir();
    }
    if (dir.canRead() == false) {
      log.fatal("Can't create directory: " + dir);
      return false;
    }
    return true;
  }

  /**
   * Loads the configuration file config.xml from the application's home dir if given, otherwise the default values will be assumed.
   * Constructor is used by Spring instantiation.
   */
  public ConfigXml(final String applicationHomeDir)
  {
    this.applicationHomeDir = applicationHomeDir;
    log.info("Using application home dir: " + applicationHomeDir);
    System.setProperty("base.dir", applicationHomeDir); // Needed by log4j
    final File dir = new File(this.applicationHomeDir);
    final boolean status = ensureDir(dir);
    if (status == true) {
      if ("true".equals(System.getProperty(SYSTEM_PROPERTY_STANDALONE)) == true) {
        log.info("Do not initialize log4j.properties. It's done by the standalone application of " + AppVersion.APP_ID + ".");
      } else {
        // Initialize log4j (not in standalone version):
        final File log4j = new File(this.applicationHomeDir, LOG4J_PROPERTY_FILE);
        if (log4j.canRead() == false) {
          try {
            log.info("Creating new log4j.properties in application's home dir: " + LOG4J_PROPERTY_FILE);
            final ClassLoader cLoader = getClass().getClassLoader();
            final InputStream is = cLoader.getResourceAsStream(LOG4J_PROPERTY_SOURCE_FILE);
            FileUtils.copyInputStreamToFile(is, log4j);
          } catch (final IOException ex) {
            log.error("Exception encountered while copiing " + LOG4J_PROPERTY_FILE + ": " + ex, ex);
          }
        }
        if (log4j.canRead() == true) {
          log.info("Read log4j configuration: " + log4j.getAbsolutePath());
          PropertyConfigurator.configure(log4j.getAbsolutePath());
        }
      }
      readConfiguration();
      this.databaseDirectory = FileHelper.getAbsolutePath(applicationHomeDir, this.databaseDirectory);
      ensureDir(new File(databaseDirectory));
      this.loggingDirectory = FileHelper.getAbsolutePath(applicationHomeDir, this.loggingDirectory);
      ensureDir(new File(loggingDirectory));
      this.workingDirectory = FileHelper.getAbsolutePath(applicationHomeDir, this.workingDirectory);
      ensureDir(new File(workingDirectory));
      this.resourceDir = FileHelper.getAbsolutePath(applicationHomeDir, this.resourceDir);
      ensureDir(new File(resourceDir));
      this.fontsDirectory = FileHelper.getAbsolutePath(applicationHomeDir, this.fontsDirectory);
      ensureDir(new File(fontsDirectory));
      this.tempDirectory = FileHelper.getAbsolutePath(applicationHomeDir, this.tempDirectory);
      ensureDir(new File(tempDirectory));
    }
    setupKeyStores();
    if (menuConfig != null) {
      menuConfig.setParents();
    }
    instance = this;
  }

  public void register(final ConfigurationListener listener)
  {
    listeners.add(listener);
  }

  /**
   * Reads the configuration file (can be called after any modification of the config file).
   */
  public String readConfiguration()
  {
    reset();
    configFile = new File(applicationHomeDir, "config.xml");
    String msg = "";
    if (configFile.canRead() == false) {
      msg = "Cannot read from config file: '" + getConfigFilePath() + "'. OK, assuming default values.";
      log.info(msg);
    } else {
      final XmlObjectReader reader = getReader();
      String xml = null;
      try {
        xml = FileUtils.readFileToString(configFile, "UTF-8");
      } catch (final IOException ex) {
        msg = "Cannot read config file '" + getConfigFilePath() + "' properly: " + ex;
        log.fatal(msg, ex);
      }
      if (xml != null) {
        try {
          final ConfigXml cfg = (ConfigXml) reader.read(xml);
          final String warnings = reader.getWarnings();
          copyDeclaredFields(null, this.getClass(), cfg, this);
          if (CollectionUtils.isNotEmpty(cfg.plugins) == true) {
            for (final ConfigurationData srcData : cfg.plugins) {
              final ConfigurationData destData = this.getPluginConfig(srcData.getClass());
              copyDeclaredFields(destData.getClass().getName() + ".", srcData.getClass(), srcData, destData);
            }
          }
          msg = "Config file '" + getConfigFilePath() + "' successfully read.";
          if (warnings != null) {
            msg += "\n" + warnings;
          }
          log.info(msg);
        } catch (final Throwable ex) {
          msg = "Cannot read config file '" + getConfigFilePath() + "' properly: " + ex;
          log.fatal(msg, ex);
        }
      }
    }
    for (final ConfigurationListener listener : listeners) {
      listener.afterRead();
    }
    return msg;
  }

  public String exportConfiguration()
  {
    final XmlObjectWriter writer = new XmlObjectWriter() {
      @Override
      protected boolean ignoreField(final Object obj, final Field field)
      {
        if (field.getDeclaringClass().isAssignableFrom(ConfigXml.class) == true
            && StringHelper.isIn(field.getName(), "expireTime", "timeOfLastRefresh") == true) {
          return true;
        }
        return super.ignoreField(obj, field);
      }
    };
    final String xml = writer.writeToXml(this, true);
    return XmlHelper.XML_HEADER + xml;
  }

  private static XmlObjectReader getReader()
  {
    final XmlObjectReader reader = new XmlObjectReader();
    final AliasMap aliasMap = new AliasMap();
    reader.setAliasMap(aliasMap);
    reader.initialize(ConfigXml.class);
    reader.initialize(ConfigureHoliday.class);
    reader.initialize(ContractType.class);
    reader.initialize(JiraIssueType.class);
    AccountingConfig.registerXmlObjects(reader, aliasMap);
    return reader;
  }

  private void setupKeyStores()
  {
    if (getKeystoreFile() != null) {
      try {
        File keystoreFile = new File(getKeystoreFile());
        if (keystoreFile.canRead() == false) {
          keystoreFile = new File(applicationHomeDir, getKeystoreFile());
        }
        if (keystoreFile.canRead() == false) {
          log.error("Can't read keystore file: " + getKeystoreFile());
          return;
        }
        final InputStream is = new FileInputStream(keystoreFile);
        usersSSLSocketFactory = createSSLSocketFactory(is, this.keystorePassphrase);
        log.info("Keystore successfully read from file: " + keystoreFile.getAbsolutePath());
      } catch (final Throwable ex) {
        log.error("Could not initialize your key store (see error message below)!");
        log.error(ex.getMessage(), ex);
      }
    }
  }

  private SSLSocketFactory createSSLSocketFactory(final InputStream is, final String passphrase) throws Exception
  {
    final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(is, passphrase.toCharArray());
    is.close();
    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);
    final X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
    final SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, new TrustManager[] { defaultTrustManager}, null);
    return context.getSocketFactory();
  }

  /**
   * For test cases.
   * @param config
   */
  static void internalSetInstance(final String config)
  {
    final XmlObjectReader reader = getReader();
    final ConfigXml cfg = (ConfigXml) reader.read(config);
    instance = new ConfigXml();
    copyDeclaredFields(null, instance.getClass(), cfg, instance);
  }

  /**
   * Copies only not null values of the configuration.
   */
  private static void copyDeclaredFields(final String prefix, final Class< ? > srcClazz, final Object src, final Object dest,
      final String... ignoreFields)
  {
    final Field[] fields = srcClazz.getDeclaredFields();
    AccessibleObject.setAccessible(fields, true);
    for (final Field field : fields) {
      if (ignoreFields != null && ArrayUtils.contains(ignoreFields, field.getName()) == false && accept(field)) {
        try {
          final Object srcFieldValue = field.get(src);
          if (srcFieldValue == null) {
            // Do nothing
          } else if (srcFieldValue instanceof ConfigurationData) {
            final Object destFieldValue = field.get(dest);
            Validate.notNull(destFieldValue);
            final StringBuffer buf = new StringBuffer();
            if (prefix != null) {
              buf.append(prefix);
            }
            String alias = null;
            if (field.isAnnotationPresent(XmlField.class)) {
              final XmlField xmlFieldAnn = field.getAnnotation(XmlField.class);
              if (xmlFieldAnn != null) {
                alias = xmlFieldAnn.alias();
              }
            }
            if (alias != null) {
              buf.append(alias);
            } else {
              buf.append(field.getClass().getName());
            }
            buf.append(".");
            copyDeclaredFields(buf.toString(), srcFieldValue.getClass(), srcFieldValue, destFieldValue, ignoreFields);
          } else if (PLUGIN_CONFIGS_FIELD_NAME.equals(field.getName()) == true) {
            // Do nothing.
          } else {
            field.set(dest, srcFieldValue);
            if (StringHelper.isIn(field.getName(), "receiveSmsKey", "phoneLookupKey") == true) {
              log.info(StringUtils.defaultString(prefix) + field.getName() + " = ****");
            } else {
              log.info(StringUtils.defaultString(prefix) + field.getName() + " = " + srcFieldValue);
            }
          }
        } catch (final IllegalAccessException ex) {
          throw new InternalError("Unexpected IllegalAccessException: " + ex.getMessage());
        }
      }
    }
    final Class< ? > superClazz = srcClazz.getSuperclass();
    if (superClazz != null) {
      copyDeclaredFields(prefix, superClazz, src, dest, ignoreFields);
    }
  }

  /**
   * Tries to get the given filename from the application's resource dir (file system). If not exist, the input stream will be taken as
   * resource input stream.
   * @param filename Filename (can include relative path settings): "test.xsl", "fo-styles/doit.xsl".
   * @return Object[2]: First value is the InputStream and second value is the url in external form.
   */
  public Object[] getInputStream(final String filename)
  {
    InputStream is = null;
    String path = null;
    final File base = new File(getResourcePath());
    if (base.isDirectory() == true) {
      final File file = new File(base, filename);
      if (file.exists() == false) {
        showNonExistingMessage(file, false);
      } else {
        try {
          is = new FileInputStream(file);
          path = file.toURI().toString();
        } catch (final FileNotFoundException ex) {
          log.error(file.getAbsoluteFile() + ": " + ex.getMessage(), ex); // Should not occur.
          is = null;
        }
        showExistingMessage(file, false);
      }
    }
    if (is == null) {
      final ClassLoader cLoader = getClass().getClassLoader();
      final URL url = cLoader.getResource(filename);
      if (url != null) {
        path = url.toExternalForm();
      }
      is = cLoader.getResourceAsStream(filename);
    }
    if (is == null) {
      log.error("File '" + filename + "' not found (wether in file system under '" + base.getAbsolutePath() + "' nor in resource!)");
    }
    final Object[] result = new Object[2];
    result[0] = is;
    result[1] = path;
    return result;
  }

  /**
   * Tries to get the given filename from the application's resource dir (file system). If not exist, the content will be taken as resource
   * input stream. Calls getInputStream(filename) and converts input stream to String.
   * @param filename Filename (can include relative path settings): "test.xsl", "fo-styles/doit.xsl".
   * @return Object[2]: First value is the content as string and second value is the url in external form.
   * @see #getInputStream(String)
   */
  public Object[] getContent(final String filename)
  {
    final Object[] result = getInputStream(filename);
    final InputStream is = (InputStream) result[0];
    if (is != null) {
      try {
        result[0] = IOUtils.toString(is, "UTF-8");
      } catch (final IOException ex) {
        log.error(ex.getMessage(), ex);
      }
    }
    return result;
  }

  private static void showNonExistingMessage(final File file, final boolean directory)
  {
    // Synchronized not needed, for concurrent calls, output entries exist twice in the worst case.
    if (nonExistingResources.contains(file.getAbsolutePath()) == false) {
      nonExistingResources.add(file.getAbsolutePath());
      existingResources.remove(file.getAbsolutePath()); // If changed by administrator during application running.
      final String type = directory == true ? "directory" : "file";
      log.info("Using default " + type + " of ProjectForge, because " + type + "'" + file.getAbsolutePath() + "' does not exist (OK)");
    }
  }

  private static void showExistingMessage(final File file, final boolean directory)
  {
    // Synchronized not needed, for concurrent calls, output entries exist twice in the worst case.
    if (existingResources.contains(file.getAbsolutePath()) == false) {
      existingResources.add(file.getAbsolutePath());
      nonExistingResources.remove(file.getAbsolutePath()); // If changed by administrator during application running.
      final String type = directory == true ? "directory" : "file";
      log.info("Using existing " + type + ":" + file.getAbsolutePath());
    }
  }

  /**
   * Returns whether or not to append the given <code>Field</code>.
   * <ul>
   * <li>Ignore transient fields
   * <li>Ignore static fields
   * <li>Ignore inner class fields</li>
   * </ul>
   * 
   * @param field The Field to test.
   * @return Whether or not to consider the given <code>Field</code>.
   */
  protected static boolean accept(final Field field)
  {
    if (field.getName().indexOf(ClassUtils.INNER_CLASS_SEPARATOR_CHAR) != -1) {
      // Reject field from inner class.
      return false;
    }
    if (Modifier.isTransient(field.getModifiers()) == true) {
      // transients.
      return false;
    }
    if (Modifier.isStatic(field.getModifiers()) == true) {
      // transients.
      return false;
    }
    return true;
  }

  /**
   * Base url for linking JIRA issues: https://jira.acme.com/jira/browse/PROJECTFORGE-222. The issue name UPPERCASE_LETTERS-### will be
   * appended to this url. ProjectForge parses the user's text input for [A-Z][A-Z0-9*]-[0-9]* and displays a list of detected JIRA-issues
   * with a link beside the text area containing such issues.<br/>
   * Example: https://jira.acme.com/jira/browse/ (don't forget closing '/'). <br/>
   * If null then no text input will be parsed and no JIRA link will be displayed.
   */
  public String getJiraBrowseBaseUrl()
  {
    return jiraBrowseBaseUrl;
  }

  /**
   * FOR INTERNAL USE ONLY (tests). Please configure this value via config.xml.
   * @param jiraBrowseBaseUrl
   */
  public void setJiraBrowseBaseUrl(final String jiraBrowseBaseUrl)
  {
    this.jiraBrowseBaseUrl = jiraBrowseBaseUrl;
  }

  public JiraConfig getJiraConfig()
  {
    return jiraConfig;
  }

  /**
   * @return true if a JIRA browse base url is given.
   */
  public final boolean isJIRAConfigured()
  {
    return StringUtils.isNotBlank(getJiraBrowseBaseUrl());
  }

  /**
   * @return the storageConfig
   */
  public StorageConfig getStorageConfig()
  {
    return storageConfig;
  }

  public boolean isStorageConfigured()
  {
    return storageConfig != null && StringUtils.isNotBlank(storageConfig.getAuthenticationToken());
  }

  /**
   * Format http://asterisk.acme.com/originatecall.php?source=#source&amp;target=#target<br/>
   * #source will be replaced by the current user's phone and #target by the chosen phone number to call.
   */
  public String getTelephoneSystemUrl()
  {
    return telephoneSystemUrl;
  }

  /**
   * For direct calls all numbers beginning with the this number will be stripped, e. g. for 0561316793: 056131679323 -> 23. So internal
   * calls are supported.
   */
  public String getTelephoneSystemNumber()
  {
    return telephoneSystemNumber;
  }

  public boolean isTelephoneSystemUrlConfigured()
  {
    return StringUtils.isNotEmpty(this.telephoneSystemUrl);
  }

  public String getTelephoneSystemOperatorPanelUrl()
  {
    return telephoneSystemOperatorPanelUrl;
  }

  /**
   * Format "http://asterisk.acme.com/sms.php?number=#number&amp;text=#text".<br/>
   * #number will be replaced by the chosen mobile phone number and #text by the sms text (url encoded).
   */
  public String getSmsUrl()
  {
    return smsUrl;
  }

  public boolean isSmsConfigured()
  {
    return StringUtils.isNotEmpty(smsUrl);
  }

  /**
   * The SMS receiver verifies this key given as get parameter to the servlet call. <br/>
   * The key should be an alpha numeric random value with at least 6 characters for security reasons.
   */
  public String getReceiveSmsKey()
  {
    return receiveSmsKey;
  }

  /**
   * The reverse phone lookup service verifies the key given as parameter to the servlet call against this key. The key should be an alpha
   * numeric random value with at least 6 characters for security reasons.
   * @return the receivePhoneLookupKey
   */
  public String getPhoneLookupKey()
  {
    return phoneLookupKey;
  }

  /**
   * The mail account for receiving mobile blogging entries by mail.
   * @return
   */
  public MailAccountConfig getMebMailAccount()
  {
    return mebMailAccount;
  }

  /**
   * @return true if meb mail account with hostname is configured, otherwise false.
   */
  public boolean isMebMailAccountConfigured()
  {
    return this.mebMailAccount != null && this.mebMailAccount.getHostname() != null;
  }

  /**
   * The currency symbol of ProjectForge. ProjectForge supports currently one currency for the whole application. <br/>
   * Please note: The deprecated stripes action only works with "€".
   * @return the application wide currency symbol, e. g. "€".
   */
  public String getCurrencySymbol()
  {
    return currencySymbol;
  }

  /**
   * The default locale is currently used for getting the week of year in Calendar.
   */
  public Locale getDefaultLocale()
  {
    return defaultLocale;
  }

  /**
   * The default time notation (12-hour or 24-hour). This notation is used, if the user has not chosen his personal time notation. Default
   * is 24-hour for locales starting with "de" (German), otherwise 12-hour.
   */
  public TimeNotation getDefaultTimeNotation()
  {
    return defaultTimeNotation;
  }

  /**
   * The default first day of week (1 - Sunday, 2 - Monday, ...)
   * @return the firstDayOfWeek
   */
  public int getFirstDayOfWeek()
  {
    return firstDayOfWeek;
  }

  /**
   * Supported values "LETTER", default is "DINA4".
   * @return PrintSetup short value. Default is
   * @see PrintSetup#A4_PAPERSIZE.
   */
  public short getDefaultPaperSize()
  {
    if (excelDefaultPaperSizeValue != -42) {
      return excelDefaultPaperSizeValue;
    }
    if ("LETTER".equals(excelDefaultPaperSize) == true) {
      excelDefaultPaperSizeValue = PrintSetup.LETTER_PAPERSIZE;
    } else {
      excelDefaultPaperSizeValue = PrintSetup.A4_PAPERSIZE;
    }
    return excelDefaultPaperSizeValue;
  }

  /** ProjectForges home dir (for resources, images, configuration etc.). */
  public String getApplicationHomeDir()
  {
    return applicationHomeDir;
  }

  /**
   * Resource directory relative to application's home (default 'resources').
   */
  public String getResourceDir()
  {
    return resourceDir;
  }

  /**
   * Absolute path of resource directory (default '&lt;app-home&gt;/resources').
   */
  public String getResourcePath()
  {
    if (this.applicationsResourcePath == null) {
      final File file;
      if (new File(resourceDir).isAbsolute() == true) {
        file = new File(resourceDir);
      } else {
        file = new File(applicationHomeDir, resourceDir);
      }
      this.applicationsResourcePath = file.getAbsolutePath();
    }
    return applicationsResourcePath;
  }

  /**
   * @return the databaseDirectory
   */
  public String getDatabaseDirectory()
  {
    return databaseDirectory;
  }

  /**
   * @param databaseDirectory the databaseDirectory to set absolute or relative to the application's home dir.
   * @return this for chaining.
   */
  public void setDatabaseDirectory(final String databaseDirectory)
  {
    this.databaseDirectory = databaseDirectory;
  }

  /**
   * @return the loggingDirectory
   */
  public String getLoggingDirectory()
  {
    return loggingDirectory;
  }

  /**
   * @param loggingDirectory the loggingDirectory to set absolute or relative to the application's home dir.
   * @return this for chaining.
   */
  public void setLoggingDirectory(final String loggingDirectory)
  {
    this.loggingDirectory = loggingDirectory;
  }

  /**
   * This directory is used for e. g. storing uploaded files. The absolute path will be returned. <br/>
   * Default value: "work"
   * @see #setWorkingDirectory(String)
   */
  public String getWorkingDirectory()
  {
    return workingDirectory;
  }

  /**
   * Sets the working dir as relative sub directory of the application's home dir or the absolute path if given.
   * @param workingDirectory
   */
  public void setWorkingDirectory(final String workingDirectory)
  {
    this.workingDirectory = workingDirectory;
  }

  /**
   * Default value: "resources/fonts" (absolute path).
   * @return the fontsDirectory
   */
  public String getFontsDirectory()
  {
    return fontsDirectory;
  }

  /**
   * @param fontsDirectory the fontsDirectory to set
   * @return this for chaining.
   */
  public void setFontsDirectory(final String fontsDirectory)
  {
    this.fontsDirectory = fontsDirectory;
  }

  /**
   * This directory is used e. g. by the ImageCropper. The absolute path will be returned. <br/>
   * Default value: "tmp"
   * @see #setWorkingDirectory(String)
   */
  public String getTempDirectory()
  {
    return tempDirectory;
  }

  /**
   * Sets the temporary dir as relative sub directory of the application's home dir or the absolute path if given. This directory is used by
   * ProjectForge to save temporary files such as images from the ImageCropper.
   * @param tempDirectory
   */
  public void setTempDirectory(final String tempDirectory)
  {
    this.tempDirectory = tempDirectory;
  }

  public String getConfigFilePath()
  {
    return configFile.getPath();
  }

  /**
   * @return true if at least a send mail host is given, otherwise false.
   */
  public boolean isSendMailConfigured()
  {
    return sendMailConfiguration != null && StringUtils.isNotBlank(sendMailConfiguration.getHost()) == true;
  }

  public SendMailConfig getSendMailConfiguration()
  {
    return sendMailConfiguration;
  }

  /**
   * The servlet's context path, "/ProjectForge" at default. You should configure another context path such as "/" if the ProjectForge app
   * runs in another context, such as root context.
   */
  public String getServletContextPath()
  {
    return servletContextPath;
  }

  public void setServletContextPath(final String servletContextPath)
  {
    this.servletContextPath = servletContextPath;
  }

  /**
   * Only given, if the administrator have configured this domain. Otherwise e. g. the ImageCropper uses
   * req.getHttpServletRequest().getScheme() + "://" + req.getHttpServletRequest().getLocalName() + ":" +
   * req.getHttpServletRequest().getLocalPort()
   * @return domain (host) in form https://www.acme.de:8443/
   */
  public String getDomain()
  {
    return domain;
  }

  public void setDomain(final String domain)
  {
    this.domain = domain;
  }

  /**
   * If configured then this logo file is used for displaying at the top of the navigation menu.
   * @return The path of the configured logo (relative to the image dir of the application's resource path, at default:
   *         '&lt;app-home&gt;/resources/images').
   * @see #getResourcePath()
   */
  public String getLogoFile()
  {
    return logoFile;
  }

  public List<ConfigureHoliday> getHolidays()
  {
    return holidays;
  }

  public List<ContractType> getContractTypes()
  {
    return contractTypes;
  }

  public SSLSocketFactory getUsersSSLSocketFactory()
  {
    return usersSSLSocketFactory;
  }

  /**
   * Here you can define a list of main classes of type AbstractPlugin. These classes will be initialized on startup. Multiple entries
   * should be separated by white chars and/or ','.
   * @return
   */
  public String[] getPluginMainClasses()
  {
    return StringUtils.split(pluginMainClasses, " \r\n\t,");
  }

  /**
   * If no such plugin config exist, a new instance is created and returned.
   * @return the pluginConfigs
   */
  public ConfigurationData getPluginConfig(final Class< ? extends ConfigurationData> configClass)
  {
    if (plugins == null) {
      plugins = new ArrayList<ConfigurationData>();
    } else {
      for (final ConfigurationData configData : plugins) {
        if (configData != null && configClass.isAssignableFrom(configData.getClass()) == true) {
          return configData;
        }
      }
    }
    final ConfigurationData config = (ConfigurationData) BeanHelper.newInstance(configClass);
    plugins.add(config);
    return config;
  }

  /**
   * For additional certificates you can set the file name of the jssecert file in your ProjectForge home (config) directory (path of your
   * confix.xml). <br/>
   * If given then the key-store file is used.
   */
  public String getKeystoreFile()
  {
    return keystoreFile;
  }

  /**
   * For overwriting the default settings.<br/>
   * Format for hourly *:00 is (see Quartz documentation for further information) "0 0 * * * ?"
   */
  public String getCronExpressionHourlyJob()
  {
    return cronExpressionHourlyJob;
  }

  /**
   * For overwriting the default settings.<br/>
   * Format for nightly at 2:30 AM (UTC) is (see Quartz documentation for further information) "0 30 2 * * ?"
   */
  public String getCronExpressionNightlyJob()
  {
    return cronExpressionNightlyJob;
  }

  /**
   * For overwriting the settings of applicationContext-web.xml.<br/>
   * Format for every 10 minutes (5, 15, 25, ...) is (see Quartz documentation for further information) "0 5/10 * * * ?"
   */
  public String getCronExpressionMebPollingJob()
  {
    return cronExpressionMebPollingJob;
  }

  /**
   * If given then this login handler will be used instead of {@link LoginDefaultHandler}. For ldap please use e. g.
   * org.projectforge.ldap.LdapLoginHandler.
   * @return the loginHandlerClass
   */
  public String getLoginHandlerClass()
  {
    return loginHandlerClass;
  }

  /**
   * Here you can add menu entries to be hidden or can build your own menu tree or just modify the existing one. If you don't configure this
   * element, you will receive the standard ProjectForge menu containing all menu entries which are available for the system and the user. <br/>
   * Please note: ProjectForge assures, that only such menu entries are visible, to which the user has the access to (independant from your
   * definitions here)! <br/>
   * If you want to make a menu entry invisible, you can add this to this root element like this:<br/>
   * 
   * <pre>
   *  &lt;menu-entry id="DEVELOPER_DOC" visible="false"/&gt;
   *  <br/>
   *  See all the predefined id's here: {@link MenuItemDef}
   *  <br/>
   * This root element will not be shown.
   */
  public MenuEntryConfig getMenuConfig()
  {
    return menuConfig;
  }

  /**
   * @return the webConfig
   * @see WebConfig
   */
  public WebConfig getWebConfig()
  {
    return webConfig;
  }

  /**
   * Experimental and undocumented setting.
   */
  public boolean isPortletMode()
  {
    return portletMode;
  }

  /**
   * @return the accountingConfig
   */
  public AccountingConfig getAccountingConfig()
  {
    return accountingConfig;
  }

  /**
   * @return the ldapConfig
   */
  public LdapConfig getLdapConfig()
  {
    return ldapConfig;
  }

  /**
   * @param ldapConfig the ldapConfig to set
   * @return this for chaining.
   */
  public void setLdapConfig(final LdapConfig ldapConfig)
  {
    this.ldapConfig = ldapConfig;
  }
}
