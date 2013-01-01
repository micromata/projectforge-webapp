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

import javax.sql.DataSource;

import org.projectforge.common.AbstractCache;
import org.projectforge.database.HibernateUtils;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Provides some system information in a cache.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SystemInfoCache extends AbstractCache
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SystemInfoCache.class);

  private DataSource dataSource;

  private boolean cost2EntriesExists, projectEntriesExists, customerEntriesExists;

  /**
   * SystemInfoCache can be used either over Spring context or with this static method.
   * @return
   */
  public static SystemInfoCache instance()
  {
    return instance;
  }

  private static SystemInfoCache instance;

  /**
   * Only for internal usage on start-up of ProjectForge.
   * @param theInstance
   */
  public static void internalInitialize(final SystemInfoCache theInstance)
  {
    instance = theInstance;
  }

  public boolean isCustomerEntriesExists()
  {
    checkRefresh();
    return customerEntriesExists;
  }

  public boolean isProjectEntriesExists()
  {
    checkRefresh();
    return projectEntriesExists;
  }

  public boolean isCost2EntriesExists()
  {
    checkRefresh();
    return cost2EntriesExists;
  }

  @Override
  protected void refresh()
  {
    log.info("Refreshing SystemInfoCache...");
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    customerEntriesExists = hasTableEntries(jdbc, KundeDO.class);
    projectEntriesExists = hasTableEntries(jdbc, ProjektDO.class);
    cost2EntriesExists = hasTableEntries(jdbc, Kost2DO.class);
    log.info("Refreshing SystemInfoCache done.");
  }

  private boolean hasTableEntries(final JdbcTemplate jdbc, final Class< ? > entity)
  {
    try {
      final int count = jdbc.queryForInt("SELECT COUNT(*) FROM " + HibernateUtils.getDBTableName(entity));
      return count > 0;
    } catch (final Exception ex) {
      log.error(ex.getMessage(), ex);
      return false;
    }
  }

  public void setDataSource(final DataSource dataSource)
  {
    this.dataSource = dataSource;
  }
}
