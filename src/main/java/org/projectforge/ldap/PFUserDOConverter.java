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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.ListHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.user.PFUserDO;
import org.projectforge.xml.stream.XmlObjectReader;
import org.projectforge.xml.stream.XmlObjectWriter;

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
    if (isPosixAccountValuesEmpty(ldapUser) == false) {
      user.setLdapValues(getLdapValuesAsXml(ldapUser));
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
    setLdapValues(ldapUser, user.getLdapValues());
    return ldapUser;
  }

  public static boolean isPosixAccountValuesEmpty(final LdapUser ldapUser)
  {
    return ldapUser.getUidNumber() == null
        && StringUtils.isBlank(ldapUser.getHomeDirectory()) == true
        && StringUtils.isBlank(ldapUser.getLoginShell()) == true
        && ldapUser.getGidNumber() == null;
  }

  public static boolean isSambaAccountValuesEmpty(final LdapUser ldapUser)
  {
    return ldapUser.getSambaSIDNumber() == null && ldapUser.getSambaPrimaryGroupSIDNumber() == null;
  }

  /**
   * Sets the LDAP values such as posix account properties of the given ldapUser configured in the given xml string.
   * @param ldapUser
   * @param ldapValuesAsXml Posix account values as xml.
   */
  public static void setLdapValues(final LdapUser ldapUser, final String ldapValuesAsXml)
  {
    if (StringUtils.isBlank(ldapValuesAsXml) == true) {
      return;
    }
    final LdapConfig ldapConfig = ConfigXml.getInstance().getLdapConfig();
    final LdapPosixAccountsConfig posixAccountsConfig = ldapConfig != null ? ldapConfig.getPosixAccountsConfig() : null;
    if (posixAccountsConfig == null) {
      // No posix account default values configured
      return;
    }
    final LdapUserValues values = readLdapUserValues(ldapValuesAsXml);
    if (values == null) {
      return;
    }
    if (values.getUidNumber() != null) {
      ldapUser.setUidNumber(values.getUidNumber());
    } else {
      ldapUser.setUidNumber(-1);
    }
    if (values.getGidNumber() != null) {
      ldapUser.setGidNumber(values.getGidNumber());
    } else {
      ldapUser.setGidNumber(posixAccountsConfig.getDefaultGidNumber());
    }
    if (StringUtils.isNotBlank(values.getHomeDirectory()) == true) {
      ldapUser.setHomeDirectory(values.getHomeDirectory());
    } else {
      ldapUser.setHomeDirectory(posixAccountsConfig.getHomeDirectoryPrefix() + ldapUser.getUid());
    }
    if (StringUtils.isNotBlank(values.getLoginShell()) == true) {
      ldapUser.setLoginShell(values.getLoginShell());
    } else {
      ldapUser.setLoginShell(posixAccountsConfig.getDefaultLoginShell());
    }
    if (values.getSambaSIDNumber() != null) {
      ldapUser.setSambaSIDNumber(values.getSambaSIDNumber());
    } else {
      ldapUser.setSambaSIDNumber(null);
    }
    if (values.getSambaPrimaryGroupSIDNumber() != null) {
      ldapUser.setSambaPrimaryGroupSIDNumber(values.getSambaPrimaryGroupSIDNumber());
    } else {
      ldapUser.setSambaPrimaryGroupSIDNumber(null);
    }
  }

  public static LdapUserValues readLdapUserValues(final String ldapValuesAsXml)
  {
    if (StringUtils.isBlank(ldapValuesAsXml) == true) {
      return null;
    }
    final XmlObjectReader reader = new XmlObjectReader();
    reader.initialize(LdapUserValues.class);
    final LdapUserValues values = (LdapUserValues) reader.read(ldapValuesAsXml);
    return values;
  }

  /**
   * Exports the LDAP values such as posix account properties of the given ldapUser as xml string.
   * @param ldapUser
   */
  public static String getLdapValuesAsXml(final LdapUser ldapUser)
  {
    final LdapConfig ldapConfig = ConfigXml.getInstance().getLdapConfig();
    final LdapPosixAccountsConfig posixAccountsConfig = ldapConfig != null ? ldapConfig.getPosixAccountsConfig() : null;
    final LdapSambaAccountsConfig sambaAccountsConfig = ldapConfig != null ? ldapConfig.getSambaAccountsConfig() : null;
    LdapUserValues values = null;
    if (posixAccountsConfig != null) {
      values = new LdapUserValues();
      if (ldapUser.getUidNumber() != null) {
        values.setUidNumber(ldapUser.getUidNumber());
      }
      if (ldapUser.getGidNumber() != null) {
        values.setGidNumber(ldapUser.getGidNumber());
      }
      values.setHomeDirectory(ldapUser.getHomeDirectory());
      values.setLoginShell(ldapUser.getLoginShell());
    }
    if (sambaAccountsConfig != null) {
      if (values == null) {
        values = new LdapUserValues();
      }
      if (ldapUser.getSambaSIDNumber() != null) {
        values.setSambaSIDNumber(ldapUser.getSambaSIDNumber());
      }
      if (ldapUser.getSambaPrimaryGroupSIDNumber() != null) {
        values.setSambaPrimaryGroupSIDNumber(ldapUser.getSambaPrimaryGroupSIDNumber());
      }
    }
    return getLdapValuesAsXml(values);
  }

  /**
   * Exports the LDAP values such as posix account properties of the given ldapUser as xml string.
   * @param ldapUser
   */
  public static String getLdapValuesAsXml(final LdapUserValues values)
  {
    final XmlObjectReader reader = new XmlObjectReader();
    reader.initialize(LdapUserValues.class);
    final String xml = XmlObjectWriter.writeAsXml(values);
    return xml;
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
    boolean modified;
    final List<String> properties = new LinkedList<String>();
    ListHelper.addAll(properties, "commonName", "givenName", "surname", "mail", "description", "organization", "deactivated",
        "restrictedUser");
    if (LdapUserDao.isPosixAccountsConfigured() == true && isPosixAccountValuesEmpty(src) == false) {
      ListHelper.addAll(properties, "uidNumber", "gidNumber", "homeDirectory", "loginShell");
    }
    if (LdapUserDao.isSambaAccountsConfigured() == true && isSambaAccountValuesEmpty(src) == false) {
      ListHelper.addAll(properties, "sambaSIDNumber", "sambaPrimaryGroupSIDNumber",  "sambaNTPassword");
    }
    modified = BeanHelper.copyProperties(src, dest, true, properties.toArray(new String[0]));
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
