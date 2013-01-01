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

package org.projectforge.ldap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.projectforge.test.TestBase;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.Login;
import org.projectforge.user.LoginResult;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;
import org.springframework.util.CollectionUtils;

// Create
// ~/ProjectForge/testldapConfig.xml
//<?xml version="1.0" encoding="UTF-8" ?>
//<ldapConfig>
//    <server>ldaps://192.168.76.177</server>
//    <port>636</port>
//    <userBase>ou=pf-test-users</userBase>
//    <groupBase>ou=pf-test-groups</groupBase>
//    <baseDN>dc=acme,dc=priv</baseDN>
//    <authentication>simple</authentication>
//    <managerUser>cn=manager</managerUser>
//    <managerPassword>test</managerPassword>
//    <sslCertificateFile>/Users/kai/ProjectForge/testldap.cert</sslCertificateFile>
//</ldapConfig>

public class LdapMasterLoginHandlerTest extends TestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapMasterLoginHandlerTest.class);

  private GroupDao groupDao;

  private LdapGroupDao ldapGroupDao;

  private LdapUserDao ldapUserDao;

  private LdapRealTestHelper ldapRealTestHelper;

  private String getPath()
  {
    return ldapRealTestHelper.getUserPath();
  }

  @Before
  public void setup()
  {
    ldapRealTestHelper = new LdapRealTestHelper().setup();
    ldapUserDao = ldapRealTestHelper.ldapUserDao;
    ldapGroupDao = ldapRealTestHelper.ldapGroupDao;
  }

  @After
  public void tearDown()
  {
    ldapRealTestHelper.tearDown();
  }

  @Test
  public void loginAndCreateLdapUser()
  {
    final String userBase = "ou=pf-mock-test-users";
    final LdapUserDao ldapUserDao = mock(LdapUserDao.class);
    final LdapMasterLoginHandler loginHandler = new LdapMasterLoginHandler();
    loginHandler.ldapConfig = new LdapConfig().setUserBase(userBase);
    loginHandler.userDao = userDao;
    loginHandler.ldapUserDao = ldapUserDao;
    loginHandler.ldapOrganizationalUnitDao = mock(LdapOrganizationalUnitDao.class);
    loginHandler.initialize();
    Login.getInstance().setLoginHandler(loginHandler);
    logon(TEST_ADMIN_USER);
    userDao.internalSave(new PFUserDO().setUsername("kai").setPassword(userDao.encryptPassword("successful")).setFirstname("Kai")
        .setLastname("Reinhard"));
    Assert.assertEquals(LoginResultStatus.SUCCESS, loginHandler.checkLogin("kai", "successful").getLoginResultStatus());

    final ArgumentCaptor<LdapUser> argumentCaptor = ArgumentCaptor.forClass(LdapUser.class);
    verify(ldapUserDao).createOrUpdate(Mockito.anyString(), argumentCaptor.capture());
    final LdapUser createdLdapUser = argumentCaptor.getValue();
    Assert.assertEquals("kai", createdLdapUser.getUid());
    Assert.assertEquals("Kai", createdLdapUser.getGivenName());
    Assert.assertEquals("Reinhard", createdLdapUser.getSurname());
    // Assert.assertEquals("successful", createdLdapUser.get());
  }

  @Test
  public void realTest()
  {
    if (ldapRealTestHelper.isAvailable() == false) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    logon(TEST_ADMIN_USER);
    final LdapMasterLoginHandler loginHandler = createLoginHandler();
    // Create users and group.
    final Integer userId1 = createUser("ldapMaster1", "test123", "firstname1", "lastname1");
    final Integer userId2 = createUser("ldapMaster2", "test123", "firstname2", "lastname2");
    final Integer userId3 = createUser("ldapMaster3", "test123", "firstname3", "lastname3");
    final Integer userId4 = createUser("ldapMaster4", "test123", "firstname4", "lastname4");
    final Integer groupId1 = createGroup("ldapMasterGroup1", "This is a stupid description.");
    GroupDO group = groupDao.internalGetById(groupId1);
    synchronizeLdapUsers(loginHandler);
    LdapGroup ldapGroup = ldapGroupDao.findById(groupId1);
    Assert.assertTrue(isMembersEmpty(ldapGroup));

    // Assign users to group
    group.setAssignedUsers(new HashSet<PFUserDO>());
    group.addUser(userDao.getUserGroupCache().getUser(userId1));
    group.addUser(userDao.getUserGroupCache().getUser(userId2));
    group.addUser(userDao.getUserGroupCache().getUser(userId3));
    groupDao.internalUpdate(group);
    synchronizeLdapUsers(loginHandler);
    ldapGroup = ldapGroupDao.findById(groupId1);
    assertMembers(ldapGroup, "ldapMaster1", "ldapMaster2", "ldapMaster3");
    Assert.assertFalse(isMembersEmpty(ldapGroup));
    LdapUser ldapUser = ldapUserDao.findById(userId1, getPath());
    Assert.assertEquals("ldapMaster1", ldapUser.getUid());

    // Renaming one user, deleting one user and assigning third user
    userDao.internalMarkAsDeleted(userDao.getById(userId2));
    PFUserDO user3 = userDao.getById(userId3);
    user3.setUsername("ldapMasterRenamed3");
    userDao.internalUpdate(user3);
    group = userDao.getUserGroupCache().getGroup(groupId1);
    group.addUser(userDao.getById(userId4));
    groupDao.internalUpdate(group);
    synchronizeLdapUsers(loginHandler);
    ldapGroup = ldapGroupDao.findById(groupId1);
    assertMembers(ldapGroup, "ldapMaster1", "ldapMasterRenamed3", "ldapMaster4");

    // Renaming one user and mark him as restricted
    user3 = userDao.getById(userId3);
    user3.setUsername("ldapMaster3");
    user3.setRestrictedUser(true);
    userDao.internalUpdate(user3);
    synchronizeLdapUsers(loginHandler);
    ldapUser = ldapUserDao.findById(userId3, getPath());
    Assert.assertEquals("ldapMaster3", ldapUser.getUid());
    Assert.assertTrue( ldapUser.getOrganizationalUnit().contains("ou=restricted"));
    ldapGroup = ldapGroupDao.findById(groupId1);
    assertMembers(ldapGroup, "ldapMaster1", "ldapMaster3,ou=restricted", "ldapMaster4");

    // Renaming group
    group = groupDao.getById(groupId1);
    group.setName("ldapMasterGroupRenamed1");
    groupDao.internalUpdate(group);
    synchronizeLdapUsers(loginHandler);
    ldapGroup = ldapGroupDao.findById(groupId1);
    assertMembers(ldapGroup, "ldapMaster1", "ldapMaster3,ou=restricted", "ldapMaster4");
    Assert.assertEquals("ldapMasterGroupRenamed1", ldapGroup.getCommonName());

    // Change password
    final PFUserDO user1 = userDao.getById(userId1);
    final LoginResult loginResult = Login.getInstance().checkLogin(user1.getUsername(), "test123");
    Assert.assertEquals(LoginResultStatus.SUCCESS, loginResult.getLoginResultStatus());
    Assert.assertNotNull(ldapUserDao.authenticate(user1.getUsername(), "test123"));
    Login.getInstance().passwordChanged(user1, "newpassword");
    Assert.assertNotNull(ldapUserDao.authenticate(user1.getUsername(), "newpassword"));

    // Delete all groups
    final Collection<GroupDO> groups = userDao.getUserGroupCache().getAllGroups();
    for (final GroupDO g : groups) {
      groupDao.internalMarkAsDeleted(g);
    }
    synchronizeLdapUsers(loginHandler);
    Assert.assertEquals("LDAP groups must be empty (all groups are deleted in the PF data-base).", 0,
        ldapGroupDao.findAll(ldapRealTestHelper.ldapConfig.getGroupBase()).size());
    final Collection<PFUserDO> users = userDao.getUserGroupCache().getAllUsers();
    for (final PFUserDO user : users) {
      userDao.internalMarkAsDeleted(user);
    }
    synchronizeLdapUsers(loginHandler);
    Assert.assertEquals("LDAP users must be empty (all user are deleted in the PF data-base).", 0,
        ldapUserDao.findAll(ldapRealTestHelper.ldapConfig.getGroupBase()).size());
    ldapUser = ldapUserDao.findById(userId1, getPath());
    Assert.assertNull(ldapUser);
  }

  private boolean isMembersEmpty(final LdapGroup ldapGroup)
  {
    final Set<String> members = ldapGroup.getMembers();
    if (CollectionUtils.isEmpty(members) == true) {
      return true;
    }
    if (members.size() > 1) {
      return false;
    }
    final String member = members.iterator().next();
    return member == null || member.startsWith("cn=none") == true;
  }

  private void assertMembers(final LdapGroup ldapGroup, final String... usernames)
  {
    final Set<String> members = ldapGroup.getMembers();
    Assert.assertFalse(CollectionUtils.isEmpty(members));
    Assert.assertEquals(usernames.length, members.size());
    final LdapConfig ldapConfig = ldapRealTestHelper.ldapConfig;
    for (final String username : usernames) {
      final String user = "uid=" + username + "," + ldapConfig.getUserBase() + "," + ldapConfig.getBaseDN();
      Assert.assertTrue(members.contains(user));
    }
  }

  private Integer createUser(final String username, final String password, final String firstname, final String lastname)
  {
    final PFUserDO user = new PFUserDO().setUsername(username).setPassword(userDao.encryptPassword(password)).setFirstname(firstname)
        .setLastname(lastname);
    return (Integer) userDao.internalSave(user);
  }

  private Integer createGroup(final String name, final String description)
  {
    final GroupDO group = new GroupDO().setName(name).setDescription(description);
    return (Integer) groupDao.internalSave(group);
  }

  private LdapMasterLoginHandler createLoginHandler()
  {
    final LdapMasterLoginHandler loginHandler = new LdapMasterLoginHandler();
    loginHandler.ldapConfig = ldapRealTestHelper.ldapConfig;
    loginHandler.userDao = userDao;
    loginHandler.ldapUserDao = ldapUserDao;
    loginHandler.ldapOrganizationalUnitDao = ldapRealTestHelper.ldapOrganizationalUnitDao;
    loginHandler.initialize();
    Login.getInstance().setLoginHandler(loginHandler);
    return loginHandler;
  }

  private void synchronizeLdapUsers(final LdapMasterLoginHandler loginHandler)
  {
    userDao.getUserGroupCache().forceReload(); // Synchronize ldap users.
    while (true) {
      try {
        Thread.sleep(200);
      } catch (final InterruptedException ex) {
      }
      if (userDao.getUserGroupCache().isRefreshInProgress() == false && loginHandler.isRefreshInProgress() == false) {
        break;
      }
    }
  }

  /**
   * @param groupDao the groupDao to set
   * @return this for chaining.
   */
  public void setGroupDao(final GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }
}
