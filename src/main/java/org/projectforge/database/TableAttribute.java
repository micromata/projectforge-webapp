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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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

  // For debug messages.
  private Class< ? > entityClass;

  private String property;

  private Class< ? > propertyType;

  /**
   * Only set for List and Sets.
   */
  private Class< ? > genericType;

  private Boolean generated;

  private String name;

  private int length = 255;

  private int precision = 0;

  private int scale = 0;

  private boolean primaryKey;

  private boolean unique;

  private String foreignTable;

  private String foreignAttribute;

  private String defaultValue;

  private List<Annotation> annotations;

  /**
   * Creates a property and gets the information from the entity class. The JPA annotations Column, JoinColumn, Entity, Table and ID are
   * supported.
   * @param clazz
   * @param property
   */
  public TableAttribute(final Class< ? > clazz, final String property)
  {
    this.entityClass = clazz;
    this.property = property;
    this.name = property;
    final Method getterMethod = BeanHelper.determineGetter(clazz, property);
    if (getterMethod == null) {
      throw new IllegalStateException("Can't determine getter: " + clazz + "." + property);
    }
    propertyType = BeanHelper.determinePropertyType(getterMethod);
    final boolean primitive = propertyType.isPrimitive();
    if (Boolean.class.isAssignableFrom(propertyType) == true || Boolean.TYPE.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.BOOLEAN;
    } else if (Integer.class.isAssignableFrom(propertyType) == true || Integer.TYPE.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.INT;
    } else if (Short.class.isAssignableFrom(propertyType) == true || Short.TYPE.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.SHORT;
    } else if (String.class.isAssignableFrom(propertyType) == true || propertyType.isEnum() == true) {
      type = TableAttributeType.VARCHAR;
    } else if (BigDecimal.class.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.DECIMAL;
    } else if (java.sql.Date.class.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.DATE;
    } else if (java.util.Date.class.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.TIMESTAMP;
    } else if (java.util.Locale.class.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.LOCALE;
    } else if (java.util.List.class.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.LIST;
      setGenericReturnType(getterMethod);
    } else if (java.util.Set.class.isAssignableFrom(propertyType) == true) {
      type = TableAttributeType.SET;
      setGenericReturnType(getterMethod);
    } else {
      final Entity entity = propertyType.getAnnotation(Entity.class);
      final javax.persistence.Table table = propertyType.getAnnotation(javax.persistence.Table.class);
      if (entity != null && table != null && StringUtils.isNotEmpty(table.name()) == true) {
        this.foreignTable = table.name();
        final String idProperty = JPAHelper.getIdProperty(propertyType);
        if (idProperty == null) {
          log.info("Id property not found for class '" + propertyType + "'): " + clazz + "." + property);
        }
        this.foreignAttribute = idProperty;
        final Column column = JPAHelper.getColumnAnnotation(propertyType, idProperty);
        if (column != null && StringUtils.isNotEmpty(column.name()) == true) {
          this.foreignAttribute = column.name();
        }
      } else {
        log.info("Unsupported property (@Entity, @Table and @Table.name expected for the destination class '"
            + propertyType
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
      if (joinColumn.nullable() == false) {
        this.nullable = false;
      }
    }
  }

  /**
   * @param method
   * @return
   */
  private void setGenericReturnType(final Method method)
  {
    final Type returnType = method.getGenericReturnType();
    if (returnType instanceof ParameterizedType) {
      final ParameterizedType type = (ParameterizedType) returnType;
      final Type[] typeArguments = type.getActualTypeArguments();
      if (typeArguments.length > 0) {
        final Class< ? > typeArgClass = (Class< ? >) typeArguments[0];
        if (log.isDebugEnabled() == true) {
          log.debug("Generic type found for '" + entityClass + "." + property + "': '" + typeArgClass + "'.");
        }
        genericType = typeArgClass;
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
   * The generic type is set if the property class is a list or set.
   * @return the genericType
   */
  public Class< ? > getGenericType()
  {
    return genericType;
  }

  /**
   * @return the propertyType
   */
  public Class< ? > getPropertyType()
  {
    return propertyType;
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
   * @param primaryKey
   * @return
   */
  public TableAttribute setPrimaryKey(final boolean primaryKey)
  {
    this.primaryKey = primaryKey;
    return this;
  }

  /**
   * True if the primary key should be generated by the database (if annotation {@link GeneratedValue} is specified). May be set manually by
   * {@link #setGenerated(Boolean)}.
   */
  public boolean isGenerated()
  {
    if (generated != null) {
      return generated;
    }
    return getAnnotation(GeneratedValue.class) != null;
  }

  /**
   * @param generated the generated to set
   * @return this for chaining.
   */
  public TableAttribute setGenerated(final Boolean generated)
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

  /**
   * @return the annotations
   */
  public List<Annotation> getAnnotations()
  {
    return annotations;
  }

  @SuppressWarnings("unchecked")
  public <T extends Annotation> T getAnnotation(final Class<T> annotationClass)
  {
    if (annotations == null) {
      return null;
    }
    for (final Annotation annotation : annotations) {
      if (annotation.annotationType().isAssignableFrom(annotationClass) == true) {
        return (T) annotation;
      }
    }
    return null;
  }

  /**
   * @param annotations the annotations to set
   */
  public void setAnnotations(final List<Annotation> annotations)
  {
    this.annotations = annotations;
  }
}
