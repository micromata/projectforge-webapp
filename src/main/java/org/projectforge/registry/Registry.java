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

package org.projectforge.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.projectforge.core.BaseDao;
import org.projectforge.task.TaskTree;

/**
 * Registry for dao's. Here you can register additional daos and plugins (extensions of ProjectForge).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class Registry
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Registry.class);

  private static final Registry instance = new Registry();

  private Map<String, RegistryEntry> mapByName = new HashMap<String, RegistryEntry>();

  private Map<Class< ? extends BaseDao< ? >>, RegistryEntry> mapByClass = new HashMap<Class< ? extends BaseDao< ? >>, RegistryEntry>();

  private List<RegistryEntry> orderedList = new ArrayList<RegistryEntry>();

  private TaskTree taskTree;

  public static Registry instance()
  {
    return instance;
  }

  /**
   * Registers the given entry and appends it to the ordered list of registry entries.
   * @param entry The entry to register.
   * @return this for chaining.
   */
  public Registry register(final RegistryEntry entry)
  {
    Validate.notNull(entry);
    mapByName.put(entry.getId(), entry);
    mapByClass.put(entry.getDaoClassType(), entry);
    orderedList.add(entry);
    return this;
  }

  /**
   * Registers the given entry and inserts it to the ordered list of registry entries at the given position.
   * @param existingEntry A previous added entry, at which the new entry should be inserted.
   * @param insertBefore If true then the given entry will be added before the existing entry, otherwise after.
   * @param entry The entry to register.
   * @return this for chaining.
   */
  public Registry register(final RegistryEntry existingEntry, final boolean insertBefore, final RegistryEntry entry)
  {
    Validate.notNull(existingEntry);
    Validate.notNull(entry);
    mapByName.put(entry.getId(), entry);
    mapByClass.put(entry.getDaoClassType(), entry);
    int idx = orderedList.indexOf(existingEntry);
    if (idx < 0) {
      log.error("Registry entry '" + existingEntry.getId() + "' not found. Appending the given entry to the list.");
      orderedList.add(entry);
    } else if (insertBefore == true) {
      orderedList.add(idx, entry);
    } else {
      orderedList.add(idx + 1, entry);
    }
    return this;
  }

  public RegistryEntry getEntry(final String id)
  {
    return mapByName.get(id);
  }

  public RegistryEntry getEntry(final Class< ? extends BaseDao< ? >> daoClass)
  {
    return mapByClass.get(daoClass);
  }

  /**
   * @return The list of entries in the order of their registration: the first registered entry is the first of the returned list etc.
   */
  public List<RegistryEntry> getOrderedList()
  {
    return orderedList;
  }

  public BaseDao< ? > getDao(final String id)
  {
    final RegistryEntry entry = getEntry(id);
    return entry != null ? entry.getDao() : null;
  }

  public BaseDao< ? > getDao(final Class< ? extends BaseDao< ? >> daoClass)
  {
    final RegistryEntry entry = getEntry(daoClass);
    return entry != null ? entry.getDao() : null;
  }

  public void setTaskTree(final TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public TaskTree getTaskTree()
  {
    return taskTree;
  }

  private Registry()
  {
  }
}
