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

package org.projectforge.fibu;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.DateHolder;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.PFPersistancyBehavior;
import org.projectforge.fibu.kost.KostZuweisungDO;

@MappedSuperclass
public abstract class AbstractRechnungDO<T extends AbstractRechnungsPositionDO> extends DefaultBaseDO
{
  private static final long serialVersionUID = -8936320220788212987L;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  protected String betreff;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  protected String bemerkung;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  protected String besonderheiten;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  protected Date datum;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  protected Date faelligkeit;

  @Field(index = Index.UN_TOKENIZED)
  protected transient Integer zahlungsZielInTagen;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  protected Date bezahlDatum;

  @Field(index = Index.UN_TOKENIZED)
  protected BigDecimal zahlBetrag;

  @PFPersistancyBehavior(autoUpdateCollectionEntries = true)
  @IndexedEmbedded(depth = 2)
  protected List<T> positionen = null;

  protected String uiStatusAsXml;

  protected RechnungUIStatus uiStatus;

  @Override
  public void recalculate()
  {
    if (this.datum == null || this.faelligkeit == null) {
      this.zahlungsZielInTagen = null;
      return;
    }
    final DateHolder date = new DateHolder(this.datum);
    this.zahlungsZielInTagen = date.daysBetween(this.faelligkeit);
  }

  @Column(length = 4000)
  public String getBetreff()
  {
    return betreff;
  }

  public AbstractRechnungDO<T> setBetreff(final String betreff)
  {
    this.betreff = betreff;
    return this;
  }

  @Column(length = 4000)
  public String getBesonderheiten()
  {
    return besonderheiten;
  }

  public AbstractRechnungDO<T> setBesonderheiten(final String besonderheiten)
  {
    this.besonderheiten = besonderheiten;
    return this;
  }

  @Column(length = 4000)
  public String getBemerkung()
  {
    return bemerkung;
  }

  public AbstractRechnungDO<T> setBemerkung(final String bemerkung)
  {
    this.bemerkung = bemerkung;
    return this;
  }

  @Column(nullable = false)
  public Date getDatum()
  {
    return datum;
  }

  public AbstractRechnungDO<T> setDatum(final Date datum)
  {
    this.datum = datum;
    return this;
  }

  @Column
  public Date getFaelligkeit()
  {
    return faelligkeit;
  }

  public AbstractRechnungDO<T> setFaelligkeit(final Date faelligkeit)
  {
    this.faelligkeit = faelligkeit;
    return this;
  }

  /**
   * Wird nur zur Berechnung benutzt und kann für die Anzeige aufgerufen werden. Vorher sollte recalculate aufgerufen werden.
   * @see #recalculate()
   */
  @Transient
  public Integer getZahlungsZielInTagen()
  {
    return zahlungsZielInTagen;
  }

  public AbstractRechnungDO<T> setZahlungsZielInTagen(final Integer zahlungsZielInTagen)
  {
    this.zahlungsZielInTagen = zahlungsZielInTagen;
    return this;
  }

  @Transient
  public BigDecimal getGrossSum()
  {
    BigDecimal brutto = BigDecimal.ZERO;
    if (this.positionen != null) {
      for (final T position : this.positionen) {
        brutto = brutto.add(position.getBruttoSum());
      }
    }
    return brutto;
  }

  @Transient
  public BigDecimal getNetSum()
  {
    BigDecimal netto = BigDecimal.ZERO;
    if (this.positionen != null) {
      for (final T position : this.positionen) {
        netto = netto.add(position.getNetSum());
      }
    }
    return netto;
  }

  @Transient
  public BigDecimal getVatAmountSum()
  {
    BigDecimal vatAmount = BigDecimal.ZERO;
    if (this.positionen != null) {
      for (final T position : this.positionen) {
        vatAmount = vatAmount.add(position.getVatAmount());
      }
    }
    return vatAmount;
  }

  @Column(name = "bezahl_datum")
  public Date getBezahlDatum()
  {
    return bezahlDatum;
  }

