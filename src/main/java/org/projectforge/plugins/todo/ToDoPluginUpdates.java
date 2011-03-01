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
import org.projectforge.admin.UpdateEntryImpl;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ToDoPluginUpdates
{
  static DatabaseUpdateDao dao;

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(ToDoPlugin.ID, "1.0.0", "2011-02-28", "Adds table PLUGIN_T_TODO.") {
      final Table table = new Table(ToDoDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        return dao.doesExist(table) == true ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final Table table = new Table(ToDoDO.class) //
            .addAttributes("id", "created", "lastUpdate", "deleted", "reporter", "assignee", "task", "title", "comment", "description",
                "status", "type", "resubmission");
        dao.createTable(table);
        dao.createMissingIndices();
        return UpdateRunningStatus.DONE;
      }
    };
  }
}
