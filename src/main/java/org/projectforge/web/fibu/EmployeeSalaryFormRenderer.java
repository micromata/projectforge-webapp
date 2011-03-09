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

import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.LayoutLength.ONEHALF;
import static org.projectforge.web.wicket.layout.LayoutLength.QUART;
import static org.projectforge.web.wicket.layout.LayoutLength.THREEQUART;
import static org.projectforge.web.wicket.layout.TextFieldLPanel.INPUT_ID;

import java.math.BigDecimal;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.EmployeeDO;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.EmployeeSalaryType;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.converter.CurrencyConverter;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.SelectLPanel;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class EmployeeSalaryFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = 2532546903021383010L;

  private EmployeeSalaryDO data;

  private ISelectCallerPage callerPage;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.ONEHALF;

  public EmployeeSalaryFormRenderer(final MarkupContainer container, final ISelectCallerPage callerPage, final LayoutContext layoutContext,
      final EmployeeSalaryDO data)
  {
    super(container, layoutContext);
    this.data = data;
    this.callerPage = callerPage;
  }

  @SuppressWarnings("serial")
  @Override
  public void add()
  {
    final EmployeeSelectPanel employeeSelectPanel = new EmployeeSelectPanel(SelectLPanel.WICKET_ID_SELECT_PANEL,
        new PropertyModel<EmployeeDO>(data, "employee"), callerPage, "employee");
    doPanel.addSelectPanel(employeeSelectPanel, new PanelContext(FULL, getString("fibu.employee"), LABEL_LENGTH));
    employeeSelectPanel.setRequired(true);
    employeeSelectPanel.init();
    {
      // DropDownChoice months
      final LabelValueChoiceRenderer<Integer> monthChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
      for (int i = 0; i <= 11; i++) {
        monthChoiceRenderer.addValue(i, StringHelper.format2DigitNumber(i + 1));
      }
      @SuppressWarnings("unchecked")
      final DropDownChoice monthChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "month"), monthChoiceRenderer.getValues(),
          monthChoiceRenderer);
      monthChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(monthChoice, new PanelContext(data, "month", HALF, getString("calendar.month"), LABEL_LENGTH));
    }
    final MinMaxNumberField<Integer> yearField = new MinMaxNumberField<Integer>(TextFieldLPanel.INPUT_ID, new PropertyModel<Integer>(data,
        "year"), 1900, 2999);
    doPanel.addTextField(yearField, new PanelContext(QUART).setLabel(getString("year")));
    {
      // DropDownChoice salary type
      final LabelValueChoiceRenderer<EmployeeSalaryType> typeStatusChoiceRenderer = new LabelValueChoiceRenderer<EmployeeSalaryType>(
          container, EmployeeSalaryType.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice typeChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "type"),
          typeStatusChoiceRenderer.getValues(), typeStatusChoiceRenderer);
      typeChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(typeChoice,
          new PanelContext(data, "type", THREEQUART, getString("fibu.employee.salary.type"), LABEL_LENGTH));
    }
    final TextField<BigDecimal> bruttoField = new TextField<BigDecimal>(INPUT_ID, new PropertyModel<BigDecimal>(data, "bruttoMitAgAnteil")) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new CurrencyConverter();
      }
    };
    doPanel.addTextField(bruttoField, new PanelContext(HALF, getString("fibu.employee.salary.bruttoMitAgAnteil"), LABEL_LENGTH));

    // Brutto
    doPanel.addTextArea(new PanelContext(data, "comment", ONEHALF, getString("comment"), ONEHALF).setBreakBetweenLabelAndField(true)
        .setCssStyle("height: 20em;"));
  }
}
