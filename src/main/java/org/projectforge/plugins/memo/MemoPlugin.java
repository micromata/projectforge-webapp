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

package org.projectforge.plugins.memo;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.user.UserRights;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * Your plugin initialization. Register all your components such as i18n files, data-access object etc.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MemoPlugin extends AbstractPlugin
{
  public static final String ID = "Memo";

  public static final String RESOURCE_BUNDLE_NAME = MemoPlugin.class.getPackage().getName() + ".MemoI18nResources";

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { MemoDO.class};

  private MemoDao memoDao;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }

  @Override
  protected void initialize()
  {
    MemoPluginUpdates.dao = databaseUpdateDao;
    final RegistryEntry entry = new RegistryEntry(ID, MemoDao.class, memoDao);
    // The MemoDao is automatically available by the scripting engine!
    register(entry);
    registerListPageColumnsCreator(ID, MemoListPage.class);
    addMountPages(ID, MemoListPage.class, MemoEditPage.class);

    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);
    registerMenuItem(new MenuItemDef(parentMenu, ID, 10, "plugins.memo.menu", MemoListPage.class));

    UserRights.instance().addRight(new MemoRight());
    // Memo: Hibernate-search indexer.
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  public void setMemoDao(MemoDao memoDao)
  {
    this.memoDao = memoDao;
  }

  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return MemoPluginUpdates.getInitializationUpdateEntry();
  }
}
