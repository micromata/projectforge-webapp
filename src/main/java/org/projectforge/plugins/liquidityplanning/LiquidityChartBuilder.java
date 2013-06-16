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

import java.util.Calendar;

import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.projectforge.calendar.DayHolder;
import org.projectforge.charting.AbstractChartBuilder;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LiquidityChartBuilder extends AbstractChartBuilder
{
  /**
   * @param timesheetDao
   * @param userId
   * @param workingHoursPerDay
   * @param forLastNDays
   * @param shape e. g. new Ellipse2D.Float(-3, -3, 6, 6) or null, if no marker should be printed.
   * @param stroke e. g. new BasicStroke(3.0f).
   * @param showAxisValues
   * @return
   */
  public JFreeChart create(final LiquidityAnalysis analysis, final int nextDays)
  {
    final DayHolder dh = new DayHolder();

    final TimeSeries sollSeries = new TimeSeries("Soll");
    final TimeSeries istSeries = new TimeSeries("Haben");
    {
      final Day day = new Day(dh.getDayOfMonth(), dh.getMonth() + 1, dh.getYear());
      sollSeries.add(day, 0);
      istSeries.add(day, 0);
      dh.add(Calendar.DATE, 1);
    }
    return null;//create(sollSeries, istSeries, shape, stroke, showAxisValues, "hours");
  }
}
