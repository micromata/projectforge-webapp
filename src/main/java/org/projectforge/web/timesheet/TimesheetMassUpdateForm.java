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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.SingleButtonPanel;


public class TimesheetMassUpdateForm extends AbstractForm<TimesheetDO, TimesheetMassUpdatePage>
{
  private static final long serialVersionUID = -6785832818308468337L;

  private DropDownChoice<Integer> kost2Choice;

  private MarkupContainer kost2Row;

  protected TimesheetDO data;

  private List<Kost2DO> kost2List;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  protected boolean updateTask;

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
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    TaskSelectPanel taskSelectPanel = new TaskSelectPanel("task", new PropertyModel<TaskDO>(data, "task"), parentPage, "taskId");
    taskSelectPanel.setEnableLinks(false);
    add(taskSelectPanel);
    taskSelectPanel.init();
    final CheckBox updateTaskCheckBox = new CheckBox("updateTask", new PropertyModel<Boolean>(this, "updateTask"));
    WicketUtils.addTooltip(updateTaskCheckBox, getString("massupdate.updateTask"));
    add(updateTaskCheckBox);
    kost2List = timesheetDao.getKost2List(data);
    addKost2Row();
    final Button cancelButton = new Button("button", new Model<String>(getString("cancel"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onCancelSubmit();
      }
    };
    cancelButton.add(WebConstants.BUTTON_CLASS_CANCEL);
    cancelButton.setDefaultFormProcessing(false);
    final SingleButtonPanel cancelButtonPanel = new SingleButtonPanel("cancel", cancelButton);
    add(cancelButtonPanel);
    final Button updateAllButton = new Button("button", new Model<String>(getString("updateAll"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onUpdateAllSubmit();
      }
    };
    updateAllButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    updateAllButton.add(new SimpleAttributeModifier("onclick", "return showUpdateQuestionDialog()"));
    setDefaultButton(updateAllButton);
    final SingleButtonPanel updateAllButtonPanel = new SingleButtonPanel("updateAll", updateAllButton);
    add(updateAllButtonPanel);
  }

  protected void refresh()
  {
    kost2List = timesheetDao.getKost2List(data);
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
    kost2Choice.setChoiceRenderer(kost2ChoiceRenderer);
    kost2Choice.setChoices(kost2ChoiceRenderer.getValues());
  }

  @SuppressWarnings("serial")
  protected void addKost2Row()
  {
    kost2Row = new WebMarkupContainer("kost2Row") {
      @Override
      public boolean isVisible()
      {
        return CollectionUtils.isNotEmpty(kost2List);
      }
    };
    add(kost2Row);
    final LabelValueChoiceRenderer<Integer> kost2ChoiceRenderer = getKost2LabelValueChoiceRenderer();
    kost2Choice = TimesheetEditForm.createKost2ChoiceRenderer(timesheetDao, taskTree, kost2ChoiceRenderer, data, kost2List);
    kost2Row.add(kost2Choice);
  }

  private LabelValueChoiceRenderer<Integer> getKost2LabelValueChoiceRenderer()
  {
    return TimesheetEditForm.getKost2LabelValueChoiceRenderer(timesheetDao, kost2List, data, kost2Choice);
  }

}
