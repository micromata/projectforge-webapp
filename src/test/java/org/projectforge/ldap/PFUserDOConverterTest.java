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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.ConfigXmlTest;
import org.projectforge.user.PFUserDO;

public class PFUserDOConverterTest
{
  @BeforeClass
  public static void setup()
  {
    final ConfigXml xml = ConfigXmlTest.createTestConfiguration();
    final LdapConfig ldapConfig = new LdapConfig();
    xml.setLdapConfig(ldapConfig);
    final LdapPosixAccountsConfig posixAccountsConfig = new LdapPosixAccountsConfig();
    ldapConfig.setPosixAccountsConfig(posixAccountsConfig);
  }

  @Test
  public void convert()
  {
    PFUserDO user = new PFUserDO().setUsername("k.reinhard").setFirstname("Kai").setLastname("Reinhard")
        .setEmail("k.reinhard@micromata.de").setDescription("Developer").setOrganization("Micromata GmbH");
    user.setId(42);
    LdapUser ldapUser = PFUserDOConverter.convert(user);
    assertEquals("k.reinhard", ldapUser.getUid());
    assertEquals("k.reinhard", ldapUser.getId());
    assertEquals(PFUserDOConverter.ID_PREFIX + "42", ldapUser.getEmployeeNumber());
    assertEquals("Kai Reinhard", ldapUser.getCommonName());
    assertEquals("Developer", ldapUser.getDescription());
    assertEquals("Kai", ldapUser.getGivenName());
    assertEquals("Reinhard", ldapUser.getSurname());
    assertEquals("Micromata GmbH", ldapUser.getOrganization());
    assertEquals(1, ldapUser.getMail().length);
    assertEquals("k.reinhard@micromata.de", ldapUser.getMail()[0]);

    user = PFUserDOConverter.convert(ldapUser);
    assertEquals("k.reinhard", user.getUsername());
    assertEquals(new Integer(42), user.getId());
    assertEquals("Developer", user.getDescription());
    assertEquals("Kai", user.getFirstname());
    assertEquals("Reinhard", user.getLastname());
    assertEquals("Micromata GmbH", user.getOrganization());
    assertEquals("k.reinhard@micromata.de", user.getEmail());

    user = new PFUserDO();
    ldapUser = PFUserDOConverter.convert(user);
    assertNull(ldapUser.getId());
    assertNull(ldapUser.getUid());
    assertNull(ldapUser.getEmployeeNumber());
  }

  @Test
  public void copy()
  {
    final PFUserDO src = createUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    PFUserDO dest = createUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    Assert.assertFalse(PFUserDOConverter.copyUserFields(src, dest));
    assertUser(src, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    assertUser(dest, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    dest = new PFUserDO();
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, dest));
    assertUser(src, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    assertUser(dest, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        createUser("", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        createUser("kai", "", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, createUser("kai", "Kai", "", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, createUser("kai", "Kai", "Reinhard", "", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, createUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, createUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "")));
  }

  private PFUserDO createUser(final String username, final String firstname, final String lastname, final String email,
      final String organization, final String description)
  {
    return new PFUserDO().setUsername(username).setFirstname(firstname).setLastname(lastname).setEmail(email).setOrganization(organization)
        .setDescription(description);
  }

  private void assertUser(final PFUserDO user, final String username, final String firstname, final String lastname, final String email,
      final String organization, final String description)
  {
    Assert.assertEquals(username, user.getUsername());
    Assert.assertEquals(firstname, user.getFirstname());
    Assert.assertEquals(lastname, user.getLastname());
    Assert.assertEquals(email, user.getEmail());
    Assert.assertEquals(organization, user.getOrganization());
    Assert.assertEquals(description, user.getDescription());
  }

  @Test
  public void copyLdapUser()
  {
    final LdapUser src = LdapTestUtils.createLdapUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    LdapUser dest = LdapTestUtils.createLdapUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    Assert.assertFalse(PFUserDOConverter.copyUserFields(src, dest));
    LdapTestUtils.assertUser(src, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    LdapTestUtils.assertUser(dest, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    dest = new LdapUser();
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, dest));
    LdapTestUtils.assertUser(src, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    LdapTestUtils.assertUser(dest, null, "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        LdapTestUtils.createLdapUser("kai", "", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        LdapTestUtils.createLdapUser("kai", "", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        LdapTestUtils.createLdapUser("kai", "Kai", "", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        LdapTestUtils.createLdapUser("kai", "Kai", "Reinhard", "", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        LdapTestUtils.createLdapUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        LdapTestUtils.createLdapUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "")));
  }

  @Test
  public void setNullMailArray()
  {
    final LdapUser ldapUser = new LdapUser();
    PFUserDOConverter.setMailNullArray(ldapUser);
    Assert.assertNull(ldapUser.getMail());
    ldapUser.setMail(new String[1]);
    PFUserDOConverter.setMailNullArray(ldapUser);
    Assert.assertNull(ldapUser.getMail());
    ldapUser.setMail(new String[2]);
    ldapUser.getMail()[1] = "Hurzel";
    Assert.assertEquals(ldapUser.getMail()[1], "Hurzel");
  }

  @Test
  public void testLdapValues()
  {
    PFUserDO user = new PFUserDO().setLdapValues("");
    user.setUsername("kai");
    LdapUser ldapUser = PFUserDOConverter.convert(user);
    LdapTestUtils.assertPosixAccountValues(ldapUser, null, null, null, null);
    user.setLdapValues("<values uidNumber=\"65535\" />");
    ldapUser = PFUserDOConverter.convert(user);
    LdapTestUtils.assertPosixAccountValues(ldapUser, 65535, -1, "/home/kai", "/bin/bash");
    ldapUser.setUidNumber(42).setGidNumber(1000).setHomeDirectory("/home/user").setLoginShell("/bin/ksh");
    user = PFUserDOConverter.convert(ldapUser);
    ldapUser = PFUserDOConverter.convert(user);
    LdapTestUtils.assertPosixAccountValues(ldapUser, 42, 1000, "/home/user", "/bin/ksh");
    Assert.assertEquals("<values uidNumber=\"42\" gidNumber=\"1000\" homeDirectory=\"/home/user\" loginShell=\"/bin/ksh\"/>",
        user.getLdapValues());
  }
}
