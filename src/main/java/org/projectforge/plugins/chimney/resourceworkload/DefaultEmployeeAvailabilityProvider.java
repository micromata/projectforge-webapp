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
 * Default implementation of IEmployeeAvailabilityProvider to figure out the daily availability of any employee in ProjectForge.
 * At the moment this is only a mock which just uses the assumption that an employee works EmployeeDO.getWochenstunden()/5 hours at each work day of the employee.
 * work days of the employee are retrieved from any IEmployeeAvailabilityProvider
 * TODO: figure out holidays specific to employee which is at the moment not supported.
 * @see IEmployeeAvailabilityProvider
 * @see IEmployeeWorkdayProvider
 * @author Sweeps <pf@byte-storm.com>
 */
public class DefaultEmployeeAvailabilityProvider implements IEmployeeAvailabilityProvider
{

  private final WorkdayDateCalculator workdayCalculator = new WorkdayDateCalculator(
      new ProjectForgeHolidaysHolidayChecker()
  );

  private final IEmployeeWorkdayProvider employeeWorkdayProvider;

  /**
   * creates IEmployeeAvailabilityProvider based on the given IEmployeeWorkdayProvider
   * @param employeeWorkdayProvider provider for working days of EmployeeDO
   */
  public DefaultEmployeeAvailabilityProvider(final IEmployeeWorkdayProvider employeeWorkdayProvider) {
    this.employeeWorkdayProvider = employeeWorkdayProvider;
  }

  /**
   * 
   * @see org.projectforge.plugins.chimney.resourceworkload.IEmployeeAvailabilityProvider#getAvialabilityHours(org.projectforge.fibu.EmployeeDO, org.joda.time.DateMidnight)
   */
  @Override
  public double getAvialabilityHours(final EmployeeDO employee, final DateMidnight day)
  {
    if (employee==null)
      // if no employee is specified, we assume 8.0 work hours each workday and no individual holidays ;)
      return workdayCalculator.isWorkday(day) ? 8.0 : 0.0;
    // otherwise, we take the 'wochenstunden' divided by five at each individual workday of the employee
    return employeeWorkdayProvider.isWorkdayFor(employee, day)? employee.getWochenstunden()/5.0 : 0.0;
  }



}
