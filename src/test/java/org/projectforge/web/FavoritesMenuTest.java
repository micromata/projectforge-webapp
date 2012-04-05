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

package org.projectforge.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.FavoritesMenu.ParseMode;
import org.projectforge.web.wicket.WicketPageTestBase;
import org.projectforge.xml.stream.XmlHelper;

public class FavoritesMenuTest extends WicketPageTestBase
{
  private final static String jsTreeXml = XmlHelper.replaceQuotes("<?xml version='1.0' encoding='UTF-8'?><root>" //
      + "<item id='c-TASK_TREE' rel='leaf'><content><name><![CDATA[Aufgaben]]></name></content></item>" //
      + "<item id='c-CALENDAR' rel='leaf'><content><name><![CDATA[Kalender]]></name></content></item>" //
      + "<item id='c-toDo' rel='leaf'><content><name><![CDATA[ToDo]]></name></content></item>" //
      + "<item id='c-BOOK_LIST' rel='leaf'><content><name><![CDATA[Bücher]]></name></content></item>" //
      + "<item id='c-HR_VIEW' rel='leaf'><content><name><![CDATA[Personalplanung]]></name></content></item>" //
      // Fibu
      + "<item id='' rel='' state='open'><content><name><![CDATA[FiBu]]></name></content>" //
      + "<item id='c-ORDER_LIST' rel='leaf'><content><name><![CDATA[Auftragsbuch]]></name></content></item>" //
      + "<item id='c-OUTGOING_INVOICE_LIST' rel='leaf'><content><name><![CDATA[Debitorenrechnungen]]></name></content></item>" //
      + "</item>" //
      // Adressen
      + "<item id='' rel='' state='open'><content><name><![CDATA[Adressen]]></name></content>" //
      + "<item id='c-ADDRESS_LIST' rel='leaf'><content><name><![CDATA[Adressen]]></name></content></item>" //
      + "<item id='c-addressCampaignValues' rel='leaf'><content><name><![CDATA[Adressen für Kampagnen]]></name></content></item>" //
      + "</item>" //
      + "<item id='c-PHONE_CALL' rel='leaf'><content><name><![CDATA[Direktwahl]]></name></content></item>" //
      + "</root>");

  private UserXmlPreferencesCache userXmlPreferencesCache;

  @Test
  public void readJsTreeXml()
  {
    logon(TEST_ADMIN_USER);
    final FavoritesMenu menu = new FavoritesMenu(userXmlPreferencesCache, accessChecker);
    menu.readFromXml(jsTreeXml, ParseMode.JS_TREE);
    final List<MenuEntry> mainEntries = menu.getMenuEntries();
    assertEquals(8, mainEntries.size());
    assertEquals("TASK_TREE", mainEntries.get(0).getId());
    assertEquals("menu.taskTree", mainEntries.get(0).getI18nKey());
    final MenuEntry fibu = mainEntries.get(5);
    assertNull(fibu.getId());
    assertEquals("FiBu", fibu.getName());
    assertNull(fibu.getI18nKey());
    final Collection<MenuEntry> fibuEntries = fibu.getSubMenuEntries();
    assertEquals(2, fibuEntries.size());
    assertEquals("ORDER_LIST", fibuEntries.iterator().next().getId());
  }

  /**
   * @param userXmlPreferencesCache the userXmlPreferencesCache to set
   */
  @Override
  public void setUserXmlPreferencesCache(final UserXmlPreferencesCache userXmlPreferencesCache)
  {
    this.userXmlPreferencesCache = userXmlPreferencesCache;
  }
}
