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

package org.projectforge.fibu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.common.DateHolder;
import org.projectforge.core.UserException;
import org.projectforge.test.TestBase;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightDO;
import org.projectforge.user.UserRightDao;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;

public class AuftragDaoTest extends TestBase
{
  private static int dbNumber = AuftragDao.START_NUMBER;

  private AuftragDao auftragDao;

  private ProjektDao projektDao;

  private GroupDao groupDao;

  private UserRightDao userRightDao;

  private Random random = new Random();

  @Test
  public void getNextNumber()
  {
    logon(TEST_FINANCE_USER);
    AuftragDO auftrag = new AuftragDO();
    auftrag.setNummer(auftragDao.getNextNumber(auftrag));
    auftrag.addPosition(new AuftragsPositionDO());
    auftragDao.save(auftrag);
    assertEquals(dbNumber++, auftrag.getNummer().intValue());
    auftrag = new AuftragDO();
    auftrag.setNummer(auftragDao.getNextNumber(auftrag));
    auftrag.addPosition(new AuftragsPositionDO());
    auftragDao.save(auftrag);
    assertEquals(dbNumber++, auftrag.getNummer().intValue());
  }

  @Test
  public void checkAccess()
  {
    logon(TEST_FINANCE_USER);
    AuftragDO auftrag1 = new AuftragDO();
    auftrag1.setNummer(auftragDao.getNextNumber(auftrag1));
    auftragDao.setContactPerson(auftrag1, getUserId(TEST_FINANCE_USER));
    Serializable id1 = null;
    try {
      id1 = auftragDao.save(auftrag1);
      fail("UserException expected: Order should have positions.");
    } catch (UserException ex) {
      assertEquals("fibu.auftrag.error.auftragHatKeinePositionen", ex.getI18nKey());
    }
    auftrag1.addPosition(new AuftragsPositionDO());
    id1 = auftragDao.save(auftrag1);
    dbNumber++; // Needed for getNextNumber test;
    auftrag1 = auftragDao.getById(id1);

    AuftragDO auftrag2 = new AuftragDO();
    auftrag2.setNummer(auftragDao.getNextNumber(auftrag2));
    auftragDao.setContactPerson(auftrag2, getUserId(TEST_PROJECT_MANAGER_USER));
    auftrag2.addPosition(new AuftragsPositionDO());
    Serializable id2 = auftragDao.save(auftrag2);
    dbNumber++; // Needed for getNextNumber test;
    auftrag2 = auftragDao.getById(id2);

    AuftragDO auftrag3 = new AuftragDO();
    auftrag3.setNummer(auftragDao.getNextNumber(auftrag3));
    auftragDao.setContactPerson(auftrag3, getUserId(TEST_PROJECT_MANAGER_USER));
    DateHolder date = new DateHolder();
    date.add(Calendar.YEAR, -2); // 2 years old.
    auftrag3.setAngebotsDatum(date.getSQLDate());
    auftrag3.setAuftragsStatus(AuftragsStatus.ABGESCHLOSSEN);
    AuftragsPositionDO position = new AuftragsPositionDO();
    position.setVollstaendigFakturiert(true);
    position.setStatus(AuftragsPositionsStatus.ABGESCHLOSSEN);
    auftrag3.addPosition(position);
    Serializable id3 = auftragDao.save(auftrag3);
    dbNumber++; // Needed for getNextNumber test;
    auftrag3 = auftragDao.getById(id3);

    logon(TEST_PROJECT_MANAGER_USER);
    try {
      auftragDao.getById(id1);
      fail("AccessException expected: Projectmanager should not have access to foreign orders.");
    } catch (AccessException ex) {
      // OK
    }
    auftragDao.getById(id2);
    try {
      auftragDao.getById(id3);
      fail("AccessException expected: Projectmanager should not have access to 2 years old orders.");
    } catch (AccessException ex) {
      // OK
    }

    logon(TEST_CONTROLLING_USER);
    auftrag1 = auftragDao.getById(id1);
    checkNoWriteAccess(id1, auftrag1, "Controller");

    logon(TEST_USER);
    checkNoAccess(id1, auftrag1, "Other");

    logon(TEST_ADMIN_USER);
    checkNoAccess(id1, auftrag1, "Admin ");
  }

