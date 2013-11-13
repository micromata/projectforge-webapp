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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.projectforge.common.AbstractCache;
import org.projectforge.user.PFUserDO;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Caches the tenants.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TenantsCache extends AbstractCache
{
  private static Logger log = Logger.getLogger(TenantsCache.class);

  private HibernateTemplate hibernateTemplate;

  /** The key is the order id. */
  private Collection<TenantDO> tenants;

  public boolean isEmpty()
  {
    checkRefresh();
    return CollectionUtils.isEmpty(tenants);
  }

  public TenantDO getTenant(final Integer id)
  {
    if (id == null) {
      return null;
    }
    checkRefresh();
    if (tenants == null) {
      return null;
    }
    for (final TenantDO tenant : tenants) {
      if (id.equals(tenant.getId()) == true) {
        return tenant;
      }
    }
    return null;
  }

  /**
   * @return the tenants
   */
  public Collection<TenantDO> getTenants()
  {
    checkRefresh();
    return tenants;
  }

  /**
   * @param userId
   * @return the tenants
   */
  public Collection<TenantDO> getTenantsOfUser(final Integer userId)
  {
    final Collection<TenantDO> list = getTenants();
    final Set<TenantDO> result = new TreeSet<TenantDO>(new TenantsComparator());
    if (list != null) {
      for (final TenantDO tenant : list) {
        if (tenant.isDeleted() == true) {
          continue;
        }
        if (isUserAssignedToTenant(tenant, userId) == true) {
          result.add(tenant);
        }
      }
    }
    return result;
  }

  public boolean isUserAssignedToTenant(final Integer tenantId, final Integer userId)
  {
    if (tenantId == null || userId == null) {
      return false;
    }
    final TenantDO tenant = getTenant(tenantId);
    return isUserAssignedToTenant(tenant, userId);
  }

  /**
   * 
   * @param tenant
   * @param userId
   * @return true if tenant is not null and not deleted and the given user is assigned to the given tenant. Otherwise false.
   */
  public boolean isUserAssignedToTenant(final TenantDO tenant, final Integer userId)
  {
    if (tenant == null || userId == null || tenant.isDeleted() == true) {
      return false;
    }
    final Collection<PFUserDO> assignedUsers = tenant.getAssignedUsers();
    if (assignedUsers == null) {
      return false;
    }
    if (userId == null) {
      return false;
    }
    for (final PFUserDO assignedUser : assignedUsers) {
      if (userId.equals(assignedUser.getId()) == true) {
        // User is assigned to the given tenant.
        return true;
      }
    }
    return false;
  }

  /**
   * @param list
   * @return csv list of tenants.
   */
  public String getTenantShortNames(final Collection<TenantDO> list)
  {
    if (list == null || list.size() == 0) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    String separator = "";
    for (final TenantDO tenant : list) {
      sb.append(separator).append(tenant.getShortName());
      separator = ", ";
    }
    return sb.toString();
  }

  /**
   * This method will be called by CacheHelper and is synchronized via getData();
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void refresh()
  {
    log.info("Initializing TenantsCache ...");
    // This method must not be synchronized because it works with a new copy of maps.
    final List<TenantDO> list = hibernateTemplate.find("from TenantDO t where deleted=false");
    this.tenants = list;
    log.info("Initializing of TenantsCache done.");
  }

  public void setHibernateTemplate(final HibernateTemplate hibernateTemplate)
  {
    this.hibernateTemplate = hibernateTemplate;
  }
}
