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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.projectforge.calendar.DayHolder;
import org.projectforge.charting.AbstractChartBuilder;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LiquidityChartBuilder extends AbstractChartBuilder
{
  /**
   * @param analysis
   * @param nextDays
   * @return
   */
  public JFreeChart create(final LiquidityAnalysis analysis, final int nextDays)
  {
    Validate.isTrue(nextDays > 0 && nextDays < 500);
    final DayHolder dh = new DayHolder();

    final TimeSeries accumulatedSeries = new TimeSeries("accumulated");
    final TimeSeries creditSeries = new TimeSeries("credits");
    final TimeSeries debitSeries = new TimeSeries("debits");
    final Iterator<LiquidityEntry> it = analysis.getEntries().iterator();
    LiquidityEntry current = it.hasNext() == true ? it.next() : null;
    double accumulated = 0;
    for (int i = 0; i < nextDays; i++) {
      double debits = 0;
      double credits = 0;
      if (current != null) {
        while (current.getDateOfPayment() == null
            || dh.before(current.getDateOfPayment()) == false
            || dh.isSameDay(current.getDateOfPayment()) == true) {
          final BigDecimal amount = current.getAmount();
          if (amount != null) {
            final double val = amount.doubleValue();
            if (val < 0) {
              credits += val;
            } else {
              debits += val;
            }
          }
          current = it.hasNext() == true ? it.next() : null;
        }
      }
      final Day day = new Day(dh.getDayOfMonth(), dh.getMonth() + 1, dh.getYear());
      accumulated += debits + credits;
      accumulatedSeries.add(day, accumulated);
      creditSeries.add(day, -credits);
      debitSeries.add(day, debits);
      dh.add(Calendar.DATE, 1);
    }
    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(accumulatedSeries);
    // dataset.addSeries(creditSeries);
    // dataset.addSeries(debitSeries);
    return prepare(dataset).create(true, null);
  }
}
