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
import org.apache.commons.lang.math.NumberUtils;
import org.projectforge.address.AddressDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapAddress extends LdapObject
{
  private final AddressDO address;

  private String[] mail;

  public LdapAddress()
  {
    address = new AddressDO();
  }

  public LdapAddress(final AddressDO address)
  {
    this.address = address;
    updateCommonName();
  }

  private void updateCommonName()
  {
    String cn;
    if (getGivenName() != null) {
      if (getSurname() != null) {
        cn = getGivenName() + " " + getSurname();
      } else {
        cn = getGivenName();
      }
    } else {
      cn = getSurname();
    }
    setCommonName(cn);
  }

  /**
   * @return the sn
   * @see AddressDO#getName()
   */
  public String getSurname()
  {
    return address.getName();
  }

  /**
   * @param surname the sn to set
   * @return this for chaining.
   * @see AddressDO#setName(String)
   */
  public LdapAddress setSurname(final String surname)
  {
    address.setName(surname);
    updateCommonName();
    return this;
  }

  /**
   * @return the uid
   */
  public String getUid()
  {
    return String.valueOf(address.getId());
  }

  /**
   * @param uid the uid to set
   * @return this for chaining.
   */
  public LdapAddress setUid(final String uid)
  {
    address.setId(NumberUtils.createInteger(uid));
    return this;
  }

  /**
   * @return the givenName
   */
  public String getGivenName()
  {
    return address.getFirstName();
  }

  /**
   * @param givenName the givenName to set
   * @return this for chaining.
   */
  public LdapAddress setGivenName(final String givenName)
  {
    address.setFirstName(givenName);
    updateCommonName();
    return this;
  }

  /**
   * @return the mail
   */
  public String[] getMail()
  {
    return mail;
  }

  /**
   * @param mail the mail to set
   * @return this for chaining.
   */
  public LdapAddress setMail(final String... mail)
  {
    this.mail = mail;
    return this;
  }

  public String getBusinessPhone()
  {
    return address.getBusinessPhone();
  }

  public String getMobilePhone()
  {
    return address.getMobilePhone();
  }

  public String getEmail()
  {
    return address.getEmail();
  }

  public String getOrganization()
  {
    return address.getOrganization();
  }

  public String getPrivatePhone()
  {
    return address.getPrivatePhone();
  }

  public String getPrivateMobilePhone()
  {
    return address.getPrivateMobilePhone();
  }

  public String getPrivateEmail()
  {
    return address.getPrivateEmail();
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
