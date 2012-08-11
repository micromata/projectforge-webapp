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
import org.projectforge.test.TestBase;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

public class LdapLoginHandlerTest extends TestBase
{
  @Test
  public void login()
  {
    final LdapUserDao ldapUserDao = mock(LdapUserDao.class);
    when(ldapUserDao.authenticate("kai", "successful", (String[]) null)).thenReturn(true);
    when(ldapUserDao.authenticate("kai", "fail", (String[]) null)).thenReturn(false);
    when(ldapUserDao.findByUsername("kai")).thenReturn(
        new LdapPerson().setUid("kai").setDescription("Developer").setGivenName("Kai").setMail("k.reinhard@acme.com")
        .setOrganization("Micromata").setSurname("Reinhard"));
    final LdapLoginHandler loginHandler = new LdapLoginHandler();
    loginHandler.ldapUserDao = ldapUserDao;
    loginHandler.ldapConfig = new LdapConfig();
    Assert.assertEquals(LoginResultStatus.FAILED, loginHandler.checkLogin("kai", "fail").getLoginResultStatus());

    Assert.assertFalse("User shouldn't be available yet in the data-base.",
        userDao.doesUsernameAlreadyExist(new PFUserDO().setUsername("kai")));
    // Assert.assertEquals(LoginResultStatus.SUCCESS, loginHandler.checkLogin("kai", "successful").getLoginResultStatus());
    // Assert.assertTrue("User should be created in data-base as a new user (in ldap).",
    // userDao.doesUsernameAlreadyExist(new PFUserDO().setUsername("kai")));
    // final PFUserDO user = userDao.getInternalByName("kai");
    // Assert.assertEquals("kai", user.getUsername());
    // Assert.assertEquals(userDao.encryptPassword("successful"), user.getPassword());
    // Assert.assertEquals("Kai", user.getFirstname());
    // Assert.assertEquals("Reinhard", user.getLastname());
    // Assert.assertEquals("Micromata", user.getOrganization());
    // Assert.assertEquals("k.reinhard@acme.com", user.getEmail());
    // Assert.assertEquals("Developer", user.getDescription());
    //
    // userDao.internalMarkAsDeleted(user);
    // Assert.assertEquals("User is deleted in data-base. Login not possible.", LoginResultStatus.LOGIN_EXPIRED,
    // loginHandler.checkLogin("kai", "successful").getLoginResultStatus());
  }
}
