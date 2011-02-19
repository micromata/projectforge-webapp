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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helper for the web menu. Use MenuTreeTable instead.
 */
public class Menu implements Serializable
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Menu.class);

  private static final long serialVersionUID = -4954464926815538198L;

  private MenuEntry rootMenuEntry = new MenuEntry();

  protected List<MenuEntry> favoriteMenuEntries;

  public Collection<MenuEntry> getMenuEntries()
  {
    return rootMenuEntry.getSubMenuEntries();
  }

  public MenuEntry findById(final String id)
  {
    return rootMenuEntry.findById(id);
  }

  /**
   * @param favoritesString coma separated list of MenuItemDefs.
   */
  public void setFavoriteMenuEntries(final String favoritesString)
  {
    this.favoriteMenuEntries = new ArrayList<MenuEntry>();
    if (favoritesString == null) {
      return;
    }
    final StringTokenizer tokenizer = new StringTokenizer(favoritesString, ",");
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

  public List<MenuEntry> getFavoriteMenuEntries()
  {
    synchronized (this) {
      if (this.favoriteMenuEntries == null || this.favoriteMenuEntries.size() == 0) {
        this.favoriteMenuEntries = new ArrayList<MenuEntry>();
        final MenuItemRegistry registry = MenuItemRegistry.instance();
        addFavoriteMenuEntry(registry.get(MenuItemDefId.TASK_TREE));
        addFavoriteMenuEntry(registry.get(MenuItemDefId.CALENDAR));
        addFavoriteMenuEntry(registry.get(MenuItemDefId.ADDRESS_LIST));
        addFavoriteMenuEntry(registry.get(MenuItemDefId.BOOK_LIST));
        addFavoriteMenuEntry(registry.get(MenuItemDefId.PHONE_CALL));
        addFavoriteMenuEntry(registry.get(MenuItemDefId.MEB));
      }
      return this.favoriteMenuEntries;
    }
  }

  public boolean isFirst(final MenuEntry entry)
  {
    return (rootMenuEntry.subMenuEntries != null && rootMenuEntry.subMenuEntries.size() > 0 && rootMenuEntry.subMenuEntries.iterator()
        .next() == entry);
  }

  public void addMenuEntry(final MenuEntry menuEntry)
  {
    MenuEntry parent = menuEntry.getParent();
    if (parent == null) {
      final MenuItemDef parentItemDef = menuEntry.menuItemDef.getParent();
      if (parentItemDef == null) {
        parent = rootMenuEntry;
      } else {
        parent = getMenuEntry(parentItemDef);
        if (parent == null) {
          log.error("Oups, can't find parent menu item: " + parentItemDef.getId());
          parent = rootMenuEntry;
        }
      }
    }
    parent.addMenuEntry(menuEntry);
  }

  private MenuEntry getMenuEntry(final MenuItemDef menuItemDef)
  {
    if (getMenuEntries() == null) {
      return null;
    }
    for (final MenuEntry menuEntry : getMenuEntries()) {
      final MenuEntry result = getMenuEntry(menuEntry, menuItemDef);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private MenuEntry getMenuEntry(final MenuEntry parent, final MenuItemDef menuItemDef)
  {
    if (parent.menuItemDef == menuItemDef) {
      return parent;
    }
    if (parent.hasSubMenuEntries() == true) {
      for (final MenuEntry subMenuEntry : parent.getSubMenuEntries()) {
        final MenuEntry result = getMenuEntry(subMenuEntry, menuItemDef);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  private void addFavoriteMenuEntry(final MenuItemDef menuItemDef)
  {
    final MenuEntry menuEntry = getMenuEntry(menuItemDef);
    if (menuEntry == null) {
      return;
    }
    for (final MenuEntry entry : this.favoriteMenuEntries) {
      if (entry.menuItemDef == menuItemDef) {
        return;
      }
    }
    this.favoriteMenuEntries.add(menuEntry);
  }
}
