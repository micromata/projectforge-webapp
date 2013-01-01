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

package org.projectforge.task;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.projectforge.access.AccessDao;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2ArtDao;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.test.TestBase;


public class TaskKostTest extends TestBase
{
  // private static final Logger log = Logger.getLogger(TaskTest.class);

  TaskDao taskDao;

  AccessDao accessDao;

  ProjektDao projektDao;

  Kost2Dao kost2Dao;

  Kost2ArtDao kost2ArtDao;

  public void setAccessDao(AccessDao accessDao)
  {
    this.accessDao = accessDao;
  }

  public void setTaskDao(TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setProjektDao(ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }

  public void setKost2Dao(Kost2Dao kost2Dao)
  {
    this.kost2Dao = kost2Dao;
  }

  public void setKost2ArtDao(Kost2ArtDao kost2ArtDao)
  {
    this.kost2ArtDao = kost2ArtDao;
  }

  @Test
  public void checkKost2()
  {
    logon(getUser(TEST_FINANCE_USER));
    final TaskTree taskTree = taskDao.getTaskTree();
    final Kost2DO kost2a = kost2Dao.getById(kost2Dao.save(new Kost2DO().setNummernkreis(1).setBereich(137).setTeilbereich(05).setKost2Art(
        new Kost2ArtDO().withId(1)))); // Kost2: 1.137.05.01
    final Kost2DO kost2b = kost2Dao.getById(kost2Dao.save(new Kost2DO().setNummernkreis(1).setBereich(137).setTeilbereich(05).setKost2Art(
        new Kost2ArtDO().withId(2)))); // Kost2: 1.137.05.02
    final Kost2DO kost2c = kost2Dao.getById(kost2Dao.save(new Kost2DO().setNummernkreis(2).setBereich(423).setTeilbereich(12).setKost2Art(
        new Kost2ArtDO().withId(1)))); // Kost2: 2.423.12.01
    final TaskDO task = initTestDB.addTask("kost2test2", "root");
    task.setKost2BlackWhiteList("1.137.05.01, 1.137.05.02, 2.423.12.01");
    taskDao.update(task);
    List<Kost2DO> list = taskTree.getKost2List(task.getId());
    assertEquals(3, list.size());
    assertKost2(kost2a, list.get(0));
    assertKost2(kost2b, list.get(1));
    assertKost2(kost2c, list.get(2));
    task.setKost2BlackWhiteList("1.137.05.01, 1.137.05.02, 2.423.12.01").setKost2IsBlackList(true);
    taskDao.update(task);
    list = taskTree.getKost2List(task.getId());
    assertNull(list);
    task.setKost2BlackWhiteList("1.137.05.01, 1.137.05.02, 2.423.12.01, jwe9jdkjn").setKost2IsBlackList(false);
    taskDao.update(task);
    list = taskTree.getKost2List(task.getId());
    assertEquals(3, list.size());
    assertKost2(kost2a, list.get(0));
    assertKost2(kost2b, list.get(1));
    assertKost2(kost2c, list.get(2));
  }

  @Test
  public void checkProjektKost2()
  {
    logon(getUser(TEST_FINANCE_USER));
    final TaskTree taskTree = taskDao.getTaskTree();
    final TaskDO task = initTestDB.addTask("kost2test1", "root");
    final ProjektDO projekt = projektDao.getById(projektDao.save(new ProjektDO().setName("Kost2 test project").setInternKost2_4(137)
        .setNummer(05).setTask(task))); // Kost2: 4.137.05
    List<Kost2DO> list = taskTree.getKost2List(task.getId());
    assertNull(list);
    final Kost2DO kost2a = kost2Dao.getById(kost2Dao.save(new Kost2DO().setNummernkreis(4).setBereich(137).setTeilbereich(05).setProjekt(
        projekt).setKost2Art(new Kost2ArtDO().withId(1)))); // Kost2: 4.137.05.01
    final Kost2DO kost2b = kost2Dao.getById(kost2Dao.save(new Kost2DO().setNummernkreis(4).setBereich(137).setTeilbereich(05).setProjekt(
        projekt).setKost2Art(new Kost2ArtDO().withId(2)))); // Kost2: 4.137.05.02
    list = taskTree.getKost2List(task.getId());
    assertEquals(2, list.size());
    assertKost2(kost2a, list.get(0));
    assertKost2(kost2b, list.get(1));
    final Kost2DO kost2c = kost2Dao.getById(kost2Dao.save(new Kost2DO().setNummernkreis(4).setBereich(137).setTeilbereich(05).setProjekt(
        projekt).setKost2Art(new Kost2ArtDO().withId(3)))); // Kost2: 4.137.05.03
    final Kost2DO kost2d = kost2Dao.getById(kost2Dao.save(new Kost2DO().setNummernkreis(4).setBereich(137).setTeilbereich(05).setProjekt(
        projekt).setKost2Art(new Kost2ArtDO().withId(4)))); // Kost2: 4.137.05.04
    list = taskTree.getKost2List(task.getId());
    assertEquals(4, list.size());
    assertKost2(kost2a, list.get(0));
    assertKost2(kost2b, list.get(1));
    assertKost2(kost2c, list.get(2));
    assertKost2(kost2d, list.get(3));
    task.setKost2BlackWhiteList("02,3, 5.123.423.11"); // White list
    // 5.123.423.11 will be ignored.
    taskDao.update(task);
    list = taskTree.getKost2List(task.getId());
    assertEquals(2, list.size());
    assertKost2(kost2b, list.get(0));
    assertKost2(kost2c, list.get(1));
    task.setKost2BlackWhiteList("05.02; 4.137.05.03, 5.123.423.11").setKost2IsBlackList(true); // Black list
    // 5.123.423.11 will be ignored.
    taskDao.update(task);
    list = taskTree.getKost2List(task.getId());
    assertEquals(2, list.size());
    assertKost2(kost2a, list.get(0));
    assertKost2(kost2d, list.get(1));
    task.setKost2BlackWhiteList("*").setKost2IsBlackList(true); // Black list (ignore all)
    taskDao.update(task);
    list = taskTree.getKost2List(task.getId());
    assertNull(list);
    task.setKost2BlackWhiteList("-").setKost2IsBlackList(false); // White list
    taskDao.update(task);
    list = taskTree.getKost2List(task.getId());
    assertNull(list);
    task.setKost2BlackWhiteList("*").setKost2IsBlackList(false); // White list
    taskDao.update(task);
    list = taskTree.getKost2List(task.getId());
    assertEquals(4, list.size());
  }

  private void assertKost2(final Kost2DO expected, final Kost2DO actual)
  {
    assertArrayEquals("Kost2DO not expected.", new Integer[] { expected.getNummernkreis(), expected.getBereich(),
        expected.getTeilbereich(), expected.getKost2ArtId()}, new Integer[] { actual.getNummernkreis(), actual.getBereich(),
        actual.getTeilbereich(), actual.getKost2ArtId()});
  }
}
