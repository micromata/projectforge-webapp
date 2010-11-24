/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import org.projectforge.web.wicket.AbstractBasePage;

/**
 * Helper for the web menu. Use MenuTreeTable instead.
 */
public class Menu implements Serializable
{
  private static final long serialVersionUID = -4954464926815538198L;

  private Collection<MenuEntry> menuEntries = new ArrayList<MenuEntry>();

  protected MenuEntry selectedMenu;

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
}
