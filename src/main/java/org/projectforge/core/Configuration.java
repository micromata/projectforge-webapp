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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.AbstractCache;
import org.projectforge.common.DateFormats;
import org.projectforge.task.TaskDO;
import org.projectforge.xml.stream.XmlObject;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * This class also provides the configuration of the parameters which are stored via ConfigurationDao. Those parameters are cached. <br/>
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@XmlObject(alias = "config")
public class Configuration extends AbstractCache
{
  private static transient final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Configuration.class);

  private static transient Configuration instance;

  private ConfigurationDao configurationDao;

  private ConfigurableListableBeanFactory beanFactory;

  private Map<ConfigurationParam, Object> configurationParamMap;

  private boolean testMode, developmentMode;

  public void setBeanFactory(final ConfigurableListableBeanFactory beanFactory)
  {
    this.beanFactory = beanFactory;
  }

  public ConfigurableListableBeanFactory getBeanFactory()
  {
    return this.beanFactory;
  }

  public void setConfigurationDao(final ConfigurationDao configurationDao)
  {
    this.configurationDao = configurationDao;
  }

  public static void init4TestMode()
  {
    if (instance == null) {
      new Configuration();
      instance.testMode = true;
      instance.developmentMode = true;
      instance.configurationParamMap = new HashMap<ConfigurationParam, Object>();
    }
  }

  public static Configuration getInstance()
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

  public static boolean isDevelopmentMode()
  {
    if (instance == null) {
      return false;
    }
    return instance.developmentMode;
  }

  public void internalSetDevelopmentMode(final boolean developmentMode)
  {
    this.developmentMode = developmentMode;
  }

  public Configuration()
  {
    super(TICKS_PER_HOUR);
    instance = this;
  }

  public boolean isMebConfigured()
  {
    if (StringUtils.isNotEmpty(getStringValue(ConfigurationParam.MEB_SMS_RECEIVING_PHONE_NUMBER)) == true
        || isMebMailAccountConfigured() == true) {
      return true;
    }
    return false;
  }

  /**
   * @see ConfigXml#isMebMailAccountConfigured()
   */
  public boolean isMebMailAccountConfigured()
  {
    return ConfigXml.getInstance().isMebMailAccountConfigured();
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
   * Available date formats (configurable as parameter, see web dialogue with system parameters).
   * @return
   */
  public String[] getDateFormats()
  {
    final String str = getStringValue(ConfigurationParam.DATE_FORMATS);
    final String[] sa = StringUtils.split(str, " \t\r\n,;");
    return sa;
  }

  /**
   * @return The first entry of {@link #getDateFormats()} if exists, otherwise yyyy-MM-dd (ISO date format).
   */
  public String getDefaultDateFormat()
  {
    final String[] sa = getDateFormats();
    if (sa != null && sa.length > 0) {
      return sa[0];
    } else {
      return DateFormats.ISO_DATE;
    }
  }

  /**
   * Available excel date formats (configurable as parameter, see web dialogue with system parameters).
   * @return
   */
  public String[] getExcelDateFormats()
  {
    final String str = getStringValue(ConfigurationParam.EXCEL_DATE_FORMATS);
    final String[] sa = StringUtils.split(str, " \t\r\n,;");
    return sa;
  }

  /**
   * @return The first entry of {@link #getExcelDateFormats()} if exists, otherwise YYYY-MM-DD (ISO date format).
   */
  public String getDefaultExcelDateFormat()
  {
    final String[] sa = getExcelDateFormats();
    if (sa != null && sa.length > 0) {
      return sa[0];
    } else {
      return DateFormats.EXCEL_ISO_DATE;
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
   * @return The boolean value of the given parameter stored as ConfigurationDO in the data base.
   * @throws ClassCastException if configuration parameter is from the wrong type.
   */
  public boolean getBooleanValue(final ConfigurationParam parameter)
  {
    final Object obj = getValue(parameter);
    if (obj != null && Boolean.TRUE.equals(obj)) {
      return true;
    } else {
      return false;
    }
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

  public boolean isCostConfigured()
  {
    return getBooleanValue(ConfigurationParam.COST_CONFIGURED);
  }

  private Object getValue(final ConfigurationParam parameter)
  {
    checkRefresh();
    return this.configurationParamMap.get(parameter);
  }

  @Override
  protected void refresh()
  {
    if (testMode == true) {
      // Do nothing.
      log.info("Initializing Configuration (ConfigurationDO parameters): Do nothing (test mode)...");
      return;
    }
    log.info("Initializing Configuration (ConfigurationDO parameters) ...");
    final Map<ConfigurationParam, Object> newMap = new HashMap<ConfigurationParam, Object>();
    List<ConfigurationDO> list;
    try {
      list = configurationDao.internalLoadAll();
    } catch (final Exception ex) {
      log.fatal("******* Exception while getting configuration parameters from data-base (only OK for migration from older versions): "
          + ex.getMessage());
      list = new ArrayList<ConfigurationDO>();
    }
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
