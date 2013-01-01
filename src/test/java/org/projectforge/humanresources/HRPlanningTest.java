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

package org.projectforge.humanresources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.access.OperationType;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.test.TestBase;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRightDO;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

public class HRPlanningTest extends TestBase
{
  private static ProjektDO projekt1, projekt2;

  private GroupDao groupDao;

  private HRPlanningDao hrPlanningDao;

  private KundeDao kundeDao;

  private UserGroupCache userGroupCache;

  private static boolean initialized;

  public void setGroupDao(GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setHrPlanningDao(HRPlanningDao hrPlanningDao)
  {
    this.hrPlanningDao = hrPlanningDao;
  }

  public void setKundeDao(KundeDao kundeDao)
  {
    this.kundeDao = kundeDao;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  @Before
  public void createProjekts()
  {
    if (initialized == true) {
      return;
    }
    logon(TestBase.TEST_FINANCE_USER);
    final KundeDO kunde = new KundeDO();
    kunde.setName("ACME ltd.");
    kunde.setId(59);
    kundeDao.save(kunde);
    projekt1 = initTestDB.addProjekt(kunde, 0, "Web portal");
    projekt2 = initTestDB.addProjekt(kunde, 1, "Order management");
    initialized = true;
  }

  @Test
  public void testUserRights()
  {
    PFUserDO user1 = initTestDB.addUser("HRPlanningTestUser1");
    final HRPlanningRight right = (HRPlanningRight) UserRights.instance().getRight(UserRightId.PM_HR_PLANNING);
    assertFalse(right.isAvailable(userGroupCache, user1));
    final HRPlanningDO planning = new HRPlanningDO().setUser(getUser(TestBase.TEST_USER));
    logon(user1);
    assertFalse(hrPlanningDao.hasLoggedInUserAccess(planning, null, OperationType.SELECT, false));
    try {
      hrPlanningDao.hasLoggedInUserAccess(planning, null, OperationType.SELECT, true);
      fail("AccessException excepted.");
    } catch (AccessException ex) {
      // OK
    }
    logon(TestBase.TEST_ADMIN_USER);
    GroupDO group = initTestDB.getGroup(ORGA_GROUP);
    group.getAssignedUsers().add(user1);
    groupDao.update(group);
    assertTrue(right.isAvailable(userGroupCache, user1));
    logon(user1);
    assertFalse(hrPlanningDao.hasLoggedInUserAccess(planning, null, OperationType.SELECT, false));
    assertTrue(accessChecker.hasLoggedInUserSelectAccess(UserRightId.PM_HR_PLANNING, false));
    assertFalse(accessChecker.hasLoggedInUserSelectAccess(UserRightId.PM_HR_PLANNING, planning, false));
    assertFalse(accessChecker.hasLoggedInUserHistoryAccess(UserRightId.PM_HR_PLANNING, planning, false));
    assertFalse(accessChecker.hasLoggedInUserInsertAccess(UserRightId.PM_HR_PLANNING, planning, false));
    logon(TestBase.TEST_ADMIN_USER);
    user1.addRight(new UserRightDO(user1, UserRightId.PM_HR_PLANNING, UserRightValue.READONLY));
    userDao.update(user1);
    logon(user1);
    assertTrue(hrPlanningDao.hasLoggedInUserAccess(planning, null, OperationType.SELECT, false));
    assertTrue(accessChecker.hasLoggedInUserSelectAccess(UserRightId.PM_HR_PLANNING, planning, false));
    assertTrue(accessChecker.hasLoggedInUserHistoryAccess(UserRightId.PM_HR_PLANNING, planning, false));
    assertFalse(accessChecker.hasLoggedInUserInsertAccess(UserRightId.PM_HR_PLANNING, planning, false));
    logon(TestBase.TEST_ADMIN_USER);
    user1 = userDao.getById(user1.getId());
    user1.getRight(UserRightId.PM_HR_PLANNING).setValue(UserRightValue.READWRITE);
    userDao.update(user1);
    logon(user1);
    assertTrue(hrPlanningDao.hasLoggedInUserAccess(planning, null, OperationType.SELECT, false));
    assertTrue(accessChecker.hasLoggedInUserSelectAccess(UserRightId.PM_HR_PLANNING, planning, false));
    assertTrue(accessChecker.hasLoggedInUserHistoryAccess(UserRightId.PM_HR_PLANNING, planning, false));
    assertTrue(accessChecker.hasLoggedInUserInsertAccess(UserRightId.PM_HR_PLANNING, planning, false));
  }

  @Test
  public void getFirstDayOfWeek()
  {
    final java.sql.Date date = createDate(2010, Calendar.JANUARY, 9, 1, 10, 57, 456);
    assertEquals("2010-01-04 00:00:00.000 +0000", DateHelper.formatAsUTC(HRPlanningDO.getFirstDayOfWeek(date)));
  }

  @Test
  public void testBeginOfWeek()
  {
    logon(TestBase.TEST_FINANCE_USER);
    HRPlanningDO planning = new HRPlanningDO();
    final java.sql.Date date = createDate(2010, Calendar.JANUARY, 9, 1, 10, 57, 456);
    final DateHolder firstDayOfWeek = new DateHolder(DateHelper.UTC);
    firstDayOfWeek.setDate(2010, Calendar.JANUARY, 4, 0, 0, 0, 0);
    final long millis = firstDayOfWeek.getTimeInMillis();
    planning.setFirstDayOfWeek(date);
    assertEquals("2010-01-04 00:00:00.000 +0000", DateHelper.formatAsUTC(planning.getWeek()));
    assertEquals(millis, planning.getWeek().getTime());
    // planning.setWeek(date);
    planning.setUser(getUser(TestBase.TEST_USER));
    assertEquals("2010-01-04 00:00:00.000 +0000", DateHelper.formatAsUTC(planning.getWeek()));
    final Serializable id = hrPlanningDao.save(planning);
    planning = hrPlanningDao.getById(id);
    assertEquals("2010-01-04 00:00:00.000 +0000", DateHelper.formatAsUTC(planning.getWeek()));
  }

  @Test
  public void overwriteDeletedEntries()
  {
    logon(TestBase.TEST_FINANCE_USER);
    // Create planning:
    HRPlanningDO planning = new HRPlanningDO();
    planning.setUser(getUser(TEST_USER));
    planning.setWeek(createDate(2010, Calendar.JANUARY, 11, 0, 0, 0, 0));
    assertUTCDate(planning.getWeek(), 2010, Calendar.JANUARY, 11, 0, 0, 0);
    HRPlanningEntryDO entry = new HRPlanningEntryDO();
    setHours(entry, 1, 2, 3, 4, 5, 6);
    entry.setProjekt(projekt1);
    planning.addEntry(entry);
    entry = new HRPlanningEntryDO();
    setHours(entry, 2, 4, 6, 8, 10, 12);
    entry.setStatus(HRPlanningEntryStatus.OTHER);
    planning.addEntry(entry);
    entry = new HRPlanningEntryDO();
    setHours(entry, 6, 5, 4, 3, 2, 1);
    entry.setProjekt(projekt2);
    planning.addEntry(entry);
    Serializable id = hrPlanningDao.save(planning);
    // Check saved planning
    planning = hrPlanningDao.getById(id);
    assertUTCDate(planning.getWeek(), 2010, Calendar.JANUARY, 11, 0, 0, 0);
    assertEquals(3, planning.getEntries().size());
    assertHours(planning.getProjectEntry(projekt1), 1, 2, 3, 4, 5, 6);
    assertHours(planning.getProjectEntry(projekt2), 6, 5, 4, 3, 2, 1);
    assertHours(planning.getStatusEntry(HRPlanningEntryStatus.OTHER), 2, 4, 6, 8, 10, 12);
    // Delete entry
    planning.getProjectEntry(projekt1).setDeleted(true);
    hrPlanningDao.update(planning);
    // Check deleted entry and re-adding it
    planning = hrPlanningDao.getById(id);
    assertTrue(planning.getProjectEntry(projekt1).isDeleted());
    entry = new HRPlanningEntryDO();
    setHours(entry, 7, 9, 11, 1, 3, 5);
    entry.setProjekt(projekt1);
    planning.addEntry(entry);
    hrPlanningDao.update(planning);
  }

  private void setHours(final HRPlanningEntryDO entry, final int monday, final int tuesday, final int wednesday, final int thursday,
      final int friday, final int weekend)
  {
    entry.setMondayHours(new BigDecimal(monday));
    entry.setTuesdayHours(new BigDecimal(tuesday));
    entry.setWednesdayHours(new BigDecimal(wednesday));
    entry.setThursdayHours(new BigDecimal(thursday));
    entry.setFridayHours(new BigDecimal(friday));
    entry.setWeekendHours(new BigDecimal(weekend));
  }

  private void assertHours(final HRPlanningEntryDO entry, final int monday, final int tuesday, final int wednesday, final int thursday,
      final int friday, final int weekend)
  {
    assertBigDecimal(monday, entry.getMondayHours());
    assertBigDecimal(tuesday, entry.getTuesdayHours());
    assertBigDecimal(wednesday, entry.getWednesdayHours());
    assertBigDecimal(thursday, entry.getThursdayHours());
    assertBigDecimal(friday, entry.getFridayHours());
    assertBigDecimal(weekend, entry.getWeekendHours());
  }

  private java.sql.Date createDate(int year, int month, int day, int hour, int minute, int second, int millisecond)
  {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.GERMAN);
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, second);
    cal.set(Calendar.MILLISECOND, millisecond);
    return new java.sql.Date(cal.getTimeInMillis());
  }
}
