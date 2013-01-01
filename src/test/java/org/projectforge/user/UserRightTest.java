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

package org.projectforge.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;
import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.test.TestBase;

public class UserRightTest extends TestBase
{
  private AccessChecker accessChecker;

  private GroupDao groupDao;

  public void setAccessChecker(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  public void setGroupDao(final GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  @Test
  public void testUserDO()
  {
    logon(TEST_ADMIN_USER);
    PFUserDO user = new PFUserDO();
    user.setUsername("UserRightTest");
    user//
        .addRight(new UserRightDO(UserRightId.FIBU_AUSGANGSRECHNUNGEN, UserRightValue.TRUE)) // Invalid setting / value!
        .addRight(new UserRightDO(UserRightId.FIBU_EINGANGSRECHNUNGEN, UserRightValue.READONLY)) //
        .addRight(new UserRightDO(UserRightId.FIBU_DATEV_IMPORT, UserRightValue.FALSE));
    user = userDao.getById(userDao.save(user));
    Set<UserRightDO> rights = user.getRights();
    assertEquals("3 rights added to user", 3, rights.size());
    logon(user.getUsername());
    assertFalse("User not in required groups.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_AUSGANGSRECHNUNGEN, false,
        UserRightValue.TRUE));
    assertFalse("User not in required groups.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_EINGANGSRECHNUNGEN, false,
        UserRightValue.READONLY));
    try {
      accessChecker.hasLoggedInUserRight(UserRightId.FIBU_EINGANGSRECHNUNGEN, true, UserRightValue.READONLY);
      fail("AccessException required.");
    } catch (final AccessException ex) {
      // OK
    }
    assertFalse("Right valid but not available (user is not in fibu group).", accessChecker.hasLoggedInUserRight(
        UserRightId.FIBU_DATEV_IMPORT, false, UserRightValue.FALSE));
    assertFalse("Right valid but not available (user is not in fibu group).", accessChecker.hasLoggedInUserRight(
        UserRightId.FIBU_DATEV_IMPORT, false, UserRightValue.TRUE));
    logon(TEST_ADMIN_USER);
    final GroupDO group = getGroup(ProjectForgeGroup.FINANCE_GROUP.toString());
    group.getAssignedUsers().add(user);
    groupDao.update(group);
    logon(user.getUsername());
    user = userDao.getById(user.getId());
    rights = user.getRights();
    assertEquals("3 rights added to user", 3, rights.size());
    assertTrue("Invalid setting but value matches.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_AUSGANGSRECHNUNGEN, false,
        UserRightValue.TRUE));
    assertTrue("Right matches.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_EINGANGSRECHNUNGEN, false, UserRightValue.READONLY));
    assertTrue("Right valid.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_DATEV_IMPORT, false, UserRightValue.FALSE));
    assertFalse("Right valid.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_DATEV_IMPORT, false, UserRightValue.TRUE));
    try {
      accessChecker.hasLoggedInUserRight(UserRightId.FIBU_DATEV_IMPORT, true, UserRightValue.TRUE);
      fail("AccessException required.");
    } catch (final AccessException ex) {
      // OK
    }
  }

  @Test
  public void testControllingUserDO()
  {
    logon(TEST_ADMIN_USER);
    PFUserDO user = new PFUserDO();
    user.setUsername("UserRightTestControlling");
    user//
        .addRight(new UserRightDO(UserRightId.ORGA_OUTGOING_MAIL, UserRightValue.FALSE)) //
        .addRight(new UserRightDO(UserRightId.FIBU_AUSGANGSRECHNUNGEN, UserRightValue.READONLY)) //
        .addRight(new UserRightDO(UserRightId.FIBU_EINGANGSRECHNUNGEN, UserRightValue.READWRITE)) // Not available
        .addRight(new UserRightDO(UserRightId.FIBU_DATEV_IMPORT, UserRightValue.TRUE));
    user = userDao.getById(userDao.save(user));
    final GroupDO group = getGroup(ProjectForgeGroup.CONTROLLING_GROUP.toString());
    group.getAssignedUsers().add(user);
    groupDao.update(group);
    logon(user.getUsername());
    user = userDao.getById(user.getId());
    Set<UserRightDO> rights = user.getRights();
    assertEquals("4 rights added to user", 4, rights.size());
    assertTrue("Right matches.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_AUSGANGSRECHNUNGEN, false, UserRightValue.READONLY));
    assertFalse("Right matches but not available.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_EINGANGSRECHNUNGEN, false,
        UserRightValue.READWRITE));
    assertTrue("Right valid.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_DATEV_IMPORT, false, UserRightValue.TRUE));
    assertFalse("Right valid.", accessChecker.hasLoggedInUserRight(UserRightId.FIBU_DATEV_IMPORT, false, UserRightValue.FALSE));
    try {
      accessChecker.hasLoggedInUserRight(UserRightId.FIBU_EINGANGSRECHNUNGEN, true, UserRightValue.READWRITE);
      fail("AccessException required.");
    } catch (final AccessException ex) {
      // OK
    }
    assertTrue(accessChecker.hasLoggedInUserReadAccess(UserRightId.FIBU_AUSGANGSRECHNUNGEN, false));
    assertTrue(accessChecker.hasLoggedInUserReadAccess(UserRightId.ORGA_INCOMING_MAIL, false)); // Because only one value is available and
                                                                                                // therefore set
    // for controlling users.
    assertFalse(accessChecker.hasLoggedInUserReadAccess(UserRightId.ORGA_OUTGOING_MAIL, false)); // Also only one value is available but the
                                                                                                 // explicit
    // FALSE setting is taken.
  }

  @Test
  public void testConfigurable()
  {
    final UserRight right = UserRights.instance().getRight(UserRightId.PM_HR_PLANNING);
    logon(TEST_PROJECT_MANAGER_USER);
    assertFalse("Right is not configurable, because all available right values are automatically assigned to the current user", right
        .isConfigurable(userDao.getUserGroupCache(), PFUserContext.getUser()));
    logon(TEST_ADMIN_USER);
    assertFalse("Right is not configurable, because no right values are available.", right.isConfigurable(userDao.getUserGroupCache(),
        PFUserContext.getUser()));
    PFUserDO user = new PFUserDO();
    user.setUsername("testConfigurableRight");
    user = userDao.getById(userDao.save(user));
    GroupDO group = getGroup(ProjectForgeGroup.FINANCE_GROUP.toString());
    group.getAssignedUsers().add(user);
    groupDao.update(group);
    logon(user.getUsername());
    assertTrue("Right is configurable, because serveral right values are available.", right.isConfigurable(userDao.getUserGroupCache(),
        PFUserContext.getUser()));
    logon(TEST_ADMIN_USER);
    group = getGroup(ProjectForgeGroup.PROJECT_MANAGER.toString());
    group.getAssignedUsers().add(user);
    groupDao.update(group);
    logon(user.getUsername());
    assertFalse("Right is not configurable, because all available right values are automatically assigned to the current user", right
        .isConfigurable(userDao.getUserGroupCache(), PFUserContext.getUser()));
  }

  @Test
  public void testHRPlanningRight()
  {
    final UserRight right = UserRights.instance().getRight(UserRightId.PM_HR_PLANNING);
    logon(TEST_PROJECT_MANAGER_USER);
    assertTrue("Right valid.", accessChecker.hasLoggedInUserRight(UserRightId.PM_HR_PLANNING, false, UserRightValue.READWRITE));
    logon(TEST_ADMIN_USER);
    assertFalse("Right invalid.", accessChecker.hasLoggedInUserRight(UserRightId.PM_HR_PLANNING, false, UserRightValue.READWRITE));
    assertFalse("Right is not configurable, because no right values are available.", right.isConfigurable(userDao.getUserGroupCache(),
        PFUserContext.getUser()));
    PFUserDO user = new PFUserDO();
    user.setUsername("testHRPlanningRight");
    user = userDao.getById(userDao.save(user));
    GroupDO group = getGroup(ProjectForgeGroup.CONTROLLING_GROUP.toString());
    group.getAssignedUsers().add(user);
    groupDao.update(group);
    group = getGroup(ProjectForgeGroup.FINANCE_GROUP.toString());
    group.getAssignedUsers().add(user);
    groupDao.update(group);
    logon(user.getUsername());
    assertFalse("Right invalid.", accessChecker.hasLoggedInUserRight(UserRightId.PM_HR_PLANNING, false, UserRightValue.READWRITE));
    assertTrue("Right is configurable, because serveral right values are available.", right.isConfigurable(userDao.getUserGroupCache(),
        PFUserContext.getUser()));
    logon(TEST_ADMIN_USER);
    group = getGroup(ProjectForgeGroup.PROJECT_MANAGER.toString());
    group.getAssignedUsers().add(user);
    groupDao.update(group);
    logon(user.getUsername());
    assertTrue("Right now valid because project managers have always READWRITE access.", accessChecker.hasLoggedInUserRight(
        UserRightId.PM_HR_PLANNING, false, UserRightValue.READWRITE));
    assertFalse("Right is not configurable, because all available right values are automatically assigned to the current user", right
        .isConfigurable(userDao.getUserGroupCache(), PFUserContext.getUser()));
  }
}
