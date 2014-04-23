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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Indexed;
import org.projectforge.core.DefaultBaseDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
@Entity
@Indexed
@Table(name = "T_ADDRESSKAT")
public class AddressKatDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 3628375412472899736L;
  private static final String SOCIAL_MEDIA_VALUES = "AIM;Facebook;Gado-Gado;Google Talk;ICQ;Jabber;MSN;QQ;Skype;Twitter;Yahoo;other;";

  private String socialMediaValues;

  @Column
  public String getSocialMediaValues()
  {
    return SOCIAL_MEDIA_VALUES + socialMediaValues;
  }

  public AddressKatDO setSocialMediaValues(final String socialMediaValues)
  {
    final String s = socialMediaValues.substring(socialMediaValues.indexOf("other;"));
    this.socialMediaValues = s;
    return this;
  }

  @Transient
  protected static String[] getValuesArray(final String values)
  {
    if (StringUtils.isBlank(values) == true) {
      return null;
    }
    final String[] sar = StringUtils.split(values, ";");
    for (int i=0; i < sar.length; i++) {
      sar[i] = StringUtils.trim(sar[i]);
    }
    return sar;
  }

  @Transient
  public String[] getValuesArray() {
    return getValuesArray(getSocialMediaValues());
  }
}
