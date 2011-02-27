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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.projectforge.task.TaskDO;

public class TableAttributeTest
{
  @Test
  public void createTableScript()
  {
    TableAttribute attr = new TableAttribute(TaskDO.class, "id");
    assertEquals("column name 'pk' expected.", "pk", attr.getName());
    assertTrue("primary key", attr.isPrimaryKey());
    assertFalse("primary key should be not nullable.", attr.isNullable());
    assertEquals(TableAttributeType.INT, attr.getType());

    attr = new TableAttribute(TaskDO.class, "created");
    assertEquals("created", attr.getName());
    assertFalse(attr.isPrimaryKey());
    assertTrue(attr.isNullable());
    assertEquals(TableAttributeType.TIMESTAMP, attr.getType());
  }
}
