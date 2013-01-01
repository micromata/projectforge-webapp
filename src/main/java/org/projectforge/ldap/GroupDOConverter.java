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

package org.projectforge.ldap;

import java.util.Map;

import org.apache.commons.collections.SetUtils;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.registry.Registry;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class GroupDOConverter
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupDOConverter.class);

  static final String ID_PREFIX = "pf-id-";

  public static Integer getId(final LdapGroup group)
  {
    final String businessCategory = group.getBusinessCategory();
    if (businessCategory != null && businessCategory.startsWith(ID_PREFIX) == true && businessCategory.length() > ID_PREFIX.length()) {
      final String id = businessCategory.substring(ID_PREFIX.length());
      return NumberHelper.parseInteger(id);
    }
    return null;
  }

  public static GroupDO convert(final LdapGroup group)
  {
    final GroupDO pfGroup = new GroupDO();
    pfGroup.setId(getId(group));
    pfGroup.setName(group.getCommonName());
    pfGroup.setOrganization(group.getOrganization());
    pfGroup.setDescription(group.getDescription());
    return pfGroup;
  }

  public static LdapGroup convert(final GroupDO pfGroup, final String baseDN, final Map<Integer, LdapUser> ldapUserMap)
  {
    final LdapGroup ldapGroup = new LdapGroup();
    if (pfGroup.getId() != null) {
      ldapGroup.setBusinessCategory(buildBusinessCategory(pfGroup));
    }
    ldapGroup.setCommonName(pfGroup.getName());
    ldapGroup.setOrganization(pfGroup.getOrganization());
    ldapGroup.setDescription(pfGroup.getDescription());
    if (pfGroup.getAssignedUsers() != null) {
      for (final PFUserDO user : pfGroup.getAssignedUsers()) {
        if (user.isDeactivated() == true || user.isDeleted() == true) {
          // Do not add deleted or deactivated users.
          continue;
        }
        final LdapUser ldapUser = ldapUserMap.get(user.getId());
        if (ldapUser != null) {
          ldapGroup.addMember(ldapUser, baseDN);
        } else {
          final PFUserDO cacheUser = Registry.instance().getUserGroupCache().getUser(user.getId());
          if (cacheUser == null || cacheUser.isDeleted() == false) {
            log.warn("LDAP user with id '"
                + user.getId()
                + "' not found in given ldapUserMap. User will be ignored in group '"
                + pfGroup.getName()
                + "'.");
          }
        }
      }
    }
    return ldapGroup;
  }

  public static String buildBusinessCategory(final GroupDO group)
  {
    return ID_PREFIX + group.getId();
  }

  /**
   * Copies the fields shared with ldap.
   * @param src
   * @param dest
   * @return true if any modification is detected, otherwise false.
   */
  public static boolean copyGroupFields(final GroupDO src, final GroupDO dest)
  {
    final boolean modified = BeanHelper.copyProperties(src, dest, true, "name", "organization", "description");
    return modified;
  }

  /**
   * Copies the fields.
   * @param src
   * @param dest
   * @return true if any modification is detected, otherwise false.
   */
  public static boolean copyGroupFields(final LdapGroup src, final LdapGroup dest)
  {
    boolean modified = BeanHelper.copyProperties(src, dest, true, "description", "organization");
    // Checks if the sets aren't equal:
    if (SetUtils.isEqualSet(src.getMembers(), dest.getMembers()) == false) {
      if (LdapGroupDao.hasMembers(src) == true || LdapGroupDao.hasMembers(dest) == true) {
        // If both, src and dest have no members, then do nothing, otherwise:
        modified = true;
        dest.clearMembers();
        dest.addAllMembers(src.getMembers());
      }
    }
    return modified;
  }
}
