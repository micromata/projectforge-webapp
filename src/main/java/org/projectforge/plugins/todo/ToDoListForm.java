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

package org.projectforge.plugins.todo;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.CoolCheckBoxPanel;

public class ToDoListForm extends AbstractListForm<ToDoFilter, ToDoListPage>
{
  private static final long serialVersionUID = -8310609149068611648L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToDoListForm.class);

  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new CoolCheckBoxPanel("openedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "opened"),
        getString(ToDoStatus.OPENED.getI18nKey()), true));
    filterContainer.add(new CoolCheckBoxPanel("inprogressCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "inprogress"),
        getString(ToDoStatus.IN_PROGRESS.getI18nKey()), true));
    filterContainer.add(new CoolCheckBoxPanel("closedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "closed"),
        getString(ToDoStatus.CLOSED.getI18nKey()), true));
    filterContainer.add(new CoolCheckBoxPanel("postponedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "postponed"),
        getString(ToDoStatus.POSTPONED.getI18nKey()), true));
    filterContainer.add(new CoolCheckBoxPanel("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted"),
        getString("onlyDeleted"), true).setTooltip(getString("onlyDeleted.tooltip")));
  }

  public ToDoListForm(ToDoListPage parentPage)
  {
    super(parentPage);
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
