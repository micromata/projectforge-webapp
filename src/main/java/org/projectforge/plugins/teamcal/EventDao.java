/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.plugins.teamcal;

import org.projectforge.core.BaseDao;
import org.projectforge.user.UserRightId;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class EventDao extends BaseDao<EventDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_CALENDAR", "plugin16", "plugins.teamcalendar.event");;
  //
  //  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "owner.username", "owner.firstname", "owner.lastname",
  //    "fullAccessGroup.name", "readOnlyAccessGroup.name", "minimalAccessGroup.name"};
  //
  //  private GroupDao groupDao;
  //
  //  private UserDao userDao;
  //
  public EventDao()
  {
    super(EventDO.class);
    userRightId = USER_RIGHT_ID;
  }

  //
  //  @Override
  //  protected String[] getAdditionalSearchFields()
  //  {
  //    return ADDITIONAL_SEARCH_FIELDS;
  //  }
  //
  //  public void setOwner(final CalendarDO calendar, final Integer userId)
  //  {
  //    final PFUserDO user = userDao.getOrLoad(userId);
  //    calendar.setOwner(user);
  //  }
  //
  //  public void setFullAccessGroup(final CalendarDO calendar, final Integer groupId)
  //  {
  //    final GroupDO group = groupDao.getOrLoad(groupId);
  //    calendar.setFullAccessGroup(group);
  //  }
  //
  //  public void setReadOnlyAccessGroup(final CalendarDO calendar, final Integer groupId)
  //  {
  //    final GroupDO group = groupDao.getOrLoad(groupId);
  //    calendar.setReadOnlyAccessGroup(group);
  //  }
  //
  //  public void setMinimalAccessGroup(final CalendarDO calendar, final Integer groupId)
  //  {
  //    final GroupDO group = groupDao.getOrLoad(groupId);
  //    calendar.setMinimalAccessGroup(group);
  //  }
  //
  @Override
  public EventDO newInstance()
  {
    return new EventDO();
  }
  //
  //  public void setGroupDao(final GroupDao groupDao)
  //  {
  //    this.groupDao = groupDao;
  //  }
  //
  //  public void setUserDao(final UserDao userDao)
  //  {
  //    this.userDao = userDao;
  //  }
}
