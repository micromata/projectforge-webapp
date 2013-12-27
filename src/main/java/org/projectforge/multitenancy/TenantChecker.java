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

package org.projectforge.multitenancy;

import org.projectforge.access.AccessException;
import org.projectforge.core.BaseDO;
import org.projectforge.core.Configuration;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ThreadLocalUserContext;
import org.projectforge.user.UserContext;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TenantChecker
{
  private static final TenantChecker instance = new TenantChecker();

  public static TenantChecker getInstance()
  {
    return instance;
  }

  private TenantDao tenantDao;

  public boolean isMultiTenancyAvailable()
  {
    return Configuration.getInstance().isMultiTenancyConfigured();
  }

  /**
   * @param tenant
   * @param obj
   * @return true if id of tenant is equals to tenant id of the given object, otherwise false.
   */
  public boolean isPartOfTenant(final TenantDO tenant, final BaseDO< ? > obj)
  {
    if (tenant == null) {
      return false;
    }
    return isPartOfTenant(tenant.getId(), obj);
  }

  public boolean isPartOfTenant(final Integer tenantId, final BaseDO< ? > obj)
  {
    if (obj == null) {
      return false;
    }
    if (tenantId == null || obj.getTenantId() == null) {
      return false;
    }
    return tenantId.equals(obj.getTenantId());
  }

  /**
   * Checks only if the current chosen tenant of the logged-in-user fit the tenant of the given object. This means the user's current tenant
   * is the same tenant the given object is assigned to, or the current tenant is the default tenant and the given object is not assigned to
   * a tenant.<br/>
   * If no multi-tenancy is configured, always true is returned.
   * @param obj
   * @return
   */
  public boolean isPartOfCurrentTenant(final BaseDO< ? > obj)
  {
    if (isMultiTenancyAvailable() == false) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    final UserContext userContext = ThreadLocalUserContext.getUserContext();
    if (userContext == null) {
      return false;
    }
    final TenantDO currentTenant = userContext.getCurrentTenant();
    if (currentTenant == null) {
      return false;
    }
    if (obj.getTenantId() == null) {
      // The given object isn't assigned to a tenant, so assuming the default tenant.
      return currentTenant.isDefaultTenant();
    }
    return obj.getTenantId().equals(currentTenant.getId());
  }

  public void checkPartOfCurrentTenant(final BaseDO< ? > obj)
  {
    if (isPartOfCurrentTenant(obj) == false) {
      final UserContext userContext = ThreadLocalUserContext.getUserContext();
      final TenantDO currentTenant = userContext.getCurrentTenant();
      final String currentTenantString = currentTenant != null ? currentTenant.getShortName() : ThreadLocalUserContext.getLocalizedString("multitenancy.defaultTenant");
      final TenantDO objectTenant = obj.getTenant();
      final String objectTenantString = objectTenant != null ? objectTenant.getShortName() : ThreadLocalUserContext.getLocalizedString("multitenancy.defaultTenant");
      throw new AccessException(ThreadLocalUserContext.getUser(), "access.exception.usersCurrentTenantDoesNotMatch", currentTenantString, objectTenantString);
    }
  }

  public boolean isPartOfTenant(final TenantDO tenant, final PFUserDO user)
  {
    if (tenant == null) {
      return false;
    }
    return isPartOfTenant(tenant.getId(), user);
  }

  public boolean isPartOfTenant(final Integer tenantId, final PFUserDO user)
  {
    // if (isPartOfTenant(tenantId, (AbstractBaseDO< ? >) user) == true) {
    // Ignore this setting (because it's weather displayed nor modifiable!
    // return true;
    // }
    if (getTenantsCache().isUserAssignedToTenant(tenantId, user.getId()) == true) {
      return true;
    }
    return false;
  }

  /**
   * @param user
   * @return true in multi-tenancy environments if the given user (may be initialized by Hibernate or not) has the read-write right of
   *         {@link TenantDao#USER_RIGHT_ID}.
   */
  public boolean isSuperAdmin(final PFUserDO user)
  {
    return isMultiTenancyAvailable() == true && getDao().hasInsertAccess(user) == true;
  }

  private TenantDao getDao()
  {
    if (tenantDao == null) {
      tenantDao = Registry.instance().getDao(TenantDao.class);
    }
    return tenantDao;
  }

  private TenantsCache getTenantsCache()
  {
    return getDao().getTenantsCache();
  }
}
