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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.test.TestConfiguration;


public class AbstractBaseDOTest
{
  @BeforeClass
  public static void setUp()
  {
    TestConfiguration.initAsTestConfiguration();
  }

  @Test
  public void determinePropertyName() throws NoSuchMethodException
  {
    FooDO obj = createFooDO(21, 22, false, true, "Hurzel");
    final String created = DateHelper.getForTestCase(obj.getCreated());
    final String lastUpdate = DateHelper.getForTestCase(obj.getLastUpdate());

    FooDO src = createFooDO(19, 20, true, false, "Test");

    obj.copyValuesFrom(src);

    assertEquals("Test", obj.getTestString());
    assertEquals(true, obj.isDeleted());
    assertEquals(false, obj.isTestBoolean());
    assertEquals(created, DateHelper.getForTestCase(obj.getCreated()));
    assertEquals(lastUpdate, DateHelper.getForTestCase(obj.getLastUpdate()));

    obj = createFooDO(21, 22, false, true, "Hurzel");
    src = createFooDO(19, 20, false, true, null);
    obj.copyValuesFrom(src);
    assertEquals("Expected, that the property will be overwritten by null", null, obj.getTestString());
    assertEquals(false, obj.isDeleted());
    assertEquals(true, obj.isTestBoolean());
    assertEquals(created, DateHelper.getForTestCase(obj.getCreated()));
    assertEquals(lastUpdate, DateHelper.getForTestCase(obj.getLastUpdate()));
  }

  private FooDO createFooDO(final int createdDayOfMonth, final int lastUpdateDateOfMonth, final boolean deleted, final boolean testBoolean, final String testString)
  {
    final FooDO obj = new FooDO();
    final DateHolder dateHolder = new DateHolder(DatePrecision.SECOND, Locale.GERMAN);
    obj.setId(42);
    dateHolder.setDate(1970, Calendar.NOVEMBER, createdDayOfMonth, 4, 50, 0);
    obj.setCreated(dateHolder.getDate());
    dateHolder.setDate(1970, Calendar.NOVEMBER, lastUpdateDateOfMonth, 4, 50, 0);
    obj.setLastUpdate(dateHolder.getDate());
    obj.setDeleted(deleted);
    obj.setTestBoolean(testBoolean);
    obj.setTestString(testString);
    return obj;
  }

  @Test
  public void copyValuesFrom()
  {
    final FooDO srcFoo = new FooDO();
    srcFoo.setManagedChilds(new ArrayList<BarDO>());
    srcFoo.setUnmanagedChilds1(new ArrayList<BarDO>());
    srcFoo.setUnmanagedChilds2(new ArrayList<BarDO>());
    srcFoo.getManagedChilds().add(new BarDO(1, "src1"));
    srcFoo.getManagedChilds().add(new BarDO(2, "src2"));
    srcFoo.getUnmanagedChilds1().add(new BarDO(3, "src3"));
    srcFoo.getUnmanagedChilds1().add(new BarDO(4, "src4"));
    srcFoo.getUnmanagedChilds2().add(new BarDO(5, "src5"));
    srcFoo.getUnmanagedChilds2().add(new BarDO(6, "src6"));
    final FooDO destFoo = new FooDO();
    destFoo.setManagedChilds(new ArrayList<BarDO>());
    destFoo.setUnmanagedChilds1(new ArrayList<BarDO>());
    destFoo.setUnmanagedChilds2(new ArrayList<BarDO>());
    destFoo.getManagedChilds().add(new BarDO(1, "dest1"));
    destFoo.getManagedChilds().add(new BarDO(2, "dest2"));
    destFoo.getUnmanagedChilds1().add(new BarDO(3, "dest3"));
    destFoo.getUnmanagedChilds1().add(new BarDO(4, "dest4"));
    destFoo.getUnmanagedChilds2().add(new BarDO(5, "dest5"));
    destFoo.getUnmanagedChilds2().add(new BarDO(6, "dest6"));
    destFoo.copyValuesFrom(srcFoo);
    ArrayList<BarDO> list = (ArrayList<BarDO>)destFoo.getManagedChilds();
    assertEquals("src1", list.get(0).getTestString());
    assertEquals("src2", list.get(1).getTestString());
    list = (ArrayList<BarDO>)destFoo.getUnmanagedChilds1();
    assertEquals("dest3", list.get(0).getTestString());
    assertEquals("dest4", list.get(1).getTestString());
    list = (ArrayList<BarDO>)destFoo.getUnmanagedChilds2();
    assertEquals("dest5", list.get(0).getTestString());
    assertEquals("dest6", list.get(1).getTestString());
  }
}
