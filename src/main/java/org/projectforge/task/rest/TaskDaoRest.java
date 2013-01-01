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

package org.projectforge.task.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * REST-Schnittstelle für {@link TaskDao}
 * 
 * @author Daniel Ludwig (d.ludwig@micromata.de)
 * 
 */
@Path("task")
public class TaskDaoRest {
  @Autowired
  private TaskDao taskDao;

  /**
   * Rest-Call für:
   * {@link TaskDao#getList(org.projectforge.core.BaseSearchFilter)}
   *
   * @param searchTerm
   */
  @GET
  @Path("list/{search}")
  @Produces(MediaType.APPLICATION_XML)
  public Response getList(@PathParam("search") final String searchTerm,
      @QueryParam("notopened") final boolean notOpened,
      @QueryParam("opened") final boolean opened,
      @QueryParam("closed") final boolean closed,
      @QueryParam("deleted") final boolean deleted) {
    final TaskFilter filter = new TaskFilter();
    filter.setClosed(closed);
    filter.setDeleted(deleted);
    filter.setOpened(opened);
    filter.setNotOpened(notOpened);

    filter.setSearchString(searchTerm);
    final List<TaskDO> list = taskDao.getList(filter);
    if (list == null || list.isEmpty()) {
      return Response.ok(new TasksElement()).build();
    }

    final TasksElement t = new TasksElement();
    t.convertAll(list);
    return Response.ok(t).build();
  }

  public TaskDao getTaskDao() {
    return taskDao;
  }

  public void setTaskDao(final TaskDao taskDao) {
    this.taskDao = taskDao;
  }
}
