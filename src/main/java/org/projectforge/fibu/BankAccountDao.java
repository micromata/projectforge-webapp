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

import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.user.ProjectForgeGroup;

public class BankAccountDao extends BaseDao<BankAccountDO>
{
  public BankAccountDao()
  {
    super(BankAccountDO.class);
  }

  /**
   * User must member of group finance or controlling.
   * @see org.projectforge.core.BaseDao#hasSelectAccess()
   */
  @Override
  public boolean hasSelectAccess(boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(throwException, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasSelectAccess(org.projectforge.core.ExtendedBaseDO, boolean)
   * @see #hasSelectAccess(boolean)
   */
  @Override
  public boolean hasSelectAccess(BankAccountDO obj, boolean throwException)
  {
    return hasSelectAccess(throwException);
  }

  /**
   * User must member of group finance.
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(BankAccountDO obj, BankAccountDO oldObj, OperationType operationType, boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(throwException, ProjectForgeGroup.FINANCE_GROUP);
  }

  @Override
  public BankAccountDO newInstance()
  {
    return new BankAccountDO();
  }
}
