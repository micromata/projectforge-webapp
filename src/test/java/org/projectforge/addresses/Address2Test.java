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

package org.projectforge.addresses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.core.SimpleHistoryEntry;
import org.projectforge.task.TaskDO;
import org.projectforge.test.TestBase;
import org.projectforge.user.PFUserDO;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.HistoryEntryType;
import de.micromata.hibernate.history.delta.PropertyDelta;

public class AddressEntryTest extends TestBase
{
  private final static Logger log = Logger.getLogger(AddressEntryTest.class);

  private AddressEntryDao addressEntryDao;

  public void setAddressEntryDao(final AddressEntryDao addressEntryDao)
  {
    this.addressEntryDao = addressEntryDao;
  }

  //  public AddressEntryTest() {
  //    addressEntryDao = new AddressEntryDao(AddressEntryDO.class);
  //  }

  @Test
  public void testSaveAndUpdate()
  {
    logon(ADMIN);

    final AddressEntryDO a1 = new AddressEntryDO();
    a1.setName("Kai Reinhard");
    a1.setTask(getTask("1.1"));
    addressEntryDao.save(a1);
    log.debug(a1);

    a1.setName("Hurzel");
    addressEntryDao.setTask(a1, getTask("1.2").getId());
    addressEntryDao.update(a1);
    assertEquals("Hurzel", a1.getName());

    final AddressEntryDO a2 = addressEntryDao.getById(a1.getId());
    assertEquals("Hurzel", a2.getName());
    assertEquals(getTask("1.2").getId(), a2.getTaskId());
    a2.setName("Micromata GmbH");
    addressEntryDao.setTask(a2, getTask("1").getId());
    addressEntryDao.update(a2);
    log.debug(a2);

    final AddressEntryDO a3 = addressEntryDao.getById(a1.getId());
    assertEquals("Micromata GmbH", a3.getName());
    assertEquals(getTask("1").getId(), a3.getTaskId());
    log.debug(a3);
  }

  @Test
  public void testDeleteAndUndelete()
  {
    logon(ADMIN);
    AddressEntryDO a1 = new AddressEntryDO();
    a1.setName("Test");
    a1.setTask(getTask("1.1"));
    addressEntryDao.save(a1);

    final Integer id = a1.getId();
    a1 = addressEntryDao.getById(id);
    addressEntryDao.markAsDeleted(a1);
    a1 = addressEntryDao.getById(id);
    assertEquals("Should be marked as deleted.", true, a1.isDeleted());

    addressEntryDao.undelete(a1);
    a1 = addressEntryDao.getById(id);
    assertEquals("Should be undeleted.", false, a1.isDeleted());
  }

  @Test(expected = RuntimeException.class)
  public void testDelete()
  {
    AddressEntryDO a1 = new AddressEntryDO();
    a1.setName("Not deletable");
    a1.setTask(getTask("1.1"));
    addressEntryDao.save(a1);
    final Integer id = a1.getId();
    a1 = addressEntryDao.getById(id);
    addressEntryDao.delete(a1);
  }

