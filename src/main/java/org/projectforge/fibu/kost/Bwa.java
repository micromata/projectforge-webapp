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
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.Priority;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.reporting.Report;
import org.projectforge.user.PFUserContext;


public class Bwa
{
  private static final Logger log = Logger.getLogger(Bwa.class);

  private String title;

  private String shortname; // Ein Kurzname um z.B. labels oder Dateinamen zu generieren

  private List<BwaZeile> bwaZeilen;

  private int counter = 0;

  private int year;

  private int month;

  private int toMonth = -1;

  private BigDecimal erfolgsquote;

  private BigDecimal relativePerformance;

  private Object reference;

  private boolean storeBuchungssaetzeInZeilen;

  /**
   * Fügt alle namentlichen BwaZeilen der Bwa in die übergebene Map. Nützlich für JasperReport, einmal unter der Bezeichnung und einmal
   * unter der Zeilennummer als key.
   * @param map Key ist die Zeilen
   */
  public static void putBwaWerte(Map<String, Object> map, Bwa bwa)
  {
    for (BwaZeile zeile : bwa.getZeilen()) {
      map.put(String.valueOf(zeile.getZeile()), zeile.getBwaWert());
      if (StringUtils.isNotBlank(zeile.getBezeichnung()) == true) {
        map.put(zeile.getBwaZeileId().getKey(), zeile.getBwaWert());
      }
    }
    map.put("erfolgsquote", bwa.getErfolgsquote());
    map.put("relativePerformance", bwa.getRelativePerformance());
  }

  /**
   * @return { "erfolgsquote", "relativePerformance"}
   */
  public static String[] getAdditionalValues()
  {
    return new String[] { "erfolgsquote", "relativePerformance"};
  }

  /**
   * Fügt Leerzeile hinzu. Die Priorität ist automatisch {@link Priority#LOW}.
   */
  private void addBwaZeile(Integer zeile)
  {
    BwaZeile bwaZeile = new BwaZeile(this, zeile, Priority.LOW, 0);
    bwaZeilen.add(bwaZeile);
  }

  private void addBwaZeile(BwaZeileId id, Priority priority)
  {
    addBwaZeile(id, priority, 0);
  }

  /**
   * @param zeile
   * @param bezeichnung
   * @param indent Einrücktiefe, beginnend bei 0 (default).
   */
  private void addBwaZeile(BwaZeileId id, Priority priority, int indent)
  {
    bwaZeilen.add(new BwaZeile(this, id, priority, indent));
  }

  public BwaZeile getZeile(Integer zeile)
  {
    if (bwaZeilen == null) {
      return null;
    }
    for (BwaZeile bwaZeile : bwaZeilen) {
      if (zeile.intValue() == bwaZeile.getZeile()) {
        return bwaZeile;
      }
    }
    throw new UnsupportedOperationException("Zeile '" + zeile + "' not found.");
  }

  public BwaZeile getZeile(BwaZeileId id)
  {
    return getZeile(id.getId());
  }

  public BwaZeile getZeile(String key)
  {
    for (BwaZeile bwaZeile : bwaZeilen) {
      if (bwaZeile.getBwaZeileId() != null && ObjectUtils.equals(bwaZeile.getBwaZeileId().getKey(), key) == true) {
        return bwaZeile;
      }
    }
    throw new UnsupportedOperationException("Zeile '" + key + "' not found.");
  }

  private BwaZeile get(BwaZeileId id)
  {
    return getZeile(id);
  }

