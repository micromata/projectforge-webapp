/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.test.TestBase;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

public class InitDatabaseDaoWithTestDataTest extends TestBase
{
  private InitDatabaseDao initDatabaseDao;

  private AddressDao addressDao;

  private TaskDao taskDao;

  private BookDao bookDao;

  private UserGroupCache userGroupCache;

  public void setInitDatabaseDao(InitDatabaseDao initDatabaseDao)
  {
    this.initDatabaseDao = initDatabaseDao;
  }

  public void setAddressDao(AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }

  public void setTaskDao(TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setBookDao(BookDao bookDao)
  {
    this.bookDao = bookDao;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  @BeforeClass
  public static void setUp() throws Exception
  {
    init(false);
  }

  @Test
  public void initializeEmptyDatabase()
  {
    final String encryptedPassword = userDao.encryptPassword("testpassword");
    userGroupCache.setExpired(); // Force reload (because it's may be expired due to previous tests).
    assertTrue(initDatabaseDao.isEmpty());
    initDatabaseDao.initializeEmptyDatabaseWithTestData("myadmin", encryptedPassword, null);
    final PFUserDO initialAdminUser = userDao.authenticateUser("myadmin", encryptedPassword);
    assertNotNull(initialAdminUser);
    assertEquals("myadmin", initialAdminUser.getUsername());
    final Collection<Integer> col = userGroupCache.getUserGroups(initialAdminUser);
    assertEquals(5, col.size());
    assertTrue(userGroupCache.isUserMemberOfAdminGroup(initialAdminUser.getId()));
    assertTrue(userGroupCache.isUserMemberOfFinanceGroup(initialAdminUser.getId()));

    final List<PFUserDO> userList = userDao.internalLoadAll();
    assertTrue(userList.size() > 0);
    for (final PFUserDO user : userList) {
      assertNull("For security reasons the stay-logged-in-key should be null.", user.getStayLoggedInKey());
    }

    final List<AddressDO> addressList = addressDao.internalLoadAll();
    assertTrue(addressList.size() > 0);

    final List<BookDO> bookList = bookDao.internalLoadAll();
    assertTrue(bookList.size() > 2);

    final List<TaskDO> taskList = taskDao.internalLoadAll();
    assertTrue(taskList.size() > 10);

    boolean exception = false;
    try {
      initDatabaseDao.initializeEmptyDatabase(InitDatabaseDao.DEFAULT_ADMIN_USER, encryptedPassword, null);
      fail("AccessException expected.");
    } catch (AccessException ex) {
      exception = true;
      // Everything fine.
    }
    assertTrue(exception);

    exception = false;
    try {
      initDatabaseDao.initializeEmptyDatabaseWithTestData(InitDatabaseDao.DEFAULT_ADMIN_USER, encryptedPassword, null);
      fail("AccessException expected.");
    } catch (AccessException ex) {
      exception = true;
      // Everything fine.
    }
    assertTrue(exception);
  }
}
