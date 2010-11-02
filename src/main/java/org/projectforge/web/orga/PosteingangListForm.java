/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.orga;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.orga.PosteingangDao;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.YearListCoiceRenderer;


public class PosteingangListForm extends AbstractListForm<PosteingangListFilter, PosteingangListPage>
{
  private static final long serialVersionUID = 5594012692306669398L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PosteingangListForm.class);

  @SpringBean(name = "posteingangDao")
  private PosteingangDao posteingangDao;

  @Override
  protected void init()
  {
    super.init();
    // DropDownChoice years
    final YearListCoiceRenderer yearListChoiceRenderer = new YearListCoiceRenderer(posteingangDao.getYears(), true);
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
  }

  public PosteingangListForm(PosteingangListPage parentPage)
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
  protected PosteingangListFilter newSearchFilterInstance()
  {
    return new PosteingangListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
