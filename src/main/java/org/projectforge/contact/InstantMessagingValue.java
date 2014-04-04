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
public class InstantMessagingValue implements Serializable
{
  private static final long serialVersionUID = 5659903071636285902L;

  @XmlField
  private String contactType;

  @XmlField
  private String imType;

  @XmlField
  private String user;

  public String getContactType()
  {
    return contactType;
  }

  public InstantMessagingValue setContactType(final String contactType)
  {
    this.contactType = contactType;
    return this;
  }

  public String getImType()
  {
    return imType;
  }

  public InstantMessagingValue setImType(final String imType)
  {
    this.imType = imType;
    return this;
  }

  public String getUser()
  {
    return user;
  }

  public InstantMessagingValue setUser(final String user)
  {
    this.user = user;
    return this;
  }

}
