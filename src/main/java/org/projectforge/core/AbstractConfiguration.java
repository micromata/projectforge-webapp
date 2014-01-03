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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.AbstractCache;
import org.projectforge.task.TaskDO;
import org.projectforge.xml.stream.XmlObject;

/**
 * This class also provides the configuration of the parameters which are stored via ConfigurationDao. Those parameters are cached. <br/>
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@XmlObject(alias = "config")
public abstract class AbstractConfiguration extends AbstractCache
{
  private static transient final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractConfiguration.class);

  protected ConfigurationDao configurationDao;

  protected Map<ConfigurationParam, Object> configurationParamMap;

  private final boolean global;

  public void setConfigurationDao(final ConfigurationDao configurationDao)
  {
    this.configurationDao = configurationDao;
  }

  public static void init4TestMode()
  {
    GlobalConfiguration._init4TestMode();
    Configuration._init4TestMode();
  }

  // public static AbstractConfiguration getInstance()
  // {
  // if (instance == null) {
  // throw new IllegalStateException("Configuration is not yet configured");
  // }
  // return instance;
  // }

  // public static boolean isInitialized()
  // {
  // return instance != null;
  // }

  public AbstractConfiguration(final boolean global)
  {
    super(TICKS_PER_HOUR);
    this.global = global;
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

  protected Object getValue(final ConfigurationParam parameter)
  {
    checkRefresh();
    return this.configurationParamMap.get(parameter);
  }

  protected abstract List<ConfigurationDO> loadParameters();

  @Override
  protected void refresh()
  {
    // if (testMode == true) {
    // // Do nothing.
    // log.info("Initializing Configuration (ConfigurationDO parameters): Do nothing (test mode)...");
    // return;
    // }
    log.info("Initializing " + this.getClass().getName() + " (ConfigurationDO parameters) ...");
    final Map<ConfigurationParam, Object> newMap = new HashMap<ConfigurationParam, Object>();
    List<ConfigurationDO> list;
    try {
      list = loadParameters();
    } catch (final Exception ex) {
      log.fatal("******* Exception while getting configuration parameters from data-base (only OK for migration from older versions): "
          + ex.getMessage());
      list = new ArrayList<ConfigurationDO>();
    }
    for (final ConfigurationParam param : ConfigurationParam.values()) {
      if (param.isGlobal() != global) {
        continue;
      }
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
