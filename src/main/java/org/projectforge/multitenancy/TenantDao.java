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

import java.util.List;

import org.projectforge.core.BaseDao;
import org.projectforge.core.UserException;
import org.projectforge.user.UserRightId;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TenantDao extends BaseDao<TenantDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TenantDao.class);

  public static final UserRightId USER_RIGHT_ID = UserRightId.ADMIN_TENANT;

  public TenantDao()
  {
    super(TenantDO.class);
    userRightId = USER_RIGHT_ID;
  }

  public TenantDO getDefaultTenant()
  {
    @SuppressWarnings("unchecked")
    final List<TenantDO> list = getHibernateTemplate().find("from TenantDO t where t.defaultTenant = true");
    if (list != null && list.isEmpty() == true) {
      return null;
    }
    if (list.size() > 1) {
      log.warn("There are more than one tenent object declared as default! No or only one tenant should be defined as default!");
    }
    return list.get(0);
  }

  /**
   * @see org.projectforge.core.BaseDao#onSaveOrModify(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void onSaveOrModify(final TenantDO obj)
  {
    if (obj.isDefaultTenant() == false) {
      return;
    }
    final TenantDO defaultTenant = getDefaultTenant();
    if (defaultTenant == null) {
      return;
    }
    if (obj.getId() == null || defaultTenant.getId() != obj.getId()) {
      throw new UserException("multitenancy.error.maxOnlyOneTenantShouldBeDefault");
    }
  }

  @Override
  public TenantDO newInstance()
  {
    return new TenantDO();
  }

  /**
   * @see org.projectforge.core.BaseDao#useOwnCriteriaCacheRegion()
   */
  @Override
  protected boolean useOwnCriteriaCacheRegion()
  {
    return true;
  }
}
