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

package org.projectforge.web.timesheet;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.MyBeanComparator;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class TimesheetMassUpdatePage extends AbstractSecuredPage implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimesheetMassUpdatePage.class);

  @SpringBean(name = "dateTimeFormatter")
  private DateTimeFormatter dateTimeFormatter;

  @SpringBean(name = "kostCache")
  private KostCache kostCache;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  private List<TimesheetDO> timesheets;

  private final TimesheetMassUpdateForm form;

  private AbstractSecuredPage callerPage;

  public TimesheetMassUpdatePage(final AbstractSecuredPage callerPage, final List<TimesheetDO> timesheets)
  {
    super(new PageParameters());
    this.callerPage = callerPage;
    this.timesheets = timesheets;
    form = new TimesheetMassUpdateForm(this);
    Integer taskId = null;
    for (final TimesheetDO sheet : timesheets) {
      if (taskId == null) {
        taskId = sheet.getTaskId();
      } else if (taskId.equals(sheet.getTaskId()) == false) {
        taskId = null;
        break;
      }
    }
    if (taskId != null) {
      // All time sheets have the same task, so pre-select this task.
      timesheetDao.setTask(form.data, taskId);
    }
    body.add(form);
    form.init();
    final List<IColumn<TimesheetDO>> columns = TimesheetListPage.createDataTable(this, true, false, taskFormatter, taskTree, kostCache,
        userFormatter, dateTimeFormatter);
    @SuppressWarnings("serial")
    final SortableDataProvider<TimesheetDO> sortableDataProvider = new SortableDataProvider<TimesheetDO>() {
      public Iterator<TimesheetDO> iterator(int first, int count)
      {
        SortParam sp = getSort();
        if (timesheets == null) {
          return null;
        }
        Comparator<TimesheetDO> comp = new MyBeanComparator<TimesheetDO>(sp.getProperty(), sp.isAscending());
        Collections.sort(timesheets, comp);
        return timesheets.subList(first, first + count).iterator();
      }

      public int size()
      {
        return timesheets != null ? timesheets.size() : 0;
      }

      public IModel<TimesheetDO> model(final TimesheetDO object)
      {
        return new Model<TimesheetDO>() {
          @Override
          public TimesheetDO getObject()
          {
            return object;
          }
        };
      }
    };
    sortableDataProvider.setSort("startTime", false);

    final DefaultDataTable<TimesheetDO> dataTable = new DefaultDataTable<TimesheetDO>("table", columns, sortableDataProvider, 1000);
    body.add(dataTable);
    body.add(new Label("showUpdateQuestionDialog", "function showUpdateQuestionDialog() {\n" + //
        "  return window.confirm('"
        + getString("question.massUpdateQuestion")
        + "');\n"
        + "}\n") //
        .setEscapeModelStrings(false));
  }

  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  public void select(String property, Object selectedValue)
  {
    if ("taskId".equals(property) == true) {
      timesheetDao.setTask(form.data, (Integer) selectedValue);
      form.refresh();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(String property)
  {
    if ("taskId".equals(property) == true) {
      form.data.setTask(null);
      form.refresh();
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("timesheet.massupdate.title");
  }

  protected void onCancelSubmit()
  {
    setResponsePage(callerPage);
  }

  protected void onUpdateAllSubmit()
  {
    timesheetDao.massUpdate(timesheets, form.data);
    if (callerPage instanceof AbstractListPage< ? , ? , ? >) {
      ((AbstractListPage< ? , ? , ? >) callerPage).setMassUpdateMode(false);
    }
    setResponsePage(callerPage);
  }
}
