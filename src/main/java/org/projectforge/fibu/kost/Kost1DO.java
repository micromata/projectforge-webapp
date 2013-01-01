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

package org.projectforge.fibu.kost;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.ShortDisplayNameCapable;
import org.projectforge.fibu.KostFormatter;

import de.micromata.hibernate.history.Historizable;

@Entity
@Indexed
@ClassBridge(name = "nummer", index = Index.TOKENIZED, store = Store.NO, impl = HibernateSearchKost1Bridge.class)
@Table(name = "T_FIBU_KOST1", uniqueConstraints = { @UniqueConstraint(columnNames = { "nummernkreis", "bereich", "teilbereich", "endziffer"})})
public class Kost1DO extends DefaultBaseDO implements Historizable, ShortDisplayNameCapable
{
  private static final long serialVersionUID = -6534347300453425760L;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private KostentraegerStatus kostentraegerStatus;

  private int nummernkreis;

  private int bereich;

  private int teilbereich;

  private int endziffer;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  public KostentraegerStatus getKostentraegerStatus()
  {
    return kostentraegerStatus;
  }

  public void setKostentraegerStatus(KostentraegerStatus kostentraegerStatus)
  {
    this.kostentraegerStatus = kostentraegerStatus;
  }

  @Transient
  public String getShortDisplayName()
  {
    return KostFormatter.format(this);
  }
  
  /**
   * Format: #.###.##.##
   * @see KostFormatter#format(Kost2DO)
   */
  @Transient
  public String getFormattedNumber()
  {
    return KostFormatter.format(this);
  }

  /**
   * @see KostFormatter#getKostAsInt(int, int, int, int)
   */
  @Transient
  public Integer getNummer()
  {
    return KostFormatter.getKostAsInt(nummernkreis, bereich, teilbereich, endziffer);
  }

  /**
   * Nummernkreis entspricht der ersten Ziffer.
   * @return
   */
  @Column(name = "nummernkreis")
  public int getNummernkreis()
  {
    return nummernkreis;
  }

  public void setNummernkreis(int bereich)
  {
    this.nummernkreis = bereich;
  }

  /**
   * Bereich entspricht der 2.-4. Ziffer.
   * @return
   */
  @Column(name = "bereich")
  public int getBereich()
  {
    return bereich;
  }

  public void setBereich(int bereich)
  {
    this.bereich = bereich;
  }

  /**
   * Teilbereich entspricht der 5.-6. Ziffer.
   * @return
   */
  @Column(name = "teilbereich")
  public int getTeilbereich()
  {
    return teilbereich;
  }

  public void setTeilbereich(int teilbereich)
  {
    this.teilbereich = teilbereich;
  }

  @Column(name = "endziffer")
  public int getEndziffer()
  {
    return endziffer;
  }

  public void setEndziffer(int endziffer)
  {
    this.endziffer = endziffer;
  }

  /**
   * Optionale Kommentare zum Kostenträger.
   * @return
   */
  @Column(length = 4000)
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * return true if nummernkreis, bereich, teilbereich and endziffer is equal, otherwise false;
   * @see java.lang.Object#equals(java.lang.Object)
   */

  @Override
  public boolean equals(Object o)
  {
    if (o instanceof Kost1DO) {
      Kost1DO other = (Kost1DO) o;
      return (this.nummernkreis == other.nummernkreis && this.bereich == other.bereich && this.teilbereich == other.teilbereich && this.endziffer == other.endziffer);
    }
    return false;
  }

  /**
   * Uses HashCodeBuilder with property nummernkreis, bereich, teilbereich and endziffer.
   * @see java.lang.Object#hashCode()
   * @see HashCodeBuilder#append(int)
   */
  @Override
  public int hashCode()
  {
    HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(this.nummernkreis).append(this.bereich).append(this.teilbereich).append(this.endziffer);
    return hcb.toHashCode();
  }
}
