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

package org.projectforge.fibu.kost;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.Order;
import org.projectforge.common.CurrencyHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.core.QueryFilter;
import org.projectforge.export.ContentProvider;
import org.projectforge.export.ExportColumn;
import org.projectforge.export.ExportSheet;
import org.projectforge.export.ExportWorkbook;
import org.projectforge.export.I18nExportColumn;
import org.projectforge.export.PropertyMapping;
import org.projectforge.export.XlsContentProvider;
import org.projectforge.fibu.AbstractRechnungDO;
import org.projectforge.fibu.AbstractRechnungsPositionDO;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.EingangsrechnungsPositionDO;
import org.projectforge.fibu.KontoCache;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KontoDao;
import org.projectforge.fibu.ProjektFormatter;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserContext;

/**
 * For excel export.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class KostZuweisungExport
{
  public static final KostZuweisungExport instance = new KostZuweisungExport();

  private class MyContentProvider extends XlsContentProvider
  {
    public MyContentProvider(final ExportWorkbook workbook)
    {
      super(workbook);
    }

    @Override
    public ContentProvider newInstance()
    {
      return new MyContentProvider(this.workbook);
    }
  };

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(KostZuweisungExport.class);

  private enum InvoicesCol
  {
    BRUTTO("fibu.common.brutto", XlsContentProvider.LENGTH_CURRENCY), //
    KONTO("fibu.buchungssatz.konto", 14), //
    REFERENZ("fibu.common.reference", XlsContentProvider.LENGTH_STD), //
    DATE("date", XlsContentProvider.LENGTH_DATE), //
    GEGENKONTO("fibu.buchungssatz.gegenKonto", 14), //
    KOST1("fibu.kost1", XlsContentProvider.LENGTH_KOSTENTRAEGER), //
    KOST2("fibu.kost2", XlsContentProvider.LENGTH_KOSTENTRAEGER), //
    TEXT("description", XlsContentProvider.LENGTH_EXTRA_LONG), //
    KORREKTUR("fibu.common.fehlBetrag", XlsContentProvider.LENGTH_CURRENCY);

    final String theTitle;

    final int width;

    InvoicesCol(final String theTitle, final int width)
    {
      this.theTitle = theTitle;
      this.width = (short) width;
    }
  }

  /**
   * Export all cost assignements of the given invoices as excel list.
   * @param list
   * @return
   */
  public byte[] exportRechnungen(final List< ? extends AbstractRechnungDO< ? extends AbstractRechnungsPositionDO>> list,
      final String sheetTitle, final KontoCache kontoCache)
  {
    final List<KostZuweisungDO> zuweisungen = new ArrayList<KostZuweisungDO>();
    for (final AbstractRechnungDO< ? > rechnung : list) {
      if (rechnung.getPositionen() != null) {
        for (final AbstractRechnungsPositionDO position : rechnung.getPositionen()) {
          if (CollectionUtils.isNotEmpty(position.getKostZuweisungen()) == true) {
            zuweisungen.addAll(position.getKostZuweisungen());
          } else {
            final KostZuweisungDO zuweisung = new KostZuweisungDO();
            if (position instanceof RechnungsPositionDO) {
              zuweisung.setRechnungsPosition((RechnungsPositionDO) position);
            } else {
              zuweisung.setEingangsrechnungsPosition((EingangsrechnungsPositionDO) position);
            }
            zuweisungen.add(zuweisung);
          }
        }
      }
    }
    return export(zuweisungen, sheetTitle, kontoCache);
  }

  /**
   * Exports the filtered list as table.
   */
  public byte[] export(final List<KostZuweisungDO> list, final String sheetTitle, final KontoCache kontoCache)
  {
    log.info("Exporting kost zuweisung list.");
    final ExportWorkbook xls = new ExportWorkbook();
    final ContentProvider contentProvider = new MyContentProvider(xls);
    // create a default Date format and currency column
    xls.setContentProvider(contentProvider);

    final ExportSheet sheet = xls.addSheet(sheetTitle);
    sheet.createFreezePane(0, 1);

    final ExportColumn[] cols = new ExportColumn[InvoicesCol.values().length];
    int i = 0;
    for (final InvoicesCol col : InvoicesCol.values()) {
      cols[i++] = new I18nExportColumn(col, col.theTitle, col.width);
    }

    // column property names
    sheet.setColumns(cols);

    final ContentProvider sheetProvider = sheet.getContentProvider();
    sheetProvider.putFormat(InvoicesCol.BRUTTO, "#,##0.00;[Red]-#,##0.00");
    sheetProvider.putFormat(InvoicesCol.KORREKTUR, "#,##0.00;[Red]-#,##0.00");
    sheetProvider.putFormat(InvoicesCol.KOST1, "#");
    sheetProvider.putFormat(InvoicesCol.KOST2, "#");
    sheetProvider.putFormat(InvoicesCol.DATE, "dd.MM.yyyy");

    final PropertyMapping mapping = new PropertyMapping();
    for (final KostZuweisungDO zuweisung : list) {
      final AbstractRechnungsPositionDO position;
      final AbstractRechnungDO< ? > rechnung;
      final String referenz;
      final String text;
      if (zuweisung.getRechnungsPosition() != null) {
        position = zuweisung.getRechnungsPosition();
        rechnung = ((RechnungsPositionDO) position).getRechnung();
        final RechnungDO r = (RechnungDO) rechnung;
        referenz = String.valueOf(r.getNummer());
        text = ProjektFormatter.formatProjektKundeAsString(r.getProjekt(), r.getKunde(), r.getKundeText());
      } else {
        position = zuweisung.getEingangsrechnungsPosition();
        rechnung = ((EingangsrechnungsPositionDO) position).getEingangsrechnung();
        final EingangsrechnungDO r = (EingangsrechnungDO) rechnung;
        referenz = r.getReferenz();
        text = r.getKreditor();
      }
      final BigDecimal grossSum = position.getBruttoSum();

      BigDecimal korrektur = null;
      if (grossSum.compareTo(position.getKostZuweisungGrossSum()) != 0) {
        korrektur = CurrencyHelper.getGrossAmount(position.getKostZuweisungNetFehlbetrag(), position.getVat());
        if (NumberHelper.isZeroOrNull(korrektur) == true) {
          korrektur = null;
        }
      }
      mapping.add(InvoicesCol.BRUTTO, zuweisung.getBrutto());
      Integer kontoNummer = null;
      if (rechnung instanceof RechnungDO) {
        final KontoDO konto = kontoCache.getKonto(((RechnungDO) rechnung));
        if (konto != null) {
          kontoNummer = konto.getNummer();
        }
      } else if (rechnung instanceof EingangsrechnungDO) {
        final Integer kontoId = ((EingangsrechnungDO) rechnung).getKontoId();
        if (kontoId != null) {
          final KontoDO konto = kontoCache.getKonto(kontoId);
          if (konto != null) {
            kontoNummer = konto.getNummer();
          }
        }
      }
      mapping.add(InvoicesCol.KONTO, kontoNummer != null ? kontoNummer : "");
      mapping.add(InvoicesCol.REFERENZ, StringHelper.removeNonDigitsAndNonASCIILetters(referenz));
      mapping.add(InvoicesCol.DATE, rechnung.getDatum());
      mapping.add(InvoicesCol.GEGENKONTO, "");
      mapping.add(InvoicesCol.KOST1, zuweisung.getKost1() != null ? zuweisung.getKost1().getNummer() : "");
      mapping.add(InvoicesCol.KOST2, zuweisung.getKost2() != null ? zuweisung.getKost2().getNummer() : "");
      mapping.add(InvoicesCol.TEXT, text);
      mapping.add(InvoicesCol.KORREKTUR, korrektur);
      sheet.addRow(mapping.getMapping(), 0);
    }
    addAccounts(xls, contentProvider);
    return xls.getAsByteArray();
  }

  private enum AccountsCol
  {
    NUMBER("fibu.konto.nummer", 16), //
    NAME("fibu.konto.bezeichnung", XlsContentProvider.LENGTH_STD), //
    STATUS("status", 14), //
    DATE_OF_LAST_MODIFICATION("lastUpdate", XlsContentProvider.LENGTH_TIMESTAMP), //
    DATE_OF_CREATION("created", XlsContentProvider.LENGTH_TIMESTAMP), //
    DESCRIPTION("comment", XlsContentProvider.LENGTH_EXTRA_LONG);

    final String theTitle;

    final int width;

    AccountsCol(final String theTitle, final int width)
    {
      this.theTitle = theTitle;
      this.width = (short) width;
    }
  }

  private void addAccounts(final ExportWorkbook xls, final ContentProvider contentProvider)
  {
    final ExportSheet sheet = xls.addSheet(PFUserContext.getLocalizedString("fibu.konto.konten"));
    sheet.createFreezePane(0, 1);

    final ExportColumn[] cols = new ExportColumn[AccountsCol.values().length];
    int i = 0;
    for (final AccountsCol col : AccountsCol.values()) {
      cols[i++] = new I18nExportColumn(col, col.theTitle, col.width);
    }

    // column property names
    sheet.setColumns(cols);

    final ContentProvider sheetProvider = sheet.getContentProvider();
    sheetProvider.putFormat(AccountsCol.DATE_OF_LAST_MODIFICATION, "dd.MM.yyyy HH:mm");
    sheetProvider.putFormat(AccountsCol.DATE_OF_CREATION, "dd.MM.yyyy HH:mm");
    sheetProvider.putFormat(AccountsCol.NUMBER, "#");

    final KontoDao kontoDao = Registry.instance().getDao(KontoDao.class);
    final QueryFilter filter = new QueryFilter();
    filter.addOrder(Order.desc("lastUpdate"));
    final List<KontoDO> list = kontoDao.getList(filter);

    final PropertyMapping mapping = new PropertyMapping();
    for (final KontoDO konto : list) {
      mapping.add(AccountsCol.NUMBER, konto.getNummer());
      mapping.add(AccountsCol.NAME, konto.getBezeichnung());
      mapping.add(AccountsCol.DATE_OF_LAST_MODIFICATION, konto.getLastUpdate());
      mapping.add(AccountsCol.DATE_OF_CREATION, konto.getCreated());
      String status = "";
      if (konto.isDeleted() == true) {
        status = PFUserContext.getLocalizedString("deleted");
      } else if (konto.getStatus() != null) {
        status = PFUserContext.getLocalizedString(konto.getStatus().getI18nKey());
      }
      mapping.add(AccountsCol.STATUS, status);
      mapping.add(AccountsCol.DESCRIPTION, konto.getDescription());
      sheet.addRow(mapping.getMapping(), 0);
    }
  }
}
