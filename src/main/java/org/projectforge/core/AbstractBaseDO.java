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

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.collection.PersistentSet;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.ReflectionToString;
import org.projectforge.database.HibernateUtils;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@MappedSuperclass
public abstract class AbstractBaseDO<I extends Serializable> implements ExtendedBaseDO<I>, Serializable
{
  private static final long serialVersionUID = -2225460450662176301L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractBaseDO.class);

  private Date created;

  private Date lastUpdate;

  private boolean deleted;

  private boolean minorChange = false;

  private transient Map<String, Object> attributeMap;

  /**
   * If any re-calculations have to be done before displaying, indexing etc. This method have an implementation if a data object has
   * transient fields which are calculated by other fields. This default implementation does nothing.
   */
  public void recalculate()
  {
  }

  @Basic
  public boolean isDeleted()
  {
    return deleted;
  }

  public void setDeleted(final boolean deleted)
  {
    this.deleted = deleted;
  }

  @Basic
  public Date getCreated()
  {
    return created;
  }

  public void setCreated(final Date created)
  {
    this.created = created;
  }

  public void setCreated()
  {
    this.created = new Date();
  }

  /**
   * 
   * Last update will be modified automatically for every update of the database object.
   * @return
   */
  @Basic
  @Column(name = "last_update")
  public Date getLastUpdate()
  {
    return lastUpdate;
  }

  public void setLastUpdate(final Date lastUpdate)
  {
    this.lastUpdate = lastUpdate;
  }

  public void setLastUpdate()
  {
    this.lastUpdate = new Date();
  }

  /**
   * Default value is false.
   * @see org.projectforge.core.BaseDO#isMinorChange()
   */
  @Transient
  public boolean isMinorChange()
  {
    return minorChange;
  }

  public void setMinorChange(final boolean value)
  {
    this.minorChange = value;
  }

  public Object getAttribute(final String key)
  {
    if (attributeMap == null) {
      return null;
    }
    return attributeMap.get(key);
  }

  public void setAttribute(final String key, final Object value)
  {
    synchronized (attributeMap) {
      if (attributeMap == null) {
        attributeMap = new HashMap<String, Object>();
      }
    }
    attributeMap.put(key, value);
  }

  /**
   * Returns string containing all fields (except the password, via ReflectionToStringBuilder).
   * @return
   */
  @Override
  public String toString()
  {
    return ReflectionToString.asString(this);
  }

  /**
   * Copies all values from the given src object excluding the values created and lastUpdate. Do not overwrite created and lastUpdate from
   * the original database object.
   * @param src
   * @param ignoreFields Does not copy these properties (by field name).
   * @return true, if any modifications are detected, otherwise false;
   */
  public ModificationStatus copyValuesFrom(final BaseDO< ? extends Serializable> src, final String... ignoreFields)
  {
    return copyValues(src, this, ignoreFields);
  }

  /**
   * Copies all values from the given src object excluding the values created and lastUpdate. Do not overwrite created and lastUpdate from
   * the original database object.
   * @param src
   * @param dest
   * @param ignoreFields Does not copy these properties (by field name).
   * @return true, if any modifications are detected, otherwise false;
   */
  @SuppressWarnings("unchecked")
  public static ModificationStatus copyValues(final BaseDO src, final BaseDO dest, final String... ignoreFields)
  {
    if (ClassUtils.isAssignable(src.getClass(), dest.getClass()) == false) {
      throw new RuntimeException("Try to copyValues from different BaseDO classes: this from type "
          + dest.getClass().getName()
          + " and src from type"
          + src.getClass().getName()
          + "!");
    }
    if (src.getId() != null && (ignoreFields == null || ArrayUtils.contains(ignoreFields, "id") == false)) {
      dest.setId(src.getId());
    }
    return copyDeclaredFields(src.getClass(), src, dest, ignoreFields);
  }

  /**
   * 
   * @param srcClazz
   * @param src
   * @param dest
   * @param ignoreFields
   * @return true, if any modifications are detected, otherwise false;
   */
  @SuppressWarnings("unchecked")
  private static ModificationStatus copyDeclaredFields(final Class< ? > srcClazz, final BaseDO< ? > src, final BaseDO< ? > dest,
      final String... ignoreFields)
  {
    final Field[] fields = srcClazz.getDeclaredFields();
    AccessibleObject.setAccessible(fields, true);
    ModificationStatus modificationStatus = null;
    for (final Field field : fields) {
      final String fieldName = field.getName();
      if ((ignoreFields != null && ArrayUtils.contains(ignoreFields, fieldName) == true) || accept(field) == false) {
        continue;
      }
      try {
        final Object srcFieldValue = field.get(src);
        final Object destFieldValue = field.get(dest);
        if (field.getType().isPrimitive() == true) {
          if (ObjectUtils.equals(destFieldValue, srcFieldValue) == false) {
            field.set(dest, srcFieldValue);
            modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
          }
          continue;
        } else if (srcFieldValue == null) {
          if (field.getType() == String.class) {
            if (StringUtils.isNotEmpty((String) destFieldValue) == true) {
              field.set(dest, null);
              modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
            }
          } else if (destFieldValue != null) {
            field.set(dest, null);
            modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
          } else {
            // dest was already null
          }
        } else if (srcFieldValue instanceof Collection) {
          Collection<Object> destColl = (Collection<Object>) destFieldValue;
          final Collection<Object> srcColl = (Collection<Object>) srcFieldValue;
          final Collection<Object> toRemove = new ArrayList<Object>();
          if (srcColl != null && destColl == null) {
            if (srcColl instanceof TreeSet) {
              destColl = new TreeSet<Object>();
            } else if (srcColl instanceof HashSet) {
              destColl = new HashSet<Object>();
            } else if (srcColl instanceof List) {
              destColl = new ArrayList<Object>();
            } else if (srcColl instanceof PersistentSet) {
              destColl = new HashSet<Object>();
            } else {
              log.error("Unsupported collection type: " + srcColl.getClass().getName());
            }
            field.set(dest, destColl);
          }
          for (final Object o : destColl) {
            if (srcColl.contains(o) == false) {
              toRemove.add(o);
            }
          }
          for (final Object o : toRemove) {
            if (log.isDebugEnabled() == true) {
              log.debug("Removing collection entry: " + o);
            }
            destColl.remove(o);
            modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
          }
          for (final Object srcEntry : srcColl) {
            if (destColl.contains(srcEntry) == false) {
              if (log.isDebugEnabled() == true) {
                log.debug("Adding new collection entry: " + srcEntry);
              }
              destColl.add(srcEntry);
              modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
            } else if (srcEntry instanceof BaseDO) {
              final PFPersistancyBehavior behavior = field.getAnnotation(PFPersistancyBehavior.class);
              if (behavior != null && behavior.autoUpdateCollectionEntries() == true) {
                BaseDO< ? > destEntry = null;
                for (final Object entry : destColl) {
                  if (entry.equals(srcEntry) == true) {
                    destEntry = (BaseDO< ? >) entry;
                    break;
                  }
                }
                Validate.notNull(destEntry);
                final ModificationStatus st = destEntry.copyValuesFrom((BaseDO< ? >) srcEntry);
                modificationStatus = getModificationStatus(modificationStatus, st);
              }
            }
          }
        } else if (srcFieldValue instanceof BaseDO) {
          final Serializable srcFieldValueId = HibernateUtils.getIdentifier((BaseDO< ? >) srcFieldValue);
          if (srcFieldValueId != null) {
            if (destFieldValue == null || ObjectUtils.equals(srcFieldValueId, ((BaseDO< ? >) destFieldValue).getId()) == false) {
              field.set(dest, srcFieldValue);
              modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
            }
          } else {
            log.error("Can't get id though can't copy the BaseDO (see error message above about HHH-3502).");
          }
        } else if (srcFieldValue instanceof java.sql.Date) {
          if (destFieldValue == null) {
            field.set(dest, srcFieldValue);
            modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
          } else {
            final DayHolder srcDay = new DayHolder((Date) srcFieldValue);
            final DayHolder destDay = new DayHolder((Date) destFieldValue);
            if (srcDay.isSameDay(destDay) == false) {
              field.set(dest, srcDay.getSQLDate());
              modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
            }
          }
        } else if (srcFieldValue instanceof Date) {
          if (destFieldValue == null || ((Date) srcFieldValue).getTime() != ((Date) destFieldValue).getTime()) {
            field.set(dest, srcFieldValue);
            modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
          }
        } else if (srcFieldValue instanceof BigDecimal) {
          if (destFieldValue == null || ((BigDecimal) srcFieldValue).compareTo((BigDecimal) destFieldValue) != 0) {
            field.set(dest, srcFieldValue);
            modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
          }
        } else if (ObjectUtils.equals(destFieldValue, srcFieldValue) == false) {
          field.set(dest, srcFieldValue);
          modificationStatus = getModificationStatus(modificationStatus, src, fieldName);
        }
      } catch (final IllegalAccessException ex) {
        throw new InternalError("Unexpected IllegalAccessException: " + ex.getMessage());
      }
    }
    final Class< ? > superClazz = srcClazz.getSuperclass();
    if (superClazz != null) {
      final ModificationStatus st = copyDeclaredFields(superClazz, src, dest, ignoreFields);
      modificationStatus = getModificationStatus(modificationStatus, st);
    }
    return modificationStatus;
  }

  protected static ModificationStatus getModificationStatus(final ModificationStatus currentStatus, final BaseDO< ? > src,
      final String modifiedField)
  {
    if (currentStatus == ModificationStatus.MAJOR
        || src instanceof AbstractHistorizableBaseDO == false
        || ((AbstractHistorizableBaseDO< ? >) src).isNonHistorizableAttribute(modifiedField) == false) {
      return ModificationStatus.MAJOR;
    }
    return ModificationStatus.MINOR;
  }

  public static ModificationStatus getModificationStatus(final ModificationStatus currentStatus, final ModificationStatus status)
  {
    if (currentStatus == ModificationStatus.MAJOR || status == ModificationStatus.MAJOR) {
      return ModificationStatus.MAJOR;
    }
    if (currentStatus == ModificationStatus.MINOR || status == ModificationStatus.MINOR) {
      return ModificationStatus.MINOR;
    }
    return ModificationStatus.NONE;
  }

  /**
   * Returns whether or not to append the given <code>Field</code>.
   * <ul>
   * <li>Ignore transient fields
   * <li>Ignore static fields
   * <li>Ignore inner class fields</li>
   * </ul>
   * 
   * @param field The Field to test.
   * @return Whether or not to consider the given <code>Field</code>.
   */
  protected static boolean accept(final Field field)
  {
    if (field.getName().indexOf(ClassUtils.INNER_CLASS_SEPARATOR_CHAR) != -1) {
      // Reject field from inner class.
      return false;
    }
    if (Modifier.isTransient(field.getModifiers()) == true) {
      // transients.
      return false;
    }
    if (Modifier.isStatic(field.getModifiers()) == true) {
      // transients.
      return false;
    }
    if ("created".equals(field.getName()) == true || "lastUpdate".equals(field.getName()) == true) {
      return false;
    }
    return true;
  }
}
