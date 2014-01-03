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

package org.projectforge.multitenancy;

import org.apache.log4j.Logger;
import org.projectforge.access.AccessDao;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.registry.Registry;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskTree;

/**
 * Holds caches of a single tenant. After the configured time to live (TTL) this registry is detached from {@link TenantRegistryMap}.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TenantRegistry
{
  /**
   * Default time to live for an holder.
   */
  private static final long TIME_TO_LIVE_MS = 60 * 60 * 1000;

  private static Logger log = Logger.getLogger(TenantRegistry.class);

  private long lastUsage;

  private final long timeToLive;

  private final Configuration configuration;

  private final TaskTree taskTree;

  private final TenantDO tenant;

  public TenantRegistry(final TenantDO tenant)
  {
    this.tenant = tenant;
    this.lastUsage = System.currentTimeMillis();
    this.timeToLive = TIME_TO_LIVE_MS;
    taskTree = new TaskTree();
    final Registry registry = Registry.instance();
    taskTree.setAccessDao(registry.getDao(AccessDao.class));
    taskTree.setAuftragDao(registry.getDao(AuftragDao.class));
    taskTree.setKostCache(registry.getKostCache());
    taskTree.setProjektDao(registry.getDao(ProjektDao.class));
    taskTree.setTaskDao(registry.getDao(TaskDao.class));
    configuration = new Configuration(tenant);
    configuration.setConfigurationDao(registry.getDao(ConfigurationDao.class));
  }

  /**
   * @return true if the last usage of this holder more ms in the past than the TTL.
   */
  public boolean isOutdated()
  {
    return System.currentTimeMillis() - lastUsage > 0;
  }

  /**
   * @return the configuration
   */
  public Configuration getConfiguration()
  {
    return configuration;
  }

  /**
   * @return the taskTree
   */
  public TaskTree getTaskTree()
  {
    updateUsageTime();
    return taskTree;
  }

  /**
   * @return the tenant
   */
  public TenantDO getTenant()
  {
    return tenant;
  }

  private void updateUsageTime()
  {
    this.lastUsage = System.currentTimeMillis();
  }

  /**
   * @return the lastUsage time in ms.
   */
  public long getLastUsage()
  {
    return lastUsage;
  }

  /**
   * @return the timeToLive of this holder in ms.
   */
  public long getTimeToLive()
  {
    return timeToLive;
  }
}
