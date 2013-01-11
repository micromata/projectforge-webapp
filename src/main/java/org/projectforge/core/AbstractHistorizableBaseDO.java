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

package org.projectforge.core;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.projectforge.common.BeanHelper;

import de.micromata.hibernate.history.ExtendedHistorizable;

/**
 * Declares lastUpdate and created as invalidHistorizableProperties.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@MappedSuperclass
public abstract class AbstractHistorizableBaseDO<I extends Serializable> extends AbstractBaseDO<I> implements ExtendedHistorizable
{
  private static final long serialVersionUID = -5980671510045450615L;

  private static final Map<Class< ? >, Set<String>> nonHistorizableProperties = new HashMap<Class< ? >, Set<String>>();

  protected static void putNonHistorizableProperty(final Class< ? > cls, final String... properties)
  {
    final Field[] fields = BeanHelper.getAllDeclaredFields(cls);
    AccessibleObject.setAccessible(fields, true);
    for (final String property : properties) {
      boolean found = false;
      for (final Field field : fields) {
        if (property.equals(field.getName()) == true) {
          found = true;
          if (getNonHistorizableAttributes(cls).contains(property) == true) {
            throw new IllegalArgumentException("Property '" + property + "' was already added to class '" + cls.getName() + "'. May-be a wrong class-name is used?");
          }
          getNonHistorizableAttributes(cls).add(property);
          break;
        }
      }
      if (found == false) {
        throw new IllegalArgumentException("Property '" + property + "' not found in class '" + cls.getName() + "'.");
      }
    }
  }

  @Transient
  public Set<String> getHistorizableAttributes()
  {
    return null;
  }

  @Transient
  public Set<String> getNonHistorizableAttributes()
  {
    return getNonHistorizableAttributes(this.getClass());
  }

  public boolean isNonHistorizableAttribute(final String property)
  {
    return getNonHistorizableAttributes().contains(property);
  }

  private static Set<String> getNonHistorizableAttributes(final Class< ? > cls)
  {
    Set<String> result = nonHistorizableProperties.get(cls);
    if (result == null) {
      result = new HashSet<String>();
      result.add("lastUpdate");
      result.add("created");
      nonHistorizableProperties.put(cls, result);
    }
    return result;
  }
}
