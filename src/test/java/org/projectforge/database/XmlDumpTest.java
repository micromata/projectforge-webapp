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

import org.apache.log4j.Logger;
import org.junit.Test;
import org.projectforge.test.TestBase;


public class XmlDumpTest extends TestBase
{
  private static final Logger log = Logger.getLogger(XmlDumpTest.class);

  @Test
  public void restore()
  {
    log.warn("Do nothing in Test XmlDumpTest");
    /*try {
      XmlDump xmlDump = (XmlDump) getConfiguration().getAndAutowireBean("xmlDump", XmlDump.class);
      clearDatabase();
      xmlDump.restoreDatabaseFromClasspathResource("/database-dump.xml.gz", "utf-8");
      UserDao userDao = (UserDao) getConfiguration().getAndAutowireBean("userDao", UserDao.class);
      List<PFUserDO> list = userDao.internalLoadAll();
      for (PFUserDO user : list) {
        log.info(user);
      }
      xmlDump.dumpDatabase();
    } catch (Throwable ex) {
      ex.printStackTrace();
      fail(ex.getMessage());
    }*/
  }
}