  public AbstractRechnungDO<T> setBezahlDatum(final Date bezahlDatum)
  {
    this.bezahlDatum = bezahlDatum;
    return this;
  }

  /**
   * Bruttobetrag, den der Kunde bezahlt hat.
   * @return
   */
  @Column(name = "zahl_betrag")
  public BigDecimal getZahlBetrag()
  {
    return zahlBetrag;
  }

  public AbstractRechnungDO<T> setZahlBetrag(final BigDecimal zahlBetrag)
  {
    this.zahlBetrag = zahlBetrag;
    return this;
  }

  @Transient
  public abstract boolean isBezahlt();

  @Transient
  public boolean isUeberfaellig()
  {
    if (isBezahlt() == true) {
      return false;
    }
    final DayHolder today = new DayHolder();
    return (this.faelligkeit == null || this.faelligkeit.before(today.getDate()) == true);
  }

  @Transient
  public abstract List<T> getPositionen();

  public AbstractRechnungDO<T> setPositionen(final List<T> positionen)
  {
    this.positionen = positionen;
    return this;
  }

  /**
   * @param idx
   * @return PositionDO with given index or null, if not exist.
   */
  public T getPosition(final int idx)
  {
    if (positionen == null) {
      return null;
    }
    if (idx >= positionen.size()) { // Index out of bounds.
      return null;
    }
    return positionen.get(idx);
  }

  public AbstractRechnungDO<T> addPosition(final T position)
  {
    ensureAndGetPositionen();
    short number = 1;
    for (final T pos : positionen) {
      if (pos.getNumber() >= number) {
        number = pos.getNumber();
        number++;
      }
    }
    position.setNumber(number);
    position.setRechnung(this);
    this.positionen.add(position);
    return this;
  }

  public List<T> ensureAndGetPositionen()
  {
    {
      if (this.positionen == null) {
        setPositionen(new ArrayList<T>());
      }
      return getPositionen();
    }
  }

  /**
   * @return The total sum of all cost assignment net amounts of all positions.
   */
  @Transient
  public BigDecimal getKostZuweisungenNetSum()
  {
    if (this.positionen == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal netSum = BigDecimal.ZERO;
    for (final T pos : this.positionen) {
      if (CollectionUtils.isNotEmpty(pos.kostZuweisungen) == true) {
        for (final KostZuweisungDO zuweisung : pos.kostZuweisungen) {
          if (zuweisung.getNetto() != null) {
            netSum = netSum.add(zuweisung.getNetto());
          }
        }
      }
    }
    return netSum;
  }

  @Transient
  public BigDecimal getKostZuweisungFehlbetrag()
  {
    return getKostZuweisungenNetSum().subtract(getNetSum());
  }

  public boolean hasKostZuweisungen()
  {
    if (this.positionen == null) {
      return false;
    }
    for (final T pos : this.positionen) {
      if (CollectionUtils.isNotEmpty(pos.kostZuweisungen) == true) {
        return true;
      }
    }
    return false;
  }

  /**
   * The user interface status of an invoice. The {@link RechnungUIStatus} is stored as XML.
   * @return the XML representation of the uiStatus.
   * @see RechnungUIStatus
   */
  @Column(name = "ui_status_as_xml", length = 10000)
  public String getUiStatusAsXml()
  {
    return uiStatusAsXml;
  }

  /**
   * @param uiStatus the uiStatus to set
   * @return this for chaining.
   */
  public AbstractRechnungDO<T> setUiStatusAsXml(final String uiStatus)
  {
    this.uiStatusAsXml = uiStatus;
    return this;
  }

  /**
   * @return the rechungUiStatus
   */
  @Transient
  public RechnungUIStatus getUiStatus()
  {
    if (uiStatus == null) {
      uiStatus = new RechnungUIStatus();
    }
    return uiStatus;
  }

  /**
   * @param rechungUiStatus the rechungUiStatus to set
   * @return this for chaining.
   */
  public AbstractRechnungDO<T> setUiStatus(final RechnungUIStatus uiStatus)
  {
    this.uiStatus = uiStatus;
    return this;
  }
}