  public Bwa()
  {
    bwaZeilen = new ArrayList<BwaZeile>();
    addBwaZeile(1010); // Leerzeile
    addBwaZeile(BwaZeileId.UMSATZERLOESE, Priority.MIDDLE, 1);
    addBwaZeile(BwaZeileId.BEST_VERDG, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.AKT_EIGENLEISTUNGEN, Priority.LOW, 1);
    addBwaZeile(1050); // Leerzeile
    addBwaZeile(BwaZeileId.GESAMTLEISTUNG, Priority.HIGH);
    addBwaZeile(1052); // Leerzeile
    addBwaZeile(BwaZeileId.MAT_WARENEINKAUF, Priority.HIGH, 1);
    addBwaZeile(1070); // Leerzeile
    addBwaZeile(BwaZeileId.ROHERTRAG, Priority.HIGH);
    addBwaZeile(1081); // Leerzeile
    addBwaZeile(BwaZeileId.SO_BETR_ERLOESE, Priority.LOW, 1);
    addBwaZeile(1091); // Leerzeile
    addBwaZeile(BwaZeileId.BETRIEBL_ROHERTRAG, Priority.MIDDLE);
    addBwaZeile(1093); // Leerzeile
    addBwaZeile(BwaZeileId.KOSTENARTEN, Priority.LOW);
    addBwaZeile(BwaZeileId.PERSONALKOSTEN, Priority.HIGH, 1);
    addBwaZeile(BwaZeileId.RAUMKOSTEN, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.BETRIEBL_STEUERN, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.VERSICH_BEITRAEGE, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.BESONDERE_KOSTEN, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.KFZ_KOSTEN, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.WERBE_REISEKOSTEN, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.KOSTEN_WARENABGABE, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.ABSCHREIBUNGEN, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.REPARATUR_INSTANDH, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.SONSTIGE_KOSTEN, Priority.LOW, 1);
    addBwaZeile(BwaZeileId.GESAMTKOSTEN, Priority.HIGH);
    addBwaZeile(1290); // Leerzeile
    addBwaZeile(BwaZeileId.BETRIEBSERGEBNIS, Priority.HIGH);
    addBwaZeile(1301); // Leerzeile
    addBwaZeile(BwaZeileId.ZINSAUFWAND, Priority.LOW, 2);
    addBwaZeile(BwaZeileId.SONST_NEUTR_AUFW, Priority.LOW, 2);
    addBwaZeile(BwaZeileId.NEUTRALER_AUFWAND, Priority.LOW, 1);
    addBwaZeile(1321); // Leerzeile
    addBwaZeile(BwaZeileId.ZINSERTRAEGE, Priority.LOW, 2);
    addBwaZeile(BwaZeileId.SONST_NEUTR_ERTR, Priority.LOW, 2);
    addBwaZeile(BwaZeileId.VERR_KALK_KOSTEN, Priority.LOW, 2);
    addBwaZeile(BwaZeileId.NEUTRALER_ERTRAG, Priority.LOW, 1);
    addBwaZeile(1331);
    addBwaZeile(BwaZeileId.KONTENKL_UNBESETZT, Priority.LOW, 1);
    addBwaZeile(1342);
    addBwaZeile(BwaZeileId.ERGEBNIS_VOR_STEUERN, Priority.HIGH);
    addBwaZeile(1350);
    addBwaZeile(BwaZeileId.STEUERN_EINK_U_ERTR, Priority.LOW, 1);
    addBwaZeile(1360);
    addBwaZeile(BwaZeileId.VORLAEUFIGES_ERGEBNIS, Priority.HIGH);
    addBwaZeile(1390);
  }

  public Bwa(List<BuchungssatzDO> buchungsSaetze)
  {
    this();
    setBuchungssaetze(buchungsSaetze);
  }

