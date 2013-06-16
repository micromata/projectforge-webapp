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
public class LiquidityAnalysis
{
  private final List<LiquidityEntry> entries = new LinkedList<LiquidityEntry>();

  /**
   * @return this for chaining.
   */
  public LiquidityAnalysis clear()
  {
    entries.clear();
    return this;
  }

  /**
   * @return this for chaining.
   */
  public LiquidityAnalysis sort()
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

  public LiquidityAnalysis add(final Collection<LiquidityEntryDO> list)
  {
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
      entries.add(entry);
    }
    return this;
  }

  public LiquidityAnalysis addInvoices(final Collection<RechnungDO> list)
  {
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
      entry.setSubject(invoice.getKundeAsString() + ": " + invoice.getBetreff());
      entry.setType(LiquidityEntryType.DEBITOR);
      entries.add(entry);
    }
    return this;
  }

  public LiquidityAnalysis addCreditorInvoices(final Collection<EingangsrechnungDO> list)
  {
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
      entries.add(entry);
    }
    return this;
  }
}
