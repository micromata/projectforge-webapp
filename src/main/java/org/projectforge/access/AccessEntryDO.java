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

package org.projectforge.access;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDO;


/**
 * Represents a single generic access entry for the four main SQL functionalities.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Table(name = "T_GROUP_TASK_ACCESS_ENTRY", uniqueConstraints = { @UniqueConstraint(columnNames = { "group_task_access_fk", "access_type"})})
public class AccessEntryDO implements Comparable<AccessEntryDO>, Serializable, BaseDO<Integer>
{
  private static final long serialVersionUID = 5973002212430487361L;

  // private static final Logger log = Logger.getLogger(AccessEntryDO.class);

  private AccessType accessType = null;

  private boolean accessSelect = false;

  private boolean accessInsert = false;

  private boolean accessUpdate = false;

  private boolean accessDelete = false;

  private Integer id;

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
  
  /**
   * @return Always false.
   * @see org.projectforge.core.BaseDO#isMinorChange()
   */
  @Transient
  public boolean isMinorChange()
  {
    return false;
  }

  /**
   * Throws UnsupportedOperationException.
   * @see org.projectforge.core.BaseDO#setMinorChange(boolean)
   */
  public void setMinorChange(boolean value)
  {
    throw new UnsupportedOperationException();
  }

  public AccessEntryDO()
  {
  }

  public AccessEntryDO(AccessType accessType)
  {
    this.accessType = accessType;
  }

  public AccessEntryDO(AccessType type, boolean accessSelect, boolean accessInsert, boolean accessUpdate, boolean accessDelete)
  {
    this.accessType = type;
    setAccess(accessSelect, accessInsert, accessUpdate, accessDelete);
  }

  public boolean hasPermission(OperationType opType)
  {
    if (opType == OperationType.SELECT) {
      return this.accessSelect;
    } else if (opType == OperationType.INSERT) {
      return this.accessInsert;
    } else if (opType == OperationType.UPDATE) {
      return this.accessUpdate;
    } else {
      return this.accessDelete;
    }
  }

  /**
   */
  @Column(name = "access_type")
  @Enumerated(EnumType.STRING)
  public AccessType getAccessType()
  {
    return this.accessType;
  }

  public void setAccessType(AccessType type)
  {
    this.accessType = type;
  }

  public void setAccess(boolean accessSelect, boolean accessInsert, boolean accessUpdate, boolean accessDelete)
  {
    this.accessSelect = accessSelect;
    this.accessInsert = accessInsert;
    this.accessUpdate = accessUpdate;
    this.accessDelete = accessDelete;
  }

  /**
   */
  @Column(name = "access_select")
  public boolean getAccessSelect()
  {
    return this.accessSelect;
  }

  public void setAccessSelect(boolean value)
  {
    this.accessSelect = value;
  }

  @Column(name = "access_insert")
  public boolean getAccessInsert()
  {
    return this.accessInsert;
  }

  public void setAccessInsert(boolean value)
  {
    this.accessInsert = value;
  }

  @Column(name = "access_update")
  public boolean getAccessUpdate()
  {
    return this.accessUpdate;
  }

  public void setAccessUpdate(boolean value)
  {
    this.accessUpdate = value;
  }

  @Column(name = "access_delete")
  public boolean getAccessDelete()
  {
    return this.accessDelete;
  }

  public void setAccessDelete(boolean value)
  {
    this.accessDelete = value;
  }

  /*
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(AccessEntryDO o)
  {
    return this.accessType.compareTo(o.accessType);
  }

  @Override
  public boolean equals(Object o)
  {
    if (o instanceof AccessEntryDO) {
      AccessEntryDO other = (AccessEntryDO) o;
      if (ObjectUtils.equals(this.getAccessType(), other.getAccessType()) == false)
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
    if (getAccessType() != null)
      hcb.append(getAccessType().ordinal());
    hcb.append(getId());
    return hcb.toHashCode();
  }

  public String toString()
  {
    ToStringBuilder sb = new ToStringBuilder(this);
    sb.append("id", getId());
    sb.append("type", this.accessType);
    sb.append("select", this.accessSelect);
    sb.append("insert", this.accessInsert);
    sb.append("update", this.accessUpdate);
    sb.append("delete", this.accessDelete);
    return sb.toString();
  }

  /**
   * Copies the values accessSelect, accessInsert, accessUpdate and accessDelete from the given src object excluding the values created and
   * modified. Null values will be excluded.
   * @param src
   */
  public boolean copyValuesFrom(BaseDO<? extends Serializable> src, String... ignoreFields)
  {
    return AbstractBaseDO.copyValues(src, this, ignoreFields);
  }

  public Object getAttribute(String key)
  {
    throw new UnsupportedOperationException();
  }

  public void setAttribute(String key, Object value)
  {
    throw new UnsupportedOperationException();
  }
}
