/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.address.contact;

import java.sql.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.common.StringHelper;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.PFPersistancyBehavior;
import org.projectforge.core.PropertyInfo;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserContext;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 *
 */
@Entity
@Indexed
@Table(name = "T_CONTACT")
public class ContactDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -1177059694759828682L;

  //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ContactDO.class);

  private TaskDO task;

  @PropertyInfo(i18nKey = "name")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String name; // 255 not null

  @PropertyInfo(i18nKey = "firstName")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String firstName; // 255

  @PropertyInfo(i18nKey = "form")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private FormOfAddress form;

  @PropertyInfo(i18nKey = "title")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String title; // 255

  @PropertyInfo(i18nKey = "birthday")
  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date birthday;

  @PropertyInfo(i18nKey = "contact.imValues")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String socialMediaValues;

  @PropertyInfo(i18nKey = "contact.emailValues")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String emailValues;

  @PropertyInfo(i18nKey = "contact.phoneValues")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String phoneValues;

  @PropertyInfo(i18nKey = "contact.contacts")
  @PFPersistancyBehavior(autoUpdateCollectionEntries = true)
  @IndexedEmbedded(depth = 1)
  private Set<ContactEntryDO> contacts = null;

  private ContactStatus contactStatus = ContactStatus.ACTIVE;

  private AddressStatus addressStatus = AddressStatus.UPTODATE;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String publicKey; // 7000

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String fingerprint; // 255

  private Locale communicationLanguage;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String website; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String organization; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String division; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String positionText; // 255

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment; // 5000;

  /**
   * Get the contact entries for this object.
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "contact")
  @IndexColumn(name = "number", base = 1)
  public Set<ContactEntryDO> getContacts()
  {
    return this.contacts;
  }

  /**
   * @param number
   * @return ContactEntryDO with given position number or null (iterates through the list of contacts and compares the number), if not
   *         exist.
   */
  public ContactEntryDO getContact(final short number)
  {
    if (contacts == null) {
      return null;
    }
    for (final ContactEntryDO contact : this.contacts) {
      if (contact.getNumber() == number) {
        return contact;
      }
    }
    return null;
  }

  public ContactDO setContacts(final Set<ContactEntryDO> contacts)
  {
    this.contacts = contacts;
    return this;
  }

  public ContactDO addContact(final ContactEntryDO contactEntry)
  {
    ensureAndGetContacts();
    short number = 1;
    for (final ContactEntryDO pos : contacts) {
      if (pos.getNumber() >= number) {
        number = pos.getNumber();
        number++;
      }
    }
    contactEntry.setNumber(number);
    contactEntry.setContact(this);
    this.contacts.add(contactEntry);
    return this;
  }

  public Set<ContactEntryDO> ensureAndGetContacts()
  {
    if (this.contacts == null) {
      setContacts(new LinkedHashSet<ContactEntryDO>());
    }
    return getContacts();
  }

  @Column
  public Date getBirthday()
  {
    return birthday;
  }

  public ContactDO setBirthday(final Date birthday)
  {
    this.birthday = birthday;
    return this;
  }

  @Column(name = "first_name", length = 255)
  public String getFirstName()
  {
    return firstName;
  }

  public ContactDO setFirstName(final String firstName)
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

  @Column(length = 255, nullable = false)
  public String getName()
  {
    return name;
  }

  public ContactDO setName(final String name)
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

  public ContactDO setForm(final FormOfAddress form)
  {
    this.form = form;
    return this;
  }

  /**
   * Not used as object due to performance reasons.
   * @return
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id")
  public TaskDO getTask()
  {
    return task;
  }

  public void setTask(final TaskDO task)
  {
    this.task = task;
  }

  @Transient
  public Integer getTaskId()
  {
    if (this.task == null)
      return null;
    return task.getId();
  }

  @Column(length = 255)
  public String getTitle()
  {
    return title;
  }

  public ContactDO setTitle(final String title)
  {
    this.title = title;
    return this;
  }

  @Column
  public String getSocialMediaValues()
  {
    return socialMediaValues;
  }

  public ContactDO setSocialMediaValues(final String socialMediaValues)
  {
    this.socialMediaValues = socialMediaValues;
    return this;
  }

  @Column
  public String getEmailValues()
  {
    return emailValues;
  }

  public ContactDO setEmailValues(final String emailValues)
  {
    this.emailValues = emailValues;
    return this;
  }

  @Column
  public String getPhoneValues()
  {
    return phoneValues;
  }

  public ContactDO setPhoneValues(final String phoneValues)
  {
    this.phoneValues = phoneValues;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "contact_status", length = 20, nullable = false)
  public ContactStatus getContactStatus()
  {
    return contactStatus;
  }

  public ContactDO setContactStatus(final ContactStatus contactStatus)
  {
    this.contactStatus = contactStatus;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "address_status", length = 20, nullable = false)
  public AddressStatus getAddressStatus()
  {
    return addressStatus;
  }

  public ContactDO setAddressStatus(final AddressStatus addressStatus)
  {
    this.addressStatus = addressStatus;
    return this;
  }

  @Column(name = "public_key", length = 7000)
  public String getPublicKey()
  {
    return publicKey;
  }

  public ContactDO setPublicKey(final String publicKey)
  {
    this.publicKey = publicKey;
    return this;
  }

  @Column(length = 255)
  public String getFingerprint()
  {
    return fingerprint;
  }

  public ContactDO setFingerprint(final String fingerprint)
  {
    this.fingerprint = fingerprint;
    return this;
  }

  /**
   * @return The communication will take place in this language.
   */
  @Column(name = "communication_language")
  public Locale getCommunicationLanguage()
  {
    return communicationLanguage;
  }

  public ContactDO setCommunicationLanguage(final Locale communicationLanguage)
  {
    this.communicationLanguage = communicationLanguage;
    return this;
  }

  @Column(length = 255)
  public String getWebsite()
  {
    return website;
  }

  public ContactDO setWebsite(final String website)
  {
    this.website = website;
    return this;
  }

  @Column(length = 255)
  public String getOrganization()
  {
    return organization;
  }

  public ContactDO setOrganization(final String organization)
  {
    this.organization = organization;
    return this;
  }

  @Column(name = "comment", length = 5000)
  public String getComment()
  {
    return comment;
  }

  public ContactDO setComment(final String comment)
  {
    this.comment = comment;
    return this;
  }

  @Column(length = 255)
  public String getPositionText()
  {
    return positionText;
  }

  public ContactDO setPositionText(final String positionText)
  {
    this.positionText = positionText;
    return this;
  }

  @Column(length = 255)
  public String getDivision()
  {
    return division;
  }

  public ContactDO setDivision(final String division)
  {
    this.division = division;
    return this;
  }

}
