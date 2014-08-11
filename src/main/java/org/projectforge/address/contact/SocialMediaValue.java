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
public class SocialMediaValue implements Serializable
{
  private static final long serialVersionUID = 5659903071636285902L;

  @XmlField
  @PropertyInfo(i18nKey = "contactType")
  private ContactType contactType;

  @XmlField
  @PropertyInfo(i18nKey = "imType")
  private SocialMediaType socialMediaType;

  @XmlField
  @PropertyInfo(i18nKey = "user")
  private String user;

  public ContactType getContactType()
  {
    return contactType;
  }

  public SocialMediaValue setContactType(final ContactType contactType)
  {
    this.contactType = contactType;
    return this;
  }

  public SocialMediaType getSocialMediaType()
  {
    return socialMediaType;
  }

  public SocialMediaValue setSocialMediaType(final SocialMediaType socialMediaType)
  {
    this.socialMediaType = socialMediaType;
    return this;
  }

  public String getUser()
  {
    return user;
  }

  public SocialMediaValue setUser(final String user)
  {
    this.user = user;
    return this;
  }

}
