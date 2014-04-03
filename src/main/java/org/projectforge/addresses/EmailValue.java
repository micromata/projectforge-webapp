/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.addresses;

import java.io.Serializable;

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
  private String contactType;

  @XmlField
  private String email;

  public String getContactType()
  {
    return contactType;
  }

  public EmailValue setContactType(final String contactType)
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
