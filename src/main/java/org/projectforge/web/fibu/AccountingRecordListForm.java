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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.fibu.kost.BuchungssatzDao;
import org.projectforge.fibu.kost.BusinessAssessment;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.YearListCoiceRenderer;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.IconPanel;
import org.projectforge.web.wicket.flowlayout.IconType;
import org.projectforge.web.wicket.flowlayout.TextStyle;

public class AccountingRecordListForm extends AbstractListForm<AccountingRecordListFilter, AccountingRecordListPage>
{
  private static final long serialVersionUID = -1669760774183582053L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccountingRecordListForm.class);

  @SpringBean(name = "buchungssatzDao")
  private BuchungssatzDao buchungssatzDao;

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    if (parentPage.reportId != null) {
      setPageSize(1000);
    }
    super.init();
    gridBuilder.newColumnsPanel();
    {
      gridBuilder.newColumnPanel(DivType.COL_60);
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true);
      // DropDownChoices from
      final YearListCoiceRenderer yearListChoiceRenderer = new YearListCoiceRenderer(buchungssatzDao.getYears(), false);
      final DropDownChoice<Integer> fromYearChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(), new PropertyModel<Integer>(this,
          "fromYear"), yearListChoiceRenderer.getYears(), yearListChoiceRenderer);
      fromYearChoice.setNullValid(false).setRequired(true);
      fs.add(fromYearChoice);
      final LabelValueChoiceRenderer<Integer> monthChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
      for (int i = 0; i <= 11; i++) {
        monthChoiceRenderer.addValue(i, StringHelper.format2DigitNumber(i + 1));
      }
      final DropDownChoice<Integer> fromMonthChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(), new PropertyModel<Integer>(
          this, "fromMonth"), monthChoiceRenderer.getValues(), monthChoiceRenderer);
      fromMonthChoice.setNullValid(true);
      fs.add(fromMonthChoice);

      fs.add(new DivTextPanel(fs.newChildId(), " - "));

      // DropDownChoices to
      final DropDownChoice<Integer> toYearChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(), new PropertyModel<Integer>(this,
          "toYear"), yearListChoiceRenderer.getYears(), yearListChoiceRenderer);
      toYearChoice.setNullValid(false).setRequired(true);
      fs.add(toYearChoice);

      final DropDownChoice<Integer> toMonthChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(), new PropertyModel<Integer>(this,
          "toMonth"), monthChoiceRenderer.getValues(), monthChoiceRenderer);
      toMonthChoice.setNullValid(true);
      fs.add(toMonthChoice);
    }
    {
      // DropDownChoice page size
      gridBuilder.newColumnPanel(DivType.COL_40);
      addPageSizeFieldset();
    }
    {
      // Statistics
      gridBuilder.newBlockPanel();
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.businessAssessment"), true).setNoLabelFor();
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          final BusinessAssessment bwa = parentPage.getBusinessAssessment();
          return getString("fibu.businessAssessment.overallPerformance")
              + ": "
              + CurrencyFormatter.format(bwa != null ? bwa.getOverallPerformanceRowAmount() : BigDecimal.ZERO)
              + WebConstants.HTML_TEXT_DIVIDER;
        }
      }, TextStyle.BLUE));
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          final BusinessAssessment bwa = parentPage.getBusinessAssessment();
          return getString("fibu.businessAssessment.merchandisePurchase")
              + ": "
              + CurrencyFormatter.format(bwa != null ? bwa.getMerchandisePurchaseRowAmount() : BigDecimal.ZERO)
              + WebConstants.HTML_TEXT_DIVIDER;
        }
      }));
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          final BusinessAssessment bwa = parentPage.getBusinessAssessment();
          return getString("fibu.businessAssessment.preliminaryResult")
              + ": "
              + CurrencyFormatter.format(bwa != null ? bwa.getPreliminaryResultRowAmount() : BigDecimal.ZERO);
        }
      }));

      final RepeatingView repeater = new RepeatingView(FieldsetPanel.DESCRIPTION_SUFFIX_ID) {
        /**
         * @see org.apache.wicket.Component#isVisible()
         */
        @Override
        public boolean isVisible()
        {
          return parentPage.getBusinessAssessment() != null;
        }
      };
      fs.setDescriptionSuffix(repeater);
      IconPanel icon = new IconPanel(repeater.newChildId(), IconType.CIRCLE_PLUS).setOnClick("javascript:showBusinessAssessment();");
      icon.setMarkupId("showBusinessAssessment");
      repeater.add(icon);
      icon = new IconPanel(repeater.newChildId(), IconType.CIRCLE_MINUS).setOnClick("javascript:hideBusinessAssessment();")
          .appendAttribute("style", "display: none;");
      icon.setMarkupId("hideBusinessAssessment");
      repeater.add(icon);
    }
    {
      gridBuilder.newBlockPanel();
      final DivPanel businessAssessmentPanel = gridBuilder.getPanel();
      businessAssessmentPanel.setMarkupId("businessAssessment");
      businessAssessmentPanel.add(AttributeModifier.append("style", "display: none;"));
      final FieldsetPanel fieldset = new FieldsetPanel(businessAssessmentPanel, "").setNoLabelFor();
      final Label label = new Label(DivTextPanel.WICKET_ID, new Model<String>() {
        /**
         * @see org.apache.wicket.model.Model#getObject()
         */
        @Override
        public String getObject()
        {
          final BusinessAssessment businessAssessment = parentPage.getBusinessAssessment();
          if (businessAssessment == null) {
            return "";
          }
          return businessAssessment.asHtml();
        }
      });
      label.setEscapeModelStrings(false);
      fieldset.add(new DivTextPanel(fieldset.newChildId(), label).setMarkupId("businessAssessment"));
    }
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

  public void setFromYear(final Integer year)
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

  public void setToYear(final Integer year)
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

  public void setFromMonth(final Integer month)
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

  public void setToMonth(final Integer month)
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
