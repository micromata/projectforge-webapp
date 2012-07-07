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

package org.projectforge.ldap;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapGroup extends LdapObject
{
  private String description;

  private final Set<String> members = new HashSet<String>();

  public LdapGroup addMember(final String dn)
  {
    members.add(dn);
    return this;
  }

  /**
   * @return the members
   */
  public Set<String> getMembers()
  {
    return members;
  }

  public String getDescription()
  {
    return description;
  }

  public LdapGroup setDescription(final String description)
  {
    this.description = description;
    return this;
  }
}
