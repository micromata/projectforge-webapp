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

package org.projectforge.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.projectforge.calendar.ConfigureHoliday;
import org.projectforge.common.AbstractCache;
import org.projectforge.common.FileHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.jira.JiraConfig;
import org.projectforge.jira.JiraIssueType;
import org.projectforge.mail.MailAccountConfig;
import org.projectforge.mail.SendMailConfig;
import org.projectforge.orga.ContractType;
import org.projectforge.task.TaskDO;
import org.projectforge.web.MenuEntryConfig;
import org.projectforge.web.MenuItemDef;
import org.projectforge.xml.stream.XmlField;
import org.projectforge.xml.stream.XmlHelper;
import org.projectforge.xml.stream.XmlObject;
import org.projectforge.xml.stream.XmlObjectReader;
import org.projectforge.xml.stream.XmlObjectWriter;
import org.projectforge.xml.stream.XmlOmitField;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Configure ProjectForge via config.xml in the application's base dir. It's also provides the configuration of the parameters which are
 * stored via ConfigurationDao. Those parameters are cached. <br/>
 * The config.xml will never re-read automatically. Please call the web admin page to force a re-read.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@XmlObject(alias = "config")
public class Configuration extends AbstractCache
{
  public static final String[] LOCALIZATIONS = { "en", "de"};

