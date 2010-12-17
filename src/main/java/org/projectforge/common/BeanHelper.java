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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Some useful methods for determing and converting property, getter and setter names.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class BeanHelper
{
  private static final Logger log = Logger.getLogger(BeanHelper.class);

  public static String determinePropertyName(Method method)
  {
    String name = method.getName();
    if (name.startsWith("get") == true || name.startsWith("set") == true) {
      return StringUtils.uncapitalize(name.substring(3));
    } else if (name.startsWith("is") == true) {
      return StringUtils.uncapitalize(name.substring(2));
    }
    return method.getName();
  }

  public static Method determineGetter(Class< ? > clazz, String fieldname)
  {
    String cap = StringUtils.capitalize(fieldname);
    Method[] methods = clazz.getMethods();
    for (Method method : methods) {
      if (("get" + cap).equals(method.getName()) == true || ("is" + cap).equals(method.getName()) == true) {
        return method;
      }
    }
    return null;
  }

  public static Class< ? > determinePropertyType(Method method)
  {
    String name = method.getName();
    if (name.startsWith("get") == false && name.startsWith("is") == false) {
      throw new UnsupportedOperationException("determinePropertyType only yet implemented for getter methods.");
    }
    return method.getReturnType();
  }

  /**
   * Does not work for multiple setter methods with one argument and different parameter type (e. g. setField(Date) and setField(long)).
   * @param clazz
   * @param fieldname
   * @return
   */
  public static Method determineSetter(Class< ? > clazz, String fieldname)
  {
    String cap = StringUtils.capitalize(fieldname);
    Method[] methods = clazz.getMethods();
    for (Method method : methods) {
      if (("set" + cap).equals(method.getName()) == true && method.getParameterTypes().length == 1) {
        return method;
      }
    }
    return null;
  }

  public static Method determineSetter(Class< ? > clazz, Method method)
  {
    String name = method.getName();
    if (name.startsWith("set") == true) {
      return method;
    } else {
      try {
        if (name.startsWith("get") == true) {
          Class< ? > parameterType = method.getReturnType();
          String setterName = "s" + name.substring(1);
          return clazz.getMethod(setterName, new Class[] { parameterType});
        } else if (name.startsWith("is") == true) {
          Class< ? > parameterType = method.getReturnType();
          String setterName = "set" + name.substring(2);
          return clazz.getMethod(setterName, new Class[] { parameterType});
        }
      } catch (SecurityException ex) {
        log.fatal("Could not determine setter for '" + name + "': " + ex, ex);
        throw new RuntimeException(ex);
      } catch (NoSuchMethodException ex) {
        log.fatal("Could not determine setter for '" + name + "': " + ex, ex);
        throw new RuntimeException(ex);
      }
    }
    log.error("Could not determine setter for '" + name + "'.");
    return null;
  }

  public static void invokeSetter(Object obj, Method method, Object value)
  {
    Method setter = determineSetter(obj.getClass(), method);
    invoke(obj, setter, value);
  }

  /**
   * Invokes the method of the given object (without arguments).
   * @param obj
   * @param method
   * @return
   */
  public static Object invoke(Object obj, Method method)
  {
    return invoke(obj, method, null);
  }

  public static Object invoke(Object obj, Method method, Object[] args)
  {
    try {
      return method.invoke(obj, args);
    } catch (IllegalArgumentException ex) {
      log.fatal("Could not invoke '" + method.getName() + "': " + ex + " for object [" + obj + "] with args: " + args, ex);
      throw new RuntimeException(ex);
    } catch (IllegalAccessException ex) {
      log.fatal("Could not invoke '" + method.getName() + "': " + ex + " for object [" + obj + "] with args: " + args, ex);
      throw new RuntimeException(ex);
    } catch (InvocationTargetException ex) {
      log.fatal("Could not invoke '" + method.getName() + "': " + ex + " for object [" + obj + "] with args: " + args, ex);
      throw new RuntimeException(ex);
    }
  }

  public static Object newInstance(final String className)
  {
    Class< ? > clazz = null;
    try {
      clazz = Class.forName(className);
    } catch (final ClassNotFoundException ex) {
      log.error("Can't create instance of '" + className + "': " + ex.getMessage(), ex);
    }
    if (clazz != null) {
      return newInstance(clazz);
    }
    return null;
  }

  public static Object newInstance(final Class< ? > clazz)
  {
    Constructor< ? > constructor = null;
    try {
      constructor = clazz.getDeclaredConstructor(new Class[0]);
    } catch (SecurityException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    } catch (NoSuchMethodException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    }
    if (constructor == null) {
      try {
        return clazz.newInstance();
      } catch (InstantiationException ex) {
        log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
      } catch (IllegalAccessException ex) {
        log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
      }
      return null;
    }
    constructor.setAccessible(true);
    try {
      return constructor.newInstance();
    } catch (IllegalArgumentException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    } catch (InstantiationException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    } catch (IllegalAccessException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    } catch (InvocationTargetException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    }
    return null;
  }

  public static Object newInstance(final Class< ? > clazz, final Class< ? > paramType, final Object param)
  {
    return newInstance(clazz, new Class< ? >[] { paramType}, param);
  }

  public static Object newInstance(final Class< ? > clazz, final Class< ? > paramType1, final Class< ? > paramType2, final Object param1,
      final Object param2)
  {
    return newInstance(clazz, new Class< ? >[] { paramType1, paramType2}, param1, param2);
  }

  public static Object newInstance(final Class< ? > clazz, final Class< ? >[] paramTypes, final Object... params)
  {
    Constructor< ? > constructor = null;
    try {
      constructor = clazz.getDeclaredConstructor(paramTypes);
    } catch (SecurityException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    } catch (NoSuchMethodException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    }
    constructor.setAccessible(true);
    try {
      return constructor.newInstance(params);
    } catch (IllegalArgumentException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    } catch (InstantiationException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    } catch (IllegalAccessException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    } catch (InvocationTargetException ex) {
      log.error("Can't create instance of '" + clazz.getName() + "': " + ex.getMessage(), ex);
    }
    return null;
  }

  public static Object invoke(Object obj, Method method, Object arg)
  {
    return invoke(obj, method, new Object[] { arg});
  }

  /**
   * Return all fields declared by the given class and all super classes.
   * @param clazz
   * @return
   * @see Class#getDeclaredFields()
   */
  public static Field[] getAllDeclaredFields(Class< ? > clazz)
  {
    Field[] fields = clazz.getDeclaredFields();
    while (clazz.getSuperclass() != null) {
      clazz = clazz.getSuperclass();
      fields = (Field[]) ArrayUtils.addAll(fields, clazz.getDeclaredFields());
    }
    return fields;
  }

  /**
   * Invokes getter method of the given bean.
   * @param bean
   * @param property
   * @return
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public static Object getProperty(Object bean, String property)
  {
    Method getter = determineGetter(bean.getClass(), property);
    if (getter == null) {
      throw new RuntimeException("Getter for property '" + property + "' not found.");
    }
    try {
      return getter.invoke(bean);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    }
  }

  /**
   * Invokes setter method of the given bean.
   * @param bean
   * @param property
   * @param value
   * @see Method#invoke(Object, Object...)
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public static Object setProperty(final Object bean, final String property, final Object value)
  {
    Method setter = determineSetter(bean.getClass(), property);
    if (setter == null) {
      throw new RuntimeException("Getter for property '" + property + "' not found.");
    }
    try {
      return setter.invoke(bean, value);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    }
  }

  /**
   * Invokes getter method of the given bean and returns the idx element of array or collection. Use-age: "user[3]".
   * @param bean
   * @param property Must be from format "xxx[#]"
   * @return
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public static Object getIndexedProperty(Object bean, String property)
  {
    int pos = property.indexOf('[');
    if (pos <= 0) {
      throw new UnsupportedOperationException("'" + property + "' is not an indexed property, such as 'xxx[#]'.");
    }
    String prop = property.substring(0, pos);
    String indexString = property.substring(pos + 1, property.length() - 1);
    Integer index = NumberHelper.parseInteger(indexString);
    if (index == null) {
      throw new UnsupportedOperationException("'" + property + "' contains no number as index string: '" + indexString + "'.");
    }
    Object value = getProperty(bean, prop);
    if (value == null) {
      return null;
    }
    if (value instanceof Collection< ? > == true) {
      CollectionUtils.get(value, index);
    } else if (value.getClass().isArray() == true) {
      return Array.get(value, index);
    }
    throw new UnsupportedOperationException("Collection or array from type '"
        + value.getClass()
        + "' not yet supported: '"
        + property
        + "'.");
  }

  /**
   * Later Genome SimpleProperty should be used. Property or nested property can be null. Indexed properties are also supported.
   * @param bean
   * @param property
   * @return
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public static Object getNestedProperty(Object bean, String property)
  {
    if (StringUtils.isEmpty(property) == true || bean == null) {
      return null;
    }
    String[] props = StringUtils.split(property, '.');
    Object value = bean;
    for (String prop : props) {
      if (prop.indexOf('[') > 0) {
        value = getIndexedProperty(value, prop);
      } else {
        value = getProperty(value, prop);
      }
      if (value == null) {
        return null;
      }
    }
    return value;
  }

  /**
   * Gets all declared fields which are neither transient, static nor final.
   * @param clazz
   * @return
   */
  public static Field[] getDeclaredPropertyFields(final Class< ? > clazz)
  {
    final Field[] fields = getAllDeclaredFields(clazz);
    final List<Field> list = new ArrayList<Field>();
    for (final Field field : fields) {
      if (Modifier.isTransient(field.getModifiers()) == false
          && Modifier.isStatic(field.getModifiers()) == false
          && Modifier.isFinal(field.getModifiers()) == false) {
        list.add(field);
      }
    }
    final Field[] result = new Field[list.size()];
    list.toArray(result);
    return result;
  }

  public static Object getFieldValue(final Object obj, final Field field)
  {
    try {
      return field.get(obj);
    } catch (IllegalArgumentException ex) {
      log.error("Exception encountered while getting value of field '"
          + field.getName()
          + "' of object from type '"
          + obj.getClass().getName()
          + "': "
          + ex, ex);
    } catch (IllegalAccessException ex) {
      log.error("Exception encountered while getting value of field '"
          + field.getName()
          + "' of object from type '"
          + obj.getClass().getName()
          + "': "
          + ex, ex);
    }
    return null;
  }
}