  @Test
  public void testHistory()
  {
    final PFUserDO user = getUser(TestBase.ADMIN);
    logon(user.getUsername());
    AddressEntryDO a1 = new AddressEntryDO();
    a1.setName("History test");
    a1.setTask(getTask("1.1"));
    addressEntryDao.save(a1);
    final Integer id = a1.getId();
    a1.setName("History 2");
    addressEntryDao.update(a1);
    HistoryEntry[] historyEntries = addressEntryDao.getHistoryEntries(a1);
    assertEquals(2, historyEntries.length);
    HistoryEntry entry = historyEntries[0];
    log.debug(entry);
    assertHistoryEntry(entry, id, user, HistoryEntryType.UPDATE, "name", String.class, "History test", "History 2");
    entry = historyEntries[1];
    log.debug(entry);
    assertHistoryEntry(entry, id, user, HistoryEntryType.INSERT, null, null, null, null);

    a1.setTask(getTask("1.2"));
    addressEntryDao.update(a1);
    historyEntries = addressEntryDao.getHistoryEntries(a1);
    assertEquals(3, historyEntries.length);
    entry = historyEntries[0];
    log.debug(entry);
    assertHistoryEntry(entry, id, user, HistoryEntryType.UPDATE, "task", TaskDO.class, getTask("1.1").getId(), getTask("1.2").getId());

    a1.setTask(getTask("1.1"));
    a1.setName("History test");
    addressEntryDao.update(a1);
    historyEntries = addressEntryDao.getHistoryEntries(a1);
    assertEquals(4, historyEntries.length);
    entry = historyEntries[0];
    log.debug(entry);
    assertHistoryEntry(entry, id, user, HistoryEntryType.UPDATE, null, null, null, null);
    final List<PropertyDelta> delta = entry.getDelta();
    assertEquals(2, delta.size());
    for (int i = 0; i < 2; i++) {
      final PropertyDelta prop = delta.get(0);
      if ("name".equals(prop.getPropertyName()) == true) {
        assertPropertyDelta(prop, "name", String.class, "History 2", "History test");
      } else {
        assertPropertyDelta(prop, "task", TaskDO.class, getTask("1.2").getId(), getTask("1.1").getId());
      }
    }

    List<SimpleHistoryEntry> list = addressEntryDao.getSimpleHistoryEntries(a1);
    assertEquals(5, list.size());
    for (int i = 0; i < 2; i++) {
      final SimpleHistoryEntry se = list.get(i);
      if ("name".equals(se.getPropertyName()) == true) {
        assertSimpleHistoryEntry(se, user, HistoryEntryType.UPDATE, "name", String.class, "History 2", "History test");
      } else {
        assertSimpleHistoryEntry(se, user, HistoryEntryType.UPDATE, "task", TaskDO.class, getTask("1.2").getId(), getTask("1.1").getId());
      }
    }
    SimpleHistoryEntry se = list.get(2);
    assertSimpleHistoryEntry(se, user, HistoryEntryType.UPDATE, "task", TaskDO.class, getTask("1.1").getId(), getTask("1.2").getId());
    se = list.get(3);
    assertSimpleHistoryEntry(se, user, HistoryEntryType.UPDATE, "name", String.class, "History test", "History 2");
    se = list.get(4);
    assertSimpleHistoryEntry(se, user, HistoryEntryType.INSERT, null, null, null, null);

    a1 = addressEntryDao.getById(a1.getId());
    final Date date = a1.getLastUpdate();
    final String oldName = a1.getName();
    a1.setName("Micromata GmbH");
    a1.setName(oldName);
    addressEntryDao.update(a1);
    a1 = addressEntryDao.getById(a1.getId());
    list = addressEntryDao.getSimpleHistoryEntries(a1);
    assertEquals(5, list.size());
    assertEquals(date, a1.getLastUpdate()); // Fails: Fix AbstractBaseDO.copyDeclaredFields: ObjectUtils.equals(Boolean, boolean) etc.
  }

