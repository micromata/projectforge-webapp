/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.core;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserPrefDO;

public class HibernateSearchDependentObjectsReindexerTest
{
  @Test
  public void register()
  {
    final HibernateSearchDependentObjectsReindexer reindexer = new HibernateSearchDependentObjectsReindexer();
    reindexer.register(TaskDO.class);
    List<HibernateSearchDependentObjectsReindexer.Entry> list = reindexer.map.get(PFUserDO.class);
    assertEquals(1, reindexer.map.size());
    assertEquals(1, list.size());
    assertEntry(list.get(0), TaskDO.class, "responsibleUser");
    reindexer.register(GroupDO.class);
    list = reindexer.map.get(PFUserDO.class);
    assertEquals(1, reindexer.map.size());
    assertEquals(1, list.size());
    reindexer.register(UserPrefDO.class);
    assertEquals(1, reindexer.map.size());
    assertEquals(2, list.size());
    assertEntry(list.get(1), UserPrefDO.class, "user");
  }

  private void assertEntry(final HibernateSearchDependentObjectsReindexer.Entry entry, final Class< ? > clazz, final String fieldName)
  {
    assertEquals(fieldName, entry.fieldName);
    assertEquals(clazz, entry.clazz);
  }
}
