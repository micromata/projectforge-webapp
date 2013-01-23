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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.list.UnmodifiableList;
import org.projectforge.access.AccessChecker;
import org.projectforge.fibu.AuftragRight;
import org.projectforge.fibu.ProjektRight;
import org.projectforge.gantt.GanttChartRight;
import org.projectforge.humanresources.HRPlanningRight;
import org.projectforge.meb.MebRight;

public class UserRights
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserRights.class);
  /**
   * FALSE, TRUE;
   */
  public static final UserRightValue[] FALSE_TRUE = new UserRightValue[] { UserRightValue.FALSE, UserRightValue.TRUE};

  /**
   * FALSE, READONLY, READWRITE;
   */
  public static final UserRightValue[] FALSE_READONLY_READWRITE = new UserRightValue[] { UserRightValue.FALSE, UserRightValue.READONLY,
    UserRightValue.READWRITE};

  /**
   * FALSE, READONLY, PARTLY_READWRITE, READWRITE;
   */
  public static final UserRightValue[] FALSE_READONLY_PARTLYREADWRITE_READWRITE = new UserRightValue[] { UserRightValue.FALSE,
    UserRightValue.READONLY, UserRightValue.PARTLYREADWRITE, UserRightValue.READWRITE};

  /**
   * READONLY, READWRITE;
   */
  public static final UserRightValue[] READONLY_READWRITE = new UserRightValue[] { UserRightValue.READONLY, UserRightValue.READWRITE};

  /**
   * READONLY, PARTY_READWRITE, READWRITE;
   */
  public static final UserRightValue[] READONLY_PARTLYREADWRITE_READWRITE = new UserRightValue[] { UserRightValue.READONLY, UserRightValue.PARTLYREADWRITE, UserRightValue.READWRITE};

  public static final ProjectForgeGroup[] FIBU_GROUPS = { ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP};

  public static final ProjectForgeGroup[] FIBU_ORGA_GROUPS = { ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.ORGA_TEAM,
    ProjectForgeGroup.CONTROLLING_GROUP};

  public static final ProjectForgeGroup[] FIBU_ORGA_PM_GROUPS = { ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.ORGA_TEAM,
    ProjectForgeGroup.CONTROLLING_GROUP, ProjectForgeGroup.PROJECT_MANAGER, ProjectForgeGroup.PROJECT_ASSISTANT};

  private static UserRights instance;

  private final Map<UserRightId, UserRight> rights = new HashMap<UserRightId, UserRight>();

  private final Map<String, UserRightId> rightIds = new HashMap<String, UserRightId>();

  private final List<UserRight> orderedRights = new ArrayList<UserRight>();

  private final AccessChecker accessChecker;

  public static UserRights instance()
  {
    return instance;
  }

  public static AccessChecker getAccessChecker()
  {
    if (instance == null) {
      throw new IllegalStateException("UserRights not yet initalized, so AccessChecker is not yet available.");
    }
    return instance.accessChecker;
  }

  public static UserGroupCache getUserGroupCache()
  {
    return getAccessChecker().getUserGroupCache();
  }

  public static UserRights initialize(final AccessChecker accessChecker)
  {
    if (instance != null) {
      // log.warn("UserRights already initialized. Method initialized was called twice!");
      return instance;
    }
    instance = new UserRights(accessChecker);
    return instance;
  }

  public UserRight getRight(final UserRightId id)
  {
    return rights.get(id);
  }

  public UserRightId getRightId(final String userRightId)
  {
    final UserRightId rightId =  rightIds.get(userRightId);
    if (rightId == null) {
      log.warn("UserRightId with id '" + userRightId + "' not found.");
    }
    return rightId;
  }

  @SuppressWarnings("unchecked")
  public List<UserRight> getOrderedRights()
  {
    return UnmodifiableList.decorate(orderedRights);
  }

  private UserRights(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;



    addRight(UserRightCategory.FIBU, UserRightId.FIBU_EMPLOYEE, FALSE_READONLY_READWRITE, FIBU_ORGA_GROUPS).setReadOnlyForControlling();
    addRight(UserRightCategory.FIBU, UserRightId.FIBU_EMPLOYEE_SALARY, FALSE_READONLY_READWRITE, FIBU_GROUPS).setAvailableGroupRightValues(
        ProjectForgeGroup.CONTROLLING_GROUP, UserRightValue.FALSE, UserRightValue.READONLY);
    addRight(UserRightCategory.FIBU, UserRightId.FIBU_AUSGANGSRECHNUNGEN, FALSE_READONLY_READWRITE, FIBU_ORGA_GROUPS)
    .setReadOnlyForControlling();
    addRight(UserRightCategory.FIBU, UserRightId.FIBU_EINGANGSRECHNUNGEN, FALSE_READONLY_READWRITE, FIBU_ORGA_GROUPS)
    .setReadOnlyForControlling();
    addRight(UserRightCategory.FIBU, UserRightId.FIBU_DATEV_IMPORT, FALSE_TRUE, FIBU_GROUPS);
    addRight(UserRightCategory.FIBU, UserRightId.FIBU_COST_UNIT, FALSE_READONLY_READWRITE, FIBU_ORGA_GROUPS).setReadOnlyForControlling();
    addRight(UserRightCategory.FIBU, UserRightId.FIBU_ACCOUNTS, FALSE_READONLY_READWRITE, FIBU_ORGA_GROUPS).setReadOnlyForControlling();
    addRight(UserRightCategory.ORGA, UserRightId.ORGA_CONTRACTS, FALSE_READONLY_READWRITE, FIBU_ORGA_GROUPS).setReadOnlyForControlling();
    addRight(UserRightCategory.ORGA, UserRightId.ORGA_INCOMING_MAIL, FALSE_READONLY_READWRITE, FIBU_ORGA_GROUPS)
    .setReadOnlyForControlling();
    addRight(UserRightCategory.ORGA, UserRightId.ORGA_OUTGOING_MAIL, FALSE_READONLY_READWRITE, FIBU_ORGA_GROUPS)
    .setReadOnlyForControlling();
    addRight(new ProjektRight());
    addRight(new AuftragRight());
    addRight(new MebRight());
    addRight(new GanttChartRight());
    addRight(new HRPlanningRight());
  }

  public UserGroupsRight addRight(final UserRightCategory category, final UserRightId id, final UserRightValue[] rightValues,
      final ProjectForgeGroup... fibuGroups)
  {
    final UserGroupsRight right = new UserGroupsRight(id, category, rightValues, fibuGroups);
    addRight(right);
    return right;
  }

  public void addRight(final UserRight right)
  {
    final UserRightId userRightId = right.getId();
    rights.put(right.getId(), right);
    rightIds.put(userRightId.getId(), userRightId);
    orderedRights.add(right);
  }
}
