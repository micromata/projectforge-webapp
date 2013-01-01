/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.marketing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.database.Constants;

/**
 * A marketing campaign for addresses (eg. mailings).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_MARKETING_ADDRESS_CAMPAIGN")
public class AddressCampaignDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 4434215180122488556L;

  public static final int MAX_VALUE_LENGTH = 100;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String title;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String values;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  @Column(length = Constants.LENGTH_TITLE)
  public String getTitle()
  {
    return title;
  }

  public void setTitle(final String title)
  {
    this.title = title;
  }

  public static String[] getValuesArray(final String values)
  {
    if (StringUtils.isBlank(values) == true) {
      return null;
    }
    return StringUtils.split(values, "; ");
  }

  @Transient
  public String[] getValuesArray()
  {
    return getValuesArray(values);
  }

  @Column(length = 1000)
  public String getValues()
  {
    return values;
  }

  public void setValues(final String values)
  {
    this.values = values;
  }

  @Column(length = Constants.LENGTH_COMMENT)
  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }
}
