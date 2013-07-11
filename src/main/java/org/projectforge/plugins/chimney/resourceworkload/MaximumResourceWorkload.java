/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.resourceworkload;

import org.joda.time.DateMidnight;
import org.joda.time.Duration;
import org.projectforge.fibu.EmployeeDO;
import org.projectforge.plugins.chimney.web.resourceworkload.model.IWorkloadData;

/**
 * This class takes an arbitrary employee to calculate the availability in hours on each day in a givven time interval.
 * 
 * @see IWorkloadData
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public class MaximumResourceWorkload implements IWorkloadData
{
  private final EmployeeDO employee;
  private final IEmployeeAvailabilityProvider employeeAvailabilityProvider;
  //private static final Logger log = Logger.getLogger(RealResourceWorkload.class);

  /**
   * constructor to create an IWorkloadData which calculates the availability of a given employee
   * @see IWorkloadData
   * @param employee
   * @param employeeAvailabilityProvider
   */
  public MaximumResourceWorkload(final EmployeeDO employee, final IEmployeeAvailabilityProvider employeeAvailabilityProvider)
  {
    this.employee = employee;
    this.employeeAvailabilityProvider = employeeAvailabilityProvider;
  }


  /**
   * returns the workload hours per day with respect to all timesheets of the user
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

    for (DateMidnight currentDay = beginDay; !currentDay.isAfter(endDay); currentDay = currentDay.plusDays(1)) {
      final int index = (int)(new Duration(beginDay, currentDay).getStandardDays());
      assert( index>=0 && index<dayCount );
      //if ( Holidays.getInstance().isWorkingDay( new DayHolder(new Date(currentDay.getMillis())) ) ) {
      workloadHoursPerDay[index] += employeeAvailabilityProvider.getAvialabilityHours(employee, currentDay);
    }


    return workloadHoursPerDay;
  }
}
