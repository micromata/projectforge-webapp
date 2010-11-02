/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.projectforge.test.TestBase;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;

public class UserTest extends TestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserTest.class);

  private GroupDao groupDao;

  private TransactionTemplate txTemplate;

  public void setGroupDao(GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setTxTemplate(TransactionTemplate txTemplate)
  {
    this.txTemplate = txTemplate;
  }

  @Test
  public void testUserDO()
  {
    logon(TEST_ADMIN_USER);
    PFUserDO user = userDao.getInternalByName(TEST_ADMIN_USER);
    assertEquals(user.getUsername(), TEST_ADMIN_USER);
    UserGroupCache cache = userDao.getUserGroupCache();
    PFUserDO user1 = getUser("user1");
    String groupnames = cache.getGroupnames(user1.getId());
    assertEquals("Groupnames", "group1, group2", groupnames);
    assertEquals(true, cache.isUserMemberOfGroup(user1.getId(), getGroupId("group1")));
    assertEquals(false, cache.isUserMemberOfGroup(user1.getId(), getGroupId("group3")));
    GroupDO group = cache.getGroup(getGroupId("group1"));
    assertEquals("group1", group.getName());
    PFUserDO admin = getUser(ADMIN);
    assertEquals("Administrator", true, cache.isUserMemberOfAdminGroup(admin.getId()));
    assertEquals("Not administrator", false, cache.isUserMemberOfAdminGroup(user1.getId()));
  }

  @Test
  public void testGetUserDisplayname()
  {
    PFUserDO user = new PFUserDO();
    user.setUsername("hurzel");
    assertEquals("getUserDisplayname", "hurzel", user.getUserDisplayname());
    user.setLastname("Reinhard");
    assertEquals("getFullname", "Reinhard", user.getFullname());
    assertEquals("getUserDisplayname", "Reinhard (hurzel)", user.getUserDisplayname());
    user.setFirstname("Kai");
    assertEquals("getFullname", "Kai Reinhard", user.getFullname());
    assertEquals("getUserDisplayname", "Kai Reinhard (hurzel)", user.getUserDisplayname());
  }

  @Test
  public void testSaveAndUpdate()
  {
    logon(TEST_ADMIN_USER);
    PFUserDO user = new PFUserDO();
    user.setUsername("UserTest");
    user.setPassword("Hurzel");
    user.setDescription("Description");
    Serializable id = userDao.save(user);
    user = userDao.getById(id);
    assertEquals("UserTest", user.getUsername());
    assertNull(user.getPassword()); // Not SHA, should be ignored.
    assertEquals("Description", user.getDescription());
    user.setDescription("Description\ntest");
    user.setPassword("secret");
    userDao.update(user);
    user = userDao.getById(id);
    assertEquals("Description\ntest", user.getDescription());
    assertNull(user.getPassword()); // Not SHA, should be ignored.
    user.setPassword("SHA{...}");
    userDao.update(user);
    user = userDao.getById(id);
    assertEquals("SHA{...}", user.getPassword());
  }

  @Test
  public void testCopyValues()
  {
    PFUserDO src = new PFUserDO();
    src.setPassword("test");
    src.setUsername("usertest");
    PFUserDO dest = new PFUserDO();
    dest.copyValuesFrom(src);
    assertNull(dest.getPassword());
    assertEquals("usertest", dest.getUsername());
    log.error("Last error message about not encrypted password is OK for this test!");
    src.setPassword("SHA{9B4DDF20612345C5FC7A9355022E07368CDDF23A}");
    dest.copyValuesFrom(src);
    assertEquals("SHA{9B4DDF20612345C5FC7A9355022E07368CDDF23A}", dest.getPassword());
  }

  @Test
  public void testPasswordQuality()
  {
    assertEquals("Empty password not allowed.", UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED, userDao.checkPasswordQuality(null));
    assertEquals("Password with less than 6 characters not allowed.", UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED, userDao
        .checkPasswordQuality(""));
    assertEquals("Password with less than 6 characters not allowed.", UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED, userDao
        .checkPasswordQuality("o2345"));
    assertEquals("Password must have one non letter at minimum.", UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED, userDao
        .checkPasswordQuality("ProjectForge"));
    assertEquals("Password must have one letter at minimum.", UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED, userDao
        .checkPasswordQuality("123456"));
    assertEquals("Password must have one letter at minimum.", UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK_FAILED, userDao
        .checkPasswordQuality("1234567"));
    assertNull("Password OK.", userDao.checkPasswordQuality("kjh!id"));
    assertNull("Password OK.", userDao.checkPasswordQuality("kjh8idsf"));
    assertNull("Password OK.", userDao.checkPasswordQuality("  5 g "));
  }

  @Test
  public void history()
  {
    logon(TEST_ADMIN_USER);
    txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        initTestDB.addUser("UserTest.historyUser1a");
        initTestDB.addUser("UserTest.historyUser1b");
        initTestDB.addUser("UserTest.historyUser2a");
        initTestDB.addUser("UserTest.historyUser2b");
        initTestDB.addGroup("UserTest.historyGroup1", new String[] { "UserTest.historyUser1a", "UserTest.historyUser1b"});
        initTestDB.addGroup("UserTest.historyGroup2", (String[]) null);
        initTestDB.addGroup("UserTest.historyGroup3", (String[]) null);
        return null;
      }
    });

    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        // Checking history entries of user for new group:
        HistoryEntry[] historyEntries = userDao.getHistoryEntries(getUser("UserTest.historyUser1a"));
        assertEquals(2, historyEntries.length); // insert and update assignedGroups
        HistoryEntry entry = historyEntries[0]; // Update assignedGroups entry
        assertEquals(1, entry.getDelta().size());
        assertEquals("", entry.getDelta().get(0).getOldValue());
        assertGroupIds(new String[] { "UserTest.historyGroup1"}, entry.getDelta().get(0).getNewValue());

        // Checking history entries of new group:
        historyEntries = groupDao.getHistoryEntries(getGroup("UserTest.historyGroup1"));
        assertEquals(1, historyEntries.length); // insert
        return null;
      }
    });

    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        // (Un)assigning groups:
        PFUserDO user = userDao.internalGetById(getUserId("UserTest.historyUser1a"));
        Set<Integer> groupIdsToAssign = new HashSet<Integer>();
        groupIdsToAssign.add(getGroupId("UserTest.historyGroup2"));
        groupIdsToAssign.add(getGroupId("UserTest.historyGroup3"));
        Set<Integer> groupIdsToUnassign = new HashSet<Integer>();
        groupIdsToUnassign.add(getGroupId("UserTest.historyGroup1"));
        groupDao.assignGroups(user, groupIdsToAssign, groupIdsToUnassign);
        return null;
      }
    });

    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        // Checking history of updated user:
        HistoryEntry[] historyEntries = userDao.getHistoryEntries(getUser("UserTest.historyUser1a"));
        assertEquals(3, historyEntries.length);
        assertUserHistoryEntry(historyEntries[0], new String[] { "UserTest.historyGroup2", "UserTest.historyGroup3"},
            new String[] { "UserTest.historyGroup1"});

        // Checking history entries of updated groups:
        historyEntries = groupDao.getHistoryEntries(getGroup("UserTest.historyGroup1"));
        GroupDO group = groupDao.internalGetById(getGroupId("UserTest.historyGroup1"));
        Set<PFUserDO> users = group.getAssignedUsers();
        assertEquals(1, users.size()); // Assigned users are: "UserTest.historyUser1b"
        assertEquals("2 history entries (1 insert and 1 assigned users", 2, historyEntries.length); // insert and update assignedUsers
        assertGroupHistoryEntry(historyEntries[0], null, new String[] { "UserTest.historyUser1a"});
        return null;
      }
    });
  }

  @Test
  public void testUniqueUsernameDO()
  {
    final Serializable[] ids = new Integer[2];
    txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        PFUserDO user = createTestUser("42");
        ids[0] = userDao.internalSave(user);
        user = createTestUser("100");
        ids[1] = userDao.internalSave(user);
        return null;
      }
    });
    txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        PFUserDO user = createTestUser("42");
        assertTrue("Username should already exist.", userDao.doesUsernameAlreadyExist(user));
        user.setUsername("5");
        assertFalse("Signature should not exist.", userDao.doesUsernameAlreadyExist(user));
        userDao.internalSave(user);
        return null;
      }
    });
    txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        PFUserDO dbBook = userDao.internalGetById(ids[1]);
        PFUserDO user = new PFUserDO();
        user.copyValuesFrom(dbBook);
        assertFalse("Signature does not exist.", userDao.doesUsernameAlreadyExist(user));
        user.setUsername("42");
        assertTrue("Signature does already exist.", userDao.doesUsernameAlreadyExist(user));
        user.setUsername("4711");
        assertFalse("Signature does not exist.", userDao.doesUsernameAlreadyExist(user));
        userDao.internalUpdate(user);
        return null;
      }
    });
    txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        PFUserDO user = userDao.internalGetById(ids[1]);
        assertFalse("Signature does not exist.", userDao.doesUsernameAlreadyExist(user));
        return null;
      }
    });
  }

  private PFUserDO createTestUser(String username)
  {
    PFUserDO user = new PFUserDO();
    user.setUsername(username);
    return user;
  }

  /**
   * Checks if the history entry contains all and only all expected entries in old and new value.
   * @param entry
   * @param expectedAssignedUserNames
   * @param expectedUnassignedUserNames
   */
  void assertGroupHistoryEntry(HistoryEntry entry, String[] expectedAssignedUserNames, String[] expectedUnassignedUserNames)
  {
    List<PropertyDelta> list = entry.getDelta();
    assertEquals(1, list.size());
    PropertyDelta delta = list.get(0);
    assertUserIds(expectedUnassignedUserNames, delta.getOldValue());
    assertUserIds(expectedAssignedUserNames, delta.getNewValue());
  }

  /**
   * Checks if the history entry contains all and only all expected entries in old and new value.
   * @param entry
   * @param expectedAssignedGroupNames
   * @param expectedUnassignedGroupNames
   */
  void assertUserHistoryEntry(HistoryEntry entry, String[] expectedAssignedGroupNames, String[] expectedUnassignedGroupNames)
  {
    List<PropertyDelta> list = entry.getDelta();
    assertEquals(1, list.size());
    PropertyDelta delta = list.get(0);
    assertGroupIds(expectedUnassignedGroupNames, delta.getOldValue());
    assertGroupIds(expectedAssignedGroupNames, delta.getNewValue());
  }

  /**
   * Convert expectedGroupNames in list of expected group ids: {2,4,7} Asserts that all group ids in groupssString are expected and vice
   * versa.
   * @param expectedGroupNames
   * @param groupsString csv of groups, e. g. {2,4,7}
   */
  void assertGroupIds(String[] expectedGroupNames, String groupsString)
  {
    if (expectedGroupNames == null) {
      assertTrue(StringUtils.isEmpty(groupsString));
    }
    String[] expectedGroups = new String[expectedGroupNames.length];
    for (int i = 0; i < expectedGroupNames.length; i++) {
      expectedGroups[i] = getGroup(expectedGroupNames[i]).getId().toString();
    }
    assertIds(expectedGroups, groupsString);
  }

  /**
   * Convert expectedUserNames in list of expected users ids: {2,4,7} Asserts that all user ids in usersString are expected and vice versa.
   * @param expectedUserNames
   * @param groupsString csv of groups, e. g. {2,4,7}
   */
  void assertUserIds(String[] expectedUserNames, String usersString)
  {
    if (expectedUserNames == null) {
      assertTrue(StringUtils.isEmpty(usersString));
      return;
    }
    String[] expectedUsers = new String[expectedUserNames.length];
    for (int i = 0; i < expectedUserNames.length; i++) {
      expectedUsers[i] = getUser(expectedUserNames[i]).getId().toString();
    }
    assertIds(expectedUsers, usersString);
  }

  private void assertIds(String[] expectedEntries, String csvString)
  {
    String[] entries = StringUtils.split(csvString, ',');
    for (String expected : expectedEntries) {
      assertTrue("'" + expected + "' expected in: " + ArrayUtils.toString(entries), ArrayUtils.contains(entries, expected));
    }
    for (String entry : entries) {
      assertTrue("'" + entry + "' doesn't expected in: " + ArrayUtils.toString(expectedEntries), ArrayUtils
          .contains(expectedEntries, entry));
    }
  }
}
