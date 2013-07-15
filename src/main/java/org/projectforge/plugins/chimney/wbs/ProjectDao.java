/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import org.apache.commons.lang.Validate;
import org.projectforge.core.BaseDao;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;

public class ProjectDao extends AbstractWBSNodeDao<ProjectDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjectDao.class);

  private UserDao userDao;

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  public ProjectDao()
  {
    super(ProjectDO.class);
    userRightId = WbsNodeRight.USER_RIGHT_ID;
  }

  /**
   * Does an project with the given title already exists? Works also for existing projects (if title was modified).
   * @param project
   * @return
   */
  public boolean doesTitleAlreadyExist(final ProjectDO project)
  {
    Validate.notNull(project);
    log.warn("*** TODO: Check unique title of project.");
    // List<ProjectDO> list = null;
    // if (project.getId() == null) {
    // // New project
    // list = getHibernateTemplate().find("from ProjectDO p where p.title = ?", project.getTitle());
    // } else {
    // // Project already exists. Check maybe changed title:
    // list = getHibernateTemplate().find("from ProjectDO p where p.title = ? and pk <> ?",
    // new Object[] { project.getTitle(), project.getId()});
    // }
    // if (CollectionUtils.isNotEmpty(list) == true) {
    // return true;
    // }
    return false;
  }

  /**
   * @param project
   * @param responsibleUserId If null, then user will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setResponsibleUser(final ProjectDO project, final Integer responsibleUserId)
  {
    final PFUserDO user = userDao.getOrLoad(responsibleUserId);
    project.setResponsibleUser(user);
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
    if (obj.getTask().getParentTask() == null)
      obj.getTask().setParentTask(getRootTask());
  }

  private TaskDO getRootTask()
  {
    return taskDao.getTaskTree().getRootTaskNode().getTask();
  }

}