  public void setBuchungssaetze(List<BuchungssatzDO> buchungsSaetze)
  {
    if (bwaZeilen == null) {
      return;
    }
    if (CollectionUtils.isNotEmpty(buchungsSaetze) == true) {
      for (BuchungssatzDO satz : buchungsSaetze) {
        counter++;
        // Diese Berechnungen werden anhand des Wertenachweises einer Bwa geführt:
        if (satz.isIgnore() == true) {
          continue;
        }
        int konto = satz.getKonto().getNummer();
        if (konto >= 4000 && konto <= 4799) {
          get(BwaZeileId.UMSATZERLOESE).addKontoUmsatz(satz);
        } else if (konto >= 5700 && konto <= 5999) {
          get(BwaZeileId.MAT_WARENEINKAUF).addKontoUmsatz(satz);
        } else if (konto == 4830 || konto == 4947) {
          get(BwaZeileId.SO_BETR_ERLOESE).addKontoUmsatz(satz);
        } else if (konto >= 6000 && konto <= 6199) {
          get(BwaZeileId.PERSONALKOSTEN).addKontoUmsatz(satz);
        } else if (konto >= 6310 && konto <= 6330) {
          get(BwaZeileId.RAUMKOSTEN).addKontoUmsatz(satz);
        } else if (konto == 7685) {
          get(BwaZeileId.BETRIEBL_STEUERN).addKontoUmsatz(satz);
        } else if (konto >= 6400 && konto <= 6430) {
          get(BwaZeileId.VERSICH_BEITRAEGE).addKontoUmsatz(satz);
        } else if (konto >= 6520 && konto <= 6599) {
          get(BwaZeileId.KFZ_KOSTEN).addKontoUmsatz(satz);
        } else if (konto >= 6600 && konto <= 6699) {
          get(BwaZeileId.WERBE_REISEKOSTEN).addKontoUmsatz(satz);
        } else if (konto == 6740) {
          get(BwaZeileId.KOSTEN_WARENABGABE).addKontoUmsatz(satz);
        } else if (konto >= 6200 && konto <= 6299) {
          get(BwaZeileId.ABSCHREIBUNGEN).addKontoUmsatz(satz);
        } else if (konto >= 6470 && konto <= 6490) {
          get(BwaZeileId.REPARATUR_INSTANDH).addKontoUmsatz(satz);
        } else if (konto >= 6800 && konto <= 6855 || konto == 6300) {
          get(BwaZeileId.SONSTIGE_KOSTEN).addKontoUmsatz(satz);
        } else if (konto == 7310) {
          get(BwaZeileId.ZINSAUFWAND).addKontoUmsatz(satz);
        } else if (konto == 7100) {
          get(BwaZeileId.ZINSERTRAEGE).addKontoUmsatz(satz);
        } else if (konto == 6392 || konto == 6895 || konto == 6960) {
          get(BwaZeileId.SONST_NEUTR_AUFW).addKontoUmsatz(satz);
        } else if (konto == 4845 || konto == 4855 || konto == 4930 || konto == 4937 || konto == 4925 || konto == 4960 || konto == 4975) {
          get(BwaZeileId.SONST_NEUTR_ERTR).addKontoUmsatz(satz);
        } else if (konto >= 7600 && konto <= 7640) {
          get(BwaZeileId.STEUERN_EINK_U_ERTR).addKontoUmsatz(satz);
        } else {
          log.warn("Ignoring Satz: " + satz);
          satz.setIgnore(true);
        }
      }
      recalculate();
    }
  }

  public Bwa(int year, int month)
  {
    this.year = year;
    this.month = month;
  }

  private BwaZeile[] getZeilen(BwaZeileId... ids)
  {
    BwaZeile[] zeilen = new BwaZeile[ids.length];
    for (int i = 0; i < ids.length; i++) {
      zeilen[i] = get(ids[i]);
    }
    return zeilen;
  }

