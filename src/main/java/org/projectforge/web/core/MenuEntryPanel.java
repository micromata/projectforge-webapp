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

package org.projectforge.web.core;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.MenuEntry;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MenuEntryPanel extends Panel
{
  private static final long serialVersionUID = -5842187160235305180L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MenuEntryPanel.class);

  public MenuEntryPanel(String id)
  {
    super(id);
  }

  public void init(final MenuEntry menuEntry)
  {
    add(new Label("entry", getString(menuEntry.getLabel())));
    if (menuEntry.hasSubMenuEntries() == true) {
      final WebMarkupContainer ul = new WebMarkupContainer("subMenu");
      add(ul);
      final RepeatingView menuRepeater = new RepeatingView("menuEntries");
      ul.add(menuRepeater);
      for (final MenuEntry subEntry : menuEntry.getSubMenuEntries()) {
        final MenuEntryPanel entryPanel = new MenuEntryPanel(menuRepeater.newChildId());
        entryPanel.setRenderBodyOnly(true);
        menuRepeater.add(entryPanel);
        entryPanel.init(subEntry);
      }
    } else {
      add(new Label("subMenu").setVisible(false));
    }
  }
}
