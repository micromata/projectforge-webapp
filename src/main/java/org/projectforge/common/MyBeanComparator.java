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

package org.projectforge.common;

import java.util.Comparator;

import org.apache.commons.lang.ClassUtils;
import org.projectforge.core.StringComparator;

public class MyBeanComparator<T> implements Comparator<T>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyBeanComparator.class);

  private String property, secondProperty;

  private boolean ascending, secondAscending;

  public MyBeanComparator(final String property)
  {
    this(property, true);

  }

  public MyBeanComparator(final String property, final boolean asc)
  {
    this.property = property;
    this.ascending = asc;
  }

  public MyBeanComparator(final String property, final boolean ascending, final String secondProperty, final boolean secondAscending)
  {
    this.property = property;
    this.ascending = ascending;
    this.secondProperty = secondProperty;
    this.secondAscending = secondAscending;
  }

  public int compare(final T o1, final T o2)
  {
    final int result = compare(o1, o2, property, ascending);
    if (result != 0) {
      return result;
    }
    return compare(o1, o2, secondProperty, secondAscending);
  }

  @SuppressWarnings({ "unchecked", "rawtypes"})
  private int compare(final T o1, final T o2, final String prop, final boolean asc)
  {
    if (prop == null) {
      // Not comparable.
      return 0;
    }
    try {
      final Object value1 = BeanHelper.getNestedProperty(o1, prop);
      final Object value2 = BeanHelper.getNestedProperty(o2, prop);
      if (value1 == null) {
        if (value2 == null)
          return 0;
        else return (asc == true) ? -1 : 1;
      }
      if (value2 == null) {
        return (asc == true) ? 1 : -1;
      }
      if (value1 instanceof String && value2 instanceof String) {
        return StringComparator.getInstance().compare((String)value1, (String)value2, asc);
      }
      if (ClassUtils.isAssignable(value2.getClass(), value1.getClass()) == true) {
        if (asc == true) {
          return ((Comparable) value1).compareTo(value2);
        } else {
          return -((Comparable) value1).compareTo(value2);
        }
      } else {
        final String sval1 = String.valueOf(value1);
        final String sval2 = String.valueOf(value2);
        if (asc == true) {
          return sval1.compareTo(sval2);
        } else {
          return -sval1.compareTo(sval2);
        }
      }
    } catch (final Exception ex) {
      log.error("Exception while comparing values of property '" + prop + "': " + ex.getMessage());
      return 0;
    }
  }
}
