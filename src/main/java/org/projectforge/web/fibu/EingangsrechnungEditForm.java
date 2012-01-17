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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.calendar.DayHolder;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.EingangsrechnungsPositionDO;
import org.projectforge.registry.Registry;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextField;


public class EingangsrechnungEditForm extends
AbstractRechnungEditForm<EingangsrechnungDO, EingangsrechnungsPositionDO, EingangsrechnungEditPage>
{
  private static final long serialVersionUID = 5286417118638335693L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EingangsrechnungEditForm.class);

  private DropDownChoice<Long> datumChoice;

  public EingangsrechnungEditForm(final EingangsrechnungEditPage parentPage, final EingangsrechnungDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  protected void onInit()
  {
    final PFAutoCompleteTextField<String> kreditorField = new PFAutoCompleteTextField<String>("kreditor", new PropertyModel<String>(data,
        "kreditor")) {
      @Override
      protected List<String> getChoices(final String input)
      {
        return parentPage.getBaseDao().getAutocompletion("kreditor", input);
      }
    };
    kreditorField.withMatchContains(true).withMinChars(2).withFocus(true);
    add(kreditorField);

    final WebMarkupContainer kontoRow = new WebMarkupContainer("kontoRow");
    add(kontoRow);
    if (Registry.instance().getKontoCache().isEmpty() == true) {
      kontoRow.setVisible(false);
    } else {
      kontoRow.setVisible(false);
      //      final KontoSelectPanel kontoSelectPanel = new KontoSelectPanel("kontoSelectPanel", new PropertyModel<KontoDO>(data, "konto"),
      //          parentPage, "kontoId");
      //      add(kontoSelectPanel);
      //      kontoSelectPanel.setKontoNumberRanges(AccountingConfig.getInstance().getDebitorsAccountNumberRanges()).init();
    }

    add(new MaxLengthTextField("referenz", new PropertyModel<String>(data, "referenz")));
    // DropDownChoice datumList
    final LabelValueChoiceRenderer<Long> datumChoiceRenderer = WicketUtils.getDatumChoiceRenderer(20);
    datumChoice = new DropDownChoice<Long>("datumList", new PropertyModel<Long>(this, "datumInMillis"), datumChoiceRenderer.getValues(),
        datumChoiceRenderer) {
      @Override
      public boolean isVisible()
      {
        return data.getDatum() == null;
      }
    };
    datumChoice.setNullValid(true);
    datumChoice.setRequired(false);
    add(datumChoice);
  }

  @Override
  protected void cloneRechnung()
  {
    parentPage.cloneRechnung();
  }

  @Override
  protected void validation()
  {
    final Long datumInMillis = datumChoice.getConvertedInput();
    if (datumInMillis != null) {
      final DayHolder day = new DayHolder(new java.sql.Date(datumInMillis));
      data.setDatum(day.getSQLDate());
      datumPanel.markModelAsChanged();
    } else if (datumPanel.getConvertedInput() == null) {
      addFieldRequiredError("fibu.rechnung.datum");
    }
    super.validation();
  }

  /**
   * @return null
   */
  public Long getDatumInMillis()
  {
    return null;
  }

  /**
   * Dummy method. Does nothing.
   * @param datumInMillis
   */
  public void setDatumInMillis(final Long datumInMillis)
  {
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected EingangsrechnungsPositionDO newPositionInstance()
  {
    return new EingangsrechnungsPositionDO();
  }
}
