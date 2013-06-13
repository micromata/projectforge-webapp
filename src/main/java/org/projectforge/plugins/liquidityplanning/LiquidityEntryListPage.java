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
import java.util.Date;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.DayHolder;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.CurrencyPropertyColumn;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.RowCssClass;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.flowlayout.IconPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@ListPage(editPage = LiquidityEntryEditPage.class)
public class LiquidityEntryListPage extends AbstractListPage<LiquidityEntryListForm, LiquidityEntryDao, LiquidityEntryDO> implements
IListPageColumnsCreator<LiquidityEntryDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LiquidityEntryListPage.class);

  private static final long serialVersionUID = 9158903150132480532L;

  @SpringBean(name = "liquidityEntryDao")
  private LiquidityEntryDao liquidityEntryDao;

  private LiquidityEntriesStatistics statistics;

  public LiquidityEntryListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.liquidityplanning.entry");
  }

  LiquidityEntriesStatistics getStatistics()
  {
    if (statistics == null) {
      statistics = liquidityEntryDao.buildStatistics(getList());
    }
    return statistics;
  }

  @SuppressWarnings("serial")
  public List<IColumn<LiquidityEntryDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<LiquidityEntryDO, String>> columns = new ArrayList<IColumn<LiquidityEntryDO, String>>();
    final Date today = new DayHolder().getDate();
    final CellItemListener<LiquidityEntryDO> cellItemListener = new CellItemListener<LiquidityEntryDO>() {
      public void populateItem(final Item<ICellPopulator<LiquidityEntryDO>> item, final String componentId,
          final IModel<LiquidityEntryDO> rowModel)
      {
        final LiquidityEntryDO liquidityEntry = rowModel.getObject();
        appendCssClasses(item, liquidityEntry.getId(), liquidityEntry.isDeleted());
        if (liquidityEntry.isDeleted() == true) {
          // Do nothing further
        } else {
          if (liquidityEntry.isPayed() == false) {
            if (liquidityEntry.getDateOfPayment() == null || liquidityEntry.getDateOfPayment().before(today) == true) {
              appendCssClasses(item, RowCssClass.IMPORTANT_ROW);
            } else {
              appendCssClasses(item, RowCssClass.BLUE);
            }
          }
        }
      }
    };

    columns.add(new CellItemListenerPropertyColumn<LiquidityEntryDO>(LiquidityEntryDO.class, getSortable("dateOfPayment", sortable),
        "dateOfPayment", cellItemListener) {
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
            DateTimeFormatter.instance().getFormattedDate(liquidityEntry.getDateOfPayment())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CurrencyPropertyColumn<LiquidityEntryDO>(LiquidityEntryDO.class, getSortable("amount", sortable), "amount",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<LiquidityEntryDO>(LiquidityEntryDO.class,
        getSortable("payed", sortable), "payed", cellItemListener) {
      @Override
      public void populateItem(final Item<ICellPopulator<LiquidityEntryDO>> item, final String componentId,
          final IModel<LiquidityEntryDO> rowModel)
      {
        final LiquidityEntryDO entry = rowModel.getObject();
        if (entry.isPayed() == true) {
          item.add(new IconPanel(componentId, IconType.ACCEPT));
        } else {
          item.add(createInvisibleDummyComponent(componentId));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<LiquidityEntryDO>(LiquidityEntryDO.class, getSortable("subject", sortable), "subject",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<LiquidityEntryDO>(LiquidityEntryDO.class, getSortable("comment", sortable), "comment",
        cellItemListener));

    return columns;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "dateOfPayment", SortOrder.DESCENDING);
    form.add(dataTable);
    final ContentMenuEntryPanel exportExcelButton = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
      @Override
      public void onClick()
      {
        // exportExcel();
      };
    }, getString("exportAsXls")).setTooltip(getString("tooltip.export.excel"));
    addContentMenuEntry(exportExcelButton);
  }

  // void exportExcel()
  // {
  // refresh();
  // final List<LiquidityEntryDO> entries = getList();
  // if (entries == null || entries.size() == 0) {
  // // Nothing to export.
  // form.addError("validation.error.nothingToExport");
  // return;
  // }
  // final String filename = "ProjectForge-liquidity_" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".xls";
  //
  // final ExportWorkbook xls = new ExportWorkbook();
  // final ContentProvider contentProvider = new MyXlsContentProvider(xls);
  // // create a default Date format and currency column
  // xls.setContentProvider(contentProvider);
  //
  // final ExportSheet sheet = xls.addSheet("plugins.liquidityplanning.entry.title.heading");
  // sheet.createFreezePane(0, 1);
  //
  // final ExportColumn[] cols = new ExportColumn[5];
  // int i = 0;
  // for (final ExcelCol col : ExcelCol.values()) {
  // cols[i++] = new I18nExportColumn(col, col.theTitle, col.width);
  // }
  //
  //
  // // column property names
  // sheet.setColumns(cols);
  //
  // final ContentProvider sheetProvider = sheet.getContentProvider();
  // sheetProvider.putFormat(InvoicesCol.BRUTTO, "#,##0.00;[Red]-#,##0.00");
  // sheetProvider.putFormat(InvoicesCol.KORREKTUR, "#,##0.00;[Red]-#,##0.00");
  // sheetProvider.putFormat(InvoicesCol.KOST1, "#");
  // sheetProvider.putFormat(InvoicesCol.KOST2, "#");
  // sheetProvider.putFormat(InvoicesCol.DATE, "dd.MM.yyyy");
  //
  // final PropertyMapping mapping = new PropertyMapping();
  // for (final KostZuweisungDO zuweisung : list) {
  // final AbstractRechnungsPositionDO position;
  // final AbstractRechnungDO< ? > rechnung;
  // final String referenz;
  // final String text;
  // if (zuweisung.getRechnungsPosition() != null) {
  // position = zuweisung.getRechnungsPosition();
  // rechnung = ((RechnungsPositionDO) position).getRechnung();
  // final RechnungDO r = (RechnungDO) rechnung;
  // referenz = String.valueOf(r.getNummer());
  // text = ProjektFormatter.formatProjektKundeAsString(r.getProjekt(), r.getKunde(), r.getKundeText());
  // } else {
  // position = zuweisung.getEingangsrechnungsPosition();
  // rechnung = ((EingangsrechnungsPositionDO) position).getEingangsrechnung();
  // final EingangsrechnungDO r = (EingangsrechnungDO) rechnung;
  // referenz = r.getReferenz();
  // text = r.getKreditor();
  // }
  // final BigDecimal grossSum = position.getBruttoSum();
  //
  // BigDecimal korrektur = null;
  // if (grossSum.compareTo(position.getKostZuweisungGrossSum()) != 0) {
  // korrektur = CurrencyHelper.getGrossAmount(position.getKostZuweisungNetFehlbetrag(), position.getVat());
  // if (NumberHelper.isZeroOrNull(korrektur) == true) {
  // korrektur = null;
  // }
  // }
  // mapping.add(InvoicesCol.BRUTTO, zuweisung.getBrutto());
  // Integer kontoNummer = null;
  // if (rechnung instanceof RechnungDO) {
  // final KontoDO konto = kontoCache.getKonto(((RechnungDO) rechnung));
  // if (konto != null) {
  // kontoNummer = konto.getNummer();
  // }
  // } else if (rechnung instanceof EingangsrechnungDO) {
  // final Integer kontoId = ((EingangsrechnungDO) rechnung).getKontoId();
  // if (kontoId != null) {
  // final KontoDO konto = kontoCache.getKonto(kontoId);
  // if (konto != null) {
  // kontoNummer = konto.getNummer();
  // }
  // }
  // }
  // mapping.add(InvoicesCol.KONTO, kontoNummer != null ? kontoNummer : "");
  // mapping.add(InvoicesCol.REFERENZ, StringHelper.removeNonDigitsAndNonASCIILetters(referenz));
  // mapping.add(InvoicesCol.DATE, rechnung.getDatum());
  // mapping.add(InvoicesCol.GEGENKONTO, "");
  // mapping.add(InvoicesCol.KOST1, zuweisung.getKost1() != null ? zuweisung.getKost1().getNummer() : "");
  // mapping.add(InvoicesCol.KOST2, zuweisung.getKost2() != null ? zuweisung.getKost2().getNummer() : "");
  // mapping.add(InvoicesCol.TEXT, text);
  // mapping.add(InvoicesCol.KORREKTUR, korrektur);
  // sheet.addRow(mapping.getMapping(), 0);
  // }
  // addAccounts(xls, contentProvider);
  // return xls.getAsByteArray();
  //
  // final byte[] xls = KostZuweisungExport.instance.exportRechnungen(rechnungen, getString("fibu.common.debitor"), kontoCache);
  // if (xls == null || xls.length == 0) {
  // log.error("Oups, xls has zero size. Filename: " + filename);
  // return;
  // }
  // DownloadUtils.setDownloadTarget(xls, filename);
  // }

  /**
   * Forces the statistics to be reloaded.
   * @see org.projectforge.web.wicket.AbstractListPage#refresh()
   */
  @Override
  public void refresh()
  {
    super.refresh();
    this.statistics = null;
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
