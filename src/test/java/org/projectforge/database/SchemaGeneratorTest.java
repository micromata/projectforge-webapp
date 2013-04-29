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

import junit.framework.Assert;

import org.junit.Test;
import org.projectforge.access.AccessEntryDO;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;

public class SchemaGeneratorTest
{
  @Test
  public void prepareOneToMany()
  {
    final SchemaGenerator generator = new SchemaGenerator(null);
    generator.add(AccessEntryDO.class, GroupTaskAccessDO.class);
    final Table accessEntryTable = generator.getTable(AccessEntryDO.class);
    final Table groupTaskAccessTable = generator.getTable(GroupTaskAccessDO.class);
    Assert.assertNull(accessEntryTable.getAttributeByName("group_task_access_fk"));
    generator.prepareOneToMany();
    final TableAttribute attr = accessEntryTable.getAttributeByName("group_task_access_fk");
    Assert.assertNotNull(attr);
    Assert.assertEquals(groupTaskAccessTable.getName(), attr.getForeignTable());
    Assert.assertEquals(groupTaskAccessTable.getPrimaryKey().getName(), attr.getForeignAttribute());
  }

  @Test
  public void prepareManyToMany()
  {
    final SchemaGenerator generator = new SchemaGenerator(null);
    generator.add(PFUserDO.class, GroupDO.class);
    Table groupUserTable = generator.getTable("t_group_user");
    Assert.assertNull(groupUserTable);
    generator.prepareManyToMany();
    groupUserTable = generator.getTable("t_group_user");
    Assert.assertNotNull(groupUserTable);
    TableAttribute attr = groupUserTable.getAttributeByName("user_id");
    assertTableAttribute(attr, "USER_ID", "T_PF_USER", "pk", TableAttributeType.INT, false);
    attr = groupUserTable.getAttributeByName("group_id");
    assertTableAttribute(attr, "GROUP_ID", "T_GROUP", "pk", TableAttributeType.INT, false);
  }

  private void assertTableAttribute(final TableAttribute attr, final String name, final String foreignTable, final String foreignAttribute, final TableAttributeType type, final boolean nullable)
  {
    Assert.assertNotNull(attr);
    Assert.assertEquals(name, attr.getName());
    Assert.assertEquals(foreignTable, attr.getForeignTable());
    Assert.assertEquals(foreignAttribute, attr.getForeignAttribute());
    Assert.assertEquals(type, attr.getType());
    Assert.assertEquals(nullable, attr.isNullable());
  }
}
