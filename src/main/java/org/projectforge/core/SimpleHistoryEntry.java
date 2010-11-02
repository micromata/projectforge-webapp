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

package org.projectforge.core;

import java.sql.Timestamp;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.projectforge.common.NumberHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.HistoryEntryType;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * For storing the hibernate history entries in flat format.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SimpleHistoryEntry
{
  private PFUserDO user;
  
  private HistoryEntryType entryType;
  
  private String propertyName;
  
  private String propertyType;
  
  private String oldValue;
  
  private String newValue;
  
  private Timestamp timestamp;
    
  public SimpleHistoryEntry(UserGroupCache userCache, HistoryEntry entry)
  {
    this.timestamp = entry.getTimestamp();
    Integer userId = NumberHelper.parseInteger(entry.getUserName());
    if (userId != null) {
      this.user = userCache.getUser(userId);
    }
    // entry.getClassName();
    // entry.getComment();
    this.entryType = entry.getType();
    // entry.getEntityId();
  }

  public SimpleHistoryEntry(UserGroupCache userCache, HistoryEntry entry, PropertyDelta prop)
  {
    this(userCache, entry);
    this.propertyType = prop.getPropertyType();
    this.oldValue = prop.getOldValue();
    this.newValue = prop.getNewValue();
    this.propertyName = prop.getPropertyName();
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
   * @return the oldValue
   */
  public String getOldValue()
  {
    return oldValue;
  }

  /**
   * @return the propertyName
   */
  public String getPropertyName()
  {
    return propertyName;
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
  public String toString()
  {
    return new ReflectionToStringBuilder(this).toString();
  }
}
