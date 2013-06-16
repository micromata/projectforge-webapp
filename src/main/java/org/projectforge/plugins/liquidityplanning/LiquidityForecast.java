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

package org.projectforge.plugins.liquidityplanning;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.RechnungDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LiquidityForecast implements Serializable
{
  private static final long serialVersionUID = 5385319337895942452L;

  private final List<LiquidityEntry> entries = new LinkedList<LiquidityEntry>();

  private Collection<LiquidityEntry> liquiEntries;

  private Collection<LiquidityEntry> invoices;

  private Collection<LiquidityEntry> creditorInvoices;

  /**
   * Refresh forecast from stored liqui-entries, invoices and creditor invoices and sort the entries.
   * @return this for chaining.
   * @see #sort()
   */
  public LiquidityForecast build()
  {
    entries.clear();
    entries.addAll(this.liquiEntries);
    entries.addAll(this.invoices);
    entries.addAll(this.creditorInvoices);
    sort();
    return this;
  }

  /**
   * @return this for chaining.
   */
  private LiquidityForecast sort()
  {
    Collections.sort(entries, new Comparator<LiquidityEntry>() {
      @Override
      public int compare(final LiquidityEntry o1, final LiquidityEntry o2)
      {
        if (o1.getDateOfPayment() == null) {
          if (o2.getDateOfPayment() != null) {
            return -1;
          }
        } else if (o2.getDateOfPayment() == null) {
          return 1;
        } else {
          final int compare = o1.getDateOfPayment().compareTo(o2.getDateOfPayment());
          if (compare != 0) {
            return compare;
          }
        }
        final String s1 = o1.getSubject() != null ? o1.getSubject() : "";
        final String s2 = o2.getSubject() != null ? o2.getSubject() : "";
        return s1.compareTo(s2);
      }
    });
    return this;
  }

  /**
   * @return the entries
   */
  public List<LiquidityEntry> getEntries()
  {
    return entries;
  }

  public LiquidityForecast set(final Collection<LiquidityEntryDO> list)
  {
    this.liquiEntries = new LinkedList<LiquidityEntry>();
    if (list == null) {
      return this;
    }
    for (final LiquidityEntryDO liquiEntry : list) {
      final LiquidityEntry entry = new LiquidityEntry();
      entry.setDateOfPayment(liquiEntry.getDateOfPayment());
      entry.setAmount(liquiEntry.getAmount());
      entry.setPaid(liquiEntry.isPaid());
      entry.setSubject(liquiEntry.getSubject());
      entry.setType(LiquidityEntryType.LIQUIDITY);
      this.liquiEntries.add(entry);
    }
    return this;
  }

  public LiquidityForecast setInvoices(final Collection<RechnungDO> list)
  {
    this.invoices = new LinkedList<LiquidityEntry>();
    if (list == null) {
      return this;
    }
    for (final RechnungDO invoice : list) {
      final LiquidityEntry entry = new LiquidityEntry();
      if (invoice.getBezahlDatum() != null) {
        entry.setDateOfPayment(invoice.getBezahlDatum());
      } else {
        entry.setDateOfPayment(invoice.getFaelligkeit());
      }
      entry.setAmount(invoice.getGrossSum());
      entry.setPaid(invoice.isBezahlt());
      entry.setSubject("#" + invoice.getNummer() + ": " + invoice.getKundeAsString() + ": " + invoice.getBetreff());
      entry.setType(LiquidityEntryType.DEBITOR);
      this.invoices.add(entry);
    }
    return this;
  }

  public LiquidityForecast setCreditorInvoices(final Collection<EingangsrechnungDO> list)
  {
    this.creditorInvoices = new LinkedList<LiquidityEntry>();
    if (list == null) {
      return this;
    }
    for (final EingangsrechnungDO invoice : list) {
      final LiquidityEntry entry = new LiquidityEntry();
      if (invoice.getBezahlDatum() != null) {
        entry.setDateOfPayment(invoice.getBezahlDatum());
      } else {
        entry.setDateOfPayment(invoice.getFaelligkeit());
      }
      entry.setAmount(invoice.getGrossSum().negate());
      entry.setPaid(invoice.isBezahlt());
      entry.setSubject(invoice.getKreditor() + ": " + invoice.getBetreff());
      entry.setType(LiquidityEntryType.CREDITOR);
      this.creditorInvoices.add(entry);
    }
    return this;
  }

  /**
   * @return the invoices
   */
  public Collection<LiquidityEntry> getInvoices()
  {
    return invoices;
  }

  /**
   * @return the creditorInvoices
   */
  public Collection<LiquidityEntry> getCreditorInvoices()
  {
    return creditorInvoices;
  }
}
