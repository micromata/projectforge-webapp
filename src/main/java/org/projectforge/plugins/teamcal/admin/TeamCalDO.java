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

package org.projectforge.plugins.teamcal.admin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.database.Constants;
import org.projectforge.user.PFUserDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Entity
@Indexed
@ClassBridge(name = "usersgroups", index = Index.TOKENIZED, store = Store.NO, impl = HibernateSearchUsersGroupsBridge.class)
@Table(name = "T_PLUGIN_CALENDAR")
public class TeamCalDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 2869432134443084605L;

  // @UserPrefParameter(i18nKey = "plugins.teamcal.subject")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String title;

  @IndexedEmbedded(depth = 1)
  private PFUserDO owner;

  private String fullAccessGroupIds, fullAccessUserIds;

  private String readonlyAccessGroupIds, readonlyAccessUserIds;

  private String minimalAccessGroupIds, minimalAccessUserIds;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  public TeamCalDO()
  {
  }

  /**
   * @return the title
   */
  @Column(length = Constants.LENGTH_TITLE)
  public String getTitle()
  {
    return title;
  }

  /**
   * @param title the title to set
   * @return this for chaining.
   */
  public TeamCalDO setTitle(final String title)
  {
    this.title = title;
    return this;
  }

  /**
   * @return the owner
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_fk")
  public PFUserDO getOwner()
  {
    return owner;
  }

  @Transient
  public Integer getOwnerId()
  {
    return owner != null ? owner.getId() : null;
  }

  /**
   * @param owner the owner to set
   * @return this for chaining.
   */
  public TeamCalDO setOwner(final PFUserDO owner)
  {
    this.owner = owner;
    return this;
  }

  /**
   * Members of these groups have full read/write access to all entries of this calendar.
   * @return the fullAccessGroupIds
   */
  @Column(name = "full_access_group_ids", nullable = true)
  public String getFullAccessGroupIds()
  {
    return fullAccessGroupIds;
  }

  /**
   * These users have full read/write access to all entries of this calendar.
   * @param fullAccessGroupIds the fullAccessGroupIds to set
   * @return this for chaining.
   */
  public TeamCalDO setFullAccessGroupIds(final String fullAccessGroupIds)
  {
    this.fullAccessGroupIds = fullAccessGroupIds;
    return this;
  }

  /**
   * @return the fullAccessUserIds
   */
  @Column(name = "full_access_user_ids", nullable = true)
  public String getFullAccessUserIds()
  {
    return fullAccessUserIds;
  }

  /**
   * @param fullAccessUserIds the fullAccessUserIds to set
   * @return this for chaining.
   */
  public TeamCalDO setFullAccessUserIds(final String fullAccessUserIds)
  {
    this.fullAccessUserIds = fullAccessUserIds;
    return this;
  }

  /**
   * Members of these groups have full read-only access to all entries of this calendar.
   * @return the readonlyAccessGroupIds
   */
  @Column(name = "readonly_access_group_ids", nullable = true)
  public String getReadonlyAccessGroupIds()
  {
    return readonlyAccessGroupIds;
  }

  /**
   * @param readonlyAccessGroupIds the readonlyAccessGroupIds to set
   * @return this for chaining.
   */
  public TeamCalDO setReadonlyAccessGroupIds(final String readonlyAccessGroupIds)
  {
    this.readonlyAccessGroupIds = readonlyAccessGroupIds;
    return this;
  }

  /**
   * These users have full read-only access to all entries of this calendar.
   * @return the readonlyAccessUserIds
   */
  @Column(name = "readonly_access_user_ids", nullable = true)
  public String getReadonlyAccessUserIds()
  {
    return readonlyAccessUserIds;
  }

  /**
   * @param readonlyAccessUserIds the readonlyAccessUserIds to set
   * @return this for chaining.
   */
  public TeamCalDO setReadonlyAccessUserIds(final String readonlyAccessUserIds)
  {
    this.readonlyAccessUserIds = readonlyAccessUserIds;
    return this;
  }

  /**
   * Members of these group have read-only access to all entries of this calendar, but they can only see the event start and stop time
   * @return the minimalAccessGroupIds
   */
  @Column(name = "minimal_access_group_ids", nullable = true)
  public String getMinimalAccessGroupIds()
  {
    return minimalAccessGroupIds;
  }

  /**
   * @param minimalAccessGroupIds the minimalAccessGroupIds to set
   * @return this for chaining.
   */
  public TeamCalDO setMinimalAccessGroupIds(final String minimalAccessGroupIds)
  {
    this.minimalAccessGroupIds = minimalAccessGroupIds;
    return this;
  }

  /**
   * Members of this group have only access to the start and stop time, nothing else.
   * @return the minimalAccessUserIds
   */
  @Column(name = "minimal_access_user_ids", nullable = true)
  public String getMinimalAccessUserIds()
  {
    return minimalAccessUserIds;
  }

  /**
   * @param minimalAccessUserIds the minimalAccessUserIds to set
   * @return this for chaining.
   */
  public TeamCalDO setMinimalAccessUserIds(final String minimalAccessUserIds)
  {
    this.minimalAccessUserIds = minimalAccessUserIds;
    return this;
  }

  @Column(length = Constants.LENGTH_TEXT)
  public String getDescription()
  {
    return description;
  }

  /**
   * @return this for chaining.
   */
  public TeamCalDO setDescription(final String description)
  {
    this.description = description;
    return this;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final HashCodeBuilder hcb = new HashCodeBuilder().append(this.getId());
    return hcb.hashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    final int id = this.getId();
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final TeamCalDO other = (TeamCalDO) obj;
    if (id != other.getId())
      return false;
    return true;
  }
}
