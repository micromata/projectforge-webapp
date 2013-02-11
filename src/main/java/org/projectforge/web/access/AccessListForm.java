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
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

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
    gridBuilder.newSplitPanel(GridSize.COL66);
    {
      // Group
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("group")).supressLabelForWarning();
      final GroupSelectPanel groupSelectPanel = new GroupSelectPanel(fs.newChildId(), new Model<GroupDO>() {
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
      fs.add(groupSelectPanel);
      groupSelectPanel.setDefaultFormProcessing(false);
      groupSelectPanel.init();
    }
    gridBuilder.newSplitPanel(GridSize.COL33);
    {
      // User
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("user")).supressLabelForWarning();
      final UserSelectPanel assigneeSelectPanel = new UserSelectPanel(fs.newChildId(), new Model<PFUserDO>() {
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
      fs.add(assigneeSelectPanel);
      assigneeSelectPanel.setDefaultFormProcessing(false);
      assigneeSelectPanel.init().withAutoSubmit(true);
      fs.addHelpIcon(getString("access.tooltip.filter.user"));
    }
    gridBuilder.newGridPanel();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("task")).supressLabelForWarning();
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(fs.newChildId(), new Model<TaskDO>() {
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
      fs.add(taskSelectPanel);
      taskSelectPanel.init();
      taskSelectPanel.setRequired(false);
    }
  }

  public AccessListForm(final AccessListPage parentPage)
  {
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#onOptionsPanelCreate(org.projectforge.web.wicket.flowlayout.FieldsetPanel, org.projectforge.web.wicket.flowlayout.DivPanel)
   */
  @Override
  protected void onOptionsPanelCreate(final FieldsetPanel optionsFieldsetPanel, final DivPanel optionsCheckBoxesPanel)
  {
    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(),
        new PropertyModel<Boolean>(getSearchFilter(), "inherit"), getString("inherit")).setTooltip(
            getString("access.tooltip.filter.inherit")));
    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(),
        new PropertyModel<Boolean>(getSearchFilter(), "includeAncestorTasks"), getString("access.filter.includeAncestorTasks")).setTooltip(
            getString("access.tooltip.filter.includeAncestorTasks")));
    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(),
        new PropertyModel<Boolean>(getSearchFilter(), "includeDescendentTasks"), getString("access.filter.includeDescendentTasks")).setTooltip(
            getString("access.tooltip.filter.includeDescendentTasks")));
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
