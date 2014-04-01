/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.addresses;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.address.FormOfAddress;
import org.projectforge.common.StringHelper;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.user.PFUserContext;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
@Entity
@Indexed
@Table(name = "T_ADDRESSES")
public class AddressEntryDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -1724220844452834692L;

  //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressEntryDO.class);

  @Enumerated(EnumType.STRING)
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private AddressEntryType addressType;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String name; // 255 not null

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String firstName; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private FormOfAddress form;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String title; // 255

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date birthday;


  @Column
  public Date getBirthday()
  {
    return birthday;
  }

  public AddressEntryDO setBirthday(final Date birthday)
  {
    this.birthday = birthday;
    return this;
  }

  @Column(name = "first_name", length = 255)
  public String getFirstName()
  {
    return firstName;
  }

  public AddressEntryDO setFirstName(final String firstName)
  {
    this.firstName = firstName;
    return this;
  }

  @Transient
  public String getFullName()
  {
    return StringHelper.listToString(", ", name, firstName);
  }

  @Transient
  public String getFullNameWithTitleAndForm() {
    final StringBuffer buf = new StringBuffer();
    if (getForm() != null) {
      buf.append(PFUserContext.getLocalizedString(getForm().getI18nKey())).append(" ");
    }
    if (getTitle() != null) {
      buf.append(getTitle()).append(" ");
    }
    if (getFirstName() != null) {
      buf.append(getFirstName()).append(" ");
    }
    if (getName() != null) {
      buf.append(getName());
    }
    return buf.toString();
  }

  @Column(length = 255)
  public String getName()
  {
    return name;
  }

  public AddressEntryDO setName(final String name)
  {
    this.name = name;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "form", length = 10)
  public FormOfAddress getForm()
  {
    return form;
  }

  public AddressEntryDO setForm(final FormOfAddress form)
  {
    this.form = form;
    return this;
  }

  @Column(length = 255)
  public String getTitle()
  {
    return title;
  }

  public AddressEntryDO setTitle(final String title)
  {
    this.title = title;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 15, name = "address_type")
  public AddressEntryType getAddressTypeg()
  {
    return addressType;
  }

  /**
   * @return this for chaining.
   */
  public AddressEntryDO setSkillRating(final AddressEntryType addressType)
  {
    this.addressType = addressType;
    return this;
  }
}
