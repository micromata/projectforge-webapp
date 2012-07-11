/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.apache.commons.lang.ObjectUtils;
import org.projectforge.access.OperationType;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightAccessCheck;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightValue;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
public class SkillRatingRight extends UserRightAccessCheck<SkillRatingDO>
{
  private static final long serialVersionUID = 197678676075684591L;

  /**
   * @param id
   * @param category
   * @param rightValues
   */
  public SkillRatingRight()
  {
    super(SkillRatingDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final SkillRatingDO obj, final SkillRatingDO oldObj, final OperationType operationType)
  {
    final SkillRatingDO skill = (oldObj != null) ? oldObj : obj;

    if (skill == null) {
      return true; // General insert and select access given by default.
    }

    switch (operationType) {
      case SELECT:
      case INSERT:
        // Everyone is allowed to read and create skillratings
        return true;
      case UPDATE:
      case DELETE:
        // Only owner is allowed to edit his skillratings
        return ObjectUtils.equals(user.getId(), skill.getUserId());
      default:
        return false;
    }
  }
}