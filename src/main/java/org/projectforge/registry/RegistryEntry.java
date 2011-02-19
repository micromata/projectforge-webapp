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

import org.projectforge.core.BaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.ScriptingDao;

/**
 * For registering a dao object and its scripting dao (optional).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class RegistryEntry
{
  private String id;

  private String i18nPrefix;

  private BaseDao< ? > dao;

  private ScriptingDao< ? > scriptingDao;

  private Class< ? extends BaseSearchFilter> searchFilterClass;

  private Class< ? extends BaseDao< ? >> daoClassType;

  /**
   * @param id
   * @param daoClassType Needed because dao is a proxy or whatever object.
   * @param dao
   */
  public RegistryEntry(final String id, final Class< ? extends BaseDao< ? >> daoClassType, final BaseDao< ? > dao)
  {
    this(id, daoClassType, dao, null);
  }

  /**
   * @param id
   * @param daoClassType Needed because dao is a proxy or whatever object.
   * @param dao
   * @param i18nPrefix
   */
  public RegistryEntry(final String id, final Class< ? extends BaseDao< ? >> daoClassType, final BaseDao< ? > dao, final String i18nPrefix)
  {
    this.id = id;
    this.daoClassType = daoClassType;
    this.dao = dao;
    this.i18nPrefix = (i18nPrefix != null) ? i18nPrefix : id;
  }

  public RegistryEntry setSearchFilterClass(final Class< ? extends BaseSearchFilter> searchFilterClass)
  {
    this.searchFilterClass = searchFilterClass;
    return this;
  }

  /**
   * Register an own ScriptingDao. If this method isn't call than the generic ScriptingDao is used.
   * @param scriptingDao
   * @return this for chaining.
   */
  public RegistryEntry setScriptingDao(final ScriptingDao< ? > scriptingDao)
  {
    this.scriptingDao = scriptingDao;
    return this;
  }

  @SuppressWarnings("unchecked")
  public ScriptingDao< ? > getScriptingDao()
  {
    if (this.scriptingDao == null) {
      this.scriptingDao = new ScriptingDao(this.dao);
    }
    return scriptingDao;
  }

  /**
   * @return The dao specific filter or null if not registered.
   */
  public final Class< ? extends BaseSearchFilter> getSearchFilterClass()
  {
    return this.searchFilterClass;
  }

  public String getId()
  {
    return id;
  }

  public Class< ? extends BaseDO< ? >> getDOClass()
  {
    return dao.getDOClass();
  }

  public BaseDao< ? > getDao()
  {
    return dao;
  }

  public Class< ? extends BaseDao< ? >> getDaoClassType()
  {
    return daoClassType;
  }

  /**
   * Is used e. g. by {@link org.projectforge.web.core.SearchForm}: &lt;i18nPrefix&gt;.title.heading.
   * @return The prefix of the i18n keys to prepend, e. g. "fibu.kost1". If not especially, than the id will be used as prefix.
   */
  public String getI18nPrefix()
  {
    return i18nPrefix;
  }

  public String getI18nTitleHeading()
  {
    return i18nPrefix + ".title.heading";
  }
}
