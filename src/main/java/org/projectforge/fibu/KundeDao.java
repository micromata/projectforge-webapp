/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import org.hibernate.criterion.Order;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.user.ProjectForgeGroup;


public class KundeDao extends BaseDao<KundeDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(KundeDao.class);

  public KundeDao()
  {
    super(KundeDO.class);
    avoidNullIdCheckBeforeSave = true;
  }

  @Override
  public List<KundeDO> getList(BaseSearchFilter filter)
  {
    QueryFilter queryFilter = new QueryFilter(filter);
    queryFilter.addOrder(Order.asc("id"));
    return getList(queryFilter);
  }

  /**
   * return Always true, no generic select access needed for address objects.
   * @see org.projectforge.core.BaseDao#hasSelectAccess()
   */
  @Override
  public boolean hasSelectAccess(boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(throwException, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
        ProjectForgeGroup.PROJECT_MANAGER, ProjectForgeGroup.PROJECT_ASSISTANT);
  }

  @Override
  public boolean hasSelectAccess(KundeDO obj, boolean throwException)
  {
    if (obj == null) {
      return true;
    }
    if (accessChecker.isUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP) == true) {
      return true;
    }
    if (accessChecker.isUserMemberOfGroup(ProjectForgeGroup.PROJECT_MANAGER, ProjectForgeGroup.PROJECT_ASSISTANT) == true) {
      if (obj.getStatus() != null
          && obj.getStatus().isIn(KundeStatus.ENDED, KundeStatus.NONACTIVE, KundeStatus.NONEXISTENT) == false
          && obj.isDeleted() == false) {
        // Ein Projektleiter sieht keine nicht mehr aktiven oder gelöschten Kunden.
        return true;
      }
    }
    if (throwException == true) {
      accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP);
      log.error("Should not occur! An exception should be thrown.");
    }
    return false;
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(KundeDO obj, KundeDO oldObj, OperationType operationType, boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(throwException, ProjectForgeGroup.FINANCE_GROUP);
  }

  @Override
  public boolean hasHistoryAccess(boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(throwException, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
  }

  @Override
  public KundeDO newInstance()
  {
    return new KundeDO();
  }
}
