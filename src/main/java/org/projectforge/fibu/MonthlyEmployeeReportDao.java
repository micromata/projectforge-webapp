/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.fibu;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.task.TaskFormatter;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class MonthlyEmployeeReportDao extends HibernateDaoSupport
{
  private TaskFormatter taskFormatter;
  
  private TimesheetDao timesheetDao;

  private EmployeeDao employeeDao;
  
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public MonthlyEmployeeReport getReport(int year, int month, PFUserDO user) {
    Validate.notNull(user);
    MonthlyEmployeeReport report = new MonthlyEmployeeReport(year, month);
    EmployeeDO employee = employeeDao.getByUserId(user.getId());
    if (employee != null) {
      report.setEmployee(employee);
    } else {
      report.setUser(user);
    }
    report.setTaskFormatter(taskFormatter);
    report.init();
    TimesheetFilter filter = new TimesheetFilter();
    filter.setDeleted(false);
    filter.setStartTime(report.getFromDate());
    filter.setStopTime(report.getToDate());
    filter.setUserId(user.getId());
    List<TimesheetDO> list = timesheetDao.getList(filter);
    if (CollectionUtils.isNotEmpty(list) == true) {
      for (TimesheetDO sheet : list) {
        report.addTimesheet(sheet);
      }
    }
    report.calculate();
    return report;
  }

  public void setTaskFormatter(TaskFormatter taskFormatter)
  {
    this.taskFormatter = taskFormatter;
  }
  
  public void setTimesheetDao(TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }
  
  public void setEmployeeDao(EmployeeDao employeeDao)
  {
    this.employeeDao = employeeDao;
  }
}
