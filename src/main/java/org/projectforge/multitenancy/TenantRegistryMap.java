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
import org.projectforge.access.AccessException;
import org.projectforge.common.AbstractCache;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ThreadLocalUserContext;
import org.projectforge.user.UserRights;

/**
 * Holds TenantCachesHolder element and detaches them if not used for some time to save memory.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TenantRegistryMap extends AbstractCache
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TenantRegistryMap.class);

  private static final long EXPIRE_TIME = AbstractCache.TICKS_PER_DAY;

  private final Map<Integer, TenantRegistry> tenantRegistryMap = new HashMap<Integer, TenantRegistry>();

  private TenantRegistry singleTenantRegistry;

  private TenantRegistry dummyTenantRegistry;

  private static TenantRegistryMap instance = new TenantRegistryMap();

  public static TenantRegistryMap getInstance()
  {
    return instance;
  }

  private TenantRegistryMap()
  {
    super(EXPIRE_TIME);
  }

  public TenantRegistry getTenantRegistry(TenantDO tenant)
  {
    checkRefresh();
    if (TenantChecker.getInstance().isMultiTenancyAvailable() == false) {
      if (tenant != null) {
        log.warn("Oups, why call getTenantRegistry with tenant " + tenant.getId() + " if ProjectForge is running in single tenant mode?");
      }
      return getSingleTenantRegistry();
    }
    if (tenant == null) {
      final TenantDO defaultTenant = Registry.instance().getTenantsCache().getDefaultTenant();
      if (defaultTenant != null) {
        final PFUserDO user = ThreadLocalUserContext.getUser();
        if (TenantChecker.getInstance().isPartOfTenant(defaultTenant, user)) {
          tenant = defaultTenant;
        }
      }
    }
    if (tenant == null) {
      final PFUserDO user = ThreadLocalUserContext.getUser();
      if (user == null) {
        return getDummyTenantRegistry();
      }
      if (UserRights.getAccessChecker().isUserMemberOfAdminGroup(user) == true) {
        throw new AccessException("multitenancy.accessException.noTenant.adminUser");
      }
      throw new AccessException("multitenancy.accessException.noTenant.nonAdminUser");
    }
    Validate.notNull(tenant);
    synchronized (this) {
      TenantRegistry registry = tenantRegistryMap.get(tenant.getId());
      if (registry == null) {
        registry = new TenantRegistry(tenant);
        tenantRegistryMap.put(tenant.getId(), registry);
      }
      return registry;
    }
  }

  public TenantRegistry getTenantRegistry()
  {
    if (TenantChecker.getInstance().isMultiTenancyAvailable() == false) {
      return getSingleTenantRegistry();
    }
    final TenantDO tenant = TenantChecker.getCurrentTenant();
    return getTenantRegistry(tenant);
  }

  private TenantRegistry getSingleTenantRegistry()
  {
    synchronized (this) {
      if (singleTenantRegistry == null) {
        singleTenantRegistry = new TenantRegistry(null);
      }
      return singleTenantRegistry;
    }
  }

  /**
   * The dummy tenant registry is implemented only for misconfigured systems, meaning the administrator has configured tenants but no
   * default tenant and the users try to login in, but no tenant will be found (error messages will occur). The super admin is the onliest
   * user to fix this issue (because the system is available due to this dummy tenant).
   * @return
   */
  private TenantRegistry getDummyTenantRegistry()
  {
    synchronized (this) {
      if (dummyTenantRegistry == null) {
        final TenantDO dummyTenant = new TenantDO().setName("Dummy tenant").setShortName("Dummy tenant")
            .setDescription("This tenant is only a technical tenant, if no default tenant is given.");
        dummyTenant.setId(-1);
        dummyTenantRegistry = new TenantRegistry(dummyTenant);
      }
      return dummyTenantRegistry;
    }
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
