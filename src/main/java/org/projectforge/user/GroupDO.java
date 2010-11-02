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

package org.projectforge.user;

import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.common.StringHelper;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.ShortDisplayNameCapable;


/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_GROUP", uniqueConstraints = { @UniqueConstraint(columnNames = { "name"})})
public class GroupDO extends DefaultBaseDO implements ShortDisplayNameCapable
{
  // private static final Logger log = Logger.getLogger(GroupDO.class);

  private static final long serialVersionUID = 5044537226571167954L;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String name;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String organization;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  private String usernames;

  @IndexedEmbedded(depth = 1)
  private Set<PFUserDO> assignedUsers;

  @ManyToMany(targetEntity = org.projectforge.user.PFUserDO.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
  @JoinTable(name = "T_GROUP_USER", joinColumns = { @JoinColumn(name = "GROUP_ID")}, inverseJoinColumns = { @JoinColumn(name = "USER_ID")})
  public Set<PFUserDO> getAssignedUsers()
  {
    return assignedUsers;
  }

  /**
   * Returns the collection of assigned users only if initialized. Avoids a LazyInitializationException.
   * @return
   */
  @Transient
  public Set<PFUserDO> getSafeAssignedUsers()
  {
    if (this.assignedUsers == null || Hibernate.isInitialized(this.assignedUsers) == false) {
      return null;
    }
    return this.assignedUsers;
  }

  @Transient
  public String getUsernames()
  {
    if (usernames != null) {
      return usernames;
    }
    if (getSafeAssignedUsers() == null) {
      return "";
    }
    List<String> list = new ArrayList<String>();
    for (PFUserDO user : getAssignedUsers()) {
      if (user != null) {
        list.add(user.getUsername());
      }
    }
    usernames = StringHelper.listToString(list, ", ", true);
    return usernames;
  }

  public void setAssignedUsers(Set<PFUserDO> assignedUsers)
  {
    this.assignedUsers = assignedUsers;
  }

  @Column(length = 1000)
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  @Column(length = 100)
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @Column(length = 100)
  public String getOrganization()
  {
    return organization;
  }

  public void setOrganization(String organization)
  {
    this.organization = organization;
  }

  @Override
  public String toString()
  {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("name", getName());
    builder.append("organization", getOrganization());
    builder.append("description", getDescription());
    if (getSafeAssignedUsers() != null) {
      builder.append("assignedUsers", this.assignedUsers);
    } else {
      builder.append("assignedUsers", "LazyCollection");
    }
    return builder.toString();
  }

  @Override
  public boolean equals(Object o)
  {
    if (o instanceof GroupDO) {
      GroupDO other = (GroupDO) o;
      if (ObjectUtils.equals(this.getName(), other.getName()) == false)
        return false;
      return true;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(this.getName());
    return hcb.toHashCode();
  }

  @Transient
  public String getShortDisplayName()
  {
    return this.getName();
  }
}
