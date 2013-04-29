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
import junit.framework.Assert;

import org.junit.Test;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.task.TaskDO;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightDO;

public class TableTest
{
  @Test
  public void createTables()
  {
    assertEquals("T_PF_USER", new Table(PFUserDO.class).getName());
    assertEquals("T_TASK", new Table(TaskDO.class).getName());
    assertEquals("T_TIMESHEET", new Table(TimesheetDO.class).getName());

    final Table table = new Table(TaskDO.class);
    table.addAttributes("title", "priority", "maxHours", "startDate", "responsibleUser");
    assertAttribute(table.getAttributes().get(0), "title");
    assertAttribute(table.getAttributes().get(1), "priority");
    assertAttribute(table.getAttributes().get(2), "max_hours");
    assertAttribute(table.getAttributes().get(3), "start_date");
    assertAttribute(table.getAttributes().get(4), "responsible_user_id");

    assertAttribute(table.getAttributeByProperty("responsibleUser"), "responsible_user_id");
  }

  @Test
  public void autoAddAttributes()
  {
    Table table = new Table(ConfigurationDO.class);
    table.autoAddAttributes();
    assertAttribute(table, "stringValue");
    assertAttribute(table, "created");

    table = new Table(UserRightDO.class);
    table.autoAddAttributes();
    assertAttribute(table, "right_id");
  }

  private void assertAttribute(final Table table, final String name)
  {
    for (final TableAttribute attr : table.getAttributes()) {
      if (attr.getName().equals(name) == true) {
        return;
      }
    }
    Assert.fail("Attribute '" + name + "' not found.");
  }

  private void assertAttribute(final TableAttribute attribute, final String name)
  {
    assertEquals(name, attribute.getName());
  }
}
