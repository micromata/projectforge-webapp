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
import java.lang.reflect.Method;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.BeanHelper;

/**
 * Represents one attribute of a table (e. g. for creation).
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TableAttribute implements Serializable
{
  private static final long serialVersionUID = -8369835632981780449L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TableAttribute.class);

  private boolean nullable = true;

  private TableAttributeType type;

  private String property;

  private String name;

  private int length = 255;

  private int precision = 0;

  private int scale = 0;

  private boolean primaryKey;

  private boolean generated;

  private boolean unique;

  private String foreignTable;

  private String foreignAttribute;

  private String defaultValue;

  /**
   * Creates a property and gets the information from the entity class. The JPA annotations Column, JoinColumn, Entity, Table and ID are
   * supported.
   * @param clazz
   * @param property
   */
  public TableAttribute(final Class< ? > clazz, final String property)
  {
    this.property = property;
    this.name = property;
    final Method getterMethod = BeanHelper.determineGetter(clazz, property);
    if (getterMethod == null) {
      throw new IllegalStateException("Can't determine getter: " + clazz + "." + property);
    }
    final Class< ? > dType = BeanHelper.determinePropertyType(getterMethod);
    final boolean primitive = dType.isPrimitive();
    if (Boolean.class.isAssignableFrom(dType) == true || Boolean.TYPE.isAssignableFrom(dType) == true) {
      type = TableAttributeType.BOOLEAN;
    } else if (Integer.class.isAssignableFrom(dType) == true || Integer.TYPE.isAssignableFrom(dType) == true) {
      type = TableAttributeType.INT;
    } else if (String.class.isAssignableFrom(dType) == true || dType.isEnum() == true) {
      type = TableAttributeType.VARCHAR;
    } else if (BigDecimal.class.isAssignableFrom(dType) == true) {
      type = TableAttributeType.DECIMAL;
    } else if (java.sql.Date.class.isAssignableFrom(dType) == true) {
      type = TableAttributeType.DATE;
    } else if (java.util.Date.class.isAssignableFrom(dType) == true) {
      type = TableAttributeType.TIMESTAMP;
    } else if (java.util.Locale.class.isAssignableFrom(dType) == true) {
      type = TableAttributeType.LOCALE;
    } else {
      final Entity entity = dType.getAnnotation(Entity.class);
      final javax.persistence.Table table = dType.getAnnotation(javax.persistence.Table.class);
      if (entity != null && table != null && StringUtils.isNotEmpty(table.name()) == true) {
        this.foreignTable = table.name();
        final String idProperty = JPAHelper.getIdProperty(dType);
        if (idProperty == null) {
          log.info("Id property not found for class '" + dType + "'): " + clazz + "." + property);
        }
        this.foreignAttribute = idProperty;
        final Column column = JPAHelper.getColumnAnnotation(dType, idProperty);
        if (column != null && StringUtils.isNotEmpty(column.name()) == true) {
          this.foreignAttribute = column.name();
        }
      } else {
        log.info("Unsupported property (@Entity, @Table and @Table.name expected for the destination class '"
            + dType
            + "'): "
            + clazz
            + "."
            + property);
      }
      type = TableAttributeType.INT;
    }
    final Id id = JPAHelper.getIdAnnotation(clazz, property);
    if (id != null) {
      this.primaryKey = true;
      this.nullable = false;
    }
    if (primitive == true) {
      nullable = false;
    }
    final Column column = JPAHelper.getColumnAnnotation(clazz, property);
    if (column != null) {
      if (isPrimaryKey() == false && primitive == false) {
        this.nullable = column.nullable();
      }
      if (StringUtils.isNotEmpty(column.name()) == true) {
        this.name = column.name();
      }
      if (type.isIn(TableAttributeType.VARCHAR, TableAttributeType.CHAR) == true) {
        this.length = column.length();
      }
      if (type == TableAttributeType.DECIMAL) {
        this.precision = column.precision();
        this.scale = column.scale();
      }
      this.unique = column.unique();
    }
    if (type == TableAttributeType.DECIMAL && this.scale == 0 && this.precision == 0) {
      throw new UnsupportedOperationException("Decimal values should have a precision and scale definition: " + clazz + "." + property);
    }
    final JoinColumn joinColumn = JPAHelper.getJoinColumnAnnotation(clazz, property);
    if (joinColumn != null) {
      if (StringUtils.isNotEmpty(joinColumn.name()) == true) {
        this.name = joinColumn.name();
      }
    }
  }

  public TableAttribute(final String name, final TableAttributeType type)
  {
    this.name = name;
    this.type = type;
  }

  public TableAttribute(final String name, final TableAttributeType type, final boolean nullable)
  {
    this(name, type);
    this.nullable = nullable;
  }

  public TableAttribute(final String name, final TableAttributeType type, final int length)
  {
    this(name, type);
    if (type != TableAttributeType.VARCHAR && type != TableAttributeType.CHAR) {
      throw new UnsupportedOperationException("Length not supported for attributes of type '" + type + "'.");
    }
    this.length = length;
  }

  public TableAttribute(final String name, final TableAttributeType type, final int length, final boolean nullable)
  {
    this(name, type, length);
    this.nullable = nullable;
  }

  public TableAttribute(final String name, final TableAttributeType type, final int precision, final int scale)
  {
    this(name, type);
    if (type != TableAttributeType.DECIMAL) {
      throw new UnsupportedOperationException("Precision and scale not supported for attributes of type '" + type + "'.");
    }
    this.precision = precision;
    this.scale = scale;
  }

  public TableAttribute(final String name, final TableAttributeType type, final int precision, final int scale, final boolean nullable)
  {
    this(name, type, precision, scale);
    this.nullable = nullable;
  }

  public boolean isNullable()
  {
    return nullable;
  }

  public TableAttribute setNullable(final boolean nullable)
  {
    this.nullable = nullable;
    return this;
  }

  /**
   * Not yet supported.
   */
  public boolean isUnique()
  {
    return unique;
  }

  public TableAttributeType getType()
  {
    return type;
  }

  public TableAttribute setType(final TableAttributeType type)
  {
    this.type = type;
    return this;
  }

  /**
   * @return The name of the property or if not exist the data-base identifier of the attribute.
   */
  public String getProperty()
  {
    return property != null ? property : name;
  }

  /**
   * @return The data base identifier of the attribute.
   */
  public String getName()
  {
    return name;
  }

  public TableAttribute setName(final String name)
  {
    this.name = name;
    return this;
  }

  /**
   * Length of CHAR and VARCHAR.
   * @return
   */
  public int getLength()
  {
    return length;
  }

  public TableAttribute setLength(final int length)
  {
    this.length = length;
    return this;
  }

  /**
   * Precision of numerical (decimal) values.
   */
  public int getPrecision()
  {
    return precision;
  }

  public TableAttribute setPrecision(final int precision)
  {
    this.precision = precision;
    return this;
  }

  /**
   * Scale of numerical (decimal) values.
   */
  public int getScale()
  {
    return scale;
  }

  public TableAttribute setScale(final int scale)
  {
    this.scale = scale;
    return this;
  }

  public boolean isPrimaryKey()
  {
    return primaryKey;
  }

  /**
   * Sets also this attribute as generated at default if it's from type INT.
   * @param primaryKey
   * @return
   */
  public TableAttribute setPrimaryKey(final boolean primaryKey)
  {
    this.primaryKey = primaryKey;
    if (this.type == TableAttributeType.INT) {
      this.generated = true;
    }
    return this;
  }

  /**
   * True (default for primary keys of type INT) if the primary key should be generated by the database.
   */
  public boolean isGenerated()
  {
    return generated;
  }

  public TableAttribute setGenerated(final boolean generated)
  {
    this.generated = generated;
    return this;
  }

  public String getForeignTable()
  {
    return foreignTable;
  }

  /**
   * Sets foreign table. The primary key is set to default ("pk") if not set before.
   * @param foreignTable
   * @return this for chaining.
   */
  public TableAttribute setForeignTable(final String foreignTable)
  {
    this.foreignTable = foreignTable;
    if (this.foreignAttribute == null) {
      this.foreignAttribute = "pk";
    }
    return this;
  }

  /**
   * Sets foreign table. The primary key is set to default ("pk") if not set before.
   * @param foreignTableEntity
   * @return this for chaining.
   */
  public TableAttribute setForeignTable(final Class< ? > foreignTableEntity)
  {
    return setForeignTable(new Table(foreignTableEntity));
  }

  /**
   * Sets foreign table. The primary key is set to default ("pk") if not set before.
   * @param foreignTable
   * @return this for chaining.
   */
  public TableAttribute setForeignTable(final Table foreignTable)
  {
    return setForeignTable(foreignTable.getName());
  }

  public String getForeignAttribute()
  {
    return foreignAttribute;
  }

  public TableAttribute setForeignAttribute(final String foreignAttribute)
  {
    this.foreignAttribute = foreignAttribute;
    return this;
  }

  /**
   * @since 3.3.46 (didn't work before).
   */
  public String getDefaultValue()
  {
    return defaultValue;
  }

  public TableAttribute setDefaultValue(final String defaultValue)
  {
    this.defaultValue = defaultValue;
    return this;
  }
}
