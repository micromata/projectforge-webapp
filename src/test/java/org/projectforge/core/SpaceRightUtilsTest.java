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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class SpaceRightUtilsTest
{
  @Test
  public void getValues()
  {
    final SpaceRightDO right = new SpaceRightDO();
    right.setValue(null);
    Map<String, String> map = SpaceRightUtils.getValues(right);
    assertEquals(0, map.size());
    map = SpaceRightUtils.getValues(right.setValue(""));
    assertEquals(0, map.size());
    map = SpaceRightUtils.getValues(right.setValue("right"));
    assertEquals(0, map.size());
    map = SpaceRightUtils.getValues(right.setValue("right="));
    assertEquals(1, map.size());
    assertNull(map.get("right"));
    assertTrue(map.containsKey("right"));
    map = SpaceRightUtils.getValues(right.setValue("right=rw"));
    assertEquals(1, map.size());
    assertEquals("rw", map.get("right"));
    map = SpaceRightUtils.getValues(right.setValue("right=rw,=true"));
    assertEquals(1, map.size());
    assertEquals("rw", map.get("right"));
    map = SpaceRightUtils.getValues(right.setValue("right=rw,notification=true"));
    assertEquals(2, map.size());
    assertEquals("rw", map.get("right"));
    assertEquals("true", map.get("notification"));
    map = SpaceRightUtils.getValues(right.setValue("right=rw,notification=true,"));
    assertEquals(2, map.size());
    assertEquals("rw", map.get("right"));
    assertEquals("true", map.get("notification"));
    map = SpaceRightUtils.getValues(right.setValue("right = rw, notification=true ,"));
    assertEquals(2, map.size());
    assertEquals("rw", map.get("right"));
    assertEquals("true", map.get("notification"));
  }
}
