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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.projectforge.user.PFUserContext;

public class ExportWorkbook
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExportWorkbook.class);

  private Workbook poiWorkbook;

  private List<ExportSheet> sheets;

  private ContentProvider contentProvider;

  private int numberOfCellStyles = 0;

  private int numberOfDataFormats = 0;

  private Map<String, Short> dataFormats = new HashMap<String, Short>();

  private Locale locale;

  public ExportWorkbook()
  {
    sheets = new ArrayList<ExportSheet>();
    poiWorkbook = new HSSFWorkbook();
  }

  public ExportWorkbook(final File excelFile) throws FileNotFoundException, IOException
  {
    poiWorkbook = new HSSFWorkbook(new FileInputStream(excelFile), true);
    int no = poiWorkbook.getNumberOfSheets();
    sheets = new ArrayList<ExportSheet>(no);
    for (int i = 0; i < no; i++) {
      final Sheet sh = poiWorkbook.getSheetAt(i);
      final ExportSheet sheet = new ExportSheet(null, poiWorkbook.getSheetName(i), sh);
      sheets.add(sheet);
    }
  }

  public ExportWorkbook setLocale(Locale locale)
  {
    this.locale = locale;
    return this;
  }

  /**
   * If not set then the locale of the logged in user is taken.
   */
  public Locale getLocale()
  {
    if (locale != null) {
      return locale;
    }
    return PFUserContext.getLocale();
  }

  /**
   * Calls updateStyle for all containing sheets.
   * @see ExportSheet#updateStyles(ContentProvider)
   */
  public void updateStyles()
  {
    for (ExportSheet sheet : sheets) {
      sheet.updateStyles();
    }
  }

  /**
   * Calls updateStyles first.
   * @param out
   * @throws IOException
   * @see #updateStyles()
   */
  public void write(OutputStream out) throws IOException
  {
    updateStyles();
    poiWorkbook.write(out);
    if (log.isDebugEnabled() == true) {
      log.info("Excel sheet exported: number of cell styles="
          + this.numberOfCellStyles
          + ", number of data formats="
          + this.numberOfDataFormats);
    }
  }

  public byte[] getAsByteArray()
  {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      write(baos);
    } catch (IOException ex) {
      log.fatal("Exception encountered " + ex, ex);
      throw new RuntimeException(ex);
    }
    return baos.toByteArray();
  }

  public ExportSheet addSheet(String name)
  {
    return addSheet(name, null);
  }

  /**
   * 
   * @param name
   * @return
   */
  public ExportSheet addSheet(final String name, final ContentProvider contentProvider)
  {
    String title = name;
    if (name.length() >= ExportSheet.MAX_XLS_SHEETNAME_LENGTH) {
      title = StringUtils.abbreviate(name, ExportSheet.MAX_XLS_SHEETNAME_LENGTH);
    }
    Sheet poiSheet = poiWorkbook.createSheet(title);
    ContentProvider cp = getContentProvider();
    if (contentProvider != null) {
      cp = contentProvider;
    } else {
      cp = new XlsContentProvider(this);
    }
    ExportSheet sheet = new ExportSheet(cp, name, poiSheet);
    sheets.add(sheet);
    return sheet;
  }

  public int getNumberOfSheets()
  {
    return sheets.size();
  }

  public ExportSheet getSheet(final int index)
  {
    return sheets.get(index);
  }

  public ExportSheet getSheet(String name)
  {
    for (ExportSheet sheet : sheets) {
      if (StringUtils.equals(sheet.getName(), name) == true) {
        return sheet;
      }
    }
    return null;
  }

  public CellStyle createCellStyle()
  {
    ++numberOfCellStyles;
    return poiWorkbook.createCellStyle();
  }

  public Font createFont()
  {
    return poiWorkbook.createFont();
  }

  public CreationHelper getCreationHelper()
  {
    return poiWorkbook.getCreationHelper();
  }

  public void setContentProvider(ContentProvider contentProvider)
  {
    this.contentProvider = contentProvider;
  }

  public ContentProvider getContentProvider()
  {
    return contentProvider;
  }

  public short getDataFormat(final String format)
  {
    if (dataFormats.containsKey(format) == true) {
      return dataFormats.get(format);
    }
    short value = getCreationHelper().createDataFormat().getFormat(format);
    dataFormats.put(format, value);
    ++this.numberOfDataFormats;
    return value;
  }
}
