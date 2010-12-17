/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.projectforge.core.BaseDao;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.registry.Registry;
import org.projectforge.web.access.AccessListPage;
import org.projectforge.web.address.AddressListPage;
import org.projectforge.web.book.BookListPage;
import org.projectforge.web.fibu.EingangsrechnungListPage;
import org.projectforge.web.fibu.KontoListPage;
import org.projectforge.web.fibu.Kost1ListPage;
import org.projectforge.web.fibu.Kost2ArtListPage;
import org.projectforge.web.fibu.Kost2ListPage;
import org.projectforge.web.fibu.ProjektListPage;
import org.projectforge.web.fibu.RechnungListPage;
import org.projectforge.web.task.TaskListPage;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.user.GroupListPage;
import org.projectforge.web.user.UserListPage;

/**
 * Registry for dao's. Here you can register additional daos and plugins (extensions of ProjectForge).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class WebRegistry
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WebRegistry.class);

  private static final WebRegistry instance = new WebRegistry();

  private Map<String, WebRegistryEntry> map = new HashMap<String, WebRegistryEntry>();

  private List<WebRegistryEntry> orderedList = new ArrayList<WebRegistryEntry>();

  public static WebRegistry instance()
  {
    return instance;
  }

  /**
   * Creates a new WebRegistryEntry and registers it. Id must be found in {@link Registry}.
   * @param id
   */
  public WebRegistryEntry register(final String id)
  {
    return register(new WebRegistryEntry(id));
  }

  public WebRegistryEntry register(final WebRegistryEntry entry)
  {
    Validate.notNull(entry);
    map.put(entry.getId(), entry);
    orderedList.add(entry);
    return entry;
  }

  /**
   * Creates a new WebRegistryEntry and registers it. Id must be found in {@link Registry}.
   * @param id
   */
  public WebRegistryEntry register(final String id, final boolean insertBefore, final WebRegistryEntry entry)
  {
    return register(new WebRegistryEntry(id), insertBefore, entry);
  }

  public WebRegistryEntry register(final WebRegistryEntry existingEntry, final boolean insertBefore, final WebRegistryEntry entry)
  {
    Validate.notNull(existingEntry);
    Validate.notNull(entry);
    map.put(entry.getId(), entry);
    int idx = orderedList.indexOf(existingEntry);
    if (idx < 0) {
      log.error("Registry entry '" + existingEntry.getId() + "' not found. Appending the given entry to the list.");
      orderedList.add(entry);
    } else if (insertBefore == true) {
      orderedList.add(idx, entry);
    } else {
      orderedList.add(idx + 1, entry);
    }
    return entry;
  }

  public WebRegistryEntry getEntry(final String id)
  {
    return map.get(id);
  }

  public List<WebRegistryEntry> getOrderedList()
  {
    return orderedList;
  }

  public BaseDao< ? > getDao(final String id)
  {
    final WebRegistryEntry entry = getEntry(id);
    return entry != null ? entry.getDao() : null;
  }

  private WebRegistry()
  {
    register(DaoRegistry.ADDRESS).setListPageColumnsCreatorClass(AddressListPage.class);
    register(DaoRegistry.TIMESHEET).setListPageColumnsCreatorClass(TimesheetListPage.class);
    register(DaoRegistry.TASK).setListPageColumnsCreatorClass(TaskListPage.class);
    register(DaoRegistry.BOOK).setListPageColumnsCreatorClass(BookListPage.class);
    register(DaoRegistry.RECHNUNG).setListPageColumnsCreatorClass(RechnungListPage.class);
    register(DaoRegistry.EINGANGSRECHNUNG).setListPageColumnsCreatorClass(
        EingangsrechnungListPage.class);
    register(DaoRegistry.USER).setListPageColumnsCreatorClass(UserListPage.class);
    register(DaoRegistry.GROUP).setListPageColumnsCreatorClass(GroupListPage.class);
    register(DaoRegistry.ACCESS).setListPageColumnsCreatorClass(AccessListPage.class);
    register(DaoRegistry.BUCHUNGSSATZ);// TODO: .setListPageColumnsCreatorClass(
    // BuchungssatzListPage.class);
    register(DaoRegistry.KOST1).setListPageColumnsCreatorClass(Kost1ListPage.class);
    register(DaoRegistry.KOST2).setListPageColumnsCreatorClass(Kost2ListPage.class);
    register(DaoRegistry.KOST2_ART).setListPageColumnsCreatorClass(Kost2ArtListPage.class);
    register(DaoRegistry.KONTO).setListPageColumnsCreatorClass(KontoListPage.class);
    register(DaoRegistry.KUNDE);// TODO: .setListPageColumnsCreatorClass(KundeListPage.class);
    register(DaoRegistry.PROJEKT).setListPageColumnsCreatorClass(ProjektListPage.class);
    // register(DaoRegistry.ORDERBOOK).setListPageColumnsCreatorClass(AuftragListPage.class);

  }
}
