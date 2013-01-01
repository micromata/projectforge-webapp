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

package org.projectforge.user;

import java.util.ArrayList;
import java.util.List;

import org.projectforge.web.UserFilter;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class Login
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Login.class);

  private static final Login instance = new Login();

  public static Login getInstance()
  {
    return instance;
  }

  private LoginHandler loginHandler;

  /**
   * @see LoginHandler#checkLogin(String, String)
   */
  public LoginResult checkLogin(final String username, final String password)
  {
    if (loginHandler == null) {
      log.warn("No login possible because no login handler is defined yet.");
      return new LoginResult().setLoginResultStatus(LoginResultStatus.FAILED);
    }
    if (username == null || password == null) {
      return new LoginResult().setLoginResultStatus(LoginResultStatus.FAILED);
    }
    return loginHandler.checkLogin(username, password);
  }

  /**
   * @see LoginHandler#isAdminUser(PFUserDO)
   */
  public boolean isAdminUser(final PFUserDO user)
  {
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, so can't check either user is admin user or not.");
      return false;
    }
    if (user == null) {
      return false;
    }
    return loginHandler.isAdminUser(user);
  }

  /**
   * @see LoginHandler#checkStayLoggedIn(PFUserDO)
   */
  public boolean checkStayLoggedIn(final PFUserDO user)
  {
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, so can't accept the stay-logged-in request.");
      return false;
    }
    if (user == null) {
      return false;
    }
    return loginHandler.checkStayLoggedIn(user);
  }

  public void passwordChanged(final PFUserDO user, final String newPassword)
  {
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, so can't handle password-changed request.");
      return;
    }
    if (user == null) {
      return;
    }
    loginHandler.passwordChanged(user, newPassword);
  }

  public boolean isPasswordChangeSupported(final PFUserDO user)
  {
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, so can't check support of password-change functionality.");
      return false;
    }
    if (user == null) {
      return false;
    }
    return loginHandler.isPasswordChangeSupported(user);
  }

  /**
   * @see LoginHandler#getAllUsers()
   */
  public List<PFUserDO> getAllUsers()
  {
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, so can't get all users.");
      return new ArrayList<PFUserDO>();
    }
    return loginHandler.getAllUsers();
  }

  /**
   * @see LoginHandler#getAllGroups()
   */
  public List<GroupDO> getAllGroups()
  {
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, so can't get all groups.");
      return new ArrayList<GroupDO>();
    }
    return loginHandler.getAllGroups();
  }

  public void afterUserGroupCacheRefresh(final List<PFUserDO> users, final List<GroupDO> groups)
  {
    if (UserFilter.isUpdateRequiredFirst() == true) {
      // Don't run e. g. LDAP synchronization because user and groups may not be available!
      return;
    }
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, so can't get all groups.");
      return;
    }
    loginHandler.afterUserGroupCacheRefresh(users, groups);
  }

  /**
   * @param loginHandler the loginHandler to set
   */
  public void setLoginHandler(final LoginHandler loginHandler)
  {
    this.loginHandler = loginHandler;
    log.info("LoginHandler " + loginHandler.getClass().getName() + " registered.");
  }

  public boolean hasExternalUsermanagementSystem()
  {
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, assuming that no external user management system is supported.");
      return false;
    }
    return loginHandler.hasExternalUsermanagementSystem();
  }
}
