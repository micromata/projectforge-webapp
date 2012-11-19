/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll;

import org.projectforge.core.BaseDao;
import org.projectforge.user.UserRightId;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollDao extends BaseDao<PollDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_POLL", "plugin16", "plugins.poll");

  /**
   * @param clazz
   */
  protected PollDao(Class<PollDO> clazz)
  {
    super(clazz);
  }

  /**
   * @see org.projectforge.core.BaseDao#newInstance()
   */
  @Override
  public PollDO newInstance()
  {
    return new PollDO();
  }

}
