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
public class LdapUser extends LdapObject
{
  private String surname, givenName;

  private String description;

  private String mail;

  private String uid;

  /**
   * @return the sn
   */
  public String getSurname()
  {
    return surname;
  }

  /**
   * @param surname the sn to set
   * @return this for chaining.
   */
  public LdapUser setSurname(final String surname)
  {
    this.surname = surname;
    return this;
  }

  /**
   * @return the uid
   */
  public String getUid()
  {
    return uid;
  }

  /**
   * @param uid the uid to set
   * @return this for chaining.
   */
  public LdapUser setUid(final String uid)
  {
    this.uid = uid;
    return this;
  }

  public String getDescription()
  {
    return description;
  }

  public LdapUser setDescription(final String description)
  {
    this.description = description;
    return this;
  }

  /**
   * @return the givenName
   */
  public String getGivenName()
  {
    return givenName;
  }

  /**
   * @param givenName the givenName to set
   * @return this for chaining.
   */
  public LdapUser setGivenName(final String givenName)
  {
    this.givenName = givenName;
    return this;
  }


  /**
   * @return the mail
   */
  public String getMail()
  {
    return mail;
  }

  /**
   * @param mail the mail to set
   * @return this for chaining.
   */
  public LdapUser setMail(final String mail)
  {
    this.mail = mail;
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
