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

package org.projectforge.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.test.TestBase;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

public class InitDatabaseDaoTest extends TestBase
{
  static final String DEFAULT_ADMIN_PASSWORD = "manage";

  private InitDatabaseDao initDatabaseDao;

  private UserGroupCache userGroupCache;

  public void setInitDatabaseDao(final InitDatabaseDao initDatabaseDao)
  {
    this.initDatabaseDao = initDatabaseDao;
  }

  public void setUserGroupCache(final UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  @BeforeClass
  public static void setUp() throws Exception
  {
    preInit();
    init(false);
  }

  @Test
  public void initializeEmptyDatabase()
  {
    final String encryptedPassword = userDao.encryptPassword(DEFAULT_ADMIN_PASSWORD);
    userGroupCache.setExpired(); // Force reload (because it's may be expired due to previous tests).
    assertTrue(initDatabaseDao.isEmpty());
    final PFUserDO admin = new PFUserDO();
    admin.setId(1);
    PFUserContext.setUser(admin);
    initDatabaseDao.initializeEmptyDatabase(InitDatabaseDao.DEFAULT_ADMIN_USER, encryptedPassword, null);
    final PFUserDO user = userDao.authenticateUser(InitDatabaseDao.DEFAULT_ADMIN_USER, encryptedPassword);
    assertNotNull(user);
    assertEquals(InitDatabaseDao.DEFAULT_ADMIN_USER, user.getUsername());
    final Collection<Integer> col = userGroupCache.getUserGroups(user);
    assertEquals(4, col.size());
    assertTrue(userGroupCache.isUserMemberOfAdminGroup(user.getId()));
    assertTrue(userGroupCache.isUserMemberOfFinanceGroup(user.getId()));

    boolean exception = false;
    try {
      initDatabaseDao.initializeEmptyDatabase(InitDatabaseDao.DEFAULT_ADMIN_USER, encryptedPassword, null);
      fail("AccessException expected.");
    } catch (final AccessException ex) {
      exception = true;
      // Everything fine.
    }
    assertTrue(exception);

    exception = false;
    try {
      initDatabaseDao.initializeEmptyDatabaseWithTestData(InitDatabaseDao.DEFAULT_ADMIN_USER, encryptedPassword, null);
      fail("AccessException expected.");
    } catch (final AccessException ex) {
      exception = true;
      // Everything fine.
    }
    assertTrue(exception);
  }
}
