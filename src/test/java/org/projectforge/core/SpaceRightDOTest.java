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

package org.projectforge.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;


public class SpaceRightDOTest
{
  @Test
  public void keyValues()
  {
    final SpaceRightDO right = new SpaceRightDO();
    assertNull("No rights does exist.", right.getRight(null));
    assertNull("No rights does exist.", right.getRight(""));
    assertNull("No rights does exist.", right.getRight("test"));
    right.setValue("read");
    assertNull("No rights does exist.", right.getRight("test"));
    right.setValue("="); // Error
    assertNull("No rights does exist.", right.getRight("test"));
    right.setValue("read="); // Error
    assertNull("Right read is null.", right.getRight("read"));
    right.setValue("read=true");
    assertEquals("Right read is true.", "true", right.getRight("read"));
    right.setValue("read=true,write=false,notification=");
    assertEquals("Right read is true.", "true", right.getRight("read"));
    assertEquals("Right write is false.", "false", right.getRight("write"));
    assertNull("Right notification is null.", right.getRight("notification"));
    right.setValue("read=true,write=false,notification,delete=true");
    assertEquals("Right read is true.", "true", right.getRight("read"));
    assertEquals("Right write is false.", "false", right.getRight("write"));
    assertNull("Right notification is null.", right.getRight("notification"));
    assertEquals("Right delete is true.", "true", right.getRight("delete"));
  }
}
