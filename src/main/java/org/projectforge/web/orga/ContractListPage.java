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

package org.projectforge.web.orga;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.orga.ContractDO;
import org.projectforge.orga.ContractDao;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = ContractEditPage.class)
public class ContractListPage extends AbstractListPage<ContractListForm, ContractDao, ContractDO>
{
  private static final long serialVersionUID = 671935723386728113L;

  @SpringBean(name = "contractDao")
  private ContractDao contractDao;

  public ContractListPage(PageParameters parameters)
  {
    super(parameters, "legalAffaires.contract");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    final List<IColumn<ContractDO>> columns = new ArrayList<IColumn<ContractDO>>();

    final CellItemListener<ContractDO> cellItemListener = new CellItemListener<ContractDO>() {
      public void populateItem(Item<ICellPopulator<ContractDO>> item, String componentId, IModel<ContractDO> rowModel)
      {
        final ContractDO contract = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(contract.getId(), contract.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("legalAffaires.contract.number")), "number",
        "number", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final ContractDO contract = (ContractDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, ContractEditPage.class, contract.getId(), ContractListPage.this,
            NumberHelper.getAsString(contract.getNumber())));
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("date")), "date", "date", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("legalAffaires.contract.type")), "type", "type",
        cellItemListener));
    columns
        .add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("status")), "status", "status", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("title")), "title", "title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("legalAffaires.contract.coContractorA")),
        "coContractorA", "coContractorA", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("legalAffaires.contract.coContractorB")),
        "coContractorB", "coContractorB", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("resubmissionOnDate")), "resubmissionOnDate",
        "resubmissionOnDate", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<ContractDO>(new Model<String>(getString("dueDate")), "dueDate", "dueDate",
        cellItemListener));
    dataTable = createDataTable(columns, "number", false);
    form.add(dataTable);
  }

  @Override
  protected ContractListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new ContractListForm(this);
  }

  @Override
  protected ContractDao getBaseDao()
  {
    return contractDao;
  }

  @Override
  protected IModel<ContractDO> getModel(ContractDO object)
  {
    return new DetachableDOModel<ContractDO, ContractDao>(object, getBaseDao());
  }
}
