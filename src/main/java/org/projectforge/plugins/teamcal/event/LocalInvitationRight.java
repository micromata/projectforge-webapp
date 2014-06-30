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
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * @author Werner Feder (w.feder.extern@micromata.de)
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
    if (UserRights.getAccessChecker().isUserMemberOfAdminGroup(user) == true) {
      return true;
    }

    final LocalInvitationDO invitation = (oldObj != null) ? oldObj : obj;
    if (invitation == null) {
      return true;
    }

    if (invitation.getUserId().equals(PFUserContext.getUserId()) == true) {
      return true;
    }

    return false;
  }
}
