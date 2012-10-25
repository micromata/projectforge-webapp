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

  public static Integer getId(final LdapPerson person)
  {
    final String employeeNumber = person.getEmployeeNumber();
    if (employeeNumber != null && employeeNumber.startsWith(ID_PREFIX) == true && employeeNumber.length() > ID_PREFIX.length()) {
      final String id = employeeNumber.substring(ID_PREFIX.length());
      return NumberHelper.parseInteger(id);
    }
    return null;
  }

  public static PFUserDO convert(final LdapPerson person)
  {
    final PFUserDO user = new PFUserDO();
    user.setLastname(person.getSurname());
    user.setFirstname(person.getGivenName());
    user.setUsername(person.getUid());
    user.setId(getId(person));
    user.setOrganization(person.getOrganization());
    user.setDescription(person.getDescription());
    final String[] mails = person.getMail();
    if (mails != null) {
      for (final String mail : mails) {
        if (StringUtils.isNotEmpty(mail) == true) {
          user.setEmail(mail);
          break;
        }
      }
    }
    if (person.isDeleted() == true) {
      user.setDeleted(true);
    }
    if (person.isDeactivated() == true || LdapUserDao.isDeactivated(person) == true) {
      user.setDeactivated(true);
    }
    if (person.isRestrictedUser() == true || LdapUserDao.isRestrictedUser(person) == true) {
      user.setRestrictedUser(true);
    }
    return user;
  }

  public static LdapPerson convert(final PFUserDO user)
  {
    final LdapPerson person = new LdapPerson();
    person.setSurname(user.getLastname());
    person.setGivenName(user.getFirstname());
    person.setUid(user.getUsername());
    if (user.getId() != null) {
      person.setEmployeeNumber(buildEmployeeNumber(user));
    }
    person.setOrganization(user.getOrganization());
    person.setDescription(user.getDescription());
    person.setMail(user.getEmail());
    person.setDeleted(user.isDeleted());
    person.setDeactivated(user.isDeactivated());
    if (user.isDeactivated() == true) {
      person.setMail(LdapUserDao.DEACTIVATED_MAIL);
    }
    person.setRestrictedUser(user.isRestrictedUser());
    return person;
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
  public static boolean copyUserFields(final LdapPerson src, final LdapPerson dest)
  {
    setMailNullArray(src);
    setMailNullArray(dest);
    final boolean modified = BeanHelper.copyProperties(src, dest, true, "commonName", "uid", "givenName", "surname", "mail", "description",
        "organization", "deactivated", "restrictedUser");
    return modified;
  }

  static void setMailNullArray(final LdapPerson person)
  {
    if (person.getMail() == null) {
      return;
    }
    for (final String mail : person.getMail()) {
      if (mail != null) {
        return;
      }
    }
    // All array entries are null, therefore set the mail value itself to null:
    person.setMail((String[]) null);
  }
}
