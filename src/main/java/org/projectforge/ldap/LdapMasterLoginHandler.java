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
 * This LDAP login handler has read-write access to the LDAP server and acts as master of the user and group data. All changes of
 * ProjectForge's users and groups will be written through. Any change of the LDAP server will be ignored and my be overwritten by
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
    // TODO: tbd.
    final LoginResult loginResult = new LoginResult();
    final String organizationalUnits = ldapConfig.getUserBase();
    final boolean authenticated = ldapUserDao.authenticate(username, password, organizationalUnits);
    if (authenticated == false) {
      log.info("User login failed: " + username);
      return loginResult.setLoginResultStatus(LoginResultStatus.FAILED);
    }
    PFUserDO user = userDao.getInternalByName(username);
    if (user == null) {
      log.info("LDAP user '" + username + "' doesn't yet exist in ProjectForge's data base. Creating new user...");
      final LdapPerson ldapUser = ldapUserDao.findByUsername(username, organizationalUnits);
      user = PFUserDOConverter.convert(ldapUser);
      user.setId(null); // Force new id.
      user.setPassword(userDao.encryptPassword(password));
      userDao.internalSave(user);
    }
    if (user.isDeleted() == true) {
      log.info("User has no system access (is deleted): " + user.getDisplayUsername());
      return loginResult.setLoginResultStatus(LoginResultStatus.LOGIN_EXPIRED);
    } else {
      return loginResult.setLoginResultStatus(LoginResultStatus.SUCCESS).setUser(user);
    }
  }

  /**
   * Updates also any modified group of ProjectForge's data-base in LDAP.
   * @see org.projectforge.user.LoginHandler#getAllGroups()
   */
  @Override
  public List<GroupDO> getAllGroups()
  {
    final String organizationalUnits = ldapConfig.getGroupBase();
    final List<LdapGroup> ldapGroups = ldapGroupDao.findAll(organizationalUnits);
    final List<GroupDO> groups = new ArrayList<GroupDO>(ldapGroups.size());
    for (final LdapGroup ldapGroup : ldapGroups) {
      groups.add(GroupDOConverter.convert(ldapGroup));
    }
    return groups;
  }

  /**
   * Updates also any modified user of ProjectForge's data-base in LDAP. New users will be created and ProjectForge users which are not
   * available in LDAP will be created.
   * @see org.projectforge.user.LoginHandler#getAllUsers()
   */
  @Override
  public List<PFUserDO> getAllUsers()
  {
    final String organizationalUnits = ldapConfig.getUserBase();
    final List<LdapPerson> ldapUsers = ldapUserDao.findAll(organizationalUnits);
    final List<PFUserDO> users = new ArrayList<PFUserDO>(ldapUsers.size());
    for (final LdapPerson ldapUser : ldapUsers) {
      users.add(PFUserDOConverter.convert(ldapUser));
    }
    final List<PFUserDO> dbUsers = userDao.internalLoadAll();
    // TODO: synchronize
    return users;
  }
}
