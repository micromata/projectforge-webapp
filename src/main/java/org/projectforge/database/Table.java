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

package org.projectforge.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one attribute of a table (e. g. for creation).
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class Table
{
  private String name;

  private List<TableAttribute> attributes = new ArrayList<TableAttribute>();

  public Table(final String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  /**
   * Multiple primary keys are not allowed.
   * @return Primary key if found or null.
   */
  public TableAttribute getPrimaryKey()
  {
    for (final TableAttribute attr : attributes) {
      if (attr.isPrimaryKey() == true) {
        return attr;
      }
    }
    return null;
  }

  public List<TableAttribute> getAttributes()
  {
    return attributes;
  }

  public Table addAttribute(final TableAttribute attr)
  {
    attributes.add(attr);
    return this;
  }
}
