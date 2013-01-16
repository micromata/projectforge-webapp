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
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.task.TaskDO;
import org.projectforge.test.TestBase;

public class HibernateUtilsTest extends TestBase
{
  @Test
  public void propertyLengthTest()
  {
    Integer length = HibernateUtils.getPropertyLength("org.projectforge.task.TaskDO", "title");
    assertEquals(new Integer(40), length);
    length = HibernateUtils.getPropertyLength("org.projectforge.task.TaskDO", "shortDescription");
    assertEquals(new Integer(255), length);
    length = HibernateUtils.getPropertyLength(TaskDO.class, "shortDescription");
    assertEquals(new Integer(255), length);
    HibernateUtils.enterTestMode();
    length = HibernateUtils.getPropertyLength("org.projectforge.task.TaskDO", "unknown");
    HibernateUtils.exitTestMode();
    assertNull(length);
    length = HibernateUtils.getPropertyLength("unknown", "name");
    assertNull(length);
  }

  @BeforeClass
  public static void setUp() throws Exception
  {
    preInit();
    init(false);
  }
}
