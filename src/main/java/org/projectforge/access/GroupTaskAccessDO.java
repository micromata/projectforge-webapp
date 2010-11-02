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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.projectforge.core.BaseDO;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;


/**
 * Represents an access entry with the permissions of one group to one task. The persistent data object of GroupTaskAccess.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Indexed
@Table(name = "T_GROUP_TASK_ACCESS", uniqueConstraints = { @UniqueConstraint(columnNames = { "group_id", "task_id"})})
public class GroupTaskAccessDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -8819516962428533352L;

  static {
    invalidHistorizableProperties.add("accessEntries");
  }

  @IndexedEmbedded
  private GroupDO group;

  @IndexedEmbedded
  private TaskDO task;
  
  private boolean recursive = true;

  private Set<AccessEntryDO> accessEntries = null;

  /**
   * Returns the specified access.
   * @param accessType TASKS_ACCESS, BUGS_ACCESS, ...
   * @return The specified access or null if not found.
   */
  @Transient
  public AccessEntryDO getAccessEntry(AccessType accessType)
  {
    if (this.accessEntries == null)
      return null;
    for (AccessEntryDO entry : this.accessEntries) {
      if (entry.getAccessType() == accessType) {
        return entry;
      }
    }
    return null;
  }

  @Transient
  public boolean hasPermission(AccessType accessType, OperationType opType)
  {
    AccessEntryDO entry = getAccessEntry(accessType);
    if (entry == null) {
      return false;
    } else {
      return entry.hasPermission(opType);
    }
  }

  /**
   * Get the history entries for this object.
   * 
   */
  @OneToMany(cascade = { CascadeType.ALL}, fetch = FetchType.EAGER)
  @JoinColumn(name = "group_task_access_fk")
  @Cascade(value = { org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.SAVE_UPDATE})
  public Set<AccessEntryDO> getAccessEntries()
  {
    return this.accessEntries;
  }

  @Transient
  public List<AccessEntryDO> getOrderedEntries()
  {
    List<AccessEntryDO> list = new ArrayList<AccessEntryDO>();
    AccessEntryDO entry = getAccessEntry(AccessType.TASK_ACCESS_MANAGEMENT);
    if (entry != null)
      list.add(entry);
    entry = getAccessEntry(AccessType.TASKS);
    if (entry != null)
      list.add(entry);
    entry = getAccessEntry(AccessType.TIMESHEETS);
    if (entry != null)
      list.add(entry);
    entry = getAccessEntry(AccessType.OWN_TIMESHEETS);
    if (entry != null)
      list.add(entry);
    return list;
  }

  public GroupTaskAccessDO setAccessEntries(final Set<AccessEntryDO> col)
  {
    this.accessEntries = col;
    return this;
  }

  public GroupTaskAccessDO addAccessEntry(final AccessEntryDO entry)
  {
    if (this.accessEntries == null) {
      setAccessEntries(new HashSet<AccessEntryDO>());
    }
    this.accessEntries.add(entry);
    return this;
  }

  // @Column(name = "group_id")
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "group_id", nullable = false)
  public GroupDO getGroup()
  {
    return group;
  }

  public GroupTaskAccessDO setGroup(final GroupDO group)
  {
    this.group = group;
    return this;
  }

  // @Column(name = "task_id")
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE}, targetEntity = TaskDO.class)
  @JoinColumn(name = "task_id", nullable = false)
  public TaskDO getTask()
  {
    return task;
  }

  public GroupTaskAccessDO setTask(final TaskDO task)
  {
    this.task = task;
    return this;
  }

  public AccessEntryDO ensureAndGetAccessEntry(AccessType accessType)
  {
    if (this.accessEntries == null) {
      setAccessEntries(new HashSet<AccessEntryDO>());
    }
    AccessEntryDO entry = getAccessEntry(accessType);
    if (entry == null) {
      entry = new AccessEntryDO(accessType);
      this.addAccessEntry(entry);
    }
    return entry;
  }

  @Override
  public boolean equals(Object o)
  {
    if (o instanceof GroupTaskAccessDO) {
      GroupTaskAccessDO other = (GroupTaskAccessDO) o;
      if (ObjectUtils.equals(this.getGroupId(), other.getGroupId()) == false)
        return false;
      if (ObjectUtils.equals(this.getTaskId(), other.getTaskId()) == false)
        return false;
      return true;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(getTaskId());
    hcb.append(getGroupId());
    return hcb.toHashCode();
  }

  @Transient
  public Integer getGroupId()
  {
    if (this.group == null)
      return null;
    return this.group.getId();
  }

  @Transient
  public Integer getTaskId()
  {
    if (this.task == null)
      return null;
    return this.task.getId();
  }

  /**
   * If true then the group rights are also valid for all sub tasks. If false, then each sub task needs its own definition.
   */
  public boolean isRecursive()
  {
    return recursive;
  }
  
  public void setRecursive(boolean recursive)
  {
    this.recursive = recursive;
  }
  
  /**
   * Copies all values from the given src object excluding the values created and modified. Null values will be excluded.
   * @param src
   */
  @Override
  public boolean copyValuesFrom(BaseDO<? extends Serializable> source, String... ignoreFields)
  {
    boolean modified = super.copyValuesFrom(source, ignoreFields);
    GroupTaskAccessDO src = (GroupTaskAccessDO) source;
    if (src.getAccessEntries() != null) {
      for (AccessEntryDO srcEntry : src.getAccessEntries()) {
        AccessEntryDO destEntry = ensureAndGetAccessEntry(srcEntry.getAccessType());
        if (destEntry.copyValuesFrom(srcEntry) == true)
          modified = true;
      }
      Iterator<AccessEntryDO> iterator = getAccessEntries().iterator();
      while (iterator.hasNext()) {
        AccessEntryDO destEntry = iterator.next();
        if (src.getAccessEntry(destEntry.getAccessType()) == null) {
          iterator.remove();
        }
      }
    }
    return modified;
  }

  public String toString()
  {
    ToStringBuilder tos = new ToStringBuilder(this);
    tos.append("id", getId());
    tos.append("task", getTaskId());
    tos.append("group", getGroupId());
    if (Hibernate.isInitialized(this.accessEntries) == true) {
      tos.append("entries", this.accessEntries);
    } else {
      tos.append("entries", "LazyCollection");
    }
    return tos.toString();
  }
  
  @SuppressWarnings("unchecked")
  @Transient
  public Set getHistorizableAttributes()
  {
    return null;
  }
}
