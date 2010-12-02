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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.Menu;
import org.projectforge.web.MenuBuilder;
import org.projectforge.web.MenuEntry;
import org.projectforge.web.wicket.AbstractSecuredPage;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MenuPanel extends Panel
{
  private static final long serialVersionUID = -7858806882044188339L;

  private static final String USER_PREF_MENU_KEY = "usersMenu";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MenuPanel.class);

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  private Menu menu;

  public MenuPanel(String id)
  {
    super(id);
  }

  public void init()
  {
    final RepeatingView menuRepeater = new RepeatingView("menuEntries");
    add(menuRepeater);

    getMenu();
    for (final MenuEntry entry : menu.getMenuEntries()) {
      final MenuEntryPanel entryPanel = new MenuEntryPanel(menuRepeater.newChildId());
      entryPanel.setRenderBodyOnly(true);
      menuRepeater.add(entryPanel);
      entryPanel.init(entry);
    }
  }

  private Menu getMenu()
  {
    AbstractSecuredPage securedPage = null;
    if (getPage() instanceof AbstractSecuredPage) {
      securedPage = ((AbstractSecuredPage) getPage());
      menu = (Menu) securedPage.getUserPrefEntry(USER_PREF_MENU_KEY);
      if (menu != null) {
        return menu;
      }
    }
    if (menu != null) { // After getting menu from user pref entry, because otherwise resetMenu() doesn't work if menu is stored in this
      // panel.
      return menu;
    }
    if (log.isDebugEnabled() == true) {
      log.debug("Build new menu.");
    }
    menu = menuBuilder.getMenu(PFUserContext.getUser());
    if (securedPage != null) {
      securedPage.putUserPrefEntry(USER_PREF_MENU_KEY, menu, false);
    }
    return menu;
  }

}
