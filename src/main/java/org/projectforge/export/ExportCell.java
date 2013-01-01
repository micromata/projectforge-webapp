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

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;

public class ExportCell
{
  private Cell poiCell;

  private int row;

  private int col;

  private CellFormat cellFormat;

  private ContentProvider styleProvider;

  public ExportCell(ContentProvider styleProvider, Cell poiCell, int row, int col)
  {
    this.styleProvider = styleProvider;
    this.poiCell = poiCell;
    this.row = row;
    this.col = col;
  }

  public void setValue(Object value)
  {
    setValue(value, null);
  }

  public void setValue(Object value, String property)
  {
    styleProvider.setValue(this, value, property);
  }

  public double getNumericCellValue()
  {
    if (poiCell == null) {
      return 0.0;
    }
    return poiCell.getNumericCellValue();
  }

  public boolean getBooleanCellValue()
  {
    if (poiCell == null) {
      return false;
    }
    return poiCell.getBooleanCellValue();
  }

  public String getStringCellValue()
  {
    Object obj = getCellValue();
    if (obj == null) {
      return "";
    } else if (obj instanceof String) {
      return (String) obj;
    }
    return String.valueOf(obj);
  }

  public Object getCellValue()
  {
    if (poiCell == null) {
      return null;
    }
    switch (poiCell.getCellType()) {
      case Cell.CELL_TYPE_STRING:
        return poiCell.getRichStringCellValue().getString();
      case Cell.CELL_TYPE_NUMERIC:
        if (DateUtil.isCellDateFormatted(poiCell)) {
          return poiCell.getDateCellValue();
        }
        return poiCell.getNumericCellValue();
      case Cell.CELL_TYPE_BOOLEAN:
        return poiCell.getBooleanCellValue();
      case Cell.CELL_TYPE_FORMULA:
        return poiCell.getCellFormula();
      default:
        return null;
    }
  }

  public boolean isNumericCellType()
  {
    if (poiCell == null) {
      return false;
    }
    return poiCell.getCellType() == Cell.CELL_TYPE_NUMERIC;
  }

  public Date getDateCellValue()
  {
    if (poiCell == null) {
      return null;
    }
    return poiCell.getDateCellValue();
  }

  public Cell getPoiCell()
  {
    return poiCell;
  }

  public void setStyleProvider(ContentProvider styleProvider)
  {
    this.styleProvider = styleProvider;
  }

  public int getRow()
  {
    return row;
  }

  public int getCol()
  {
    return col;
  }

  public void setCellFormat(CellFormat cellFormat)
  {
    this.cellFormat = cellFormat;
  }

  public CellFormat ensureAndGetCellFormat()
  {
    if (cellFormat == null) {
      cellFormat = new CellFormat();
    }
    return cellFormat;
  }

  public CellFormat getCellFormat()
  {
    return cellFormat;
  }

  /**
   * Should only be called directly before the export. Please note: Excel does support only a limited number of different cell styles, so
   * re-use cell styles with same format.
   */
  public void setCellStyle(CellStyle cellStyle)
  {
    this.poiCell.setCellStyle(cellStyle);
  }
}
