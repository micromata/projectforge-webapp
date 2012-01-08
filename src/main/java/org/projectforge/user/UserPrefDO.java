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

package org.projectforge.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.common.StringHelper;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDO;

/**
 * Stores preferences of the user for any objects such as list filters or templates for adding new objects (time sheets etc.).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_USER_PREF", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_fk", "area", "name"})})
public class UserPrefDO extends AbstractBaseDO<Integer>
{
  private static final long serialVersionUID = -7752620237173115542L;

  @IndexedEmbedded(depth = 1)
  private PFUserDO user;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String name; // 255 not null

  private UserPrefArea area; // 20;

  private Set<UserPrefEntryDO> prefEntries;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
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

  @Column(length = 255, nullable = false)
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_fk", nullable = false)
  public PFUserDO getUser()
  {
    return user;
  }

  public void setUser(PFUserDO user)
  {
    this.user = user;
  }

  @Transient
  public UserPrefArea getArea()
  {
    return area;
  }

  public void setArea(UserPrefArea area)
  {
    this.area = area;
  }

  /**
   * Only for storing the user pref area in the data base.
   */
  @Column(name = "area", length = UserPrefArea.MAX_ID_LENGTH, nullable = false)
  public String getAreaString()
  {
    return area != null ? area.getId() : null;
  }

  /**
   * Only for restoring the user pref area from the data base.
   */
  public void setAreaString(final String areaId)
  {
    this.area = areaId != null ? UserPrefAreaRegistry.instance().getEntry(areaId) : null;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "user_pref_fk")
  public Set<UserPrefEntryDO> getUserPrefEntries()
  {
    return this.prefEntries;
  }

  @Transient
  public Set<UserPrefEntryDO> getSortedUserPrefEntries()
  {
    final SortedSet<UserPrefEntryDO> result = new TreeSet<UserPrefEntryDO>(new Comparator<UserPrefEntryDO>() {
      public int compare(UserPrefEntryDO o1, UserPrefEntryDO o2)
      {
        return StringHelper.compareTo(o1.orderString, o2.orderString);
      }
    });
    result.addAll(this.prefEntries);
    return result;
  }

  public void setUserPrefEntries(Set<UserPrefEntryDO> userPrefEntries)
  {
    this.prefEntries = userPrefEntries;
  }

  public void addUserPrefEntry(final UserPrefEntryDO userPrefEntry)
  {
    if (this.prefEntries == null) {
      this.prefEntries = new HashSet<UserPrefEntryDO>();
    }
    this.prefEntries.add(userPrefEntry);
  }

  /**
   * Copies all values from the given src object excluding the values created and modified. Null values will be excluded.
   * @param src
   */
  @Override
  public boolean copyValuesFrom(BaseDO< ? extends Serializable> source, String... ignoreFields)
  {
    boolean modified = super.copyValuesFrom(source, ignoreFields);
    final UserPrefDO src = (UserPrefDO) source;
    if (src.getUserPrefEntries() != null) {
      for (final UserPrefEntryDO srcEntry : src.getUserPrefEntries()) {
        final UserPrefEntryDO destEntry = ensureAndGetAccessEntry(srcEntry.getParameter());
        if (destEntry.copyValuesFrom(srcEntry) == true)
          modified = true;
      }
      final Iterator<UserPrefEntryDO> iterator = getUserPrefEntries().iterator();
      while (iterator.hasNext()) {
        final UserPrefEntryDO destEntry = iterator.next();
        if (src.getUserPrefEntry(destEntry.getParameter()) == null) {
          iterator.remove();
        }
      }
    }
    return modified;
  }

  public UserPrefEntryDO ensureAndGetAccessEntry(final String parameter)
  {
    if (this.prefEntries == null) {
      setUserPrefEntries(new TreeSet<UserPrefEntryDO>());
    }
    UserPrefEntryDO entry = getUserPrefEntry(parameter);
    if (entry == null) {
      entry = new UserPrefEntryDO();
      entry.setParameter(parameter);
      this.addUserPrefEntry(entry);
    }
    return entry;
  }

  @Transient
  public UserPrefEntryDO getUserPrefEntry(final String parameter)
  {
    if (this.prefEntries == null)
      return null;
    for (final UserPrefEntryDO entry : this.prefEntries) {
      if (entry.getParameter().equals(parameter) == true) {
        return entry;
      }
    }
    return null;
  }

  /**
   * @param parameter
   * @return A list of all parameters which depends on the given parameter or null if no dependent parameter exists for this parameter.
   */
  public List<UserPrefEntryDO> getDependentUserPrefEntries(final String parameter)
  {
    List<UserPrefEntryDO> list = null;
    for (final UserPrefEntryDO entry : this.prefEntries) {
      if (parameter.equals(entry.dependsOn) == true) {
        if (list == null) {
          list = new ArrayList<UserPrefEntryDO>();
        }
        list.add(entry);
      }
    }
    return list;
  }
}
