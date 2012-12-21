/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.attendee;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PollAttendeeRight extends UserRightAccessCheck<PollAttendeeDO>
{
  private static final long serialVersionUID = 5546777247602641113L;

  /**
   * @param id
   * @param category
   * @param rightValues
   */
  public PollAttendeeRight()
  {
    super(PollAttendeeDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final PollAttendeeDO obj)
  {
    return true; // TODO
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasInsertAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasInsertAccess(final PFUserDO user, final PollAttendeeDO obj)
  {
    return true; // TODO
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasAccess(org.projectforge.user.PFUserDO, java.lang.Object, java.lang.Object,
   *      org.projectforge.access.OperationType)
   */
  @Override
  public boolean hasAccess(PFUserDO user, PollAttendeeDO obj, PollAttendeeDO oldObj, OperationType operationType)
  {
    // return super.hasAccess(user, obj, oldObj, operationType);
    return true;
  }
}
