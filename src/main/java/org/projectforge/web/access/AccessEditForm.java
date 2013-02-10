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
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class AccessEditForm extends AbstractEditForm<GroupTaskAccessDO, AccessEditPage>
{
  private static final long serialVersionUID = 1949792988059857771L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessEditForm.class);

  public AccessEditForm(final AccessEditPage parentPage, final GroupTaskAccessDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGridPanel();
    {
      // Task
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("task"));
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(fs.newChildId(), new PropertyModel<TaskDO>(data, "task"), parentPage,
          "taskId");
      fs.add(taskSelectPanel.setRequired(true));
      taskSelectPanel.init();
    }
    {
      // Group
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("group"));
      final GroupSelectPanel groupSelectPanel = new GroupSelectPanel(fs.newChildId(), new PropertyModel<GroupDO>(data, "group"),
          parentPage, "groupId");
      fs.add(groupSelectPanel.setRequired(true));
      groupSelectPanel.init();
    }
    {
      // Option
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"));
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel
      .add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(data, "recursive"), getString("recursive"))
      .setTooltip(getString("access.recursive.help")));
    }
    {
      // Access entries table
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("access.accessTable"));
      final AccessEditTablePanel accessEditTablePanel = new AccessEditTablePanel(fs.newChildId(), data);
      fs.add(accessEditTablePanel);
      accessEditTablePanel.init();
    }
    {
      // Templates
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("access.templates")).supressLabelForWarning();
      fs.add(new SingleButtonPanel(fs.newChildId(), new Button(SingleButtonPanel.WICKET_ID, new Model<String>("clear")) {
        @Override
        public final void onSubmit()
        {
          data.clear();
        }
      }, getString("access.templates.clear"), SingleButtonPanel.GREY));
      fs.add(new SingleButtonPanel(fs.newChildId(), new Button(SingleButtonPanel.WICKET_ID, new Model<String>("guest")) {
        @Override
        public final void onSubmit()
        {
          data.guest();
        }
      }, getString("access.templates.guest"), SingleButtonPanel.GREY));
      fs.add(new SingleButtonPanel(fs.newChildId(), new Button(SingleButtonPanel.WICKET_ID, new Model<String>("employee")) {
        @Override
        public final void onSubmit()
        {
          data.employee();
        }
      }, getString("access.templates.employee"), SingleButtonPanel.GREY));
      fs.add(new SingleButtonPanel(fs.newChildId(), new Button(SingleButtonPanel.WICKET_ID, new Model<String>("leader")) {
        @Override
        public final void onSubmit()
        {
          data.leader();
        }
      }, getString("access.templates.leader"), SingleButtonPanel.GREY));
      fs.add(new SingleButtonPanel(fs.newChildId(), new Button(SingleButtonPanel.WICKET_ID, new Model<String>("administrator")) {
        @Override
        public final void onSubmit()
        {
          data.administrator();
        }
      }, getString("access.templates.administrator"), SingleButtonPanel.RED));
    }
    {
      // Description
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("description"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description")), true);
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
