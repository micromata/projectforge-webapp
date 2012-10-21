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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.projectforge.test.TestBase;
import org.projectforge.user.LoginHandler;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

public class LdapSlaveLoginHandlerTest extends TestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapSlaveLoginHandlerTest.class);

  private LdapUserDao ldapUserDao;

  private LdapOrganizationalUnitDao ldapOrganizationalUnitDao;

  private LdapRealTestHelper ldapRealTestHelper;

  private String getUserPath()
  {
    return ldapRealTestHelper.getUserPath();
  }

  @Before
  public void setup()
  {
    ldapRealTestHelper = new LdapRealTestHelper();
    ldapUserDao = new LdapUserDao();
    ldapOrganizationalUnitDao = new LdapOrganizationalUnitDao();
    ldapUserDao.setLdapConnector(ldapRealTestHelper.ldapConnector);
    ldapOrganizationalUnitDao.setLdapConnector(ldapRealTestHelper.ldapConnector);
    if (ldapRealTestHelper.isAvailable() == true) {
      ldapOrganizationalUnitDao.createIfNotExist(getUserPath(), "Test area for tests of ProjectForge.");
    }
  }

  @After
  public void tearDown()
  {
    if (ldapRealTestHelper.isAvailable() == true) {
      ldapOrganizationalUnitDao.deleteIfExists(getUserPath());
    }
  }

  @Test
  public void testMockedSimpleMode()
  {
    final String userBase = "ou=pf-mock-test-users";
    final String testUsername = "mockedLdapSlaveTestuser";
    ldapUserDao = mock(LdapUserDao.class);
    when(ldapUserDao.authenticate(Mockito.eq(testUsername), Mockito.eq("successful"), Mockito.eq(userBase))).thenReturn(
        new LdapPerson().setUid(testUsername));
    when(ldapUserDao.authenticate(Mockito.anyString(), Mockito.eq("fail"), Mockito.eq(userBase))).thenReturn(null);
    final LdapSlaveLoginHandler loginHandler = new LdapSlaveLoginHandler();
    loginHandler.ldapConfig = new LdapConfig().setUserBase(userBase);
    loginHandler.userDao = userDao;
    loginHandler.ldapUserDao = ldapUserDao;
    loginHandler.ldapOrganizationalUnitDao = mock(LdapOrganizationalUnitDao.class);
    loginHandler.initialize();
    loginHandler.setMode(LdapSlaveLoginHandler.Mode.SIMPLE);
    testSimpleMode(loginHandler, testUsername);
  }

  @Test
  public void testSimpleMode()
  {
    if (ldapRealTestHelper.isAvailable() == false) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    final LdapSlaveLoginHandler loginHandler = new LdapSlaveLoginHandler();
    loginHandler.ldapConfig = ldapRealTestHelper.ldapConfig;
    loginHandler.userDao = userDao;
    loginHandler.ldapUserDao = ldapUserDao;
    loginHandler.ldapOrganizationalUnitDao = ldapOrganizationalUnitDao;
    loginHandler.initialize();
    loginHandler.setMode(LdapSlaveLoginHandler.Mode.SIMPLE);
    final String testUsername = "ldapSlaveTestuser";
    final LdapPerson ldapUser = new LdapPerson().setUid(testUsername).setGivenName("Kai").setSurname("Reinhard").setEmployeeNumber("42");
    final String userBase = ldapRealTestHelper.ldapConfig.getUserBase();
    ldapUser.setOrganizationalUnit(userBase);
    ldapUserDao.create(userBase, ldapUser);
    ldapUserDao.changePassword(ldapUser, null, "successful");
    testSimpleMode(loginHandler, testUsername);
    ldapUserDao.delete(ldapUser);
  }

  private void testSimpleMode(final LoginHandler loginHandler, final String testUsername)
  {
    logon(TEST_ADMIN_USER);
    Assert.assertNull("If failed, a previous test run didn't cleared the data-base.", userDao.getUserGroupCache().getUser(testUsername));

    // Check failed login:
    LoginResult result = loginHandler.checkLogin(testUsername, "fail");
    Assert.assertEquals("User login failed against LDAP therefore login should be failed.", LoginResultStatus.FAILED,
        result.getLoginResultStatus());

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
    Assert.assertEquals("User is a local user, thus the LDAP authentication should be ignored.", LoginResultStatus.FAILED,
        result.getLoginResultStatus());

    user.setPassword(userDao.encryptPassword("test"));
    userDao.internalUpdate(user);
    result = loginHandler.checkLogin(testUsername, "test");
    Assert.assertEquals("User is a local user, thus authentication should be done by the login default handler.",
        LoginResultStatus.SUCCESS, result.getLoginResultStatus());
    user = result.getUser();
    Assert.assertEquals(testUsername, user.getUsername());
  }

  @Test
  public void loginInMockedSlaveMode()
  {
    final String userBase = "ou=pf-mock-test-users";
    final LdapUserDao ldapUserDao = mock(LdapUserDao.class);
    LoginResult loginResult;
    final LdapPerson kai = new LdapPerson().setUid("kai").setDescription("Developer").setGivenName("Kai").setMail("k.reinhard@acme.com")
        .setOrganization("Micromata").setSurname("Reinhard");
    when(ldapUserDao.authenticate("kai", "successful", userBase)).thenReturn(kai);
    when(ldapUserDao.authenticate("kai", "fail", userBase)).thenReturn(null);
    when(ldapUserDao.findByUsername("kai", userBase)).thenReturn(kai);
    final LdapSlaveLoginHandler loginHandler = new LdapSlaveLoginHandler();
    loginHandler.ldapUserDao = ldapUserDao;
    loginHandler.ldapConfig = new LdapConfig().setUserBase(userBase);
    loginHandler.userDao = userDao;
    Assert.assertEquals(LoginResultStatus.FAILED, loginHandler.checkLogin("kai", "fail").getLoginResultStatus());

    Assert.assertFalse("User shouldn't be available yet in the data-base.",
        userDao.doesUsernameAlreadyExist(new PFUserDO().setUsername("kai")));
    loginResult = loginHandler.checkLogin("kai", "successful");
    Assert.assertEquals(LoginResultStatus.SUCCESS, loginResult.getLoginResultStatus());
    LdapTestUtils.assertUser(loginResult.getUser(), "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    Assert.assertTrue("User should be created in data-base as a new user (in ldap).",
        userDao.doesUsernameAlreadyExist(new PFUserDO().setUsername("kai")));
    final PFUserDO user = userDao.getInternalByName("kai");
    LdapTestUtils.assertUser(user, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    Assert.assertEquals(userDao.encryptPassword("successful"), user.getPassword());

    userDao.internalMarkAsDeleted(user);
    Assert.assertEquals("User is deleted in data-base. Login not possible.", LoginResultStatus.LOGIN_EXPIRED,
        loginHandler.checkLogin("kai", "successful").getLoginResultStatus());
  }
}
