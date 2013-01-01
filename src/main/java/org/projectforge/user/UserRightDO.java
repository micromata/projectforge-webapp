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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.projectforge.core.DefaultBaseDO;

@Entity
@Indexed
@Table(name = "T_USER_RIGHT", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_fk", "right_id"})})
public class UserRightDO extends DefaultBaseDO implements Comparable<UserRightDO>, Serializable
{
  private static final long serialVersionUID = 6703048743393453733L;

  @IndexedEmbedded(depth = 2)
  private UserRightId rightId;

  @Field(index = Index.UN_TOKENIZED)
  protected UserRightValue value;

  @IndexedEmbedded(depth = 1)
  protected PFUserDO user;

  public UserRightDO()
  {

  }

  public UserRightDO(final UserRightId rightId)
  {
    this(null, rightId, null);
  }

  public UserRightDO(final UserRightId rightId, final UserRightValue value)
  {
    this(null, rightId, value);
  }

  public UserRightDO(final PFUserDO user, final UserRightId rightId)
  {
    this(user, rightId, null);
  }

  public UserRightDO(final PFUserDO user, final UserRightId rightId, final UserRightValue value)
  {
    this.user = user;
    this.rightId = rightId;
    this.value = value;
  }

  @Transient
  public UserRightId getRightId()
  {
    return rightId;
  }

  public UserRightDO setRightId(final UserRightId rightId)
  {
    this.rightId = rightId;
    return this;
  }

  /**
   * Only for storing the right id in the data base.
   */
  @Column(name = "right_id", length = 40, nullable = false)
  public String getRightIdString()
  {
    return rightId != null ? rightId.getId() : null;
  }

  /**
   * Only for resstoring the right id from the data base.
   */
  public void setRightIdString(final String rightId)
  {
    this.rightId = rightId != null ? UserRights.instance().getRightId(rightId) : null;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 40)
  public UserRightValue getValue()
  {
    return value;
  }

  public UserRightDO setValue(final UserRightValue value)
  {
    this.value = value;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_fk", nullable = false)
  public PFUserDO getUser()
  {
    return user;
  }

  @Transient
  public Integer getUserId()
  {
    if (this.user == null)
      return null;
    return user.getId();
  }

  public UserRightDO setUser(PFUserDO user)
  {
    this.user = user;
    return this;
  }

  /*
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(final UserRightDO o)
  {
    return this.rightId.compareTo(o.rightId);
  }

  @Override
  public boolean equals(final Object o)
  {
    if (o instanceof UserRightDO) {
      final UserRightDO other = (UserRightDO) o;
      if (ObjectUtils.equals(this.getRightId(), other.getRightId()) == false)
        return false;
      if (ObjectUtils.equals(this.getId(), other.getId()) == false)
        return false;
      return true;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    HashCodeBuilder hcb = new HashCodeBuilder();
    if (getRightId() != null)
      hcb.append(getRightId().hashCode());
    hcb.append(getId());
    return hcb.toHashCode();
  }

  public String toString()
  {
    ToStringBuilder sb = new ToStringBuilder(this);
    sb.append("id", getId());
    sb.append("userId", this.getUserId());
    sb.append("rightId", this.rightId);
    sb.append("value", this.value);
    return sb.toString();
  }
}
