/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.AbstractHistorizableBaseDO;
import org.projectforge.core.IManualIndex;
import org.projectforge.lucene.PFAnalyzer;


/**
 * Die letzten beiden Ziffern (Endziffern) eines Kostenträgers repräsentieren die Kostenart. Anhand der Endziffer kann abgelesen werden, um
 * welche Art von Kostenträger es sich handelt (fakturiert/nicht fakturiert, Akquise, Wartung etc.)
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Indexed
@Table(name = "T_FIBU_KOST2ART")
@Analyzer(impl = PFAnalyzer.class)
public class Kost2ArtDO extends AbstractHistorizableBaseDO<Integer> implements Comparable<Kost2ArtDO>, IManualIndex
{
  private static final long serialVersionUID = 2398122998160436266L;

  /** Zweistellige Endziffer von KOST2 */
  private Integer id;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String name;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  private boolean fakturiert;

  private BigDecimal workFraction;

  private boolean projektStandard;

  /** Zweistellige Endziffer von KOST2 */
  @Id
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  /**
   * Muss größer als 0 und kleiner als 100 sein, sonst wird ein Validierungsfehler geworfen.
   * @param nummer
   */
  public void setId(Integer id)
  {
    this.id = id;
  }
  
  public Kost2ArtDO withId(final Integer id) {
    setId(id);
    return this;
  }

  @Column(length = 255, nullable = false)
  public String getName()
  {
    return name;
  }

  public Kost2ArtDO setName(String name)
  {
    this.name = name;
    return this;
  }

  @Column(length = 5000)
  public Kost2ArtDO setDescription(String description)
  {
    this.description = description;
    return this;
  }

  public String getDescription()
  {
    return description;
  }

  @Column(nullable = false)
  public Kost2ArtDO setFakturiert(boolean fakturiert)
  {
    this.fakturiert = fakturiert;
    return this;
  }

  /**
   * Werden die Aufwendungen nach außen fakturiert, d. h. stehen den Ausgaben auch Einnahmen entgegen (i. d. R. Kundenrechnungen oder
   * Fördermaßnahmen).
   * @return
   */
  public boolean isFakturiert()
  {
    return fakturiert;
  }

  @Column(name = "work_fraction")
  public BigDecimal getWorkFraction()
  {
    return workFraction;
  }

  public Kost2ArtDO setWorkFraction(BigDecimal workFraction)
  {
    this.workFraction = workFraction;
    return this;
  }

  /**
   * Wenn true, dann wird diese Kostenart für Projekte als Standardendziffer für Kostenträger vorgeschlagen.
   */
  @Column(name = "projekt_standard")
  public boolean isProjektStandard()
  {
    return projektStandard;
  }

  public Kost2ArtDO setProjektStandard(boolean projektStandard)
  {
    this.projektStandard = projektStandard;
    return this;
  }

  /**
   * return true if id is equal, otherwise false;
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o)
  {
    if (o instanceof Kost2ArtDO) {
      Kost2ArtDO other = (Kost2ArtDO) o;
      return (ObjectUtils.equals(this.id, other.id));
    }
    return false;
  }

  /**
   * Uses HashCodeBuilder with property id.
   * @see java.lang.Object#hashCode()
   * @see HashCodeBuilder#append(int)
   */
  @Override
  public int hashCode()
  {
    HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(this.id);
    return hcb.toHashCode();
  }

  public int compareTo(Kost2ArtDO o)
  {
    return id.compareTo(o.id);
  }
}
