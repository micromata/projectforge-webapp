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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.projectforge.user.PFUserDO;

public class LdapUserDaoTest
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapUserDaoTest.class);

  private LdapUserDao ldapUserDao;

  private LdapOrganizationalUnitDao ldapOrganizationalUnitDao;

  private LdapRealTestHelper ldapRealTestHelper;

  private String getPath()
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
      ldapOrganizationalUnitDao.createIfNotExist(getPath(), "Test area for tests of ProjectForge.");
      ldapOrganizationalUnitDao.createIfNotExist(LdapUserDao.DEACTIVATED_SUB_CONTEXT, "for deactivated users.", getPath());
      ldapOrganizationalUnitDao.createIfNotExist(LdapUserDao.RESTRICTED_USER_SUB_CONTEXT, "for restricted users.", getPath());
    }
  }

  @After
  public void tearDown()
  {
    if (ldapRealTestHelper.isAvailable() == true) {
      ldapOrganizationalUnitDao.deleteIfExists(LdapUserDao.DEACTIVATED_SUB_CONTEXT, getPath());
      ldapOrganizationalUnitDao.deleteIfExists(LdapUserDao.RESTRICTED_USER_SUB_CONTEXT, getPath());
      ldapOrganizationalUnitDao.deleteIfExists(getPath());
    }
  }

  @Test
  public void createAuthenticateAndDeleteUser()
  {
    if (ldapRealTestHelper.isAvailable() == false) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    final String uid = "test-user-42";
    final LdapPerson user = new LdapPerson().setUid(uid).setGivenName("Kai").setSurname("ProjectForge Test").setDescription("description")
        .setHomePhoneNumber("0123").setMail("kr@acme.com").setMobilePhoneNumber("4567").setOrganization("ProjectForge")
        .setTelephoneNumber("890").setEmployeeNumber(PFUserDOConverter.ID_PREFIX + "42");
    user.setOrganizationalUnit(getPath());
    ldapUserDao.createOrUpdate(getPath(), user);
    final LdapPerson user2 = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertNotNull(user2);
    LdapTestUtils.assertUser(user2, user.getUid(), user.getGivenName(), user.getSurname(), user.getMail(), user.getOrganization(),
        user.getDescription());
    Assert.assertEquals(LdapUtils.getOu(getPath()), LdapUtils.getOu(user2.getOrganizationalUnit()));

    Assert.assertNull(ldapUserDao.authenticate(uid, "", getPath()));
    // Change password
    ldapUserDao.changePassword(user, null, "hurzel");
    Assert.assertEquals(getPath(), ldapUserDao.findByUsername(uid, getPath()).getOrganizationalUnit());
    final LdapPerson ldapUser = ldapUserDao.authenticate(uid, "hurzel", getPath());
    Assert.assertNotNull(ldapUser);
    Assert.assertEquals(user.getUid(), ldapUser.getUid());

    // Delete user
    ldapUserDao.delete(user);
    Assert.assertNull(ldapUserDao.findByUsername(uid, getPath()));
  }

  @Test
  public void activateAndDeactivateUser()
  {
    if (ldapRealTestHelper.isAvailable() == false) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    final String uid = "test-user-43";
    final LdapPerson user = new LdapPerson().setUid(uid).setGivenName("Kai").setSurname("ProjectForge Test").setEmployeeNumber(PFUserDOConverter.ID_PREFIX + "43");
    user.setOrganizationalUnit(getPath());
    ldapUserDao.createOrUpdate(getPath(), user);
    ldapUserDao.changePassword(user, null, "hurzel");
    final LdapPerson ldapUser = ldapUserDao.authenticate(uid, "hurzel", getPath());
    Assert.assertNotNull(ldapUser);
    ldapUserDao.deactivateUser(user);
    Assert.assertNull(ldapUserDao.authenticate(uid, "hurzel", getPath()));
    final LdapPerson user2 = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertNotNull(user2);
    Assert.assertEquals(LdapUtils.getOu(LdapUserDao.DEACTIVATED_SUB_CONTEXT, getPath()), LdapUtils.getOu(user2.getOrganizationalUnit()));

    // Reactivate user:
    ldapUserDao.reactivateUser(user2);
    Assert.assertNull(ldapUserDao.authenticate(uid, "hurzel", getPath()));
    // Delete user
    ldapUserDao.delete(user2);
    Assert.assertNull(ldapUserDao.findByUsername(uid, getPath()));
  }

  @Test
  public void updateUser()
  {
    if (ldapRealTestHelper.isAvailable() == false) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    final String uid = "test-user-44";
    final PFUserDO user = new PFUserDO().setUsername(uid).setLastname("Reinhard").setFirstname("Kai").setOrganization("Micromata GmbH")
        .setEmail("k.reinhard@acme.com").setDeactivated(true);
    user.setId(44);
    // Test creation of deactivated user:
    final LdapPerson ldapUser = PFUserDOConverter.convert(user);
    ldapUser.setOrganizationalUnit(getPath());
    ldapUserDao.createOrUpdate(getPath(), ldapUser);
    LdapPerson ldapUser2 = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertNotNull(ldapUser2);
    Assert
    .assertEquals(LdapUtils.getOu(LdapUserDao.DEACTIVATED_SUB_CONTEXT, getPath()), LdapUtils.getOu(ldapUser2.getOrganizationalUnit()));
    Assert.assertTrue(ldapUser.isDeactivated());
    // Test update from deactivated to activated:
    ldapUser2.setDeactivated(false);
    ldapUserDao.update(getPath(), ldapUser2);
    ldapUser2 = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertEquals(LdapUtils.getOu(getPath()), LdapUtils.getOu(ldapUser2.getOrganizationalUnit()));
    // Test update from activated to deactivated:
    ldapUser2.setDeactivated(true);
    ldapUserDao.update(getPath(), ldapUser2);
    ldapUser2 = ldapUserDao.findByUsername(uid, getPath());
    Assert
    .assertEquals(LdapUtils.getOu(LdapUserDao.DEACTIVATED_SUB_CONTEXT, getPath()), LdapUtils.getOu(ldapUser2.getOrganizationalUnit()));
    Assert.assertTrue(ldapUser.isDeactivated());
    // Delete user
    ldapUserDao.delete(ldapUser2);
    Assert.assertNull(ldapUserDao.findByUsername(uid, getPath()));
  }

  @Test
  public void restrictedUsers()
  {
    if (ldapRealTestHelper.isAvailable() == false) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    final String uid = "test-user-45";
    final PFUserDO user = new PFUserDO().setUsername(uid).setLastname("Reinhard").setFirstname("Kai").setOrganization("Micromata GmbH")
        .setEmail("k.reinhard@acme.com").setRestrictedUser(true);
    user.setId(45);
    // Test creation of restricted users:
    final LdapPerson initialLdapUser = PFUserDOConverter.convert(user);
    initialLdapUser.setOrganizationalUnit(getPath());
    ldapUserDao.createOrUpdate(getPath(), initialLdapUser);
    LdapPerson ldapUser = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertNotNull(ldapUser);
    Assert.assertEquals(LdapUtils.getOu(LdapUserDao.RESTRICTED_USER_SUB_CONTEXT, getPath()),
        LdapUtils.getOu(ldapUser.getOrganizationalUnit()));
    Assert.assertTrue(ldapUser.isRestrictedUser());
    // Test update from restricted user to normal user:
    ldapUser.setRestrictedUser(false);
    ldapUserDao.update(getPath(), ldapUser);
    ldapUser = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertEquals(LdapUtils.getOu(getPath()), LdapUtils.getOu(ldapUser.getOrganizationalUnit()));
    // Test update from normal user to restricted user:
    ldapUser.setRestrictedUser(true);
    ldapUserDao.update(getPath(), ldapUser);
    ldapUser = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertEquals(LdapUtils.getOu(LdapUserDao.RESTRICTED_USER_SUB_CONTEXT, getPath()),
        LdapUtils.getOu(ldapUser.getOrganizationalUnit()));
    Assert.assertTrue(ldapUser.isRestrictedUser());

    // Test deactivated users (restricted context should be ignored):
    ldapUser.setDeactivated(true);
    ldapUserDao.update(getPath(), ldapUser);
    ldapUser = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertEquals(LdapUtils.getOu(LdapUserDao.DEACTIVATED_SUB_CONTEXT, getPath()), LdapUtils.getOu(ldapUser.getOrganizationalUnit()));
    Assert.assertTrue(ldapUser.isDeactivated());
    Assert.assertFalse(ldapUser.isRestrictedUser());

    // Delete user
    ldapUserDao.delete(ldapUser);
    Assert.assertNull(ldapUserDao.findByUsername(uid, getPath()));

    // Create restricted and deactivated user. Restriction should be ignored:
    ldapUser = PFUserDOConverter.convert(user);
    ldapUser.setDeactivated(true);
    ldapUser.setOrganizationalUnit(getPath());
    ldapUserDao.createOrUpdate(getPath(), ldapUser);
    ldapUser = ldapUserDao.findByUsername(uid, getPath());
    Assert.assertEquals(LdapUtils.getOu(LdapUserDao.DEACTIVATED_SUB_CONTEXT, getPath()), LdapUtils.getOu(ldapUser.getOrganizationalUnit()));
    Assert.assertTrue(ldapUser.isDeactivated());
    Assert.assertFalse(ldapUser.isRestrictedUser());

    ldapUser.setDeactivated(false).setRestrictedUser(true);
    ldapUserDao.createOrUpdate(getPath(), ldapUser);
    Assert.assertEquals(LdapUtils.getOu(LdapUserDao.RESTRICTED_USER_SUB_CONTEXT, getPath()),
        LdapUtils.getOu(ldapUser.getOrganizationalUnit()));
    Assert.assertTrue(ldapUser.isRestrictedUser());

    // Delete user
    ldapUserDao.delete(ldapUser);
    Assert.assertNull(ldapUserDao.findByUsername(uid, getPath()));
  }
}
