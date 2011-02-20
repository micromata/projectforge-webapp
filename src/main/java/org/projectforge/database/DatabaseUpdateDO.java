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

package org.projectforge.database;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.projectforge.Version;
import org.projectforge.core.BaseDO;

/**
 * Represents data-base updates of ProjectForge core and plugins.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Table(name = "t_database_update")
public class DatabaseUpdateDO implements BaseDO<Integer>
{
  private Integer id;

  private Date date;

  private Version version;

  private String plugin;

  private String description;

  @Id
  @GeneratedValue
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  public void setId(Integer id)
  {
    this.id = (Integer) id;
  }

  @Column(name = "version", length = 15)
  public String getVersionString()
  {
    return version != null ? version.toString() : null;
  }

  /**
   * @param version
   * @return this for chaining.
   */
  public DatabaseUpdateDO setVersionString(final String versionString)
  {
    if (versionString == null) {
      version = null;
    } else {
      version = new Version(versionString);
    }
    return this;
  }

  public Date getDate()
  {
    return date;
  }

  /**
   * @param date
   * @return this for chaining.
   */
  public DatabaseUpdateDO setDate(final Date date)
  {
    this.date = date;
    return this;
  }

  @Column(length = 255)
  public String getPlugin()
  {
    return plugin;
  }

  /**
   * @param plugin
   * @return this for chaining.
   */
  public DatabaseUpdateDO setPlugin(final String plugin)
  {
    this.plugin = plugin;
    return this;
  }

  @Column(length = 4000)
  public String getDescription()
  {
    return description;
  }

  /**
   * @param description
   * @return this for chaining.
   */
  public DatabaseUpdateDO setDescription(final String description)
  {
    this.description = description;
    return this;
  }

  /**
   * @return Always false.
   * @see org.projectforge.core.BaseDO#isMinorChange()
   */
  @Transient
  @Override
  public boolean isMinorChange()
  {
    return false;
  }

  /**
   * Throws UnsupportedOperationException.
   * @see org.projectforge.core.BaseDO#setMinorChange(boolean)
   */
  @Override
  public void setMinorChange(boolean value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean copyValuesFrom(BaseDO< ? extends Serializable> src, String... ignoreFields)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getAttribute(String key)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttribute(String key, Object value)
  {
    throw new UnsupportedOperationException();
  }
}
