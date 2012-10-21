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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.projectforge.test.TestBase;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

public class LdapSimpleSlaveLoginHandlerTest extends TestBase
{
  @Test
  public void loginAndCreateLdapUser()
  {
    final String userBase = "ou=pf-mock-test-users";
    final String testUsername = "slaveLdap";
    final LdapUserDao ldapUserDao = mock(LdapUserDao.class);
    when(ldapUserDao.authenticate(Mockito.anyString(), Mockito.eq("successful"), Mockito.eq(userBase))).thenReturn(true);
    when(ldapUserDao.authenticate(Mockito.anyString(), Mockito.eq("fail"), Mockito.eq(userBase))).thenReturn(false);
    final LdapSimpleSlaveLoginHandler loginHandler = new LdapSimpleSlaveLoginHandler();
    loginHandler.ldapConfig = new LdapConfig().setUserBase(userBase);
    loginHandler.userDao = userDao;
    loginHandler.ldapUserDao = ldapUserDao;
    loginHandler.ldapOrganizationalUnitDao = mock(LdapOrganizationalUnitDao.class);
    loginHandler.initialize();
    logon(TEST_ADMIN_USER);
    Assert.assertNull("If failed, a previous test run didn't cleared the data-base.", userDao.getUserGroupCache().getUser(testUsername));

    // Check failed login:
    LoginResult result = loginHandler.checkLogin(testUsername, "fail");
    Assert.assertEquals("User login failed against LDAP therefore login should be failed.", LoginResultStatus.FAILED, result.getLoginResultStatus());

    // Check successful login for new ProjectForge users:
    result = loginHandler.checkLogin(testUsername, "successful");
    Assert.assertEquals(LoginResultStatus.SUCCESS, result.getLoginResultStatus());
    Assert.assertNotNull("User should be returned.", result.getUser());
    PFUserDO user = userDao.getInternalByName(testUsername);
    Assert.assertNotNull("User should be created by login handler.", user);
    Assert.assertEquals(testUsername, user.getUsername());
    Assert.assertEquals(result.getUser().getId(), user.getId());

    // Check successful login for existing ProjectForge users:
    result = loginHandler.checkLogin(testUsername, "successful");
    Assert.assertEquals(LoginResultStatus.SUCCESS, result.getLoginResultStatus());
    Assert.assertNotNull("User should be returned.", result.getUser());
    user = userDao.getInternalByName(testUsername);
    Assert.assertNotNull("User should be created by login handler.", user);
    Assert.assertEquals(testUsername, user.getUsername());
    Assert.assertEquals(result.getUser().getId(), user.getId());

    // Check that LDAP is ignored for local users:
    user.setLocalUser(true);
    userDao.internalUpdate(user);
    result = loginHandler.checkLogin(testUsername, "successful");
    Assert.assertEquals("User is a local user, thus the LDAP authentication should be ignored.", LoginResultStatus.FAILED, result.getLoginResultStatus());

    user.setPassword(userDao.encryptPassword("test"));
    userDao.internalUpdate(user);
    result = loginHandler.checkLogin(testUsername, "test");
    Assert.assertEquals("User is a local user, thus authentication should be done by the login default handler.", LoginResultStatus.SUCCESS, result.getLoginResultStatus());
    user = result.getUser();
    Assert.assertEquals(testUsername, user.getUsername());
  }

  // @Test
  // public void loginAndUpdateLdapUser()
  // {
  // }
}
