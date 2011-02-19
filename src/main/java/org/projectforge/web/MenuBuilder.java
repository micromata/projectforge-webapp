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

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.PFUserDO;

public class MenuBuilder implements Serializable
{
  private static final long serialVersionUID = -924049082728488113L;

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  public void setAccessChecker(AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  private MenuCache menuCache = new MenuCache();

  public void expireMenu(final Integer userId)
  {
    menuCache.removeMenu(userId);
  }

  public void refreshAllMenus()
  {
    menuCache.setExpired();
  }

  private void buildMenuTree(final Menu menu, final PFUserDO user)
  {
    if (user == null) {
      return;
    }
    final MenuBuilderContext context = new MenuBuilderContext(menu, accessChecker, user);
    final MenuItemRegistry registry = MenuItemRegistry.instance();
    if (LoginPage.FIRST_PSEUDO_SETUP_USER.equals(user.getUsername()) == true) {
      final MenuItemDef firstLogin = registry.get(MenuItemDefId.SYSTEM_FIRST_LOGIN_SETUP_PAGE);
      menu.addMenuEntry(firstLogin.createMenuEntry(menu, context));
      return;
    }
    for (final MenuItemDef menuItemDef : registry.getMenuItemList()) {
      final MenuEntry menuEntry = menuItemDef.createMenuEntry(menu, context);
      if (menuEntry == null) {
        continue;
      }
    }
  }

  public Menu getMenu(final PFUserDO user)
  {
    Menu menu = null;
    if (user != null) {
      menu = menuCache.getMenu(user.getId());
      if (menu != null) {
        return menu;
      }
    }
    menu = new Menu();
    buildMenuTree(menu, user);
    if (user != null) {
      menuCache.putMenu(user.getId(), menu);
    }
    return menu;
  }
}
