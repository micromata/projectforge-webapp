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

package org.projectforge.web.fibu;

import org.apache.log4j.Logger;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.EmployeeSalaryDao;
import org.projectforge.fibu.EmployeeSalaryFilter;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.YearListCoiceRenderer;

public class EmployeeSalaryListForm extends AbstractListForm<EmployeeSalaryFilter, EmployeeSalaryListPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmployeeSalaryListForm.class);

  private static final long serialVersionUID = -5969136444233092172L;

  @SpringBean(name = "employeeSalaryDao")
  private EmployeeSalaryDao employeeSalaryDao;

  @Override
  protected void init()
  {
    super.init();
    // DropDownChoice years
    final YearListCoiceRenderer yearListChoiceRenderer = new YearListCoiceRenderer(employeeSalaryDao.getYears(), true);
    @SuppressWarnings("unchecked")
    final DropDownChoice yearChoice = new DropDownChoice("year", new PropertyModel(this, "year"), yearListChoiceRenderer.getYears(),
        yearListChoiceRenderer);
    yearChoice.setNullValid(false);
    filterContainer.add(yearChoice);
    // DropDownChoice months
    final LabelValueChoiceRenderer<Integer> monthChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (int i = 0; i <= 11; i++) {
      monthChoiceRenderer.addValue(i, StringHelper.format2DigitNumber(i + 1));
    }
    @SuppressWarnings("unchecked")
    final DropDownChoice monthChoice = new DropDownChoice("month", new PropertyModel(this, "month"), monthChoiceRenderer.getValues(),
        monthChoiceRenderer);
    monthChoice.setNullValid(true);
    monthChoice.setRequired(false);
    filterContainer.add(monthChoice);
    filterContainer.add(new CheckBox("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted")));

    @SuppressWarnings("serial")
    final Button exportAsXlsButton = new Button("button", new Model<String>(getString("exportAsXls"))) {
      @Override
      public final void onSubmit()
      {
        if (getSearchFilter().getMonth() < 0 || getSearchFilter().getMonth() > 11) {
          addError("fibu.employee.salary.error.monthNotGiven");
          return;
        }
        parentPage.exportExcel();
      }
    };
    exportAsXlsButton.setDefaultFormProcessing(false).add(
        new SimpleAttributeModifier("title", getString("fibu.employee.salary.exportXls.tooltip")));
    final SingleButtonPanel exportVCardsPanel = new SingleButtonPanel(getNewActionButtonChildId(), exportAsXlsButton);
    prependActionButton(exportVCardsPanel);
}

  public EmployeeSalaryListForm(EmployeeSalaryListPage parentPage)
  {
    super(parentPage);
  }

  public Integer getYear()
  {
    return getSearchFilter().getYear();
  }

  public void setYear(final Integer year)
  {
    if (year == null) {
      getSearchFilter().setYear(-1);
    } else {
      getSearchFilter().setYear(year);
    }
  }

  public Integer getMonth()
  {
    return getSearchFilter().getMonth();
  }

  public void setMonth(final Integer month)
  {
    if (month == null) {
      getSearchFilter().setMonth(-1);
    } else {
      getSearchFilter().setMonth(month);
    }
  }

  @Override
  protected EmployeeSalaryFilter newSearchFilterInstance()
  {
    return new EmployeeSalaryFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
