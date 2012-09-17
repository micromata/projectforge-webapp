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

public class LdapUserDaoTest extends LdapRealTestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapUserDaoTest.class);

  private static final String PATH = "ou=projectforge-test";

  LdapUserDao ldapUserDao;

  LdapOrganizationalUnitDao ldapOrganizationalUnitDao;

  @Override
  @Before
  public void setup()
  {
    super.setup();
    ldapUserDao = new LdapUserDao();
    ldapOrganizationalUnitDao = new LdapOrganizationalUnitDao();
    ldapUserDao.setLdapConnector(ldapConnector);
    ldapOrganizationalUnitDao.setLdapConnector(ldapConnector);
    if (ldapConfig != null) {
      ldapOrganizationalUnitDao.createIfNotExist(PATH, "Test area for tests of ProjectForge.");
      ldapOrganizationalUnitDao.createIfNotExist("deactivated", "for deactivated objects.", PATH);
    }
  }

  @After
  public void tearDown()
  {
    if (ldapConfig != null) {
      ldapOrganizationalUnitDao.deleteIfExists("deactivated", PATH);
      ldapOrganizationalUnitDao.deleteIfExists(PATH);
    }
  }

  @Test
  public void createAuthenticateAndDeleteUser()
  {
    if (ldapConfig == null) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    final String uid = "test-user-42";
    final LdapPerson user = new LdapPerson().setUid(uid).setGivenName("Kai").setSurname("ProjectForge Test").setDescription("description")
        .setHomePhoneNumber("0123").setMail("kr@acme.com").setMobilePhoneNumber("4567").setOrganization("ProjectForge")
        .setTelephoneNumber("890").setEmployeeNumber("42");
    user.setOrganizationalUnit(PATH);
    ldapUserDao.createOrUpdate(PATH, user);
    final LdapPerson user2 = ldapUserDao.findByUsername(uid, PATH);
    Assert.assertNotNull(user2);
    LdapTestUtils.assertUser(user2, user.getUid(), user.getGivenName(), user.getSurname(), user.getMail(), user.getOrganization(),
        user.getDescription());
    Assert.assertEquals(LdapUtils.getOu(PATH), LdapUtils.getOu(user2.getOrganizationalUnit()));

    Assert.assertFalse(ldapUserDao.authenticate(uid, "", PATH));
    // Change password
    ldapUserDao.changePassword(user, null, "hurzel");
    Assert.assertEquals(PATH, ldapUserDao.findByUsername(uid, PATH).getOrganizationalUnit());
    Assert.assertTrue(ldapUserDao.authenticate(uid, "hurzel", PATH));

    // Delete user
    ldapUserDao.delete(user);
    Assert.assertNull(ldapUserDao.findByUsername(uid, PATH));
  }

  @Test
  public void activateAndDeactivateUser()
  {
    if (ldapConfig == null) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    final String uid = "test-user-43";
    final LdapPerson user = new LdapPerson().setUid(uid).setGivenName("Kai").setSurname("ProjectForge Test").setEmployeeNumber("43");
    user.setOrganizationalUnit(PATH);
    ldapUserDao.createOrUpdate(PATH, user);
    ldapUserDao.changePassword(user, null, "hurzel");
    Assert.assertTrue(ldapUserDao.authenticate(uid, "hurzel", PATH));
    ldapUserDao.deactivateUser(user);
    Assert.assertFalse(ldapUserDao.authenticate(uid, "hurzel", PATH));
    final LdapPerson user2 = ldapUserDao.findByUsername(uid, PATH);
    Assert.assertNotNull(user2);
    Assert.assertEquals(LdapUtils.getOu(LdapUserDao.DEACTIVATED_SUB_CONTEXT, PATH), LdapUtils.getOu(user2.getOrganizationalUnit()));

    // Reactivate user:
    ldapUserDao.reactivateUser(user2);
    Assert.assertFalse(ldapUserDao.authenticate(uid, "hurzel", PATH));
    // Delete user
    ldapUserDao.delete(user2);
    Assert.assertNull(ldapUserDao.findByUsername(uid, PATH));
  }

  @Test
  public void updateUser()
  {
    if (ldapConfig == null) {
      log.info("No LDAP server configured for tests. Skipping test.");
      return;
    }
    final String uid = "test-user-44";
    final PFUserDO user = new PFUserDO().setUsername(uid).setLastname("Reinhard").setFirstname("Kai").setOrganization("Micromata GmbH")
        .setEmail("k.reinhard@acme.com").setDeactivated(true);
    user.setId(44);
    final LdapPerson ldapUser = PFUserDOConverter.convert(user);
    ldapUser.setOrganizationalUnit(PATH);
    ldapUserDao.createOrUpdate(PATH, ldapUser);
    LdapPerson ldapUser2 = ldapUserDao.findByUsername(uid, PATH);
    Assert.assertNotNull(ldapUser2);
    Assert.assertEquals(LdapUtils.getOu(LdapUserDao.DEACTIVATED_SUB_CONTEXT, PATH), LdapUtils.getOu(ldapUser2.getOrganizationalUnit()));
    Assert.assertTrue(ldapUser.isDeactivated());
    ldapUser2.setDeactivated(false);
    ldapUserDao.update(PATH, ldapUser2);
    ldapUser2 = ldapUserDao.findByUsername(uid, PATH);
    Assert.assertEquals(LdapUtils.getOu(PATH), LdapUtils.getOu(ldapUser2.getOrganizationalUnit()));
    // Delete user
    ldapUserDao.delete(ldapUser2);
    Assert.assertNull(ldapUserDao.findByUsername(uid, PATH));
  }
}
