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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.xls.Formula;

public class XlsContentProvider implements ContentProvider
{
  static final int LENGHT_UNIT = 256;

  public static final int LENGTH_CURRENCY = 11;

  public static final int LENGTH_DATE = 10;

  public static final int LENGTH_DATETIME = 15;

  public static final int LENGTH_DURATION = 10;

  public static final int LENGTH_EMAIL = 30;

  public static final int LENGTH_EXTRA_LONG = 80;

  public static final int LENGTH_KOSTENTRAEGER = 11;

  public static final int LENGTH_ID = 8;

  public static final int LENGTH_PHONENUMBER = 20;

  public static final int LENGTH_STD = 30;

  public static final int LENGTH_TIMESTAMP = 16;

  public static final int LENGTH_USER = 20;

  public static final int LENGTH_ZIPCODE = 7;

  public static Font FONT_HEADER;

  public static short FONT_HEADER_SIZE = 10;

  public static Font FONT_NORMAL;

  public static Font FONT_NORMAL_BOLD;

  public static Font FONT_WHITE_BOLD;

  static protected Font FONT_RED;

  static protected Font FONT_RED_BOLD;

  protected Map<CellFormat, CellStyle> reusableCellFormats = new HashMap<CellFormat, CellStyle>();

  protected ExportWorkbook workbook;

  private final Map<Object, CellFormat> formatMap = new HashMap<Object, CellFormat>();

  private final Map<Object, CellFormat> defaultFormatMap = new HashMap<Object, CellFormat>();

  private final Map<Integer, Integer> colWidthMap = new HashMap<Integer, Integer>();

  private boolean autoFormatCells = true;

  public XlsContentProvider(final ExportWorkbook workbook)
  {
    this.workbook = workbook;
    createFonts();
    defaultFormatMap.put(Date.class, new CellFormat(DateFormats.getExcelFormatString(DateFormatType.TIMESTAMP_MINUTES)));
    defaultFormatMap.put(java.sql.Date.class, new CellFormat(DateFormats.getExcelFormatString(DateFormatType.DATE)));
    defaultFormatMap.put(Integer.class, new CellFormat("#,##0", CellStyle.ALIGN_RIGHT));
    defaultFormatMap.put(Number.class, new CellFormat("#,###.######", CellStyle.ALIGN_RIGHT));
    defaultFormatMap.put(DateHolder.class, new CellFormat("YYYY-MM-DD").setAutoDatePrecision(true)); // format unused.
    defaultFormatMap.put(DayHolder.class, new CellFormat(DateFormats.getExcelFormatString(DateFormatType.DATE)));
    defaultFormatMap.put(java.sql.Timestamp.class, new CellFormat(DateFormats.getExcelFormatString(DateFormatType.TIMESTAMP_MILLIS)));
  }

  public void updateSheetStyle(final ExportSheet sheet)
  {
    for (final Map.Entry<Integer, Integer> entry : colWidthMap.entrySet()) {
      sheet.setColumnWidth(entry.getKey(), entry.getValue());
    }
  }

  /**
   * If true then first row and even/odd rows will be formatted with bordered cells.
   * @param autoFormatCells
   */
  public void setAutoFormatCells(final boolean autoFormatCells)
  {
    this.autoFormatCells = autoFormatCells;
  }

  /**
   * Highlights even and odd rows and sets first column bold if even and odd rows are configured.
   * @see org.projectforge.export.ContentProvider#updateRowStyle(org.projectforge.export.ExportRow)
   */
  public void updateRowStyle(final ExportRow row)
  {
    if (autoFormatCells == true) {
      for (final ExportCell cell : row.getCells()) {
        final CellFormat format = cell.ensureAndGetCellFormat();
        format.setFillForegroundColor(HSSFColor.WHITE.index);
        switch (row.getRowNum()) {
          /*
           * case 0: font = FONT_HEADER; break;
           */
          case 0:
            format.setFont(FONT_NORMAL_BOLD);
            // alignment = CellStyle.ALIGN_CENTER;
            break;
          default:
            format.setFont(FONT_NORMAL);
            if (row.getRowNum() % 2 == 0) {
              format.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            }
            break;
        }
      }
    }
  }

  public void updateCellStyle(final ExportCell cell)
  {
    final CellFormat format = cell.ensureAndGetCellFormat();
    CellStyle cellStyle = reusableCellFormats.get(format);
    if (cellStyle == null) {
      cellStyle = workbook.createCellStyle();
      reusableCellFormats.put(format, cellStyle);
      format.copyToCellStyle(cellStyle);
      if (format.getFillForegroundColor() != null) {
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
      }
      cellStyle.setBorderBottom((short) 1);
      cellStyle.setBorderLeft((short) 1);
      cellStyle.setBorderRight((short) 1);
      cellStyle.setBorderTop((short) 1);
      cellStyle.setWrapText(true);
      final String dataFormat = format.getDataFormat();
      if (dataFormat != null) {
        final short df = workbook.getDataFormat(format.getDataFormat());
        cellStyle.setDataFormat(df);
      }
    }
    cell.setCellStyle(cellStyle);
  }

