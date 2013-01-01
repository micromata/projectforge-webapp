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

package org.projectforge.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.projectforge.core.ConfigXml;


public class ExportSheet
{
  /** Sheet names are limited to this length */
  public final static int MAX_XLS_SHEETNAME_LENGTH = 31;

  /** Constant for an empty cell */
  public static final String EMPTY = "LEAVE_CELL_EMPTY";

  private Sheet poiSheet;

  private List<ExportRow> rows;

  private String name;

  private String[] propertyNames;

  private int rowCounter = 0;

  private ContentProvider contentProvider;

  public ExportSheet(final ContentProvider contentProvider, final String name, final Sheet poiSheet)
  {
    this.contentProvider = contentProvider;
    this.name = name;
    this.poiSheet = poiSheet;
    this.rows = new ArrayList<ExportRow>();
    final int lastRowNum = poiSheet.getLastRowNum();
    if (lastRowNum > 0) {
      // poiSheet does already exists.
      for (int i = poiSheet.getFirstRowNum(); i < poiSheet.getLastRowNum(); i++) {
        final Row poiRow = poiSheet.getRow(i);
        final ExportRow row = new ExportRow(contentProvider, this, poiRow, i);
        rows.add(row);
      }
    }
    final PrintSetup printSetup = getPrintSetup();
    printSetup.setPaperSize(ConfigXml.getInstance().getDefaultPaperSize());
  }

  /**
   * Convenient method: Adds all column names, titles, width and adds a head row.
   * @param columns
   */
  public void setColumns(ExportColumn... columns)
  {
    if (columns == null) {
      return;
    }
    // build all column names, title, widths from fixed and variable columns
    final int numCols = columns.length;
    final String[] colNames = new String[numCols];
    final ExportRow headRow = addRow();
    int idx = 0;
    for (final ExportColumn col : columns) {
      headRow.addCell(idx, col.title);
      colNames[idx] = col.name;
      contentProvider.putColWidth(idx, col.width);
      ++idx;
    }
    setPropertyNames(colNames);
  }

  public PrintSetup getPrintSetup()
  {
    return poiSheet.getPrintSetup();
  }

  public ExportRow addRow()
  {
    final Row poiRow = poiSheet.createRow(rowCounter);
    final ExportRow row = new ExportRow(contentProvider, this, poiRow, rowCounter++);
    this.rows.add(row);
    return row;
  }

  public ExportRow addRow(final Object... values)
  {
    final ExportRow row = addRow();
    row.setValues(values);
    return row;
  }

  public ExportRow addRow(final Object rowBean)
  {
    return addRow(rowBean, 0);
  }

  public ExportRow addRow(final Object rowBean, final int startCol)
  {
    final ExportRow row = addRow();
    row.fillBean(rowBean, propertyNames, 0);
    return row;
  }

  public void addRows(final Object[] rowBeans)
  {
    addRows(rowBeans, 0);
  }

  public void addRows(final Object[] rowBeans, final int startCol)
  {
    for (final Object rowBean : rowBeans) {
      addRow(rowBean, startCol);
    }
  }

  public void addRows(final Collection< ? > rowBeans)
  {
    addRows(rowBeans, 0);
  }

  public void addRows(final Collection< ? > rowBeans, final int startCol)
  {
    for (final Object rowBean : rowBeans) {
      addRow(rowBean, startCol);
    }
  }

  public String getName()
  {
    return name;
  }

  public ExportRow getRow(int row)
  {
    return this.rows.get(row);
  }

  public List<ExportRow> getRows()
  {
    return rows;
  }

  /**
   * For filling the table via beans.
   * @param propertyNames
   */
  public void setPropertyNames(final String[] propertyNames)
  {
    this.propertyNames = propertyNames;
  }

  /**
   * @see ExportRow#updateStyles(StyleProvider)
   */
  public void updateStyles()
  {
    if (contentProvider != null) {
      contentProvider.updateSheetStyle(this);
      for (final ExportRow row : rows) {
        row.updateStyles(contentProvider);
      }
    }
  }

  public ContentProvider getContentProvider()
  {
    return contentProvider;
  }

  public void setContentProvider(final ContentProvider contentProvider)
  {
    this.contentProvider = contentProvider;
  }

  public void setColumnWidth(final int col, final int width)
  {
    poiSheet.setColumnWidth(col, width);
  }

  /**
   * Freezes the firt toRow lines and the first toCol columns.
   * @param toRow
   * @param toCol
   * @see Sheet#createFreezePane(int, int)
   */
  public void createFreezePane(final int toCol, final int toRow)
  {
    poiSheet.createFreezePane(toCol, toRow);
  }

  /**
   * @param x
   * @param y
   * @see Sheet#setZoom(int, int)
   */
  public void setZoom(final int x, final int y)
  {
    poiSheet.setZoom(x, y);
  }

  /**
   * Merges cells and sets the value.
   * @param colFrom
   * @param colTo
   * @param value
   */
  public ExportCell setMergedRegion(final int firstRow, final int lastRow, final int firstCol, final int lastCol, final Object value)
  {
    final CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
    poiSheet.addMergedRegion(region);
    final ExportRow row = getRow(firstRow);
    final ExportCell cell = row.addCell(firstCol, value);
    return cell;
  }

  public Sheet getPoiSheet()
  {
    return poiSheet;
  }
}
