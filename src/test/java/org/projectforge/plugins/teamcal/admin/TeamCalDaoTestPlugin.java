/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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
import org.projectforge.plugins.teamcal.TeamCalPlugin;
import org.projectforge.registry.Registry;
import org.projectforge.test.PluginTestBase;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.springframework.beans.BeansException;

public class TeamCalDaoTestPlugin extends PluginTestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalDaoTestPlugin.class);

  private TeamCalDao teamCalDao;

  private Integer calId;

  @BeforeClass
  public static void setup() throws BeansException, IOException
  {
    init("org/projectforge/plugins/teamcal/pluginContext.xml", new TeamCalPlugin());
  }

  @Test
  public void accessTest()
  {
    logon(TEST_ADMIN_USER);
    final PFUserDO owner = initTestDB.addUser("TeamCalOwnerUser");
    final PFUserDO fullUser1 = initTestDB.addUser("TeamCalFullUser1");
    final PFUserDO fullUser2 = initTestDB.addUser("TeamCalFullUser2");
    final PFUserDO fullUser3 = initTestDB.addUser("TeamCalFullUser3");
    final PFUserDO readonlyUser1 = initTestDB.addUser("TeamCalReadonlyUser1");
    final PFUserDO readonlyUser2 = initTestDB.addUser("TeamCalReadonlyUser2");
    final PFUserDO readonlyUser3 = initTestDB.addUser("TeamCalReadonlyUser3");
    final PFUserDO minimalUser1 = initTestDB.addUser("TeamCalMinimalUser1");
    final PFUserDO minimalUser2 = initTestDB.addUser("TeamCalMinimalUser2");
    final PFUserDO minimalUser3 = initTestDB.addUser("TeamCalMinimalUser3");
    final PFUserDO noAccessUser = initTestDB.addUser("TeamCalNoAccessUser");

    final GroupDO fullGroup1 = initTestDB.addGroup("TeamCalFullGroup1", fullUser1.getUsername());
    final GroupDO readonlyGroup1 = initTestDB.addGroup("TeamCalReadonlyGroup1", readonlyUser1.getUsername());
    final GroupDO minimalGroup1 = initTestDB.addGroup("TeamCalMinimalGroup", minimalUser1.getUsername());

    logon(owner);
    final TeamCalDO cal = new TeamCalDO();
    cal.setOwner(owner);
    cal.setFullAccessGroupIds("" + fullGroup1.getId());
    cal.setReadonlyAccessGroupIds("" + readonlyGroup1.getId());
    cal.setMinimalAccessGroupIds("" + minimalGroup1.getId());
    cal.setFullAccessUserIds("" + fullUser3.getId());
    cal.setReadonlyAccessUserIds("" + readonlyUser3.getId());
    cal.setMinimalAccessUserIds("" + minimalUser3.getId());
    cal.setTitle("title");
    calId = (Integer) getTeamCalDao().save(cal);
    Assert.assertEquals("title", teamCalDao.getById(calId).getTitle());
    checkSelectAccess(true, fullUser1, fullUser3, readonlyUser1, readonlyUser3, minimalUser1, minimalUser3);
    checkSelectAccess(false, noAccessUser);

    checkUpdateAccess(cal, true, owner, getUser(TEST_ADMIN_USER));
    checkUpdateAccess(cal, false, fullUser1, fullUser3, readonlyUser1, readonlyUser3, minimalUser1, minimalUser3, noAccessUser);
  }

  private void checkSelectAccess(final boolean access, final PFUserDO... users)
  {
    for (final PFUserDO user : users) {
      logon(user);
      try {
        Assert.assertEquals("title", teamCalDao.getById(calId).getTitle());
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
