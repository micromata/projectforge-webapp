/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.search.annotations.Indexed;
import org.projectforge.core.BaseDO;
import org.projectforge.core.ModificationStatus;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_CALENDAR_EVENT_ATTACHMENT")
public class TeamEventAttachmentDO implements Serializable, Comparable<TeamEventAttachmentDO>, BaseDO<Integer>
{
  private static final long serialVersionUID = -7858238331041883784L;

  private Integer id;

  private String filename;

  private byte[] content;

  @Override
  @Id
  @GeneratedValue
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  @Override
  public void setId(final Integer id)
  {
    this.id = id;
  }

  @Column
  public String getFilename()
  {
    return filename;
  }

  public TeamEventAttachmentDO setFilename(final String filename)
  {
    this.filename = filename;
    return this;
  }

  @Column
  public byte[] getContent()
  {
    return content;
  }

  public TeamEventAttachmentDO setContent(final byte[] content)
  {
    this.content = content;
    return this;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final TeamEventAttachmentDO arg0)
  {
    if (this.id != null && ObjectUtils.equals(this.id, arg0.id) == true) {
      return 0;
    }
    return this.toString().toLowerCase().compareTo(arg0.toString().toLowerCase());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final HashCodeBuilder hcb = new HashCodeBuilder();
    if (this.id != null) {
      hcb.append(this.id);
      return hcb.toHashCode();
    }
    if (this.filename != null) {
      hcb.append(this.filename);
    }
    if (this.content != null) {
      hcb.append(this.content);
    }
    return hcb.toHashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object o)
  {
    if (o instanceof TeamEventAttachmentDO) {
      if (this.id != null && ObjectUtils.equals(this.id, ((TeamEventAttachmentDO) o).id) == true) {
        return true;
      }
      final TeamEventAttachmentDO other = (TeamEventAttachmentDO) o;
      if (StringUtils.equals(this.getFilename(), other.getFilename()) == false)
        return false;
      if (ObjectUtils.equals(this.getContent(), other.getContent()) == false)
        return false;
      return true;
    }
    return false;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    if (StringUtils.isBlank(filename) == true) {
      return String.valueOf(id);
    }
    return StringUtils.defaultString(this.filename);
  }

  /**
   * @see org.projectforge.core.BaseDO#isMinorChange()
   */
  @Transient
  @Override
  public boolean isMinorChange()
  {
    return false;
  }

  /**
   * @see org.projectforge.core.BaseDO#setMinorChange(boolean)
   */
  @Override
  public void setMinorChange(final boolean value)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.core.BaseDO#getAttribute(java.lang.String)
   */
  @Transient
  @Override
  public Object getAttribute(final String key)
  {
    throw new UnsupportedOperationException();
  }

  /**
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
    if (src instanceof TeamEventAttachmentDO == false) {
      throw new UnsupportedOperationException();
    }
    final TeamEventAttachmentDO source = (TeamEventAttachmentDO) src;
    ModificationStatus modStatus = ModificationStatus.NONE;
    if (ObjectUtils.equals(this.id, source.id) == false) {
      modStatus = ModificationStatus.MAJOR;
      this.id = source.id;
    }
    if (ObjectUtils.equals(this.filename, source.filename) == false) {
      modStatus = ModificationStatus.MAJOR;
      this.filename = source.filename;
    }
    if (ObjectUtils.equals(this.content, source.content) == false) {
      modStatus = ModificationStatus.MAJOR;
      this.content = source.content;
    }
    return modStatus;
  }
}
