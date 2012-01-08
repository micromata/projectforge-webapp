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

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.fibu.AuftragFilter;
import org.projectforge.fibu.AuftragsPositionsArt;
import org.projectforge.fibu.AuftragsStatistik;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.YearListCoiceRenderer;


public class AuftragListForm extends AbstractListForm<AuftragListFilter, AuftragListPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AuftragListForm.class);

  private static final long serialVersionUID = -5969136444233092172L;

  private AuftragsStatistik auftragsStatistik;

  @SpringBean(name = "auftragDao")
  private AuftragDao auftragDao;

  @SuppressWarnings( "serial")
  @Override
  protected void init()
  {
    super.init();
    // DropDownChoice years
    final YearListCoiceRenderer yearListChoiceRenderer = new YearListCoiceRenderer(auftragDao.getYears(), true);
    @SuppressWarnings("unchecked")
    final DropDownChoice yearChoice = new DropDownChoice("year", new PropertyModel(this, "year"), yearListChoiceRenderer.getYears(),
        yearListChoiceRenderer);
    yearChoice.setNullValid(false);
    filterContainer. add(yearChoice);
    {
      // DropDownChoice listType
      final LabelValueChoiceRenderer<String> typeChoiceRenderer = new LabelValueChoiceRenderer<String>();
      for (String str : AuftragFilter.LIST) {
        typeChoiceRenderer.addValue(str, getString("fibu.auftrag.filter.type." + str));
      }
      typeChoiceRenderer.addValue("deleted", getString("deleted"));
      @SuppressWarnings("unchecked")
      final DropDownChoice typeChoice = new DropDownChoice("listType", new PropertyModel(this, "searchFilter.listType"), typeChoiceRenderer
          .getValues(), typeChoiceRenderer);
      typeChoice.setNullValid(false);
      filterContainer. add(typeChoice);
    }
    {
      // DropDownChoice Auftragsart
      final LabelValueChoiceRenderer<Integer> auftragsPositionsArtChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
      auftragsPositionsArtChoiceRenderer.addValue(-1, getString("filter.all"));
      for (AuftragsPositionsArt art : AuftragsPositionsArt.values()) {
        auftragsPositionsArtChoiceRenderer.addValue(art.ordinal(), getString(art.getI18nKey()));
      }
      @SuppressWarnings("unchecked")
      final DropDownChoice auftragsPositionsArtChoice = new DropDownChoice("auftragsPositionsArt", new PropertyModel(this, "auftragsPositionsArt"),
          auftragsPositionsArtChoiceRenderer.getValues(), auftragsPositionsArtChoiceRenderer);
      auftragsPositionsArtChoice.setNullValid(false);
      filterContainer. add(auftragsPositionsArtChoice);
    }
    final Label nettoLabel = new Label("netto", new Model<String>() {
      @Override
      public String getObject()
      {
        return getStatisticsValue("fibu.common.netto", getAuftragsStatistik().getNettoSum(), getAuftragsStatistik().getCounter());
      }
    });
    filterContainer. add(nettoLabel);

    final Label akquiseLabel = new Label("akquise", new Model<String>() {
      @Override
      public String getObject()
      {
        return ", " + getStatisticsValue("akquise", getAuftragsStatistik().getAkquiseSum(), getAuftragsStatistik().getCounterAkquise());
      }
    }) {
      @Override
      public boolean isVisible()
      {
        return (getAuftragsStatistik().getCounterAkquise() > 0);
      }
    };
    filterContainer. add(akquiseLabel);

    final Label beauftragtLabel = new Label("beauftragt", new Model<String>() {
      @Override
      public String getObject()
      {
        return ", "
            + getStatisticsValue("fibu.auftrag.status.beauftragt", getAuftragsStatistik().getBeauftragtSum(), getAuftragsStatistik()
                .getCounterBeauftragt());
      }
    }) {
      @Override
      public boolean isVisible()
      {
        return (getAuftragsStatistik().getCounterBeauftragt() > 0);
      }
    };
    filterContainer. add(beauftragtLabel);

    final Label fakturiertLabel = new Label("fakturiert", new Model<String>() {
      @Override
      public String getObject()
      {
        return ", "
            + getStatisticsValue("fibu.auftrag.filter.type.vollstaendigFakturiert", getAuftragsStatistik().getFakturiertSum(),
                getAuftragsStatistik().getCounterFakturiert());
      }
    }) {
      @Override
      public boolean isVisible()
      {
        return (getAuftragsStatistik().getCounterFakturiert() > 0);
      }
    };
    filterContainer. add(fakturiertLabel);

    final Label zufakturierenLabel = new Label("zufakturieren", new Model<String>() {
      @Override
      public String getObject()
      {
        return ", "
            + getStatisticsValue("fibu.auftrag.filter.type.abgeschlossenNichtFakturiert", getAuftragsStatistik().getZuFakturierenSum(),
                getAuftragsStatistik().getCounterZuFakturieren());
      }
    }) {
      @Override
      public boolean isVisible()
      {
        return (getAuftragsStatistik().getCounterZuFakturieren() > 0);
      }
    };
    filterContainer.add(zufakturierenLabel);
  }

  protected void refresh()
  {
    this.auftragsStatistik = null;
  }

  public Integer getYear()
  {
    return getSearchFilter().getYear();
  }

  public void setYear(Integer year)
  {
    if (year == null) {
      getSearchFilter().setYear(-1);
    } else {
      getSearchFilter().setYear(year);
    }
  }

  public Integer getAuftragsPositionsArt()
  {
    if (getSearchFilter().getAuftragsPositionsArt() != null) {
      return getSearchFilter().getAuftragsPositionsArt().ordinal();
    } else {
      return -1;
    }
  }

  public void setAuftragsPositionsArt(Integer auftragsPositionsArt)
  {
    if (auftragsPositionsArt == null || auftragsPositionsArt == -1) {
      getSearchFilter().setAuftragsPositionsArt(null);
    } else {
      getSearchFilter().setAuftragsPositionsArt(AuftragsPositionsArt.values()[auftragsPositionsArt]);
    }
  }

  public AuftragListForm(AuftragListPage parentPage)
  {
    super(parentPage);
  }

  private AuftragsStatistik getAuftragsStatistik()
  {
    if (auftragsStatistik == null) {
      auftragsStatistik = auftragDao.buildStatistik(getParentPage().getList());
    }
    return auftragsStatistik;
  }

  private String getStatisticsValue(String label, BigDecimal amount, int count)
  {
    return getString(label) + ": " + CurrencyFormatter.format(amount) + " (" + count + ")";
  }

  @Override
  protected AuftragListFilter newSearchFilterInstance()
  {
    return new AuftragListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
