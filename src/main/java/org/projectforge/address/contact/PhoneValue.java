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

import org.projectforge.address.PhoneType;
import org.projectforge.core.PropertyInfo;
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
  @PropertyInfo(i18nKey = "phoneType")
  private PhoneType phoneType;

  @XmlField
  @PropertyInfo(i18nKey = "number")
  private String number;

  public PhoneType getPhoneType()
  {
    return phoneType;
  }

  public PhoneValue setPhoneType(final PhoneType phoneType)
  {
    this.phoneType = phoneType;
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
