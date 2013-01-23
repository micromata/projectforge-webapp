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

package org.projectforge.test;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.projectforge.access.AccessDao;
import org.projectforge.access.AccessEntryDO;
import org.projectforge.access.AccessType;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.common.DateHelper;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2ArtDao;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRightDO;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;

public class InitTestDB
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InitTestDB.class);

  private UserDao userDao;

  private AccessDao accessDao;

  private ConfigurationDao configurationDao;

  private ProjektDao projektDao;

  private Kost2Dao kost2Dao;

  private TaskDao taskDao;

  private TimesheetDao timesheetDao;

  private GroupDao groupDao;

  private UserGroupCache userGroupCache;

  private Kost2ArtDao kost2ArtDao;

  private final Map<String, GroupDO> groupMap = new HashMap<String, GroupDO>();

  private final Map<String, PFUserDO> userMap = new HashMap<String, PFUserDO>();

  private final Map<String, TaskDO> taskMap = new HashMap<String, TaskDO>();

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setUserGroupCache(final UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  public void setAccessDao(final AccessDao accessDao)
  {
    this.accessDao = accessDao;
  }

  public void setConfigurationDao(final ConfigurationDao configurationDao)
  {
    this.configurationDao = configurationDao;
  }

  public void setTaskDao(final TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setTimesheetDao(final TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }

  public void setGroupDao(final GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setKost2Dao(final Kost2Dao kost2Dao)
  {
    this.kost2Dao = kost2Dao;
  }

  public void setKost2ArtDao(final Kost2ArtDao kost2ArtDao)
  {
    this.kost2ArtDao = kost2ArtDao;
  }

  public void setProjektDao(final ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }

  public void putUser(final PFUserDO user)
  {
    this.userMap.put(user.getUsername(), user);
  }

  public PFUserDO addUser(final String username)
  {
    return addUser(username, null);
  }

  public PFUserDO addUser(final String username, final String password)
  {
    final PFUserDO user = new PFUserDO();
    user.setUsername(username);
    if (password != null) {
      user.setPassword(userDao.encryptPassword(password));
    }
    return addUser(user);
  }

  public PFUserDO addUser(final PFUserDO user)
  {
    userDao.internalSave(user);
    putUser(user);
    if (user.getUsername().equals(TestBase.ADMIN) == true) {
      TestBase.ADMIN_USER = user;
    }
    return user;
  }

  public PFUserDO getUser(final String userName)
  {
    return this.userMap.get(userName);
  }

  public void putGroup(final GroupDO group)
  {
    this.groupMap.put(group.getName(), group);
  }

  public GroupDO addGroup(final String groupname, final String... usernames)
  {
    final GroupDO group = new GroupDO();
    group.setName(groupname);
    if (usernames != null) {
      final Set<PFUserDO> col = new HashSet<PFUserDO>();
      for (final String username : usernames) {
        col.add(getUser(username));
      }
      group.setAssignedUsers(col);
    }
    groupDao.internalSave(group);
    putGroup(group);
    userGroupCache.setExpired();
    return group;
  }

  public GroupDO getGroup(final String groupName)
  {
    return this.groupMap.get(groupName);
  }

  public void putTask(final TaskDO task)
  {
    this.taskMap.put(task.getTitle(), task);
  }

  public TaskDO addTask(final String taskName, final String parentTaskName)
  {
    Validate.isTrue(taskName.length() <= TaskDO.TITLE_LENGTH);
    return addTask(taskName, parentTaskName, null);
  }

  public TaskDO addTask(final String taskName, final String parentTaskName, final String shortDescription)
  {
    TaskDO task = new TaskDO();
    task.setTitle(taskName);
    if (parentTaskName != null) {
      task.setParentTask(getTask(parentTaskName));
    }
    if (shortDescription != null) {
      task.setShortDescription(shortDescription);
    }
    final Serializable id = taskDao.internalSave(task);
    task = taskDao.internalGetById(id);
    putTask(task);
    return task;
  }

  public TaskDO getTask(final String taskName)
  {
    return this.taskMap.get(taskName);
  }

  public TimesheetDO addTimesheet(final PFUserDO user, final TaskDO task, final Timestamp startTime, final Timestamp stopTime,
      final String description)
  {
    final TimesheetDO timesheet = new TimesheetDO();
    timesheet.setDescription(description);
    timesheet.setStartTime(startTime);
    timesheet.setStopTime(stopTime);
    timesheet.setTask(task);
    timesheet.setUser(user);
    timesheetDao.internalSave(timesheet);
    return timesheet;
  }

  public ProjektDO addProjekt(final KundeDO kunde, final Integer projektNummer, final String projektName, final Integer... kost2ArtIds)
  {
    final ProjektDO projekt = new ProjektDO();
    projekt.setNummer(projektNummer);
    projekt.setName(projektName);
    projektDao.setKunde(projekt, kunde.getId());
    projektDao.save(projekt);
    if (kost2ArtIds != null) {
      for (final Integer id : kost2ArtIds) {
        final Kost2DO kost2 = new Kost2DO();
        kost2.setProjekt(projekt);
        kost2.setNummernkreis(5);
        kost2.setBereich(kunde.getId());
        kost2.setTeilbereich(projekt.getNummer());
        kost2Dao.setKost2Art(kost2, id);
        kost2Dao.save(kost2);
      }
    }
    return projekt;
  }

  void initDatabase()
  {
    final PFUserDO origUser = PFUserContext.getUser();
    final PFUserDO initUser = new PFUserDO().setUsername("Init-database-pseudo-user");
    initUser.setId(-1);
    PFUserContext.setUser(initUser);
    initConfiguration();
    initUsers();
    initGroups();
    initTaskTree();
    initAccess();
    initKost2Arts();
    PFUserContext.setUser(origUser);
  }

  private void initConfiguration()
  {
    configurationDao.checkAndUpdateDatabaseEntries();
    final ConfigurationDO entry = configurationDao.getEntry(ConfigurationParam.DEFAULT_TIMEZONE);
    entry.setTimeZone(DateHelper.EUROPE_BERLIN);
    configurationDao.internalUpdate(entry);
  }

  private void initUsers()
  {
    addUser(TestBase.ADMIN);
    addUser(TestBase.TEST_ADMIN_USER, TestBase.TEST_ADMIN_USER_PASSWORD);
    PFUserDO user = new PFUserDO();
    user.setUsername(TestBase.TEST_FINANCE_USER);
    user//
    .addRight(new UserRightDO(UserRightId.FIBU_AUSGANGSRECHNUNGEN, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_EINGANGSRECHNUNGEN, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_EMPLOYEE, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_EMPLOYEE_SALARY, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_ACCOUNTS, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_COST_UNIT, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.PM_ORDER_BOOK, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.PM_PROJECT, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.PM_HR_PLANNING, UserRightValue.READWRITE)); //
    addUser(user);
    user = new PFUserDO();
    user.setUsername(TestBase.TEST_FULL_ACCESS_USER);
    user//
    .addRight(new UserRightDO(UserRightId.FIBU_AUSGANGSRECHNUNGEN, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_EINGANGSRECHNUNGEN, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_EMPLOYEE, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_EMPLOYEE_SALARY, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_ACCOUNTS, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.FIBU_COST_UNIT, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.PM_ORDER_BOOK, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.PM_PROJECT, UserRightValue.READWRITE)) //
    .addRight(new UserRightDO(UserRightId.PM_HR_PLANNING, UserRightValue.READWRITE)); //
    user.setPassword(userDao.encryptPassword(TestBase.TEST_FULL_ACCESS_USER_PASSWORD));
    addUser(user);
    addUser(TestBase.TEST_USER, TestBase.TEST_USER_PASSWORD);
    addUser(TestBase.TEST_USER2);
    user = addUser(TestBase.TEST_DELETED_USER, TestBase.TEST_DELETED_USER_PASSWORD);
    userDao.internalMarkAsDeleted(user);
    addUser("user1");
    addUser("user2");
    addUser("user3");
    addUser(TestBase.TEST_CONTROLLING_USER);
    addUser(TestBase.TEST_MARKETING_USER);
    addUser(TestBase.TEST_PROJECT_MANAGER_USER);
    user = new PFUserDO();
    user.setUsername(TestBase.TEST_PROJECT_ASSISTANT_USER);
    user.addRight(new UserRightDO(UserRightId.PM_ORDER_BOOK, UserRightValue.PARTLYREADWRITE));
    addUser(user);
  }

  private void initGroups()
  {
    addGroup(TestBase.ADMIN_GROUP, new String[] { "PFAdmin", TestBase.TEST_ADMIN_USER, TestBase.TEST_FULL_ACCESS_USER});
    addGroup(TestBase.FINANCE_GROUP, new String[] { TestBase.TEST_FINANCE_USER, TestBase.TEST_FULL_ACCESS_USER});
    addGroup(TestBase.CONTROLLING_GROUP, new String[] { TestBase.TEST_CONTROLLING_USER, TestBase.TEST_FULL_ACCESS_USER});
    addGroup(TestBase.ORGA_GROUP);
    addGroup(TestBase.PROJECT_MANAGER, new String[] { TestBase.TEST_PROJECT_MANAGER_USER});
    addGroup(TestBase.PROJECT_ASSISTANT, new String[] { TestBase.TEST_PROJECT_ASSISTANT_USER});
    addGroup(TestBase.MARKETING_GROUP, new String[] { TestBase.TEST_MARKETING_USER});
    addGroup(TestBase.TEST_GROUP, new String[] { TestBase.TEST_USER});
    addGroup("group1", new String[] { "user1", "user2"});
    addGroup("group2", new String[] { "user1"});
    addGroup("group3", new String[] {});
  }

  private void initKost2Arts()
  {
    addKost2Art(0, "Akquise");
    addKost2Art(1, "Research");
    addKost2Art(2, "Realization");
    addKost2Art(3, "Systemadministration");
    addKost2Art(4, "Travel costs");
  }

  private void addKost2Art(final Integer id, final String name)
  {
    final Kost2ArtDO kost2Art = new Kost2ArtDO();
    kost2Art.setId(id);
    kost2Art.setName("Akquise");
    kost2ArtDao.internalSave(kost2Art);
  }

  private void initTaskTree()
  {
    if (log.isDebugEnabled() == true) {
      log.debug("Setting taskTree.expired: " + taskDao.getTaskTree());
    }
    taskDao.getTaskTree().clear();
    if (log.isDebugEnabled() == true) {
      log.debug("TaskTree after reload: " + taskDao.getTaskTree());
    }
    addTask("root", null);
    addTask("1", "root");
    addTask("1.1", "1");
    addTask("1.2", "1");
    addTask("1.1.1", "1.1");
    addTask("1.1.2", "1.1");
    addTask("2", "root");
    addTask("2.1", "2");
    addTask("2.2", "2");
  }

  public GroupTaskAccessDO createGroupTaskAccess(final GroupDO group, final TaskDO task)
  {
    Validate.notNull(group);
    Validate.notNull(task);
    final GroupTaskAccessDO access = new GroupTaskAccessDO();
    access.setGroup(group);
    access.setTask(task);
    accessDao.internalSave(access);
    return access;
  }

  public GroupTaskAccessDO createGroupTaskAccess(final GroupDO group, final TaskDO task, final AccessType accessType,
      final boolean accessSelect, final boolean accessInsert, final boolean accessUpdate, final boolean accessDelete)
  {
    final GroupTaskAccessDO access = createGroupTaskAccess(group, task);
    final AccessEntryDO entry = access.ensureAndGetAccessEntry(accessType);
    entry.setAccess(accessSelect, accessInsert, accessUpdate, accessDelete);
    accessDao.internalUpdate(access);
    return access;
  }

  private void initAccess()
  {
    GroupTaskAccessDO access = createGroupTaskAccess(getGroup("group1"), getTask("1"));
    final AccessEntryDO entry = access.ensureAndGetAccessEntry(AccessType.TASKS);
    entry.setAccess(true, true, true, true);
    accessDao.internalUpdate(access);

    // Create some test tasks with test access:
    addTask("testAccess", "root");
    addTask("ta_1_siud", "testAccess", "Testuser has all access rights: select, insert, update, delete");
    addTask("ta_1_1", "ta_1_siud");
    access = createGroupTaskAccess(getGroup(TestBase.TEST_GROUP), getTask("ta_1_siud"));
    setAllAccessEntries(access, true, true, true, true);
    addTask("ta_2_siux", "testAccess", "Testuser has the access rights: select, insert, update");
    addTask("ta_2_1", "ta_2_siux");
    access = createGroupTaskAccess(getGroup(TestBase.TEST_GROUP), getTask("ta_2_siux"));
    setAllAccessEntries(access, true, true, true, false);
    addTask("ta_3_sxxx", "testAccess", "Testuser has only select rights: select");
    addTask("ta_3_1", "ta_3_sxxx");
    access = createGroupTaskAccess(getGroup(TestBase.TEST_GROUP), getTask("ta_3_sxxx"));
    setAllAccessEntries(access, true, false, false, false);
    addTask("ta_4_xxxx", "testAccess", "Testuser has no rights.");
    addTask("ta_4_1", "ta_4_xxxx");
    access = createGroupTaskAccess(getGroup(TestBase.TEST_GROUP), getTask("ta_4_xxxx"));
    setAllAccessEntries(access, false, false, false, false);
    addTask("ta_5_sxux", "testAccess", "Testuser has select and update rights.");
    addTask("ta_5_1", "ta_5_sxux");
    access = createGroupTaskAccess(getGroup(TestBase.TEST_GROUP), getTask("ta_5_sxux"));
    setAllAccessEntries(access, true, false, true, false);
  }

  private void setAllAccessEntries(final GroupTaskAccessDO access, final boolean selectAccess, final boolean insertAccess,
      final boolean updateAccess, final boolean deleteAccess)
  {
    AccessEntryDO entry = access.ensureAndGetAccessEntry(AccessType.TASK_ACCESS_MANAGEMENT);
    entry.setAccess(selectAccess, insertAccess, updateAccess, deleteAccess);
    entry = access.ensureAndGetAccessEntry(AccessType.TASKS);
    entry.setAccess(selectAccess, insertAccess, updateAccess, deleteAccess);
    entry = access.ensureAndGetAccessEntry(AccessType.TIMESHEETS);
    entry.setAccess(selectAccess, insertAccess, updateAccess, deleteAccess);
    entry = access.ensureAndGetAccessEntry(AccessType.OWN_TIMESHEETS);
    entry.setAccess(selectAccess, insertAccess, updateAccess, deleteAccess);
    accessDao.internalUpdate(access);
  }
}
