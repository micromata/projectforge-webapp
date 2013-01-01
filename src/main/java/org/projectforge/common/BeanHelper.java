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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
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

  private static boolean TEST_MODE = false;

  /**
   * For internal test cases only! If true, log errors are suppressed. Please call {@link #exitTestMode()} always directly after your test
   * call!
   */
  public static void enterTestMode()
  {
    TEST_MODE = true;
    log.info("***** Entering TESTMODE.");
  }

  /**
   * For internal test cases only! If true, log errors are suppressed. Please set TEST_MODE always to false after your test call!
   */
  public static void exitTestMode()
  {
    TEST_MODE = false;
    log.info("***** Exit TESTMODE.");
  }

  public static String determinePropertyName(final Method method)
  {
    final String name = method.getName();
    if (name.startsWith("get") == true || name.startsWith("set") == true) {
      return StringUtils.uncapitalize(name.substring(3));
    } else if (name.startsWith("is") == true) {
      return StringUtils.uncapitalize(name.substring(2));
    }
    return method.getName();
  }

  public static Method determineGetter(final Class< ? > clazz, final String fieldname)
  {
    final String cap = StringUtils.capitalize(fieldname);
    final Method[] methods = clazz.getMethods();
    for (final Method method : methods) {
      if (("get" + cap).equals(method.getName()) == true || ("is" + cap).equals(method.getName()) == true) {
        if (method.isBridge() == false) {
          // Don't return bridged methods (methods defined in interface or super class with different return type).
          return method;
        }
      }
    }
    return null;
  }

  public static Class< ? > determinePropertyType(final Method method)
  {
    if (method == null) {
      return null;
    }
    final String name = method.getName();
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
  public static Method determineSetter(final Class< ? > clazz, final String fieldname)
  {
    final String cap = StringUtils.capitalize(fieldname);
    final Method[] methods = clazz.getMethods();
    for (final Method method : methods) {
      if (("set" + cap).equals(method.getName()) == true && method.getParameterTypes().length == 1) {
        return method;
      }
    }
    return null;
  }

  public static Method determineSetter(final Class< ? > clazz, final Method method)
  {
    final String name = method.getName();
    if (name.startsWith("set") == true) {
      return method;
    } else {
      try {
        if (name.startsWith("get") == true) {
          final Class< ? > parameterType = method.getReturnType();
          final String setterName = "s" + name.substring(1);
          return clazz.getMethod(setterName, new Class[] { parameterType});
        } else if (name.startsWith("is") == true) {
          final Class< ? > parameterType = method.getReturnType();
          final String setterName = "set" + name.substring(2);
          return clazz.getMethod(setterName, new Class[] { parameterType});
        }
      } catch (final SecurityException ex) {
        log.fatal("Could not determine setter for '" + name + "': " + ex, ex);
        throw new RuntimeException(ex);
      } catch (final NoSuchMethodException ex) {
        log.fatal("Could not determine setter for '" + name + "': " + ex, ex);
        throw new RuntimeException(ex);
      }
    }
    log.error("Could not determine setter for '" + name + "'.");
    return null;
  }

  public static void invokeSetter(final Object obj, final Method method, final Object value)
  {
    final Method setter = determineSetter(obj.getClass(), method);
    invoke(obj, setter, value);
  }

  /**
   * Invokes the method of the given object (without arguments).
   * @param obj
   * @param method
   * @return
   */
  public static Object invoke(final Object obj, final Method method)
  {
    return invoke(obj, method, null);
  }

  public static Object invoke(final Object obj, final Method method, final Object[] args)
  {
    try {
      return method.invoke(obj, args);
    } catch (final IllegalArgumentException ex) {
      log.fatal("Could not invoke '" + method.getName() + "': " + ex + " for object [" + obj + "] with args: " + args, ex);
      throw new RuntimeException(ex);
    } catch (final IllegalAccessException ex) {
      log.fatal("Could not invoke '" + method.getName() + "': " + ex + " for object [" + obj + "] with args: " + args, ex);
      throw new RuntimeException(ex);
    } catch (final InvocationTargetException ex) {
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
      logInstantiationException(ex, className);
    }
    if (clazz != null) {
      return newInstance(clazz);
    }
    return null;
  }

  private static void logInstantiationException(final Exception ex, final Class< ? > clazz)
  {
    logInstantiationException(ex, clazz.getName());
  }

  private static void logInstantiationException(final Exception ex, final String className)
  {
    if (TEST_MODE == false) {
      log.error("Can't create instance of '" + className + "': " + ex.getMessage(), ex);
    } else {
      log.info("***** TESTMODE: '" + className + "' wasn't created (OK in test mode).");
    }
  }

  public static Object newInstance(final Class< ? > clazz)
  {
    Constructor< ? > constructor = null;
    try {
      constructor = clazz.getDeclaredConstructor(new Class[0]);
    } catch (final SecurityException ex) {
      logInstantiationException(ex, clazz);
    } catch (final NoSuchMethodException ex) {
      logInstantiationException(ex, clazz);
    }
    if (constructor == null) {
      try {
        return clazz.newInstance();
      } catch (final InstantiationException ex) {
        logInstantiationException(ex, clazz);
      } catch (final IllegalAccessException ex) {
        logInstantiationException(ex, clazz);
      }
      return null;
    }
    constructor.setAccessible(true);
    try {
      return constructor.newInstance();
    } catch (final IllegalArgumentException ex) {
      logInstantiationException(ex, clazz);
    } catch (final InstantiationException ex) {
      logInstantiationException(ex, clazz);
    } catch (final IllegalAccessException ex) {
      logInstantiationException(ex, clazz);
    } catch (final InvocationTargetException ex) {
      logInstantiationException(ex, clazz);
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
    } catch (final SecurityException ex) {
      logInstantiationException(ex, clazz);
    } catch (final NoSuchMethodException ex) {
      logInstantiationException(ex, clazz);
    }
    constructor.setAccessible(true);
    try {
      return constructor.newInstance(params);
    } catch (final IllegalArgumentException ex) {
      logInstantiationException(ex, clazz);
    } catch (final InstantiationException ex) {
      logInstantiationException(ex, clazz);
    } catch (final IllegalAccessException ex) {
      logInstantiationException(ex, clazz);
    } catch (final InvocationTargetException ex) {
      logInstantiationException(ex, clazz);
    }
    return null;
  }

  public static Object invoke(final Object obj, final Method method, final Object arg)
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
  public static Object getProperty(final Object bean, final String property)
  {
    final Method getter = determineGetter(bean.getClass(), property);
    if (getter == null) {
      throw new RuntimeException("Getter for property '" + property + "' not found.");
    }
    try {
      return getter.invoke(bean);
    } catch (final IllegalArgumentException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    } catch (final IllegalAccessException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    } catch (final InvocationTargetException ex) {
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
    final Method setter = determineSetter(bean.getClass(), property);
    if (setter == null) {
      throw new RuntimeException("Getter for property '" + property + "' not found.");
    }
    try {
      return setter.invoke(bean, value);
    } catch (final IllegalArgumentException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    } catch (final IllegalAccessException ex) {
      throw new RuntimeException("For property '" + property + "'.", ex);
    } catch (final InvocationTargetException ex) {
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
  public static Object getIndexedProperty(final Object bean, final String property)
  {
    final int pos = property.indexOf('[');
    if (pos <= 0) {
      throw new UnsupportedOperationException("'" + property + "' is not an indexed property, such as 'xxx[#]'.");
    }
    final String prop = property.substring(0, pos);
    final String indexString = property.substring(pos + 1, property.length() - 1);
    final Integer index = NumberHelper.parseInteger(indexString);
    if (index == null) {
      throw new UnsupportedOperationException("'" + property + "' contains no number as index string: '" + indexString + "'.");
    }
    final Object value = getProperty(bean, prop);
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
  public static Object getNestedProperty(final Object bean, final String property)
  {
    if (StringUtils.isEmpty(property) == true || bean == null) {
      return null;
    }
    final String[] props = StringUtils.split(property, '.');
    Object value = bean;
    for (final String prop : props) {
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

  /**
   * @param obj
   * @param clazz Class of object or superclass at which the fieldname is declared
   * @param fieldname
   * @return
   */
  public static Object getFieldValue(final Object obj, final Class<?> clazz, final String fieldname)
  {
    if (obj == null) {
      return null;
    }
    try {
      final Field field = clazz.getDeclaredField(fieldname);
      field.setAccessible(true);
      return getFieldValue(obj, field);
    } catch (final SecurityException ex) {
      log.error("Exception encountered while getting field '"
          + fieldname
          + "' of object from type '"
          + clazz.getName()
          + "': "
          + ex, ex);
    } catch (final NoSuchFieldException ex) {
      log.error("Exception encountered while getting field '"
          + fieldname
          + "' of object from type '"
          + clazz.getName()
          + "': "
          + ex, ex);
    }
    return null;
  }

  public static Object getFieldValue(final Object obj, final Field field)
  {
    try {
      return field.get(obj);
    } catch (final IllegalArgumentException ex) {
      log.error("Exception encountered while getting value of field '"
          + field.getName()
          + "' of object from type '"
          + obj.getClass().getName()
          + "': "
          + ex, ex);
    } catch (final IllegalAccessException ex) {
      log.error("Exception encountered while getting value of field '"
          + field.getName()
          + "' of object from type '"
          + obj.getClass().getName()
          + "': "
          + ex, ex);
    }
    return null;
  }

  /**
   * Copies all properties from src object to dest object.
   * @param src
   * @param dest
   * @param properties
   * @return true if any property is different between src and dest object.
   */
  public static boolean copyProperties(final Object src, final Object dest, final String... properties)
  {
    return copyProperties(src, dest, false, properties);
  }

  /**
   * Copies all properties from src object to dest object.
   * @param src
   * @param dest
   * @param whitespaceEqualsNull If true than a String modification from null to "" and vice versa will be ignored.
   * @param properties
   * @return true if any property is different between src and dest object.
   */
  @SuppressWarnings("unchecked")
  public static boolean copyProperties(final Object src, final Object dest, final boolean whitespaceEqualsNull, final String... properties)
  {
    boolean modified = false;
    for (final String property : properties) {
      final Object srcValue = BeanHelper.getProperty(src, property);
      final Object destValue = BeanHelper.getProperty(dest, property);
      if (srcValue != null && srcValue instanceof Collection) {
        final Collection<Object> srcColl = (Collection<Object>) srcValue;
        final Collection<Object> destColl = (Collection<Object>) destValue;
        if (ListUtils.isEqualList(srcColl, destColl) == false) {
          modified = true;
          BeanHelper.setProperty(dest, property, srcValue);
        }
      } else if (srcValue != null && srcValue.getClass().isArray() == true) {
        final Object[] srcArr = (Object[]) srcValue;
        final Object[] destArr = (Object[]) destValue;
        if (Arrays.equals(srcArr, destArr) == false) {
          modified = true;
          BeanHelper.setProperty(dest, property, srcValue);
        }
      } else {
        if (ObjectUtils.equals(srcValue, destValue) == false) {
          if (whitespaceEqualsNull == true && (srcValue instanceof String || destValue instanceof String)) {
            if (StringUtils.isEmpty((String) srcValue) == false || StringUtils.isEmpty((String) destValue) == false) {
              // Do not copy this property if srcValue and destValue are empty ("" or null):
              BeanHelper.setProperty(dest, property, srcValue);
              modified = true;
            }
          } else {
            BeanHelper.setProperty(dest, property, srcValue);
            modified = true;
          }
        }
      }
    }
    return modified;
  }
}
