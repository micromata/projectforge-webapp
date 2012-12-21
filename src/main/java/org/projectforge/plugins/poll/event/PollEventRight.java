/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.event;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PollEventRight extends UserRightAccessCheck<PollEventDO>
{
  private static final long serialVersionUID = 5546777247602641113L;

  /**
   * @param id
   * @param category
   * @param rightValues
   */
  public PollEventRight()
  {
    super(PollEventDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasInsertAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasInsertAccess(final PFUserDO user, final PollEventDO obj)
  {
    return true; // TODO
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasSelectAccess(org.projectforge.user.PFUserDO, java.lang.Object)
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final PollEventDO obj)
  {
    return true; // TODO
  }

  /**
   * @see org.projectforge.user.UserRightAccessCheck#hasAccess(org.projectforge.user.PFUserDO, java.lang.Object, java.lang.Object,
   *      org.projectforge.access.OperationType)
   */
  @Override
  public boolean hasAccess(PFUserDO user, PollEventDO obj, PollEventDO oldObj, OperationType operationType)
  {
    // TODO
    return true;
  }
}
