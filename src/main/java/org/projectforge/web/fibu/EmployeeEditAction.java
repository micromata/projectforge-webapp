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



public class EmployeeEditAction// extends BaseEditActionBean<EmployeeDao, EmployeeDO>
{
//  private static final Logger log = Logger.getLogger(EmployeeEditAction.class);
//
//  public void setEmployeeDao(EmployeeDao employeeDao)
//  {
//    this.baseDao = employeeDao;
//  }
//
//  public List<LabelValueBean<String, EmployeeStatus>> getStatusList()
//  {
//    List<LabelValueBean<String, EmployeeStatus>> list = new ArrayList<LabelValueBean<String, EmployeeStatus>>();
//    for (EmployeeStatus status : EmployeeStatus.values()) {
//      list.add(new LabelValueBean<String, EmployeeStatus>(getLocalizedString("fibu.employee.status." + status.getKey()), status));
//    }
//    return list;
//  }
//
//  @ValidateNestedProperties( { @Validate(field = "id"), @Validate(field = "status", converter = EnumeratedTypeConverter.class),
//      @Validate(field = "wochenstunden", minvalue = 0, maxvalue = 100, converter = IntegerTypeConverter.class),
//      @Validate(field = "urlaubstage", minvalue = 0, maxvalue = 100, converter = IntegerTypeConverter.class),
//      @Validate(field = "position", maxlength = EmployeeDO.POSITION_LENGTH),
//      @Validate(field = "abteilung", maxlength = EmployeeDO.ABTEILUNG_LENGTH),
//      @Validate(field = "comment", maxlength = Constants.COMMENT_LENGTH)})
//  public EmployeeDO getEmployee()
//  {
//    return getData();
//  }
//
//  public void setEmployee(EmployeeDO data)
//  {
//    setData(data);
//  }
//
//  @Validate(required = true)
//  @Select(selectAction = Kost1ListAction.class)
//  public Integer getKost1Id()
//  {
//    return getData().getKost1Id();
//  }
//
//  public void setKost1Id(Integer kost1Id)
//  {
//    baseDao.setKost1(getData(), kost1Id);
//  }
//
//  @Validate(required = true)
//  @Select(selectAction = UserListAction.class)
//  public Integer getUserId()
//  {
//    return getData().getUserId();
//  }
//
//  public void setUserId(Integer userId)
//  {
//    baseDao.setUser(getData(), userId);
//  }
//
//  @Validate(converter = DateTypeConverter.class)
//  @SelectDate
//  public Date getEintrittsDatum()
//  {
//    return getData().getEintrittsDatum();
//  }
//
//  public void setEintrittsDatum(Date eintrittsdatum)
//  {
//    getData().setEintrittsDatum(eintrittsdatum);
//  }
//
//  @Validate(converter = DateTypeConverter.class)
//  @SelectDate
//  public Date getAustrittsDatum()
//  {
//    return getData().getAustrittsDatum();
//  }
//
//  public void setAustrittsDatum(Date austrittsdatum)
//  {
//    getData().setAustrittsDatum(austrittsdatum);
//  }
//
//  @Override
//  protected Logger getLogger()
//  {
//    return log;
//  }
//
//  @Override
//  protected EmployeeDO createDataInstance()
//  {
//    return new EmployeeDO();
//  }
}
