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

import org.apache.commons.lang.Validate;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.projectforge.core.BaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.web.wicket.IListPageColumnsCreator;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class WebRegistryEntry
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WebRegistryEntry.class);

  private RegistryEntry registryEntry;

  private Class< ? extends IListPageColumnsCreator< ? >> listPageColumnsCreatorClass;

  /**
   * Id must be found in {@link Registry}.
   * @param id
   */
  public WebRegistryEntry(final String id)
  {
    Validate.notNull(id);
    registryEntry = Registry.instance().getEntry(id);
    Validate.notNull(registryEntry);
  }

  /**
   * Id must be found in {@link Registry}.
   * @param id
   */
  public WebRegistryEntry(final String id, final Class< ? extends IListPageColumnsCreator< ? >> listPageColumnsCreatorClass)
  {
    Validate.notNull(id);
    registryEntry = Registry.instance().getEntry(id);
    this.listPageColumnsCreatorClass = listPageColumnsCreatorClass;
    if (registryEntry == null) {
      log.error("Object with id '" + id + "' isn't registered in Registry!");
    }
  }

  public WebRegistryEntry(final RegistryEntry registryEntry)
  {
    this.registryEntry = registryEntry;
  }

  public WebRegistryEntry setListPageColumnsCreatorClass(Class< ? extends IListPageColumnsCreator< ? >> listPageColumnsCreatorClass)
  {
    this.listPageColumnsCreatorClass = listPageColumnsCreatorClass;
    return this;
  }

  public Class< ? extends IListPageColumnsCreator< ? >> getListPageColumnsCreatorClass()
  {
    return listPageColumnsCreatorClass;
  }

  /**
   * Creates a proxy via LazyInitProxyFactory. Use-full if needed in Wicket components. Avoids Wicket serialization of the dao.
   * @return
   */
  public BaseDao< ? > getProxyDao()
  {
    return (BaseDao< ? >) LazyInitProxyFactory.createProxy(registryEntry.getDaoClassType(), new DaoLocator(registryEntry.getId()));
  }

  public BaseDao< ? > getDao()
  {
    return registryEntry.getDao();
  }

  public Class< ? extends BaseDao< ? >> getDaoClassType()
  {
    return registryEntry.getDaoClassType();
  }

  public Class< ? extends BaseDO< ? >> getDOClass()
  {
    return registryEntry.getDOClass();
  }

  public String getI18nPrefix()
  {
    return registryEntry.getI18nPrefix();
  }

  public String getI18nTitleHeading()
  {
    return registryEntry.getI18nTitleHeading();
  }

  public String getId()
  {
    return registryEntry.getId();
  }

  public final Class< ? extends BaseSearchFilter> getSearchFilterClass()
  {
    return registryEntry.getSearchFilterClass();
  }
}
