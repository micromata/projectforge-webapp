/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.task.rest;

import org.projectforge.task.TaskDao;

/**
 * REST-Schnittstelle für {@link TaskDao}
 * 
 * @author Daniel Ludwig (d.ludwig@micromata.de)
 * 
 */
//@Path("task")
public class TaskDaoRest {
  // @Autowired
  // private TaskDao taskDao;
  //
  // /**
  // * Rest-Call für:
  // * {@link TaskDao#getList(org.projectforge.core.BaseSearchFilter)}
  // *
  // * @param searchTerm
  // */
  // @GET
  // @Path("list/{search}")
  // @Produces(MediaType.APPLICATION_XML)
  // public Response getList(@PathParam("search") final String searchTerm,
  // @QueryParam("notopened") final boolean notOpened,
  // @QueryParam("opened") final boolean opened,
  // @QueryParam("closed") final boolean closed,
  // @QueryParam("deleted") final boolean deleted) {
  // final TaskFilter filter = new TaskFilter();
  // filter.setClosed(closed);
  // filter.setDeleted(deleted);
  // filter.setOpened(opened);
  // filter.setNotOpened(notOpened);
  //
  // filter.setSearchString(searchTerm);
  // final List<TaskDO> list = taskDao.getList(filter);
  // if (list == null || list.isEmpty()) {
  // return Response.ok(new TasksElement()).build();
  // }
  //
  // final TasksElement t = new TasksElement();
  // t.convertAll(list);
  // return Response.ok(t).build();
  // }
  //
  // public TaskDao getTaskDao() {
  // return taskDao;
  // }
  //
  // public void setTaskDao(final TaskDao taskDao) {
  // this.taskDao = taskDao;
  // }
}
