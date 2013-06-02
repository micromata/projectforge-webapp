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

package org.projectforge.fibu;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.excel.ContentProvider;
import org.projectforge.excel.ExportSheet;
import org.projectforge.excel.ExportWorkbook;
import org.projectforge.export.MyXlsContentProvider;
import org.projectforge.user.PFUserContext;

/**
 * For excel export.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class EmployeeExport
{
  private class MyContentProvider extends MyXlsContentProvider
  {
    public MyContentProvider(final ExportWorkbook workbook)
    {
      super(workbook);
    }

    @Override
    public org.projectforge.excel.ContentProvider newInstance()
    {
      return new MyContentProvider(this.workbook);
    }
  };

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmployeeExport.class);

  /**
   * Exports the filtered list as table with almost all fields.
   */
  public byte[] export(final List<EmployeeDO> list, final Object... params)
  {
    if (CollectionUtils.isEmpty(list) == true) {
      return null;
    }
    log.info("Exporting employee list.");
    final ExportWorkbook xls = new ExportWorkbook();
    final ContentProvider contentProvider = new MyContentProvider(xls);
    // create a default Date format and currency column
    xls.setContentProvider(contentProvider);

    final String sheetTitle = PFUserContext.getLocalizedString("fibu.employee.title.heading");
    final ExportSheet sheet = xls.addSheet(sheetTitle);
    final ContentProvider sheetProvider = sheet.getContentProvider();
    sheet.createFreezePane(1, 1);
    // sheet.setColumns(columns);
    // for (final EmployeeDO order : list) {
    // final PropertyMapping mapping = new PropertyMapping();
    // addOrderMapping(mapping, order, params);
    // sheet.addRow(mapping.getMapping(), 0);
    // }
    // sheet.getPoiSheet().setAutoFilter(
    // org.apache.poi.ss.util.CellRangeAddress.valueOf("A1:" + (Character.toString((char) (65 + OrderCol.values().length - 1))) + "1"));
    return xls.getAsByteArray();
  }
}
