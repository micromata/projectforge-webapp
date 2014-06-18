/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import java.util.List;

import org.projectforge.core.BaseDao;
import org.projectforge.user.UserRightId;

/**
 * @author Werner Feder (w.feder-extern@micromata.de)
 *
 */
public class LocalInvitationDao extends BaseDao<LocalInvitationDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_CALENDAR_EVENT_INVITATION", "plugin15", "plugins.teamcalendar.event");

  /**
   * @param clazz
   */
  protected LocalInvitationDao()
  {
    super(LocalInvitationDO.class);
    userRightId = USER_RIGHT_ID;
  }

  /**
   * @see org.projectforge.core.BaseDao#newInstance()
   */
  @Override
  public LocalInvitationDO newInstance()
  {
    return new LocalInvitationDO();
  }

  @SuppressWarnings("unchecked")
  public List<LocalInvitationDO> getInvitations(final Integer userId) {
    if (userId == null) {
      return null;
    }
    List<LocalInvitationDO> list;
    list = getHibernateTemplate().find("from LocalInvitationDO e where e.user.id=? and e.deleted=false", userId);
    return list;
  }

}
