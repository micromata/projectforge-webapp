/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.database.Constants;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_CALENDAR")
public class TeamCalDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 2869432134443084605L;

  //  @UserPrefParameter(i18nKey = "plugins.teamcal.subject")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String title;

  @IndexedEmbedded(depth = 1)
  private PFUserDO owner;

  @IndexedEmbedded(depth = 1)
  private GroupDO fullAccessGroup;

  @IndexedEmbedded(depth = 1)
  private GroupDO readOnlyAccessGroup;

  @IndexedEmbedded(depth = 1)
  private GroupDO minimalAccessGroup;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  public TeamCalDO(){
  }

  @Column(length = Constants.LENGTH_TITLE)
  /**
   * @return the title
   */
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
   * Members of this group have full read/write access to all entries of this calendar.
   * @return the fullAccessGroup
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "full_access_group_id", nullable = true)
  public GroupDO getFullAccessGroup()
  {
    return fullAccessGroup;
  }

  @Transient
  public void setNewTeamCalsubscription(final PFUserDO user, final TeamCalDO teamcal) {

  }

  /**
   * @param fullAccessGroup the fullAccessGroup to set
   * @return this for chaining.
   */
  public TeamCalDO setFullAccessGroup(final GroupDO fullAccessGroup)
  {
    this.fullAccessGroup = fullAccessGroup;
    return this;
  }

  @Transient
  public Integer getFullAccessGroupId()
  {
    return this.fullAccessGroup != null ? fullAccessGroup.getId() : null;
  }

  /**
   * Members of this group have full read-only access to all entries of this calendar.
   * @return the readOnlyAccessGroup
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "readOnly_access_group_id", nullable = true)
  public GroupDO getReadOnlyAccessGroup()
  {
    return readOnlyAccessGroup;
  }

  /**
   * @param readOnlyAccessGroup the readOnlyAccessGroup to set
   * @return this for chaining.
   */
  public TeamCalDO setReadOnlyAccessGroup(final GroupDO readOnlyAccessGroup)
  {
    this.readOnlyAccessGroup = readOnlyAccessGroup;
    return this;
  }

  @Transient
  public Integer getReadOnlyAccessGroupId()
  {
    return this.readOnlyAccessGroup != null ? readOnlyAccessGroup.getId() : null;
  }

  /**
   * Members of this group have read-only access to all entries of this calendar, but they can only see the event start and stop time
   * (perhaps including the location).
   * @return the minimalAccessGroup
   */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "minimal_access_group_id", nullable = true)
  public GroupDO getMinimalAccessGroup()
  {
    return minimalAccessGroup;
  }

  /**
   * @param minimalAccessGroup the minimalAccessGroup to set
   * @return this for chaining.
   */
  public TeamCalDO setMinimalAccessGroup(final GroupDO minimalAccessGroup)
  {
    this.minimalAccessGroup = minimalAccessGroup;
    return this;
  }

  @Transient
  public Integer getMinimalAccessGroupId()
  {
    return this.minimalAccessGroup != null ? minimalAccessGroup.getId() : null;
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
    final int prime = 31;
    int result = 1;
    result = prime * result + this.getId();
    return result;
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
