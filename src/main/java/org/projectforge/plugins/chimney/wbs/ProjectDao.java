/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import org.projectforge.task.TaskDO;

public class ProjectDao extends AbstractWBSNodeDao<ProjectDO>
{

  public ProjectDao() {
    super(ProjectDO.class);
    userRightId = WbsNodeRight.USER_RIGHT_ID;
  }

  @Override
  public ProjectDO newInstance()
  {
    final ProjectDO instance = new ProjectDO();
    return instance;
  }

  @Override
  protected void onSaveOrModify(final ProjectDO obj)
  {
    setRootTaskIfNull(obj);
    super.onSaveOrModify(obj);
  }

  private void setRootTaskIfNull(final ProjectDO obj)
  {
    if (obj.getTaskDo().getParentTask() == null)
      obj.getTaskDo().setParentTask(getRootTask());
  }

  private TaskDO getRootTask()
  {
    return taskDao.getTaskTree().getRootTaskNode().getTask();
  }

}
