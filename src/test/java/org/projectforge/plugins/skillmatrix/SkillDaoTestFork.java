/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.skillmatrix;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.access.AccessException;
import org.projectforge.registry.Registry;
import org.projectforge.test.PluginTestBase;
import org.projectforge.user.PFUserDO;
import org.springframework.beans.BeansException;

public class SkillDaoTestFork extends PluginTestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillDaoTestFork.class);

  private SkillDao skillDao;

  private Integer skillId;

  @BeforeClass
  public static void setup() throws BeansException, IOException
  {
    SkillTestHelper.setup();
  }

  @Test
  public void accessTest()
  {
    final SkillTestHelper testHelper = new SkillTestHelper();
    final SkillDO skill = testHelper.prepareUsersAndGroups("skill", this, getSkillDao());
    skillId = skill.getId();
    logon(testHelper.getOwner());
    Assert.assertEquals("skill.title", skillDao.getById(skillId).getTitle());
    checkSelectAccess(true, testHelper.getOwner(), testHelper.getFullUser1(), testHelper.getReadonlyUser1());
    checkSelectAccess(false, testHelper.getNoAccessUser(), testHelper.getFullUser2(), testHelper.getReadonlyUser2());
  }

  private void checkSelectAccess(final boolean access, final PFUserDO... users)
  {
    for (final PFUserDO user : users) {
      logon(user);
      try {
        Assert.assertEquals("skill.title", skillDao.getById(skillId).getTitle());
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

  private SkillDao getSkillDao()
  {
    if (this.skillDao == null) {
      this.skillDao = Registry.instance().getDao(SkillDao.class);
    }
    return this.skillDao;
  }
}
