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

package org.projectforge.web.task;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.task.TaskFilter;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.CoolCheckBoxPanel;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class TaskListForm extends AbstractListForm<TaskFilter, TaskListPage>
{
  private static final long serialVersionUID = 153015604624697061L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskListForm.class);

  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new CoolCheckBoxPanel("notOpenedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "notOpened"),
        getString("task.status.notOpened"), true));
    filterContainer.add(new CoolCheckBoxPanel("openedCheckBox", new PropertyModel<Boolean>(searchFilter, "opened"),
        getString("task.status.opened"), true));
    filterContainer.add(new CoolCheckBoxPanel("closedCheckBox", new PropertyModel<Boolean>(searchFilter, "closed"),
        getString("task.status.closed"), true));
    filterContainer.add(new CoolCheckBoxPanel("deletedCheckBox", new PropertyModel<Boolean>(searchFilter, "deleted"), getString("deleted"),
        true));

    @SuppressWarnings("serial")
    final Button taskTreeButton = new Button("button", new Model<String>(getString("task.tree.perspective"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onTreeViewSubmit();
      }
    };
    taskTreeButton.add(WebConstants.BUTTON_CLASS_NOBUTTON);
    addActionButton(new SingleButtonPanel(getNewActionButtonChildId(), taskTreeButton));
  }

  public TaskListForm(final TaskListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected TaskFilter newSearchFilterInstance()
  {
    return new TaskFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
