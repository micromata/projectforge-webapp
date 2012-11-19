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
public class PollEventDao extends BaseDao<PollEventDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_POLL_EVENT", "plugin16", "plugins.poll.event");

  /**
   * @param clazz
   */
  protected PollEventDao(Class<PollEventDO> clazz)
  {
    super(clazz);
  }

  /**
   * @see org.projectforge.core.BaseDao#newInstance()
   */
  @Override
  public PollEventDO newInstance()
  {
    return new PollEventDO();
  }

}
