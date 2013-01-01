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

package org.projectforge.plugins.teamcal;

import java.io.IOException;

import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.test.AbstractTestBase;
import org.projectforge.test.PluginTestBase;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.springframework.beans.BeansException;

public class TeamCalTestHelper
{
  private PFUserDO owner, fullUser1, fullUser2, fullUser3, readonlyUser1, readonlyUser2, readonlyUser3, minimalUser1, minimalUser2,
  minimalUser3, noAccessUser;

  private GroupDO fullGroup1, readonlyGroup1, minimalGroup1;

  public static void setup() throws BeansException, IOException
  {
    PluginTestBase.init("org/projectforge/plugins/teamcal/pluginContext.xml", new TeamCalPlugin());
  }

  public TeamCalDO prepareUsersAndGroups(final String prefix, final AbstractTestBase testBase, final TeamCalDao teamCalDao)
  {
    testBase.logon(AbstractTestBase.TEST_ADMIN_USER);
    owner = AbstractTestBase.initTestDB.addUser(prefix + "OwnerUser");
    fullUser1 = AbstractTestBase.initTestDB.addUser(prefix + "FullUser1");
    fullUser2 = AbstractTestBase.initTestDB.addUser(prefix + "FullUser2");
    fullUser3 = AbstractTestBase.initTestDB.addUser(prefix + "FullUser3");
    readonlyUser1 = AbstractTestBase.initTestDB.addUser(prefix + "ReadonlyUser1");
    readonlyUser2 = AbstractTestBase.initTestDB.addUser(prefix + "ReadonlyUser2");
    readonlyUser3 = AbstractTestBase.initTestDB.addUser(prefix + "ReadonlyUser3");
    minimalUser1 = AbstractTestBase.initTestDB.addUser(prefix + "MinimalUser1");
    minimalUser2 = AbstractTestBase.initTestDB.addUser(prefix + "MinimalUser2");
    minimalUser3 = AbstractTestBase.initTestDB.addUser(prefix + "MinimalUser3");
    noAccessUser = AbstractTestBase.initTestDB.addUser(prefix + "NoAccessUser");

    fullGroup1 = AbstractTestBase.initTestDB.addGroup(prefix + "FullGroup1", fullUser1.getUsername());
    readonlyGroup1 = AbstractTestBase.initTestDB.addGroup(prefix + "ReadonlyGroup1", readonlyUser1.getUsername());
    minimalGroup1 = AbstractTestBase.initTestDB.addGroup(prefix + "MinimalGroup", minimalUser1.getUsername());

    testBase.logon(owner);
    final TeamCalDO cal = new TeamCalDO();
    cal.setOwner(owner);
    cal.setFullAccessGroupIds("" + fullGroup1.getId());
    cal.setReadonlyAccessGroupIds("" + readonlyGroup1.getId());
    cal.setMinimalAccessGroupIds("" + minimalGroup1.getId());
    cal.setFullAccessUserIds("" + fullUser3.getId());
    cal.setReadonlyAccessUserIds("" + readonlyUser3.getId());
    cal.setMinimalAccessUserIds("" + minimalUser3.getId());
    cal.setTitle(prefix + ".title");
    final Integer calId = (Integer) teamCalDao.save(cal);
    return teamCalDao.getById(calId);
  }

  public PFUserDO getOwner()
  {
    return owner;
  }

  public PFUserDO getFullUser1()
  {
    return fullUser1;
  }

  public PFUserDO getFullUser2()
  {
    return fullUser2;
  }

  public PFUserDO getFullUser3()
  {
    return fullUser3;
  }

  public PFUserDO getReadonlyUser1()
  {
    return readonlyUser1;
  }

  public PFUserDO getReadonlyUser2()
  {
    return readonlyUser2;
  }

  public PFUserDO getReadonlyUser3()
  {
    return readonlyUser3;
  }

  public PFUserDO getMinimalUser1()
  {
    return minimalUser1;
  }

  public PFUserDO getMinimalUser2()
  {
    return minimalUser2;
  }

  public PFUserDO getMinimalUser3()
  {
    return minimalUser3;
  }

  public PFUserDO getNoAccessUser()
  {
    return noAccessUser;
  }

  public GroupDO getFullGroup1()
  {
    return fullGroup1;
  }

  public GroupDO getReadonlyGroup1()
  {
    return readonlyGroup1;
  }

  public GroupDO getMinimalGroup1()
  {
    return minimalGroup1;
  }
}
