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

package org.projectforge;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VersionTest
{
  @Test
  public void compare()
  {
    final Version v0_95 = new Version("0.95");
    final Version v1 = new Version("1.0");
    final Version v1_2 = new Version("1.2");
    final Version v1_10 = new Version("1.10");
    final Version v1_10_17_1043 = new Version("1.10.17.1043");
    final Version v1_10_17_1043b = new Version("1.10.17.1043");
    final Version v1_10_17_1044 = new Version("1.10.17.1044");
    final Version v1_10_17_1045 = new Version("1.10.17.1045");

    compare(v0_95, v1);
    equal(v1_10_17_1043, v1_10_17_1043b);
    compare(v1_2, v1_10);
    compare(v1_10_17_1043, v1_10_17_1044);
    compare(v1_10_17_1043, v1_10_17_1045);
    compare(v1_10_17_1044, v1_10_17_1045);
  }

  private void compare(final Version v1, final Version v2)
  {
    assertEquals(-1, v1.compareTo(v2));
    assertEquals(1, v2.compareTo(v1));
    equal(v1, v1);
    equal(v2, v2);
  }

  private void equal(final Version v1, final Version v2)
  {
    assertEquals(0, v1.compareTo(v2));
    assertEquals(0, v2.compareTo(v1));
  }

  @Test
  public void testToString()
  {
    assertEquals("3.5", new Version(3, 5, 0, 0).toString());
    assertEquals("3.5", new Version(3, 5, 0).toString());
    assertEquals("3.0", new Version(3, 0, 0).toString());
    assertEquals("0.0", new Version(0, 0, 0).toString());
    assertEquals("3.5.0.1", new Version(3, 5, 0, 1).toString());
    assertEquals("3.0.0.1", new Version(3, 0, 0, 1).toString());

    assertEquals("3.5", new Version("3.5.0.0").toString());
    assertEquals("3.5", new Version("3.5.0.0").toString());
    assertEquals("3.0", new Version("3.0.0").toString());
    assertEquals("3.0.0.1", new Version("3.0.0.1").toString());
    assertEquals("3.5.4.1", new Version("3.5.4.1").toString());
  }
}
