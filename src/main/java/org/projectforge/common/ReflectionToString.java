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

package org.projectforge.common;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.TimeZone;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.projectforge.core.BaseDO;
import org.projectforge.database.HibernateUtils;

/**
 * 
 * @author wolle (wolle@micromata.de)
 * 
 */
public class ReflectionToString extends ReflectionToStringBuilder
{
  public ReflectionToString(Object arg0)
  {
    super(Hibernate.isInitialized(arg0) ? arg0 : "Lazy" + arg0.getClass() + "@" + System.identityHashCode(arg0));
  }

  @Override
  public ToStringBuilder append(final String fieldName, final Object object)
  {
    if (object != null) {
      if (Hibernate.isInitialized(object) == false) {
        if (BaseDO.class.isAssignableFrom(object.getClass()) == true) {
          // Work around for Jassist bug:
          final Serializable id = HibernateUtils.getIdentifier((BaseDO< ? >) object);
          return super.append(fieldName, id != null ? id : "<id>");
        }
        return super.append(fieldName, "LazyCollection");
      } else if (object instanceof TimeZone) {
        return super.append(fieldName, ((TimeZone)object).getID());
      }
    }
    return super.append(fieldName, object);
  }

  @Override
  protected boolean accept(Field field)
  {
    try {
      Object value = getValue(field);
      if (Hibernate.isInitialized(value) == false) {
        append(field.getName(), value);
        return false;
      }
    } catch (IllegalArgumentException ex) {
      return false;
    } catch (IllegalAccessException ex) {
      return false;
    }
    return super.accept(field);
  }

  public static String asString(Object o)
  {
    return new ReflectionToString(o).toString();
  }
}
