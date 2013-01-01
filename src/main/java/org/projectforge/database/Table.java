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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;

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

  private final List<TableAttribute> attributes = new ArrayList<TableAttribute>();

  public Table(final Class< ? > entityClass)
  {
    this.entityClass = entityClass;
    final Entity entity = entityClass.getAnnotation(Entity.class);
    final javax.persistence.Table table = entityClass.getAnnotation(javax.persistence.Table.class);
    if (entity != null && table != null && StringUtils.isNotEmpty(table.name()) == true) {
      this.name = table.name();
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

  public TableAttribute getAttribute(final String name)
  {
    for (final TableAttribute attr : attributes) {
      if (name.equals(attr.getProperty()) == true) {
        return attr;
      }
    }
    return null;
  }

  public Class< ? > getEntityClass()
  {
    return entityClass;
  }

  public String getName()
  {
    return name;
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
}
