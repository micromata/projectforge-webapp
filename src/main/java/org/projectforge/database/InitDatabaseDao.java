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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.projectforge.access.AccessException;
import org.projectforge.database.xstream.XStreamSavingConverter;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskStatus;
import org.projectforge.task.TaskTree;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRightDO;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For initialization of a new ProjectForge system.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class InitDatabaseDao extends HibernateDaoSupport
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InitDatabaseDao.class);

  static final String TEST_DATA_BASE_DUMP_FILE = "/data/init-test-data.xml.gz";

  public static final String DEFAULT_ADMIN_USER = "admin";

  private DatabaseDao databaseDao;

  private DatabaseUpdateDao databaseUpdateDao;

  private UserGroupCache userGroupCache;

  private GroupDao groupDao;

  private TaskTree taskTree;

  private XmlDump xmlDump;

  private UserDao userDao;

  public void setDatabaseDao(DatabaseDao databaseDao)
  {
    this.databaseDao = databaseDao;
  }

  public void setDatabaseUpdateDao(DatabaseUpdateDao databaseUpdateDao)
  {
    this.databaseUpdateDao = databaseUpdateDao;
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  public void setGroupDao(GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setXmlDump(XmlDump xmlDump)
  {
    this.xmlDump = xmlDump;
  }

  /**
   * If the database is empty (user list is empty) then a admin user and ProjectForge root task will be created.
   * @return
   */
  public PFUserDO initializeEmptyDatabase(final String adminUsername, final String encryptedAdminPassword, final TimeZone adminUserTimezone)
  {
    log.fatal("User wants to initialize database.");
    if (isEmpty() == false) {
      databaseNotEmpty();
    }
    TaskDO task = new TaskDO();
    task.setTitle("Root");
    task.setStatus(TaskStatus.N);
    task.setShortDescription("ProjectForge root task");
    task.setCreated();
    task.setLastUpdate();
    Serializable id = getHibernateTemplate().save(task);
    log.info("New object added (" + id + "): " + task.toString());
    // Use of taskDao does not work with maven test case: Could not synchronize database state with session?

    // Create Admin user
    PFUserDO admin = new PFUserDO();
    admin.setUsername(adminUsername);
    admin.setLastname("Administrator");
    admin.setPassword(encryptedAdminPassword);
    admin.setDescription("ProjectForge administrator");
    admin.setTimeZone(adminUserTimezone);
    admin.addRight(new UserRightDO(UserRightId.FIBU_AUSGANGSRECHNUNGEN, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.FIBU_COST_UNIT, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.FIBU_EINGANGSRECHNUNGEN, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.FIBU_DATEV_IMPORT, UserRightValue.TRUE));
    admin.addRight(new UserRightDO(UserRightId.FIBU_EMPLOYEE_SALARY, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.ORGA_CONTRACTS, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.ORGA_INCOMING_MAIL, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.ORGA_OUTGOING_MAIL, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.PM_PROJECT, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.PM_ORDER_BOOK, UserRightValue.READWRITE));
    admin.addRight(new UserRightDO(UserRightId.PM_HR_PLANNING, UserRightValue.READWRITE));
    userDao.internalSave(admin);

    Set<PFUserDO> adminUsers = new HashSet<PFUserDO>();
    adminUsers.add(admin);
    addGroup(ProjectForgeGroup.ADMIN_GROUP, "Administrators of ProjectForge", adminUsers);
    addGroup(ProjectForgeGroup.CONTROLLING_GROUP, "Users for having read access to the company's finances.", adminUsers);
    addGroup(ProjectForgeGroup.FINANCE_GROUP, "Finance and Accounting", adminUsers);
    addGroup(ProjectForgeGroup.MARKETING_GROUP, "Marketing users can download all addresses in excel format.", null);
    addGroup(ProjectForgeGroup.ORGA_TEAM, "The organization team has access to post in- and outbound, contracts etc..", adminUsers);
    addGroup(ProjectForgeGroup.PROJECT_MANAGER, "Project managers have access to assigned orders and resource planning.", null);
    addGroup(ProjectForgeGroup.PROJECT_ASSISTANT, "Project assistants have access to assigned orders.", null);

    taskTree.setExpired();
    userGroupCache.setExpired();

    log.fatal("Empty database successfully initialized.");
    return admin;
  }

  private void addGroup(ProjectForgeGroup projectForgeGroup, String description, Set<PFUserDO> users)
  {
    GroupDO group = new GroupDO();
    group.setName(projectForgeGroup.toString());
    group.setDescription(description);
    group.setAssignedUsers(users);
    groupDao.internalSave(group);
  }

  public PFUserDO initializeEmptyDatabaseWithTestData(final String adminUsername, final String encryptedAdminPassword,
      final TimeZone adminUserTimezone)
  {
    log.fatal("User wants to initialize database with test data.");
    if (isEmpty() == false) {
      databaseNotEmpty();
    }
    final XStreamSavingConverter xstreamSavingConverter = xmlDump.restoreDatabaseFromClasspathResource(TEST_DATA_BASE_DUMP_FILE, "utf-8");
    xmlDump.verifyDump(xstreamSavingConverter);
    PFUserDO user = userDao.getInternalByName(DEFAULT_ADMIN_USER);
    if (user == null) {
      log.error("Initialization of database failed. Perhaps caused by corrupted init-test-data.xml.gz.");
    } else {
      user.setUsername(adminUsername);
      if (encryptedAdminPassword != null) {
        // Should only be null for test cases.
        user.setPassword(encryptedAdminPassword);
      }
      user.setTimeZone(adminUserTimezone);
      userDao.internalUpdate(user);
      log.fatal("Database successfully initialized with test data.");
    }
    databaseDao.rebuildDatabaseSearchIndices();
    taskTree.setExpired();
    userGroupCache.setExpired();
    return user;
  }

  public boolean isEmpty()
  {
    try {
      if (userGroupCache.internalGetNumberOfUsers() == 0) {
        final Table userTable = new Table(PFUserDO.class);
        return databaseUpdateDao.internalDoesTableExist(userTable.getName()) == false
            || databaseUpdateDao.internalIsTableEmpty(userTable.getName()) == true;
      }
    } catch (final Exception ex) {
      // In the case, that user table is not readable.
      final Table userTable = new Table(PFUserDO.class);
      return databaseUpdateDao.internalDoesTableExist(userTable.getName()) == false
          || databaseUpdateDao.internalIsTableEmpty(userTable.getName()) == true;
    }
    return false;
  }

  private void databaseNotEmpty()
  {
    String msg = "Database seems to be not empty. Initialization of database aborted.";
    log.error(msg);
    throw new AccessException(msg);
  }
}
