/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.resourceworkload;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableInstant;
import org.projectforge.plugins.chimney.web.resourceworkload.model.IWorkloadData;
import org.projectforge.timesheet.TimesheetDO;


/**
 * This class takes an arbitrary list of timesheets assignments and calculates the real workload sum in hours for each day in a given time interval
 * @see IWorkloadData
 * @author Sweeps <pf@byte-storm.com>
 */
public class TimesheetWorkload implements IWorkloadData
{

  private static final double MILLIS_TO_HOURS_FACTOR = 1.0 / (1000.0 * 60.0 * 60.0);


  final List<TimesheetDO> allTimesheets;
  //private static final Logger log = Logger.getLogger(RealResourceWorkload.class);

  /**
   * constructor to create an IWorkloadData which calculates the real workload for a given list of timesheets.
   * @see IWorkloadData
   * @param allTimesheets
   */
  public TimesheetWorkload(final List<TimesheetDO> allTimesheets)
  {
    this.allTimesheets = allTimesheets;
  }


  /**
   * returns the workload hours per day in the specified time interval with respect to all given timesheets
   *
   * @see org.projectforge.plugins.chimney.web.resourceworkload.model.IWorkloadData#getWorkloadHoursPerDay(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
   */
  @Override
  public double[] getWorkloadHoursPerDay(final DateMidnight beginDay, final DateMidnight endDay)
  {
    if (endDay.isBefore(beginDay)) {
      throw new IllegalArgumentException("negative interval: begin day must not be before end day.");
    }

    final int dayCount = (int)new Duration(beginDay, endDay).getStandardDays()+1;
    final double[] workloadHoursPerDay = new double[dayCount];

    for (int i=0; i < dayCount; ++i) {
      workloadHoursPerDay[i] = 0;
    }

    //log.info("calc workload for '"+user.getUsername()+"': days = "+dayCount+" ("+beginDay+" - "+endDay+")");
    TimesheetDO timesheet;
    for (final Iterator<TimesheetDO> tIt = allTimesheets.iterator(); tIt.hasNext(); ) {
      timesheet = tIt.next();
      addTimeSheetWorkload(beginDay, endDay, timesheet, workloadHoursPerDay);
    }

    return workloadHoursPerDay;
  }

  private void addTimeSheetWorkload(
      final DateMidnight beginDay, final DateMidnight endDay,
      final TimesheetDO timesheet, final double[] workloadHoursPerDay) {

    DateMidnight itBeginDay, itEndDay;
    ReadableInstant tsBeginDate, tsEndDate;
    tsBeginDate = new DateTime(timesheet.getStartTime().getTime());
    tsEndDate = new DateTime(timesheet.getStopTime().getTime());
    //log.info(" consider timesheet '"+timesheet+"'");

    // filter timesheets not in the interval
    if (tsBeginDate.isAfter(endDay) || tsEndDate.isBefore(beginDay)) {
      //log.info("  out of interval");
      return;
    }
    // figure out the overlapping days of timesheet interval and the full interval (beginDay,endDay)
    if (tsBeginDate.isAfter(beginDay)) {
      itBeginDay = new DateMidnight(tsBeginDate.getMillis());
    } else {
      itBeginDay = beginDay;
      //log.info("  cut begin at interval begin");
    }
    if (tsEndDate.isBefore(endDay)) {
      itEndDay = new DateMidnight(tsEndDate.getMillis());;
    } else {
      itEndDay = endDay;
      //log.info("  cut end at interval end");
    }

    // iterate all days of the timesheet and sum the workload
    DateMidnight nextDay;
    long intersectDuration;
    for (DateMidnight currentDay = itBeginDay; !currentDay.isAfter(itEndDay); currentDay = nextDay) {
      nextDay = currentDay.plusDays(1);
      intersectDuration = intersectDuration(currentDay.getMillis(), nextDay.getMillis(), tsBeginDate.getMillis(), tsEndDate.getMillis());
      final int index = (int)(new Duration(beginDay, currentDay).getStandardDays());
      assert( index>=0 && index<workloadHoursPerDay.length );
      //log.info("  add "+(MILLIS_TO_HOURS_FACTOR * intersectDuration)+" to day index "+index);
      workloadHoursPerDay[index] += MILLIS_TO_HOURS_FACTOR * intersectDuration;
    }
  }

  private long intersectDuration(final long ival1BeginTime, final long ival1EndTime, final long ival2BeginTime, final long ival2EndTime) {
    long intersectBegin, intersectEnd;

    if (ival2BeginTime < ival1BeginTime) {
      intersectBegin = ival1BeginTime;
    } else {
      intersectBegin = ival2BeginTime;
    }

    if (ival2EndTime > ival1EndTime) {
      intersectEnd = ival1EndTime;
    } else {
      intersectEnd = ival2EndTime;
    }

    return Math.max(intersectEnd-intersectBegin, 0);
  }

}
