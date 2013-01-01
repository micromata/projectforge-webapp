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

package org.projectforge.plugins.teamcal.admin;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.plugins.teamcal.TeamCalTestHelper;
import org.projectforge.registry.Registry;
import org.projectforge.test.PluginTestBase;
import org.projectforge.user.PFUserDO;
import org.springframework.beans.BeansException;

public class TeamCalDaoTestFork extends PluginTestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalDaoTestFork.class);

  private TeamCalDao teamCalDao;

  private Integer calId;

  @BeforeClass
  public static void setup() throws BeansException, IOException
  {
    TeamCalTestHelper.setup();
  }

  @Test
  public void accessTest()
  {
    final TeamCalTestHelper testHelper = new TeamCalTestHelper();
    final TeamCalDO cal = testHelper.prepareUsersAndGroups("teamCal", this, getTeamCalDao());
    calId = cal.getId();
    logon(testHelper.getOwner());
    Assert.assertEquals("teamCal.title", teamCalDao.getById(calId).getTitle());
    checkSelectAccess(true, testHelper.getOwner(), testHelper.getFullUser1(), testHelper.getFullUser3(), testHelper.getReadonlyUser1(),
        testHelper.getReadonlyUser3(), testHelper.getMinimalUser1(), testHelper.getMinimalUser3());
    checkSelectAccess(false, testHelper.getNoAccessUser());

    checkUpdateAccess(cal, true, testHelper.getOwner(), getUser(TEST_ADMIN_USER));
    checkUpdateAccess(cal, false, testHelper.getFullUser1(), testHelper.getFullUser3(), testHelper.getReadonlyUser1(),
        testHelper.getReadonlyUser3(), testHelper.getMinimalUser1(), testHelper.getMinimalUser3(), testHelper.getNoAccessUser());
  }

  private void checkSelectAccess(final boolean access, final PFUserDO... users)
  {
    for (final PFUserDO user : users) {
      logon(user);
      try {
        Assert.assertEquals("teamCal.title", teamCalDao.getById(calId).getTitle());
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

  private void checkUpdateAccess(final TeamCalDO cal, final boolean access, final PFUserDO... users)
  {
    for (final PFUserDO user : users) {
      logon(user);
      try {
        cal.setTitle("Calendar of " + user.getUsername());
        teamCalDao.update(cal);
        Assert.assertEquals("Calendar of " + user.getUsername(), teamCalDao.getById(calId).getTitle());
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

  private TeamCalDao getTeamCalDao()
  {
    if (this.teamCalDao == null) {
      this.teamCalDao = (TeamCalDao) Registry.instance().getDao(TeamCalDao.class);
    }
    return this.teamCalDao;
  }
}
