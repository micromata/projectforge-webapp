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

package org.projectforge.common;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.projectforge.common.KeyValueBean;
import org.projectforge.web.common.TwoListHelper;


/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TwoListHelperTest
{
  @Test
  public void execute()
  {
    List<KeyValueBean<Integer, String>> completeList = new ArrayList<KeyValueBean<Integer, String>>();
    completeList.add(new KeyValueBean<Integer, String>(1));
    completeList.add(new KeyValueBean<Integer, String>(2));
    completeList.add(new KeyValueBean<Integer, String>(3));
    completeList.add(new KeyValueBean<Integer, String>(4));
    completeList.add(new KeyValueBean<Integer, String>(5));
    TwoListHelper<Integer, String> twoList = new TwoListHelper<Integer, String>(completeList, null);
    List<Integer> assignedKeys = new ArrayList<Integer>();
    twoList = new TwoListHelper<Integer, String>(completeList, assignedKeys);
    assertEquals("Initialize.", "[]:[1, 2, 3, 4, 5]", twoList.getTestString());
    assertEquals("Check values to assign.", "[]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[]", twoList.getValuesToUnassign().toString());
    List<Integer> l = new ArrayList<Integer>();
    l.add(1);
    twoList.assignItems(l);
    assertEquals("Assign one.", "[1]:[2, 3, 4, 5]", twoList.getTestString());
    assertEquals("Check values to assign.", "[1]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[]", twoList.getValuesToUnassign().toString());
    twoList.assignItems(l);
    assertEquals("Assign again (with no effect).", "[1]:[2, 3, 4, 5]", twoList.getTestString());
    l.add(2);
    l.add(3);
    l.add(4);
    l.add(5);
    twoList.assignItems(l);
    assertEquals("Assign all.", "[1, 2, 3, 4, 5]:[]", twoList.getTestString());
    assertEquals("Check values to assign.", "[1, 2, 3, 4, 5]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[]", twoList.getValuesToUnassign().toString());
    twoList.unassignItems(l);
    assertEquals("Unassign all.", "[]:[1, 2, 3, 4, 5]", twoList.getTestString());
    assertEquals("Check values to assign.", "[]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[]", twoList.getValuesToUnassign().toString());
    twoList.unassignItems(l);
    assertEquals("Unassign all again with no effect.", "[]:[1, 2, 3, 4, 5]", twoList.getTestString());
    assertEquals("Check values to assign.", "[]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[]", twoList.getValuesToUnassign().toString());
    assignedKeys = new ArrayList<Integer>();
    assignedKeys.add(1);
    assignedKeys.add(2);
    assignedKeys.add(3);
    twoList = new TwoListHelper<Integer, String>(completeList, assignedKeys);
    assertEquals("Initialize.", "[1, 2, 3]:[4, 5]", twoList.getTestString());
    assertEquals("Check values to assign.", "[]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[]", twoList.getValuesToUnassign().toString());
    l = new ArrayList<Integer>();
    l.add(1);
    l.add(2);
    l.add(3);
    l.add(4);
    l.add(5);
    twoList.assignItems(l);
    assertEquals("Check lists.", "[1, 2, 3, 4, 5]:[]", twoList.getTestString());
    assertEquals("Check values to assign.", "[4, 5]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[]", twoList.getValuesToUnassign().toString());
    l = new ArrayList<Integer>();
    l.add(1);
    l.add(5);
    twoList.unassignItems(l);
    assertEquals("Check lists.", "[2, 3, 4]:[1, 5]", twoList.getTestString());
    assertEquals("Check values to assign.", "[4]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[1]", twoList.getValuesToUnassign().toString());
    assignedKeys.add(4);
    assignedKeys.add(5);
    twoList = new TwoListHelper<Integer, String>(completeList, assignedKeys);
    assertEquals("Initialize.", "[1, 2, 3, 4, 5]:[]", twoList.getTestString());
    assertEquals("Check values to assign.", "[]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[]", twoList.getValuesToUnassign().toString());
    twoList.unassignItems(assignedKeys);
    assertEquals("Check list.", "[]:[1, 2, 3, 4, 5]", twoList.getTestString());
    assertEquals("Check values to assign.", "[]", twoList.getValuesToAssign().toString());
    assertEquals("Check values to unassign.", "[1, 2, 3, 4, 5]", twoList.getValuesToUnassign().toString());
  }
}
