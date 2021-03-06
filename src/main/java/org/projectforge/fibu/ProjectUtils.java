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

package org.projectforge.fibu;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

/**
 * Some useful helper methods.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ProjectUtils
{
  /**
   * @param username
   * @return List of all projects of which the given user (by login name) is member of the project manager group.
   */
  public static Collection<ProjektDO> getProjectsOfManager(final String username)
  {
    final PFUserDO user = Registry.instance().getUserGroupCache().getUser(username);
    return getProjectsOfManager(user);
  }

  /**
   * @param userId
   * @return List of all projects of which the given user (by user id) is member of the project manager group.
   */
  public static Collection<ProjektDO> getProjectsOfManager(final Integer userId)
  {
    final PFUserDO user = Registry.instance().getUserGroupCache().getUser(userId);
    return getProjectsOfManager(user);
  }

  /**
   * @param user
   * @return List of all projects of which the given user is member of the project manager group.
   */
  public static Collection<ProjektDO> getProjectsOfManager(final PFUserDO user)
  {
    final Collection<ProjektDO> result = new LinkedList<ProjektDO>();
    final ProjektDao projektDao = Registry.instance().getDao(ProjektDao.class);
    final ProjektFilter filter = new ProjektFilter();
    final List<ProjektDO> projects = projektDao.getList(filter);
    if (CollectionUtils.isEmpty(projects) == true) {
      return result;
    }
    final UserGroupCache userGroupCache = Registry.instance().getUserGroupCache();
    for (final ProjektDO project : projects) {
      final Integer groupId = project.getProjektManagerGroupId();
      if (groupId == null) {
        // No manager group defined.
        continue;
      }
      if (userGroupCache.isUserMemberOfGroup(user, groupId) == false) {
        continue;
      }
      result.add(project);
    }
    return result;
  }
}
