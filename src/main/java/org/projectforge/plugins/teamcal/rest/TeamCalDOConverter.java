/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.rest;

import org.apache.commons.lang.ObjectUtils;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.admin.TeamCalRight;
import org.projectforge.rest.objects.CalendarObject;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserRights;
import org.projectforge.web.rest.converter.DOConverter;

/**
 * For conversion of TeamCalDO to CalendarObject.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TeamCalDOConverter
{
  public static CalendarObject getCalendarObject(final TeamCalDO src)
  {
    if (src == null) {
      return null;
    }
    final Integer userId = PFUserContext.getUserId();
    final CalendarObject cal = new CalendarObject();
    DOConverter.copyFields(cal, src);
    cal.setTitle(src.getTitle());
    cal.setDescription(src.getDescription());
    cal.setExternalSubscription(src.isExternalSubscription());
    final TeamCalRight right = (TeamCalRight) UserRights.instance().getRight(TeamCalDao.USER_RIGHT_ID);
    cal.setMinimalAccess(right.hasMinimalAccess(src, userId));
    cal.setReadonlyAccess(right.hasReadonlyAccess(src, userId));
    cal.setFullAccess(right.hasFullAccess(src, userId));
    cal.setOwner(ObjectUtils.equals(userId, src.getOwnerId()));
    return cal;
  }
}
