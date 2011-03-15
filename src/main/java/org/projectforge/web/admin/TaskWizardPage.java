/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.admin;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class TaskWizardPage extends AbstractSecuredPage implements ISelectCallerPage, WizardPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskWizardPage.class);

  boolean managingGroupCreated;

  private TaskWizardForm form;

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  public TaskWizardPage(PageParameters parameters)
  {
    super(parameters);
    form = new TaskWizardForm(this);
    body.add(form);
    form.init();
  }

  void create()
  {

  }

  /**
   * Visibility of the create button.
   */
  boolean actionRequired()
  {
    return form.task != null && (form.managerGroup != null || form.team != null);
  }

  @Override
  public void cancelSelection(String property)
  {
  }

  @Override
  public void select(String property, Object selectedValue)
  {
    if ("taskId".equals(property) == true) {
      form.task = taskDao.getById((Integer) selectedValue);
    } else if ("managerGroupId".equals(property) == true) {
      form.managerGroup = groupDao.getById((Integer) selectedValue);
    } else if ("teamId".equals(property) == true) {
      form.team = groupDao.getById((Integer) selectedValue);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  @Override
  public void unselect(String property)
  {
    if ("taskId".equals(property) == true) {
      form.task = null;
    } else if ("managerGroupId".equals(property) == true) {
      form.managerGroup = null;
    } else if ("teamId".equals(property) == true) {
      form.team = null;
    } else {
      log.error("Property '" + property + "' not supported for deselection.");
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("task.wizard.pageTitle");
  }

  @Override
  public void setCreatedObject(final Object createdObject)
  {
    if (createdObject == null) {
      return;
    } else if (createdObject instanceof TaskDO) {
      form.task = (TaskDO) createdObject;
    } else if (createdObject instanceof GroupDO) {
      if (managingGroupCreated == true) {
        form.managerGroup = (GroupDO) createdObject;
      } else {
        form.team = (GroupDO) createdObject;
      }
    }
  }
}
