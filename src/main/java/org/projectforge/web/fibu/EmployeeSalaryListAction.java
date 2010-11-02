/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.fibu;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.log4j.Logger;
import org.projectforge.common.DateHelper;
import org.projectforge.common.LabelValueBean;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.EmployeeSalaryDao;
import org.projectforge.fibu.datev.EmployeeSalaryExportDao;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseListActionBean;


/**
 */
@StrictBinding
@UrlBinding("/secure/fibu/EmployeeSalaryList.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/fibu/employeeSalaryList.jsp")
public class EmployeeSalaryListAction extends BaseListActionBean<EmployeeSalaryListFilter, EmployeeSalaryDao, EmployeeSalaryDO>
{
  private static final Logger log = Logger.getLogger(EmployeeSalaryListAction.class);

  private EmployeeSalaryExportDao employeeSalaryExportDao;

  public List<LabelValueBean<String, Integer>> getYearList()
  {
    int[] years = baseDao.getYears();
    List<LabelValueBean<String, Integer>> list = new ArrayList<LabelValueBean<String, Integer>>();
    for (int year : years) {
      list.add(new LabelValueBean<String, Integer>(String.valueOf(year), year));
    }
    return list;
  }

  public List<LabelValueBean<String, Integer>> getMonthList()
  {
    List<LabelValueBean<String, Integer>> list = new ArrayList<LabelValueBean<String, Integer>>();
    list.add(new LabelValueBean<String, Integer>("--", -1));
    for (int month = 0; month < 12; month++) {
      list.add(new LabelValueBean<String, Integer>(StringHelper.format2DigitNumber(month + 1), month));
    }
    return list;
  }

  public Resolution exportAsXls()
  {
    if (getActionFilter().getMonth() < 0 || getActionFilter().getMonth() > 11) {
      addError("actionFilter.month", "fibu.employee.salary.error.monthNotGiven");
      return getInputPage();
    }
    log.info("Exporting employee salaries as excel sheet for: "
        + DateHelper.formatMonth(getActionFilter().getYear(), getActionFilter().getMonth()));
    List<EmployeeSalaryDO> l = getList();
    if (l == null) {
      l = buildList();
    }
    byte[] xls = employeeSalaryExportDao.export(l);
    if (xls == null || xls.length == 0) {
      return getInputPage();
    }
    String filename = "ProjectForge-EmployeeSalaries_"
        + DateHelper.formatMonth(getActionFilter().getYear(), getActionFilter().getMonth())
        + "_"
        + DateHelper.getDateAsFilenameSuffix(new Date())
        + ".xls";
    return getDownloadResolution(filename, xls);
  }

  public void setEmployeeSalaryDao(EmployeeSalaryDao employeeSalaryDao)
  {
    this.baseDao = employeeSalaryDao;
  }

  @Validate
  public int getYear()
  {
    return getActionFilter().getYear();
  }

  public void setYear(int year)
  {
    getActionFilter().setYear(year);
  }

  @Validate
  public int getMonth()
  {
    return getActionFilter().getMonth();
  }

  public void setMonth(int month)
  {
    getActionFilter().setMonth(month);
  }

  public void setEmployeeSalaryExportDao(EmployeeSalaryExportDao employeeSalaryExportDao)
  {
    this.employeeSalaryExportDao = employeeSalaryExportDao;
  }

  /**
   * Needed only for StrictBinding. If method has same signature as super.getActionFilter then stripes ignores these validate settings
   * (bug?).
   */
  @ValidateNestedProperties( { @Validate(field = "searchString"), @Validate(field = "deleted")})
  public EmployeeSalaryListFilter getFilter()
  {
    return super.getActionFilter();
  }

  /**
   * @return always true.
   * @see org.projectforge.web.core.BaseListActionBean#isShowResultInstantly()
   */
  @Override
  protected boolean isShowResultInstantly()
  {
    return true;
  }

  @Override
  protected EmployeeSalaryListFilter createFilterInstance()
  {
    return new EmployeeSalaryListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
