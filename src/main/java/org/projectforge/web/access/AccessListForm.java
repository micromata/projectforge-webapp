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

package org.projectforge.web.access;

import org.apache.log4j.Logger;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessFilter;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.CoolCheckBoxPanel;
import org.projectforge.web.wicket.components.LabelForPanel;

public class AccessListForm extends AbstractListForm<AccessFilter, AccessListPage>
{
  private static final long serialVersionUID = 7972009688251087219L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessListForm.class);

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new CoolCheckBoxPanel("inheritCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "inherit"),
        getString("inherit"), true));
    filterContainer.add(new CoolCheckBoxPanel("recursiveCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "recursive"),
        getString("recursive"), true));
    filterContainer.add(new CoolCheckBoxPanel("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted"),
        getString("onlyDeleted"), true).setTooltip(getString("onlyDeleted.tooltip")));
    
    
    final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("task", new Model<TaskDO>() {
      @Override
      public TaskDO getObject()
      {
        return taskTree.getTaskById(getSearchFilter().getTaskId());
      }
    }, parentPage, "taskId") {
      @Override
      protected void selectTask(final TaskDO task)
      {
        super.selectTask(task);
        if (task != null) {
          getSearchFilter().setTaskId(task.getId());
        }
        parentPage.refresh();
      }
    };
    filterContainer.add(taskSelectPanel);
    taskSelectPanel.setEnableLinks(true);
    taskSelectPanel.init();
    taskSelectPanel.setRequired(false);
    filterContainer.add(new LabelForPanel("taskLabel", taskSelectPanel, getString("task")));

    final GroupSelectPanel groupSelectPanel = new GroupSelectPanel("group", new Model<GroupDO>() {
      @Override
      public GroupDO getObject()
      {
        return userGroupCache.getGroup(getSearchFilter().getGroupId());
      }

      @Override
      public void setObject(final GroupDO object)
      {
        if (object == null) {
          getSearchFilter().setGroupId(null);
        } else {
          getSearchFilter().setGroupId(object.getId());
        }
      }
    }, parentPage, "groupId");
    filterContainer.add(groupSelectPanel);
    groupSelectPanel.setDefaultFormProcessing(false);
    groupSelectPanel.init();
    filterContainer.add(new LabelForPanel("groupLabel", groupSelectPanel, getString("group")));

    final UserSelectPanel userSelectPanel = new UserSelectPanel("user", new Model<PFUserDO>() {
      @Override
      public PFUserDO getObject()
      {
        return userGroupCache.getUser(getSearchFilter().getUserId());
      }

      @Override
      public void setObject(final PFUserDO object)
      {
        if (object == null) {
          getSearchFilter().setUserId(null);
        } else {
          getSearchFilter().setUserId(object.getId());
        }
      }
    }, parentPage, "userId");
    filterContainer.add(userSelectPanel);
    userSelectPanel.setDefaultFormProcessing(false);
    userSelectPanel.init().withAutoSubmit(true);
    filterContainer.add(new LabelForPanel("userLabel", userSelectPanel, getString("user")));
}

  public AccessListForm(AccessListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected AccessFilter newSearchFilterInstance()
  {
    return new AccessFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
