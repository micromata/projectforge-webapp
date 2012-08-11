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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LdapUtilsTest
{
  @Test
  public void getOu()
  {
    assertEquals("", LdapUtils.getOu());
    assertEquals("", LdapUtils.getOu(new String[0]));
    assertEquals("ou=users", LdapUtils.getOu("ou=users"));
    assertEquals("ou=users", LdapUtils.getOu("users"));
    assertEquals("ou=users,ou=pf", LdapUtils.getOu("ou=users,ou=pf"));
    assertEquals("ou=users,ou=pf", LdapUtils.getOu("users", "pf"));
  }

  @Test
  public void organizationalUnit()
  {
    assertNull(LdapUtils.getOrganizationalUnit(null));
    assertNull(LdapUtils.getOrganizationalUnit(null, null));
    assertNull(LdapUtils.getOrganizationalUnit(null, ""));
    assertNull(LdapUtils.getOrganizationalUnit("cn=hurzel"));
    assertNull(LdapUtils.getOrganizationalUnit("cn=hurzel", null));
    assertNull(LdapUtils.getOrganizationalUnit("cn=hurzel", ""));
    assertStringArray(LdapUtils.getOrganizationalUnit("cn=hurzel,ou=users", ""), "users");
    assertStringArray(LdapUtils.getOrganizationalUnit("cn=hurzel", "ou=users"), "users");
    assertStringArray(LdapUtils.getOrganizationalUnit("cn=hurzel,ou=intern,ou=users", ""), "intern", "users");
    assertStringArray(LdapUtils.getOrganizationalUnit("cn=hurzel,ou=intern", "ou=users"), "intern", "users");
    assertStringArray(LdapUtils.getOrganizationalUnit("cn=hurzel", "ou=intern,ou=users"), "intern", "users");
  }

  private void assertStringArray(final String[] array, final String... expected)
  {
    assertNotNull(array);
    assertEquals(expected.length, array.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], array[i]);
    }
  }
}
