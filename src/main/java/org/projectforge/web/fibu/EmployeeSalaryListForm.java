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

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.fibu.EmployeeSalaryFilter;
import org.projectforge.web.wicket.AbstractListForm;


public class EmployeeSalaryListForm extends AbstractListForm<EmployeeSalaryFilter, EmployeeSalaryListPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmployeeSalaryListForm.class);

  private static final long serialVersionUID = -5969136444233092172L;

  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new CheckBox("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted")));
    filterContainer.add(new CheckBox("showOnlyActiveEntriesCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "showOnlyActiveEntries")));
  }

  public EmployeeSalaryListForm(EmployeeSalaryListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected void validation()
  {
//    if (form.getSearchFilter().getMonth() < 0 || form.getSearchFilter().getMonth() > 11) {
//      addError("fibu.employee.salary.error.monthNotGiven");
//      return getInputPage();
  //  }

  //    updateStopDate();
//    if (getData().getDuration() < 60000) {
//      // Duration is less than 60 seconds.
//      addError("timesheet.error.zeroDuration");
//    } else if (getData().getDuration() > TimesheetDao.MAXIMUM_DURATION) {
//      addError("timesheet.error.maximumDurationExceeded");
//    }
//    if (kost2Row.isVisible() == false && getData().getKost2Id() == null) {
//      // Kost2 is not available for current task.
//      final TaskNode taskNode = taskTree.getTaskNodeById(getData().getTaskId());
//      if (taskNode != null) {
//        final List<Integer> descendents = taskNode.getDescendantIds();
//        for (final Integer taskId : descendents) {
//          if (CollectionUtils.isNotEmpty(taskTree.getKost2List(taskId)) == true) {
//            // But Kost2 is available for sub task, so user should book his time sheet
//            // on a sub task with kost2s.
//            addError("timesheet.error.kost2NeededChooseSubTask");
//            break;
//          }
//        }
//      }
//    }
  }

  @Override
  protected EmployeeSalaryFilter newSearchFilterInstance()
  {
    return new EmployeeSalaryFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
