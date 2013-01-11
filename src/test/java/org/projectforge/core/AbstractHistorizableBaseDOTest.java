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

package org.projectforge.core;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.task.TaskDO;

public class AbstractHistorizableBaseDOTest
{
  @Test
  public void testNonHistorizableProperties()
  {
    final TaskDO task = new TaskDO();
    Set<String> set = task.getNonHistorizableAttributes();
    Assert.assertEquals(2, set.size());
    Assert.assertTrue(set.contains("lastUpdate"));
    Assert.assertTrue(set.contains("created"));
    Assert.assertTrue(task.isNonHistorizableAttribute("lastUpdate"));
    Assert.assertTrue(task.isNonHistorizableAttribute("created"));
    Assert.assertFalse(task.isNonHistorizableAttribute("title"));

    final AuftragDO order = new AuftragDO();
    set = order.getNonHistorizableAttributes();
    Assert.assertEquals(4, set.size());
    Assert.assertTrue(order.isNonHistorizableAttribute("uiStatus"));
    Assert.assertTrue(order.isNonHistorizableAttribute("uiStatusAsXml"));
    Assert.assertFalse(order.isNonHistorizableAttribute("subject"));
  }
}
