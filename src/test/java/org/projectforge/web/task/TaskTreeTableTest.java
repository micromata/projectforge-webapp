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

package org.projectforge.web.task;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.projectforge.access.AccessType;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskFilter;
import org.projectforge.test.TestBase;


public class TaskTreeTableTest extends TestBase
{
  private static final Logger log = Logger.getLogger(TaskTreeTableTest.class);
  
  private TaskDao taskDao;
  
  public void setTaskDao(TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }
  
  @Test
  public void testTreeTable()
  {
    initTestDB.addTask("TreeTable1", "root");
    initTestDB.addTask("TreeTable1.1", "TreeTable1");
    initTestDB.addTask("TreeTable1.1.1", "TreeTable1.1");
    initTestDB.addTask("TreeTable1.1.2", "TreeTable1.1");
    initTestDB.addTask("TreeTable1.2", "TreeTable1");
    initTestDB.addTask("TreeTable2", "root");
    initTestDB.addUser("TreeTableTest");
    initTestDB.addGroup("TreeTableTest", new String[] {"TreeTableTest"});
    initTestDB.createGroupTaskAccess(getGroup("TreeTableTest"), getTask("TreeTable1"), AccessType.TASKS, true, true, true, true);
    initTestDB.createGroupTaskAccess(getGroup("TreeTableTest"), getTask("TreeTable2"), AccessType.TASKS, true, true, true, true);
    logon("TreeTableTest");
    TaskTreeTable treeTable = new TaskTreeTable(taskDao.getTaskTree());
    TaskFilter filter = new TaskFilter();
    filter.setOpened(true);
    filter.setNotOpened(true);
    List<TaskTreeTableNode> nodes = treeTable.getNodeList(filter);
    for (TaskTreeTableNode node : nodes) {
      log.debug(node);
    }
    /*assertEquals(2, nodes.size());
    treeTable.setOpenedStatusOfNode("open", getTask("TreeTable1").getId());
    nodes = treeTable.getNodeList(filter);
    assertEquals(4, nodes.size());
    treeTable.setOpenedStatusOfNode("close", getTask("TreeTable1").getId());
    nodes = treeTable.getNodeList(filter);
    assertEquals(2, nodes.size());
    treeTable.setOpenedStatusOfNode("explore", getTask("TreeTable1").getId());
    nodes = treeTable.getNodeList(filter);
    assertEquals(6, nodes.size());
    treeTable.setOpenedStatusOfNode("close", getTask("TreeTable1").getId());
    nodes = treeTable.getNodeList(filter);
    assertEquals(2, nodes.size());
    treeTable.setOpenedStatusOfNode("explore", getTask("TreeTable1").getId());
    treeTable.setOpenedStatusOfNode("implore", getTask("TreeTable1").getId()); // Implore should be the result
    nodes = treeTable.getNodeList(filter);
    assertEquals(2, nodes.size());
    treeTable.setOpenedStatusOfNode("open", getTask("TreeTable1").getId());
    nodes = treeTable.getNodeList(filter);
    assertEquals(4, nodes.size());
    treeTable.setOpenedStatusOfNode("explore", getTask("TreeTable1").getId());
    treeTable.setOpenedStatusOfNode("explore", getTask("TreeTable1").getId()); // Implore should be the result
    treeTable.setOpenedStatusOfNode("open", getTask("TreeTable1").getId());
    nodes = treeTable.getNodeList(filter);
    assertEquals(4, nodes.size());
    treeTable.setOpenedStatusOfNode("close", getTask("TreeTable1").getId());
    TaskDO task = getTask("TreeTable2");
    task.setStatus(TaskStatus.C); // Closing task
    taskDao.update(task);
    nodes = treeTable.getNodeList(filter);
    assertEquals(1, nodes.size());
    task = getTask("TreeTable1");
    task.setShortDescription("Hurzel");
    taskDao.update(task);
    nodes = treeTable.getNodeList(filter);
    assertEquals(1, nodes.size());
    assertEquals("Hurzel", nodes.get(0).getShortDescription());*/
  }
}
