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

import java.util.Comparator;

import org.apache.commons.lang.ClassUtils;

public class MyBeanComparator<T> implements Comparator<T>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyBeanComparator.class);

  private String property;

  private boolean asc;

  public MyBeanComparator(final String property)
  {
    this(property, true);

  }

  public MyBeanComparator(final String property, final boolean asc)
  {
    this.property = property;
    this.asc = asc;
  }

  @SuppressWarnings({ "unchecked", "rawtypes"})
  public int compare(final T o1, final T o2)
  {
    try {
      final Object value1 = BeanHelper.getNestedProperty(o1, property);
      final Object value2 = BeanHelper.getNestedProperty(o2, property);
      if (value1 == null) {
        if (value2 == null)
          return 0;
        else return (asc == true) ? -1 : 1;
      }
      if (value2 == null) {
        return (asc == true) ? 1 : -1;
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
      log.error("Exception while comparing values of property '" + property + "': " + ex.getMessage());
      return 0;
    }
  }
}
