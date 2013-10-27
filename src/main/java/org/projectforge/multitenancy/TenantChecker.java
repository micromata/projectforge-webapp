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

import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.Configuration;

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

  public boolean isMultiTenancyAvailable()
  {
    return Configuration.getInstance().isMultiTenancyConfigured();
  }

  /**
   * @param tenant
   * @param obj
   * @return true if id of tenant is equals to tenant id of the given object, otherwise false.
   */
  public boolean isPartOfTenant(final TenantDO tenant, final AbstractBaseDO< ? > obj)
  {
    if (tenant == null) {
      return false;
    }
    return isPartOfTenant(tenant.getId(), obj);
  }

  public boolean isPartOfTenant(final Integer tenantId, final AbstractBaseDO< ? > obj)
  {
    if (obj == null) {
      return false;
    }
    if (tenantId == null || obj.getTenantId() == null) {
      return false;
    }
    return tenantId.equals(obj.getTenantId());
  }
}