  public void setValue(final ExportCell cell, final Object value)
  {
    setValue(cell, value, null);
  }

  /**
   * 
   */
  public void setValue(final ExportCell cell, final Object value, final String property)
  {
    final Cell poiCell = cell.getPoiCell();
    if (value instanceof Date) { // Attention: Time zone is not given!
      poiCell.setCellValue((Date) value);
    } else if (value instanceof DateHolder) {
      poiCell.setCellValue(((DateHolder) value).getCalendar());
    } else if (value instanceof Calendar) {
      poiCell.setCellValue((Calendar) value);
    } else if (value instanceof Boolean) {
      poiCell.setCellValue(((Boolean) value).booleanValue());
    } else if (value instanceof Number) {
      poiCell.setCellValue(((Number) value).doubleValue());
    } else if (value instanceof Formula) {
      poiCell.setCellFormula(((Formula) value).getExpr());
    } else {
      poiCell.setCellValue(ConvertUtils.convert(value));
    }
    CellFormat cellFormat = getCellFormat(cell, value, property, formatMap);
    if (cellFormat == null) {
      cellFormat = getCellFormat(cell, value, property, defaultFormatMap);
    }
    if (cellFormat == null) {
      cellFormat = new CellFormat();
      cellFormat.setAlignment(CellStyle.ALIGN_LEFT);
      cellFormat.setDataFormat("@");
      cellFormat.setWrapText(true);
    }
    cell.setCellFormat(cellFormat);
  }

  /**
   * @param cell
   * @param value
   * @param property
   * @param map
   * @return A clone of a pre-defined cell format if found, otherwise null.
   */
  private CellFormat getCellFormat(final ExportCell cell, final Object value, final String property, final Map<Object, CellFormat> map)
  {
    CellFormat format = null;
    if (property != null) {
      format = map.get(property);
    }
    if (format == null) {
      Class< ? > clazz = value == null ? null : value.getClass();
      while (format == null && clazz != null) {
        format = map.get(clazz);
        clazz = clazz.getSuperclass();
      }
    }
    if (format == null) {
      return null;
    }
    if (value != null
        && DateHolder.class.isAssignableFrom(value.getClass()) == true
        && BooleanUtils.isTrue(format.getAutoDatePrecision()) == true) {
      // Find a format dependent on the precision:
      final DatePrecision precision = ((DateHolder) value).getPrecision();
      if (precision == DatePrecision.DAY) {
        return new CellFormat(DateFormats.getExcelFormatString(DateFormatType.DATE));
      } else if (precision == DatePrecision.SECOND) {
        return new CellFormat(DateFormats.getExcelFormatString(DateFormatType.TIMESTAMP_SECONDS));
      } else if (precision == DatePrecision.MILLISECOND) {
        return new CellFormat(DateFormats.getExcelFormatString(DateFormatType.TIMESTAMP_MILLIS));
      } else {
        // HOUR_OF_DAY, MINUTE, MINUTE_15 or null
        return new CellFormat(DateFormats.getExcelFormatString(DateFormatType.TIMESTAMP_MINUTES));
      }
    }
    return format.clone();
  }

  public void putFormat(final Object obj, final CellFormat cellFormat)
  {
    formatMap.put(obj, cellFormat);
  }

  public void putFormat(final Enum< ? > col, final CellFormat cellFormat)
  {
    putFormat(col.name(), cellFormat);
  }

  public void putFormat(final Object obj, final String dataFormat)
  {
    formatMap.put(obj, new CellFormat(dataFormat));
  }

  public void putFormat(final Enum< ? > col, final String dataFormat)
  {
    putFormat(col.name(), dataFormat);
  }

  public void putColWidth(final int colIdx, final int charLength)
  {
    this.colWidthMap.put(colIdx, charLength * LENGHT_UNIT);
  }

  public void setColWidths(final int... charLengths)
  {
    for (int colIdx = 0; colIdx < charLengths.length; colIdx++) {
      putColWidth(colIdx, charLengths[colIdx]);
    }

  }

  public ContentProvider newInstance()
  {
    return new XlsContentProvider(this.workbook);
  }

  private void createFonts()
  {
    FONT_HEADER = workbook.createFont();
    FONT_HEADER.setFontHeightInPoints(FONT_HEADER_SIZE);
    FONT_HEADER.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

    FONT_NORMAL_BOLD = workbook.createFont();
    FONT_NORMAL_BOLD.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

    FONT_WHITE_BOLD = workbook.createFont();
    FONT_WHITE_BOLD.setColor(HSSFColor.WHITE.index);
    FONT_WHITE_BOLD.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

    FONT_RED = workbook.createFont();
    FONT_RED.setColor(HSSFColor.RED.index);

    FONT_RED_BOLD = workbook.createFont();
    FONT_RED_BOLD.setColor(HSSFColor.RED.index);
    FONT_RED_BOLD.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

    FONT_NORMAL = workbook.createFont();
  }
}
