/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.liquidityplanning;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.CurrencyPropertyColumn;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@ListPage(editPage = LiquidityEntryEditPage.class)
public class LiquidityEntryListPage extends AbstractListPage<LiquidityEntryListForm, LiquidityEntryDao, LiquidityEntryDO> implements
IListPageColumnsCreator<LiquidityEntryDO>
{
  private static final long serialVersionUID = 9158903150132480532L;

  @SpringBean(name = "liquidityEntryDao")
  private LiquidityEntryDao liquidityEntryDao;

  public LiquidityEntryListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.liquidityplanning.entry");
  }

  @SuppressWarnings("serial")
  public List<IColumn<LiquidityEntryDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<LiquidityEntryDO, String>> columns = new ArrayList<IColumn<LiquidityEntryDO, String>>();
    final CellItemListener<LiquidityEntryDO> cellItemListener = new CellItemListener<LiquidityEntryDO>() {
      public void populateItem(final Item<ICellPopulator<LiquidityEntryDO>> item, final String componentId,
          final IModel<LiquidityEntryDO> rowModel)
      {
        final LiquidityEntryDO liquidityEntry = rowModel.getObject();
        appendCssClasses(item, liquidityEntry.getId(), liquidityEntry.isDeleted());
      }
    };

    columns.add(new CellItemListenerPropertyColumn<LiquidityEntryDO>(new Model<String>(
        getString("plugins.liquidityplanning.entry.dateOfPayment")), getSortable("dateOfPayment", sortable), "dateOfPayment",
        cellItemListener) {
      /**
       * @see org.projectforge.web.wicket.CellItemListenerPropertyColumn#populateItem(org.apache.wicket.markup.repeater.Item,
       *      java.lang.String, org.apache.wicket.model.IModel)
       */
      @Override
      public void populateItem(final Item<ICellPopulator<LiquidityEntryDO>> item, final String componentId,
          final IModel<LiquidityEntryDO> rowModel)
      {
        final LiquidityEntryDO liquidityEntry = rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, LiquidityEntryEditPage.class, liquidityEntry.getId(), returnToPage,
            DateTimeFormatter.instance().getFormattedDateTime(liquidityEntry.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CurrencyPropertyColumn<LiquidityEntryDO>(getString("fibu.common.betrag"), getSortable("amount", sortable), "amount",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<LiquidityEntryDO>(
        new Model<String>(getString("plugins.liquidityplanning.entry.subject")), getSortable("subject", sortable), "subject",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<LiquidityEntryDO>(getString("comment"), getSortable("comment", sortable), "comment",
        cellItemListener));

    return columns;
  }

  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "dateOfPayment", SortOrder.DESCENDING);
    form.add(dataTable);
  }

  @Override
  protected LiquidityEntryListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new LiquidityEntryListForm(this);
  }

  @Override
  protected LiquidityEntryDao getBaseDao()
  {
    return liquidityEntryDao;
  }

  protected LiquidityEntryDao getLiquidityEntryDao()
  {
    return liquidityEntryDao;
  }
}
