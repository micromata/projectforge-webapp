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

package org.projectforge.fibu;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.AbstractHistorizableBaseDO;
import org.projectforge.core.IManualIndex;
import org.projectforge.core.ShortDisplayNameCapable;
import org.projectforge.lucene.PFAnalyzer;


/**
 * Jeder Kunde bei Micromata hat eine Kundennummer. Die Kundennummer ist Bestandteil von KOST2 (2.-4. Ziffer). Aufträge aus dem
 * Auftragsbuch, sowie Rechnungen etc. werden Kunden zugeordnet.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_FIBU_KUNDE")
@Analyzer(impl = PFAnalyzer.class)
public class KundeDO extends AbstractHistorizableBaseDO<Integer> implements ShortDisplayNameCapable, IManualIndex
{
  private static final long serialVersionUID = -2138613066430251341L;

  private Integer id;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String name;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String identifier;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String division;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private KundeStatus status;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  /**
   * @return "5.###" ("5.<kunde id>")
   */
  @Transient
  public String getKost()
  {
    return "5." + KostFormatter.format3Digits(id);
  }

  /**
   * 1. Ziffer des Kostenträgers: Ist für Kunden immer 5.
   * @return 5
   */
  @Transient
  public int getNummernkreis()
  {
    return 5;
  }

  /**
   * Kundennummer.
   * @see #getId()
   */
  @Transient
  public Integer getBereich()
  {
    return id;
  }

  /** Ziffer 2-4 von KOST2 (000-999). Ist der primary key. */
  @Id
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  @Column(length = 255, nullable = false)
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * The identifier is used e. g. for display the project as short name in human resources planning tables.
   * @return
   */
  @Column(length = 20)
  public String getIdentifier()
  {
    return identifier;
  }

  public void setIdentifier(String identifier)
  {
    this.identifier = identifier;
  }

  /**
   * @return Identifier if exists otherwise name of project.
   */
  @Transient
  public String getKundeIdentifierDisplayName()
  {
    if (StringUtils.isNotBlank(this.identifier) == true) {
      return this.identifier;
    }
    return this.name;
  }

  @Column(length = 255)
  public String getDivision()
  {
    return division;
  }

  public void setDivision(String division)
  {
    this.division = division;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  public KundeStatus getStatus()
  {
    return status;
  }

  public void setStatus(KundeStatus status)
  {
    this.status = status;
  }

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
   * @see org.projectforge.core.ShortDisplayNameCapable#getShortDisplayName()
   * @see KostFormatter#format(KundeDO)
   */
  @Transient
  public String getShortDisplayName()
  {
    return KostFormatter.formatKunde(this);
  }
}
