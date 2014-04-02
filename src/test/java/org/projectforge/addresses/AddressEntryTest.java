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

import org.apache.log4j.Logger;
import org.junit.Test;
import org.projectforge.test.TestBase;

public class AddressEntryTest extends TestBase
{
  private final static Logger log = Logger.getLogger(AddressEntryTest.class);

  private AddressEntryDao addressEntryDao;
  private Address2Dao address2Dao;

  public void setAddressEntryDao(final AddressEntryDao addressEntryDao)
  {
    this.addressEntryDao = addressEntryDao;
  }

  public void setAddress2Dao(final Address2Dao address2Dao)
  {
    this.address2Dao = address2Dao;
  }

  @Test
  public void testSaveAndUpdate()
  {
    logon(ADMIN);

    //final Address2Dao address2Dao = new Address2Dao();
    final Address2DO a1 = new Address2DO();
    a1.setName("Kai Reinhard");
    a1.setTask(getTask("1.1"));
    address2Dao.save(a1);
    log.debug(a1);

    final AddressEntryDO ae1 = new AddressEntryDO();
    ae1.setAddress(a1);
    ae1.setAddressType(AddressType.BUSINESS);
    ae1.setStreet("Marie-Calm-Straße 1-5");
    addressEntryDao.save(ae1);
    log.debug(ae1);

    final AddressEntryDO ae2 = addressEntryDao.getById(ae1.getId());
    assertEquals("Marie-Calm-Straße 1-5", ae2.getStreet());

    ae2.setAddressType(AddressType.POSTAL);
    ae2.setStreet("Teststrasse 42");

    addressEntryDao.update(ae2);
    log.debug(ae2);

    final AddressEntryDO ae3 = addressEntryDao.getById(ae2.getId());
    assertEquals("Teststrasse 42", ae3.getStreet());
    assertEquals(AddressType.POSTAL, ae3.getAddressType());
    log.debug(ae3);
  }

  @Test
  public void testDeleteAndUndelete()
  {
    logon(ADMIN);
    final Address2DO a1 = new Address2DO();
    a1.setName("Test");
    a1.setTask(getTask("1.1"));
    address2Dao.save(a1);

    AddressEntryDO ae1 = new AddressEntryDO();
    ae1.setAddress(a1);
    ae1.setAddressType(AddressType.BUSINESS);
    ae1.setStreet("Marie-Calm-Straße 1-5");
    addressEntryDao.save(ae1);
    log.debug(ae1);

    final Integer id = ae1.getId();
    ae1 = addressEntryDao.getById(id);
    addressEntryDao.markAsDeleted(ae1);
    ae1 = addressEntryDao.getById(id);
    assertEquals("Should be marked as deleted.", true, ae1.isDeleted());

    addressEntryDao.undelete(ae1);
    ae1 = addressEntryDao.getById(id);
    assertEquals("Should be undeleted.", false, ae1.isDeleted());
  }

  //  @Test(expected = RuntimeException.class)
  //  public void testDelete()
  //  {
  //    AddressEntryDO a1 = new AddressEntryDO();
  //    a1.setName("Not deletable");
  //    a1.setTask(getTask("1.1"));
  //    address2Dao.save(a1);
  //    final Integer id = a1.getId();
  //    a1 = address2Dao.getById(id);
  //    address2Dao.delete(a1);
  //  }

