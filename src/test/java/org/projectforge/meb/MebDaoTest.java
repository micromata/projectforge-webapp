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

package org.projectforge.meb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Date;

import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.test.TestBase;

public class MebDaoTest extends TestBase
{
  private MebDao mebDao;

  @Test
  public void testGetMail()
  {
    assertEquals("598d4c200461b81522a3328565c25f7c", MebDao.createCheckSum("hallo"));
    assertEquals("598d4c200461b81522a3328565c25f7c", MebDao.createCheckSum(" h#äöüß.,al   lo\n.,-"));
  }

  @Test
  public void testMebDaoAccess()
  {
    MebEntryDO entry = new MebEntryDO().setDate(new Date()).setSender("1234567890").setStatus(MebEntryStatus.RECENT);
    logon(TestBase.TEST_USER);
    try {
      mebDao.save(entry);
      fail("Exception expected because only administrators can insert entries without an owner.");
    } catch (final AccessException ex) {
      // OK.
    }
    logon(TestBase.ADMIN);
    mebDao.save(entry); // Allowed for admins
    logon(TestBase.TEST_USER);
    entry = new MebEntryDO().setDate(new Date()).setSender("1234567890").setStatus(MebEntryStatus.RECENT);
    entry.setOwner(getUser(TestBase.TEST_USER));
    Serializable id = mebDao.save(entry);
    mebDao.getById(id);
    logon(TestBase.TEST_USER2);
    try {
      mebDao.getById(id);
      fail("Exception expected because user shouldn't have access to foreign entries.");
    } catch (final AccessException ex) {
      // OK.
    }
    entry = new MebEntryDO().setDate(new Date()).setOwner(getUser(TestBase.TEST_USER)).setSender("1234567890").setStatus(MebEntryStatus.RECENT);
    try {
      mebDao.save(entry);
      fail("Exception expected because only administrators can insert entries without an owner.");
    } catch (final AccessException ex) {
      // OK.
    }
    logon(TestBase.TEST_USER);
    mebDao.save(entry);
    logon(TestBase.ADMIN);
    entry = new MebEntryDO().setDate(new Date()).setSender("1234567890").setStatus(MebEntryStatus.RECENT);
    id = mebDao.save(entry);
    entry = mebDao.getById(id);
    entry.setOwner(getUser(TestBase.TEST_USER));
    mebDao.update(entry);
    try {
      entry = mebDao.getById(id);
      fail("Exception expected because only owners have select access.");
    } catch (final AccessException ex) {
      // OK.
    }
  }

  public void setMebDao(MebDao mebDao)
  {
    this.mebDao = mebDao;
  }
}
