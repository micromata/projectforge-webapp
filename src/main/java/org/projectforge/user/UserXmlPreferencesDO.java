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

package org.projectforge.user;

import java.io.Serializable;
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
public class UserXmlPreferencesDO implements Serializable
{
  public static final int MAX_SERIALIZED_LENGTH = 10000;

  private static final long serialVersionUID = 3203177155834463761L;

  /**
   * Don't forget to increase, if any changes in the object stored in user data are made. If not, the user preferences will be lost because
   * of unsupported (de)serialization.
   */
  public static final int CURRENT_VERSION = 4;

  private Integer id;

  private Integer userId;

  private String serializedSettings;

  private String key;

  private Date created;

  private Date lastUpdate;

  private Integer version;

  @Id
  @GeneratedValue
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  public void setId(final Integer id)
  {
    this.id = id;
  }

  @Column(name = "user_id", nullable = false)
  public Integer getUserId()
  {
    return userId;
  }

  public void setUserId(final Integer userId)
  {
    this.userId = userId;
  }

  /**
   * Optional if the user preference should be stored in its own data base entry.
   */
  @Column(length = 1000)
  public String getKey()
  {
    return key;
  }

  public void setKey(final String key)
  {
    this.key = key;
  }

  /**
   * Contains the serialized settings, stored in the database.
   * @return
   */
  @Column(length = MAX_SERIALIZED_LENGTH)
  public String getSerializedSettings()
  {
    return serializedSettings;
  }

  public void setSerializedSettings(final String settings)
  {
    this.serializedSettings = settings;
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
   * For migrating older entries the version for every entry is given.
   */
  public int getVersion()
  {
    return version;
  }

  public void setVersion(final int version)
  {
    this.version = version;
  }

  /**
   * Sets CURRENT_VERSION as version.
   * @see #CURRENT_VERSION
   */
  @Column
  public void setVersion()
  {
    this.version = CURRENT_VERSION;
  }
}
