/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.resourceworkload;

import java.util.Date;

import org.jfree.data.time.Day;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.joda.time.DateMidnight;
import org.projectforge.plugins.chimney.web.resourceworkload.model.IWorkloadData;

/**
 * This Factory creates TimeSeries for JFreeChart for given resource workload data
 * @author Sweeps <pf@byte-storm.com>
 */
public class WorkloadSeriesFactory
{

  // private static final Logger log = Logger.getLogger(ResourceWorkloadDatasetGenerator.class);

  /**
   * Creates a JFree TimeSeries for a given label, workload data (@see {@link IWorkloadData}) and time interval.
   * To achieve a straight line in between each day, the data for one day generates two points (the first and the last millisecond of the day) in the result TimeSeries (@see {@link TimeSeries})
   * @param label label/key of the series
   * @param resourceWorkload resource workload to retrieve the data
   * @param beginDay start of the time interval
   * @param endDay end of the time interval
   * @return TimeSeries (@see {@link TimeSeries}) which can be visualized with JFreeChart
   */
  public static TimeSeries createTimeSeries(final String seriesLabel, final IWorkloadData resourceWorkload, final DateMidnight beginDay, final DateMidnight endDay) {
    // set label/key for time series:
    final TimeSeries resourceWorkloadSeries = new TimeSeries(seriesLabel);
    // log.info("create time series: "+seriesLabel);
    // generate graph data
    final double[] workloadHoursPerDay = resourceWorkload.getWorkloadHoursPerDay(beginDay, endDay);
    DateMidnight currentDay = new DateMidnight(beginDay);
    for (int i=0; i < workloadHoursPerDay.length; ++i, currentDay = currentDay.plusDays(1)) {
      final Day jFreeDay = new Day(new Date(currentDay.getMillis()));
      // set two points (first an last millisecond) in the time series for one workload entry for one day:
      resourceWorkloadSeries.add(new Millisecond(new Date(jFreeDay.getFirstMillisecond())), workloadHoursPerDay[i]);
      resourceWorkloadSeries.add(new Millisecond(new Date(jFreeDay.getLastMillisecond())), workloadHoursPerDay[i]);
    }
    return resourceWorkloadSeries;
  }

}
