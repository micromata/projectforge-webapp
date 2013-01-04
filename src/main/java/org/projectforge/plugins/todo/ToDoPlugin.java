/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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
import org.projectforge.registry.DaoRegistry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ToDoPlugin extends AbstractPlugin
{
  public static final String ID = "toDo";

  public static final String RESOURCE_BUNDLE_NAME = ToDoPlugin.class.getPackage().getName() + ".ToDoI18nResources";

  static UserPrefArea USER_PREF_AREA;

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { ToDoDO.class};

  /**
   * This dao should be defined in pluginContext.xml (as resources) for proper initialization.
   */
  private ToDoDao toDoDao;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#initialize()
   */
  @Override
  protected void initialize()
  {
    // DatabaseUpdateDao is needed by the updater:
    ToDoPluginUpdates.dao = databaseUpdateDao;
    final RegistryEntry entry = new RegistryEntry(ID, ToDoDao.class, toDoDao, "plugins.todo");
    // The ToDoDao is automatically available by the scripting engine!
    register(entry); // Insert at second position after Address entry (for SearchPage).

    // Register the web part:
    registerWeb(ID, ToDoListPage.class, ToDoEditPage.class, DaoRegistry.ADDRESS, false); // Insert at second position after Address entry (for SearchPage).

    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);
    registerMenuItem(new ToDoMenuItemDef(parentMenu, ID, 5, "plugins.todo.menu", ToDoListPage.class));
    // .setMobileMenu(ToDoMobileListPage.class, 10));

    // Define the access management:
    registerRight(new ToDoRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);

    // Register favorite entries (the user can modify these templates/favorites via 'own settings'):
    USER_PREF_AREA = registerUserPrefArea("TODO_FAVORITE", ToDoDO.class, "todo.favorite");
  }

  /**
   * Setter is called by the Spring framework with a proper initialized data access object (defined in pluginContext.xml).
   * @param toDoDao
   */
  public void setToDoDao(final ToDoDao toDoDao)
  {
    this.toDoDao = toDoDao;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#getInitializationUpdateEntry()
   */
  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return ToDoPluginUpdates.getInitializationUpdateEntry();
  }
}
