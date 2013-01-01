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
import java.math.RoundingMode;
import java.util.Date;

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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.common.StringHelper;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.fibu.KontoDO;

import de.micromata.hibernate.history.Historizable;

/**
 * Repräsentiert einen importierten Datev-Buchungssatz. Die Buchungssätze bilden die Grundlage für betriebwirtschaftliche Auswertungen.
 */
@Entity
@Indexed
@Table(name = "t_fibu_buchungssatz", uniqueConstraints = { @UniqueConstraint(columnNames = { "year", "month", "satznr"})})
public class BuchungssatzDO extends DefaultBaseDO implements Historizable, Comparable<BuchungssatzDO>
{
  private static final long serialVersionUID = 8634592782531883482L;

  private static final Logger log = Logger.getLogger(BuchungssatzDO.class);

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Integer year;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Integer month;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Integer satznr;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private BigDecimal betrag;

  @Field(index = Index.UN_TOKENIZED)
  private SHType sh;

  private boolean ignore = false;

  @IndexedEmbedded(depth = 1)
  private KontoDO konto;

  @IndexedEmbedded(depth = 1)
  private KontoDO gegenKonto;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date datum;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String beleg;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String text;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String menge;

  @IndexedEmbedded(depth = 1)
  private Kost1DO kost1;

  @IndexedEmbedded(depth = 3)
  private Kost2DO kost2;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  /**
   * In form yyyy-mm-###
   */
  @Transient
  public final String getFormattedSatzNummer()
  {
    return String.valueOf(year) + '-' + StringHelper.format2DigitNumber(month + 1) + '-' + formatSatzNr();
  }

  public final String formatSatzNr()
  {
    if (satznr == null) {
      return "";
    }
    return StringUtils.leftPad(String.valueOf(satznr), 5, '0');
  }

  /**
   * Führt nach der Datev-/Steffi-Logik Betrachtungen durch, ob dieser Datensatz berücksichtigt werden muss bzw. ob die Betrag im Haben oder
   * im Soll anzuwenden ist.
   */
  public void calculate()
  {
    if (konto == null || kost2 == null) {
      log.warn("Can't calculate Buchungssatz, because konto or kost2 is not given (for import it will be detected, OK): " + this);
      return;
    }
    int kto = konto.getNummer();
    SHType sollHaben = sh;
    if (kto >= 4400 && kto <= 4499) {
      // Konto 4400 - 4499 werden im Haben gebucht: Umsatz.
    }
    if (kto >= 5900 && kto <= 5999) {
      // Fremdleistungen
    }
    if (sollHaben == SHType.SOLL) {
      betrag = betrag.negate();
    }
    if (kost2.isEqual(1, 0, 0, 0) == true && kto >= 6000 && kto <= 6299) { // "1.000.00.00"
      // Bei diesen Buchungen handelt es sich um Kontrollbuchungen mit dem Gegenkonto 3790, was wir hier nochmals prüfen:
      if (gegenKonto.getNummer() != 3790) {
        // log.error("Bei dieser Buchung ist das Gegenkonto nicht 3790, wie von der Buchhaltung mitgeteilt "
        // + "(deshalb wird dieser Datensatz nicht ignoriert!");
      } else {
        ignore = true;
      }
    }
  }

  /** Der Buchungstext. */
  @Column(length = 255, name = "buchungstext")
  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  /** Je nach Buchungssatz: Belegnummer / Referenznummer / Rechnungsnummer. */
  @Column(length = 255)
  public String getBeleg()
  {
    return beleg;
  }

  public void setBeleg(String beleg)
  {
    this.beleg = beleg;
  }

  @Column(nullable = false)
  public BigDecimal getBetrag()
  {
    return betrag;
  }

  public void setBetrag(BigDecimal betrag)
  {
    this.betrag = betrag != null ? betrag.setScale(2, RoundingMode.HALF_UP) : null;
  }

  @Column(nullable = false)
  public Date getDatum()
  {
    return datum;
  }

  public void setDatum(Date datum)
  {
    this.datum = datum;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "konto_id", nullable = false)
  public KontoDO getKonto()
  {
    return konto;
  }

  public void setKonto(KontoDO konto)
  {
    this.konto = konto;
  }

  @Transient
  public Integer getKontoId()
  {
    return this.konto != null ? this.konto.getId() : null;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "gegenkonto_id", nullable = false)
  public KontoDO getGegenKonto()
  {
    return gegenKonto;
  }

  public void setGegenKonto(KontoDO gegenKonto)
  {
    this.gegenKonto = gegenKonto;
  }

  @Transient
  public Integer getGegenKontoId()
  {
    return this.gegenKonto != null ? this.gegenKonto.getId() : null;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "kost1_id", nullable = false)
  public Kost1DO getKost1()
  {
    return kost1;
  }

  public void setKost1(Kost1DO kost1)
  {
    this.kost1 = kost1;
  }

  @Transient
  public Integer getKost1Id()
  {
    return this.kost1 != null ? this.kost1.getId() : null;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "kost2_id", nullable = false)
  public Kost2DO getKost2()
  {
    return kost2;
  }

  public void setKost2(Kost2DO kost2)
  {
    this.kost2 = kost2;
  }

  @Transient
  public Integer getKost2Id()
  {
    return this.kost2 != null ? this.kost2.getId() : null;
  }

  @Column(nullable = false)
  public Integer getSatznr()
  {
    return satznr;
  }

  public void setSatznr(Integer satznr)
  {
    this.satznr = satznr;
  }

  /**
   * Monat zu der die Buchung gehört.
   * @return
   */
  @Column(nullable = false)
  public Integer getMonth()
  {
    return month;
  }

  public void setMonth(Integer month)
  {
    this.month = month;
  }

  /**
   * Jahr zu der die Buchung gehört.
   * @return
   */
  @Column(nullable = false)
  public Integer getYear()
  {
    return year;
  }

  public void setYear(Integer year)
  {
    this.year = year;
  }

  @Transient
  public boolean isIgnore()
  {
    return ignore;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 7, nullable = false)
  public SHType getSh()
  {
    return sh;
  }

  public void setSH(String value)
  {
    if ("S".equals(value) == true) {
      sh = SHType.SOLL;
    } else if ("H".equals(value) == true) {
      sh = SHType.HABEN;
    } else {
      String msg = "Haben / Soll-Wert ist undefiniert: " + this.toString();
      log.error(msg);
      throw new RuntimeException(msg);
    }
  }

  @Column(length = 255)
  public String getMenge()
  {
    return menge;
  }

  public void setMenge(String menge)
  {
    this.menge = menge;
  }

  public void setSh(SHType sh)
  {
    this.sh = sh;
  }

  @Column(length = 4000)
  public String getComment()
  {
    return comment;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public void setIgnore(boolean ignore)
  {
    this.ignore = ignore;
  }

  public int compareTo(BuchungssatzDO other)
  {
    int r = this.year.compareTo(other.year);
    if (r != 0) {
      return r;
    }
    r = this.month.compareTo(other.month);
    if (r != 0) {
      return r;
    }
    return this.satznr.compareTo(other.satznr);
  }
}
