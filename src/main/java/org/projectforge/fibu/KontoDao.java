/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.fibu;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;

public class KontoDao extends BaseDao<KontoDO>
{
  private KontoCache kontoCache;

  public KontoDao()
  {
    super(KontoDO.class);
  }

  /**
   * @see org.projectforge.core.BaseDao#afterSaveOrModify(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void afterSaveOrModify(final KontoDO obj)
  {
    kontoCache.refresh();
  }

  @SuppressWarnings("unchecked")
  public KontoDO getKonto(final Integer kontonummer)
  {
    if (kontonummer == null) {
      return null;
    }
    final List<KontoDO> list = getHibernateTemplate().find("from KontoDO u where u.nummer = ?", kontonummer);
    if (CollectionUtils.isEmpty(list) == true) {
      return null;
    }
    return list.get(0);
  }

  /**
   * User must member of group finance or controlling.
   * @see org.projectforge.core.BaseDao#hasSelectAccess()
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(user, throwException, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasSelectAccess(PFUserDO, org.projectforge.core.ExtendedBaseDO, boolean)
   * @see #hasSelectAccess(PFUserDO, boolean)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final KontoDO obj, final boolean throwException)
  {
    return hasSelectAccess(user, throwException);
  }

  /**
   * User must member of group finance.
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final KontoDO obj, final KontoDO oldObj, final OperationType operationType,
      final boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(user, throwException, ProjectForgeGroup.FINANCE_GROUP);
  }

  @Override
  public KontoDO newInstance()
  {
    return new KontoDO();
  }

  /**
   * @return the kontoCache
   */
  public KontoCache getKontoCache()
  {
    return kontoCache;
  }

  /**
   * @param kontoCache the kontoCache to set
   * @return this for chaining.
   */
  public void setKontoCache(final KontoCache kontoCache)
  {
    this.kontoCache = kontoCache;
  }
}
