/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.fibu;

import java.util.ArrayList;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHolder;
import org.projectforge.common.StringHelper;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;

public class MonthlyEmployeeReportForm extends AbstractForm<MonthlyEmployeeReportFilter, MonthlyEmployeeReportPage>
{
  private static final long serialVersionUID = 8746545908106124484L;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  protected MonthlyEmployeeReportFilter filter;

  private DropDownChoice<Integer> yearChoice, monthChoice;

  public MonthlyEmployeeReportForm(final MonthlyEmployeeReportPage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    final UserSelectPanel userSelectPanel = new UserSelectPanel("selectUser", new PropertyModel<PFUserDO>(filter, "user"), parentPage,
        "user");
    userSelectPanel.setRequired(true);
    add(userSelectPanel);
    userSelectPanel.init();
    {
      // DropDownChoice months
      final LabelValueChoiceRenderer<Integer> monthChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
      for (int i = 0; i <= 11; i++) {
        monthChoiceRenderer.addValue(i, StringHelper.format2DigitNumber(i + 1));
      }
      monthChoice = new DropDownChoice<Integer>("month", new PropertyModel<Integer>(filter, "month"), monthChoiceRenderer.getValues(),
          monthChoiceRenderer);
      monthChoice.setNullValid(false).setRequired(true);
      add(monthChoice);
    }
    yearChoice = new DropDownChoice<Integer>("year", new PropertyModel<Integer>(filter, "year"), new ArrayList<Integer>());
    yearChoice.setNullValid(false).setRequired(true);
    add(yearChoice);

    final RepeatingView actionButtonsView = new RepeatingView("actionButtons");
    add(actionButtonsView.setRenderBodyOnly(true));
    final Button resetButton = new Button("button", new Model<String>(getString("reset"))) {
      @Override
      public final void onSubmit()
      {
        filter.reset();
        yearChoice.modelChanged();
        monthChoice.modelChanged();
      }
    };
    //    resetButton.add(WebConstants.BUTTON_CLASS_RESET);
    //    resetButton.setDefaultFormProcessing(false);
    //    final SingleButtonPanel resetButtonPanel = new SingleButtonPanel(actionButtonsView.newChildId(), resetButton);
    //    actionButtonsView.add(resetButtonPanel);
    //    final Button exportAsPdfButton = new Button("button", new Model<String>(getString("exportAsPdf"))) {
    //      @Override
    //      public final void onSubmit()
    //      {
    //        parentPage.exportAsPdf();
    //      }
    //    };
    //    actionButtonsView.add(new SingleButtonPanel(actionButtonsView.newChildId(), exportAsPdfButton));
    //    final Button showButton = new Button("button", new Model<String>(getString("show")));
    //    showButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    //    setDefaultButton(showButton);
    //    actionButtonsView.add(new SingleButtonPanel(actionButtonsView.newChildId(), showButton));
  }

  @Override
  public void onBeforeRender()
  {
    refreshYearList();
    super.onBeforeRender();
  }

  private void refreshYearList()
  {
    final int[] years;
    if (filter.getUser() == null) {
      years = new int[] { new DateHolder().getYear()};
    } else {
      years = timesheetDao.getYears(filter.getUser().getId());
    }
    final LabelValueChoiceRenderer<Integer> yearChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (final int year : years) {
      yearChoiceRenderer.addValue(year, String.valueOf(year));
    }
    yearChoice.setChoiceRenderer(yearChoiceRenderer);
    yearChoice.setChoices(yearChoiceRenderer.getValues());
  }
}
