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

package org.projectforge.common;

import java.util.Comparator;

public class MyBeanComparator<T> implements Comparator<T>
{
  private String property;

  private boolean asc;

  public MyBeanComparator(String property)
  {
    this(property, true);

  }

  public MyBeanComparator(String property, boolean asc)
  {
    this.property = property;
    this.asc = asc;
  }

  @SuppressWarnings("unchecked")
  public int compare(T o1, T o2)
  {
    Object value1 = BeanHelper.getNestedProperty(o1, property);
    Object value2 = BeanHelper.getNestedProperty(o2, property);
    if (value1 == null) {
      if (value2 == null)
        return 0;
      else return (asc == true) ? -1 : 1;
    }
    if (value2 == null) {
      return (asc == true) ? 1 : -1;
    }
    if (asc == true)
      return ((Comparable) value1).compareTo(value2);
    else return -((Comparable) value1).compareTo(value2);
  }
}
