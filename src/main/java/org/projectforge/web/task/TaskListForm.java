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

package org.projectforge.web.task;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.task.TaskFilter;
import org.projectforge.web.wicket.AbstractListForm;


public class TaskListForm extends AbstractListForm<TaskFilter, TaskListPage>
{
  private static final long serialVersionUID = 153015604624697061L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskListForm.class);

  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new CheckBox("notOpenedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "notOpened")));
    filterContainer.add(new CheckBox("openedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "opened")));
    filterContainer.add(new CheckBox("closedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "closed")));
    filterContainer.add(new CheckBox("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted")));
  }

  public TaskListForm(TaskListPage parentPage)
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
