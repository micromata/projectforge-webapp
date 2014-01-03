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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.projectforge.common.AbstractCache;

/**
 * Holds TenantCachesHolder element and detaches them if not used for some time to save memory.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TenantRegistryMap extends AbstractCache
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TenantRegistryMap.class);

  private static final long EXPIRE_TIME = AbstractCache.TICKS_PER_DAY;

  private final Map<Integer, TenantRegistry> tenantRegistryMap = new HashMap<Integer, TenantRegistry>();

  private static TenantRegistryMap instance = new TenantRegistryMap();

  public static TenantRegistryMap getInstance()
  {
    return instance;
  }

  private TenantRegistryMap()
  {
    super(EXPIRE_TIME);
  }

  public TenantRegistry getTenantRegistry(final TenantDO tenant)
  {
    Validate.notNull(tenant);
    checkRefresh();
    TenantRegistry registry = tenantRegistryMap.get(tenant.getId());
    if (registry == null) {
      registry = new TenantRegistry(tenant);
      tenantRegistryMap.put(tenant.getId(), registry);
    }
    return registry;
  }

  public TenantRegistry getTenantRegistry()
  {
    final TenantDO tenant = TenantChecker.getCurrentTenant();
    if (tenant == null) {
      log.warn("Current tenant is null in ThreadLocal.");
      return null;
    }
    return getTenantRegistry(tenant);
  }

  /**
   * @see org.projectforge.common.AbstractCache#refresh()
   */
  @Override
  protected void refresh()
  {
    log.info("Refreshing " + TenantRegistry.class.getName() + "...");
    final Iterator<Map.Entry<Integer, TenantRegistry>> it = tenantRegistryMap.entrySet().iterator();
    while (it.hasNext() == true) {
      final Map.Entry<Integer, TenantRegistry> entry = it.next();
      final TenantRegistry registry = entry.getValue();
      if (registry.isOutdated() == true) {
        final TenantDO tenant = registry.getTenant();
        log.info("Detaching caches of tenant '"
            + (tenant != null ? tenant.getShortName() : "null")
            + "' with id "
            + (tenant != null ? tenant.getId() : "null"));
        it.remove();
      }
    }
    log.info("Refreshing of " + TenantRegistry.class.getName() + " done.");
  }
}
