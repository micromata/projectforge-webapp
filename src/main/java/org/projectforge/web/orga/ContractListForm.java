/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.orga.ContractDao;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.YearListCoiceRenderer;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class ContractListForm extends AbstractListForm<ContractListFilter, ContractListPage>
{
  private static final long serialVersionUID = -2813402079364322428L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ContractListForm.class);

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    final ContractDao contractDao = getParentPage().getBaseDao();
    gridBuilder.newColumnsPanel();
    {
      gridBuilder.newColumnPanel(DivType.COL_60);
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true);
      // DropDownChoice years
      final YearListCoiceRenderer yearListChoiceRenderer = new YearListCoiceRenderer(contractDao.getYears(), true);
      final DropDownChoice<Integer> yearChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(), new PropertyModel<Integer>(this,
          "year"), yearListChoiceRenderer.getYears(), yearListChoiceRenderer) {
        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
          return true;
        }
      };
      yearChoice.setNullValid(false);
      fs.add(yearChoice);
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.add(createOnlyDeletedCheckBoxPanel(checkBoxPanel.newChildId()));
    }
    {
      // DropDownChoice page size
      gridBuilder.newColumnPanel(DivType.COL_40);
      addPageSizeFieldset();
    }
  }

  public ContractListForm(final ContractListPage parentPage)
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

  @Override
  protected ContractListFilter newSearchFilterInstance()
  {
    return new ContractListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
