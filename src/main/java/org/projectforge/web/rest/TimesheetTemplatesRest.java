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

package org.projectforge.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.projectforge.registry.Registry;
import org.projectforge.rest.JsonUtils;
import org.projectforge.rest.RestPaths;
import org.projectforge.rest.objects.TimesheetTemplateObject;
import org.projectforge.task.TaskDao;
import org.projectforge.user.UserPrefArea;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefDao;
import org.projectforge.web.rest.converter.TimesheetTemplateConverter;

/**
 * REST-Schnittstelle für {@link TaskDao}
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Path(RestPaths.TIMESHEET_TEMPLATE)
public class TimesheetTemplatesRest
{
  private final UserPrefDao userPrefDao;

  public TimesheetTemplatesRest()
  {
    this.userPrefDao = Registry.instance().getDao(UserPrefDao.class);
  }

  @GET
  @Path(RestPaths.LIST)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getList()
  {
    final List<UserPrefDO> list = userPrefDao.getUserPrefs(UserPrefArea.TIMESHEET_TEMPLATE);
    final List<TimesheetTemplateObject> result = new ArrayList<TimesheetTemplateObject>();
    if (list != null) {
      for (final UserPrefDO userPref : list) {
        final TimesheetTemplateObject template = TimesheetTemplateConverter.getTimesheetTemplateObject(userPref);
        if (template != null) {
          result.add(template);
        }
      }
    }
    final String json = JsonUtils.toJson(result);
    return Response.ok(json).build();
  }
}
