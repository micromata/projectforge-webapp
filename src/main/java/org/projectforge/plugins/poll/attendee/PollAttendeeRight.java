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

package org.projectforge.plugins.poll.attendee;

import org.projectforge.access.OperationType;
import org.projectforge.plugins.poll.PollRight;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 */
public class PollAttendeeRight extends UserRightAccessCheck<PollAttendeeDO>
{
  private static final long serialVersionUID = 5546777247602641113L;

  private final PollRight pollRight;

  /**
   * @param id
   * @param category
   * @param rightValues
   */
  public PollAttendeeRight()
  {
    super(PollAttendeeDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
    pollRight = new PollRight();
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final PollAttendeeDO obj)
  {
    // new poll
    if (obj == null || obj.getPoll() == null) {
      return true;
    }

    if (pollRight.isOwner(user, obj.getPoll()) == true) {
      return true;
    } else {
      if (pollRight.isVerifiedUser(user, null, obj.getPoll()) == true) {
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasInsertAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasInsertAccess(final PFUserDO user, final PollAttendeeDO obj)
  {
    // new entry if obj.getPoll() == null
    if (obj == null
        || obj.getPoll() == null
        || pollRight.isOwner(user, obj.getPoll()) == true) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasAccess(org.projectforge.user.PFUserDO, java.lang.Object, java.lang.Object,
   *      org.projectforge.access.OperationType)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final PollAttendeeDO obj, final PollAttendeeDO oldObj, final OperationType operationType)
  {
    // new entry if obj == null
    if (obj == null || pollRight.isOwner(user, obj.getPoll()) == true) {
      return true;
    } else {
      return false;
    }
  }
}