  public void recalculate()
  {
    // GESAMTLEISTUNG:
    get(BwaZeileId.GESAMTLEISTUNG).sum(getZeilen(BwaZeileId.UMSATZERLOESE, BwaZeileId.BEST_VERDG, BwaZeileId.AKT_EIGENLEISTUNGEN));
    // ROHERTRAG:
    get(BwaZeileId.ROHERTRAG).sum(get(BwaZeileId.GESAMTLEISTUNG), get(BwaZeileId.MAT_WARENEINKAUF));
    // BETRIEBL_ROHERTRAG:
    get(BwaZeileId.BETRIEBL_ROHERTRAG).sum(get(BwaZeileId.ROHERTRAG), get(BwaZeileId.SO_BETR_ERLOESE));
    // GESAMTKOSTEN:
    get(BwaZeileId.GESAMTKOSTEN).sum(
        getZeilen(BwaZeileId.PERSONALKOSTEN, BwaZeileId.RAUMKOSTEN, BwaZeileId.BETRIEBL_STEUERN, BwaZeileId.VERSICH_BEITRAEGE,
            BwaZeileId.BESONDERE_KOSTEN, BwaZeileId.KFZ_KOSTEN, BwaZeileId.WERBE_REISEKOSTEN, BwaZeileId.KOSTEN_WARENABGABE,
            BwaZeileId.ABSCHREIBUNGEN, BwaZeileId.REPARATUR_INSTANDH, BwaZeileId.SONSTIGE_KOSTEN));
    // BETRIEBSERGEBNIS:
    get(BwaZeileId.BETRIEBSERGEBNIS).sum(getZeilen(BwaZeileId.BETRIEBL_ROHERTRAG, BwaZeileId.GESAMTKOSTEN));
    // NEUTRALER_AUFWAND:
    get(BwaZeileId.NEUTRALER_AUFWAND).sum(getZeilen(BwaZeileId.ZINSAUFWAND, BwaZeileId.SONST_NEUTR_AUFW));
    // NEUTRALER_ERTRAG:
    get(BwaZeileId.NEUTRALER_ERTRAG).sum(getZeilen(BwaZeileId.ZINSERTRAEGE, BwaZeileId.SONST_NEUTR_ERTR, BwaZeileId.VERR_KALK_KOSTEN));
    // ERGEBNIS_VOR_STEUERN:
    get(BwaZeileId.ERGEBNIS_VOR_STEUERN).sum(
        getZeilen(BwaZeileId.BETRIEBSERGEBNIS, BwaZeileId.NEUTRALER_AUFWAND, BwaZeileId.NEUTRALER_ERTRAG));
    // VORLAEUFIGES_ERGEBNIS:
    get(BwaZeileId.VORLAEUFIGES_ERGEBNIS).sum(getZeilen(BwaZeileId.ERGEBNIS_VOR_STEUERN, BwaZeileId.STEUERN_EINK_U_ERTR));
    if (get(BwaZeileId.GESAMTLEISTUNG).getBwaWert().compareTo(BigDecimal.ZERO) != 0) {
      // Erfolgsquote:
      this.erfolgsquote = get(BwaZeileId.BETRIEBSERGEBNIS).getBwaWert().multiply(NumberHelper.HUNDRED).divide(
          get(BwaZeileId.GESAMTLEISTUNG).getBwaWert(), 0, RoundingMode.HALF_UP);
      // relative Performance:
      this.relativePerformance = get(BwaZeileId.VORLAEUFIGES_ERGEBNIS).getBwaWert().divide(get(BwaZeileId.GESAMTLEISTUNG).getBwaWert(), 2,
          RoundingMode.HALF_UP);
    } else {
      this.erfolgsquote = BigDecimal.ZERO;
      this.relativePerformance = BigDecimal.ZERO;
    }
  }

