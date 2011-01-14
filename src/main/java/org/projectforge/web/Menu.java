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

import org.projectforge.web.wicket.AbstractBasePage;

/**
 * Helper for the web menu. Use MenuTreeTable instead.
 */
public class Menu implements Serializable
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Menu.class);

  private static final long serialVersionUID = -4954464926815538198L;

  private Collection<MenuEntry> menuEntries = new ArrayList<MenuEntry>();

  protected MenuEntry selectedMenu;

  protected List<MenuEntry> favoriteMenuEntries;

  public void setSelectedMenu(final AbstractBasePage page)
  {
    if (menuEntries != null) {
      for (final MenuEntry entry : menuEntries) {
        final MenuEntry found = entry.findMenu(page.getPageClass());
        if (found != null) {
          this.selectedMenu = found;
        }
      }
    }
  }

  public Collection<MenuEntry> getMenuEntries()
  {
    return menuEntries;
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
        final MenuItemDef menuItemDef = MenuItemDef.valueOf(token);
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
        addFavoriteMenuEntry(MenuItemDef.TASK_TREE);
        addFavoriteMenuEntry(MenuItemDef.CALENDAR);
        addFavoriteMenuEntry(MenuItemDef.ADDRESS_LIST);
        addFavoriteMenuEntry(MenuItemDef.BOOK_LIST);
        addFavoriteMenuEntry(MenuItemDef.PHONE_CALL);
        addFavoriteMenuEntry(MenuItemDef.MEB);
      }
      return this.favoriteMenuEntries;
    }
  }

  public boolean isFirst(final MenuEntry entry)
  {
    return (menuEntries != null && menuEntries.size() > 0 && menuEntries.iterator().next() == entry);
  }

  public MenuEntry addMenuEntry(final MenuItemDef menuItemDef)
  {
    return addMenuEntry(null, menuItemDef);
  }

  public MenuEntry addMenuEntry(final MenuEntry parent, final MenuItemDef menuItemDef)
  {
    final MenuEntry menuEntry = new MenuEntry(menuItemDef, this);
    if (parent == null) {
      this.menuEntries.add(menuEntry);
    } else {
      parent.addMenuEntry(menuEntry);
    }
    return menuEntry;
  }

  private MenuEntry getMenuEntry(final MenuItemDef menuItemDef)
  {
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
