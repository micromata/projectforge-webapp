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

package org.projectforge.admin;

import static org.projectforge.admin.SystemUpdater.CORE_REGION_ID;

import java.util.ArrayList;
import java.util.List;

import org.projectforge.address.AddressDO;
import org.projectforge.database.DatabaseUpdateDO;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;
import org.projectforge.database.TableAttribute;
import org.projectforge.fibu.AuftragDO;
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
  @SuppressWarnings("serial")
  public static List<UpdateEntry> getUpdateEntries()
  {
    final List<UpdateEntry> list = new ArrayList<UpdateEntry>();
    // /////////////////////////////////////////////////////////////////
    // 4.3.2
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(CORE_REGION_ID, "4.3.2", "2013-02-15",
        "Adds t_fibu_rechnung.konto, t_pf_user.ssh_public_key, fixes contract.IN_PROGRES -> contract.IN_PROGRESS") {
      final Table rechnungTable = new Table(RechnungDO.class);
      final Table userTable = new Table(PFUserDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        final int entriesToMigrate = dao.queryForInt("select count(*) from t_contract where status='IN_PROGRES'");
        return dao.doesTableAttributesExist(rechnungTable, "konto") == true //
            && dao.doesTableAttributesExist(userTable, "sshPublicKey") //
            && entriesToMigrate == 0 //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        if (dao.doesTableAttributesExist(rechnungTable, "konto") == false) {
          dao.addTableAttributes(rechnungTable, new TableAttribute(RechnungDO.class, "konto"));
        }
        if (dao.doesTableAttributesExist(userTable, "sshPublicKey") == false) {
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
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        return dao.doesTableAttributesExist(projektTable, "konto") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED
                : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        if (dao.doesTableAttributesExist(projektTable, "konto") == false) {
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
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        return dao.doesTableAttributesExist(userTable, "authenticationToken", "localUser", "restrictedUser", "deactivated", "ldapValues") == true //
            && dao.doesTableAttributesExist(groupTable, "localGroup") == true // , "nestedGroupsAllowed", "nestedGroupIds") == true //
            && dao.doesTableAttributesExist(outgoingInvoiceTable, "uiStatusAsXml") == true //
            && dao.doesTableAttributesExist(incomingInvoiceTable, "uiStatusAsXml") == true //
            && dao.doesTableAttributesExist(orderTable, "uiStatusAsXml") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        if (dao.doesTableAttributesExist(userTable, "authenticationToken") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "authenticationToken"));
        }
        if (dao.doesTableAttributesExist(userTable, "localUser") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "localUser").setDefaultValue("false"));
        }
        if (dao.doesTableAttributesExist(userTable, "restrictedUser") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "restrictedUser").setDefaultValue("false"));
        }
        if (dao.doesTableAttributesExist(userTable, "deactivated") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "deactivated").setDefaultValue("false"));
        }
        if (dao.doesTableAttributesExist(userTable, "ldapValues") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "ldapValues"));
        }
        if (dao.doesTableAttributesExist(groupTable, "localGroup") == false) {
          dao.addTableAttributes(groupTable, new TableAttribute(GroupDO.class, "localGroup").setDefaultValue("false"));
        }
        // if (dao.doesTableAttributesExist(groupTable, "nestedGroupsAllowed") == false) {
        // dao.addTableAttributes(groupTable, new TableAttribute(GroupDO.class, "nestedGroupsAllowed").setDefaultValue("true"));
        // }
        // if (dao.doesTableAttributesExist(groupTable, "nestedGroupIds") == false) {
        // dao.addTableAttributes(groupTable, new TableAttribute(GroupDO.class, "nestedGroupIds"));
        // }
        if (dao.doesTableAttributesExist(outgoingInvoiceTable, "uiStatusAsXml") == false) {
          dao.addTableAttributes(outgoingInvoiceTable, new TableAttribute(RechnungDO.class, "uiStatusAsXml"));
        }
        if (dao.doesTableAttributesExist(incomingInvoiceTable, "uiStatusAsXml") == false) {
          dao.addTableAttributes(incomingInvoiceTable, new TableAttribute(EingangsrechnungDO.class, "uiStatusAsXml"));
        }
        if (dao.doesTableAttributesExist(orderTable, "uiStatusAsXml") == false) {
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
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        return dao.doesTableAttributesExist(userTable, "firstDayOfWeek", "hrPlanning") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED
                : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        if (dao.doesTableAttributesExist(userTable, "firstDayOfWeek") == false) {
          dao.addTableAttributes(userTable, new TableAttribute(PFUserDO.class, "firstDayOfWeek"));
        }
        if (dao.doesTableAttributesExist(userTable, "hrPlanning") == false) {
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
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        return dao.doesTableAttributesExist(scriptTable, "parameter6Name", "parameter6Type") == true //
            && dao.doesTableAttributesExist(eingangsrechnungTable, "paymentType") == true //
            ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final DatabaseUpdateDao dao = SystemUpdater.instance().databaseUpdateDao;
        if (dao.doesTableAttributesExist(scriptTable, "parameter6Name") == false) {
          dao.addTableAttributes(scriptTable, new TableAttribute(ScriptDO.class, "parameter6Name"));
        }
        if (dao.doesTableAttributesExist(scriptTable, "parameter6Type") == false) {
          dao.addTableAttributes(scriptTable, new TableAttribute(ScriptDO.class, "parameter6Type"));
        }
        if (dao.doesTableAttributesExist(eingangsrechnungTable, "paymentType") == false) {
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
        final UserDao userDao = Registry.instance().getDao(UserDao.class);
        dao.createMissingIndices();
        userDao.getUserGroupCache().setExpired();
        return UpdateRunningStatus.DONE;
      }
    });
    return list;
  }
}
