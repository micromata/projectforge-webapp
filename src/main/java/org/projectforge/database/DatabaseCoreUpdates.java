/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.database;

import java.util.ArrayList;
import java.util.List;

import org.projectforge.address.AddressDO;
import org.projectforge.continuousdb.DatabaseResultRow;
import org.projectforge.continuousdb.Table;
import org.projectforge.continuousdb.TableAttribute;
import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.continuousdb.UpdateEntryImpl;
import org.projectforge.continuousdb.UpdatePreCheckStatus;
import org.projectforge.continuousdb.UpdateRunningStatus;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.registry.Registry;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class DatabaseCoreUpdates
{
  public static final String CORE_REGION_ID = DatabaseCoreInitial.CORE_REGION_ID;

  private static final String VERSION_5_0 = "5.0";

  static MyDatabaseUpdateDao dao;

  @SuppressWarnings("serial")
  public static List<UpdateEntry> getUpdateEntries()
  {
    final List<UpdateEntry> list = new ArrayList<UpdateEntry>();
    // /////////////////////////////////////////////////////////////////
    // 5.3
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(CORE_REGION_ID, "5.3", "2013-11-24", "Adds t_pf_user.last_password_change, t_pf_user.password_salt.") {

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        if (dao.doTableAttributesExist(PFUserDO.class, "lastPasswordChange", "passwordSalt") == false) {
          return UpdatePreCheckStatus.READY_FOR_UPDATE;
        }
        return UpdatePreCheckStatus.ALREADY_UPDATED;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doTableAttributesExist(PFUserDO.class, "lastPasswordChange", "passwordSalt") == false) {
          dao.addTableAttributes(PFUserDO.class, "lastPasswordChange", "passwordSalt");
        }
        return UpdateRunningStatus.DONE;
      }
    });

    // /////////////////////////////////////////////////////////////////
    // 5.2
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(
        CORE_REGION_ID,
        "5.2",
        "2013-05-13",
        "Adds t_fibu_auftrag_position.time_of_performance_{start|end}, t_script.file{_name} and changes type of t_script.script{_backup} to byte[].") {
      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        if (dao.doTableAttributesExist(ScriptDO.class, "file", "filename") == true
            && dao.doTableAttributesExist(AuftragsPositionDO.class, "periodOfPerformanceBegin", "periodOfPerformanceEnd") == true) {
          return UpdatePreCheckStatus.ALREADY_UPDATED;
        }
        return UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doTableAttributesExist(ScriptDO.class, "file", "filename") == false) {
          dao.addTableAttributes(ScriptDO.class, "file", "filename");
          final Table scriptTable = new Table(ScriptDO.class);
          dao.renameTableAttribute(scriptTable.getName(), "script", "old_script");
          dao.renameTableAttribute(scriptTable.getName(), "scriptbackup", "old_script_backup");
          dao.addTableAttributes(ScriptDO.class, "script", "scriptBackup");
          final List<DatabaseResultRow> rows = dao.query("select pk, old_script, old_script_backup from t_script");
          if (rows != null) {
            for (final DatabaseResultRow row : rows) {
              final Integer pk = (Integer) row.getEntry("pk").getValue();
              final String oldScript = (String) row.getEntry("old_script").getValue();
              final String oldScriptBackup = (String) row.getEntry("old_script_backup").getValue();
              final ScriptDO script = new ScriptDO();
              script.setScriptAsString(oldScript);
              script.setScriptBackupAsString(oldScriptBackup);
              dao.update("update t_script set script=?, script_backup=? where pk=?", script.getScript(), script.getScriptBackup(), pk);
            }
          }
        }
        if (dao.doTableAttributesExist(AuftragsPositionDO.class, "periodOfPerformanceBegin", "periodOfPerformanceEnd") == false) {
          dao.addTableAttributes(AuftragsPositionDO.class, "periodOfPerformanceBegin", "periodOfPerformanceEnd");
        }
        return UpdateRunningStatus.DONE;
      }
    });

    // /////////////////////////////////////////////////////////////////
    // 5.0
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(CORE_REGION_ID, VERSION_5_0, "2013-02-15",
        "Adds t_fibu_rechnung.konto, t_pf_user.ssh_public_key, fixes contract.IN_PROGRES -> contract.IN_PROGRESS") {
      final Table rechnungTable = new Table(RechnungDO.class);

      final Table userTable = new Table(PFUserDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        int entriesToMigrate = 0;
        if (dao.isVersionUpdated(CORE_REGION_ID, VERSION_5_0) == false) {
          entriesToMigrate = dao.queryForInt("select count(*) from t_contract where status='IN_PROGRES'");
        }
        return dao.doTableAttributesExist(rechnungTable, "konto") == true //
            && dao.doTableAttributesExist(userTable, "sshPublicKey") //
            && entriesToMigrate == 0 //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doTableAttributesExist(rechnungTable, "konto") == false) {
          dao.addTableAttributes(rechnungTable, new TableAttribute(RechnungDO.class, "konto"));
        }
        if (dao.doTableAttributesExist(userTable, "sshPublicKey") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "sshPublicKey"));
        }
        final int entriesToMigrate = dao.queryForInt("select count(*) from t_contract where status='IN_PROGRES'");
        if (entriesToMigrate > 0) {
          dao.execute("update t_contract set status='IN_PROGRESS' where status='IN_PROGRES'", true);
        }
        return UpdateRunningStatus.DONE;
      }
    });

    // /////////////////////////////////////////////////////////////////
    // 4.3.1
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(CORE_REGION_ID, "4.3.1", "2013-01-29", "Adds t_fibu_projekt.konto") {
      final Table projektTable = new Table(ProjektDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        return dao.doTableAttributesExist(projektTable, "konto") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED
                : UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doTableAttributesExist(projektTable, "konto") == false) {
          dao.addTableAttributes(projektTable, new TableAttribute(ProjektDO.class, "konto"));
        }
        return UpdateRunningStatus.DONE;
      }
    });

    // /////////////////////////////////////////////////////////////////
    // 4.2
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(
        CORE_REGION_ID,
        "4.2",
        "2012-08-09",
        "Adds t_pf_user.authenticationToken|local_user|restricted_user|deactivated|ldap_values, t_group.local_group, t_fibu_rechnung|eingangsrechnung|auftrag(=incoming and outgoing invoice|order).ui_status_as_xml") {
      final Table userTable = new Table(PFUserDO.class);

      final Table groupTable = new Table(GroupDO.class);

      final Table outgoingInvoiceTable = new Table(RechnungDO.class);

      final Table incomingInvoiceTable = new Table(EingangsrechnungDO.class);

      final Table orderTable = new Table(AuftragDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        return dao.doTableAttributesExist(userTable, "authenticationToken", "localUser", "restrictedUser", "deactivated", "ldapValues") == true //
            && dao.doTableAttributesExist(groupTable, "localGroup") == true // , "nestedGroupsAllowed", "nestedGroupIds") == true //
            && dao.doTableAttributesExist(outgoingInvoiceTable, "uiStatusAsXml") == true //
            && dao.doTableAttributesExist(incomingInvoiceTable, "uiStatusAsXml") == true //
            && dao.doTableAttributesExist(orderTable, "uiStatusAsXml") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doTableAttributesExist(userTable, "authenticationToken") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "authenticationToken"));
        }
        if (dao.doTableAttributesExist(userTable, "localUser") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "localUser").setDefaultValue("false"));
        }
        if (dao.doTableAttributesExist(userTable, "restrictedUser") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "restrictedUser").setDefaultValue("false"));
        }
        if (dao.doTableAttributesExist(userTable, "deactivated") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "deactivated").setDefaultValue("false"));
        }
        if (dao.doTableAttributesExist(userTable, "ldapValues") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "ldapValues"));
        }
        if (dao.doTableAttributesExist(groupTable, "localGroup") == false) {
          dao.addTableAttributes(groupTable, new TableAttribute(GroupDO.class, "localGroup").setDefaultValue("false"));
        }
        // if (dao.doesTableAttributesExist(groupTable, "nestedGroupsAllowed") == false) {
        // dao.addTableAttributes(groupTable, new TableAttribute(GroupDO.class, "nestedGroupsAllowed").setDefaultValue("true"));
        // }
        // if (dao.doesTableAttributesExist(groupTable, "nestedGroupIds") == false) {
        // dao.addTableAttributes(groupTable, new TableAttribute(GroupDO.class, "nestedGroupIds"));
        // }
        if (dao.doTableAttributesExist(outgoingInvoiceTable, "uiStatusAsXml") == false) {
          dao.addTableAttributes(outgoingInvoiceTable, new TableAttribute(RechnungDO.class, "uiStatusAsXml"));
        }
        if (dao.doTableAttributesExist(incomingInvoiceTable, "uiStatusAsXml") == false) {
          dao.addTableAttributes(incomingInvoiceTable, new TableAttribute(EingangsrechnungDO.class, "uiStatusAsXml"));
        }
        if (dao.doTableAttributesExist(orderTable, "uiStatusAsXml") == false) {
          dao.addTableAttributes(orderTable, new TableAttribute(AuftragDO.class, "uiStatusAsXml"));
        }
        return UpdateRunningStatus.DONE;
      }
    });

    // /////////////////////////////////////////////////////////////////
    // 4.1
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(CORE_REGION_ID, "4.1", "2012-04-21", "Adds t_pf_user.first_day_of_week and t_pf_user.hr_planning.") {
      final Table userTable = new Table(PFUserDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        return dao.doTableAttributesExist(userTable, "firstDayOfWeek", "hrPlanning") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED
                : UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doTableAttributesExist(userTable, "firstDayOfWeek") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "firstDayOfWeek"));
        }
        if (dao.doTableAttributesExist(userTable, "hrPlanning") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "hrPlanning").setDefaultValue("true"));
        }
        return UpdateRunningStatus.DONE;
      }
    });

    // /////////////////////////////////////////////////////////////////
    // 4.0
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(CORE_REGION_ID, "4.0", "2012-04-18",
        "Adds 6th parameter to t_script and payment_type to t_fibu_eingangsrechnung.") {
      final Table scriptTable = new Table(ScriptDO.class);

      final Table eingangsrechnungTable = new Table(EingangsrechnungDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        return dao.doTableAttributesExist(scriptTable, "parameter6Name", "parameter6Type") == true //
            && dao.doTableAttributesExist(eingangsrechnungTable, "paymentType") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doTableAttributesExist(scriptTable, "parameter6Name") == false) {
          dao.addTableAttributes(scriptTable, new TableAttribute(ScriptDO.class, "parameter6Name"));
        }
        if (dao.doTableAttributesExist(scriptTable, "parameter6Type") == false) {
          dao.addTableAttributes(scriptTable, new TableAttribute(ScriptDO.class, "parameter6Type"));
        }
        if (dao.doTableAttributesExist(eingangsrechnungTable, "paymentType") == false) {
          dao.addTableAttributes(eingangsrechnungTable, new TableAttribute(EingangsrechnungDO.class, "paymentType"));
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
        final Table kundeTable = new Table(KundeDO.class);
        final Table eingangsrechnungTable = new Table(EingangsrechnungDO.class);
        final Table kontoTable = new Table(KontoDO.class);
        final Table taskTable = new Table(TaskDO.class);
        final Table addressTable = new Table(AddressDO.class);
        return dao.doTableAttributesExist(kundeTable, "konto") == true //
            && dao.doTableAttributesExist(eingangsrechnungTable, "konto") == true //
            && dao.doTableAttributesExist(kontoTable, "status") == true //
            && dao.doTableAttributesExist(addressTable, "communicationLanguage") == true //
            && dao.doTableAttributesExist(taskTable, "protectionOfPrivacy") //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final Table kundeTable = new Table(KundeDO.class);
        if (dao.doTableAttributesExist(kundeTable, "konto") == false) {
          dao.addTableAttributes(kundeTable, new TableAttribute(KundeDO.class, "konto"));
        }
        final Table eingangsrechnungTable = new Table(EingangsrechnungDO.class);
        if (dao.doTableAttributesExist(eingangsrechnungTable, "konto") == false) {
          dao.addTableAttributes(eingangsrechnungTable, new TableAttribute(EingangsrechnungDO.class, "konto"));
        }
        final Table kontoTable = new Table(KontoDO.class);
        if (dao.doTableAttributesExist(kontoTable, "status") == false) {
          dao.addTableAttributes(kontoTable, new TableAttribute(KontoDO.class, "status"));
        }
        final Table taskTable = new Table(TaskDO.class);
        if (dao.doTableAttributesExist(taskTable, "protectionOfPrivacy") == false) {
          dao.addTableAttributes(taskTable, new TableAttribute(TaskDO.class, "protectionOfPrivacy").setDefaultValue("false"));
        }
        final Table addressTable = new Table(AddressDO.class);
        if (dao.doTableAttributesExist(addressTable, "communicationLanguage") == false) {
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
        final Table dbUpdateTable = new Table(DatabaseUpdateDO.class);
        final Table userTable = new Table(PFUserDO.class);
        return dao.doExist(dbUpdateTable) == true
            && dao.doTableAttributesExist(userTable, "dateFormat", "excelDateFormat", "timeNotation") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final Table dbUpdateTable = new Table(DatabaseUpdateDO.class);
        final Table userTable = new Table(PFUserDO.class);
        dbUpdateTable.addAttributes("updateDate", "regionId", "versionString", "executionResult", "executedBy", "description");
        dao.createTable(dbUpdateTable);
        dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "dateFormat"));
        dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "excelDateFormat"));
        dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "timeNotation"));
        final UserDao userDao = Registry.instance().getDao(UserDao.class);
        dao.createMissingIndices();
        userDao.getUserGroupCache().setExpired();
        return UpdateRunningStatus.DONE;
      }
    });
    return list;
  }
}
