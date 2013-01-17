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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.access.AccessDao;
import org.projectforge.access.AccessException;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.test.TestBase;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

import de.micromata.hibernate.history.HistoryEntry;

public class InitDatabaseDaoWithTestDataTest extends TestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InitDatabaseDaoWithTestDataTest.class);

  private InitDatabaseDao initDatabaseDao;

  private AccessDao accessDao;

  private AddressDao addressDao;

  private AuftragDao auftragDao;

  private BookDao bookDao;

  private TaskDao taskDao;

  private UserGroupCache userGroupCache;

  public void setInitDatabaseDao(final InitDatabaseDao initDatabaseDao)
  {
    this.initDatabaseDao = initDatabaseDao;
  }

  public void setAccessDao(final AccessDao accessDao)
  {
    this.accessDao = accessDao;
  }

  public void setAddressDao(final AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }

  public void setAuftragDao(final AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
  }

  public void setTaskDao(final TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setBookDao(final BookDao bookDao)
  {
    this.bookDao = bookDao;
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
    final String testPassword = "demo123";
    final String encryptedPassword = userDao.encryptPassword(testPassword);
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
      assertEquals("For security reasons each password should be '" + testPassword + "'.", encryptedPassword, user.getPassword());
    }

    final List<GroupTaskAccessDO> accessList = accessDao.internalLoadAll();
    assertTrue(accessList.size() > 0);
    for (final GroupTaskAccessDO access : accessList) {
      assertNotNull("Access entries should be serialized.", access.getAccessEntries());
      assertTrue("Access entries should be serialized.", access.getAccessEntries().size() > 0);
    }

    final List<AddressDO> addressList = addressDao.internalLoadAll();
    assertTrue(addressList.size() > 0);

    final List<BookDO> bookList = bookDao.internalLoadAll();
    assertTrue(bookList.size() > 2);

    final List<TaskDO> taskList = taskDao.internalLoadAll();
    assertTrue(taskList.size() > 10);

    final List<AuftragDO> orderList = auftragDao.internalLoadAll();
    AuftragDO order = null;
    for (final AuftragDO ord : orderList) {
      if (ord.getNummer() == 1) {
        order = ord;
        break;
      }
    }
    assertNotNull("Order #1 not found.", order);
    assertEquals("Order #1 must have 3 order positions.", 3, order.getPositionen().size());

    final List<HistoryEntry> list = hibernate.loadAll(HistoryEntry.class);
    assertTrue("At least 10 history entries expected: " + list.size(), list.size() >= 10);

    log.error("****> Next exception and error message are OK (part of the test).");
    boolean exception = false;
    try {
      initDatabaseDao.initializeEmptyDatabase(InitDatabaseDao.DEFAULT_ADMIN_USER, encryptedPassword, null);
      fail("AccessException expected.");
    } catch (final AccessException ex) {
      exception = true;
      // Everything fine.
    }
    log.error("Last exception and error messages were OK (part of the test). <****");
    assertTrue(exception);

    log.error("****> Next exception and error message are OK (part of the test).");
    exception = false;
    try {
      initDatabaseDao.initializeEmptyDatabaseWithTestData(InitDatabaseDao.DEFAULT_ADMIN_USER, encryptedPassword, null);
      fail("AccessException expected.");
    } catch (final AccessException ex) {
      exception = true;
      // Everything fine.
    }
    log.error("Last exception and error messages were OK (part of the test). <****");
    assertTrue(exception);
  }
}
