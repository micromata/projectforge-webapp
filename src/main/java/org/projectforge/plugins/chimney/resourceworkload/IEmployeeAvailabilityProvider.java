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
import org.projectforge.fibu.EmployeeDO;
/**
 * Interface to retrieve the availability of employee at a day
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public interface IEmployeeAvailabilityProvider
{

  /**
   * returns the availability in hours of the given employee at the given day
   * @param employee worker
   * @param day
   * @return availability of the employee at the day in hours
   */
  public double getAvialabilityHours(EmployeeDO employee, DateMidnight day);

}
