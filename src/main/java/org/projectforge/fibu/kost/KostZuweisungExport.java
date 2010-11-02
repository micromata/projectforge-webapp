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

package org.projectforge.fibu.kost;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.projectforge.common.CurrencyHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
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
import org.projectforge.fibu.ProjektFormatter;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungsPositionDO;


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
    public MyContentProvider(ExportWorkbook workbook)
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

  private enum Col
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

    Col(String theTitle, int width)
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
      final String sheetTitle)
  {
    final List<KostZuweisungDO> zuweisungen = new ArrayList<KostZuweisungDO>();
    for (final AbstractRechnungDO< ? > rechnung : list) {
      if (rechnung.getPositionen() != null) {
        for (final AbstractRechnungsPositionDO position : rechnung.getPositionen()) {
          if (position.getKostZuweisungen() != null) {
            zuweisungen.addAll(position.getKostZuweisungen());
          }
        }
      }
    }
    return export(zuweisungen, sheetTitle);
  }

  /**
   * Exports the filtered list as table.
   */
  public byte[] export(final List<KostZuweisungDO> list, final String sheetTitle)
  {
    log.info("Exporting kost zuweisung list.");
    final ExportWorkbook xls = new ExportWorkbook();
    final ContentProvider contentProvider = new MyContentProvider(xls);
    // create a default Date format and currency column
    xls.setContentProvider(contentProvider);

    final ExportSheet sheet = xls.addSheet(sheetTitle);
    sheet.createFreezePane(0, 1);

    final ExportColumn[] cols = new ExportColumn[Col.values().length];
    int i = 0;
    for (final Col col : Col.values()) {
      cols[i++] = new I18nExportColumn(col, col.theTitle, col.width);
    }

    // column property names
    sheet.setColumns(cols);

    final ContentProvider sheetProvider = sheet.getContentProvider();
    sheetProvider.putFormat(Col.BRUTTO, "#,##0.00;[Red]-#,##0.00");
    sheetProvider.putFormat(Col.KORREKTUR, "#,##0.00;[Red]-#,##0.00");
    sheetProvider.putFormat(Col.KOST1, "#");
    sheetProvider.putFormat(Col.KOST2, "#");
    sheetProvider.putFormat(Col.DATE, "dd.MM.yyyy");

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
      mapping.add(Col.BRUTTO, zuweisung.getBrutto());
      mapping.add(Col.KONTO, "");
      mapping.add(Col.REFERENZ, StringHelper.removeNonDigitsAndNonASCIILetters(referenz));
      mapping.add(Col.DATE, rechnung.getDatum());
      mapping.add(Col.GEGENKONTO, "");
      mapping.add(Col.KOST1, zuweisung.getKost1().getNummer());
      mapping.add(Col.KOST2, zuweisung.getKost2().getNummer());
      mapping.add(Col.TEXT, text);
      mapping.add(Col.KORREKTUR, korrektur);
      sheet.addRow(mapping.getMapping(), 0);
    }
    return xls.getAsByteArray();
  }
}
