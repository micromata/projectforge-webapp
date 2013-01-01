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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.Session;
import org.projectforge.common.DateHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.HistoryEntryType;
import de.micromata.hibernate.history.delta.CollectionPropertyDelta;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * For storing the hibernate history entries in flat format.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DisplayHistoryEntry implements Serializable
{
  private static final long serialVersionUID = 3900345445639438747L;

  private PFUserDO user;

  private final HistoryEntryType entryType;

  private String propertyName;

  private String propertyType;

  private String oldValue;

  private String newValue;

  private final Timestamp timestamp;

  public DisplayHistoryEntry(final UserGroupCache userCache, final HistoryEntry entry)
  {
    this.timestamp = entry.getTimestamp();
    final Integer userId = NumberHelper.parseInteger(entry.getUserName());
    if (userId != null) {
      this.user = userCache.getUser(userId);
    }
    // entry.getClassName();
    // entry.getComment();
    this.entryType = entry.getType();
    // entry.getEntityId();
  }

  private PFUserDO getUser(final UserGroupCache userCache, final String userId)
  {
    if (StringUtils.isBlank(userId) == true) {
      return null;
    }
    final Integer id = NumberHelper.parseInteger(userId);
    if (id == null) {
      return null;
    }
    return userCache.getUser(id);
  }

  public DisplayHistoryEntry(final UserGroupCache userCache, final HistoryEntry entry, final PropertyDelta prop, final Session session)
  {
    this(userCache, entry);
    this.propertyType = prop.getPropertyType();
    Object oldObjectValue = null;
    Object newObjectValue = null;
    if (PFUserDO.class.getName().equals(this.propertyType) == true) {
      PFUserDO user = getUser(userCache, prop.getOldValue());
      if (user != null) {
        oldObjectValue = user;
      }
      user = getUser(userCache, prop.getNewValue());
      if (user != null) {
        newObjectValue = user;
      }
    }
    if (oldObjectValue == null) {
      oldObjectValue = prop.getOldObjectValue(session);
    }
    if (newObjectValue == null) {
      newObjectValue = prop.getNewObjectValue(session);
    }
    final String propType = prop.getPropertyType();
    if (prop instanceof CollectionPropertyDelta) {
      this.oldValue = String.valueOf(toShortNameOfList(oldObjectValue));
      this.newValue = String.valueOf(toShortNameOfList(newObjectValue));
    } else if ("java.util.Date".equals(propType) == true
        || "java.sql.Date".equals(propType) == true
        || "java.sql.Timestamp".equals(propType) == true) {
      this.oldValue = formatDate(oldObjectValue);
      this.newValue = formatDate(newObjectValue);
    } else {
      this.oldValue = toShortName(oldObjectValue);
      this.newValue = toShortName(newObjectValue);
    }
    this.propertyName = prop.getPropertyName();
  }

  private String formatDate(final Object objectValue)
  {
    if (objectValue == null) {
      return "";
    }
    if (objectValue instanceof java.sql.Date) {
      return DateHelper.formatIsoDate((Date) objectValue);
    } else if (objectValue instanceof Date) {
      return DateHelper.formatIsoTimestamp((Date)objectValue);
    }
    return String.valueOf(objectValue);
  }

  private Object toShortNameOfList(final Object value)
  {
    if (value instanceof Collection<?>) {
      return CollectionUtils.collect((Collection< ? >) value, new Transformer() {
        public Object transform(final Object input)
        {
          return toShortName(input);
        }
      });
    }
    return value;
  }

  String toShortName(final Object object)
  {
    return String.valueOf(object instanceof ShortDisplayNameCapable ? ((ShortDisplayNameCapable) object).getShortDisplayName() : object);
  }

  /**
   * @return the entryType
   */
  public HistoryEntryType getEntryType()
  {
    return entryType;
  }

  /**
   * @return the newValue
   */
  public String getNewValue()
  {
    return newValue;
  }

  /**
   * @param newValue the newValue to set
   * @return this for chaining.
   */
  public void setNewValue(final String newValue)
  {
    this.newValue = newValue;
  }

  /**
   * @return the oldValue
   */
  public String getOldValue()
  {
    return oldValue;
  }

  /**
   * @param oldValue the oldValue to set
   * @return this for chaining.
   */
  public void setOldValue(final String oldValue)
  {
    this.oldValue = oldValue;
  }

  /**
   * @return the propertyName
   */
  public String getPropertyName()
  {
    return propertyName;
  }

  /**
   * Use-full for prepending id of childs (e. g. entries in a collection displayed in the history table of the parent object). Example: AuftragDO -> AuftragsPositionDO.
   * @param propertyName
   */
  public void setPropertyName(final String propertyName)
  {
    this.propertyName = propertyName;
  }

  /**
   * @return the propertyType
   */
  public String getPropertyType()
  {
    return propertyType;
  }

  public PFUserDO getUser()
  {
    return user;
  }

  public Timestamp getTimestamp()
  {
    return timestamp;
  }

  /**
   * Returns string containing all fields (except the password, via ReflectionToStringBuilder).
   * @return
   */
  @Override
  public String toString()
  {
    return new ReflectionToStringBuilder(this).toString();
  }
}
