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

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.projectforge.core.ConfigXml;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapSambaAccountsUtils
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapSambaAccountsUtils.class);

  /**
   * Get all given uid numbers of all ProjectForge users including any deleted user and get the next highest and free number. The number is
   * 1000 if no uid number (with an value greater than 999) is found.
   */
  public static int getNextFreeSambaSIDNumber()
  {
    final UserGroupCache userGroupCache = Registry.instance().getUserGroupCache();
    final Collection<PFUserDO> allUsers = userGroupCache.getAllUsers();
    int currentMaxNumber = 999;
    for (final PFUserDO user : allUsers) {
      final LdapUserValues ldapUserValues = PFUserDOConverter.readLdapUserValues(user.getLdapValues());
      if (ldapUserValues == null) {
        continue;
      }
      if (ldapUserValues.getSambaSIDNumber() != null && ldapUserValues.getSambaSIDNumber().intValue() > currentMaxNumber) {
        currentMaxNumber = ldapUserValues.getUidNumber();
      }
    }
    return currentMaxNumber + 1;
  }

  /**
   * For preventing double uidNumbers.
   * @param user
   * @param sambaSIDNumber
   * @return Returns true if any user (also deleted user) other than the given user has the given uidNumber, otherwise false.
   */
  public static boolean isGivenNumberFree(final PFUserDO currentUser, final Integer sambaSIDNumber)
  {
    if (sambaSIDNumber == null) {
      // Nothing to check.
      return true;
    }
    final UserGroupCache userGroupCache = Registry.instance().getUserGroupCache();
    final Collection<PFUserDO> allUsers = userGroupCache.getAllUsers();
    for (final PFUserDO user : allUsers) {
      final LdapUserValues ldapUserValues = PFUserDOConverter.readLdapUserValues(user.getLdapValues());
      if (ObjectUtils.equals(user.getId(), currentUser.getId()) == true) {
        // The current user may have the given sambaSIDNumber already, so ignore this entry.
        continue;
      }
      if (ldapUserValues != null
          && ldapUserValues.getSambaSIDNumber() != null
          && ldapUserValues.getSambaSIDNumber().intValue() == sambaSIDNumber) {
        // Number isn't free.
        log.info("The getSambaSIDNumber (samba account) '" + sambaSIDNumber + "' is already occupied by user: " + user);
        return false;
      }
    }
    return true;
  }

  /**
   * Sets next free SambaSID or, if free and given the same id as the posix UID.
   * @param ldapUserValues
   * @param user
   */
  public static void setDefaultValues(final LdapUserValues ldapUserValues, final PFUserDO user)
  {
    final LdapConfig ldapConfig = ConfigXml.getInstance().getLdapConfig();
    LdapSambaAccountsConfig ldapSambaAccountsConfig = ldapConfig != null ? ldapConfig.getSambaAccountsConfig() : null;
    if (ldapSambaAccountsConfig == null) {
      ldapSambaAccountsConfig = new LdapSambaAccountsConfig();
    }
    if (ldapUserValues.getUidNumber() != null && isGivenNumberFree(user, ldapUserValues.getUidNumber()) == true) {
      ldapUserValues.setSambaSIDNumber(ldapUserValues.getUidNumber());
    } else {
      ldapUserValues.setSambaSIDNumber(getNextFreeSambaSIDNumber());
    }
    if (ldapSambaAccountsConfig.getDefaultSambaPrimaryGroupSID() != null) {
      ldapUserValues.setSambaPrimaryGroupSIDNumber(ldapSambaAccountsConfig.getDefaultSambaPrimaryGroupSID());
    }
  }
}
