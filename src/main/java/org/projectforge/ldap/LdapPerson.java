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

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapPerson extends LdapObject<String>
{
  private String uid, surname, givenName, organization, telephoneNumber, homePhoneNumber, description, employeeNumber;

  private boolean deleted, deactivated, passwordGiven;

  private String[] mail, mobilePhoneNumber;

  /**
   * @see org.projectforge.ldap.LdapObject#getId()
   */
  @Override
  public String getId()
  {
    return uid;
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
   */
  public String getSurname()
  {
    return surname;
  }

  /**
   * @param surname the sn to set
   * @return this for chaining.
   */
  public LdapPerson setSurname(final String surname)
  {
    this.surname = surname;
    updateCommonName();
    return this;
  }

  /**
   * The user name (login name). This is not the pk (id) of the entity.
   * @return the uid
   * @see #getEmployeeNumber()
   */
  public String getUid()
  {
    return this.uid;
  }

  /**
   * @param uid the user name (login name) to set
   * @return this for chaining.
   * @see #setEmployeeNumber(String)
   */
  public LdapPerson setUid(final String uid)
  {
    this.uid = uid;
    return this;
  }

  /**
   * The unique id of the entity.
   * @return the employeeNumber
   */
  public String getEmployeeNumber()
  {
    return employeeNumber;
  }

  /**
   * @param employeeNumber the employeeNumber to set
   * @return this for chaining.
   */
  public LdapPerson setEmployeeNumber(final String employeeNumber)
  {
    this.employeeNumber = employeeNumber;
    return this;
  }

  /**
   * @return the givenName
   */
  public String getGivenName()
  {
    return this.givenName;
  }

  /**
   * @param givenName the givenName to set
   * @return this for chaining.
   */
  public LdapPerson setGivenName(final String givenName)
  {
    this.givenName = givenName;
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
  public LdapPerson setMail(final String... mail)
  {
    this.mail = mail;
    return this;
  }

  public String getTelephoneNumber()
  {
    return this.telephoneNumber;
  }

  /**
   * @param telephoneNumber the businessPhone to set
   * @return this for chaining.
   */
  public LdapPerson setTelephoneNumber(final String telephoneNumber)
  {
    this.telephoneNumber = telephoneNumber;
    return this;
  }

  public String[] getMobilePhoneNumber()
  {
    return this.mobilePhoneNumber;
  }

  /**
   * @param mobilePhoneNumber the mobilePhone to set
   * @return this for chaining.
   */
  public LdapPerson setMobilePhoneNumber(final String... mobilePhoneNumbers)
  {
    this.mobilePhoneNumber = mobilePhoneNumbers;
    return this;
  }

  public String getOrganization()
  {
    return this.organization;
  }

  /**
   * @param organization the organization to set
   * @return this for chaining.
   */
  public LdapPerson setOrganization(final String organization)
  {
    this.organization = organization;
    return this;
  }

  public String getHomePhoneNumber()
  {
    return this.homePhoneNumber;
  }

  /**
   * @param homePhoneNumber the privatePhone to set
   * @return this for chaining.
   */
  public LdapPerson setHomePhoneNumber(final String homePhoneNumber)
  {
    this.homePhoneNumber = homePhoneNumber;
    return this;
  }

  public String getDescription()
  {
    return this.description;
  }

  /**
   * @param description the description to set
   * @return this for chaining.
   */
  public LdapPerson setDescription(final String description)
  {
    this.description = description;
    return this;
  }

  /**
   * @return the deleted
   */
  public boolean isDeleted()
  {
    return deleted;
  }

  /**
   * @param deleted the deleted to set
   * @return this for chaining.
   */
  public LdapPerson setDeleted(final boolean deleted)
  {
    this.deleted = deleted;
    return this;
  }

  /**
   * @return the deactivated
   */
  public boolean isDeactivated()
  {
    return deactivated;
  }

  /**
   * @param deactivated the deactivated to set
   * @return this for chaining.
   */
  public LdapPerson setDeactivated(final boolean deactivated)
  {
    this.deactivated = deactivated;
    return this;
  }

  /**
   * @return the passwordGiven
   */
  public boolean isPasswordGiven()
  {
    return passwordGiven;
  }

  /**
   * @param passwordGiven the passwordGiven to set
   * @return this for chaining.
   */
  public LdapPerson setPasswordGiven(final boolean passwordGiven)
  {
    this.passwordGiven = passwordGiven;
    return this;
  }
}
