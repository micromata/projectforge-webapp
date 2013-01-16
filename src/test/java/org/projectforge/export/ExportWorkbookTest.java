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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.core.ConfigXmlTest;
import org.projectforge.test.TestConfiguration;

public class ExportWorkbookTest
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExportWorkbookTest.class);

  @BeforeClass
  public static void setUp()
  {
    // Needed if this tests runs before the ConfigurationTest.
    ConfigXmlTest.createTestConfiguration();
    TestConfiguration.initAsTestConfiguration();
  }

  @Test
  public void exportGermanExcel() throws IOException
  {
    writeExcel("TestExcel_de.xls", Locale.GERMAN);
  }

  @Test
  public void exportExcel() throws IOException
  {
    writeExcel("TestExcel_en.xls", Locale.ENGLISH);
  }

  private void writeExcel(final String filename, final Locale locale) throws IOException
  {
    final ExportWorkbook workbook = new ExportWorkbook().setLocale(locale);
    final ExportSheet sheet = workbook.addSheet("Test");
    sheet.getContentProvider().setColWidths(20, 20, 20);
    sheet.addRow().setValues("Type", "Precision", "result");
    sheet.addRow().setValues("Java output", ".", "Tue Sep 28 00:27:10 UTC 2010");
    sheet.addRow().setValues("DateHolder", "DAY", getDateHolder().setPrecision(DatePrecision.DAY));
    sheet.addRow().setValues("DateHolder", "HOUR_OF_DAY", getDateHolder().setPrecision(DatePrecision.HOUR_OF_DAY));
    sheet.addRow().setValues("DateHolder", "MINUTE_15", getDateHolder().setPrecision(DatePrecision.MINUTE_15));
    sheet.addRow().setValues("DateHolder", "MINUTE", getDateHolder().setPrecision(DatePrecision.MINUTE));
    sheet.addRow().setValues("DateHolder", "SECOND", getDateHolder().setPrecision(DatePrecision.SECOND));
    sheet.addRow().setValues("DateHolder", "MILLISECOND", getDateHolder().setPrecision(DatePrecision.MILLISECOND));
    sheet.addRow().setValues("DateHolder", "-", getDateHolder());
    sheet.addRow().setValues("DayHolder", "-", new DayHolder(getDate()));
    sheet.addRow().setValues("java.util.Date", "-", getDate());
    sheet.addRow().setValues("java.sql.Timestamp", "-", new Timestamp(getDate().getTime()));
    sheet.addRow().setValues("int", "-", 1234);
    sheet.addRow().setValues("BigDecimal", "-", new BigDecimal("123123123.123123123123"));
    final File file = TestConfiguration.getWorkFile(filename);
    log.info("Writing Excel test sheet to work directory: " + file.getAbsolutePath());
    workbook.write(new FileOutputStream(file));
  }

  private DateHolder getDateHolder()
  {
    return new DateHolder(getDate(), DateHelper.UTC);
  }

  private Date getDate()
  {
    return new Date(1285633630868L);
  }
}
