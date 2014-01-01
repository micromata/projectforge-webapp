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

package org.projectforge.core;

import java.io.Serializable;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public interface BaseDO<I extends Serializable> extends IdObject<I>
{
  public I getId();

  public void setId(I id);

  /**
   * Can be used for marking changes in a data object as minor changes. This means for example, that after minor changes all dependent
   * objects will not be re-indexed.
   * @return
   */
  public boolean isMinorChange();

  /**
   * @see #isMinorChanges()
   */
  public void setMinorChange(boolean value);

  /**
   * Free use-able multi purpose attributes.
   * @param key
   * @return
   */
  public Object getAttribute(String key);

  public void setAttribute(String key, Object value);

  /**
   * Copies all values from the given src object excluding the values created and lastUpdate. Do not overwrite created and lastUpdate from
   * the original database object. Null values will be excluded therefore for such null properties the original properties will be
   * preserved. If you want to delete such properties, please overwrite them manually.<br/>
   * This method is required by BaseDao for example for updating DOs.
   * @param src
   * @return true, if any modifications are detected, otherwise false;
   */
  public ModificationStatus copyValuesFrom(BaseDO< ? extends Serializable> src, String... ignoreFields);
}
