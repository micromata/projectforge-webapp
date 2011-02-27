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

import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.HibernateUtils;
import org.projectforge.database.Table;
import org.projectforge.database.TableAttribute;
import org.projectforge.database.TableAttributeType;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ToDoPlugin extends AbstractPlugin
{
  public static final String ID = "plugins.todo";

  public static final String RESOURCE_BUNDLE_NAME = ToDoPlugin.class.getPackage().getName() + ".ToDoI18nResources";

  private ToDoDao toDoDao;

  private DatabaseUpdateDao databaseUpdateDao;

  @Override
  protected void initialize()
  {
    final RegistryEntry entry = new RegistryEntry(ID, ToDoDao.class, toDoDao);
    // The ToDoDao is automatically available by the scripting engine!
    register(entry);
    registerDataObject(ToDoDO.class);
    registerListPageColumnsCreator(ID, ToDoListPage.class);
    addMountPages(ID, ToDoListPage.class, ToDoEditPage.class);

    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);
    registerMenuItem(new MenuItemDef(parentMenu, ID, 5, "plugins.todo.menu", ToDoListPage.class));
    // Updater.
    // UserRights.
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  protected UpdatePreCheckStatus checkDatabaseUpdate()
  {
    if (databaseUpdateDao.doesTableExist("T_TODO") == true) {
      return UpdatePreCheckStatus.ALREADY_UPDATED;
    }
    return UpdatePreCheckStatus.OK; // Ready for updating.
  }

  protected UpdateRunningStatus runDatabaseUpdate()
  {
    if (databaseUpdateDao.doesTableExist("T_TODO") == true) {
      return UpdateRunningStatus.DONE;
    }

    // if (dao.doesTableAttributeExist("t_gantt_chart", "settings_as_xml") == false) {
    // if (dao.doesTableExist("t_gantt_chart") == true && dao.dropTable("t_gantt_chart") == false) {
    // throw new RuntimeException("Table t_gantt_chart is not empty! Aborting the update.");
    // }
    // Table table = new Table("t_gantt_chart")
    // .addAttribute(new TableAttribute("pk", TableAttributeType.INT, true).setPrimaryKey(true))
    // .addAttribute(new TableAttribute("created", TableAttributeType.TIMESTAMP))
    // .addAttribute(new TableAttribute("last_update", TableAttributeType.TIMESTAMP))
    // .addAttribute(new TableAttribute("deleted", TableAttributeType.BOOLEAN, true))
    // .addAttribute(new TableAttribute("name", TableAttributeType.VARCHAR, 1000))
    // .addAttribute(new TableAttribute("task_fk", TableAttributeType.INT).setForeignTable("t_task").setForeignAttribute("pk"))
    // .addAttribute(new TableAttribute("settings_as_xml", TableAttributeType.VARCHAR, 10000))
    // .addAttribute(new TableAttribute("style_as_xml", TableAttributeType.VARCHAR, 10000))
    // .addAttribute(new TableAttribute("gantt_objects_as_xml", TableAttributeType.VARCHAR, 10000))
    // .addAttribute(new TableAttribute("owner_fk", TableAttributeType.INT).setForeignTable("t_pf_user").setForeignAttribute("pk"))
    // .addAttribute(new TableAttribute("read_access", TableAttributeType.VARCHAR, 16))
    // .addAttribute(new TableAttribute("write_access", TableAttributeType.VARCHAR, 16));
    // dao.createTable(table);
    Class<?> cls = ToDoDO.class;
    final Table table = new Table("T_TODO") //
        .addAttribute(new TableAttribute(cls, "id")) //
        .addAttribute(new TableAttribute(cls, "created")) //
        .addAttribute(new TableAttribute("last_update", TableAttributeType.TIMESTAMP)) //
        .addAttribute(new TableAttribute("deleted", TableAttributeType.BOOLEAN, true)) //
        .addAttribute(
            new TableAttribute("title", TableAttributeType.VARCHAR, databaseUpdateDao.getColumnLength(ToDoDO.class, "title"), true)) //
        .addAttribute(new TableAttribute("deleted", TableAttributeType.BOOLEAN, true));
    databaseUpdateDao.createTable(table);
    return UpdateRunningStatus.DONE;
  }

  public void setDatabaseUpdateDao(DatabaseUpdateDao databaseUpdateDao)
  {
    this.databaseUpdateDao = databaseUpdateDao;
  }

  public void setToDoDao(ToDoDao toDoDao)
  {
    this.toDoDao = toDoDao;
  }
}
