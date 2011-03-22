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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

/**
 * A space is a unit which may contain issues, wiki pages etc.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_SPACE", uniqueConstraints = { @UniqueConstraint(columnNames = { "name"}), @UniqueConstraint(columnNames = { "identifier"})})
public class SpaceDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 6796022256899221724L;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String identifier;
  
  private SpaceStatus status;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String title;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  /**
   * Examples: 'PF', 'ACME-WEB-PORTAL', ...
   * @return A unique identifier of the space (with upper case letters). This identifier should be as short as possible.
   */
  @Column(length = 20, unique = true, nullable = false)
  public String getIdentifier()
  {
    return identifier;
  }

  /**
   * Sets automatically the given identifier to upper case letters: 'ProjectForge' -&gt; 'PROJECTFORGE'
   * @param identifier
   */
  public void setIdentifier(final String identifier)
  {
    this.identifier = identifier != null ? identifier.toUpperCase() : null;
  }
  
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public SpaceStatus getStatus()
  {
    return status;
  }
  
  public void setStatus(SpaceStatus status)
  {
    this.status = status;
  }

  /**
   * Examples: 'ProjectForge', 'ACME WebPortal', ...
   * @return A human readable and unique title of the space. This title should be short and descriptive, because it's displayed mostly in a
   *         list.
   */
  @Column(length = 255, unique = true, nullable = false)
  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  @Column(length = Constants.COMMENT_LENGTH)
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }
}
