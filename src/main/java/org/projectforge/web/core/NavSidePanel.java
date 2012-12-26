/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.core;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.MenuEntry;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class NavSidePanel extends NavAbstractPanel
{
  private static final long serialVersionUID = -7858806882044188339L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NavSidePanel.class);

  public NavSidePanel(final String id)
  {
    super(id);
  }

  public void init()
  {
    getMenu();

    // Main menu:
    final RepeatingView menuRepeater = new RepeatingView("menuRepeater");
    add(menuRepeater);
    if (menu.getMenuEntries() != null) {
      for (final MenuEntry menuEntry : menu.getMenuEntries()) {
        if (menuEntry.getSubMenuEntries() == null) {
          continue;
        }
        // Now we add a new menu area (title with sub menus):
        final WebMarkupContainer menuContainer = new WebMarkupContainer(menuRepeater.newChildId());
        menuRepeater.add(menuContainer);
        menuContainer.add(new Label("label", getString(menuEntry.getI18nKey())));
        final Label areaSuffixLabel = getSuffixLabel(menuEntry);
        menuContainer.add(areaSuffixLabel);

        final WebMarkupContainer subMenuContainer = new WebMarkupContainer("subMenu");
        menuContainer.add(subMenuContainer);
        if (menuEntry.hasSubMenuEntries() == false) {
          subMenuContainer.setVisible(false);
          continue;
        }

        final RepeatingView subMenuRepeater = new RepeatingView("subMenuRepeater");
        subMenuContainer.add(subMenuRepeater);
        for (final MenuEntry subMenuEntry : menuEntry.getSubMenuEntries()) {
          if (subMenuEntry.getSubMenuEntries() != null) {
            log.error("Oups: sub sub menus not supported: " + menuEntry.getId() + " has child menus which are ignored.");
          }
          // Now we add the next menu entry to the area:
          final WebMarkupContainer subMenuItem = new WebMarkupContainer(subMenuRepeater.newChildId());
          subMenuRepeater.add(subMenuItem);
          final AbstractLink link = getMenuEntryLink(subMenuEntry, true);
          subMenuItem.add(link);
        }
      }
    }
  }
}
