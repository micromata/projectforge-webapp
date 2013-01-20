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

package org.projectforge.web.timesheet;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.wicket.AbstractMassEditForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class TimesheetMassUpdateForm extends AbstractMassEditForm<TimesheetDO, TimesheetMassUpdatePage>
{
  private static final long serialVersionUID = -6785832818308468337L;

  private DropDownChoice<Integer> kost2Choice;

  private FieldsetPanel kost2Fieldset;

  protected TimesheetDO data;

  private List<Kost2DO> kost2List;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  protected boolean updateTask;

  private TimesheetPageSupport timesheetPageSupport;

  public TimesheetMassUpdateForm(final TimesheetMassUpdatePage parentPage)
  {
    super(parentPage);
    data = new TimesheetDO();
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    timesheetPageSupport = new TimesheetPageSupport(parentPage, gridBuilder, timesheetDao, data);
    gridBuilder.newGridPanel();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("task"));
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(fs.newChildId(), new PropertyModel<TaskDO>(data, "task"), parentPage,
          "taskId");
      fs.add(taskSelectPanel);
      taskSelectPanel.init();
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.addCheckBox(new PropertyModel<Boolean>(this, "updateTask"), getString("update"), getString("massupdate.updateTask"));
    }
    {
      kost2Fieldset = new FieldsetPanel(gridBuilder.getPanel(), getString("fibu.kost2")) {
        @Override
        public boolean isVisible()
        {
          return CollectionUtils.isNotEmpty(kost2List);
        }
      };
      kost2List = timesheetDao.getKost2List(data);
      final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
      kost2Choice = TimesheetEditForm.createCost2ChoiceRenderer(kost2Fieldset.getDropDownChoiceId(), timesheetDao, taskTree,
          kost2ChoiceRenderer, data, kost2List);
      kost2Fieldset.add(kost2Choice);
    }
    {
      timesheetPageSupport.addLocation();
    }
  }

  protected void refresh()
  {
    kost2List = timesheetDao.getKost2List(data);
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
    kost2Choice.setChoiceRenderer(kost2ChoiceRenderer);
    kost2Choice.setChoices(kost2ChoiceRenderer.getValues());
  }

  private LabelValueChoiceRenderer<Integer> getKost2LabelValueChoiceRenderer()
  {
    return TimesheetEditForm.getCost2LabelValueChoiceRenderer(timesheetDao, kost2List, data, kost2Choice);
  }
}
