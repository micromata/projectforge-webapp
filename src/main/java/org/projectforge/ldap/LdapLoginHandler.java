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

import org.projectforge.access.AccessChecker;
import org.projectforge.user.LoginHandler;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;

public class LdapLoginHandler implements LoginHandler
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapLoginHandler.class);

  private UserDao userDao;

  private AccessChecker accessChecker;

  private final LdapConnector ldapConnector;

  private final LdapUserDao ldapUserDao;

  private final LdapConfig ldapConfig;

  public LdapLoginHandler(final LdapConfig ldapConfig)
  {
    this.ldapConfig = ldapConfig;
    ldapConnector = new LdapConnector(ldapConfig);
    ldapUserDao = new LdapUserDao();
    ldapUserDao.ldapConnector = ldapConnector;

  }

  /**
   * @see org.projectforge.user.LoginHandler#checkLogin(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public LoginResult checkLogin(final String username, final String password)
  {
    final LoginResult loginResult = new LoginResult();
    final boolean authenticated = ldapUserDao.authenticate(username, password, ldapConfig.getGroupBase());
    if (authenticated != false) {
      log.info("User login failed: " + username);
      return loginResult.setLoginResultStatus(LoginResultStatus.FAILED);
    }
    final PFUserDO user = userDao.getInternalByName(username);
    if (user == null) {
      log.error("User login failed, can't found user '" + username + "' in ProjectForge's data base.");
      return loginResult.setLoginResultStatus(LoginResultStatus.FAILED);
    }
    if (user.isDeleted() == true) {
      log.info("User has no system access (is deleted): " + user.getDisplayUsername());
      return loginResult.setLoginResultStatus(LoginResultStatus.LOGIN_EXPIRED);
    } else {
      return loginResult.setLoginResultStatus(LoginResultStatus.SUCCESS).setUser(user);
    }
  }

  public boolean isAdminUser(final PFUserDO user)
  {
    return accessChecker.isUserMemberOfAdminGroup(user);
  }

  /**
   * @param accessChecker the accessChecker to set
   * @return this for chaining.
   */
  public void setAccessChecker(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  /**
   * @param userDao the userDao to set
   * @return this for chaining.
   */
  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

}
