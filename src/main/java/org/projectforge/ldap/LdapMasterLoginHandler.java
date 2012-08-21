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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.projectforge.user.GroupDO;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

/**
 * This LDAP login handler has read-write access to the LDAP server and acts as master of the user and group data. All changes of
 * ProjectForge's users and groups will be written through. Any change of the LDAP server will be ignored and may be overwritten by
 * ProjectForge. <br/>
 * Use this login handler if you want to configure your LDAP users and LDAP groups via ProjectForge.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LdapMasterLoginHandler extends LdapLoginHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapMasterLoginHandler.class);

  /**
   * @see org.projectforge.user.LoginHandler#checkLogin(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public LoginResult checkLogin(final String username, final String password)
  {
    final LoginResult loginResult = loginDefaultHandler.checkLogin(username, password);
    if (loginResult.getLoginResultStatus() != LoginResultStatus.SUCCESS) {
      return loginResult;
    }
    // User is now logged-in successfully.
    final String organizationalUnits = ldapConfig.getUserBase();
    final boolean authenticated = ldapUserDao.authenticate(username, password, organizationalUnits);
    if (authenticated == false) {
      log.info("User's credentials in LDAP not up-to-date: " + username + ". Updating LDAP entry...");
      final PFUserDO user = loginResult.getUser();
      final LdapPerson ldapUser = PFUserDOConverter.convert(user);
      ldapUserDao.createOrUpdate(ldapUser);
      ldapUserDao.changePassword(ldapUser, null, user.getPassword());
    }
    return loginResult;
  }

  /**
   * @see org.projectforge.user.LoginHandler#getAllGroups()
   */
  @Override
  public List<GroupDO> getAllGroups()
  {
    final List<GroupDO> groups = loginDefaultHandler.getAllGroups();
    return groups;
  }

  /**
   * @see org.projectforge.user.LoginHandler#getAllUsers()
   */
  @Override
  public List<PFUserDO> getAllUsers()
  {
    final List<PFUserDO> users = loginDefaultHandler.getAllUsers();
    final List<LdapPerson> ldapUsers = getAllLdapUsers();
    for (final PFUserDO user : users) {
      final LdapPerson updatedLdapUser = PFUserDOConverter.convert(user);
      final LdapPerson ldapUser = getLdapUser(ldapUsers, user);
      if (ldapUser == null) {
        ldapUserDao.create(updatedLdapUser);
      } else {
        final boolean modified = PFUserDOConverter.copyUserFields(updatedLdapUser, ldapUser);
        if (modified == true) {
          ldapUserDao.createOrUpdate(updatedLdapUser);
        }
      }
    }
    return users;
  }

  private LdapPerson getLdapUser(final List<LdapPerson> ldapUsers, final PFUserDO user)
  {
    for (final LdapPerson ldapUser : ldapUsers) {
      if (StringUtils.equals(ldapUser.getUid(), user.getUsername()) == true
          || StringUtils.equals(ldapUser.getEmployeeNumber(), PFUserDOConverter.buildEmployeeNumber(user)) == true) {
        return ldapUser;
      }
    }
    return null;
  }
}
