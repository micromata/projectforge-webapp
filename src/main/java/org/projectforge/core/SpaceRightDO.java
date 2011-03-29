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

package org.projectforge.core;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.common.LabelValueBean;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;

/**
 * Defines a named right for a space for an user or group.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_SPACE_RIGHT", uniqueConstraints = { @UniqueConstraint(columnNames = { "identifier", "user_fk", "group_fk", "space_fk"})})
public class SpaceRightDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -4427113590601129869L;

  @IndexedEmbedded(depth = 1)
  private SpaceDO space;

  @IndexedEmbedded(depth = 1)
  private PFUserDO user;

  @IndexedEmbedded(depth = 1)
  private GroupDO group;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String identifier;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String value;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  private transient List<LabelValueBean<String, String>> rights;

  /**
   * Examples: 'PF', 'ACME-WEB-PORTAL', ...
   * @return A unique identifier of the space (with upper case letters). This identifier should be as short as possible.
   */
  @Column(length = 100, unique = true, nullable = false)
  public String getIdentifier()
  {
    return identifier;
  }

  /**
   * Depending on the right this could be a single value such as a boolean value (e. g. 'true') or right value (e. g. 'read_write') or a
   * string containing key value pairs such as 'insert=true,update=false,notification=true'.
   */
  @Column(length = 4000)
  public String getValue()
  {
    return value;
  }

  public void setValue(final String value)
  {
    this.value = value;
    this.rights = null; // Force reparsing.
  }

  /**
   * Sets automatically the given identifier to upper case letters: 'ProjectForge' -&gt; 'PROJECTFORGE'
   * @param identifier
   */
  public void setIdentifier(final String identifier)
  {
    this.identifier = identifier != null ? identifier.toUpperCase() : null;
  }

  /**
   * This rights is according to this space.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "space_fk", nullable = false)
  public SpaceDO getSpace()
  {
    return space;
  }

  public void setSpace(final SpaceDO space)
  {
    this.space = space;
  }

  @Transient
  public Integer getSpaceId()
  {
    if (this.space == null)
      return null;
    return space.getId();
  }

  /**
   * If given then a group right is specified by this right (otherwise a user must be given).
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_fk")
  public GroupDO getGroup()
  {
    return group;
  }

  public void setGroup(final GroupDO group)
  {
    this.group = group;
  }

  @Transient
  public Integer getGroupId()
  {
    if (this.group == null)
      return null;
    return group.getId();
  }

  /**
   * If given then a user right is specified by this right (otherwise a group must be given).
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_fk")
  public PFUserDO getUser()
  {
    return user;
  }

  public void setUser(final PFUserDO user)
  {
    this.user = user;
  }

  @Transient
  public Integer getUserId()
  {
    if (this.user == null)
      return null;
    return user.getId();
  }

  /**
   * Optional comment.
   */
  @Column(length = Constants.COMMENT_LENGTH)
  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }

  @Transient
  public String getRight(final String key)
  {
    if (key == null) {
      return null;
    }
    readKeyValues();
    for (final LabelValueBean<String, String> labelValue : this.rights) {
      if (key.equals(labelValue.getLabel()) == true) {
        return labelValue.getValue();
      }
    }
    return null;
  }

  private void readKeyValues()
  {
    if (this.rights != null) {
      return;
    }
    this.rights = new ArrayList<LabelValueBean<String, String>>();
    if (value != null && value.indexOf('=') > 0) {
      final StringTokenizer tokenizer = new StringTokenizer(value, ",");
      while (tokenizer.hasMoreTokens() == true) {
        final String keyValue = tokenizer.nextToken();
        final int pos = keyValue.indexOf('=');
        if (pos > 0) {
          final String key = keyValue.substring(0, pos);
          final String val = (keyValue.length() > pos + 1) ? keyValue.substring(pos + 1) : null;
          this.rights.add(new LabelValueBean<String, String>(key, val));
        } else {
          this.rights.add(new LabelValueBean<String, String>(keyValue, null));
        }
      }
    }
  }
}
