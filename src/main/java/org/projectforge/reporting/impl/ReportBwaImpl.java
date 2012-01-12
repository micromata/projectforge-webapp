/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.reporting.impl;

import org.projectforge.fibu.kost.Bwa;
import org.projectforge.fibu.kost.BwaZeileId;
import org.projectforge.reporting.ReportBwa;
import org.projectforge.reporting.ReportBwaZeile;


public class ReportBwaImpl implements ReportBwa
{
  private final ReportBwaZeile umsatzerloese;

  private final ReportBwaZeile bestVerdg;

  private final ReportBwaZeile aktEigenleistungen;

  private final ReportBwaZeile gesamtleistung;

  private final ReportBwaZeile matWareneinkauf;

  private final ReportBwaZeile rohertrag;

  private final ReportBwaZeile soBetrErloese;

  private final ReportBwaZeile betrieblRohertrag;

  private final ReportBwaZeile kostenarten;

  private final ReportBwaZeile personalkosten;

  private final ReportBwaZeile raumkosten;

  private final ReportBwaZeile betrieblSteuern;

  private final ReportBwaZeile versichBeitraege;

  private final ReportBwaZeile fremdleistungen;

  private final ReportBwaZeile kfzKosten;

  private final ReportBwaZeile werbeReisekosten;

  private final ReportBwaZeile kostenWarenabgabe;

  private final ReportBwaZeile abschreibungen;

  private final ReportBwaZeile reparaturInstandh;

  private final ReportBwaZeile sonstigeKosten;

  private final ReportBwaZeile gesamtkosten;

  private final ReportBwaZeile betriebsErgebnis;

  private final ReportBwaZeile zinsaufwand;

  private final ReportBwaZeile sonstNeutrAufw;

  private final ReportBwaZeile neutralerAufwand;

  private final ReportBwaZeile zinsertraege;

  private final ReportBwaZeile sonstNeutrErtr;

  private final ReportBwaZeile verrKalkKosten;

  private final ReportBwaZeile neutralerErtrag;

  private final ReportBwaZeile kontenklUnbesetzt;

  private final ReportBwaZeile ergebnisVorSteuern;

  private final ReportBwaZeile steuernEinkUErtr;

  private final ReportBwaZeile vorlaeufigesErgebnis;

