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

import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.web.registry.WebRegistry;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ToDoPlugin extends AbstractPlugin
{
  public static final String ID = "todo";

  public static final String RESOURCE_BUNDLE_NAME = ToDoPlugin.class.getPackage().getName() + ".ToDoI18nResources";

  private ToDoDao toDoDao;

  @Override
  protected void initialize()
  {
    // Registry in config.xml.
    register(ID, ToDoDao.class, toDoDao, "plugins.todo");
    registerDataObject(ToDoDO.class);
    final WebRegistry webRegistry = WebRegistry.instance();
    webRegistry.register(ID, ToDoListPage.class);
    webRegistry.addMountPages(ID, ToDoListPage.class, ToDoEditPage.class);
    // Menu registration.
    // Updater.
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  public void setToDoDao(ToDoDao toDoDao)
  {
    this.toDoDao = toDoDao;
  }
}
