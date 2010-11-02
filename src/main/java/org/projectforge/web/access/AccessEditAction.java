/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.access;

import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;
import org.projectforge.access.AccessDao;
import org.projectforge.access.AccessEntryDO;
import org.projectforge.access.AccessType;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseEditAction;
import org.projectforge.web.core.BaseEditActionBean;
import org.projectforge.web.core.Select;
import org.projectforge.web.task.TaskTreeAction;
import org.projectforge.web.user.GroupListAction;


/**
 */
@UrlBinding("/secure/access/AccessEdit.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/access/accessEdit.jsp")
@BaseEditAction(listAction = AccessListAction.class)
public class AccessEditAction extends BaseEditActionBean<AccessDao, GroupTaskAccessDO>
{
  private static final Logger log = Logger.getLogger(AccessEditAction.class);

  @Select(selectAction = TaskTreeAction.class)
  public Integer getTaskId()
  {
    return getData().getTaskId();
  }

  public void setTaskId(Integer taskId)
  {
    baseDao.setTask(getData(), taskId);
  }

  @Select(selectAction = GroupListAction.class)
  public Integer getGroupId()
  {
    return getData().getGroupId();
  }

  public void setGroupId(Integer groupId)
  {
    baseDao.setGroup(getData(), groupId);
  }

  public void setAccessDao(AccessDao accessDao)
  {
    this.baseDao = accessDao;
  }

  public AccessEntryDO getTasksEntry()
  {
    return getData().ensureAndGetAccessEntry(AccessType.TASKS);
  }

  public AccessEntryDO getAccessManagementEntry()
  {
    return getData().ensureAndGetAccessEntry(AccessType.TASK_ACCESS_MANAGEMENT);
  }

  public AccessEntryDO getTimesheetsEntry()
  {
    return getData().ensureAndGetAccessEntry(AccessType.TIMESHEETS);
  }

  public AccessEntryDO getOwnTimesheetsEntry()
  {
    return getData().ensureAndGetAccessEntry(AccessType.OWN_TIMESHEETS);
  }

  @DontValidate
  public Resolution clear()
  {
    // this.testFlag = false;
    getAccessManagementEntry().setAccess(false, false, false, false);
    getTasksEntry().setAccess(false, false, false, false);
    getOwnTimesheetsEntry().setAccess(false, false, false, false);
    getTimesheetsEntry().setAccess(false, false, false, false);
    return getInputPage();
  }

  @DontValidate
  public Resolution guest()
  {
    getAccessManagementEntry().setAccess(false, false, false, false);
    getTasksEntry().setAccess(true, false, false, false);
    getOwnTimesheetsEntry().setAccess(false, false, false, false);
    getTimesheetsEntry().setAccess(false, false, false, false);
    return getInputPage();
  }

  @DontValidate
  public Resolution employee()
  {
    getAccessManagementEntry().setAccess(true, false, false, false);
    getTasksEntry().setAccess(true, true, true, true);
    getOwnTimesheetsEntry().setAccess(true, true, true, true);
    getTimesheetsEntry().setAccess(true, false, false, false);
    return getInputPage();
  }

  @DontValidate
  public Resolution leader()
  {
    getAccessManagementEntry().setAccess(true, false, false, false);
    getTasksEntry().setAccess(true, true, true, true);
    getOwnTimesheetsEntry().setAccess(true, true, true, true);
    getTimesheetsEntry().setAccess(true, true, true, true);
    return getInputPage();
  }

  @DontValidate
  public Resolution administrator()
  {
    getAccessManagementEntry().setAccess(true, true, true, true);
    getTasksEntry().setAccess(true, true, true, true);
    getOwnTimesheetsEntry().setAccess(true, true, true, true);
    getTimesheetsEntry().setAccess(true, true, true, true);
    return getInputPage();
  }

  @DontValidate
  public Resolution cancel()
  {
    return new ForwardResolution(AccessListAction.class);
  }

  public GroupTaskAccessDO getAccess()
  {
    return getData();
  }

  public void setAccess(GroupTaskAccessDO data)
  {
    setData(data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected GroupTaskAccessDO createDataInstance()
  {
    return new GroupTaskAccessDO();
  }
}
