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

package org.projectforge.web.wicket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.projectforge.web.wicket.MyResourceStreamLocator;


public class MyResourceStreamLocatorTest
{
  @Test
  public void listToString()
  {
    MyResourceStreamLocator loc = new MyResourceStreamLocator();

    assertEquals(MyResourceStreamLocator.WEB_PREFIX + "address/editAddress.html", loc.locateWebResource(MyResourceStreamLocator.PACKAGE_PREFIX + "address/editAddress.html"));
    assertEquals("wa/address/editAddress.html", loc.locateWebResource("org/projectforge/web/address/editAddress.html"));
    assertNull(loc.locateWebResource(null));
    assertEquals("org/test/address/editAddress.html", loc.locateWebResource("org/test/address/editAddress.html"));
    assertEquals("", loc.locateWebResource(""));
  }
}
