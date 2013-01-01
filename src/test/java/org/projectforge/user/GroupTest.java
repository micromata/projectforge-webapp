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

package org.projectforge.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.projectforge.test.TestBase;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.HistoryEntryType;

public class GroupTest extends TestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupTest.class);

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
  public void testSaveAndUpdate()
  {
    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        logon(TEST_ADMIN_USER);
        GroupDO group = new GroupDO();
        group.setName("testgroup");
        Set<PFUserDO> assignedUsers = new HashSet<PFUserDO>();
        group.setAssignedUsers(assignedUsers);
        assignedUsers.add(getUser(TEST_USER));
        Serializable id = groupDao.save(group);
        group = groupDao.getById(id);
        assertEquals("testgroup", group.getName());
        assertEquals(1, group.getAssignedUsers().size());
        assertTrue(group.getAssignedUsers().contains(getUser(TEST_USER)));
        PFUserDO user = getUser(TEST_USER2);
        Assert.assertNotNull(user);
        group.getAssignedUsers().add(user);
        groupDao.update(group);
        group = groupDao.getById(id);
        assertEquals(2, group.getAssignedUsers().size());
        assertTrue(group.getAssignedUsers().contains(getUser(TEST_USER)));
        assertTrue(group.getAssignedUsers().contains(user));
        return null;
      }
    });
  }

  @Test
  public void testHistory()
  {
    txTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status)
      {
        PFUserDO histUser = logon(TEST_ADMIN_USER);

        GroupDO group = new GroupDO();
        group.setName("historyGroup");
        Set<PFUserDO> assignedUsers = new HashSet<PFUserDO>();
        assignedUsers.add(getUser(TEST_USER));
        assignedUsers.add(getUser(TEST_USER2));
        group.setAssignedUsers(assignedUsers);
        Serializable id = groupDao.save(group);

        group = groupDao.getById(id);
        assertEquals(2, group.getAssignedUsers().size());
        group.getAssignedUsers().remove(getUser(TEST_USER2));
        groupDao.update(group);

        group = groupDao.getById(id);
        assertEquals(1, group.getAssignedUsers().size());
        PFUserDO user = initTestDB.addUser("historyGroupUser");
        group.getAssignedUsers().add(user);
        groupDao.update(group);

        group = groupDao.getById(id);
        assertEquals(2, group.getAssignedUsers().size());

        HistoryEntry[] historyEntries = groupDao.getHistoryEntries(group);
        assertEquals(3, historyEntries.length);
        HistoryEntry entry = historyEntries[2];
        assertHistoryEntry(entry, group.getId(), histUser, HistoryEntryType.INSERT);
        entry = historyEntries[1];
        assertHistoryEntry(entry, group.getId(), histUser, HistoryEntryType.UPDATE, "assignedUsers", PFUserDO.class, getUser(TEST_USER2)
            .getId().toString(), "");
        entry = historyEntries[0];
        assertHistoryEntry(entry, group.getId(), histUser, HistoryEntryType.UPDATE, "assignedUsers", PFUserDO.class, "", getUser(
            "historyGroupUser").getId().toString());
        historyEntries = userDao.getHistoryEntries(getUser("historyGroupUser"));
        log.debug(entry);
        return null;
      }

    });
  }
}
