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

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PFUserDOConverter
{
  static final String ID_PREFIX = "pf-id-";

  public static Integer getId(final LdapUser user)
  {
    final String employeeNumber = user.getEmployeeNumber();
    if (employeeNumber != null && employeeNumber.startsWith(ID_PREFIX) == true && employeeNumber.length() > ID_PREFIX.length()) {
      final String id = employeeNumber.substring(ID_PREFIX.length());
      return NumberHelper.parseInteger(id);
    }
    return null;
  }

  public static PFUserDO convert(final LdapUser ldapUser)
  {
    final PFUserDO user = new PFUserDO();
    user.setLastname(ldapUser.getSurname());
    user.setFirstname(ldapUser.getGivenName());
    user.setUsername(ldapUser.getUid());
    user.setId(getId(ldapUser));
    user.setOrganization(ldapUser.getOrganization());
    user.setDescription(ldapUser.getDescription());
    final String[] mails = ldapUser.getMail();
    if (mails != null) {
      for (final String mail : mails) {
        if (StringUtils.isNotEmpty(mail) == true) {
          user.setEmail(mail);
          break;
        }
      }
    }
    if (ldapUser.isDeleted() == true) {
      user.setDeleted(true);
    }
    if (ldapUser.isDeactivated() == true || LdapUserDao.isDeactivated(ldapUser) == true) {
      user.setDeactivated(true);
    }
    if (ldapUser.isRestrictedUser() == true || LdapUserDao.isRestrictedUser(ldapUser) == true) {
      user.setRestrictedUser(true);
    }
    return user;
  }

  public static LdapUser convert(final PFUserDO user)
  {
    final LdapUser ldapUser = new LdapUser();
    ldapUser.setSurname(user.getLastname());
    ldapUser.setGivenName(user.getFirstname());
    ldapUser.setUid(user.getUsername());
    if (user.getId() != null) {
      ldapUser.setEmployeeNumber(buildEmployeeNumber(user));
    }
    ldapUser.setOrganization(user.getOrganization());
    ldapUser.setDescription(user.getDescription());
    ldapUser.setMail(user.getEmail());
    ldapUser.setDeleted(user.isDeleted());
    ldapUser.setDeactivated(user.isDeactivated());
    if (user.isDeactivated() == true) {
      ldapUser.setMail(LdapUserDao.DEACTIVATED_MAIL);
    }
    ldapUser.setRestrictedUser(user.isRestrictedUser());
    return ldapUser;
  }

  public static String buildEmployeeNumber(final PFUserDO user)
  {
    return ID_PREFIX + user.getId();
  }

  /**
   * Copies the fields shared with ldap.
   * @param src
   * @param dest
   * @return true if any modification is detected, otherwise false.
   */
  public static boolean copyUserFields(final PFUserDO src, final PFUserDO dest)
  {
    final boolean modified = BeanHelper.copyProperties(src, dest, true, "username", "firstname", "lastname", "email", "description",
        "organization", "deactivated", "restrictedUser");
    return modified;
  }

  /**
   * Copies the fields. The field commonName is also copied because the dn is built from the uid, ou and dc. The cn isn't part of the dn.
   * @param src
   * @param dest
   * @return true if any modification is detected, otherwise false.
   */
  public static boolean copyUserFields(final LdapUser src, final LdapUser dest)
  {
    setMailNullArray(src);
    setMailNullArray(dest);
    final boolean modified = BeanHelper.copyProperties(src, dest, true, "commonName", "givenName", "surname", "mail", "description",
        "organization", "deactivated", "restrictedUser");
    return modified;
  }

  static void setMailNullArray(final LdapUser ldapUser)
  {
    if (ldapUser.getMail() == null) {
      return;
    }
    for (final String mail : ldapUser.getMail()) {
      if (mail != null) {
        return;
      }
    }
    // All array entries are null, therefore set the mail value itself to null:
    ldapUser.setMail((String[]) null);
  }
}
