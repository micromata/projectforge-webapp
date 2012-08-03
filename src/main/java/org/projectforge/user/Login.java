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

package org.projectforge.user;

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

  public LoginResult checkLogin(final String username, final String password)
  {
    if (loginHandler == null) {
      log.warn("No login possible because no login handler is defined yet.");
      return new LoginResult().setLoginResultStatus(LoginResultStatus.FAILED);
    }
    return loginHandler.checkLogin(username, password);
  }

  public boolean isAdminUser(final PFUserDO user) {
    if (loginHandler == null) {
      log.warn("No login handler is defined yet, so can't check either user is admin user or not.");
      return false;
    }
    return loginHandler.isAdminUser(user);
  }

  /**
   * @param loginHandler the loginHandler to set
   */
  public void setLoginHandler(final LoginHandler loginHandler)
  {
    this.loginHandler = loginHandler;
    log.info("LoginHandler " + loginHandler.getClass().getName() + " registerd.");
  }
}
