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

package org.projectforge.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.projectforge.test.TestBase;
import org.projectforge.xml.stream.XmlHelper;
import org.projectforge.xml.stream.XmlObjectReader;

public class MenuBuilderTest extends TestBase
{
  private final static String xml = XmlHelper.replaceQuotes(XmlHelper.XML_HEADER
      + "\n"
      + "<menu-entry>\n"
      + "  <sub-menu>\n"
      + "    <menu-entry id='IMAGE_CROPPER' visible='false' />\n"
      + "  </sub-menu>\n"
      + "</menu-entry>\n");

  @Test
  public void testTranslations()
  {
    final XmlObjectReader reader = new XmlObjectReader();
    reader.initialize(MenuEntryConfig.class);
    final MenuEntryConfig root = (MenuEntryConfig) reader.read(xml);
    MenuEntryConfig menu = root.getChildren().get(0);
    assertFalse(menu.isVisible());
    assertEquals(MenuItemDefId.IMAGE_CROPPER, menu.getMenuItemDef());
  }
}
