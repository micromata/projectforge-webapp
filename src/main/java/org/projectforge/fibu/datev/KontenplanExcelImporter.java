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

package org.projectforge.fibu.datev;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.projectforge.common.ImportStorage;
import org.projectforge.common.ImportedElement;
import org.projectforge.common.ImportedSheet;
import org.projectforge.core.ActionLog;
import org.projectforge.fibu.KontoDO;
import org.projectforge.xls.ExcelImport;

public class KontenplanExcelImporter
{
  public static final String NAME_OF_EXCEL_SHEET = "Kontenplan";

  private static final Logger log = Logger.getLogger(KontenplanExcelImporter.class);

  /** In dieser Zeile stehen die Überschriften der Spalten für die Konten. */
  public static final int ROW_COLUMNNAMES = 2;
  
  public void doImport(final ImportStorage<KontoDO> storage, final InputStream is, final ActionLog actionLog) throws Exception
  {
    ExcelImport<KontenplanExcelRow> imp = new ExcelImport<KontenplanExcelRow>(is);
    for (short idx = 0; idx < imp.getWorkbook().getNumberOfSheets(); idx++) {
      imp.setActiveSheet(idx);
      String name = imp.getWorkbook().getSheetName(idx);
      if (NAME_OF_EXCEL_SHEET.equals(name)) {
        imp.setActiveSheet(idx);
        HSSFSheet sheet = imp.getWorkbook().getSheetAt(idx);
        importKontenplan(storage, imp, sheet, actionLog);
        return;
      }
    }
    log.error("Oups, no sheet named 'Kontenplan' found.");
  }

  private void importKontenplan(final ImportStorage<KontoDO> storage, final ExcelImport<KontenplanExcelRow> imp, final HSSFSheet sheet,
      final ActionLog actionLog) throws Exception
  {
    ImportedSheet<KontoDO> importedSheet = new ImportedSheet<KontoDO>();
    storage.addSheet(importedSheet);
    importedSheet.setName(NAME_OF_EXCEL_SHEET);
    imp.setNameRowIndex(ROW_COLUMNNAMES);
    imp.setStartingRowIndex(ROW_COLUMNNAMES + 1);
    imp.setRowClass(KontenplanExcelRow.class);

    Map<String, String> map = new HashMap<String, String>();
    map.put("Konto", "konto");
    map.put("Bezeichnung", "bezeichnung");
    imp.setColumnMapping(map);

    KontenplanExcelRow[] rows = new KontenplanExcelRow[0];
    rows = imp.convertToRows(KontenplanExcelRow.class);
    for (int i = 0; i < rows.length; i++) {
      actionLog.incrementCounterSuccess();
      KontoDO konto = convertKonto(rows[i]);
      ImportedElement<KontoDO> element = new ImportedElement<KontoDO>(storage.nextVal(), KontoDO.class,
          DatevImportDao.KONTO_DIFF_PROPERTIES);
      element.setValue(konto);
      importedSheet.addElement(element);
      log.debug(konto);
    }
  }

  private KontoDO convertKonto(KontenplanExcelRow row) throws Exception
  {
    KontoDO konto = new KontoDO();
    konto.setNummer(row.konto);
    konto.setBezeichnung(row.bezeichnung);
    return konto;
  }
}
