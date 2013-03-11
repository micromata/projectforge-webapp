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

package org.projectforge.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.test.TestBase;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

public class AccessTestFork extends TestBase
{
  private static final Logger log = Logger.getLogger(AccessTestFork.class);

  private AccessDao accessDao;

  private TaskDao taskDao;

  private TimesheetDao timesheetDao;

  private UserGroupCache userGroupCache;

  public void setAccessDao(final AccessDao accessDao)
  {
    this.accessDao = accessDao;
  }

  public void setTaskDao(final TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setTimesheetDao(final TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }

  public void setUserGroupCache(final UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  @Test
  public void testAccessDO()
  {
    logon(TEST_ADMIN_USER);
    final List<GroupTaskAccessDO> list = accessDao.internalLoadAll();
    for (final GroupTaskAccessDO access : list) {
      log.info(access);
    }
    getInitTestDB().addTask("accesstest", "root");
    GroupTaskAccessDO groupTaskAccess = new GroupTaskAccessDO();
    accessDao.setTask(groupTaskAccess, getTask("accesstest").getId());
    groupTaskAccess.setGroup(getGroup(TEST_GROUP));
    final AccessEntryDO taskEntry = groupTaskAccess.ensureAndGetAccessEntry(AccessType.TASKS);
    taskEntry.setAccess(true, true, true, true);
    final AccessEntryDO timesheetEntry = groupTaskAccess.ensureAndGetAccessEntry(AccessType.TIMESHEETS);
    timesheetEntry.setAccess(false, false, false, false);
    final Serializable id = accessDao.save(groupTaskAccess);
    groupTaskAccess = accessDao.getById(id);
    checkAccessEntry(groupTaskAccess.getAccessEntry(AccessType.TASKS), true, true, true, true);
    checkAccessEntry(groupTaskAccess.getAccessEntry(AccessType.TIMESHEETS), false, false, false, false);
    groupTaskAccess.ensureAndGetAccessEntry(AccessType.TIMESHEETS).setAccessSelect(true);
    accessDao.update(groupTaskAccess);
    groupTaskAccess = accessDao.getById(id);
    checkAccessEntry(groupTaskAccess.getAccessEntry(AccessType.TASKS), true, true, true, true);
    checkAccessEntry(groupTaskAccess.getAccessEntry(AccessType.TIMESHEETS), true, false, false, false);
  }

  /**
   * Moves task and checks after moving, if the group task access for the moved tasks are updated.
   */
  @Test
  public void checkTaskMoves()
  {
    // First check initialization:
    final PFUserDO user1 = getUser("user1");
    assertTrue("user1 should be member of group1", userGroupCache.isUserMemberOfGroup(user1.getId(), getGroup("group1").getId()));
    assertFalse("user1 should not be member of group3", userGroupCache.isUserMemberOfGroup(user1.getId(), getGroup("group3").getId()));
    initTestDB.addTask("checkTaskMoves", "root");
    initTestDB.addTask("cTm.1", "checkTaskMoves");
    initTestDB.addTask("cTm.child", "cTm.1");
    initTestDB.addTask("cTm.2", "checkTaskMoves");
    // Full access in task cTm.1
    GroupTaskAccessDO groupTaskAccess = new GroupTaskAccessDO();
    accessDao.setTask(groupTaskAccess, getTask("cTm.1").getId());
    groupTaskAccess.setGroup(getGroup("group1"));
    AccessEntryDO taskEntry = groupTaskAccess.ensureAndGetAccessEntry(AccessType.OWN_TIMESHEETS);
    taskEntry.setAccess(true, true, true, true);
    accessDao.save(groupTaskAccess);
    // No access in task cTm.1
    groupTaskAccess = new GroupTaskAccessDO();
    accessDao.setTask(groupTaskAccess, getTask("cTm.2").getId());
    groupTaskAccess.setGroup(getGroup("group3"));

    taskEntry = groupTaskAccess.ensureAndGetAccessEntry(AccessType.OWN_TIMESHEETS);
    taskEntry.setAccess(false, false, false, false);
    accessDao.save(groupTaskAccess);

    TimesheetDO timesheet = new TimesheetDO();
    timesheet.setTask(initTestDB.getTask("cTm.child"));
    timesheet.setUser(getUser("user1"));
    timesheet.setLocation("Office");
    timesheet.setDescription("A lot of stuff done and more.");
    final long current = System.currentTimeMillis();
    timesheet.setStartTime(new Timestamp(current));
    timesheet.setStopTime(new Timestamp(current + 2 * 60 * 60 * 1000));
    final Serializable id = timesheetDao.internalSave(timesheet);

    logon(user1); // user1 is in group1, but not in group3
    timesheet = timesheetDao.getById(id); // OK, because is selectable for group1
    // Move task ctm.child to cTm.2 with no access to user1:
    final TaskDO childTask = getTask("cTm.child");
    childTask.setParentTask(getTask("cTm.2"));
    taskDao.internalUpdate(childTask);
    // try {
    timesheet = timesheetDao.getById(id); // AccessException, because is not selectable for group1
    // User has no access, but is owner of this timesheet, so the following properties are empty:
    assertEquals("Field should be hidden", TimesheetDao.HIDDEN_FIELD_MARKER, timesheet.getShortDescription());
    assertEquals("Field should be hidden", TimesheetDao.HIDDEN_FIELD_MARKER, timesheet.getDescription());
    assertEquals("Field should be hidden", TimesheetDao.HIDDEN_FIELD_MARKER, timesheet.getLocation());
    // fail("Timesheet should not be accessable for user1 (because he is not member of group3)");
    // } catch (AccessException ex) {
    // OK
    // }
  }

  private void checkAccessEntry(final AccessEntryDO entry, final boolean accessSelect, final boolean accessInsert,
      final boolean accessUpdate, final boolean accessDelete)
  {
    assertNotNull(entry);
    assertEquals(accessSelect, entry.getAccessSelect());
    assertEquals(accessInsert, entry.getAccessInsert());
    assertEquals(accessUpdate, entry.getAccessUpdate());
    assertEquals(accessDelete, entry.getAccessDelete());
  }
}
