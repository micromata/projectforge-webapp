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

package org.projectforge.plugins.todo;

import org.apache.log4j.Logger;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class ToDoListForm extends AbstractListForm<ToDoFilter, ToDoListPage>
{
  private static final long serialVersionUID = -8310609149068611648L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToDoListForm.class);

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newSplitPanel(GridSize.COL66);
    {
      // Assignee
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.todo.assignee"));
      final UserSelectPanel assigneeSelectPanel = new UserSelectPanel(fs.newChildId(), new Model<PFUserDO>() {
        @Override
        public PFUserDO getObject()
        {
          return userGroupCache.getUser(getSearchFilter().getAssigneeId());
        }

        @Override
        public void setObject(final PFUserDO object)
        {
          if (object == null) {
            getSearchFilter().setAssigneeId(null);
          } else {
            getSearchFilter().setAssigneeId(object.getId());
          }
        }
      }, parentPage, "assigneeId");
      fs.add(assigneeSelectPanel);
      assigneeSelectPanel.setDefaultFormProcessing(false);
      assigneeSelectPanel.init().withAutoSubmit(true);
    }
    gridBuilder.newSplitPanel(GridSize.COL33);
    {
      // Reporter
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.todo.reporter"));
      final UserSelectPanel reporterSelectPanel = new UserSelectPanel(fs.newChildId(), new Model<PFUserDO>() {

        @Override
        public PFUserDO getObject()
        {
          return userGroupCache.getUser(getSearchFilter().getReporterId());
        }

        @Override
        public void setObject(final PFUserDO object)
        {
          if (object == null) {
            getSearchFilter().setReporterId(null);
          } else {
            getSearchFilter().setReporterId(object.getId());
          }
        }
      }, parentPage, "reporterId");
      fs.add(reporterSelectPanel);
      reporterSelectPanel.setDefaultFormProcessing(false);
      reporterSelectPanel.init().withAutoSubmit(true);
    }
    {
      gridBuilder.newSplitPanel(GridSize.COL100);
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

  public ToDoListForm(final ToDoListPage parentPage)
  {
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#onOptionsPanelCreate(org.projectforge.web.wicket.flowlayout.FieldsetPanel,
   *      org.projectforge.web.wicket.flowlayout.DivPanel)
   */
  @Override
  protected void onOptionsPanelCreate(final FieldsetPanel optionsFieldsetPanel, final DivPanel optionsCheckBoxesPanel)
  {
    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
        getSearchFilter(), "opened"), getString(ToDoStatus.OPENED.getI18nKey())));
    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
        getSearchFilter(), "reopened"), getString(ToDoStatus.RE_OPENED.getI18nKey())));
    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
        getSearchFilter(), "inprogress"), getString(ToDoStatus.IN_PROGRESS.getI18nKey())));
    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
        getSearchFilter(), "closed"), getString(ToDoStatus.CLOSED.getI18nKey())));
    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
        getSearchFilter(), "postponed"), getString(ToDoStatus.POSTPONED.getI18nKey())));
    optionsCheckBoxesPanel
    .add(createAutoRefreshCheckBoxPanel(optionsCheckBoxesPanel.newChildId(),
        new PropertyModel<Boolean>(getSearchFilter(), "onlyRecent"), getString("plugins.todo.status.onlyRecent"),
        getString("plugins.todo.status.onlyRecent.tooltip")));
  }

  @Override
  protected ToDoFilter newSearchFilterInstance()
  {
    return new ToDoFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