  public ReportBwaImpl(final Bwa bwa)
  {
    this.umsatzerloese = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.UMSATZERLOESE));
    this.bestVerdg = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.BEST_VERDG));
    this.aktEigenleistungen = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.AKT_EIGENLEISTUNGEN));
    this.gesamtleistung = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.GESAMTLEISTUNG));
    this.matWareneinkauf = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.MAT_WARENEINKAUF));
    this.rohertrag = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.ROHERTRAG));
    this.soBetrErloese = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.SO_BETR_ERLOESE));
    this.betrieblRohertrag = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.BETRIEBL_ROHERTRAG));
    this.kostenarten = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.KOSTENARTEN));
    this.personalkosten = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.PERSONALKOSTEN));
    this.raumkosten = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.RAUMKOSTEN));
    this.betrieblSteuern = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.BETRIEBL_STEUERN));
    this.versichBeitraege = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.VERSICH_BEITRAEGE));
    this.fremdleistungen = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.FREMDLEISTUNGEN));
    this.kfzKosten = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.KFZ_KOSTEN));
    this.werbeReisekosten = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.WERBE_REISEKOSTEN));
    this.kostenWarenabgabe = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.KOSTEN_WARENABGABE));
    this.abschreibungen = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.ABSCHREIBUNGEN));
    this.reparaturInstandh = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.REPARATUR_INSTANDH));
    this.sonstigeKosten = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.SONSTIGE_KOSTEN));
    this.gesamtkosten = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.GESAMTKOSTEN));
    this.betriebsErgebnis = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.BETRIEBSERGEBNIS));
    this.zinsaufwand = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.ZINSAUFWAND));
    this.sonstNeutrAufw = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.SONST_NEUTR_AUFW));
    this.neutralerAufwand = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.NEUTRALER_AUFWAND));
    this.zinsertraege = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.ZINSERTRAEGE));
    this.sonstNeutrErtr = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.SONST_NEUTR_ERTR));
    this.verrKalkKosten = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.VERR_KALK_KOSTEN));
    this.neutralerErtrag = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.NEUTRALER_ERTRAG));
    this.kontenklUnbesetzt = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.KONTENKL_UNBESETZT));
    this.ergebnisVorSteuern = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.ERGEBNIS_VOR_STEUERN));
    this.steuernEinkUErtr = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.STEUERN_EINK_U_ERTR));
    this.vorlaeufigesErgebnis = new ReportBwaZeileImpl(bwa.getZeile(BwaZeileId.VORLAEUFIGES_ERGEBNIS));
  }

  public ReportBwaZeile getUmsatzerloese()
  {
    return umsatzerloese;
  }

  public ReportBwaZeile getAbschreibungen()
  {
    return abschreibungen;
  }

  public ReportBwaZeile getAktEigenleistungen()
  {
    return aktEigenleistungen;
  }

  public ReportBwaZeile getFremdleistungen()
  {
    return fremdleistungen;
  }

  public ReportBwaZeile getBestVerdg()
  {
    return bestVerdg;
  }

  public ReportBwaZeile getBetrieblRohertrag()
  {
    return betrieblRohertrag;
  }

  public ReportBwaZeile getBetrieblSteuern()
  {
    return betrieblSteuern;
  }

  public ReportBwaZeile getBetriebsErgebnis()
  {
    return betriebsErgebnis;
  }

  public ReportBwaZeile getErgebnisVorSteuern()
  {
    return ergebnisVorSteuern;
  }

  public ReportBwaZeile getGesamtkosten()
  {
    return gesamtkosten;
  }

  public ReportBwaZeile getGesamtleistung()
  {
    return gesamtleistung;
  }

  public ReportBwaZeile getKfzKosten()
  {
    return kfzKosten;
  }

  public ReportBwaZeile getKontenklUnbesetzt()
  {
    return kontenklUnbesetzt;
  }

  public ReportBwaZeile getKostenWarenabgabe()
  {
    return kostenWarenabgabe;
  }

  public ReportBwaZeile getKostenarten()
  {
    return kostenarten;
  }

  public ReportBwaZeile getMatWareneinkauf()
  {
    return matWareneinkauf;
  }

  public ReportBwaZeile getNeutralerAufwand()
  {
    return neutralerAufwand;
  }

  public ReportBwaZeile getNeutralerErtrag()
  {
    return neutralerErtrag;
  }

  public ReportBwaZeile getPersonalkosten()
  {
    return personalkosten;
  }

  public ReportBwaZeile getRaumkosten()
  {
    return raumkosten;
  }

  public ReportBwaZeile getReparaturInstandh()
  {
    return reparaturInstandh;
  }

  public ReportBwaZeile getRohertrag()
  {
    return rohertrag;
  }

  public ReportBwaZeile getSoBetrErloese()
  {
    return soBetrErloese;
  }

  public ReportBwaZeile getSonstNeutrAufw()
  {
    return sonstNeutrAufw;
  }

  public ReportBwaZeile getSonstNeutrErtr()
  {
    return sonstNeutrErtr;
  }

  public ReportBwaZeile getSonstigeKosten()
  {
    return sonstigeKosten;
  }

  public ReportBwaZeile getSteuernEinkUErtr()
  {
    return steuernEinkUErtr;
  }

  public ReportBwaZeile getVerrKalkKosten()
  {
    return verrKalkKosten;
  }

  public ReportBwaZeile getVersichBeitraege()
  {
    return versichBeitraege;
  }

  public ReportBwaZeile getVorlaeufigesErgebnis()
  {
    return vorlaeufigesErgebnis;
  }

  public ReportBwaZeile getWerbeReisekosten()
  {
    return werbeReisekosten;
  }

  public ReportBwaZeile getZinsaufwand()
  {
    return zinsaufwand;
  }

  public ReportBwaZeile getZinsertraege()
  {
    return zinsertraege;
  }
}
