/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.fibu;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.ShortDisplayNameCapable;

import de.micromata.hibernate.history.Historizable;

@Entity
@Indexed
@Table(name = "T_FIBU_KONTO")
public class KontoDO extends DefaultBaseDO implements Historizable, ShortDisplayNameCapable
{
  private static final long serialVersionUID = -7468158838560608225L;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Integer nummer;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String bezeichnung;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @Column(name = "nummer", nullable = false, unique = true)
  public Integer getNummer()
  {
    return nummer;
  }

  public void setNummer(Integer nummer)
  {
    this.nummer = nummer;
  }

  @Column(length = 255, nullable = false)
  public String getBezeichnung()
  {
    return bezeichnung;
  }

  public void setBezeichnung(String bezeichnung)
  {
    this.bezeichnung = bezeichnung;
  }

  @Column(name = "description", length = 4000, nullable = true)
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public boolean equals(Object o)
  {
    if (o instanceof KontoDO) {
      KontoDO other = (KontoDO) o;
      if (ObjectUtils.equals(this.getNummer(), other.getNummer()) == false) {
        return false;
      }
      return ObjectUtils.equals(this.getBezeichnung(), other.getBezeichnung());
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(this.getNummer());
    hcb.append(this.getBezeichnung());
    return hcb.toHashCode();
  }

  @Transient
  public String getShortDisplayName()
  {
    return String.valueOf(nummer);
  }
}
