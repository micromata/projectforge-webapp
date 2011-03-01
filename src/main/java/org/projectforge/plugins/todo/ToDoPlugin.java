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

package org.projectforge.plugins.todo;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ToDoPlugin extends AbstractPlugin
{
  public static final String ID = "ToDo";

  public static final String RESOURCE_BUNDLE_NAME = ToDoPlugin.class.getPackage().getName() + ".ToDoI18nResources";

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { ToDoDO.class};

  private ToDoDao toDoDao;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }
  
  @Override
  protected void initialize()
  {
    ToDoPluginUpdates.dao = databaseUpdateDao;
    final RegistryEntry entry = new RegistryEntry(ID, ToDoDao.class, toDoDao);
    // The ToDoDao is automatically available by the scripting engine!
    register(entry);
    registerListPageColumnsCreator(ID, ToDoListPage.class);
    addMountPages(ID, ToDoListPage.class, ToDoEditPage.class);

    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);
    registerMenuItem(new MenuItemDef(parentMenu, ID, 5, "plugins.todo.menu", ToDoListPage.class));
    // UserRights.
    // Hibernate-search indexer.
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  public void setToDoDao(ToDoDao toDoDao)
  {
    this.toDoDao = toDoDao;
  }

  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return ToDoPluginUpdates.getInitializationUpdateEntry();
  }
}
