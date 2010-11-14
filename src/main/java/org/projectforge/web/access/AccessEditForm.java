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

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.access.AccessEntryDO;
import org.projectforge.access.AccessType;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.web.common.TwoListHelper;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class AccessEditForm extends AbstractEditForm<GroupTaskAccessDO, AccessEditPage>
{
  private static final long serialVersionUID = 1949792988059857771L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessEditForm.class);

  TwoListHelper<Integer, String> users;

  public AccessEditForm(final AccessEditPage parentPage, GroupTaskAccessDO data)
  {
    super(parentPage, data);
    this.colspan = 2;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("task", new PropertyModel<TaskDO>(data, "task"), parentPage, "taskId");
    add(taskSelectPanel.setRequired(true));
    taskSelectPanel.init();
    final GroupSelectPanel groupSelectPanel = new GroupSelectPanel("group", new PropertyModel<GroupDO>(data, "group"), parentPage,
        "groupId");
    add(groupSelectPanel.setRequired(true));
    groupSelectPanel.init();
    final Component recursiveLabel = new Label("recursive", getString("recursive"));
    WicketUtils.addTooltip(recursiveLabel, getString("access.recursive.help"));
    add(recursiveLabel);
    add(WicketUtils.addTooltip(new CheckBox("recursiveCheckBox", new PropertyModel<Boolean>(data, "recursive")), getString("access.recursive.help")));
    final MaxLengthTextArea descriptionArea = new MaxLengthTextArea("description", new PropertyModel<String>(data, "description"));
    add(descriptionArea);
    final RepeatingView rowRepeater = new RepeatingView("accessRows");
    add(rowRepeater);
    addAccessRow(rowRepeater, data.ensureAndGetAccessEntry(AccessType.TASK_ACCESS_MANAGEMENT));
    addAccessRow(rowRepeater, data.ensureAndGetAccessEntry(AccessType.TASKS));
    addAccessRow(rowRepeater, data.ensureAndGetAccessEntry(AccessType.TIMESHEETS));
    addAccessRow(rowRepeater, data.ensureAndGetAccessEntry(AccessType.OWN_TIMESHEETS));

    add(new SingleButtonPanel("clear", new Button("button", new Model<String>(getString("access.templates.clear"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.clear();
      }
    }));
    add(new SingleButtonPanel("guest", new Button("button", new Model<String>(getString("access.templates.guest"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.guest();
      }
    }));
    add(new SingleButtonPanel("employee", new Button("button", new Model<String>(getString("access.templates.employee"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.employee();
      }
    }));
    add(new SingleButtonPanel("leader", new Button("button", new Model<String>(getString("access.templates.leader"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.leader();
      }
    }));
    add(new SingleButtonPanel("administrator", new Button("button", new Model<String>(getString("access.templates.administrator"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.administrator();
      }
    }));
  }

  private void addAccessRow(final RepeatingView rowRepeater, final AccessEntryDO accessEntry)
  {
    final WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId());
    rowRepeater.add(row);
    row.add(new Label("area", getString(accessEntry.getAccessType().getI18nKey())));
    row.add(new CheckBox("selectCheckBox", new PropertyModel<Boolean>(accessEntry, "accessSelect")));
    row.add(new CheckBox("insertCheckBox", new PropertyModel<Boolean>(accessEntry, "accessInsert")));
    row.add(new CheckBox("updateCheckBox", new PropertyModel<Boolean>(accessEntry, "accessUpdate")));
    row.add(new CheckBox("deleteCheckBox", new PropertyModel<Boolean>(accessEntry, "accessDelete")));
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
