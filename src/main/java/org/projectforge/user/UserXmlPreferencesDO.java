/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.user;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * For persistency of UserPreferencesData (stores them serialized).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Table(name = "T_USER_XML_PREFS", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "key"})})
public class UserXmlPreferencesDO
{
  /**
   * Don't forget to increase, if any changes in the object stored in user data are made. If not, the user preferences will be lost because
   * of unsupported (de)serialization.
   */
  public static final int CURRENT_VERSION = 4;

  private static final long serialVersionUID = -815810269783729566L;

  @Id
  @GeneratedValue
  @Column(name = "pk")
  private Integer id;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(length = 100000)
  private String serializedSettings;

  @Column(length = 1000)
  private String key;

  @Basic
  private Date created;

  @Basic
  @Column(name = "last_update")
  private Date lastUpdate;

  @Column
  private int version;

  public Integer getId()
  {
    return id;
  }
  
  public void setId(Integer id)
  {
    this.id = id;
  }
  
  public Integer getUserId()
  {
    return userId;
  }

  public void setUserId(Integer userId)
  {
    this.userId = userId;
  }
  
  /**
   * Optional if the user preference should be stored in its own data base entry.
   */
  public String getKey()
  {
    return key;
  }
  
  public void setKey(String key)
  {
    this.key = key;
  }

  /**
   * Contains the serialized settings, stored in the database.
   * @return
   */
  public String getSerializedSettings()
  {
    return serializedSettings;
  }

  public void setSerializedSettings(String settings)
  {
    this.serializedSettings = settings;
  }

  public Date getCreated()
  {
    return created;
  }

  public void setCreated(Date created)
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
  public Date getLastUpdate()
  {
    return lastUpdate;
  }

  public void setLastUpdate(Date lastUpdate)
  {
    this.lastUpdate = lastUpdate;
  }

  public void setLastUpdate()
  {
    this.lastUpdate = new Date();
  }

  /**
   * For migrating older entries the version for every entry is given.
   */
  public int getVersion()
  {
    return version;
  }

  public void setVersion(int version)
  {
    this.version = version;
  }

  /**
   * Sets CURRENT_VERSION as version.
   * @see #CURRENT_VERSION
   */
  public void setVersion()
  {
    this.version = CURRENT_VERSION;
  }
}
