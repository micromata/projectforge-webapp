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

package org.projectforge.admin;

import static org.projectforge.admin.SystemUpdater.CORE_REGION_ID;
import java.util.ArrayList;
import java.util.List;

import org.projectforge.database.DatabaseUpdateDO;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;
import org.projectforge.database.TableAttribute;
import org.projectforge.database.TableAttributeType;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class DatabaseCoreUpdates
{
  @SuppressWarnings("serial")
  public static List<UpdateEntry> getUpdateEntries()
  {
    final List<UpdateEntry> list = new ArrayList<UpdateEntry>();
    list.add(new UpdateEntryImpl(CORE_REGION_ID, "3.5.4", "Adds table t_database_update.") {
      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        return this.preCheckStatus = dao.doesTableExist("t_database_update") == true ? UpdatePreCheckStatus.ALREADY_UPDATED
            : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        if (dao.doesTableExist(DatabaseUpdateDO.TABLE_NAME) == true) {
          return this.runningStatus = UpdateRunningStatus.DONE;
        }
        final Table table = new Table(DatabaseUpdateDO.TABLE_NAME) //
            .addAttribute(new TableAttribute("update_date", TableAttributeType.TIMESTAMP)) //
            .addAttribute(new TableAttribute("region_id", TableAttributeType.VARCHAR, 1000)) //
            .addAttribute(new TableAttribute("version", TableAttributeType.VARCHAR, 15)) //
            .addAttribute(new TableAttribute("execution_result", TableAttributeType.VARCHAR, 1000)) //
            .addAttribute(
                new TableAttribute("executed_by_user_fk", TableAttributeType.INT).setForeignTable("t_pf_user").setForeignAttribute("pk")) //
            .addAttribute(new TableAttribute("description", TableAttributeType.VARCHAR, 4000));
        dao.createTable(table);
        return this.runningStatus = UpdateRunningStatus.DONE;
      }
    });
    return list;
  }
}
