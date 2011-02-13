/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.database;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.database.xstream.XStreamSavingConverter;
import org.projectforge.test.TestBase;

public class XmlDumpTest extends TestBase
{
  private InitDatabaseDao initDatabaseDao;

  private XmlDump xmlDump;

  public void setInitDatabaseDao(InitDatabaseDao initDatabaseDao)
  {
    this.initDatabaseDao = initDatabaseDao;
  }

  public void setXmlDump(XmlDump xmlDump)
  {
    this.xmlDump = xmlDump;
  }

  @BeforeClass
  public static void setUp() throws Exception
  {
    init(false);
  }

  @Test
  public void verifyDump()
  {
    assertTrue(initDatabaseDao.isEmpty());
    final XStreamSavingConverter converter = xmlDump
        .restoreDatabaseFromClasspathResource(InitDatabaseDao.TEST_DATA_BASE_DUMP_FILE, "utf-8");
    final int counter = xmlDump.verifyDump(converter);
    assertTrue("Minimum expected number of tested object to low: " + counter + " < 50.", counter >= 50);
  }
}
