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
 * Interface to retrive the workdays (non holidays) of an employee at a day
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IEmployeeWorkdayProvider
{

  /**
   * returns true if the given day is an working day for the given employee
   * @param employee worker
   * @param day
   * @return true, if the given day is a working day for the given employee
   */
  public boolean isWorkdayFor(final EmployeeDO employee, final DateMidnight day);

}
