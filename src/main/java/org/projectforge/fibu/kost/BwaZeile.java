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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.Priority;


public class BwaZeile implements Serializable
{
  private static final long serialVersionUID = 6135255440593281254L;

  private Priority priority;

  private BwaZeileId bwaZeileId;

  private Integer zeile;

  // Nur für Leerzeilen, wenn bwaZeileId == null:
  private int indent;

  private Bwa bwa;

  private List<BuchungssatzDO> buchungssaetze;

  private BigDecimal bwaWert = BigDecimal.ZERO;

  public BwaZeile(Bwa bwa, Integer zeile, Priority priority, int indent)
  {
    this.bwa = bwa;
    this.zeile = zeile;
    this.priority = priority;
    this.indent = indent;
  }

  public BwaZeile(Bwa bwa, BwaZeileId bwaZeileId, Priority priority, int indent)
  {
    this.bwa = bwa;
    this.bwaZeileId = bwaZeileId;
    this.priority = priority;
    this.indent = indent;
  }

  /**
   * Removes any previous existing Buchungssatz.
   * @param value Wenn true, dann werden fortan alle Buchungssätze intern hinzugefügt, deren Umsatz über addKontoUmsatz dieser Zeile
   *            hinzugefügt wurde. Andernfalls (default) werden die Buchungssätze nicht vermerkt.
   */
  public void setStoreBuchungssaetze(boolean value)
  {
    if (value == true) {
      this.buchungssaetze = new ArrayList<BuchungssatzDO>();
    } else {
      this.buchungssaetze = null;
    }
  }

  /**
   * Gibt die BWA zurück, deren Bestandteil diese Zeile ist.
   */
  public Bwa getBwa()
  {
    return bwa;
  }

  /**
   * Wenn konfiguriert, werden die Buchungsssätze zurückgegeben, die den Wert dieser BwaZeile ergeben. Dies ist sinnvoll, damit einzelne
   * Werte einer BWA im Detail nachvollzogen werden können.
   */
  public List<BuchungssatzDO> getBuchungssaetze()
  {
    return buchungssaetze;
  }

  public BigDecimal getBwaWert()
  {
    return bwaWert;
  }

  /**
   * Addiert den Kontoumsatz und falls setStoreBuchungsaetze(true) gesetzt wurde, wird der Buchungssatz intern hinzugefügt.
   * @param satz
   */
  public void addKontoUmsatz(BuchungssatzDO satz)
  {
    bwaWert = bwaWert.add(satz.getBetrag());
    if (this.buchungssaetze != null) {
      this.buchungssaetze.add(satz);
    }
  }

  public void sum(BwaZeile... zeilen)
  {
    bwaWert = BigDecimal.ZERO;
    for (BwaZeile zeile : zeilen) {
      bwaWert = bwaWert.add(zeile.getBwaWert());
      if (this.buchungssaetze != null) {
        this.buchungssaetze.addAll(zeile.getBuchungssaetze());
      }
    }
  }

  public BwaZeileId getBwaZeileId()
  {
    return bwaZeileId;
  }

  public int getZeile()
  {
    return bwaZeileId != null ? bwaZeileId.getId() : zeile;
  }

  public String getBezeichnung()
  {
    return bwaZeileId != null ? bwaZeileId.getBezeichnung() : "";
  }

  /**
   * Abhängig von der Priorität können Zeilen ein- und ausgeblendet werden. Als Priorität werden unterstützt: {@link Priority#HIGH},
   * {@link Priority#MIDDLE} and {@link Priority#LOW}.
   * @return
   */
  public Priority getPriority()
  {
    return priority;
  }

  /**
   * Einrücktiefe.
   */
  public int getIndent()
  {
    return indent;
  }

  public String toString()
  {
    return StringUtils.leftPad(NumberHelper.getAsString(getZeile()), 4)
        + " "
        + StringUtils.rightPad(getBezeichnung(), 20)
        + " "
        + StringUtils.leftPad(CurrencyFormatter.format(getBwaWert()), 18);
    /*
     * StringBuffer buf = new StringBuffer(); buf.append(row); for (KontoUmsatz umsatz : kontoUmsaetze) { buf.append("\n ");
     * buf.append(umsatz.toString()); } return buf.toString();
     */
  }
}
