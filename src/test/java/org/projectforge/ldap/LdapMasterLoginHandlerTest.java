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
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.projectforge.test.TestBase;
import org.projectforge.user.LoginResultStatus;
import org.projectforge.user.PFUserDO;

public class LdapMasterLoginHandlerTest extends TestBase
{
  @Test
  public void loginAndCreateLdapUser()
  {
    final String userBase = "ou=users";
    final LdapUserDao ldapUserDao = mock(LdapUserDao.class);
    final LdapMasterLoginHandler loginHandler = new LdapMasterLoginHandler();
    loginHandler.ldapConfig = new LdapConfig().setUserBase(userBase);
    loginHandler.userDao = userDao;
    loginHandler.ldapUserDao = ldapUserDao;
    loginHandler.ldapOrganizationalUnitDao = mock(LdapOrganizationalUnitDao.class);
    loginHandler.initialize();

    userDao.internalSave(new PFUserDO().setUsername("kai").setPassword(userDao.encryptPassword("successful")).setFirstname("Kai").setLastname("Reinhard"));
    Assert.assertEquals(LoginResultStatus.SUCCESS, loginHandler.checkLogin("kai", "successful").getLoginResultStatus());

    final ArgumentCaptor<LdapPerson> argumentCaptor = ArgumentCaptor.forClass(LdapPerson.class);
    verify(ldapUserDao).createOrUpdate(Mockito.anyString(), argumentCaptor.capture());
    final LdapPerson createdLdapUser = argumentCaptor.getValue();
    Assert.assertEquals("kai", createdLdapUser.getUid());
    Assert.assertEquals("Kai", createdLdapUser.getGivenName());
    Assert.assertEquals("Reinhard", createdLdapUser.getSurname());
    //Assert.assertEquals("successful", createdLdapUser.get());
  }

  //  @Test
  //  public void loginAndUpdateLdapUser()
  //  {
  //  }
}
