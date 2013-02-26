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

package org.projectforge.plugins.teamcal.event;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.common.DateHelperTest;
import org.projectforge.plugins.teamcal.TeamCalTestHelper;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.registry.Registry;
import org.projectforge.test.PluginTestBase;
import org.projectforge.user.PFUserDO;
import org.springframework.beans.BeansException;

public class TeamEventDaoTestFork extends PluginTestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventDaoTestFork.class);

  private TeamCalDao teamCalDao;

  private TeamEventDao teamEventDao;

  private Integer calId, eventId;

  @BeforeClass
  public static void setup() throws BeansException, IOException
  {
    TeamCalTestHelper.setup();
  }

  @Test
  public void accessTest()
  {
    final TeamCalTestHelper testHelper = new TeamCalTestHelper();
    final TeamCalDO cal = testHelper.prepareUsersAndGroups("teamEvent", this, getTeamCalDao());
    calId = cal.getId();
    logon(testHelper.getOwner());
    final TeamEventDO event = new TeamEventDO();
    event.setStartDate(new Timestamp(DateHelperTest.createDate(2012, Calendar.DECEMBER, 8, 8, 0, 0, 0).getTime()));
    event.setEndDate(new Timestamp(DateHelperTest.createDate(2012, Calendar.DECEMBER, 8, 15, 0, 0, 0).getTime()));
    event.setSubject("Testing the event dao.");
    event.setAttendees(new TreeSet<TeamEventAttendeeDO>());
    event.getAttendees().add(new TeamEventAttendeeDO().setUrl("k.reinhard@acme.com"));
    event.setLocation("At home").setNote("This is a note.");
    try {
      log.info("Next AccessException is expected:");
      teamEventDao.save(event);
      Assert.fail("AccessException expected, no calendar given in event.");
    } catch (final AccessException ex) {
      // OK
    }
    event.setCalendar(cal);
    eventId = (Integer) teamEventDao.save(event);

    checkSelectAccess(true, testHelper.getOwner(), testHelper.getFullUser1(), testHelper.getFullUser3(), testHelper.getReadonlyUser1(),
        testHelper.getReadonlyUser3());
    checkSelectAccess(false, testHelper.getNoAccessUser());

    checkUpdateAccess(event, true, testHelper.getOwner(), testHelper.getFullUser1(), testHelper.getFullUser3());
    checkUpdateAccess(event, false, testHelper.getReadonlyUser1(), testHelper.getReadonlyUser3(), testHelper.getMinimalUser1(),
        testHelper.getMinimalUser3(), testHelper.getNoAccessUser(), getUser(TEST_ADMIN_USER));
    checkMinimalAccess(eventId, testHelper.getMinimalUser1(), testHelper.getMinimalUser3());
  }

  private void checkSelectAccess(final boolean access, final PFUserDO... users)
  {
    for (final PFUserDO user : users) {
      logon(user);
      try {
        Assert.assertEquals("Testing the event dao.", teamEventDao.getById(eventId).getSubject());
        if (access == false) {
          Assert.fail("Select-AccessException expected for user: " + user.getUsername());
        }
      } catch (final AccessException ex) {
        if (access == true) {
          Assert.fail("Unexpected Selected-AccessException for user: " + user.getUsername());
        } else {
          log.info("Last AccessException was expected (OK).");
        }
      }
    }
  }

  private void checkUpdateAccess(final TeamEventDO event, final boolean access, final PFUserDO... users)
  {
    for (final PFUserDO user : users) {
      logon(user);
      try {
        event.setSubject("Event of " + user.getUsername());
        teamEventDao.update(event);
        Assert.assertEquals("Event of " + user.getUsername(), teamEventDao.getById(calId).getSubject());
        if (access == false) {
          Assert.fail("Update-AccessException expected for user: " + user.getUsername());
        }
      } catch (final AccessException ex) {
        if (access == true) {
          Assert.fail("Unexpected Update-AccessException for user: " + user.getUsername());
        } else {
          log.info("Last AccessException was expected (OK).");
        }
      }
    }
  }

  private void checkMinimalAccess(final Integer eventId, final PFUserDO... users)
  {
    for (final PFUserDO user : users) {
      logon(user);
      final TeamEventDO event = teamEventDao.getById(eventId);
      Assert.assertNull("Field should be null for minimal users.", event.getAttendees());
      Assert.assertNull("Field should be null for minimal users.", event.getSubject());
      Assert.assertNull("Field should be null for minimal users.", event.getLocation());
      Assert.assertNull("Field should be null for minimal users.", event.getNote());
    }
  }

  private TeamCalDao getTeamCalDao()
  {
    if (this.teamCalDao == null) {
      this.teamCalDao = Registry.instance().getDao(TeamCalDao.class);
    }
    return this.teamCalDao;
  }

  /**
   * @param teamEventDao the teamEventDao to set
   * @return this for chaining.
   */
  public void setTeamEventDao(final TeamEventDao teamEventDao)
  {
    this.teamEventDao = teamEventDao;
  }
}
