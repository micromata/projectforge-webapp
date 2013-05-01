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

package org.projectforge.database;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.BeanHelper;

/**
 * Represents one attribute of a table (e. g. for creation).
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class Table implements Serializable
{
  private static final long serialVersionUID = -1194016764141859556L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Table.class);

  private String name;

  private Class< ? > entityClass;

  private UniqueConstraint[] uniqueConstraints;

  private final List<TableAttribute> attributes = new ArrayList<TableAttribute>();

  public Table(final Class< ? > entityClass)
  {
    this.entityClass = entityClass;
    final Entity entity = entityClass.getAnnotation(Entity.class);
    final javax.persistence.Table table = entityClass.getAnnotation(javax.persistence.Table.class);
    if (entity != null && table != null && StringUtils.isNotEmpty(table.name()) == true) {
      this.name = table.name();
      uniqueConstraints = table.uniqueConstraints();
    } else {
      log.info("Unsupported class (@Entity, @Table and @Table.name expected): " + entityClass);
    }
  }

  /**
   * Only needed if addAttributes(String[]) will be called after and the entityClass wasn't set via constructor.
   * @param entityClass
   * @return this for chaining.
   * @see #Table(Class)
   * @see #addAttributes(String...)
   */
  public Table setEntityClass(final Class< ? > entityClass)
  {
    this.entityClass = entityClass;
    return this;
  }

  public Table(final String name)
  {
    this.name = name;
  }

  public TableAttribute getAttributeByProperty(final String property)
  {
    for (final TableAttribute attr : attributes) {
      if (property.equals(attr.getProperty()) == true) {
        return attr;
      }
    }
    return null;
  }

  public TableAttribute getAttributeByName(final String name)
  {
    if (name == null) {
      return null;
    }
    final String lowerCase = name.toLowerCase();
    for (final TableAttribute attr : attributes) {
      if (lowerCase.equals(attr.getName().toLowerCase()) == true) {
        return attr;
      }
    }
    return null;
  }

  public Class< ? > getEntityClass()
  {
    return entityClass;
  }

  /**
   * @return SQL name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @return the uniqueConstraints
   */
  public UniqueConstraint[] getUniqueConstraints()
  {
    return uniqueConstraints;
  }

  /**
   * Multiple primary keys are not allowed.
   * @return Primary key if found or null.
   */
  public TableAttribute getPrimaryKey()
  {
    for (final TableAttribute attr : attributes) {
      if (attr.isPrimaryKey() == true) {
        return attr;
      }
    }
    return null;
  }

  public List<TableAttribute> getAttributes()
  {
    return attributes;
  }

  public Table addAttribute(final TableAttribute attr)
  {
    if (getAttributeByProperty(attr.getProperty()) != null) {
      throw new IllegalArgumentException("Can't add table attribute twice: '" + entityClass + "." + attr.getProperty() + "");
    }
    if (getAttributeByName(attr.getName()) != null) {
      throw new IllegalArgumentException("Can't add table attribute twice: '" + entityClass + "." + attr.getProperty() + "");
    }
    attributes.add(attr);
    return this;
  }

  /**
   * Adds all the given properties by auto-detecting the given properties. Please note: There is no full auto-detection of all properties:
   * it should be avoided that unwanted properties are created (by a developer mistake).
   * @param properties
   * @see TableAttribute#TableAttribute(Class, String)
   * @see #addAttribute(TableAttribute)
   * @return this for chaining.
   */
  public Table addAttributes(final String... properties)
  {
    if (entityClass == null) {
      throw new IllegalStateException("Entity class isn't set. Can't add attributes from property names. Please set entity class first.");
    }
    for (final String property : properties) {
      addAttribute(new TableAttribute(entityClass, property));
    }
    return this;
  }

  /**
   * Adds default attributes of DefaultBaseDO: "id", "created", "lastUpate", "deleted".
   * @return this for chaining.
   */
  public Table addDefaultBaseDOAttributes()
  {
    return addAttributes("id", "created", "lastUpdate", "deleted");
  }

  /**
   * Adds all attributes which are annotated with Column (getter, setter or fields).
   * @return this for chaining.
   */
  public Table autoAddAttributes()
  {
    if (entityClass == null) {
      throw new IllegalStateException("Entity class isn't set. Can't add attributes from property names. Please set entity class first.");
    }
    final Field[] fields = BeanHelper.getAllDeclaredFields(entityClass);
    for (final Field field : fields) {
      final List<Annotation> annotations = handlePersistencyAnnotations(field);
      if (annotations == null) {
        continue;
      }
      final String fieldName = field.getName();
      if (log.isDebugEnabled() == true) {
        log.debug(name + "." + fieldName);
      }
      addTableAttribute(fieldName, annotations);
    }
    final List<Method> getter = BeanHelper.getAllGetterMethods(entityClass);
    for (final Method method : getter) {
      final List<Annotation> annotations = handlePersistencyAnnotations(method);
      if (annotations == null) {
        continue;
      }
      if (log.isDebugEnabled() == true) {
        log.debug(name + "." + method.getName());
      }
      final String property = BeanHelper.getProperty(method);
      if (property != null) {
        addTableAttribute(property, annotations);
      } else {
        log.error("Can't determine property of getter method: '" + method.getName());
      }
    }
    return this;
  }

  private void addTableAttribute(final String property, final List<Annotation> annotations)
  {
    final TableAttribute attr = new TableAttribute(entityClass, property);
    attr.setAnnotations(annotations);
    addAttribute(attr);
  }

  private List<Annotation> handlePersistencyAnnotations(final AccessibleObject object)
  {
    return JPAHelper.getPersistencyAnnotations(object);
  }
}
