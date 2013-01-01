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

package org.projectforge.plugins.banking;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRights;

/**
 * 
 * @author Kai Reinhard (k.reinhard@me.de)
 * 
 */
public class BankAccountRight extends UserRightAccessCheck<BankAccountDO>
{
  private static final long serialVersionUID = -1711148447929915434L;

  public BankAccountRight()
  {
    super(BankAccountDao.USER_RIGHT_ID, UserRightCategory.FIBU);
  }

  /**
   * @return true if the user is member of group FINANCE or CONTROLLING.
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(org.projectforge.access.AccessChecker, org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user)
  {
    return UserRights.getAccessChecker().isUserMemberOfGroup(user, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(java.lang.Object)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final BankAccountDO obj)
  {
    return hasSelectAccess(user);
  }

  /**
   * @return true if user is member of group FINANCE.
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(java.lang.Object)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final BankAccountDO obj, final BankAccountDO oldObj, final OperationType operationType)
  {
    return UserRights.getAccessChecker().isUserMemberOfGroup(user, ProjectForgeGroup.FINANCE_GROUP);
  }


}
