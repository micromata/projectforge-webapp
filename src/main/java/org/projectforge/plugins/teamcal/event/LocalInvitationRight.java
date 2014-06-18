/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class LocalInvitationRight extends UserRightAccessCheck<LocalInvitationDO>
{
  private static final long serialVersionUID = 5119523148189858823L;

  public LocalInvitationRight()
  {
    super(LocalInvitationDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final LocalInvitationDO obj, final LocalInvitationDO oldObj, final OperationType operationType)
  {
    return true;
  }
}
