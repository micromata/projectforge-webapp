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

package org.projectforge.database;

import org.projectforge.access.AccessEntryDO;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.address.AddressDO;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.book.BookDO;
import org.projectforge.common.DatabaseDialect;
import org.projectforge.continuousdb.DatabaseUpdateDao;
import org.projectforge.continuousdb.SchemaGenerator;
import org.projectforge.continuousdb.Table;
import org.projectforge.continuousdb.TableAttribute;
import org.projectforge.continuousdb.TableAttributeType;
import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.continuousdb.UpdateEntryImpl;
import org.projectforge.continuousdb.UpdatePreCheckStatus;
import org.projectforge.continuousdb.UpdateRunningStatus;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.EingangsrechnungsPositionDO;
import org.projectforge.fibu.EmployeeDO;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.KostZuweisungDO;
import org.projectforge.gantt.GanttChartDO;
import org.projectforge.humanresources.HRPlanningDO;
import org.projectforge.humanresources.HRPlanningEntryDO;
import org.projectforge.meb.ImportedMebEntryDO;
import org.projectforge.meb.MebEntryDO;
import org.projectforge.multitenancy.TenantDO;
import org.projectforge.orga.ContractDO;
import org.projectforge.orga.PostausgangDO;
import org.projectforge.orga.PosteingangDO;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.task.TaskDO;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefEntryDO;
import org.projectforge.user.UserRightDO;
import org.projectforge.user.UserXmlPreferencesDO;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class DatabaseCoreInitial
{
  public static final String CORE_REGION_ID = "ProjectForge";

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry(final MyDatabaseUpdater databaseUpdater)
  {
    final DatabaseUpdateDao dao = databaseUpdater.getDatabaseUpdateDao();

    final Class< ? >[] doClasses = new Class< ? >[] { //
        // First needed data-base objects:
        HistoryEntry.class, PropertyDelta.class, //
        TenantDO.class, //
        PFUserDO.class, GroupDO.class, TaskDO.class, GroupTaskAccessDO.class, //
        AccessEntryDO.class, //

        // To create second:
        KontoDO.class, //

        // To create third:
        KundeDO.class, ProjektDO.class, //
        Kost1DO.class, Kost2ArtDO.class, Kost2DO.class, //
        AuftragDO.class, AuftragsPositionDO.class, //
        EingangsrechnungDO.class, EingangsrechnungsPositionDO.class, //
        RechnungDO.class, RechnungsPositionDO.class, //
        EmployeeDO.class, //
        EmployeeSalaryDO.class, //
        KostZuweisungDO.class, //

        // All the rest:
        AddressDO.class, PersonalAddressDO.class, //
        BookDO.class, //
        ConfigurationDO.class, //
        DatabaseUpdateDO.class, //
        BuchungssatzDO.class, //
        ContractDO.class, //
        GanttChartDO.class, //
        HRPlanningDO.class, HRPlanningEntryDO.class, //
        MebEntryDO.class, ImportedMebEntryDO.class, //
        PostausgangDO.class, //
        PosteingangDO.class, //
        ScriptDO.class, //
        TimesheetDO.class, //
        UserPrefDO.class, //
        UserPrefEntryDO.class, //
        UserRightDO.class, //
        UserXmlPreferencesDO.class //
    };

    return new UpdateEntryImpl(CORE_REGION_ID, "2013-04-25", "Adds all core tables T_*.") {

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        // Does the data-base tables already exist?
        if (dao.doEntitiesExist(doClasses) == false) {
          return UpdatePreCheckStatus.READY_FOR_UPDATE;
        }
        return UpdatePreCheckStatus.ALREADY_UPDATED;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doExist(new Table(PFUserDO.class)) == false && HibernateUtils.getDialect() == DatabaseDialect.PostgreSQL) {
          // User table doesn't exist, therefore schema should be empty. PostgreSQL needs sequence for primary keys:
          dao.createSequence("hibernate_sequence", true);
        }
        final SchemaGenerator schemaGenerator = new SchemaGenerator(dao).add(doClasses);
        final Table propertyDeltaTable = schemaGenerator.getTable(PropertyDelta.class);
        final TableAttribute attr = propertyDeltaTable.getAttributeByName("clazz");
        attr.setNullable(false).setType(TableAttributeType.VARCHAR).setLength(31); // Discriminator value is may-be not handled correctly by continuous-db.
        final Table historyEntryTable = schemaGenerator.getTable(HistoryEntry.class);
        final TableAttribute typeAttr = historyEntryTable.getAttributeByName("type");
        typeAttr.setType(TableAttributeType.INT);
        schemaGenerator.createSchema();
        dao.createMissingIndices();

        return UpdateRunningStatus.DONE;
      }
    };
  }
}
