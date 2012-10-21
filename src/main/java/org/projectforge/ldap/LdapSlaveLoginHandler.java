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

import java.util.ArrayList;
import java.util.List;

import org.projectforge.user.GroupDO;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

/**
 * This LDAP login handler acts as a LDAP slave, meaning, that LDAP will be accessed in read-only mode.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LdapSlaveLoginHandler extends LdapLoginHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapSlaveLoginHandler.class);

  /**
   * @see org.projectforge.user.LoginHandler#checkLogin(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public LoginResult checkLogin(final String username, final String password)
  {
    // TODO: Groups
    final LoginResult loginResult = new LoginResult();
    final String organizationalUnits = ldapConfig.getUserBase();
    final LdapPerson ldapUser = ldapUserDao.authenticate(username, password, organizationalUnits);
    if (ldapUser == null) {
      log.info("User login failed: " + username);
      return loginResult.setLoginResultStatus(LoginResultStatus.FAILED);
    }
    PFUserDO user = userDao.getInternalByName(username);
    if (user == null) {
      log.info("LDAP user '" + username + "' doesn't yet exist in ProjectForge's data base. Creating new user...");
      user = PFUserDOConverter.convert(ldapUser);
      user.setId(null); // Force new id.
      user.setPassword(userDao.encryptPassword(password));
      userDao.internalSave(user);
    } else {
      PFUserDOConverter.copyUserFields(PFUserDOConverter.convert(ldapUser), user);
      userDao.internalUpdate(user);
      if (user.hasSystemAccess() == false) {
        log.info("User has no system access (is deleted/deactivated): " + user.getDisplayUsername());
        return loginResult.setLoginResultStatus(LoginResultStatus.LOGIN_EXPIRED);
      }
    }
    loginResult.setUser(user);
    return loginResult.setLoginResultStatus(LoginResultStatus.SUCCESS).setUser(user);
  }

  /**
   * Currently return all ProjectForge groups (done by loginDefaultHandler).
   * Planned: Updates also any (in LDAP) modified group in ProjectForge's data-base.
   * @see org.projectforge.user.LoginHandler#getAllGroups()
   */
  @Override
  public List<GroupDO> getAllGroups()
  {
    return loginDefaultHandler.getAllGroups();
    // final List<LdapGroup> ldapGroups = getAllLdapGroups();
    // final List<GroupDO> groups = new ArrayList<GroupDO>(ldapGroups.size());
    // for (final LdapGroup ldapGroup : ldapGroups) {
    // groups.add(GroupDOConverter.convert(ldapGroup));
    // }
    // return groups;
  }

  /**
   * Updates also any (in LDAP) modified user in ProjectForge's data-base. New users will be created and ProjectForge users which are not
   * available in ProjectForge's data-base will be created.
   * @see org.projectforge.user.LoginHandler#getAllUsers()
   */
  @Override
  public List<PFUserDO> getAllUsers()
  {
    final List<LdapPerson> ldapUsers = getAllLdapUsers();
    final List<PFUserDO> users = new ArrayList<PFUserDO>(ldapUsers.size());
    for (final LdapPerson ldapUser : ldapUsers) {
      users.add(PFUserDOConverter.convert(ldapUser));
    }
    //final List<PFUserDO> dbUsers = userDao.internalLoadAll();
    // TODO: synchronize
    return users;
  }

  /**
   * @see org.projectforge.user.LoginHandler#isPasswordChangeSupported(org.projectforge.user.PFUserDO)
   * @return true for local users only, false for ldap users.
   */
  @Override
  public boolean isPasswordChangeSupported(final PFUserDO user)
  {
    return user.isLocalUser();
  }

  /**
   * Does nothing.
   * @see org.projectforge.user.LoginHandler#afterUserGroupCacheRefresh(java.util.List, java.util.List)
   */
  @Override
  public void afterUserGroupCacheRefresh(final List<PFUserDO> users, final List<GroupDO> groups)
  {
  }
}
