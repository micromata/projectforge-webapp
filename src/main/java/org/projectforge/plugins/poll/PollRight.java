/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll;

import org.projectforge.user.UserRight;
import org.projectforge.user.UserRightCategory;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PollRight extends UserRight
{

  /**
   * @param id
   * @param category
   */
  public PollRight(UserRightId id, UserRightCategory category)
  {
    super(id, category);
  }

  public PollRight()
  {
    super(PollDao.USER_RIGHT_ID, UserRightCategory.PLUGINS, UserRightValue.TRUE);
  }

  /**
   * 
   */
  private static final long serialVersionUID = -8240264359189297034L;

}
