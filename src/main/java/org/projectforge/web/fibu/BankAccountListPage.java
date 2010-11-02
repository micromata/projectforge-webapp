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

package org.projectforge.web.fibu;

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
import org.projectforge.fibu.BankAccountDO;
import org.projectforge.fibu.BankAccountDao;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;


@ListPage(editPage = BankAccountEditPage.class)
public class BankAccountListPage extends AbstractListPage<BankAccountListForm, BankAccountDao, BankAccountDO>
{
  private static final long serialVersionUID = -8406452960003792763L;

  @SpringBean(name = "bankAccountDao")
  private BankAccountDao bankAccountDao;

  public BankAccountListPage(PageParameters parameters)
  {
    super(parameters, "finance.bankAccount");
  }

  public BankAccountListPage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(caller, selectProperty, "finance.bankAccount");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    final List<IColumn<BankAccountDO>> columns = new ArrayList<IColumn<BankAccountDO>>();
    final CellItemListener<BankAccountDO> cellItemListener = new CellItemListener<BankAccountDO>() {
      public void populateItem(Item<ICellPopulator<BankAccountDO>> item, String componentId, IModel<BankAccountDO> rowModel)
      {
        final BankAccountDO bankAccount = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(bankAccount.getId(), bankAccount.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<BankAccountDO>(new Model<String>(getString("finance.bankAccount.number")),
        "accountNumber", "accountNumber", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final BankAccountDO bankAccount = (BankAccountDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, BankAccountEditPage.class, bankAccount.getId(), BankAccountListPage.this,
            String.valueOf(bankAccount.getAccountNumber())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns
        .add(new CellItemListenerPropertyColumn<BankAccountDO>(new Model<String>(getString("finance.bankAccount.name")), "name", "name", cellItemListener));
    columns
        .add(new CellItemListenerPropertyColumn<BankAccountDO>(new Model<String>(getString("finance.bankAccount.bank")), "bank", "bank", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<BankAccountDO>(
        new Model<String>(getString("finance.bankAccount.bankIdentificationCode")), "bankIdentificationCode", "bankIdentificationCode", cellItemListener));
    dataTable = createDataTable(columns, "accountNumber", true);
    form.add(dataTable);
  }

   @Override
  protected BankAccountListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new BankAccountListForm(this);
  }

  @Override
  protected BankAccountDao getBaseDao()
  {
    return bankAccountDao;
  }

  @Override
  protected IModel<BankAccountDO> getModel(BankAccountDO object)
  {
    return new DetachableDOModel<BankAccountDO, BankAccountDao>(object, getBaseDao());
  }

  protected BankAccountDao getBankAccountDao()
  {
    return bankAccountDao;
  }
}
