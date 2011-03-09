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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.fibu.kost.BuchungssatzDao;
import org.projectforge.fibu.kost.Bwa;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.YearListCoiceRenderer;

public class AccountingRecordListForm extends AbstractListForm<AccountingRecordListFilter, AccountingRecordListPage>
{
  private static final long serialVersionUID = -1669760774183582053L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccountingRecordListForm.class);

  @SpringBean(name = "buchungssatzDao")
  private BuchungssatzDao buchungssatzDao;

  private WebMarkupContainer businessAssessmentRow;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    if (parentPage.reportId != null) {
      setPageSize(1000);
    }
    super.init();
    // DropDownChoice years
    final YearListCoiceRenderer yearListChoiceRenderer = new YearListCoiceRenderer(buchungssatzDao.getYears(), false);
    @SuppressWarnings("unchecked")
    final DropDownChoice fromYearChoice = new DropDownChoice("fromYear", new PropertyModel(this, "fromYear"), yearListChoiceRenderer
        .getYears(), yearListChoiceRenderer);
    fromYearChoice.setNullValid(false).setRequired(true);
    filterContainer.add(fromYearChoice);
    @SuppressWarnings("unchecked")
    final DropDownChoice toYearChoice = new DropDownChoice("toYear", new PropertyModel(this, "toYear"), yearListChoiceRenderer.getYears(),
        yearListChoiceRenderer);
    toYearChoice.setNullValid(false).setRequired(true);
    filterContainer.add(toYearChoice);

    // DropDownChoice months
    final LabelValueChoiceRenderer<Integer> monthChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (int i = 0; i <= 11; i++) {
      monthChoiceRenderer.addValue(i, StringHelper.format2DigitNumber(i + 1));
    }
    @SuppressWarnings("unchecked")
    final DropDownChoice fromMonthChoice = new DropDownChoice("fromMonth", new PropertyModel(this, "fromMonth"), monthChoiceRenderer
        .getValues(), monthChoiceRenderer);
    fromMonthChoice.setNullValid(true);
    filterContainer.add(fromMonthChoice);
    @SuppressWarnings("unchecked")
    final DropDownChoice toMonthChoice = new DropDownChoice("toMonth", new PropertyModel(this, "toMonth"), monthChoiceRenderer.getValues(),
        monthChoiceRenderer);
    toMonthChoice.setNullValid(true);
    filterContainer.add(toMonthChoice);

    filterContainer.add(businessAssessmentRow = new WebMarkupContainer("businessAssessmentRow") {
      public boolean isVisible()
      {
        return parentPage.bwa != null;
      };
    });
    final Label summaryBusinessAssessmentLabel = new Label("summaryBusinessAssessment", new Model<String>() {
      @Override
      public String getObject()
      {
        final Bwa bwa = parentPage.bwa;
        if (bwa == null) {
          return "";
        }
        final StringBuffer buf = new StringBuffer();
        buf.append(getString("fibu.businessAssessment.overallPerformance")).append(": ").append(
            CurrencyFormatter.format(bwa.getGesamtleistung().getBwaWert())).append(", ");
        buf.append(getString("fibu.businessAssessment.merchandisePurchase")).append(": ").append(
            CurrencyFormatter.format(bwa.getMatWareneinkauf().getBwaWert())).append(", ");
        buf.append(getString("fibu.businessAssessment.preliminaryResult")).append(": ").append(
            CurrencyFormatter.format(bwa.getVorlaeufigesErgebnis().getBwaWert()));
        return buf.toString();
      }
    });
    businessAssessmentRow.add(summaryBusinessAssessmentLabel);

    final Label businessAssessmentLabel = new Label("businessAssessment", new Model<String>() {
      @Override
      public String getObject()
      {
        final Bwa bwa = parentPage.bwa;
        if (bwa == null) {
          return "";
        }
        return bwa.toString();
      }
    });
    filterContainer.add(businessAssessmentLabel);
  }
  
  /**
   * The filter is not visible if only a fixed list of accounting records of a record is displayed. 
   * @see org.projectforge.web.wicket.AbstractListForm#isFilterVisible()
   */
  @Override
  protected boolean isFilterVisible()
  {
    return (parentPage.reportId == null);
  }

  protected void refresh()
  {
  }

  public Integer getFromYear()
  {
    return getSearchFilter().getFromYear();
  }

  public void setFromYear(Integer year)
  {
    if (year == null) {
      getSearchFilter().setFromYear(-1);
    } else {
      getSearchFilter().setFromYear(year);
    }
  }

  public Integer getToYear()
  {
    return getSearchFilter().getToYear();
  }

  public void setToYear(Integer year)
  {
    if (year == null) {
      getSearchFilter().setToYear(-1);
    } else {
      getSearchFilter().setToYear(year);
    }
  }

  public Integer getFromMonth()
  {
    return getSearchFilter().getFromMonth();
  }

  public void setFromMonth(Integer month)
  {
    if (month == null) {
      getSearchFilter().setFromMonth(-1);
    } else {
      getSearchFilter().setFromMonth(month);
    }
  }

  public Integer getToMonth()
  {
    return getSearchFilter().getToMonth();
  }

  public void setToMonth(Integer month)
  {
    if (month == null) {
      getSearchFilter().setToMonth(-1);
    } else {
      getSearchFilter().setToMonth(month);
    }
  }

  public AccountingRecordListForm(final AccountingRecordListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected AccountingRecordListFilter newSearchFilterInstance()
  {
    return new AccountingRecordListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
