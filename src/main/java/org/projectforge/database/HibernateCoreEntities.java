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


/**
 * Defines all core entities. The plugin entities are organized by the plugins itself.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
class HibernateCoreEntities
{
  static final Class< ? >[] CORE_ENTITIES = { //
    de.micromata.hibernate.history.HistoryEntry.class, //
    de.micromata.hibernate.history.delta.PropertyDelta.class, //
    de.micromata.hibernate.history.delta.SimplePropertyDelta.class, //
    de.micromata.hibernate.history.delta.CollectionPropertyDelta.class, //
    de.micromata.hibernate.history.delta.AssociationPropertyDelta.class, //
    de.micromata.hibernate.history.HistoryEntry.class, //
    org.projectforge.fibu.AuftragDO.class, //
    org.projectforge.fibu.AuftragsPositionDO.class, //
    org.projectforge.fibu.EmployeeDO.class, //
    org.projectforge.fibu.EmployeeSalaryDO.class, //
    org.projectforge.fibu.EingangsrechnungDO.class, //
    org.projectforge.fibu.EingangsrechnungsPositionDO.class, //
    org.projectforge.fibu.RechnungDO.class, //
    org.projectforge.fibu.RechnungsPositionDO.class, //
    org.projectforge.fibu.kost.BuchungssatzDO.class, //
    org.projectforge.fibu.kost.Kost1DO.class, //
    org.projectforge.fibu.kost.Kost2DO.class, //
    org.projectforge.fibu.kost.Kost2ArtDO.class, //
    org.projectforge.fibu.kost.KostZuweisungDO.class, //
    org.projectforge.fibu.KontoDO.class, //
    org.projectforge.fibu.KundeDO.class, //
    org.projectforge.fibu.ProjektDO.class, //
    org.projectforge.access.AccessEntryDO.class, //
    org.projectforge.access.GroupTaskAccessDO.class, //
    org.projectforge.address.AddressDO.class, //
    org.projectforge.address.PersonalAddressDO.class, //
    org.projectforge.book.BookDO.class, //
    org.projectforge.core.ConfigurationDO.class, //
    org.projectforge.database.DatabaseUpdateDO.class, //
    org.projectforge.gantt.GanttChartDO.class, //
    org.projectforge.humanresources.HRPlanningDO.class, //
    org.projectforge.humanresources.HRPlanningEntryDO.class, //
    org.projectforge.meb.ImportedMebEntryDO.class, //
    org.projectforge.meb.MebEntryDO.class, //
    org.projectforge.orga.ContractDO.class, //
    org.projectforge.orga.PostausgangDO.class, //
    org.projectforge.orga.PosteingangDO.class, //
    org.projectforge.scripting.ScriptDO.class, //
    org.projectforge.task.TaskDO.class, //
    org.projectforge.timesheet.TimesheetDO.class, //
    org.projectforge.user.GroupDO.class, //
    org.projectforge.user.PFUserDO.class, //
    org.projectforge.user.UserPrefDO.class, //
    org.projectforge.user.UserPrefEntryDO.class, //
    org.projectforge.user.UserRightDO.class, //
    org.projectforge.user.UserXmlPreferencesDO.class};
}
