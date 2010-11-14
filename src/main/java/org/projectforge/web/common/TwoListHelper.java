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

package org.projectforge.web.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.projectforge.common.KeyValueBean;

/**
 * This class is an helper class for supporting the implementation of two gui lists. The user can select entries from one list and move them
 * to the other list. This will be needed e. g. for assigning and unassigning user to one group.<br>
 * The elements stored in the first list will named as assigned values and the elements of the second will be called unassigned values.<br>
 * Finally, after the user has made his decisions (multiple assigning and/or multiple unassigning), this class will return the elements to
 * (un)assign by comparing with the original assigned values.
 */
public class TwoListHelper<K, V extends Comparable<V>> implements Serializable
{
  private static final long serialVersionUID = 3522022033150328877L;

  private List<KeyValueBean<K, V>> assignedItems;

  private List<KeyValueBean<K, V>> unassignedItems;

  private List<KeyValueBean<K, V>> originalAssignedList;

  /**
   * Initializes the lists.
   * @param fullList List of all elements available for (un)assigning.
   * @param assignedKeys List of already assigned elements (by key) or null if no elements assigned.
   */
  public TwoListHelper(final List<KeyValueBean<K, V>> fullList, final List<K> assignedKeys)
  {
    this.assignedItems = new ArrayList<KeyValueBean<K, V>>();
    this.originalAssignedList = new ArrayList<KeyValueBean<K, V>>();
    this.unassignedItems = new ArrayList<KeyValueBean<K, V>>();
    for (KeyValueBean<K, V> entry : fullList) {
      if (assignedKeys != null && assignedKeys.contains(entry.getKey()) == true) {
        this.assignedItems.add(entry);
        this.originalAssignedList.add(entry);
      } else {
        this.unassignedItems.add(entry);
      }
    }
  }

  /** Gets the actual list of assigned values. */
  public List<KeyValueBean<K, V>> getAssignedItems()
  {
    return this.assignedItems;
  }

  public void setAssignedItems(List<KeyValueBean<K, V>> assignedList)
  {
    this.assignedItems = assignedList;
  }

  public List<K> getAssignedValues()
  {
    final List<K> result = new ArrayList<K>();
    for (final KeyValueBean<K, V> entry : this.assignedItems) {
      result.add(entry.getKey());
    }
    return result;
  }

  /** Gets the actual list of unassigned values. */
  public List<KeyValueBean<K, V>> getUnassignedItems()
  {
    return unassignedItems;
  }

  public void setUnassignedItems(List<KeyValueBean<K, V>> unassignedList)
  {
    this.unassignedItems = unassignedList;
  }

  /**
   * Gets the list of values to assign by comparing current assigned list with the original assigned list.
   */
  public List<K> getValuesToAssign()
  {
    List<K> result = new ArrayList<K>();
    for (KeyValueBean<K, V> entry : this.assignedItems) {
      if (this.originalAssignedList.contains(entry) == false) {
        result.add(entry.getKey());
      }
    }
    return result;
  }

  /**
   * Gets the list of values to unassign by comparing current assigned list with the original assigned list.
   */
  public List<K> getValuesToUnassign()
  {
    List<K> result = new ArrayList<K>();
    for (KeyValueBean<K, V> entry : this.originalAssignedList) {
      if (this.assignedItems.contains(entry) == false) {
        result.add(entry.getKey());
      }
    }
    return result;
  }

  /**
   * Assigns the given values from the unassigned list to the assigned list.
   */
  public void assignItems(List<K> list)
  {
    if (list == null || list.size() == 0) {
      return;
    }
    Iterator<KeyValueBean<K, V>> iterator = unassignedItems.iterator();
    while (iterator.hasNext()) {
      KeyValueBean<K, V> entry = iterator.next();
      if (list.contains(entry.getKey()) == true) {
        iterator.remove();
        assignedItems.add(entry);
      }
    }
  }

  /**
   * Unassign some values from the assigned list to the unassigned list.
   */
  public void unassignItems(List<K> list)
  {
    if (list == null || list.size() == 0) {
      return;
    }
    Iterator<KeyValueBean<K, V>> iterator = assignedItems.iterator();
    while (iterator.hasNext()) {
      KeyValueBean<K, V> entry = iterator.next();
      if (list.contains(entry.getKey()) == true) {
        iterator.remove();
        unassignedItems.add(entry);
      }
    }
  }

  /**
   * Return a string representation of this object.
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer("TwoListHelper[assignedList=");
    append(sb, assignedItems);
    sb.append(", unassignedList=");
    append(sb, unassignedItems);
    sb.append("]");
    return (sb.toString());
  }

  /** Only for internal use junit inside test cases. */
  public String getTestString()
  {
    StringBuffer sb = new StringBuffer();
    append(sb, assignedItems);
    sb.append(":");
    append(sb, unassignedItems);
    return sb.toString();
  }

  private void append(StringBuffer sb, List<KeyValueBean<K, V>> list)
  {
    sb.append("[");
    boolean first = true;
    for (KeyValueBean<K, V> entry : list) {
      if (first == true)
        first = false;
      else sb.append(", ");
      sb.append(entry.getKey());
    }
    sb.append("]");
  }

  public void sortLists()
  {
    Collections.sort(getAssignedItems());
    Collections.sort(getUnassignedItems());
  }

  /**
   * The user has selected the assign button, so the selected items will be assigned within this method.
   * 
   */
  public void assign(List<K> selectedItemsToAssign)
  {
    assignItems(selectedItemsToAssign);
    sortLists();
  }

  /**
   * The user has selected the unassign button, so the selected items will be unassigned within this method.
   * 
   */
  public void unassign(List<K> selectedItemsToUnassign)
  {
    unassignItems(selectedItemsToUnassign);
    sortLists();
  }
}