  public String getHeader()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("BWA");
    if (year > 0) {
      buf.append(" für ").append(KostFormatter.formatBuchungsmonat(year, month));
    }
    if (toMonth > 0) {
      buf.append(" bis ").append(KostFormatter.formatBuchungsmonat(year, toMonth));
    }
    if (title != null) {
      buf.append(" \"").append(title).append("\"");
    }
    buf.append(":\n");
    return buf.toString();
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append(getHeader());
    if (bwaZeilen != null) {
      for (BwaZeile zeile : bwaZeilen) {
        asLine(buf, zeile);
      }
    }
    asLine(buf, 0, "Erfolgsquote", erfolgsquote, 0, 0, "%");
    asLine(buf, 0, "relative Performance", relativePerformance, 0, 3, " ");
    return buf.toString();
  }

  /**
   * Erfolgsquote ist das Betriebsergebnis im Verhältnis zur Gesamtleistung als Prozentzahl (Betriebsergebnis * 100 / Gesamtleistung).
   * @return
   */
  public BigDecimal getErfolgsquote()
  {
    return erfolgsquote;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTitle()
  {
    return (this.title);
  }

  public BwaZeile getGesamtleistung()
  {
    return get(BwaZeileId.GESAMTLEISTUNG);
  }

  public BwaZeile getVorlaeufigesErgebnis()
  {
    return get(BwaZeileId.VORLAEUFIGES_ERGEBNIS);
  }

  public BwaZeile getMatWareneinkauf()
  {
    return get(BwaZeileId.MAT_WARENEINKAUF);
  }

  public List<BwaZeile> getZeilen()
  {
    return bwaZeilen;
  }

  private void asLine(StringBuffer buf, int zeile, String bezeichnung, BigDecimal wert, int indent, int scale, String unit)
  {
    buf.append(StringUtils.leftPad(zeile != 0 ? NumberHelper.getAsString(zeile) : "", 4));
    int length = 25;
    for (int i = 0; i < indent; i++) {
      buf.append(" ");
      length--; // One space lost.
    }
    buf.append(" ").append(StringUtils.rightPad(StringUtils.defaultString(bezeichnung), length)).append(" ");
    if (wert != null && wert.compareTo(BigDecimal.ZERO) != 0) {
      String value;
      if ("€".equals(unit) == true) {
        value = CurrencyFormatter.format(wert);
      } else {
        NumberFormat format = NumberHelper.getNumberFractionFormat(PFUserContext.getLocale(), scale);
        value = format.format(wert) + " " + unit;
      }
      buf.append(StringUtils.leftPad(value, 18));
    }
    buf.append("\n");
  }

  private void asLine(StringBuffer buf, int zeile, String bezeichnung, BigDecimal wert, int indent)
  {
    asLine(buf, zeile, bezeichnung, wert, indent, 2, "€");
  }

  private void asLine(StringBuffer buf, BwaZeile zeile)
  {
    asLine(buf, zeile.getZeile(), zeile.getBezeichnung(), zeile.getBwaWert(), zeile.getIndent());
  }

  public String getShortname()
  {
    return shortname;
  }

  public void setShortname(String shortname)
  {
    this.shortname = shortname;
  }

  public int getCounter()
  {
    return counter;
  }

  /**
   * Berechnen der relativen Performance dieser BWA also das Verhaeltnis von Gesamtleistung zu Rohertrag
   */
  public BigDecimal getRelativePerformance()
  {
    return relativePerformance;
  }

  /**
   * Dieses Objekt kann von der benutzenden Klasse als freies Feld genutzt werden. Z. B. wird dieses Feld benutzt, um den Report zu
   * erhalten, der diese BWA enthält
   * @see Report#getChildBwaArray(boolean)
   */
  public Object getReference()
  {
    return reference;
  }

  public void setReference(Object reference)
  {
    this.reference = reference;
  }

  /**
   * @return true, wenn die Buchungssätze in den BwaZeilen gespeichert werden.
   */
  public boolean isStoreBuchungssaetzeInZeilen()
  {
    return storeBuchungssaetzeInZeilen;
  }

  public void setStoreBuchungssaetzeInZeilen(boolean value)
  {
    this.storeBuchungssaetzeInZeilen = value;
    for (BwaZeile bwaZeile : this.bwaZeilen) {
      bwaZeile.setStoreBuchungssaetze(this.storeBuchungssaetzeInZeilen);
    }
  }
}
