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

import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.LayoutLength.ONEHALF;
import static org.projectforge.web.wicket.layout.LayoutLength.QUART;
import static org.projectforge.web.wicket.layout.TextFieldLPanel.INPUT_ID;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.SHType;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.I18nEnumAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMinMaxNumberField;
import org.projectforge.web.wicket.converter.CurrencyConverter;
import org.projectforge.web.wicket.converter.IntegerConverter;
import org.projectforge.web.wicket.converter.MonthConverter;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class AccountingRecordFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = -3418428748298018009L;

  private BuchungssatzDO data;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.FULL;

  public AccountingRecordFormRenderer(final MarkupContainer container, final LayoutContext layoutContext, final BuchungssatzDO data)
  {
    super(container, layoutContext);
    this.data = data;
  }

  @Override
  public void add()
  {
    final DatePanel datePanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "datum"),
        new DatePanelSettings().withTargetType(java.sql.Date.class));
    WicketUtils.setReadonly(datePanel.getDateField());
    doPanel.addDateFieldPanel(data, "datum", getString("date"), HALF, datePanel, HALF);
    final String yearLabel = getString("calendar.year");
    final String monthLabel = getString("calendar.month");
    final MinMaxNumberField<Integer> yearField = new RequiredMinMaxNumberField<Integer>(INPUT_ID, yearLabel, new PropertyModel<Integer>(
        data, "year"), 1900, 2100).setConverter(new IntegerConverter(4));
    doPanel.addTextField(yearField, new PanelContext(QUART, yearLabel + "/" + monthLabel, LABEL_LENGTH));
    WicketUtils.setReadonly(yearField);
    final MinMaxNumberField<Integer> monthField = new RequiredMinMaxNumberField<Integer>(INPUT_ID, monthLabel, new PropertyModel<Integer>(
        data, "month"), 1, 12).setConverter(new MonthConverter());
    doPanel.addTextField(monthField, QUART);
    WicketUtils.setReadonly(monthField);

    final String satzNrLabel = getString("fibu.buchungssatz.satznr");
    final MinMaxNumberField<Integer> satzNrField = new RequiredMinMaxNumberField<Integer>(INPUT_ID, satzNrLabel,
        new PropertyModel<Integer>(data, "satznr"), 1, 99999).setConverter(new IntegerConverter(5));
    WicketUtils.setReadonly(satzNrField);
    doPanel.addTextField(satzNrField, new PanelContext(QUART, satzNrLabel, HALF));

    final String dcLabel = getString("finance.accountingRecord.dc");
    final MinMaxNumberField<BigDecimal> betragField = new MinMaxNumberField<BigDecimal>(INPUT_ID, new PropertyModel<BigDecimal>(data,
        "betrag"), new BigDecimal("-99999999"), new BigDecimal("99999999"));
    doPanel.addTextField(betragField, new PanelContext(HALF, getString("fibu.common.betrag") + "/" + dcLabel, LABEL_LENGTH));
    WicketUtils.setReadonly(betragField.setConverter(new CurrencyConverter()));
    {
      // DropDownChoice debitor/creditor
      // final LabelValueChoiceRenderer<SHType> dcChoiceRenderer = new LabelValueChoiceRenderer<SHType>(container, SHType.values());
      // final DropDownChoice<SHType> dcTypeChoice = new DropDownChoice<SHType>(SELECT_ID, new PropertyModel<SHType>(data, "sh"),
      // dcChoiceRenderer.getValues(), dcChoiceRenderer);
      // WicketUtils.setReadonly(dcTypeChoice.setNullValid(false).setRequired(true));
      // dcTypeChoice.setEnabled(false);
      // doPanel.addDropDownChoice(data, "sh", getString("finance.accountingRecord.dc"), HALF, dcTypeChoice, HALF);

      final I18nEnumAutoCompleteTextField<SHType> dcField = new I18nEnumAutoCompleteTextField<SHType>(INPUT_ID, dcLabel,
          new PropertyModel<SHType>(data, "sh"), SHType.values());
      WicketUtils.setReadonly(dcField);
      dcField.setEnabled(false);
      doPanel.addTextField(dcLabel, dcField, QUART);
    }

    final TextFieldLPanel belegFieldPanel = (TextFieldLPanel) doPanel.addTextField(new PanelContext(data, "beleg", VALUE_LENGTH,
        getString("fibu.buchungssatz.beleg"), LABEL_LENGTH));
    WicketUtils.setReadonly(belegFieldPanel.getTextField());

    final String kost2Label = getString("fibu.kost2");
    final Kost1FormComponent kost1Component = new Kost1FormComponent(INPUT_ID, new PropertyModel<Kost1DO>(data, "kost1"), true);
    doPanel.addTextField(kost1Component, new PanelContext(HALF, getString("fibu.kost1") + "/" + kost2Label, LABEL_LENGTH));
    WicketUtils.setReadonly(kost1Component);
    final Kost2FormComponent kost2Component = new Kost2FormComponent(INPUT_ID, new PropertyModel<Kost2DO>(data, "kost2"), true);
    doPanel.addTextField(kost2Label, kost2Component, HALF);
    WicketUtils.setReadonly(kost2Component);

    final String gegenKontoLabel = getString("fibu.buchungssatz.gegenKonto");
    final KontoFormComponent kontoComponent = new KontoFormComponent(INPUT_ID, new PropertyModel<KontoDO>(data, "konto"), true);
    doPanel
        .addTextField(kontoComponent, new PanelContext(HALF, getString("fibu.buchungssatz.konto") + "/" + gegenKontoLabel, LABEL_LENGTH));
    WicketUtils.setReadonly(kontoComponent);
    final KontoFormComponent gegenKontoComponent = new KontoFormComponent(INPUT_ID, new PropertyModel<KontoDO>(data, "gegenKonto"), true);
    doPanel.addTextField(gegenKontoLabel, gegenKontoComponent, HALF);
    WicketUtils.setReadonly(gegenKontoComponent);

    final TextFieldLPanel textFieldPanel = (TextFieldLPanel) doPanel.addTextField(new PanelContext(data, "text", VALUE_LENGTH,
        getString("fibu.buchungssatz.text"), LABEL_LENGTH));
    WicketUtils.setReadonly(textFieldPanel.getTextField());

    final TextFieldLPanel mengeFieldPanel = (TextFieldLPanel) doPanel.addTextField(new PanelContext(data, "menge", HALF,
        getString("fibu.buchungssatz.menge"), LABEL_LENGTH));
    WicketUtils.setReadonly(mengeFieldPanel.getTextField());

    doPanel.addTextArea(new PanelContext(data, "comment", ONEHALF, getString("comment"), LABEL_LENGTH).setBreakBetweenLabelAndField(true)
        .setCssStyle("height: 20em;"));
  }
}
