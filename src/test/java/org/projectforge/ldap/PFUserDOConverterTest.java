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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;
import org.projectforge.user.PFUserDO;

public class PFUserDOConverterTest
{
  @Test
  public void convert()
  {
    PFUserDO user = new PFUserDO().setUsername("k.reinhard").setFirstname("Kai").setLastname("Reinhard")
        .setEmail("k.reinhard@micromata.de").setDescription("Developer").setOrganization("Micromata GmbH");
    user.setId(42);
    LdapPerson person = PFUserDOConverter.convert(user);
    assertEquals("k.reinhard", person.getUid());
    assertEquals("k.reinhard", person.getId());
    assertEquals(PFUserDOConverter.ID_PREFIX + "42", person.getEmployeeNumber());
    assertEquals("Kai Reinhard", person.getCommonName());
    assertEquals("Developer", person.getDescription());
    assertEquals("Kai", person.getGivenName());
    assertEquals("Reinhard", person.getSurname());
    assertEquals("Micromata GmbH", person.getOrganization());
    assertEquals(1, person.getMail().length);
    assertEquals("k.reinhard@micromata.de", person.getMail()[0]);

    user = PFUserDOConverter.convert(person);
    assertEquals("k.reinhard", user.getUsername());
    assertEquals(new Integer(42), user.getId());
    assertEquals("Developer", user.getDescription());
    assertEquals("Kai", user.getFirstname());
    assertEquals("Reinhard", user.getLastname());
    assertEquals("Micromata GmbH", user.getOrganization());
    assertEquals("k.reinhard@micromata.de", user.getEmail());

    user = new PFUserDO();
    person = PFUserDOConverter.convert(user);
    assertNull(person.getId());
    assertNull(person.getUid());
    assertNull(person.getEmployeeNumber());
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
  public void copyLdapPerson()
  {
    final LdapPerson src = createLdapUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    LdapPerson dest = createLdapUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    Assert.assertFalse(PFUserDOConverter.copyUserFields(src, dest));
    assertUser(src, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    assertUser(dest, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    dest = new LdapPerson();
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, dest));
    assertUser(src, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    assertUser(dest, "kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer");
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        createLdapUser("", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src,
        createLdapUser("kai", "", "Reinhard", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, createLdapUser("kai", "Kai", "", "k.reinhard@acme.com", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, createLdapUser("kai", "Kai", "Reinhard", "", "Micromata", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, createLdapUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "", "Developer")));
    Assert.assertTrue(PFUserDOConverter.copyUserFields(src, createLdapUser("kai", "Kai", "Reinhard", "k.reinhard@acme.com", "Micromata", "")));
  }

  private LdapPerson createLdapUser(final String username, final String firstname, final String lastname, final String email,
      final String organization, final String description)
  {
    return new LdapPerson().setUid(username).setGivenName(firstname).setSurname(lastname).setMail(email).setOrganization(organization)
        .setDescription(description);
  }

  private void assertUser(final LdapPerson user, final String username, final String firstname, final String lastname, final String email,
      final String organization, final String description)
  {
    Assert.assertEquals(username, user.getUid());
    Assert.assertEquals(firstname, user.getGivenName());
    Assert.assertEquals(lastname, user.getSurname());
    final String mail = user.getMail() != null && user.getMail().length > 0 ? user.getMail()[0] : null;
    Assert.assertEquals(email, mail);
    Assert.assertEquals(organization, user.getOrganization());
    Assert.assertEquals(description, user.getDescription());
  }

}
