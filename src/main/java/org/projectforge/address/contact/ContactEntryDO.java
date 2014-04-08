/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.address.contact;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 */
@Entity
@Indexed
@Table(name = "T_ADDRESSENTRY")
public class ContactEntryDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -8141697905834021747L;

  //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ContactEntryDO.class);

  private ContactDO address;

  @Enumerated(EnumType.STRING)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private ContactType contactType;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String city; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String country; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String state; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String street; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String zipCode; // 255

  /**
   * Not used as object due to performance reasons.
   * @return
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "address_id", nullable = false)
  public ContactDO getAddress()
  {
    return address;
  }

  public void setAddress(final ContactDO address)
  {
    this.address = address;
  }

  @Transient
  public Integer getAddressId()
  {
    if (this.address == null)
      return null;
    return address.getId();
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 15, name = "contact_type", nullable = false)
  public ContactType getContactType()
  {
    return contactType;
  }

  /**
   * @return this for chaining.
   */
  public ContactEntryDO setContactType(final ContactType contactType)
  {
    this.contactType = contactType;
    return this;
  }

  /**
   * @return the city
   */
  public String getCity()
  {
    return city;
  }

  /**
   * @param city the city to set
   * @return this for chaining.
   */
  public ContactEntryDO setCity(final String city)
  {
    this.city = city;
    return this;
  }

  /**
   * @return the country
   */
  public String getCountry()
  {
    return country;
  }

  /**
   * @param country the country to set
   * @return this for chaining.
   */
  public ContactEntryDO setCountry(final String country)
  {
    this.country = country;
    return this;
  }

  /**
   * @return the state
   */
  public String getState()
  {
    return state;
  }

  /**
   * @param state the state to set
   * @return this for chaining.
   */
  public ContactEntryDO setState(final String state)
  {
    this.state = state;
    return this;
  }

  /**
   * @return the street
   */
  public String getStreet()
  {
    return street;
  }

  /**
   * @param street the street to set
   * @return this for chaining.
   */
  public ContactEntryDO setStreet(final String street)
  {
    this.street = street;
    return this;
  }

  /**
   * @return the zipCode
   */
  public String getZipCode()
  {
    return zipCode;
  }

  /**
   * @param zipCode the zipCode to set
   * @return this for chaining.
   */
  public ContactEntryDO setZipCode(final String zipCode)
  {
    this.zipCode = zipCode;
    return this;
  }


}
