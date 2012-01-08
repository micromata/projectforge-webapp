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

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.common.StringHelper;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.fibu.EingangsrechnungDao;
import org.projectforge.fibu.EingangsrechnungsStatistik;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.TooltipImage;
import org.projectforge.web.wicket.components.YearListCoiceRenderer;


public class EingangsrechnungListForm extends AbstractListForm<EingangsrechnungListFilter, EingangsrechnungListPage>
{
  private static final long serialVersionUID = 2678813484329104564L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EingangsrechnungListForm.class);

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    {
      final EingangsrechnungDao eingangsrechnungDao = getParentPage().getBaseDao();
      // DropDownChoice years
      final YearListCoiceRenderer yearListChoiceRenderer = new YearListCoiceRenderer(eingangsrechnungDao.getYears(), true);
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

      // Radio choices:
      final RadioGroup<String> filterType = new RadioGroup<String>("filterType", new PropertyModel<String>(getSearchFilter(), "listType"));
      filterType.add(new Radio<String>("all", new Model<String>("all"))); // filter.all
      filterType.add(new Radio<String>("unbezahlt", new Model<String>("unbezahlt"))); // fibu.rechnung.filter.unbezahlt
      filterType.add(new Radio<String>("ueberfaellig", new Model<String>("ueberfaellig"))); // fibu.rechnung.filter.ueberfaellig
      filterType.add(new Radio<String>("deleted", new Model<String>("deleted"))); // deleted
      filterContainer.add(filterType);
      filterContainer.add(new CheckBox("showKostZuweisungStatus", new PropertyModel<Boolean>(getSearchFilter(), "showKostZuweisungStatus")));

      filterContainer.add(new Label("brutto", new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("fibu.common.brutto") + ": " + CurrencyFormatter.format(getStats().getBrutto()) + ", ";
        }
      }).setRenderBodyOnly(true));
      filterContainer.add(new Label("netto", new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("fibu.common.netto") + ": " + CurrencyFormatter.format(getStats().getNetto()) + ", ";
        }
      }).setRenderBodyOnly(true));
      filterContainer.add(new Label("offen", new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("fibu.rechnung.offen") + ": " + CurrencyFormatter.format(getStats().getOffen()) + ", ";
        }
      })); // .setRenderBodyOnly(false): style attribute needed.
      filterContainer.add(new Label("ueberfaellig", new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("fibu.rechnung.filter.ueberfaellig") + ": " + CurrencyFormatter.format(getStats().getUeberfaellig()) + ", ";
        }
      })); // .setRenderBodyOnly(false): style attribute needed.
      filterContainer.add(new Label("skonto", new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("fibu.rechnung.skonto") + ": " + CurrencyFormatter.format(getStats().getSkonto()) + ", ";
        }
      }).setRenderBodyOnly(true));
      filterContainer.add(new Label("zahlungszielAverage", new Model<String>() {
        @Override
        public String getObject()
        {
          return String.valueOf(getStats().getZahlungszielAverage());
        }
      }).setRenderBodyOnly(true));
      filterContainer.add(new Label("tatsaechlichesZahlungzielAverage", new Model<String>() {
        @Override
        public String getObject()
        {
          return String.valueOf(getStats().getTatsaechlichesZahlungzielAverage());
        }
      }).setRenderBodyOnly(true));
      filterContainer.add(new Label("counter", new Model<String>() {
        @Override
        public String getObject()
        {
          return String.valueOf(getStats().getCounter());
        }
      }).setRenderBodyOnly(true));
      filterContainer.add(new Label("counterBezahlt", new Model<String>() {
        @Override
        public String getObject()
        {
          return String.valueOf(getStats().getCounterBezahlt());
        }
      }).setRenderBodyOnly(true));
      {
        final SubmitLink exportExcelButton = new SubmitLink("exportExcel") {
          public void onSubmit()
          {
            parentPage.exportExcel();
          };
        };
        filterContainer.add(exportExcelButton);
        exportExcelButton.add(new TooltipImage("exportExcelImage", getResponse(), WebConstants.IMAGE_EXPORT_EXCEL,
            getString("tooltip.export.excel")));
      }
    }
  }

  EingangsrechnungsStatistik getStats()
  {
    return parentPage.getEingangsrechnungsStatistik();
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

  public EingangsrechnungListForm(EingangsrechnungListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected EingangsrechnungListFilter newSearchFilterInstance()
  {
    return new EingangsrechnungListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
