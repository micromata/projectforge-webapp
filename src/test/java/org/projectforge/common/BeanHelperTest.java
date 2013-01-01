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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;

public class BeanHelperTest
{
  private String name;

  private boolean enabled;

  private String[] testStrings;

  private int[] testInts;

  @Test
  public void determineGetter()
  {
    Method getter = BeanHelper.determineGetter(this.getClass(), "name");
    assertEquals("getName", getter.getName());
    getter = BeanHelper.determineGetter(this.getClass(), "enabled");
    assertEquals("isEnabled", getter.getName());
    getter = BeanHelper.determineGetter(this.getClass(), "hurz");
    assertNull(getter);
    getter = BeanHelper.determineGetter(this.getClass(), "class");
    assertEquals("getClass", getter.getName());
  }

  @Test
  public void determinePropertyName() throws NoSuchMethodException
  {
    final Method getName = getClass().getDeclaredMethod("getName", new Class[] {});
    assertEquals("name", BeanHelper.determinePropertyName(getName));
    final Method setName = getClass().getDeclaredMethod("setName", new Class[] { String.class});
    assertEquals("name", BeanHelper.determinePropertyName(setName));
    final Method isEnabled = getClass().getDeclaredMethod("isEnabled", new Class[] {});
    assertEquals("enabled", BeanHelper.determinePropertyName(isEnabled));
  }

  @Test
  public void determineSetter() throws NoSuchMethodException
  {
    final Method getName = getClass().getDeclaredMethod("getName", new Class[] {});
    assertEquals("setName", BeanHelper.determineSetter(this.getClass(), getName).getName());
    final Method setName = getClass().getDeclaredMethod("setName", new Class[] { String.class});
    assertEquals("setName", BeanHelper.determineSetter(this.getClass(), setName).getName());
    final Method isEnabled = getClass().getDeclaredMethod("isEnabled", new Class[] {});
    assertEquals("setEnabled", BeanHelper.determineSetter(this.getClass(), isEnabled).getName());
  }

  @Test
  public void invokeSetter() throws NoSuchMethodException
  {
    final Method getName = getClass().getDeclaredMethod("getName", new Class[] {});
    BeanHelper.invokeSetter(this, getName, "Hurzel");
    assertEquals("Hurzel", getName());
    BeanHelper.invokeSetter(this, getName, null);
    assertNull(getName());
    final Method isEnabled = getClass().getDeclaredMethod("isEnabled", new Class[] {});
    BeanHelper.invokeSetter(this, isEnabled, true);
    assertEquals(true, isEnabled());
    BeanHelper.invokeSetter(this, isEnabled, false);
    assertEquals(false, isEnabled());
  }

  @Test
  public void setProperty() throws NoSuchMethodException
  {
    BeanHelper.setProperty(this, "name", "Hurzel");
    assertEquals("Hurzel", getName());
    BeanHelper.setProperty(this, "name", null);
    assertNull(getName());
    BeanHelper.setProperty(this, "enabled", true);
    assertEquals(true, isEnabled());
    BeanHelper.setProperty(this, "enabled", false);
    assertEquals(false, isEnabled());
  }

  @Test
  public void invoke() throws NoSuchMethodException
  {
    final Method getName = getClass().getDeclaredMethod("getName", new Class[] {});
    this.name = "invoke";
    assertEquals("invoke", BeanHelper.invoke(this, getName));
  }

  @Test
  public void getProperty()
  {
    this.name = "test";
    assertEquals("test", BeanHelper.getProperty(this, "name"));
    assertEquals("java.lang.String", BeanHelper.getNestedProperty(this, "name.class.name"));
    this.name = null;
    assertNull(BeanHelper.getNestedProperty(this, "name"));
    assertNull(BeanHelper.getNestedProperty(this, "name.class.name"));
    try {
      assertNull(BeanHelper.getNestedProperty(this, "nonExistingProperty.test.hurzel"));
      fail("Exception expected.");
    } catch (final RuntimeException ex) {
      // OK
    }
    testStrings = new String[] { null, "zwei", "drei"};
    assertEquals("zwei", BeanHelper.getIndexedProperty(this, "testStrings[1]"));
    assertEquals("java.lang.String", BeanHelper.getNestedProperty(this, "testStrings[1].class.name"));
    assertNull(BeanHelper.getNestedProperty(this, "testStrings[0]"));
    assertNull(BeanHelper.getNestedProperty(this, "testStrings[0].class.name"));
    testInts = new int[] { 1, 2, 3, 5, 6};
    assertEquals(2, BeanHelper.getIndexedProperty(this, "testInts[1]"));
  }

  @Test
  public void copyFields()
  {
    final BeanHelperTest src = new BeanHelperTest().setName("Hurzel").setEnabled(true);
    final BeanHelperTest dest = new BeanHelperTest();
    assertTrue("dest should be modified.", BeanHelper.copyProperties(src, dest, "name", "enabled"));
    assertEquals("Hurzel", dest.getName());
    assertTrue(dest.isEnabled());
    assertFalse("dest should be unmodified.", BeanHelper.copyProperties(src, dest, "name", "enabled"));
    src.setEnabled(false);
    assertTrue("dest should be modified.", BeanHelper.copyProperties(src, dest, "name", "enabled"));
    assertFalse(dest.isEnabled());
  }

  public String getName()
  {
    return this.name;
  }

  public BeanHelperTest setName(final String name)
  {
    this.name = name;
    return this;
  }

  public boolean isEnabled()
  {
    return this.enabled;
  }

  public BeanHelperTest setEnabled(final boolean value)
  {
    this.enabled = value;
    return this;
  }

  public String[] getTestStrings()
  {
    return testStrings;
  }

  public int[] getTestInts()
  {
    return testInts;
  }
}
