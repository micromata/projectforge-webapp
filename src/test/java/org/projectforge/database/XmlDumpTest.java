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

package org.projectforge.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.database.xstream.XStreamSavingConverter;
import org.projectforge.test.TestBase;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

public class XmlDumpTest extends TestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XmlDumpTest.class);

  private InitDatabaseDao initDatabaseDao;

  private UserGroupCache userGroupCache;

  private XmlDump xmlDump;

  public void setInitDatabaseDao(final InitDatabaseDao initDatabaseDao)
  {
    this.initDatabaseDao = initDatabaseDao;
  }

  public void setUserGroupCache(final UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  public void setXmlDump(final XmlDump xmlDump)
  {
    this.xmlDump = xmlDump;
  }

  @BeforeClass
  public static void setUp() throws Exception
  {
    preInit();
    init(false);
  }

  @Test
  public void verifyDump()
  {
    userGroupCache.setExpired(); // Force reload (because it's may be expired due to previous tests).
    assertTrue(initDatabaseDao.isEmpty());
    final XStreamSavingConverter converter = xmlDump
        .restoreDatabaseFromClasspathResource(InitDatabaseDao.TEST_DATA_BASE_DUMP_FILE, "utf-8");
    final int counter = xmlDump.verifyDump(converter);
    assertTrue("Import was not successful.", counter > 0);
    assertTrue("Minimum expected number of tested object to low: " + counter + " < 50.", counter >= 50);
    final PFUserDO user = userDao.internalLoadAll().get(0);
    user.setUsername("changed");
    userDao.internalUpdate(user);
    log.info("The following import error from XmlDump are OK.");
    assertEquals("Error should be detected.", -counter, xmlDump.verifyDump(converter));
    log.info("The previous import error from XmlDump are OK.");
  }
}
