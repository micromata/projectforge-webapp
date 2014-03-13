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

package org.projectforge.plugins.skillmatrix;

import org.projectforge.access.OperationType;
import org.projectforge.common.StringHelper;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;
import org.projectforge.user.UserRights;

/**
 * Define the access rights. In this example every user has access to attendee functionality.
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class AttendeeRight extends UserRightAccessCheck<AttendeeDO>
{
  private static final long serialVersionUID = -3590945654632199595L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainingRight.class);

  private static final boolean doLog = false;

  private static final String delim =",";

  private transient UserGroupCache userGroupCache;

  /**
   * @param id
   * @param category
   * @param rightValues
   */
  public AttendeeRight()
  {
    super(AttendeeDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  private UserGroupCache getUserGroupCache()
  {
    if (userGroupCache == null) {
      userGroupCache = Registry.instance().getUserGroupCache();
    }
    return userGroupCache;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean hasAccess(final PFUserDO user, final AttendeeDO obj, final AttendeeDO oldObj, final OperationType operationType)
  {
    // TODO rewrite hasAccess method
    // Zwei neue Felder pro TrainingDO (analog TeamCalDO:  private String fullAccessGroupIds, readonlyAccessGroupIds;

    if (UserRights.getAccessChecker().isUserMemberOfAdminGroup(user) == true) {
      if (doLog == true)
        log.info("Admin allowed to " + operationType.name());
      return true;
    }

    final AttendeeDO attendee = (oldObj != null) ? oldObj : obj;

    if (attendee == null) {
      if (doLog == true)
        log.info("Training == null " + operationType.name());
      return true;
    }

    boolean ret = false;
    switch (operationType) {
      case SELECT:
      {
        if (doLog == true)
          log.info("Attendee " + operationType.name() + " " + attendee.getAttendee().getFullname() + " for user " + user.getId());
        ret = ( (hasFullAccess(attendee, user.getId()) == true) || (hasReadonlyAccess(attendee, user.getId()) == true) );
        if (doLog == true)
          log.info("return " + ret);
        break;
      }
      case INSERT:
      case UPDATE:
      case DELETE:
      {
        if (doLog == true)
          log.info("Attendee " + operationType.name() + " " + attendee.getAttendee().getFullname() + " for user " + user.getId());
        ret = (hasFullAccess(attendee, user.getId()) == true);
        if (doLog == true)
          log.info("return " + ret);
        break;
      }
      default:
        break;
    }
    return ret;
  }

  public boolean hasFullAccess(final AttendeeDO attendee, final Integer userId)
  {
    final Integer[] groupIds = StringHelper.splitToIntegers(attendee.getTraining().getFullAccessGroupIds(), delim);
    return hasAccess(groupIds, userId);
  }

  public boolean hasReadonlyAccess(final AttendeeDO attendee, final Integer userId)
  {
    final Integer[] groupIds = StringHelper.splitToIntegers(attendee.getTraining().getReadonlyAccessGroupIds(), delim);
    return hasAccess(groupIds, userId);
  }

  private boolean hasAccess(final Integer[] groupIds, final Integer userId)
  {
    if (getUserGroupCache().isUserMemberOfAtLeastOneGroup(userId, groupIds) == true) {
      return true;
    }
    return false;
  }
}
