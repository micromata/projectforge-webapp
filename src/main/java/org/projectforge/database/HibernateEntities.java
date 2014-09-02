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
import java.util.Collections;
import java.util.List;

/**
 * Defines all core entities. The plugin entities are organized by the plugins itself.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class HibernateEntities
{
  // The order of the entities is important for xml dump and imports as well as for test cases (order for deleting objects at the end of
  // each test).
  // The entities are inserted in ascending order and deleted in descending order.
  static final Class< ? >[] CORE_ENTITIES = { //
    org.projectforge.user.PFUserDO.class, //
    org.projectforge.user.UserRightDO.class, //
    org.projectforge.user.GroupDO.class, //
    org.projectforge.task.TaskDO.class, //

    org.projectforge.access.GroupTaskAccessDO.class, //
    org.projectforge.access.AccessEntryDO.class, //

    org.projectforge.fibu.KontoDO.class, //
    org.projectforge.fibu.KundeDO.class, //
    org.projectforge.fibu.ProjektDO.class, //

    org.projectforge.fibu.AuftragDO.class, //
    org.projectforge.fibu.AuftragsPositionDO.class, //

    org.projectforge.fibu.kost.Kost1DO.class, //
    org.projectforge.fibu.kost.Kost2ArtDO.class, //
    org.projectforge.fibu.kost.Kost2DO.class, //

    org.projectforge.fibu.EmployeeDO.class, //
    org.projectforge.fibu.EmployeeSalaryDO.class, //

    org.projectforge.fibu.EingangsrechnungDO.class, //
    org.projectforge.fibu.EingangsrechnungsPositionDO.class, //

    org.projectforge.fibu.RechnungDO.class, //
    org.projectforge.fibu.RechnungsPositionDO.class, //

    org.projectforge.fibu.kost.KostZuweisungDO.class, //
    org.projectforge.fibu.kost.BuchungssatzDO.class, //
    org.projectforge.fibu.PaymentScheduleDO.class, //
    org.projectforge.address.AddressDO.class, //
    org.projectforge.address.PersonalAddressDO.class, //
    org.projectforge.address.contact.ContactDO.class, //
    org.projectforge.address.contact.ContactEntryDO.class, //
    org.projectforge.address.contact.PersonalContactDO.class, //
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
    org.projectforge.timesheet.TimesheetDO.class, //
    org.projectforge.user.UserPrefDO.class, //
    org.projectforge.user.UserPrefEntryDO.class, //
    org.projectforge.user.UserXmlPreferencesDO.class};

  static final Class< ? >[] HISTORY_ENTITIES = { //
    de.micromata.hibernate.history.HistoryEntry.class, //
    de.micromata.hibernate.history.delta.PropertyDelta.class, //
    de.micromata.hibernate.history.delta.SimplePropertyDelta.class, //
    de.micromata.hibernate.history.delta.CollectionPropertyDelta.class, //
    de.micromata.hibernate.history.delta.AssociationPropertyDelta.class};

  private static HibernateEntities instance = new HibernateEntities();

  private final List<Class< ? >> coreEntities = new ArrayList<Class< ? >>();

  private final List<Class< ? >> historyEntities = new ArrayList<Class< ? >>();

  private List<Class< ? >> unmodifiableList, unmodifiableListDesc;

  private final List<Class< ? >> unmodifiableHistoryEntitiesList, unmodifiableHistoryEntitiesListDesc;

  void addEntity(final Class< ? > cls)
  {
    coreEntities.add(cls);
    unmodifiableList = unmodifiableListDesc = null; // Force to re-create the unmodifiable list.
  }

  public static HibernateEntities instance()
  {
    return instance;
  }

  /**
   * The order of the entities is the order to insert entities in a data-base, so if an entity A has a reference to another entity B, B
   * should be in the list before A.
   * @return The (unmodifiable) list of all hibernate entities (including the plugin's entities).
   */
  public List<Class< ? >> getOrderedEntities()
  {
    if (unmodifiableList == null) {
      unmodifiableList = Collections.unmodifiableList(coreEntities);
    }
    return unmodifiableList;
  }

  /**
   * The order of the entities is the order to delete entities from a data-base, so if an entity A has a reference to another entity B, A
   * should be in the list before B.
   * @return The (unmodifiable) list of all hibernate entities (including the plugin's entities) in reverse order.
   */
  public List<Class< ? >> getDescOrderedEntities()
  {
    if (unmodifiableListDesc == null) {
      final List<Class< ? >> list = new ArrayList<Class< ? >>();
      for (final Class< ? > cls : coreEntities) {
        list.add(0, cls);
      }
      unmodifiableListDesc = Collections.unmodifiableList(list);
    }
    return unmodifiableListDesc;
  }

  public List<Class< ? >> getOrderedHistoryEntities()
  {
    return unmodifiableHistoryEntitiesList;
  }

  public List<Class< ? >> getDescOrderedHistoryEntities()
  {
    return unmodifiableHistoryEntitiesListDesc;
  }

  private HibernateEntities()
  {
    for (final Class< ? > cls : CORE_ENTITIES) {
      coreEntities.add(cls);
    }
    for (final Class< ? > cls : HISTORY_ENTITIES) {
      historyEntities.add(cls);
    }
    unmodifiableHistoryEntitiesList = Collections.unmodifiableList(historyEntities);
    final List<Class< ? >> list = new ArrayList<Class< ? >>();
    for (final Class< ? > cls : historyEntities) {
      list.add(0, cls);
    }
    unmodifiableHistoryEntitiesListDesc = Collections.unmodifiableList(list);
  }
}
