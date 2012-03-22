/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import org.projectforge.address.AddressDO;
import org.projectforge.database.DatabaseUpdateDO;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;
import org.projectforge.database.TableAttribute;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KundeDO;
import org.projectforge.registry.Registry;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class DatabaseCoreUpdates
{
  @SuppressWarnings("serial")
  public static List<UpdateEntry> getUpdateEntries()
  {
    final List<UpdateEntry> list = new ArrayList<UpdateEntry>();
    // /////////////////////////////////////////////////////////////////
    // 4.0
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(CORE_REGION_ID, "4.0", "2012-03-22", "Adds 6th parameter to t_script.") {
      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        final Table scriptTable = new Table(ScriptDO.class);
        return dao.doesTableAttributesExist(scriptTable, "parameter6Name") == true //
            && dao.doesTableAttributesExist(scriptTable, "parameter6Type") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        final Table scriptTable = new Table(ScriptDO.class);
        if (dao.doesTableAttributesExist(scriptTable, "parameter6Name") == false) {
          dao.addTableAttributes(scriptTable, new TableAttribute(ScriptDO.class, "parameter6Name"));
        }
        if (dao.doesTableAttributesExist(scriptTable, "parameter6Type") == false) {
          dao.addTableAttributes(scriptTable, new TableAttribute(ScriptDO.class, "parameter6Type"));
        }
        return UpdateRunningStatus.DONE;
      }
    });

    // /////////////////////////////////////////////////////////////////
    // 3.6.2
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(
        CORE_REGION_ID,
        "3.6.1.3",
        "2011-12-05",
        "Adds columns t_kunde.konto_id, t_fibu_eingangsrechnung.konto_id, t_konto.status, t_task.protection_of_privacy and t_address.communication_language.") {
      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        final Table kundeTable = new Table(KundeDO.class);
        final Table eingangsrechnungTable = new Table(EingangsrechnungDO.class);
        final Table kontoTable = new Table(KontoDO.class);
        final Table taskTable = new Table(TaskDO.class);
        final Table addressTable = new Table(AddressDO.class);
        return dao.doesTableAttributesExist(kundeTable, "konto") == true //
            && dao.doesTableAttributesExist(eingangsrechnungTable, "konto") == true //
            && dao.doesTableAttributesExist(kontoTable, "status") == true //
            && dao.doesTableAttributesExist(addressTable, "communicationLanguage") == true //
            && dao.doesTableAttributesExist(taskTable, "protectionOfPrivacy") //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        final Table kundeTable = new Table(KundeDO.class);
        if (dao.doesTableAttributesExist(kundeTable, "konto") == false) {
          dao.addTableAttributes(kundeTable, new TableAttribute(KundeDO.class, "konto"));
        }
        final Table eingangsrechnungTable = new Table(EingangsrechnungDO.class);
        if (dao.doesTableAttributesExist(eingangsrechnungTable, "konto") == false) {
          dao.addTableAttributes(eingangsrechnungTable, new TableAttribute(EingangsrechnungDO.class, "konto"));
        }
        final Table kontoTable = new Table(KontoDO.class);
        if (dao.doesTableAttributesExist(kontoTable, "status") == false) {
          dao.addTableAttributes(kontoTable, new TableAttribute(KontoDO.class, "status"));
        }
        final Table taskTable = new Table(TaskDO.class);
        if (dao.doesTableAttributesExist(taskTable, "protectionOfPrivacy") == false) {
          dao.addTableAttributes(taskTable, new TableAttribute(TaskDO.class, "protectionOfPrivacy").setDefaultValue("false"));
        }
        final Table addressTable = new Table(AddressDO.class);
        if (dao.doesTableAttributesExist(addressTable, "communicationLanguage") == false) {
          dao.addTableAttributes(addressTable, new TableAttribute(AddressDO.class, "communicationLanguage"));
        }
        dao.createMissingIndices();
        return UpdateRunningStatus.DONE;
      }
    });

    // /////////////////////////////////////////////////////////////////
    // 3.5.4
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(CORE_REGION_ID, "3.5.4", "2011-02-24",
        "Adds table t_database_update. Adds attribute (excel_)date_format, hour_format_24 to table t_pf_user.") {
      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        final Table dbUpdateTable = new Table(DatabaseUpdateDO.class);
        final Table userTable = new Table(PFUserDO.class);
        return dao.doesExist(dbUpdateTable) == true
            && dao.doesTableAttributesExist(userTable, "dateFormat", "excelDateFormat", "timeNotation") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        final Table dbUpdateTable = new Table(DatabaseUpdateDO.class);
        final Table userTable = new Table(PFUserDO.class);
        dbUpdateTable.addAttributes("updateDate", "regionId", "versionString", "executionResult", "executedBy", "description");
        dao.createTable(dbUpdateTable);
        dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "dateFormat"));
        dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "excelDateFormat"));
        dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "timeNotation"));
        final UserDao userDao = (UserDao) Registry.instance().getDao(UserDao.class);
        dao.createMissingIndices();
        userDao.getUserGroupCache().setExpired();
        return UpdateRunningStatus.DONE;
      }
    });
    return list;
  }
}
