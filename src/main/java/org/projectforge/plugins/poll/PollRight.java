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

package org.projectforge.plugins.poll;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.projectforge.access.OperationType;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 * TODO Max check right please
 * 
 */
public class PollRight extends UserRightAccessCheck<PollDO>
{

  private static final long serialVersionUID = -8240264359189297034L;

  /**
   * @param id
   * @param category
   */
  public PollRight(final UserRightId id, final UserRightCategory category)
  {
    super(id, category);
  }

  public PollRight()
  {
    super(PollDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  /**
   * General select access.
   * @return true
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user)
  {
    return true;
  }

  /**
   * @return true if user is assignee or reporter. If not, the task access is checked.
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final PollDO obj)
  {
    if (isOwner(user, obj) == true) {
      // User has full access to it's own calendars.
      return true;
    }
    return false;
  }

  /**
   * General insert access.
   * @return true
   * @see org.projectforge.user.UserRightAccessCheck#hasInsertAccess(org.projectforge.user.PFUserDO)
   */
  @Override
  public boolean hasInsertAccess(final PFUserDO user)
  {
    return true;
  }

  /**
   * If user is not reporter or assignee and task is given the access to task is assumed, meaning if the user has the right to insert sub
   * tasks he is allowed to insert to-do's to.
   * @see org.projectforge.user.UserRightAccessCheck#hasInsertAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasInsertAccess(final PFUserDO user, final PollDO obj)
  {
    return hasSelectAccess(user, obj);
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasUpdateAccess(org.projectforge.user.PFUserDO, java.lang.Object, java.lang.Object)
   */
  @Override
  public boolean hasUpdateAccess(final PFUserDO user, final PollDO obj, final PollDO oldObj)
  {
    if (ObjectUtils.equals(user.getId(), obj.getOwner().getId()) == true) {
      // User has full access to it's own calendars.
      return true;
    }
    return false;
  }

  public boolean hasAccessGroup(final GroupDO group, final UserGroupCache userGroupCache, final PFUserDO user)
  {
    if (group != null) {
      final Collection<Integer> groups = userGroupCache.getUserGroups(user);
      final Iterator<Integer> it = groups.iterator();
      while (it.hasNext()) {
        final int id = it.next();
        if (id == 0 || group.getId() == id)
          return true;
      }
    }
    return false;
  }

  /**
   * If user is not reporter or assignee and task is given the access to task is assumed, meaning if the user has the right to delete the
   * tasks he is allowed to delete to-do's to.
   * @see org.projectforge.user.UserRightAccessCheck#hasDeleteAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasDeleteAccess(final PFUserDO user, final PollDO obj)
  {
    return hasAccess(user, obj, OperationType.DELETE);
  }

  private boolean hasAccess(final PFUserDO user, final PollDO poll, final OperationType operationType)
  {
    if (poll == null) {
      return true;
    }
    if (ObjectUtils.equals(user.getId(), poll.getOwner().getId()) == true) {
      return true;
    }
    // TODO set rights
    // if ((UserRights.getUserGroupCache().isUserMemberOfGroup(user.getId(), teamCal.getReadOnlyAccessGroupId()) == true || UserRights
    // .getUserGroupCache().isUserMemberOfGroup(user.getId(), teamCal.getMinimalAccessGroupId()) == true)
    // && operationType.equals(OperationType.DELETE))
    // return false;
    else return true;
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasHistoryAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasHistoryAccess(final PFUserDO user, final PollDO obj)
  {
    if (obj != null)
      return hasUpdateAccess(user, obj, null);
    else return false;
  }

  public boolean isOwner(final PFUserDO user, final PollDO cal)
  {
    return ObjectUtils.equals(user.getId(), cal.getOwner().getId()) == true;
  }

  public boolean isMemberOfAtLeastOneGroup(final PFUserDO user, final Integer... groupIds)
  {
    return UserRights.getUserGroupCache().isUserMemberOfAtLeastOneGroup(user.getId(), groupIds);
  }
}
