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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.task.TaskDO;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.PFUserDO;

public class TableAttributeTest
{
  @Test
  public void createTableAttributes()
  {
    TableAttribute attr;
    attr = assertAttribute(TaskDO.class, "id", "pk", TableAttributeType.INT, true, false);
    attr = assertAttribute(TaskDO.class, "created", "created", TableAttributeType.TIMESTAMP, false, true);
    attr = assertAttribute(TaskDO.class, "lastUpdate", "last_update", TableAttributeType.TIMESTAMP, false, true);
    attr = assertAttribute(PFUserDO.class, "locale", "locale", TableAttributeType.LOCALE, false, true);
    attr = assertAttribute(TaskDO.class, "duration", "duration", TableAttributeType.DECIMAL, false, true);
    assertEquals(2, attr.getScale());
    assertEquals(10, attr.getPrecision());
    attr = assertAttribute(TaskDO.class, "title", "title", TableAttributeType.VARCHAR, false, false);
    assertEquals(TaskDO.TITLE_LENGTH, attr.getLength());
    attr = assertAttribute(TaskDO.class, "workpackageCode", "workpackage_code", TableAttributeType.VARCHAR, false, true);
    assertEquals(100, attr.getLength());
    attr = assertAttribute(TaskDO.class, "description", "description", TableAttributeType.VARCHAR, false, true);
    assertEquals(TaskDO.DESCRIPTION_LENGTH, attr.getLength());
    attr = assertAttribute(TaskDO.class, "maxHours", "max_hours", TableAttributeType.INT, false, true);
    attr = assertAttribute(TaskDO.class, "kost2IsBlackList", "kost2_is_black_list", TableAttributeType.BOOLEAN, false, false);
    attr = assertAttribute(TaskDO.class, "responsibleUser", "responsible_user_id", TableAttributeType.INT, false, true);
    assertEquals("T_PF_USER", attr.getForeignTable());
    assertEquals("pk", attr.getForeignAttribute());
    attr = assertAttribute(TaskDO.class, "priority", "priority", TableAttributeType.VARCHAR, false, true);
    assertEquals(TaskDO.PRIORITY_LENGTH, attr.getLength());

    attr = assertAttribute(TimesheetDO.class, "startTime", "start_time", TableAttributeType.TIMESTAMP, false, false);
    attr = assertAttribute(TimesheetDO.class, "task", "task_id", TableAttributeType.INT, false, true);
    assertEquals("T_TASK", attr.getForeignTable());
    assertEquals("pk", attr.getForeignAttribute());
    attr = assertAttribute(PFUserDO.class, "loginFailures", "loginFailures", TableAttributeType.INT, false, false);
    attr = assertAttribute(GroupTaskAccessDO.class, "recursive", "recursive", TableAttributeType.BOOLEAN, false, false);
    attr = assertAttribute(Kost1DO.class, "nummernkreis", "nummernkreis", TableAttributeType.INT, false, false);
    try {
      attr = assertAttribute(Kost2ArtDO.class, "workFraction", "work_fraction", TableAttributeType.DECIMAL, false, true);
      fail("UnsupportedException expected, because whether scale nor precision is given for this property.");
    } catch (final UnsupportedOperationException ex) {
      // Expected, because whether scale nor precision is given.
    }
  }

  private TableAttribute assertAttribute(final Class< ? > cls, final String property, final String name, final TableAttributeType type,
      final boolean primaryKey, final boolean nullable)
  {
    final TableAttribute attr = new TableAttribute(cls, property);
    assertEquals("Different column name expected.", name, attr.getName());
    assertEquals("Different nullable value expected.", nullable, attr.isNullable());
    if (primaryKey == true) {
      assertTrue(attr.isPrimaryKey());
      assertFalse("Primary key should be not nullable.", attr.isNullable());
    } else {
      assertFalse(attr.isPrimaryKey());
    }
    assertEquals("Different column type expected.", type, attr.getType());
    return attr;
  }
}
