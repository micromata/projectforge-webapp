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

package org.projectforge.web.rest.converter;

import org.projectforge.rest.objects.UserObject;
import org.projectforge.user.PFUserDO;

/**
 * For conversion of PFUserDO to user object.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class PFUserDOConverter
{
  public static UserObject getUserObject(final PFUserDO userDO)
  {
    final UserObject user = new UserObject();
    DOConverter.copyFields(user, userDO);
    user.setUsername(userDO.getUsername());
    user.setFirstName(userDO.getFirstname());
    user.setLastName(userDO.getLastname());
    user.setEmail(userDO.getEmail());
    return user;
  }
}