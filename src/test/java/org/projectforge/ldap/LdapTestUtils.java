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

import org.junit.Assert;
import org.projectforge.user.PFUserDO;

public class LdapTestUtils
{
  public static void assertUser(final PFUserDO user, final String username, final String firstname, final String lastname, final String email,
      final String organization, final String description)
  {
    Assert.assertEquals(username, user.getUsername());
    Assert.assertEquals(firstname, user.getFirstname());
    Assert.assertEquals(lastname, user.getLastname());
    Assert.assertEquals(organization, user.getOrganization());
    Assert.assertEquals(email, user.getEmail());
    Assert.assertEquals(description, user.getDescription());
  }

  public static void assertUser(final LdapPerson user, final String uid, final String givenName, final String surname, final String mail[],
      final String organization, final String description)
  {
    Assert.assertEquals(uid, user.getUid());
    Assert.assertEquals(givenName, user.getGivenName());
    Assert.assertEquals(surname, user.getSurname());
    Assert.assertEquals(organization, user.getOrganization());
    Assert.assertArrayEquals(mail, user.getMail());
    Assert.assertEquals(description, user.getDescription());
  }
}
