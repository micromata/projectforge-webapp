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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.AbstractHistorizableBaseDO;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.PFPersistancyBehavior;
import org.projectforge.user.PFUserDO;

/**
 * Repräsentiert einen Auftrag oder ein Angebot. Ein Angebot kann abgelehnt oder durch ein anderes ersetzt werden, muss also nicht zum
 * tatsächlichen Auftrag werden. Wichtig ist: Alle Felder sind historisiert, so dass Änderungen wertvolle Informationen enthalten, wie
 * beispielsweise die Beauftragungshistorie: LOI am 05.03.08 durch Herrn Müller und schriftlich am 04.04.08 durch Beschaffung.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Indexed
@Table(name = "t_fibu_auftrag")
public class AuftragDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -3114903689890703366L;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Integer nummer;

  /** Dies sind die alten Auftragsnummern oder Kundenreferenzen. */
  @Fields({ @Field(index = Index.TOKENIZED, name = "referenz_tokenized", store = Store.NO),
    @Field(index = Index.UN_TOKENIZED, store = Store.NO)})
  private String referenz;

  @PFPersistancyBehavior(autoUpdateCollectionEntries = true)
  @IndexedEmbedded(depth = 1)
  private List<AuftragsPositionDO> positionen = null;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private AuftragsStatus auftragsStatus;

  @IndexedEmbedded(depth = 1)
  private PFUserDO contactPerson;

  @IndexedEmbedded(depth = 1)
  private KundeDO kunde;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String kundeText;

  @IndexedEmbedded(depth = 2)
  private ProjektDO projekt;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String titel;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String bemerkung;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String statusBeschreibung;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date angebotsDatum;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date bindungsFrist;

  private String beauftragungsBeschreibung;

  private Date beauftragungsDatum;

  private BigDecimal fakturiertSum = null;

  protected String uiStatusAsXml;

  protected AuftragUIStatus uiStatus;

  static {
    AbstractHistorizableBaseDO.putNonHistorizableProperty(AuftragDO.class, "uiStatusAsXml", "uiStatus");
  }

  /**
   * Datum der Angebotslegung.
   * @return
   */
  @Column(name = "angebots_datum")
  public Date getAngebotsDatum()
  {
    return angebotsDatum;
  }

  public AuftragDO setAngebotsDatum(final Date angebotsDatum)
  {
    this.angebotsDatum = angebotsDatum;
    return this;
  }

  @Column(name = "bindungs_frist")
  public Date getBindungsFrist()
  {
    return bindungsFrist;
  }

  public AuftragDO setBindungsFrist(final Date bindungsFrist)
  {
    this.bindungsFrist = bindungsFrist;
    return this;
  }

  /**
   * Wann wurde beauftragt? Beachte: Alle Felder historisiert, so dass hier ein Datum z. B. mit dem LOI und später das Datum der
   * schriftlichen Beauftragung steht.
   */
  @Column(name = "beauftragungs_datum")
  public Date getBeauftragungsDatum()
  {
    return beauftragungsDatum;
  }

  public AuftragDO setBeauftragungsDatum(final Date beauftragungsDatum)
  {
    this.beauftragungsDatum = beauftragungsDatum;
    return this;
  }

  /**
   * Adds all net sums of the positions (without not ordered positions) and return the total sum.
   */
  @Transient
  public BigDecimal getNettoSumme()
  {
    if (positionen == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (final AuftragsPositionDO position : positionen) {
      final BigDecimal nettoSumme = position.getNettoSumme();
      if (nettoSumme != null && position.getStatus() != AuftragsPositionsStatus.NICHT_BEAUFTRAGT) {
        sum = sum.add(nettoSumme);
      }
    }
    return sum;
  }

  /**
   * Auftragsnummer ist eindeutig und wird fortlaufend erzeugt.
   */
  @Column(unique = true, nullable = false)
  public Integer getNummer()
  {
    return nummer;
  }

  public AuftragDO setNummer(final Integer nummer)
  {
    this.nummer = nummer;
    return this;
  }

  @Column(length = 255)
  public String getReferenz()
  {
    return referenz;
  }

  public AuftragDO setReferenz(final String referenz)
  {
    this.referenz = referenz;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 30)
  public AuftragsStatus getAuftragsStatus()
  {
    return auftragsStatus;
  }

  /**
   * @return FAKTURIERT if isVollstaendigFakturiert == true, otherwise AuftragsStatus as String.
   */
  @Transient
  public String getAuftragsStatusAsString()
  {
    if (isVollstaendigFakturiert() == true)
      return "FAKTURIERT";
    return auftragsStatus != null ? auftragsStatus.toString() : null;
  }

  public AuftragDO setAuftragsStatus(final AuftragsStatus auftragsStatus)
  {
    this.auftragsStatus = auftragsStatus;
    return this;
  }

  /**
   * Wer hat wann und wie beauftragt? Z. B. Beauftragung per E-Mail durch Herrn Müller.
   * @return
   */
  @Column(name = "beauftragungs_beschreibung", length = 4000)
  public String getBeauftragungsBeschreibung()
  {
    return beauftragungsBeschreibung;
  }

  public AuftragDO setBeauftragungsBeschreibung(final String beauftragungsBeschreibung)
  {
    this.beauftragungsBeschreibung = beauftragungsBeschreibung;
    return this;
  }

  @Column(length = 4000, name = "status_beschreibung")
  public String getStatusBeschreibung()
  {
    return statusBeschreibung;
  }

  public AuftragDO setStatusBeschreibung(final String statusBeschreibung)
  {
    this.statusBeschreibung = statusBeschreibung;
    return this;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "kunde_fk", nullable = true)
  public KundeDO getKunde()
  {
    return kunde;
  }

  public AuftragDO setKunde(final KundeDO kunde)
  {
    this.kunde = kunde;
    return this;
  }

  @Transient
  public Integer getKundeId()
  {
    if (this.kunde == null)
      return null;
    return kunde.getId();
  }

  /**
   * @see ProjektFormatter#formatProjektKundeAsString(ProjektDO, KundeDO, String)
   */
  @Transient
  public String getProjektKundeAsString()
  {
    return ProjektFormatter.formatProjektKundeAsString(this.projekt, this.kunde, this.kundeText);
  }

  /**
   * @see KundeFormatter#formatKundeAsString(KundeDO, String)
   */
  @Transient
  public String getKundeAsString()
  {
    return KundeFormatter.formatKundeAsString(this.kunde, this.kundeText);
  }

  @Transient
  public String getProjektAsString()
  {
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    if (this.projekt != null) {
      if (projekt.getKunde() != null) {
        if (first == true)
          first = false;
        else buf.append("; ");
        buf.append(projekt.getKunde().getName());
      }
      if (StringUtils.isNotBlank(projekt.getName()) == true) {
        if (first == true)
          first = false;
        else buf.append(" - ");
        buf.append(projekt.getName());
      }
    }
    return buf.toString();
  }

  /**
   * Freitextfeld, falls Kunde nicht aus Liste gewählt werden kann bzw. für Rückwärtskompatibilität mit alten Kunden.
   */
  @Column(name = "kunde_text", length = 1000)
  public String getKundeText()
  {
    return kundeText;
  }

  public AuftragDO setKundeText(final String kundeText)
  {
    this.kundeText = kundeText;
    return this;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "projekt_fk", nullable = true)
  public ProjektDO getProjekt()
  {
    return projekt;
  }

  public AuftragDO setProjekt(final ProjektDO projekt)
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

  public AuftragDO setTitel(final String titel)
  {
    this.titel = titel;
    return this;
  }

  @Column(name = "titel", length = 1000)
  public String getTitel()
  {
    return titel;
  }

  @Column(length = 4000)
  public String getBemerkung()
  {
    return bemerkung;
  }

  public AuftragDO setBemerkung(final String bemerkung)
  {
    this.bemerkung = bemerkung;
    return this;
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "contact_person_fk", nullable = true)
  public PFUserDO getContactPerson()
  {
    return contactPerson;
  }

  public AuftragDO setContactPerson(final PFUserDO contactPerson)
  {
    this.contactPerson = contactPerson;
    return this;
  }

  @Transient
  public Integer getContactPersonId()
  {
    if (this.contactPerson == null)
      return null;
    return contactPerson.getId();
  }

  /**
   * @return true wenn alle Auftragspositionen vollständig fakturiert sind.
   * @see AuftragsPositionDO#isCompleteInvoiced()
   */
  @Transient
  public boolean isVollstaendigFakturiert()
  {
    if (positionen == null || auftragsStatus != AuftragsStatus.ABGESCHLOSSEN) {
      return false;
    }
    for (final AuftragsPositionDO position : positionen) {
      if (position.isVollstaendigFakturiert() == false
          && (position.getStatus() == null || position.getStatus().isIn(AuftragsPositionsStatus.NICHT_BEAUFTRAGT) == false)) {
        return false;
      }
    }
    return true;
  }

  @Transient
  public boolean isAbgeschlossenUndNichtVollstaendigFakturiert()
  {
    if (getAuftragsStatus().isIn(AuftragsStatus.ABGESCHLOSSEN) == true && isVollstaendigFakturiert() == false) {
      return true;
    }
    if (getPositionen() != null) {
      for (final AuftragsPositionDO pos : getPositionen()) {
        if (pos.getStatus() == AuftragsPositionsStatus.ABGESCHLOSSEN && pos.isVollstaendigFakturiert() == false) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Get the position entries for this object.
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "auftrag")
  @IndexColumn(name = "number", base = 1)
  public List<AuftragsPositionDO> getPositionen()
  {
    return this.positionen;
  }

  /**
   * @param number
   * @return AuftragsPositionDO with given position number or null (iterates through the list of positions and compares the number), if not
   *         exist.
   */
  public AuftragsPositionDO getPosition(final short number)
  {
    if (positionen == null) {
      return null;
    }
    for (final AuftragsPositionDO position : this.positionen) {
      if (position.getNumber() == number) {
        return position;
      }
    }
    return null;
  }

  public AuftragDO setPositionen(final List<AuftragsPositionDO> positionen)
  {
    this.positionen = positionen;
    return this;
  }

  public AuftragDO addPosition(final AuftragsPositionDO position)
  {
    ensureAndGetPositionen();
    short number = 1;
    for (final AuftragsPositionDO pos : positionen) {
      if (pos.getNumber() >= number) {
        number = pos.getNumber();
        number++;
      }
    }
    position.setNumber(number);
    position.setAuftrag(this);
    this.positionen.add(position);
    return this;
  }

  public List<AuftragsPositionDO> ensureAndGetPositionen()
  {
    if (this.positionen == null) {
      setPositionen(new ArrayList<AuftragsPositionDO>());
    }
    return getPositionen();
  }

  /**
   * @return The sum of person days of all positions.
   */
  @Transient
  public BigDecimal getPersonDays()
  {
    BigDecimal result = BigDecimal.ZERO;
    if (this.positionen != null) {
      for (final AuftragsPositionDO pos : this.positionen) {
        if (pos.getPersonDays() != null) {
          result = result.add(pos.getPersonDays());
        }
      }
    }
    return result;
  }

  /**
   * Sums all positions. Must be set in all positions before usage. The value is not calculated automatically!
   * @see AuftragDao#calculateInvoicedSum(java.util.Collection)
   */
  @Transient
  public BigDecimal getFakturiertSum()
  {
    if (this.fakturiertSum == null) {
      this.fakturiertSum = BigDecimal.ZERO;
      if (positionen != null) {
        for (final AuftragsPositionDO pos : positionen) {
          if (NumberHelper.isNotZero(pos.getFakturiertSum()) == true) {
            this.fakturiertSum = this.fakturiertSum.add(pos.getFakturiertSum());
          }
        }
      }
    }
    return this.fakturiertSum;
  }

  public AuftragDO setFakturiertSum(final BigDecimal fakturiertSum)
  {
    this.fakturiertSum = fakturiertSum;
    return this;
  }

  /**
   * The user interface status of an order. The {@link AuftragUIStatus} is stored as XML.
   * @return the XML representation of the uiStatus.
   * @see AuftragUIStatus
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
  public AuftragDO setUiStatusAsXml(final String uiStatus)
  {
    this.uiStatusAsXml = uiStatus;
    return this;
  }

  /**
   * @return the rechungUiStatus
   */
  @Transient
  public AuftragUIStatus getUiStatus()
  {
    if (uiStatus == null) {
      uiStatus = new AuftragUIStatus();
    }
    return uiStatus;
  }

  /**
   * @param rechungUiStatus the rechungUiStatus to set
   * @return this for chaining.
   */
  public AuftragDO setUiStatus(final AuftragUIStatus uiStatus)
  {
    this.uiStatus = uiStatus;
    return this;
  }
}
