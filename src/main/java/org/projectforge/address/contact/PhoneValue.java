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

import org.projectforge.xml.stream.XmlField;
import org.projectforge.xml.stream.XmlObject;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 */
@XmlObject(alias = "value")
public class PhoneValue implements Serializable
{
  private static final long serialVersionUID = 8470994791533327287L;

  @XmlField
  private String contactType;

  @XmlField
  private String number;

  public String getContactType()
  {
    return contactType;
  }

  public PhoneValue setContactType(final String contactType)
  {
    this.contactType = contactType;
    return this;
  }

  public String getNumber()
  {
    return number;
  }

  public PhoneValue setNumber(final String number)
  {
    this.number = number;
    return this;
  }

}
