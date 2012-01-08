/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.wicket;

import org.apache.commons.lang.Validate;
import org.apache.wicket.model.LoadableDetachableModel;
import org.projectforge.core.BaseDao;
import org.projectforge.core.ExtendedBaseDO;

public class DetachableDOModel<D extends ExtendedBaseDO< ? extends Integer>, B extends BaseDao<D>> extends LoadableDetachableModel<D>
{
  private static final long serialVersionUID = 2153617089037166695L;

  protected final Integer id;

  private B baseDao;

  public DetachableDOModel(D object, B baseDao)
  {
    super(object);
    Validate.notNull(object);
    Validate.notNull(object.getId());
    this.id = object.getId();
    this.baseDao = baseDao;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return Long.valueOf(id).hashCode();
  }

  /**
   * used for data view with ReuseIfModelsEqualStrategy item reuse strategy
   * 
   * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (obj == this) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (obj instanceof DetachableDOModel< ? , ? >) {
      DetachableDOModel< ? , ? > other = (DetachableDOModel< ? , ? >) obj;
      return other.id == id;
    }
    return false;
  }

  /**
   * @see org.apache.wicket.model.LoadableDetachableModel#load()
   */
  @Override
  protected D load()
  {
    // loads data object from the database
    return baseDao.getById(id);
  }
}
