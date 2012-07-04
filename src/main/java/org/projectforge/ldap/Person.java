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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class Person
{
  private String fullName;

  private String lastName;

  private String description;

  private String country;

  private String ou;

  public String getDescription()
  {
    return description;
  }

  public Person setDescription(final String description)
  {
    this.description = description;
    return this;
  }

  public String getFullName()
  {
    return fullName;
  }

  public Person setFullName(final String fullName)
  {
    this.fullName = fullName;
    return this;
  }

  public String getLastName()
  {
    return lastName;
  }

  public Person setLastName(final String lastName)
  {
    this.lastName = lastName;
    return this;
  }

  public String getOu()
  {
    return ou;
  }

  public Person setOu(final String ou)
  {
    this.ou = ou;
    return this;
  }

  public String getCountry()
  {
    return country;
  }

  public Person setCountry(final String country)
  {
    this.country = country;
    return this;
  }

  @Override
  public boolean equals(final Object obj)
  {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString()
  {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
