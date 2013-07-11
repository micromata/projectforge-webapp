/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.resourceworkload.model;

import org.joda.time.DateMidnight;
/**
 * This Interface represents the work load hours per day of arbitrary resources in a given time interval.
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IWorkloadData
{

  /**
   * retrieves the hours of work load per day in an time interval of begin date and end date
   * @param beginDate begin date of the time interval
   * @param endDate end date of the time interval
   * @return work load in hours in the specified interval
   */
  public double[] getWorkloadHoursPerDay(DateMidnight beginDay, DateMidnight endDay);
}
