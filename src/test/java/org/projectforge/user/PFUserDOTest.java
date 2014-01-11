/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

import junit.framework.Assert;

import org.junit.Test;

public class PFUserDOTest
{
  @Test
  public void testCreateUserWithoutSecretFields()
  {
    PFUserDO user = new PFUserDO();
    Assert.assertFalse(user.hasSecretFieldValues());
    user.setPassword("test");
    Assert.assertTrue(user.hasSecretFieldValues());
    user.setPassword(null).setStayLoggedInKey("test");
    Assert.assertTrue(user.hasSecretFieldValues());
    user.setAuthenticationToken("test").setStayLoggedInKey(null);
    Assert.assertTrue(user.hasSecretFieldValues());
    user.setAuthenticationToken(null).setPasswordSalt("test");
    Assert.assertTrue(user.hasSecretFieldValues());
    user.setPasswordSalt(null);
    Assert.assertFalse(user.hasSecretFieldValues());
    user.setPassword("pw").setPasswordSalt("ps").setAuthenticationToken("at").setStayLoggedInKey("st");
    Assert.assertEquals("pw", user.getPassword());
    Assert.assertEquals("ps", user.getPasswordSalt());
    Assert.assertEquals("at", user.getAuthenticationToken());
    Assert.assertEquals("st", user.getStayLoggedInKey());
    user = PFUserDO.createCopyWithoutSecretFields(user);
    Assert.assertNull(user.getPassword());
    Assert.assertNull(user.getPasswordSalt());
    Assert.assertNull(user.getAuthenticationToken());
    Assert.assertNull(user.getStayLoggedInKey());
  }
}