  @Test
  public void checkAccess2()
  {
    logon(TEST_FINANCE_USER);
    final GroupDO group1 = initTestDB.addGroup("AuftragDaoTest.ProjectManagers1", TEST_PROJECT_ASSISTANT_USER);
    final GroupDO group2 = initTestDB.addGroup("AuftragDaoTest.ProjectManagers2", TEST_PROJECT_MANAGER_USER);
    ProjektDO projekt1 = new ProjektDO();
    projekt1.setName("ACME - Webportal 1");
    projekt1.setProjektManagerGroup(group1);
    Serializable id = projektDao.save(projekt1);
    projekt1 = projektDao.getById(id);
    AuftragDO auftrag1 = new AuftragDO();
    auftrag1.setNummer(auftragDao.getNextNumber(auftrag1));
    auftrag1.setProjekt(projekt1);
    auftrag1.addPosition(new AuftragsPositionDO());
    id = auftragDao.save(auftrag1);
    dbNumber++; // Needed for getNextNumber test;
    auftrag1 = auftragDao.getById(id);

    ProjektDO projekt2 = new ProjektDO();
    projekt2.setName("ACME - Webportal 2");
    projekt2.setProjektManagerGroup(group2);
    id = projektDao.save(projekt2);
    projekt2 = projektDao.getById(id);
    AuftragDO auftrag2 = new AuftragDO();
    auftrag2.setNummer(auftragDao.getNextNumber(auftrag2));
    auftrag2.setProjekt(projekt2);
    auftrag2.addPosition(new AuftragsPositionDO());
    id = auftragDao.save(auftrag2);
    dbNumber++; // Needed for getNextNumber test;
    auftrag2 = auftragDao.getById(id);

    logon(TEST_CONTROLLING_USER);
    checkNoWriteAccess(id, auftrag1, "Controlling");

    logon(TEST_USER);
    checkNoAccess(id, auftrag1, "Other");

    logon(TEST_PROJECT_MANAGER_USER);
    projektDao.getList(new ProjektFilter());
    checkNoAccess(auftrag1.getId(), "Project manager");
    checkNoWriteAccess(auftrag1.getId(), auftrag1, "Project manager");
    checkHasUpdateAccess(auftrag2.getId());

    logon(TEST_PROJECT_ASSISTANT_USER);
    projektDao.getList(new ProjektFilter());
    checkHasUpdateAccess(auftrag1.getId());
    checkNoAccess(auftrag2.getId(), "Project assistant");
    checkNoWriteAccess(auftrag2.getId(), auftrag2, "Project assistant");

    logon(TEST_ADMIN_USER);
    checkNoAccess(id, auftrag1, "Admin ");
  }

  @Test
  public void checkPartlyReadwriteAccess()
  {
    logon(TEST_ADMIN_USER);
    final PFUserDO user = initTestDB.addUser("AuftragDaoCheckPartlyReadWriteAccess");
    final GroupDO financeGroup = getGroup(FINANCE_GROUP);
    financeGroup.getSafeAssignedUsers().add(user);
    groupDao.update(financeGroup);
    final GroupDO projectAssistants = getGroup(PROJECT_ASSISTANT);
    projectAssistants.getSafeAssignedUsers().add(user);
    groupDao.update(projectAssistants);

    final GroupDO group = initTestDB.addGroup("AuftragDaoTest.checkPartlyReadwriteAccess");
    logon(TEST_FINANCE_USER);
    ProjektDO projekt = new ProjektDO();
    projekt.setName("ACME - Webportal checkPartlyReadwriteAccess");
    projekt.setProjektManagerGroup(group);
    Serializable id = projektDao.save(projekt);
    projekt = projektDao.getById(id);

    AuftragDO auftrag = new AuftragDO();
    auftrag.setNummer(auftragDao.getNextNumber(auftrag));
    auftrag.setProjekt(projekt);
    auftrag.addPosition(new AuftragsPositionDO());
    id = auftragDao.save(auftrag);
    dbNumber++; // Needed for getNextNumber test;
    auftrag = auftragDao.getById(id);

    logon(user);
    try {
      auftrag = auftragDao.getById(id);
      fail("Access exception expected.");
    } catch (AccessException ex) {
      assertEquals("access.exception.userHasNotRight", ex.getI18nKey());
    }
    logon(TEST_ADMIN_USER);
    user.addRight(new UserRightDO(UserRightId.PM_ORDER_BOOK, UserRightValue.PARTLYREADWRITE)); //
    userDao.update(user);
    logon(user);
    try {
      auftrag = auftragDao.getById(id);
      fail("Access exception expected.");
    } catch (AccessException ex) {
      assertEquals("access.exception.userHasNotRight", ex.getI18nKey());
    }
    logon(TEST_ADMIN_USER);
    UserRightDO right = user.getRight(UserRightId.PM_ORDER_BOOK);
    right.setValue(UserRightValue.READWRITE); // Full access
    userRightDao.update(right);
    logon(user);
    auftrag = auftragDao.getById(id);
    logon(TEST_ADMIN_USER);
    right.setValue(UserRightValue.PARTLYREADWRITE);
    userRightDao.update(right);
    group.getAssignedUsers().add(user);
    groupDao.update(group); // User is now in project manager group.
    logon(user);
    auftrag = auftragDao.getById(id);
  }

  private void checkHasUpdateAccess(Serializable auftragsId)
  {
    AuftragDO auftrag = auftragDao.getById(auftragsId);
    String value = String.valueOf(random.nextLong());
    auftrag.setBemerkung(value);
    auftragDao.update(auftrag);
    auftrag = auftragDao.getById(auftragsId);
    assertEquals(value, auftrag.getBemerkung());
  }

  private void checkNoAccess(String who)
  {
    try {
      AuftragFilter filter = new AuftragFilter();
      auftragDao.getList(filter);
      fail("AccessException expected: " + who + " users should not have select list access to orders.");
    } catch (AccessException ex) {
      // OK
    }
  }

