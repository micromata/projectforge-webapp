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

package org.projectforge.registry;

import org.projectforge.core.BaseDao;
import org.projectforge.web.core.SearchForm;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class RegistryEntry
{
  private String id;

  private String i18nPrefix;

  private BaseDao< ? > dao;

  public RegistryEntry(final String id, final BaseDao< ? > dao)
  {
    this(id, dao, null);
  }

  public RegistryEntry(final String id, final BaseDao< ? > dao, final String i18nPrefix)
  {
    this.id = id;
    this.dao = dao;
    this.i18nPrefix = (i18nPrefix != null) ? i18nPrefix : id;
  }

  public String getId()
  {
    return id;
  }

  public BaseDao< ? > getDao()
  {
    return dao;
  }

  /**
   * Is used e. g. by {@link SearchForm}: &lt;i18nPrefix&gt;.title.heading.
   * @return The prefix of the i18n keys to prepend, e. g. "fibu.kost1". If not especially, than the id will be used as prefix.
   */
  public String getI18nPrefix()
  {
    return i18nPrefix;
  }
}
