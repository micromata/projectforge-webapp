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



public class EmployeeSalaryEditAction //extends BaseEditActionBean<EmployeeSalaryDao, EmployeeSalaryDO>
{
//  private static final Logger log = Logger.getLogger(EmployeeSalaryEditAction.class);
//
//  private EmployeeSalaryEditRecentEntry recent;
//
//  public void setEmployeeSalaryDao(EmployeeSalaryDao employeeSalaryDao)
//  {
//    this.baseDao = employeeSalaryDao;
//  }
//
//  public List<LabelValueBean<String, Integer>> getMonthList()
//  {
//    return DateHelper.getMonthList();
//  }
//
//  public List<LabelValueBean<String, EmployeeSalaryType>> getTypeList()
//  {
//    List<LabelValueBean<String, EmployeeSalaryType>> list = new ArrayList<LabelValueBean<String, EmployeeSalaryType>>();
//    for (EmployeeSalaryType type : EmployeeSalaryType.values()) {
//      list.add(new LabelValueBean<String, EmployeeSalaryType>(getLocalizedString("fibu.employee.salary.type." + type.getKey()), type));
//    }
//    return list;
//  }
//
//  @Override
//  protected void onPreEdit()
//  {
//    if (getData().getId() == null) {
//      recent = getRecent();
//      getData().setYear(recent.getYear());
//      getData().setMonth(recent.getMonth());
//    }
//  }
//
//  private EmployeeSalaryEditRecentEntry getRecent()
//  {
//    if (recent == null) {
//      recent = (EmployeeSalaryEditRecentEntry) getContext().getEntry(EmployeeSalaryEditRecentEntry.class.getName());
//    }
//    if (recent == null) {
//      recent = new EmployeeSalaryEditRecentEntry();
//      Calendar cal = DateHelper.getCalendar();
//      recent.setYear(cal.get(Calendar.YEAR));
//      recent.setMonth(cal.get(Calendar.MONTH));
//      getContext().putEntry(EmployeeSalaryEditRecentEntry.class.getName(), recent, true);
//    }
//    return recent;
//  }
//
//  @Override
//  protected Resolution afterSaveOrUpdate()
//  {
//    recent = getRecent();
//    if (getData().getYear() != null) {
//      recent.setYear(getData().getYear());
//    }
//    if (getData().getMonth() != null) {
//      recent.setMonth(getData().getMonth());
//    }
//    return null;
//  }
//
//  @ValidateNestedProperties( { @Validate(field = "id"), @Validate(field = "type", converter = EnumeratedTypeConverter.class),
//      @Validate(field = "year", minvalue = 1900, maxvalue = 2100, converter = IntegerTypeConverter.class),
//      @Validate(field = "month", minvalue = 0, maxvalue = 11, converter = IntegerTypeConverter.class),
//      @Validate(field = "bruttoMitAgAnteil", required = true, converter = CurrencyTypeConverter.class),
//      @Validate(field = "comment", maxlength = Constants.COMMENT_LENGTH)})
//  public EmployeeSalaryDO getEmployeeSalary()
//  {
//    return getData();
//  }
//
//  public void setEmployeeSalary(EmployeeSalaryDO data)
//  {
//    setData(data);
//  }
//
//  @Validate(required = true)
//  @Select(selectAction = EmployeeListAction.class)
//  public Integer getEmployeeId()
//  {
//    return getData().getEmployeeId();
//  }
//
//  public void setEmployeeId(Integer employeeId)
//  {
//    baseDao.setEmployee(getData(), employeeId);
//  }
//
//  @Override
//  protected Logger getLogger()
//  {
//    return log;
//  }
//
//  @Override
//  protected EmployeeSalaryDO createDataInstance()
//  {
//    return new EmployeeSalaryDO();
//  }
}
