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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
import org.projectforge.core.PropertyInfo;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserContext;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 *
 */
@Entity
@Indexed
@Table(name = "T_ADDRESS2")
public class Address2DO extends DefaultBaseDO
{
  private static final long serialVersionUID = -1177059694759828682L;

  //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Address2DO.class);

  private TaskDO task;

  @PropertyInfo(i18nKey = "name")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String name; // 255 not null

  @PropertyInfo(i18nKey = "firstName")
  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String firstname; // 255

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

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String imValues;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String emailValues;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String phoneValues;

  @Column
  public Date getBirthday()
  {
    return birthday;
  }

  public Address2DO setBirthday(final Date birthday)
  {
    this.birthday = birthday;
    return this;
  }

  @Column(name = "first_name", length = 255)
  public String getFirstname()
  {
    return firstname;
  }

  public Address2DO setFirstname(final String firstname)
  {
    this.firstname = firstname;
    return this;
  }

  @Transient
  public String getFullName()
  {
    return StringHelper.listToString(", ", name, firstname);
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
    if (getFirstname() != null) {
      buf.append(getFirstname()).append(" ");
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

  public Address2DO setName(final String name)
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

  public Address2DO setForm(final FormOfAddress form)
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

  public Address2DO setTitle(final String title)
  {
    this.title = title;
    return this;
  }

  public String getImValues()
  {
    return imValues;
  }

  public Address2DO setImValues(final String imValues)
  {
    this.imValues = imValues;
    return this;
  }

  public String getEmailValues()
  {
    return emailValues;
  }

  public Address2DO setEmailValues(final String emailValues)
  {
    this.emailValues = emailValues;
    return this;
  }

  public String getPhoneValues()
  {
    return phoneValues;
  }

  public Address2DO setPhoneValues(final String phoneValues)
  {
    this.phoneValues = phoneValues;
    return this;
  }

}
