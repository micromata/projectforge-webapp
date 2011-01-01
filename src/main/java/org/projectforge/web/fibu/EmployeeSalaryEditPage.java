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

package org.projectforge.web.fibu;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.EmployeeSalaryDao;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;

@EditPage(defaultReturnPage = EmployeeSalaryListPage.class)
public class EmployeeSalaryEditPage extends AbstractEditPage<EmployeeSalaryDO, EmployeeSalaryEditForm, EmployeeSalaryDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = -3899191243765232906L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmployeeSalaryEditPage.class);

  @SpringBean(name = "employeeSalaryDao")
  private EmployeeSalaryDao employeeSalaryDao;

  public EmployeeSalaryEditPage(PageParameters parameters)
  {
    super(parameters, "fibu.employeeSalary");
    init();
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(final String property, final Object selectedValue)
  {
    if ("eintrittsDatum".equals(property) == true) {
      final Date date;
      if (selectedValue instanceof String) {
        final Long ms = NumberHelper.parseLong((String) selectedValue);
        date = new Date(ms);
      } else {
        date = (Date) selectedValue;
      }
      final DayHolder dh = new DayHolder(date);
   //   getData().setEintrittsDatum(dh.getDate());
      form.eintrittsDatePanel.markModelAsChanged();
    } else if ("eintrittsDatum".equals(property) == true) {
      final Date date;
      if (selectedValue instanceof String) {
        final Long ms = NumberHelper.parseLong((String) selectedValue);
        date = new Date(ms);
      } else {
        date = (Date) selectedValue;
      }
      final DayHolder dh = new DayHolder(date);
   //   getData().setAustrittsDatum(dh.getDate());
      form.austrittsDatePanel.markModelAsChanged();
    } else if ("userId".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }
  //    getBaseDao().setUser(getData(), id);
    } else if ("kost1Id".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }
  //    getBaseDao().setKost1(getData(), id);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    log.error("Property '" + property + "' not supported for selection.");
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  @Override
  protected EmployeeSalaryDao getBaseDao()
  {
    return employeeSalaryDao;
  }

  @Override
  protected EmployeeSalaryEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, EmployeeSalaryDO data)
  {
    return new EmployeeSalaryEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
