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

public interface ContentProvider
{
  public void updateSheetStyle(ExportSheet sheet);

  public void updateRowStyle(ExportRow row);

  public void updateCellStyle(ExportCell cell);

  public void setValue(ExportCell cell, Object value);

  public void setValue(ExportCell cell, Object value, String property);

  /**
   * @param obj property name or class of the matching cells.
   * @param format The cell format to use for all matching cells.
   */
  public void putFormat(Object obj, CellFormat cellFormat);

  public void putFormat(Enum< ? > col, CellFormat cellFormat);

  /**
   * Equivalent to: putFormat(obj, new CellFormat(format))
   * @see CellFormat#CellFormat(String)
   */
  public void putFormat(Object obj, String dataFrmat);

  public void putFormat(Enum< ? > col, String dataFormat);

  public void putColWidth(int colIdx, int charLength);

  public void setColWidths(int... charLengths);

  /**
   * Creates a new instance. This is usefull because every sheet of the workbook should have its own content provider (regarding col widths,
   * property formats etc.) if not set explicit.
   * @return
   */
  public ContentProvider newInstance();
}
