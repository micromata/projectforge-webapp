/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.plugins.chimney.activities;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * Define the access rights to WbsActivityDO.
 */
public class DependencyRelationRight extends UserRightAccessCheck<DependencyRelationDO>
{
  private static final long serialVersionUID = -1899134919196891429L;

  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_CHIMNEY_DEPRELATION", "plugin12",
  "plugins.chimney.rights.dependencyrelation");;

  public DependencyRelationRight()
  {
    super(USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  // TODO: Define less restrictive access
  @Override
  public boolean hasAccess(final PFUserDO user, final DependencyRelationDO obj, final DependencyRelationDO oldObj,
      final OperationType operationType)
  {
    if (UserRights.getAccessChecker().isUserMemberOfAdminGroup(user)) {
      return true;
    } else {
      return super.hasAccess(user, obj, oldObj, operationType);
    }
  }
}
