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
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.projectforge.access.AccessException;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskStatus;
import org.projectforge.task.TaskTree;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
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

  public static final String DEFAULT_ADMIN_USER = "admin";

  private UserGroupCache userGroupCache;

  private GroupDao groupDao;

  private TaskTree taskTree;

  private XmlDump xmlDump;

  private UserDao userDao;

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

  public PFUserDO initializeEmptyDatabaseWithTestData(final String adminUsername, final String encryptedAdminPassword, final TimeZone adminUserTimezone)
  {
    log.fatal("User wants to initialize database with test data.");
    if (isEmpty() == false) {
      databaseNotEmpty();
    }
    xmlDump.restoreDatabaseFromClasspathResource("/data/init-test-data.xml.gz", "utf-8");
    PFUserDO user = userDao.getInternalByName(DEFAULT_ADMIN_USER);
    if (user == null) {
      log.error("Initialization of database failed. Perhaps caused by corrupted init-test-data.xml.gz.");
    } else {
      user.setUsername(adminUsername);
      user.setPassword(encryptedAdminPassword);
      user.setTimeZone(adminUserTimezone);
      userDao.internalUpdate(user);
      log.fatal("Database successfully initialized with test data.");
    }
    taskTree.setExpired();
    userGroupCache.setExpired();
    return user;
  }

  public boolean isEmpty()
  {
    if (userGroupCache.getNumberOfUsers() == 0) {
      // Additional check for safety:
      final List<PFUserDO> users = userDao.internalLoadAll();
      if (users == null || users.size() == 0) {
        return true;
      }
    }
    return false;
  }

  private void databaseNotEmpty()
  {
    String msg = "Database seems not to be empty. Initialization of database aborted.";
    log.error(msg);
    throw new AccessException(msg);
  }
}