  private void checkNoAccess(Serializable auftragsId, String who)
  {
    try {
      auftragDao.getById(auftragsId);
      fail("AccessException expected: " + who + " users should not have select access to orders.");
    } catch (AccessException ex) {
      // OK
    }
  }

  private void checkNoAccess(Serializable id, AuftragDO auftrag, String who)
  {
    checkNoAccess(who);
    checkNoAccess(id, who);
    checkNoWriteAccess(id, auftrag, who);
  }

  private void checkNoWriteAccess(Serializable id, AuftragDO auftrag, String who)
  {
    try {
      AuftragDO auf = new AuftragDO();
      int number = auftragDao.getNextNumber(auf);
      auf.setNummer(number);
      auftragDao.save(auf);
      fail("AccessException expected: " + who + " users should not have save access to orders.");
    } catch (AccessException ex) {
      // OK
    }
    try {
      auftrag.setBemerkung(who);
      auftragDao.update(auftrag);
      fail("AccessException expected: " + who + " users should not have update access to orders.");
    } catch (AccessException ex) {
      // OK
    }
  }

  @Test
  public void checkVollstaendigFakturiert()
  {
    logon(TEST_FINANCE_USER);
    AuftragDO auftrag1 = new AuftragDO();
    auftrag1.setNummer(auftragDao.getNextNumber(auftrag1));
    auftragDao.setContactPerson(auftrag1, getUserId(TEST_PROJECT_MANAGER_USER));
    auftrag1.addPosition(new AuftragsPositionDO());
    Serializable id1 = auftragDao.save(auftrag1);
    dbNumber++; // Needed for getNextNumber test;
    auftrag1 = auftragDao.getById(id1);

    AuftragsPositionDO position = auftrag1.getPositionen().get(0);
    position.setVollstaendigFakturiert(true);
    try {
      auftragDao.update(auftrag1);
      fail("UserException expected: Only orders with state ABGESCHLOSSEN should be set as fully invoiced.");
    } catch (UserException ex) {
      assertEquals("fibu.auftrag.error.nurAbgeschlosseneAuftragsPositionenKoennenVollstaendigFakturiertSein", ex.getI18nKey());
    }

    auftrag1 = auftragDao.getById(id1);
    auftrag1.setAuftragsStatus(AuftragsStatus.ABGESCHLOSSEN);
    auftragDao.update(auftrag1);
    auftrag1 = auftragDao.getById(id1);

    logon(TEST_PROJECT_MANAGER_USER);
    position = auftrag1.getPositionen().get(0);
    position.setStatus(AuftragsPositionsStatus.ABGESCHLOSSEN);
    position.setVollstaendigFakturiert(true);
    try {
      auftragDao.update(auftrag1);
      fail("AccessException expected: Projectmanager should not able to set order as fully invoiced.");
    } catch (AccessException ex) {
      // OK
      assertEquals("fibu.auftrag.error.vollstaendigFakturiertProtection", ex.getI18nKey());
    }

    logon(TEST_FINANCE_USER);
    position = auftrag1.getPositionen().get(0);
    position.setStatus(AuftragsPositionsStatus.ABGESCHLOSSEN);
    position.setVollstaendigFakturiert(true);
    auftragDao.update(auftrag1);
  }

  @Test
  public void checkEmptyAuftragsPositionen()
  {
    logon(TEST_FINANCE_USER);
    AuftragDO auftrag = new AuftragDO();
    auftrag.setNummer(auftragDao.getNextNumber(auftrag));
    auftrag.addPosition(new AuftragsPositionDO());
    auftrag.addPosition(new AuftragsPositionDO());
    auftrag.addPosition(new AuftragsPositionDO());
    auftrag.addPosition(new AuftragsPositionDO());
    Serializable id = auftragDao.save(auftrag);
    dbNumber++; // Needed for getNextNumber test;
    auftrag = auftragDao.getById(id);
    assertEquals(1, auftrag.getPositionen().size());
    auftrag = new AuftragDO();
    auftrag.setNummer(auftragDao.getNextNumber(auftrag));
    auftrag.addPosition(new AuftragsPositionDO());
    auftrag.addPosition(new AuftragsPositionDO());
    AuftragsPositionDO position = new AuftragsPositionDO();
    position.setTitel("Hurzel");
    auftrag.addPosition(position);
    auftrag.addPosition(new AuftragsPositionDO());
    id = auftragDao.save(auftrag);
    dbNumber++; // Needed for getNextNumber test;
    auftrag = auftragDao.getById(id);
    assertEquals(3, auftrag.getPositionen().size());
    auftrag.getPositionen().get(2).setTitel(null);
    auftragDao.update(auftrag);
    auftrag = auftragDao.getById(id);
    assertEquals(3, auftrag.getPositionen().size());
  }

  public void setAuftragDao(AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
  }

  public void setGroupDao(GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setProjektDao(ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }
  
  public void setUserRightDao(UserRightDao userRightDao)
  {
    this.userRightDao = userRightDao;
  }
}
