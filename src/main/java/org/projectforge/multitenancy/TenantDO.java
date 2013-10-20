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

package org.projectforge.multitenancy;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDO;
import org.projectforge.core.ModificationStatus;

/**
 * Represents a single tenant (client) for multi-tenancy.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Table(name = "T_TENANT")
public class TenantDO implements BaseDO<Integer>, Serializable
{
  private static final long serialVersionUID = -2242576370698028282L;

  private Integer id;

  private String name;

  private String description;

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

  /**
   * @return the name
   */
  @Column(length = 100)
  public String getName()
  {
    return name;
  }

  /**
   * @param name the name to set
   * @return this for chaining.
   */
  public TenantDO setName(final String name)
  {
    this.name = name;
    return this;
  }

  /**
   * @return the description
   */
  @Column(length = 4000)
  public String getDescription()
  {
    return description;
  }

  /**
   * @param description the description to set
   * @return this for chaining.
   */
  public TenantDO setDescription(final String description)
  {
    this.description = description;
    return this;
  }

  /**
   * Returns this.
   * @see org.projectforge.core.BaseDO#getTenant()
   */
  @Transient
  @Override
  public TenantDO getTenant()
  {
    return this;
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   * @see org.projectforge.core.BaseDO#setTenant(TenantDO)
   */
  @Override
  public BaseDO<Integer> setTenant(final TenantDO tenant)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   * @see org.projectforge.core.BaseDO#isMinorChange()
   */
  @Override
  public boolean isMinorChange()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   * @see org.projectforge.core.BaseDO#setMinorChange(boolean)
   */
  @Override
  public void setMinorChange(final boolean value)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   * @see org.projectforge.core.BaseDO#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute(final String key)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   * @see org.projectforge.core.BaseDO#setAttribute(java.lang.String, java.lang.Object)
   */
  @Override
  public void setAttribute(final String key, final Object value)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.core.BaseDO#copyValuesFrom(org.projectforge.core.BaseDO, java.lang.String[])
   */
  @Override
  public ModificationStatus copyValuesFrom(final BaseDO< ? extends Serializable> src, final String... ignoreFields)
  {
    return AbstractBaseDO.copyValues(src, this, ignoreFields);
  }
}
