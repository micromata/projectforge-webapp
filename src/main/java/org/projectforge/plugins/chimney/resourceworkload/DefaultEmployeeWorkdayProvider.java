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
import org.projectforge.plugins.chimney.utils.date.ProjectForgeHolidaysHolidayChecker;
import org.projectforge.plugins.chimney.utils.date.WorkdayDateCalculator;

/**
 * Default implementation of IEmployeeWorkdayProvider to figure out the workdays of any employee in ProjectForge.
 * At the moment this is only a mock which just uses an WorkdayDateCalculator with the assumption that there are no additional holidays for an employee.
 * TODO: figure out holidays specific to employee which is at the moment not supported.
 * @see IEmployeeWorkdayProvider
 * @author Sweeps <pf@byte-storm.com>
 */
public class DefaultEmployeeWorkdayProvider implements IEmployeeWorkdayProvider
{

  /**
   * uses at the moment the WorkdayDateCalculator to figure out holidays
   */
  private final WorkdayDateCalculator workdayCalculator = new WorkdayDateCalculator(
      new ProjectForgeHolidaysHolidayChecker()
  );

  /**
   * @see org.projectforge.plugins.chimney.resourceworkload.IEmployeeWorkdayProvider#isWorkdayFor(org.projectforge.fibu.EmployeeDO, org.joda.time.DateMidnight)
   */
  @Override
  public boolean isWorkdayFor(final EmployeeDO employee, final DateMidnight day) {
    // TODO: figure out holidays specific to employee
    return workdayCalculator.isWorkday(day);
  }
}
