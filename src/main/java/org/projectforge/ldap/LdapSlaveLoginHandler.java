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
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.projectforge.user.GroupDO;
import org.projectforge.user.LoginDefaultHandler;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

/**
 * This LDAP login handler acts as a LDAP slave, meaning, that LDAP will be accessed in read-only mode. There are 3 modes available: simple,
 * users and user-groups. <h4>Simple mode</h4>
 * <ul>
 * <li>Simple means that only username and password is checked, all other user settings such as assigned groups and user name etc. are
 * managed by ProjectForge.</li>
 * <li>
 * No ldap user is needed for accessing users or groups of LDAP, only the user's login-name and password is checked by trying to
 * authenticate!</li>
 * <li>If a user is deactivated in LDAP the user has the possibility to work with ProjectForge unlimited as long as he uses his
 * stay-logged-in-method! (If not acceptable please use the {@link LdapSlaveLoginHandler} instead.)</li>
 * <li>For local users any LDAP setting is ignored.</li>
 * </ul>
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LdapSlaveLoginHandler extends LdapLoginHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapSlaveLoginHandler.class);

  enum Mode
  {
    SIMPLE, USERS, USER_GROUPS
  };

  private Mode mode;

  /**
   * Only for test cases.
   * @param mode
   */
  void setMode(final Mode mode)
  {
    this.mode = mode;
  }

  /**
   * @see org.projectforge.ldap.LdapLoginHandler#initialize()
   */
  @Override
  public void initialize()
  {
    super.initialize();
    if (StringUtils.isBlank(ldapConfig.getManagerUser()) == true) {
      mode = Mode.SIMPLE;
    } else if (StringUtils.isNotBlank(ldapConfig.getGroupBase()) == true) {
      mode = Mode.USERS;// Mode.USER_GROUPS;
      log.warn("Groups aren't yet supported by this LDAP handler.");
    } else {
      mode = Mode.USERS;
    }
    switch (mode) {
      case SIMPLE:
        log.info("LDAP slave login handler works in mode 'simple'.");
        break;
      case USERS:
        log.info("LDAP slave login handler works in mode 'users'.");
        break;
      case USER_GROUPS:
        log.info("LDAP slave login handler works in mode 'user_groups'.");
        break;
    }
  }

  /**
   * Uses the standard implementation {@link LoginDefaultHandler#checkLogin(String, String)} for local users. For all other users a LDAP
   * authentication is checked. If the LDAP authentication fails then {@link LoginResultStatus#FAILED} is returned. If successful then
   * {@link LoginResultStatus#SUCCESS} is returned with the user settings of ProjectForge database. If the user doesn't yet exist in
   * ProjectForge's data-base, it will be created after and then returned.
   * @see org.projectforge.user.LoginHandler#checkLogin(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public LoginResult checkLogin(final String username, final String password)
  {
    PFUserDO user = userDao.getInternalByName(username);
    if (user != null && user.isLocalUser() == true) {
      return loginDefaultHandler.checkLogin(username, password);
    }
    final LoginResult loginResult = new LoginResult();
    final String organizationalUnits = ldapConfig.getUserBase();
    final LdapPerson ldapUser = ldapUserDao.authenticate(username, password, organizationalUnits);
    if (ldapUser == null) {
      log.info("User login failed: " + username);
      return loginResult.setLoginResultStatus(LoginResultStatus.FAILED);
    }
    log.info("LDAP authentication was successful for: " + username);
    if (user == null) {
      log.info("LDAP user '" + username + "' doesn't yet exist in ProjectForge's data base. Creating new user...");
      user = PFUserDOConverter.convert(ldapUser);
      user.setId(null); // Force new id.
      if (mode == Mode.SIMPLE) {
        user.setNoPassword();
      } else {
        user.setPassword(userDao.encryptPassword(password));
      }
      userDao.internalSave(user);
    } else if (mode != Mode.SIMPLE) {
      PFUserDOConverter.copyUserFields(PFUserDOConverter.convert(ldapUser), user);
      userDao.internalUpdate(user);
      if (user.hasSystemAccess() == false) {
        log.info("User has no system access (is deleted/deactivated): " + user.getDisplayUsername());
        return loginResult.setLoginResultStatus(LoginResultStatus.LOGIN_EXPIRED);
      }
    }
    loginResult.setUser(user);
    if (mode == Mode.USER_GROUPS) {
      // TODO: Groups: Get groups of user.
    }
    return loginResult.setLoginResultStatus(LoginResultStatus.SUCCESS).setUser(user);
  }

  /**
   * Currently return all ProjectForge groups (done by loginDefaultHandler). Not yet implemented: Updates also any (in LDAP) modified group
   * in ProjectForge's data-base.
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
    if (mode == Mode.SIMPLE) {
      return loginDefaultHandler.getAllUsers();
    }
    final List<LdapPerson> ldapUsers = getAllLdapUsers();
    final List<PFUserDO> dbUsers = userDao.internalLoadAll();
    final List<PFUserDO> users = new ArrayList<PFUserDO>(ldapUsers.size());
    for (final LdapPerson ldapUser : ldapUsers) {
      final PFUserDO user = PFUserDOConverter.convert(ldapUser);
      users.add(user);
      final PFUserDO dbUser = getUser(dbUsers, user.getUsername());
      if (dbUser != null) {
        if (dbUser.isLocalUser() == true) {
          // Ignore local users.
          continue;
        }
        dbUser.copyValuesFrom(user);
        if (dbUser.isDeleted() == true) {
          userDao.internalUndelete(dbUser);
        }
        userDao.internalUpdate(dbUser);
      } else {
        // New user:
        userDao.internalSave(user);
      }
    }
    for (final PFUserDO dbUser : dbUsers) {
      if (dbUser.isLocalUser() == true) {
        // Ignore local users.
        continue;
      }
      final PFUserDO user = getUser(users, dbUser.getUsername());
      if (user == null) {
        // User isn't available in LDAP, therefore mark the db user as deleted.
        userDao.internalMarkAsDeleted(user);
      }
    }
    return users;
  }

  private PFUserDO getUser(final Collection<PFUserDO> col, final String username)
  {
    if (col == null || username == null) {
      return null;
    }
    for (final PFUserDO user : col) {
      if (username.equals(user.getUsername()) == true) {
        return user;
      }
    }
    return null;
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
