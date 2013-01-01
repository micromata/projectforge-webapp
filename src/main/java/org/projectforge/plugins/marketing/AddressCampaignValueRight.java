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
import org.projectforge.address.AddressDao;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;

/**
 * @author Kai Reinhard (k.reinhard@me.de)
 */
public class AddressCampaignValueRight extends UserRightAccessCheck<AddressCampaignValueDO>
{
  private static final long serialVersionUID = 4021610615575404717L;

  public AddressCampaignValueRight()
  {
    super(AddressCampaignValueDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  /**
   * @return true for select access for marketing users and true for admin users, otherwise false.
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final AddressCampaignValueDO obj, final AddressCampaignValueDO oldObj,
      final OperationType operationType)
  {
    final AddressDao addressDao = (AddressDao) Registry.instance().getDao(AddressDao.class);
    if (operationType.isIn(OperationType.SELECT, OperationType.INSERT) == true && obj == null) {
      return addressDao.hasInsertAccess(user);
    }
    return addressDao.hasAccess(user, obj != null ? obj.getAddress() : null, oldObj != null ? oldObj.getAddress() : null, operationType,
        false);
  }
}
