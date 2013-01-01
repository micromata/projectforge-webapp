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

import org.projectforge.core.BaseDao;
import org.projectforge.user.UserRightId;

public class BankAccountDao extends BaseDao<BankAccountDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_BANK_ACCOUNT", "unused", "unused");;

  public BankAccountDao()
  {
    super(BankAccountDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  public BankAccountDO newInstance()
  {
    return new BankAccountDO();
  }
}