  private static transient final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Configuration.class);

  private static transient final Set<String> nonExistingResources = new HashSet<String>();

  private static transient final Set<String> existingResources = new HashSet<String>();

  private static transient Configuration instance;

  private transient List<ConfigurationListener> listeners = new ArrayList<ConfigurationListener>();

  @XmlOmitField
  private String applicationHomeDir;

  @XmlOmitField
  private String applicationsResourcePath;

  @XmlOmitField
  private ConfigurableListableBeanFactory beanFactory;

  @XmlOmitField
  private ConfigurationDao configurationDao;

  @XmlOmitField
  private Map<ConfigurationParam, Object> configurationParamMap;

  private String resourceDir;

  private JiraConfig jiraConfig;

  private String jiraBrowseBaseUrl;

  private String telephoneSystemUrl;

  private String telephoneSystemNumber;

  private String telephoneSystemOperatorPanelUrl;

  private String smsUrl;

  private String receiveSmsKey;

  private MailAccountConfig mebMailAccount = new MailAccountConfig();

  private String currencySymbol;

  @XmlField(asElement = true)
  private Locale defaultLocale;

  private String excelDefaultPaperSize;

  private List<ConfigureHoliday> holidays;

  private List<ContractType> contractTypes;

  private transient short excelDefaultPaperSizeValue = -42;

  private transient File configFile;

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

  /**
   * Separated list of main classes (separated by white chars and or ',').
   */
  String pluginMainClasses;

  private transient SSLSocketFactory projectforgesSSLSocketFactory;

  private transient SSLSocketFactory usersSSLSocketFactory;

  @XmlField(alias = "sendMail")
  private SendMailConfig sendMailConfiguration = new SendMailConfig();

  public static Configuration getInstance()
  {
    if (instance == null) {
      throw new IllegalStateException("Configuration is not yet configured");
    }
    return instance;
  }

  static boolean isInitialized()
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
    mebMailAccount = new MailAccountConfig();
    currencySymbol = "€";
    defaultLocale = Locale.ENGLISH;
    excelDefaultPaperSize = "DINA4";
    holidays = null;
    contractTypes = null;
    workingDirectory = "work";
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
  }

  protected Configuration()
  {
    super(TICKS_PER_HOUR);
    reset();
  }

  /**
   * Loads the configuration file config.xml from the application's home dir if given, otherwise the default values will be assumed.
   * Constructor is used by Spring instantiation.
   */
  public Configuration(final String applicationHomeDir)
  {
    super(TICKS_PER_HOUR);
    this.applicationHomeDir = applicationHomeDir;
    File dir = new File(this.applicationHomeDir);
    if (dir.exists() == false) {
      log.fatal("Application's home directory does not exist: " + applicationHomeDir);
    }
    readConfiguration();
    this.workingDirectory = FileHelper.getAbsolutePath(applicationHomeDir, this.workingDirectory);
    dir = new File(workingDirectory);
    if (dir.exists() == false) {
      log.fatal("Application's working directory does not exist: " + workingDirectory);
    }
    this.tempDirectory = FileHelper.getAbsolutePath(applicationHomeDir, this.tempDirectory);
    dir = new File(tempDirectory);
    if (dir.exists() == false) {
      log.fatal("Application's temporary directory does not exist: " + tempDirectory);
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
      } catch (IOException ex) {
        msg = "Cannot read config file '" + getConfigFilePath() + "' properly: " + ex;
        log.fatal(msg, ex);
      }
      if (xml != null) {
        try {
          final Configuration cfg = (Configuration) reader.read(xml);
          final String warnings = reader.getWarnings();
          copyDeclaredFields(null, this.getClass(), cfg, this);
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
      protected boolean ignoreField(Object obj, Field field)
      {
        if (field.getDeclaringClass().isAssignableFrom(Configuration.class) == true
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
    reader.initialize(Configuration.class);
    reader.initialize(ConfigureHoliday.class);
    reader.initialize(ContractType.class);
    reader.initialize(JiraIssueType.class);
    return reader;
  }

  private void setupKeyStores()
  {
    try {
      final String filename = "keystore";
      final ClassLoader classLoader = this.getClass().getClassLoader();
      final InputStream is = classLoader.getResourceAsStream(filename);
      projectforgesSSLSocketFactory = createSSLSocketFactory(is, "changeit");
      log.info("Keystore successfully read from class path: " + filename);
    } catch (Throwable ex) {
      log
          .error("Could not initialize key store. Therefore the update pages of www.projectforge.org are not available (see error message below)!");
      log.error(ex.getMessage(), ex);
    }
    if (getKeystoreFile() != null) {
      try {
        final File keystoreFile = new File(applicationHomeDir, getKeystoreFile());
        final InputStream is = new FileInputStream(keystoreFile);
        usersSSLSocketFactory = createSSLSocketFactory(is, this.keystorePassphrase);
        log.info("Keystore successfully read from file: " + keystoreFile.getAbsolutePath());
      } catch (Throwable ex) {
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
  static void internalSetInstance(final String config, final ConfigurableListableBeanFactory beanFactory)
  {
    final XmlObjectReader reader = getReader();
    final Configuration cfg = (Configuration) reader.read(config);
    instance = new Configuration();
    copyDeclaredFields(null, instance.getClass(), cfg, instance);
    instance.setBeanFactory(beanFactory);
  }

  /**
   * Copies only not null values of the configuration.
   */
  private static void copyDeclaredFields(String prefix, Class< ? > srcClazz, Object src, Object dest, String... ignoreFields)
  {
    final Field[] fields = srcClazz.getDeclaredFields();
    AccessibleObject.setAccessible(fields, true);
    for (Field field : fields) {
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
          } else {
            field.set(dest, srcFieldValue);
            log.info(StringUtils.defaultString(prefix) + field.getName() + " = " + srcFieldValue);
          }
        } catch (IllegalAccessException ex) {
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
   * @param filename Filename (can include relative path settings): "test.xsl", "/fo-styles/doit.xsl".
   * @return Object[2]: First value is the InputStream and second value is the url in external form.
   */
  public Object[] getInputStream(String filename)
  {
    InputStream is = null;
    String url = null;
    final File base = new File(getResourcePath());
    if (base.isDirectory() == true) {
      final File file = new File(base, filename);
      if (file.exists() == false) {
        showNonExistingMessage(file, false);
      } else {
        try {
          is = new FileInputStream(file);
          url = file.toURI().toString();
        } catch (FileNotFoundException ex) {
          log.error(file.getAbsoluteFile() + ": " + ex.getMessage(), ex); // Should not occur.
          is = null;
        }
        showExistingMessage(file, false);
      }
    }
    if (is == null) {
      final ClassLoader cLoader = Configuration.class.getClassLoader();
      url = Configuration.class.getClassLoader().getResource(filename).toExternalForm();
      is = cLoader.getResourceAsStream(filename);
    }
    if (is == null) {
      log.error("File '" + filename + "' not found (wether in file system under '" + base.getAbsolutePath() + "' nor in resource!)");
    }
    final Object[] result = new Object[2];
    result[0] = is;
    result[1] = url;
    return result;
  }

  /**
   * Tries to get the given filename from the application's resource dir (file system). If not exist, the content will be taken as resource
   * input stream. Calls getInputStream(filename) and converts input stream to String.
   * @param filename Filename (can include relative path settings): "test.xsl", "/fo-styles/doit.xsl".
   * @return Object[2]: First value is the InputStream and second value is the url in external form.
   * @see #getInputStream(String)
   */
  public Object[] getContent(String filename)
  {
    final Object[] result = getInputStream(filename);
    final InputStream is = (InputStream) result[0];
    try {
      result[0] = IOUtils.toString(is, "UTF-8");
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
    }
    return result;
  }

  private static void showNonExistingMessage(File file, boolean directory)
  {
    // Synchronized not needed, for concurrent calls, output entries exist twice in the worst case.
    if (nonExistingResources.contains(file.getAbsolutePath()) == false) {
      nonExistingResources.add(file.getAbsolutePath());
      existingResources.remove(file.getAbsolutePath()); // If changed by administrator during application running.
      String type = directory == true ? "directory" : "file";
      log.info("Using default " + type + " of ProjectForge, because " + type + "'" + file.getAbsolutePath() + "' does not exist (OK)");
    }
  }

  private static void showExistingMessage(File file, boolean directory)
  {
    // Synchronized not needed, for concurrent calls, output entries exist twice in the worst case.
    if (existingResources.contains(file.getAbsolutePath()) == false) {
      existingResources.add(file.getAbsolutePath());
      nonExistingResources.remove(file.getAbsolutePath()); // If changed by administrator during application running.
      String type = directory == true ? "directory" : "file";
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
  protected static boolean accept(Field field)
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
  public void setJiraBrowseBaseUrl(String jiraBrowseBaseUrl)
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
   * The mail account for receiving mobile blogging entries by mail.
   * @return
   */
  public MailAccountConfig getMebMailAccount()
  {
    return mebMailAccount;
  }

  public boolean isMebConfigured()
  {
    if (StringUtils.isNotEmpty(getStringValue(ConfigurationParam.MEB_SMS_RECEIVING_PHONE_NUMBER)) == true
        || isMebMailAccountConfigured() == true) {
      return true;
    }
    return false;
  }

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
      File file = new File(applicationHomeDir, resourceDir);
      this.applicationsResourcePath = file.getAbsolutePath();
    }
    return applicationsResourcePath;
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
  public void setWorkingDirectory(String workingDirectory)
  {
    this.workingDirectory = workingDirectory;
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
  public void setTempDirectory(String tempDirectory)
  {
    this.tempDirectory = tempDirectory;
  }

  public String getConfigFilePath()
  {
    return configFile.getPath();
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

  public void setServletContextPath(String servletContextPath)
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

  public void setDomain(String domain)
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

  public SSLSocketFactory getProjectForgesSSLSocketFactory()
  {
    return projectforgesSSLSocketFactory;
  }

  public SSLSocketFactory getUsersSSLSocketFactory()
  {
    return usersSSLSocketFactory;
  }

  public ConfigurableListableBeanFactory getBeanFactory()
  {
    return this.beanFactory;
  }

  public void setBeanFactory(final ConfigurableListableBeanFactory beanFactory)
  {
    this.beanFactory = beanFactory;
  }

  public void setConfigurationDao(final ConfigurationDao configurationDao)
  {
    this.configurationDao = configurationDao;
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
   * Return the configured time zone. If not found the default time zone of the system is returned.
   * @param parameter
   * @see ConfigurationDao#getTimeZoneValue(ConfigurationParam)
   */
  public TimeZone getDefaultTimeZone()
  {
    final TimeZone timeZone = (TimeZone) getValue(ConfigurationParam.DEFAULT_TIMEZONE);
    if (timeZone != null) {
      return timeZone;
    } else {
      return TimeZone.getDefault();
    }
  }

  /**
   * @return The string value of the given parameter stored as ConfigurationDO in the data base.
   * @throws ClassCastException if configuration parameter is from the wrong type.
   */
  public String getStringValue(final ConfigurationParam parameter)
  {
    return (String) getValue(parameter);
  }

  /**
   * @return The BigDecimal value of the given parameter stored as ConfigurationDO in the data base.
   * @throws ClassCastException if configuration parameter is from the wrong type.
   */
  public BigDecimal getPercentValue(final ConfigurationParam parameter)
  {
    return (BigDecimal) getValue(parameter);
  }

  /**
   * @return The Integer value of the given parameter stored as ConfigurationDO in the data base.
   * @throws ClassCastException if configuration parameter is from the wrong type.
   */
  public Integer getTaskIdValue(final ConfigurationParam parameter)
  {
    final TaskDO task = (TaskDO) getValue(parameter);
    if (task != null) {
      return task.getId();
    }
    return null;
  }

  public boolean isAddressManagementConfigured()
  {
    return getTaskIdValue(ConfigurationParam.DEFAULT_TASK_ID_4_ADDRESSES) != null;
  }

  public boolean isBookManagementConfigured()
  {
    return getTaskIdValue(ConfigurationParam.DEFAULT_TASK_ID_4_BOOKS) != null;
  }

  private Object getValue(final ConfigurationParam parameter)
  {
    checkRefresh();
    return this.configurationParamMap.get(parameter);
  }

  @Override
  protected void refresh()
  {
    log.info("Initializing Configuration (ConfigurationDO parameters) ...");
    final Map<ConfigurationParam, Object> newMap = new HashMap<ConfigurationParam, Object>();
    final List<ConfigurationDO> list = configurationDao.internalLoadAll();
    for (final ConfigurationParam param : ConfigurationParam.values()) {
      ConfigurationDO configuration = null;
      for (final ConfigurationDO entry : list) {
        if (StringUtils.equals(param.getKey(), entry.getParameter()) == true) {
          configuration = entry;
          break;
        }
      }
      newMap.put(param, configurationDao.getValue(param, configuration));
    }
    this.configurationParamMap = newMap;
  }
}
