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

package org.projectforge.ldap;

import org.projectforge.user.GroupDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class GroupDOConverter
{
  public static final String UID_PREFIX = "pf-address-";

  public static GroupDO convert(final LdapGroup group) {
    final GroupDO pfGroup = new GroupDO();
    pfGroup.setId(group.getGidNumber());
    pfGroup.setName(group.getCommonName());
    pfGroup.setOrganization(group.getOrganization());
    pfGroup.setDescription(group.getDescription());
    return pfGroup;
  }

  public static LdapGroup convert(final GroupDO pfGroup)
  {
    final LdapGroup group = new LdapGroup();
    group.setGidNumber(pfGroup.getId());
    group.setCommonName(pfGroup.getName());
    group.setOrganization(pfGroup.getOrganization());
    group.setDescription(pfGroup.getDescription());
    return group;
  }
}
