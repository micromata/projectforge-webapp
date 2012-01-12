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

package org.projectforge.fibu.kost;

public enum BwaZeileId
{
  UMSATZERLOESE(1020, "umsatzErloese", "Umsatzerlöse"), //
  BEST_VERDG(1040, "bestVerdg", "Best.Verdg. FE/UE"), //
  AKT_EIGENLEISTUNGEN(1045, "aktEigenleistungen", "Akt.Eigenleistungen"), //
  GESAMTLEISTUNG(1051, "gesamtleistung", "Gesamtleistung"), //
  MAT_WARENEINKAUF(1060, "matWareneinkauf", "Mat./Wareneinkauf"), //
  ROHERTRAG(1080, "rohertrag", "Rohertrag"), //
  SO_BETR_ERLOESE(1090, "soBetrErloese", "So. betr. Erlöse"), //
  BETRIEBL_ROHERTRAG(1092, "betrieblRohertrag", "Betriebl. Rohertrag"), //
  KOSTENARTEN(1094, "kostenarten", "Kostenarten"), //
  PERSONALKOSTEN(1100, "personalkosten", "Personalkosten"), //
  RAUMKOSTEN(1120, "raumkosten", "Raumkosten"), //
  BETRIEBL_STEUERN(1140, "betrieblSteuern", "Betriebl. Steuern"), //
  VERSICH_BEITRAEGE(1150, "versichBeitraege", "Versich./Beiträge"), //
  FREMDLEISTUNGEN(1160, "fremdleistungen", "Fremdleistungen"), //
  KFZ_KOSTEN(1180, "kfzKosten", "Kfz-Kosten (o. St.)"), //
  WERBE_REISEKOSTEN(1200, "werbeReisekosten", "Werbe-/Reisekosten"), //
  KOSTEN_WARENABGABE(1220, "kostenWarenabgabe", "Kosten Warenabgabe"), //
  ABSCHREIBUNGEN(1240, "abschreibungen", "Abschreibungen"), //
  REPARATUR_INSTANDH(1250, "reparaturInstandh", "Reparatur/Instandh."), //
  SONSTIGE_KOSTEN(1260, "sonstigeKosten", "sonstige Kosten"), //
  GESAMTKOSTEN(1280, "gesamtKosten", "Gesamtkosten"), //
  BETRIEBSERGEBNIS(1300, "betriebsErgebnis", "Betriebsergebnis"), //
  ZINSAUFWAND(1310, "zinsaufwand", "Zinsaufwand"), //
  SONST_NEUTR_AUFW(1312, "sonstNeutrAufw", "Sonst. neutr. Aufw."), //
  NEUTRALER_AUFWAND(1320, "neutralerAufwand", "Neutraler Aufwand"), //
  ZINSERTRAEGE(1322, "zinsertraege", "Zinserträge"), //
  SONST_NEUTR_ERTR(1323, "sonstNeutrErtr", "Sonst. neutr. Ertr"), //
  VERR_KALK_KOSTEN(1324, "verrKalkKosten", "Verr. kalk. Kosten"), //
  NEUTRALER_ERTRAG(1330, "neutralerErtrag", "Neutraler Ertrag"), //
  KONTENKL_UNBESETZT(1340, "kontenklUnbesetzt", "Kontenkl. unbesetzt"), //
  ERGEBNIS_VOR_STEUERN(1345, "ergebnisVorSteuern", "Ergebnis vor Steuern"), //
  STEUERN_EINK_U_ERTR(1355, "steuernEinkUErtr", "Steuern Eink.u.Ertr"), //
  VORLAEUFIGES_ERGEBNIS(1380, "vorlaeufigesErgebnis", "Vorläufiges Ergebnis");

  public BwaZeileId getBwaZeileId(final int id)
  {
    for (final BwaZeileId bwaZeileId : BwaZeileId.values()) {
      if (bwaZeileId.id == id) {
        return bwaZeileId;
      }
    }
    throw new UnsupportedOperationException("BwaZeileId '" + id + "' not found.");
  }

  private int id;

  private String key;

  private String bezeichnung;

  public String getKey()
  {
    return key;
  }

  public String getBezeichnung()
  {
    return bezeichnung;
  }

  public int getId()
  {
    return id;
  }

  BwaZeileId(final int id, final String key, final String bezeichnung)
  {
    this.id = id;
    this.key = key;
    this.bezeichnung = bezeichnung;
  }
}
