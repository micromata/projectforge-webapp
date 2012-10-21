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

import org.projectforge.user.GroupDO;
import org.projectforge.user.LoginDefaultHandler;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

/**
 * This LDAP login handler acts as a simple LDAP slave, meaning, that LDAP will be accessed in read-only mode.
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
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LdapSimpleSlaveLoginHandler extends LdapLoginHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapSimpleSlaveLoginHandler.class);

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
    final boolean authenticated = ldapUserDao.authenticate(username, password, organizationalUnits);
    if (authenticated == false) {
      log.info("User login failed: " + username);
      return loginResult.setLoginResultStatus(LoginResultStatus.FAILED);
    }
    log.info("LDAP authentication was successful for: " + username);
    if (user == null) {
      log.info("LDAP user '" + username + "' doesn't yet exist in ProjectForge's data base. Creating new user...");
      user = new PFUserDO();
      user.setId(null); // Force new id.
      user.setUsername(username).setNoPassword();
      userDao.internalSave(user);
    }
    loginResult.setUser(user);
    return loginResult.setLoginResultStatus(LoginResultStatus.SUCCESS).setUser(user);
  }

  /**
   * Uses standard implementation of {@link LoginDefaultHandler}.
   * @see org.projectforge.user.LoginHandler#getAllGroups()
   * @see LoginDefaultHandler#getAllGroups()
   */
  @Override
  public List<GroupDO> getAllGroups()
  {
    return loginDefaultHandler.getAllGroups();
  }

  /**
   * Uses standard implementation of {@link LoginDefaultHandler}.
   * @see org.projectforge.user.LoginHandler#getAllUsers()
   * @see LoginDefaultHandler#getAllUsers()
   */
  @Override
  public List<PFUserDO> getAllUsers()
  {
    return loginDefaultHandler.getAllUsers();
  }

  /**
   * Does nothing.
   * @see org.projectforge.user.LoginHandler#afterUserGroupCacheRefresh(java.util.List, java.util.List)
   */
  @Override
  public void afterUserGroupCacheRefresh(final List<PFUserDO> users, final List<GroupDO> groups)
  {
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
}