  @Test
  public void checkStandardAccess()
  {
    AddressEntryDO a1 = new AddressEntryDO();
    a1.setName("testa1");
    a1.setTask(getTask("ta_1_siud"));
    addressEntryDao.internalSave(a1);
    AddressEntryDO a2 = new AddressEntryDO();
    a2.setName("testa2");
    a2.setTask(getTask("ta_2_siux"));
    addressEntryDao.internalSave(a2);
    final AddressEntryDO a3 = new AddressEntryDO();
    a3.setName("testa3");
    a3.setTask(getTask("ta_3_sxxx"));
    addressEntryDao.internalSave(a3);
    final AddressEntryDO a4 = new AddressEntryDO();
    a4.setName("testa4");
    a4.setTask(getTask("ta_4_xxxx"));
    addressEntryDao.internalSave(a4);
    logon(TestBase.TEST_USER);

    // Select
    try {
      addressEntryDao.getById(a4.getId());
      fail("User has no access to select");
    } catch (final AccessException ex) {
      assertAccessException(ex, getTask("ta_4_xxxx").getId(), AccessType.TASKS, OperationType.SELECT);
    }
    AddressEntryDO address = addressEntryDao.getById(a3.getId());
    assertEquals("testa3", address.getName());

    // Select filter
    final BaseSearchFilter searchFilter = new BaseSearchFilter();
    searchFilter.setSearchString("testa*");
    final QueryFilter filter = new QueryFilter(searchFilter);
    filter.addOrder(Order.asc("name"));
    final List<AddressEntryDO> result = addressEntryDao.getList(filter);
    assertEquals("Should found 3 address'.", 3, result.size());
    final HashSet<String> set = new HashSet<String>();
    set.add("testa1");
    set.add("testa2");
    set.add("testa3");
    assertTrue("Hit first entry", set.remove(result.get(0).getName()));
    assertTrue("Hit second entry", set.remove(result.get(1).getName()));
    assertTrue("Hit third entry", set.remove(result.get(2).getName()));
    // test_a4 should not be included in result list (no select access)

    // Insert
    address = new AddressEntryDO();
    address.setName("test");
    addressEntryDao.setTask(address, getTask("ta_4_xxxx").getId());
    try {
      addressEntryDao.save(address);
      fail("User has no access to insert");
    } catch (final AccessException ex) {
      assertAccessException(ex, getTask("ta_4_xxxx").getId(), AccessType.TASKS, OperationType.INSERT);
    }
    addressEntryDao.setTask(address, getTask("ta_1_siud").getId());
    addressEntryDao.save(address);
    assertEquals("test", address.getName());

    // Update
    a3.setName("test_a3test");
    try {
      addressEntryDao.update(a3);
      fail("User has no access to update");
    } catch (final AccessException ex) {
      assertAccessException(ex, getTask("ta_3_sxxx").getId(), AccessType.TASKS, OperationType.UPDATE);
    }
    a2.setName("testa2test");
    addressEntryDao.update(a2);
    address = addressEntryDao.getById(a2.getId());
    assertEquals("testa2test", address.getName());
    a2.setName("testa2");
    addressEntryDao.update(a2);
    address = addressEntryDao.getById(a2.getId());
    assertEquals("testa2", address.getName());

    // Update with moving in task hierarchy
    a2.setName("testa2test");
    addressEntryDao.setTask(a2, getTask("ta_1_siud").getId());
    try {
      addressEntryDao.update(a2);
      fail("User has no access to update");
    } catch (final AccessException ex) {
      assertAccessException(ex, getTask("ta_2_siux").getId(), AccessType.TASKS, OperationType.DELETE);
    }
    a2 = addressEntryDao.getById(a2.getId());
    a1.setName("testa1test");
    addressEntryDao.setTask(a1, getTask("ta_5_sxux").getId());
    try {
      addressEntryDao.update(a1);
      fail("User has no access to update");
    } catch (final AccessException ex) {
      assertAccessException(ex, getTask("ta_5_sxux").getId(), AccessType.TASKS, OperationType.INSERT);
    }
    a1 = addressEntryDao.getById(a1.getId());
    assertEquals("testa1", a1.getName());

    // Delete
    try {
      addressEntryDao.delete(a1);
      fail("Address is historizable and should not be allowed to delete.");
    } catch (final RuntimeException ex) {
      assertEquals(true, ex.getMessage().startsWith(AddressEntryDao.EXCEPTION_HISTORIZABLE_NOTDELETABLE));
    }
    try {
      addressEntryDao.markAsDeleted(a2);
      fail("User has no access to delete");
    } catch (final AccessException ex) {
      assertAccessException(ex, getTask("ta_2_siux").getId(), AccessType.TASKS, OperationType.DELETE);
    }
  }

  //  @Test
  //  public void testInstantMessagingField() throws Exception
  //  {
  //    final AddressEntryDO address = new AddressEntryDO();
  //    assertNull(address.getInstantMessaging4DB());
  //    address.setInstantMessaging(InstantMessagingType.SKYPE, "skype-name");
  //    assertEquals("SKYPE=skype-name", address.getInstantMessaging4DB());
  //    address.setInstantMessaging(InstantMessagingType.AIM, "aim-id");
  //    assertEquals("SKYPE=skype-name\nAIM=aim-id", address.getInstantMessaging4DB());
  //    address.setInstantMessaging(InstantMessagingType.YAHOO, "yahoo-name");
  //    assertEquals("SKYPE=skype-name\nAIM=aim-id\nYAHOO=yahoo-name", address.getInstantMessaging4DB());
  //    address.setInstantMessaging(InstantMessagingType.YAHOO, "");
  //    assertEquals("SKYPE=skype-name\nAIM=aim-id", address.getInstantMessaging4DB());
  //    address.setInstantMessaging(InstantMessagingType.SKYPE, "");
  //    assertEquals("AIM=aim-id", address.getInstantMessaging4DB());
  //    address.setInstantMessaging(InstantMessagingType.AIM, "");
  //    assertNull(address.getInstantMessaging4DB());
  //  }
}