  //  @Test
  //  public void testHistory()
  //  {
  //    final PFUserDO user = getUser(TestBase.ADMIN);
  //    logon(user.getUsername());
  //    Address2DO a1 = new Address2DO();
  //    a1.setName("History test");
  //    a1.setTask(getTask("1.1"));
  //    address2Dao.save(a1);
  //    final Integer id = a1.getId();
  //    a1.setName("History 2");
  //    address2Dao.update(a1);
  //    HistoryEntry[] historyEntries = address2Dao.getHistoryEntries(a1);
  //    assertEquals(2, historyEntries.length);
  //    HistoryEntry entry = historyEntries[0];
  //    log.debug(entry);
  //    assertHistoryEntry(entry, id, user, HistoryEntryType.UPDATE, "name", String.class, "History test", "History 2");
  //    entry = historyEntries[1];
  //    log.debug(entry);
  //    assertHistoryEntry(entry, id, user, HistoryEntryType.INSERT, null, null, null, null);
  //
  //    a1.setTask(getTask("1.2"));
  //    address2Dao.update(a1);
  //    historyEntries = address2Dao.getHistoryEntries(a1);
  //    assertEquals(3, historyEntries.length);
  //    entry = historyEntries[0];
  //    log.debug(entry);
  //    assertHistoryEntry(entry, id, user, HistoryEntryType.UPDATE, "task", TaskDO.class, getTask("1.1").getId(), getTask("1.2").getId());
  //
  //    a1.setTask(getTask("1.1"));
  //    a1.setName("History test");
  //    address2Dao.update(a1);
  //    historyEntries = address2Dao.getHistoryEntries(a1);
  //    assertEquals(4, historyEntries.length);
  //    entry = historyEntries[0];
  //    log.debug(entry);
  //    assertHistoryEntry(entry, id, user, HistoryEntryType.UPDATE, null, null, null, null);
  //    final List<PropertyDelta> delta = entry.getDelta();
  //    assertEquals(2, delta.size());
  //    for (int i = 0; i < 2; i++) {
  //      final PropertyDelta prop = delta.get(0);
  //      if ("name".equals(prop.getPropertyName()) == true) {
  //        assertPropertyDelta(prop, "name", String.class, "History 2", "History test");
  //      } else {
  //        assertPropertyDelta(prop, "task", TaskDO.class, getTask("1.2").getId(), getTask("1.1").getId());
  //      }
  //    }
  //
  //    List<SimpleHistoryEntry> list = address2Dao.getSimpleHistoryEntries(a1);
  //    assertEquals(5, list.size());
  //    for (int i = 0; i < 2; i++) {
  //      final SimpleHistoryEntry se = list.get(i);
  //      if ("name".equals(se.getPropertyName()) == true) {
  //        assertSimpleHistoryEntry(se, user, HistoryEntryType.UPDATE, "name", String.class, "History 2", "History test");
  //      } else {
  //        assertSimpleHistoryEntry(se, user, HistoryEntryType.UPDATE, "task", TaskDO.class, getTask("1.2").getId(), getTask("1.1").getId());
  //      }
  //    }
  //    SimpleHistoryEntry se = list.get(2);
  //    assertSimpleHistoryEntry(se, user, HistoryEntryType.UPDATE, "task", TaskDO.class, getTask("1.1").getId(), getTask("1.2").getId());
  //    se = list.get(3);
  //    assertSimpleHistoryEntry(se, user, HistoryEntryType.UPDATE, "name", String.class, "History test", "History 2");
  //    se = list.get(4);
  //    assertSimpleHistoryEntry(se, user, HistoryEntryType.INSERT, null, null, null, null);
  //
  //    a1 = address2Dao.getById(a1.getId());
  //    final Date date = a1.getLastUpdate();
  //    final String oldName = a1.getName();
  //    a1.setName("Micromata GmbH");
  //    a1.setName(oldName);
  //    address2Dao.update(a1);
  //    a1 = address2Dao.getById(a1.getId());
  //    list = address2Dao.getSimpleHistoryEntries(a1);
  //    assertEquals(5, list.size());
  //    assertEquals(date, a1.getLastUpdate()); // Fails: Fix AbstractBaseDO.copyDeclaredFields: ObjectUtils.equals(Boolean, boolean) etc.
  //  }
  //
  //  @Test
  //  public void checkStandardAccess()
  //  {
  //    Address2DO a1 = new Address2DO();
  //    a1.setName("testa1");
  //    a1.setTask(getTask("ta_1_siud"));
  //    address2Dao.internalSave(a1);
  //    Address2DO a2 = new Address2DO();
  //    a2.setName("testa2");
  //    a2.setTask(getTask("ta_2_siux"));
  //    address2Dao.internalSave(a2);
  //    final Address2DO a3 = new Address2DO();
  //    a3.setName("testa3");
  //    a3.setTask(getTask("ta_3_sxxx"));
  //    address2Dao.internalSave(a3);
  //    final Address2DO a4 = new Address2DO();
  //    a4.setName("testa4");
  //    a4.setTask(getTask("ta_4_xxxx"));
  //    address2Dao.internalSave(a4);
  //    logon(TestBase.TEST_USER);
  //
  //    // Select
  //    try {
  //      address2Dao.getById(a4.getId());
  //      fail("User has no access to select");
  //    } catch (final AccessException ex) {
  //      assertAccessException(ex, getTask("ta_4_xxxx").getId(), AccessType.TASKS, OperationType.SELECT);
  //    }
  //    Address2DO address = address2Dao.getById(a3.getId());
  //    assertEquals("testa3", address.getName());
  //
  //    // Select filter
  //    final BaseSearchFilter searchFilter = new BaseSearchFilter();
  //    searchFilter.setSearchString("testa*");
  //    final QueryFilter filter = new QueryFilter(searchFilter);
  //    filter.addOrder(Order.asc("name"));
  //    final List<Address2DO> result = address2Dao.getList(filter);
  //    assertEquals("Should found 3 address'.", 3, result.size());
  //    final HashSet<String> set = new HashSet<String>();
  //    set.add("testa1");
  //    set.add("testa2");
  //    set.add("testa3");
  //    assertTrue("Hit first entry", set.remove(result.get(0).getName()));
  //    assertTrue("Hit second entry", set.remove(result.get(1).getName()));
  //    assertTrue("Hit third entry", set.remove(result.get(2).getName()));
  //    // test_a4 should not be included in result list (no select access)
  //
  //    // Insert
  //    address = new Address2DO();
  //    address.setName("test");
  //    address2Dao.setTask(address, getTask("ta_4_xxxx").getId());
  //    try {
  //      address2Dao.save(address);
  //      fail("User has no access to insert");
  //    } catch (final AccessException ex) {
  //      assertAccessException(ex, getTask("ta_4_xxxx").getId(), AccessType.TASKS, OperationType.INSERT);
  //    }
  //    address2Dao.setTask(address, getTask("ta_1_siud").getId());
  //    address2Dao.save(address);
  //    assertEquals("test", address.getName());
  //
  //    // Update
  //    a3.setName("test_a3test");
  //    try {
  //      address2Dao.update(a3);
  //      fail("User has no access to update");
  //    } catch (final AccessException ex) {
  //      assertAccessException(ex, getTask("ta_3_sxxx").getId(), AccessType.TASKS, OperationType.UPDATE);
  //    }
  //    a2.setName("testa2test");
  //    address2Dao.update(a2);
  //    address = address2Dao.getById(a2.getId());
  //    assertEquals("testa2test", address.getName());
  //    a2.setName("testa2");
  //    address2Dao.update(a2);
  //    address = address2Dao.getById(a2.getId());
  //    assertEquals("testa2", address.getName());
  //
  //    // Update with moving in task hierarchy
  //    a2.setName("testa2test");
  //    address2Dao.setTask(a2, getTask("ta_1_siud").getId());
  //    try {
  //      address2Dao.update(a2);
  //      fail("User has no access to update");
  //    } catch (final AccessException ex) {
  //      assertAccessException(ex, getTask("ta_2_siux").getId(), AccessType.TASKS, OperationType.DELETE);
  //    }
  //    a2 = address2Dao.getById(a2.getId());
  //    a1.setName("testa1test");
  //    address2Dao.setTask(a1, getTask("ta_5_sxux").getId());
  //    try {
  //      address2Dao.update(a1);
  //      fail("User has no access to update");
  //    } catch (final AccessException ex) {
  //      assertAccessException(ex, getTask("ta_5_sxux").getId(), AccessType.TASKS, OperationType.INSERT);
  //    }
  //    a1 = address2Dao.getById(a1.getId());
  //    assertEquals("testa1", a1.getName());
  //
  //    // Delete
  //    try {
  //      address2Dao.delete(a1);
  //      fail("Address is historizable and should not be allowed to delete.");
  //    } catch (final RuntimeException ex) {
  //      assertEquals(true, ex.getMessage().startsWith(Address2Dao.EXCEPTION_HISTORIZABLE_NOTDELETABLE));
  //    }
  //    try {
  //      address2Dao.markAsDeleted(a2);
  //      fail("User has no access to delete");
  //    } catch (final AccessException ex) {
  //      assertAccessException(ex, getTask("ta_2_siux").getId(), AccessType.TASKS, OperationType.DELETE);
  //    }
  //  }

  //  @Test
  //  public void testInstantMessagingField() throws Exception
  //  {
  //    final Address2DO address = new Address2DO();
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
