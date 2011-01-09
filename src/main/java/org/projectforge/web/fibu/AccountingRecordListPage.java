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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.BuchungssatzDao;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.CurrencyPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = AccountingRecordEditPage.class)
public class AccountingRecordListPage extends AbstractListPage<AccountingRecordListForm, BuchungssatzDao, BuchungssatzDO> implements
    IListPageColumnsCreator<BuchungssatzDO>
{
  private static final long serialVersionUID = -34213362189153025L;

  @SpringBean(name = "buchungssatzDao")
  private BuchungssatzDao buchungssatzDao;

  public AccountingRecordListPage(final PageParameters parameters)
  {
    super(parameters, "fibu.buchungssatz");
    // TODO: Don't show filter if called by ReportingObjectives page.
  }

  @Override
  protected void init()
  {
    final List<IColumn<BuchungssatzDO>> columns = createColumns(this, true);

    dataTable = createDataTable(columns, "formattedSatzNummer", true);
    form.add(dataTable);
  }

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<BuchungssatzDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<BuchungssatzDO>> columns = new ArrayList<IColumn<BuchungssatzDO>>();
    final CellItemListener<BuchungssatzDO> cellItemListener = new CellItemListener<BuchungssatzDO>() {
      public void populateItem(Item<ICellPopulator<BuchungssatzDO>> item, String componentId, IModel<BuchungssatzDO> rowModel)
      {
        final BuchungssatzDO satz = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(satz.getId(), satz.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<BuchungssatzDO>(new Model<String>(getString("fibu.buchungssatz.satznr")),
        "formattedSatzNummer", "formattedSatzNummer", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final BuchungssatzDO satz = (BuchungssatzDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, AccountingRecordEditPage.class, satz.getId(),
            AccountingRecordListPage.this, String.valueOf(satz.getFormattedSatzNummer())));
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CurrencyPropertyColumn<BuchungssatzDO>(getString("fibu.common.betrag"), "betrag", "betrag", cellItemListener));
    columns
        .add(new CellItemListenerPropertyColumn<BuchungssatzDO>(getString("fibu.buchungssatz.beleg"), "beleg", "beleg", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<BuchungssatzDO>(new Model<String>(getString("fibu.kost1")), getSortable(
        "kost1.shortDisplayName", sortable), "kost1.shortDisplayName", cellItemListener) {
      @Override
      public String getTooltip(final BuchungssatzDO satz)
      {
        final Kost1DO kost1 = satz != null ? satz.getKost1() : null;
        if (kost1 == null) {
          return null;
        } else {
          return KostFormatter.formatToolTip(kost1);
        }
      }
    });
    columns.add(new CellItemListenerPropertyColumn<BuchungssatzDO>(new Model<String>(getString("fibu.kost2")), getSortable(
        "kost2.shortDisplayName", sortable), "kost2.shortDisplayName", cellItemListener) {
      @Override
      public String getTooltip(final BuchungssatzDO satz)
      {
        final Kost2DO kost2 = satz != null ? satz.getKost2() : null;
        if (kost2 == null) {
          return null;
        } else {
          return KostFormatter.formatToolTip(kost2);
        }
      }
    });
    columns.add(new CellItemListenerPropertyColumn<BuchungssatzDO>(new Model<String>(getString("fibu.buchungssatz.konto")), getSortable(
        "konto.shortDisplayName", sortable), "konto.shortDisplayName", cellItemListener) {
      @Override
      public String getTooltip(final BuchungssatzDO satz)
      {
        final KontoDO konto = satz != null ? satz.getKonto() : null;
        if (konto == null) {
          return null;
        } else {
          return konto.getBezeichnung();
        }
      }
    });
    columns.add(new CellItemListenerPropertyColumn<BuchungssatzDO>(new Model<String>(getString("fibu.buchungssatz.gegenKonto")),
        getSortable("gegenKonto.shortDisplayName", sortable), "gegenKonto.shortDisplayName", cellItemListener) {
      @Override
      public String getTooltip(final BuchungssatzDO satz)
      {
        final KontoDO gegenKonto = satz != null ? satz.getGegenKonto() : null;
        if (gegenKonto == null) {
          return null;
        } else {
          return gegenKonto.getBezeichnung();
        }
      }
    });
    columns
    .add(new CellItemListenerPropertyColumn<BuchungssatzDO>(getString("finance.accountingRecord.dc"), "sh", "sh", cellItemListener));
    columns
    .add(new CellItemListenerPropertyColumn<BuchungssatzDO>(getString("fibu.buchungssatz.text"), "text", "text", cellItemListener));
    columns
    .add(new CellItemListenerPropertyColumn<BuchungssatzDO>(getString("comment"), "comment", "comment", cellItemListener));
    return columns;
  }

  @Override
  public void refresh()
  {
    super.refresh();
    form.refresh();
  }

  @Override
  protected AccountingRecordListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new AccountingRecordListForm(this);
  }

  @Override
  protected BuchungssatzDao getBaseDao()
  {
    return buchungssatzDao;
  }

  @Override
  public List<BuchungssatzDO> getList()
  {
    if (list == null) {
      list = super.getList();
      // buchungssatzDao.calculateInvoicedSum(list);
    }
    return list;
  }

  @Override
  protected IModel<BuchungssatzDO> getModel(BuchungssatzDO object)
  {
    return new DetachableDOModel<BuchungssatzDO, BuchungssatzDao>(object, getBaseDao());
  }
}
