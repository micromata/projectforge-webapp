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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.user.PFUserDO;

/**
 * Represents a single tenant (client) for multi-tenancy.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Table(name = "T_TENANT")
public class TenantDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -2242576370698028282L;

  private String shortName;

  private String name;

  private String description;

  private Boolean defaultTenant;

  @ContainedIn
  @IndexedEmbedded(depth = 1)
  private Set<PFUserDO> assignedUsers;

  @ManyToMany(targetEntity = org.projectforge.user.PFUserDO.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
  @JoinTable(name = "T_TENANT_USER", joinColumns = @JoinColumn(name = "TENANT_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
  public Set<PFUserDO> getAssignedUsers()
  {
    return assignedUsers;
  }

  public void setAssignedUsers(final Set<PFUserDO> assignedUsers)
  {
    this.assignedUsers = assignedUsers;
  }

  public void addUser(final PFUserDO user)
  {
    if (this.assignedUsers == null) {
      this.assignedUsers = new HashSet<PFUserDO>();
    }
    this.assignedUsers.add(user);
  }

  /**
   * No or only one default tenant should be exist. All entities in the database without a given tenant_id are automatically assigned to
   * this tenant. This feature should only be used for ProjectForge installations migrated from single tenant into a multi tenancy
   * installation.
   * @return the defaultTenant
   */
  @Column(name = "default_tenant")
  public Boolean getDefaultTenant()
  {
    return defaultTenant;
  }

  /**
   * @return the defaultTenant
   */
  @Transient
  public boolean isDefaultTenant()
  {
    return defaultTenant != null && defaultTenant == true;
  }

  /**
   * @param defaultTenant the defaultTenant to set
   * @return this for chaining.
   */
  public TenantDO setDefaultTenant(final Boolean defaultTenant)
  {
    this.defaultTenant = defaultTenant;
    return this;
  }

  /**
   * The short name is the display name.
   * @return the shortName
   */
  @Column(length = 100)
  public String getShortName()
  {
    return shortName;
  }

  /**
   * @param shortName the shortName to set
   * @return this for chaining.
   */
  public TenantDO setShortName(final String shortName)
  {
    this.shortName = shortName;
    return this;
  }

  /**
   * @return the name
   */
  @Column(length = 255)
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
   * @param tenant Parameter will be ignored, this is used as tenant to set instead.
   * @see org.projectforge.core.BaseDO#setTenant(TenantDO)
   */
  @Override
  public TenantDO setTenant(final TenantDO tenant)
  {
    super.setTenant(this);
    return this;
  }
}
