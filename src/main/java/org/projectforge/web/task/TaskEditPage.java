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

package org.projectforge.web.task;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskHelper;
import org.projectforge.task.TaskTree;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.gantt.GanttChartEditPage;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.wicket.AbstractAutoLayoutEditPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

@EditPage(defaultReturnPage = TaskTreePage.class)
public class TaskEditPage extends AbstractAutoLayoutEditPage<TaskDO, TaskEditForm, TaskDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 5176663429783524587L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskEditPage.class);

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "kost2Dao")
  private Kost2Dao kost2Dao;

  public TaskEditPage(final PageParameters parameters)
  {
    super(parameters, "task");
    init();
    addTopMenuPanel();
    final Integer parentTaskId = parameters.getAsInteger("parentTaskId");
    if (NumberHelper.greaterZero(parentTaskId) == true) {
      taskDao.setParentTask(getData(), parentTaskId);
    }
  }

  @Override
  protected TaskDao getBaseDao()
  {
    return taskDao;
  }

  @Override
  protected TaskEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, TaskDO data)
  {
    return new TaskEditForm(this, data);
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(String property, Object selectedValue)
  {
    if ("parentTaskId".equals(property) == true) {
      taskDao.setParentTask(getData(), (Integer) selectedValue);
    } else if ("ganttPredecessorId".equals(property) == true) {
      taskDao.setGanttPredecessor(getData(), (Integer) selectedValue);
    } else if ("responsibleUserId".equals(property) == true) {
      taskDao.setResponsibleUser(getData(), (Integer) selectedValue);
    } else if ("startDate".equals(property) == true) {
      final Date date = (Date) selectedValue;
      getData().setStartDate(date);
      form.renderer.startDatePanel.markModelAsChanged();
    } else if ("protectTimesheetsUntil".equals(property) == true) {
      final Date date = (Date) selectedValue;
      getData().setProtectTimesheetsUntil(date);
      form.renderer.protectTimesheetsUntilPanel.markModelAsChanged();
    } else if ("endDate".equals(property) == true) {
      final Date date = (Date) selectedValue;
      getData().setEndDate(date);
      form.renderer.endDatePanel.markModelAsChanged();
    } else if ("kost2Id".equals(property) == true) {
      final Integer kost2Id = (Integer) selectedValue;
      if (kost2Id != null) {
        final Kost2DO kost2 = kost2Dao.getById(kost2Id);
        if (kost2 != null) {
          final String newKost2String = TaskHelper.addKost2(taskTree, getData(), kost2);
          getData().setKost2BlackWhiteList(newKost2String);
          form.renderer.kost2BlackWhiteTextField.modelChanged();
        }
      }
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    if ("parentTaskId".equals(property) == true) {
      getData().setParentTask(null);
    } else if ("ganttPredecessorId".equals(property) == true) {
      getData().setGanttPredecessor(null);
    } else if ("responsibleUserId".equals(property) == true) {
      getData().setResponsibleUser(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  private void addTopMenuPanel()
  {
    if (isNew() == false) {
      @SuppressWarnings("unchecked")
      final BookmarkablePageLink<String> addSubTaskLink = new BookmarkablePageLink("link", getClass());
      final ContentMenuEntryPanel addSubTaskMenuPanel = new ContentMenuEntryPanel(getNewContentMenuChildId(), addSubTaskLink,
          getString("task.menu.addSubTask"));
      contentMenuEntries.add(addSubTaskMenuPanel);

      final PageParameters timesheetEditPageParams = new PageParameters();
      timesheetEditPageParams.put(TimesheetEditPage.PARAMETER_KEY_TASK_ID, form.getData().getId());
      @SuppressWarnings("unchecked")
      final ContentMenuEntryPanel addTimesheetMenuPanel = new ContentMenuEntryPanel(getNewContentMenuChildId(), new BookmarkablePageLink("link",
          TimesheetEditPage.class, timesheetEditPageParams), getString("task.menu.addTimesheet"));
      contentMenuEntries.add(addTimesheetMenuPanel);

      @SuppressWarnings("unchecked")
      final BookmarkablePageLink<String> showTimesheetsLink = new BookmarkablePageLink("link", TimesheetListPage.class);
      if (form.getData().getId() != null) {
        showTimesheetsLink.setParameter(TimesheetListPage.PARAMETER_KEY_TASK_ID, form.getData().getId());
      }
      final ContentMenuEntryPanel showTimesheetsMenuPanel = new ContentMenuEntryPanel(getNewContentMenuChildId(), showTimesheetsLink,
          getString("task.menu.showTimesheets"));
      contentMenuEntries.add(showTimesheetsMenuPanel);

      @SuppressWarnings("unchecked")
      final BookmarkablePageLink<String> addGanttChartLink = new BookmarkablePageLink("link", GanttChartEditPage.class);
      if (form.getData().getId() != null) {
        addGanttChartLink.setParameter(GanttChartEditPage.PARAM_KEY_TASK, form.getData().getId());
      }
      final ContentMenuEntryPanel addGanttChartMenuPanel = new ContentMenuEntryPanel(getNewContentMenuChildId(), addGanttChartLink,
          getString("gantt.title.add"));
      contentMenuEntries.add(addGanttChartMenuPanel);

      addSubTaskLink.setParameter("parentTaskId", getData().getId());
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
