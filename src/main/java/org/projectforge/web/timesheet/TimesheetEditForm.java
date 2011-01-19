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

package org.projectforge.web.timesheet;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserPrefDao;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.layout.LayoutContext;

public class TimesheetEditForm extends AbstractEditForm<TimesheetDO, TimesheetEditPage>
{
  private static final long serialVersionUID = 3150725003240437752L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimesheetEditForm.class);

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userPrefDao")
  private UserPrefDao userPrefDao;

  protected SingleButtonPanel cloneButtonPanel;

  protected TimesheetFormRenderer renderer;

  public TimesheetEditForm(final TimesheetEditPage parentPage, final TimesheetDO data)
  {
    super(parentPage, data);
    renderer = new TimesheetFormRenderer(parentPage, this, new LayoutContext(this), data);
    renderer.taskTree = taskTree;
    renderer.userGroupCache = userGroupCache;
    renderer.userPrefDao = userPrefDao;
    renderer.userFormatter = userFormatter;
  }

  @Override
  protected void init()
  {
    super.init();
    renderer.add();
  }

  @SuppressWarnings("serial")
  @Override
  protected void addButtonPanel()
  {
    final Fragment buttonFragment = new Fragment("buttonPanel", "buttonFragment", this);
    buttonFragment.setRenderBodyOnly(true);
    buttonCell.add(buttonFragment);
    cloneButtonPanel = new SingleButtonPanel("clone", new Button("button", new Model<String>(getString("clone"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.cloneTimesheet();
      }
    });
    if (isNew() == true || getData().isDeleted() == true) {
      // Show clone button only for existing time sheets.
      cloneButtonPanel.setVisible(false);
    }
    buttonFragment.add(cloneButtonPanel);
  }

  @Override
  public void onBeforeRender()
  {
    renderer.onBeforeRender();
    super.onBeforeRender();
  }

  @Override
  protected void validation()
  {
    super.validation();
    renderer.validation();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
