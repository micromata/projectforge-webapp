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
public class LdapGroup extends LdapObject<Integer>
{
  private Integer gidNumber;

  private String description, organization;

  private final Set<String> members = new HashSet<String>();

  /**
   * @see org.projectforge.ldap.LdapObject#getId()
   */
  @Override
  public Integer getId()
  {
    return this.gidNumber;
  }

  public LdapGroup addMember(final String dn, final String baseDN)
  {
    members.add(dn + "," + baseDN);
    return this;
  }

  public LdapGroup addMember(final LdapObject< ? > member, final String baseDN)
  {
    return addMember(member.getDn(), baseDN);
  }

  /**
   * @return the members
   */
  public Set<String> getMembers()
  {
    return members;
  }

  public void clearMembers()
  {
    members.clear();
  }

  public void addAllMembers(final Set<String> members)
  {
    members.addAll(members);
  }

  /**
   * @return the gidNumber
   */
  public Integer getGidNumber()
  {
    return gidNumber;
  }

  /**
   * @param gidNumber the gidNumber to set
   * @return this for chaining.
   */
  public LdapGroup setGidNumber(final Integer gidNumber)
  {
    this.gidNumber = gidNumber;
    return this;
  }

  /**
   * @return the organization
   */
  public String getOrganization()
  {
    return organization;
  }

  /**
   * @param organization the organization to set
   * @return this for chaining.
   */
  public LdapGroup setOrganization(final String organization)
  {
    this.organization = organization;
    return this;
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
