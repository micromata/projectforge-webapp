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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.projectforge.access.AccessChecker;
import org.projectforge.plugins.todo.ToDoPlugin;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.core.NavAbstractPanel;

/**
 * The customizable menu of the user (stored in the data-base and customizable).
 */
public class FavoritesMenu implements Serializable
{
  public static final String USER_PREF_FAVORITES_MENU_KEY = "usersFavoritesMenu";

  private static final String USER_PREF_FAVORITES_MENU_ENTRIES_KEY = "usersFavoriteMenuEntries";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FavoritesMenu.class);

  private static final long serialVersionUID = -4954464926815538198L;

  private List<MenuEntry> menuEntries;

  private final Menu menu;

  private final AccessChecker accessChecker;

  private final UserXmlPreferencesCache userXmlPreferencesCache;

  public enum ParseMode
  {
    JS_TREE, USER_PREF
  };

  public static FavoritesMenu get(final UserXmlPreferencesCache userXmlPreferencesCache, final AccessChecker accessChecker)
  {
    final FavoritesMenu favoritesMenu = (FavoritesMenu) userXmlPreferencesCache.getEntry(USER_PREF_FAVORITES_MENU_KEY);
    if (favoritesMenu != null) {
      return favoritesMenu;
    }
    return new FavoritesMenu(userXmlPreferencesCache, accessChecker);
  }

  /**
   * @param userXmlPreferencesCache For storing and getting the persisted favorites menu.
   * @param accessChecker For building the menu entries regarding the access rights of the logged-in user.
   */
  private FavoritesMenu(final UserXmlPreferencesCache userXmlPreferencesCache, final AccessChecker accessChecker)
  {
    this.menu = (Menu) userXmlPreferencesCache.getEntry(NavAbstractPanel.USER_PREF_MENU_KEY);
    this.userXmlPreferencesCache = userXmlPreferencesCache;
    this.accessChecker = accessChecker;
    init();
  }

  public List<MenuEntry> getMenuEntries()
  {
    return this.menuEntries;
  }

  public void readFromXml(final String menuAsXml, final ParseMode mode)
  {
    if (log.isDebugEnabled() == true) {
      log.debug("readFromXml: " + menuAsXml);
    }
    Document document = null;
    try {
      document = DocumentHelper.parseText(menuAsXml);
    } catch (final DocumentException ex) {
      log.error("Exception encountered " + ex, ex);
      return;
    }
    final MenuBuilderContext context = new MenuBuilderContext(menu, accessChecker, PFUserContext.getUser(), false);
    final Element root = document.getRootElement();
    menuEntries = new ArrayList<MenuEntry>();
    for (final Iterator< ? > it = root.elementIterator("item"); it.hasNext();) {
      final Element item = (Element) it.next();
      final MenuEntry menuEntry = readFromXml(item, context, mode);
      menuEntries.add(menuEntry);
    }
  }

  private MenuEntry readFromXml(final Element item, final MenuBuilderContext context, final ParseMode mode)
  {
    if ("item".equals(item.getName()) == false) {
      log.error("Tag 'item' expected instead of '" + item.getName() + "'. Ignoring this tag.");
      return null;
    }
    String id = item.attributeValue("id");
    MenuItemDef menuItemDef = null;
    if (id != null && id.startsWith("c-") == true) {
      id = id.substring(2);
    }
    if (id != null) {
      menuItemDef = MenuItemRegistry.instance().get(id);
    }
    final MenuEntry menuEntry;
    if (menuItemDef != null) {
      menuEntry = new MenuEntry(menuItemDef, context);
    } else {
      menuEntry = new MenuEntry();
    }
    Element title;
    if (mode == ParseMode.JS_TREE) {
      final Element content = item.element("content");
      title = content != null ? content.element("name") : null;
    } else {
      title = item;
    }
    if (title != null) {
      if (title.getTextTrim() != null) {
        menuEntry.setName(title.getTextTrim());
      }
    }
    for (final Iterator< ? > it = item.elementIterator("item"); it.hasNext();) {
      if (menuItemDef != null) {
        log.warn("Menu entry shouldn't have children, because it's a leaf node.");
      }
      final Element child = (Element) it.next();
      final MenuEntry childMenuEntry = readFromXml(child, context, mode);
      if (childMenuEntry != null) {
        menuEntry.addMenuEntry(childMenuEntry);
      }
    }
    return menuEntry;
  }

  private void init()
  {
    this.menuEntries = new ArrayList<MenuEntry>();
    final String userPrefString = (String) userXmlPreferencesCache.getEntry(USER_PREF_FAVORITES_MENU_ENTRIES_KEY);
    if (StringUtils.isBlank(userPrefString) == false) {
      if (userPrefString.contains("<root>") == false) {
        // Old format:
        buildFromOldUserPrefFormat(userPrefString);
      } else {
        readFromXml(userPrefString, ParseMode.USER_PREF);
      }
    }
    if (this.menuEntries.size() == 0) {
      final MenuItemRegistry registry = MenuItemRegistry.instance();
      addFavoriteMenuEntry(registry.get(MenuItemDefId.TASK_TREE));
      addFavoriteMenuEntry(registry.get(MenuItemDefId.CALENDAR));
      addFavoriteMenuEntry(registry.get(MenuItemDefId.ADDRESS_LIST));
      addFavoriteMenuEntry(registry.get(MenuItemDefId.BOOK_LIST));
      addFavoriteMenuEntry(registry.get(MenuItemDefId.PHONE_CALL));
      addFavoriteMenuEntry(registry.get(ToDoPlugin.ID));
    }
  }

  private void addFavoriteMenuEntry(final MenuItemDef menuItemDef)
  {
    final MenuEntry menuEntry = menu.getMenuEntry(menuItemDef);
    if (menuEntry == null) {
      return;
    }
    for (final MenuEntry entry : this.menuEntries) {
      if (entry.menuItemDef == menuItemDef) {
        return;
      }
    }
    this.menuEntries.add(menuEntry);
  }

  /**
   * @param userPrefEntry coma separated list of MenuItemDefs.
   */
  private void buildFromOldUserPrefFormat(final String userPrefEntry)
  {
    this.menuEntries = new ArrayList<MenuEntry>();
    if (userPrefEntry == null) {
      return;
    }
    final StringTokenizer tokenizer = new StringTokenizer(userPrefEntry, ",");
    while (tokenizer.hasMoreTokens() == true) {
      String token = tokenizer.nextToken();
      if (token.startsWith("M_") == true) {
        token = token.substring(2);
      }
      try {
        final MenuItemDef menuItemDef = MenuItemRegistry.instance().get(token);
        if (menuItemDef == null) {
          continue;
        }
        addFavoriteMenuEntry(menuItemDef);
      } catch (final Exception ex) {
        log.info("Menu '" + token + "' not found: " + ex.getMessage(), ex);
      }
    }
  }

  public void storeAsUserPref()
  {
    if (CollectionUtils.isEmpty(menuEntries) == true) {
      userXmlPreferencesCache.putEntry(USER_PREF_FAVORITES_MENU_ENTRIES_KEY, "", true);
      userXmlPreferencesCache.removeEntry(USER_PREF_FAVORITES_MENU_KEY);
      return;
    }
    final Document document = DocumentHelper.createDocument();
    final Element root = document.addElement("root");
    for (final MenuEntry menuEntry : menuEntries) {
      buildElement(root.addElement("item"), menuEntry);
    }
    final String xml = document.asXML();
    userXmlPreferencesCache.putEntry(USER_PREF_FAVORITES_MENU_ENTRIES_KEY, xml, true);
    userXmlPreferencesCache.putEntry(USER_PREF_FAVORITES_MENU_KEY, this, false);
    if (log.isDebugEnabled() == true) {
      log.debug("Favorites menu stored: " + xml);
    }
    log.info("Favorites menu stored: " + xml);
  }

  private void buildElement(final Element element, final MenuEntry menuEntry)
  {
    if (menuEntry.getId() != null) {
      element.addAttribute("id", menuEntry.getId());
    }
    if (menuEntry.getName() != null) {
      element.addText(menuEntry.getName());
    }
    if (menuEntry.hasSubMenuEntries() == true) {
      for (final MenuEntry subMenuEntry : menuEntry.getSubMenuEntries()) {
        buildElement(element.addElement("item"), subMenuEntry);
      }
    }
  }
}
