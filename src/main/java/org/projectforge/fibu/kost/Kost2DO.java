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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.ShortDisplayNameCapable;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.ProjektDO;

import de.micromata.hibernate.history.Historizable;

@Entity
@Indexed
@ClassBridge(name = "nummer", index = Index.TOKENIZED, store = Store.NO, impl = HibernateSearchKost2Bridge.class)
@Table(name = "T_FIBU_KOST2", uniqueConstraints = { @UniqueConstraint(columnNames = { "nummernkreis", "bereich", "teilbereich",
    "kost2_art_id"})})
public class Kost2DO extends DefaultBaseDO implements Historizable, ShortDisplayNameCapable, Comparable<Kost2DO>
{
  private static final long serialVersionUID = -6534347300453425760L;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private KostentraegerStatus kostentraegerStatus;

  private int nummernkreis;

  private int bereich;

  private int teilbereich;

  private Kost2ArtDO kost2Art;

  private BigDecimal workFraction;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  @IndexedEmbedded(depth = 2)
  private ProjektDO projekt;

  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  public KostentraegerStatus getKostentraegerStatus()
  {
    return kostentraegerStatus;
  }

  public Kost2DO setKostentraegerStatus(KostentraegerStatus kostentraegerStatus)
  {
    this.kostentraegerStatus = kostentraegerStatus;
    return this;
  }

  /**
   * @see KostFormatter#getKostAsInt(int, int, int, int)
   */
  @Transient
  public Integer getNummer()
  {
    return KostFormatter.getKostAsInt(nummernkreis, bereich, teilbereich, kost2Art.getId());
  }


  /**
   * @see KostFormatter#format(Kost2DO)
   * @see org.projectforge.core.ShortDisplayNameCapable#getShortDisplayName()
   */
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
   * @see KostFormatter#formatToolTip(Kost2DO)
   */
  @Transient
  public String getToolTip()
  {
    return KostFormatter.formatToolTip(this);
  }

  @Transient
  public boolean isEqual(int nummernkreis, int bereich, int teilbereich, int kost2Art)
  {
    return this.nummernkreis == nummernkreis
        && this.bereich == bereich
        && this.teilbereich == teilbereich
        && this.kost2Art.getId() == kost2Art;
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

  public Kost2DO setNummernkreis(int nummernkreis)
  {
    this.nummernkreis = nummernkreis;
    return this;
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

  public Kost2DO setBereich(int bereich)
  {
    this.bereich = bereich;
    return this;
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

  public Kost2DO setTeilbereich(int teilbereich)
  {
    this.teilbereich = teilbereich;
    return this;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "kost2_art_id", nullable = false)
  public Kost2ArtDO getKost2Art()
  {
    return kost2Art;
  }

  public Kost2DO setKost2Art(Kost2ArtDO kost2Art)
  {
    this.kost2Art = kost2Art;
    return this;
  }

  @Transient
  public Integer getKost2ArtId()
  {
    if (this.kost2Art == null)
      return null;
    return kost2Art.getId();
  }

  @Column(name = "work_fraction")
  public BigDecimal getWorkFraction()
  {
    return workFraction;
  }

  public Kost2DO setWorkFraction(BigDecimal workFraction)
  {
    this.workFraction = workFraction;
    return this;
  }

  @Column(length = 4000)
  public String getDescription()
  {
    return description;
  }

  public Kost2DO setDescription(String description)
  {
    this.description = description;
    return this;
  }

  /**
   * Optionale Kommentare zum Kostenträger.
   * @return
   */
  @Column(length = 4000)
  public String getComment()
  {
    return comment;
  }

  public Kost2DO setComment(String comment)
  {
    this.comment = comment;
    return this;
  }

  /**
   * Projekt kann gegeben sein. Wenn Kostenträger zu einem Projekt hinzugehört, dann sind auf jeden Fall die ersten 6 Ziffern
   * identisch mit der Projektnummer.
   * @return
   */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "projekt_id")
  public ProjektDO getProjekt()
  {
    return projekt;
  }

  public Kost2DO setProjekt(ProjektDO projekt)
  {
    this.projekt = projekt;
    return this;
  }

  @Transient
  public Integer getProjektId()
  {
    if (this.projekt == null)
      return null;
    return projekt.getId();
  }

  /**
   * return true if nummernkreis, bereich, teilbereich and kost2Art is equal, otherwise false;
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o)
  {
    if (o instanceof Kost2DO) {
      Kost2DO other = (Kost2DO) o;
      if (this.nummernkreis == other.nummernkreis && this.bereich == other.bereich && this.teilbereich == other.teilbereich) {
        return ObjectUtils.equals(this.kost2Art, other.kost2Art);
      }
    }
    return false;
  }

  /**
   * Uses HashCodeBuilder with property nummernkreis, bereich, teilbereich and kost2Art.
   * @see java.lang.Object#hashCode()
   * @see HashCodeBuilder#append(int)
   * @see HashCodeBuilder#append(Object)
   */
  @Override
  public int hashCode()
  {
    HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(this.nummernkreis).append(this.bereich).append(this.teilbereich).append(this.kost2Art);
    return hcb.toHashCode();
  }

  /**
   * Compares shortDisplayName.
   * @see #getShortDisplayName()
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Kost2DO o)
  {
    return this.getShortDisplayName().compareTo(o.getShortDisplayName());
  }
}
