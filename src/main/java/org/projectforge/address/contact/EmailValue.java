/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.address.contact;

import java.io.Serializable;

import org.projectforge.core.PropertyInfo;
import org.projectforge.xml.stream.XmlField;
import org.projectforge.xml.stream.XmlObject;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 */
@XmlObject(alias = "value")
public class EmailValue implements Serializable
{
  private static final long serialVersionUID = 3930937731653442004L;

  @XmlField
  @PropertyInfo(i18nKey = "contactType")
  private ContactType contactType;

  @XmlField
  @PropertyInfo(i18nKey = "email")
  private String email;

  public ContactType getContactType()
  {
    return contactType;
  }

  public EmailValue setContactType(final ContactType contactType)
  {
    this.contactType = contactType;
    return this;
  }

  public String getEmail()
  {
    return email;
  }

  public EmailValue setEmail(final String email)
  {
    this.email = email;
    return this;
  }

}
