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

package org.projectforge.plugins.marketing;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * @author Kai Reinhard (k.reinhard@me.de)
 */
public class AddressCampaignRight extends UserRightAccessCheck<AddressCampaignDO>
{
  private static final long serialVersionUID = 4021610615575404717L;

  public AddressCampaignRight()
  {
    super(AddressCampaignDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  /**
   * @return true for select access for marketing users and true for admin users, otherwise false.
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final AddressCampaignDO obj, final AddressCampaignDO oldObj, final OperationType operationType)
  {
    if (operationType == OperationType.SELECT == true) {
      return true;
    } else if (UserRights.getAccessChecker().isUserMemberOfAdminGroup(user) == true) {
      return true;
    }
    return false;
  }
}
