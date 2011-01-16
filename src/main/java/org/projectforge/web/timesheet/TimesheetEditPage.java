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

package org.projectforge.web.timesheet;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.DayHolder;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.DateHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.timesheet.TimesheetPrefData;
import org.projectforge.timesheet.TimesheetPrefEntry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserPrefEditPage;
import org.projectforge.web.wicket.AbstractAutoLayoutEditPage;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;


@EditPage(defaultReturnPage = TimesheetListPage.class)
public class TimesheetEditPage extends AbstractAutoLayoutEditPage<TimesheetDO, TimesheetEditForm, TimesheetDao> implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimesheetEditPage.class);

  protected static final String[] BOOKMARKABLE_SELECT_PROPERTIES = new String[] { "taskId|task", "userId|user", "kost2Id|kost2"};

  /**
   * Key for preset the start date.
   */
  public static final String PARAMETER_KEY_START_DATE_IN_MILLIS = "startMillis";

  /**
   * Key for preset the stop date.
   */
  public static final String PARAMETER_KEY_STOP_DATE_IN_MILLIS = "stopMillis";

  /**
   * Key for preset the description.
   */
  public static final String PARAMETER_KEY_DESCRIPTION = "description";

  /**
   * Key for preset the task id.
   */
  public static final String PARAMETER_KEY_TASK_ID = TimesheetListPage.PARAMETER_KEY_TASK_ID;

  /**
   * Key for preset the user.
   */
  public static final String PARAMETER_KEY_USER = "user";

  /** Max length of combo box entries. */
  static final int MAX_LENGTH_OF_RECENT_TASKS = 80;

  /** The first recent block contains entries in chronological order. */
  static final int SIZE_OF_FIRST_RECENT_BLOCK = 5;

  private static final long serialVersionUID = -8192471994161712577L;

  @SpringBean(name = "kostCache")
  private KostCache kostCache;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "timesheetDao")
  private TimesheetDao timesheetDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  public TimesheetEditPage(PageParameters parameters)
  {
    super(parameters, "timesheet");
    super.init();
    if (isNew() == true) {
      final Integer taskId = parameters.getAsInteger(PARAMETER_KEY_TASK_ID);
      if (taskId != null) {
        getBaseDao().setTask(getData(), taskId);
      }
      final Long startTimeInMillis = parameters.getAsLong(PARAMETER_KEY_START_DATE_IN_MILLIS);
      final Long stopTimeInMillis = parameters.getAsLong(PARAMETER_KEY_STOP_DATE_IN_MILLIS);
      if (startTimeInMillis != null) {
        form.renderer.startDateTimePanel.setDate(startTimeInMillis);
        if (stopTimeInMillis == null) {
          getData().setStopTime(new Timestamp(startTimeInMillis)); // Default is time sheet with zero duration.
        }
      }
      if (stopTimeInMillis != null) {
        getData().setStopTime(new Timestamp(stopTimeInMillis));
        if (startTimeInMillis == null) {
          form.renderer.startDateTimePanel.setDate(stopTimeInMillis); // Default is time sheet with zero duration.
        }
      }
      final String description = parameters.getString(PARAMETER_KEY_DESCRIPTION);
      if (description != null) {
        getData().setDescription(description);
      }
      final int userId = parameters.getInt(PARAMETER_KEY_USER, -1);
      if (userId != -1) {
        timesheetDao.setUser(getData(), userId);
      }
    }

    if (isNew() == true) {
      TimesheetPrefData pref = getTimesheetPrefData();
      TimesheetPrefEntry entry = null;
      if (pref != null) {
        entry = pref.getNewesRecentEntry();
        if (getData().getTaskId() == null && entry != null) {
          getBaseDao().setTask(getData(), entry.getTaskId());
        }
      }
      if (getData().getUserId() == null) {
        getBaseDao().setUser(getData(), getUser().getId());
      }
    }

    //form.addKost2Row();
    //form.addConsumptionBar();
  }

  @Override
  protected TimesheetDao getBaseDao()
  {
    return timesheetDao;
  }

  @Override
  public void setResponsePage()
  {
    super.setResponsePage();
  }

  @Override
  protected TimesheetEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, TimesheetDO data)
  {
    return new TimesheetEditForm(this, data);
  }

  /**
   * Return list for table with all recent used time sheets.
   * @return
   */
  protected List<TimesheetDO> getRecentTimesheets()
  {
    final TimesheetPrefData data = getTimesheetPrefData();
    final List<TimesheetDO> list = new ArrayList<TimesheetDO>();
    if (data != null && data.getRecents() != null) {
      for (final TimesheetPrefEntry entry : data.getRecents()) {
        TimesheetDO sheet = getRecentSheet(entry);
        list.add(sheet);
      }
      Collections.sort(list, new Comparator<TimesheetDO>() {
        public int compare(final TimesheetDO t1, final TimesheetDO t2)
        {
          final Kost2DO kost1 = t1.getKost2();
          final Kost2DO kost2 = t2.getKost2();
          final ProjektDO project1 = kost1 != null ? kost1.getProjekt() : null;
          final ProjektDO project2 = kost2 != null ? kost2.getProjekt() : null;
          final String kunde1 = project1 != null && project1.getKunde() != null ? project1.getKunde().getName() : null;
          final String kunde2 = project2 != null && project2.getKunde() != null ? project2.getKunde().getName() : null;
          return new CompareToBuilder().append(kunde1, kunde2).append(project1 != null ? project1.getName() : null,
              project2 != null ? project2.getName() : null).append(t1.getTask() != null ? t1.getTask().getTitle() : null,
              t2.getTask() != null ? t2.getTask().getTitle() : null).toComparison();
        }
      });
      int i = 0;
      for (final TimesheetPrefEntry entry : data.getRecents()) {
        TimesheetDO sheet = getRecentSheet(entry);
        list.add(i, sheet);
        if (i++ >= SIZE_OF_FIRST_RECENT_BLOCK) {
          break;
        }
      }
    }
    return list;
  }

  /**
   * Gets the recent locations as Json object.
   */
  public List<String> getRecentLocations()
  {
    TimesheetPrefData data = getTimesheetPrefData();
    if (data != null) {
      return data.getRecentLocations();
    }
    return null;
  }

  private TimesheetDO getRecentSheet(TimesheetPrefEntry entry)
  {
    TimesheetDO sheet = new TimesheetDO();
    TaskDO task = taskTree.getTaskById(entry.getTaskId());
    sheet.setTask(task);
    Kost2DO kost2 = kostCache.getKost2(entry.getKost2Id());
    sheet.setKost2(kost2);
    sheet.setDescription(entry.getDescription());
    sheet.setLocation(entry.getLocation());
    PFUserDO user = userGroupCache.getUser(entry.getUserId());
    sheet.setUser(user);
    return sheet;
  }

  protected TimesheetPrefData getTimesheetPrefData()
  {
    TimesheetPrefData pref = (TimesheetPrefData) getUserPrefEntry(TimesheetEditPage.class.getName());
    if (pref == null) {
      pref = new TimesheetPrefData();
      putUserPrefEntry(TimesheetEditPage.class.getName(), pref, true);
    }
    return pref;
  }

  /**
   * Sets the id of the current time sheet to null and the user to the logged in user and returns to the input page. This results in adding
   * a new time sheet. (Does not clone TimesheetEditAction!)
   */
  protected void cloneTimesheet()
  {
    TimesheetDO timesheet = getData();
    log.info("Clone of time sheet chosen: " + timesheet);
    timesheet.setId(null);
    getBaseDao().setUser(timesheet, getUser().getId());
    form.renderer.cloneButtonPanel.setVisible(false);
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(final String property, final Object selectedValue)
  {
    if ("taskId".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }
      getBaseDao().setTask(getData(), id);
      form.renderer.refresh();
    } else if ("date".equals(property) == true) {
      if (selectedValue instanceof TimePeriod) {
        final Date startDate = ((TimePeriod) selectedValue).getFromDate();
        final Date stopDate = ((TimePeriod) selectedValue).getToDate();
        if (startDate != null) {
          getData().setStartDate(startDate);
          form.renderer.startDateTimePanel.markModelAsChanged();
        } else if (stopDate != null) {
          getData().setStartDate(stopDate);
          form.renderer.startDateTimePanel.markModelAsChanged();
        }
        if (stopDate != null) {
          getData().setStopTime(new Timestamp(stopDate.getTime()));
          // form.stopDateTimePanel.markModelAsChanged();
        } else if (startDate != null) {
          getData().setStopTime(new Timestamp(startDate.getTime()));
          // form.stopDateTimePanel.markModelAsChanged();
        }
      } else {
        final Date date;
        if (selectedValue instanceof String) {
          final Long ms = NumberHelper.parseLong((String) selectedValue);
          date = new Date(ms);
        } else {
          date = (Date) selectedValue;
        }
        final DayHolder dh = new DayHolder(date);
        final DateHolder startDateHolder = form.renderer.startDateTimePanel.getDateHolder();
        startDateHolder.setDay(dh.getCalendar());
        getData().setStartDate(startDateHolder.getDate());
        form.renderer.startDateTimePanel.markModelAsChanged();
      }
    } else if ("userId".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }
      getBaseDao().setUser(getData(), id);
    } else if ("kost2Id".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }
      getBaseDao().setKost2(getData(), id);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    if ("taskId".equals(property) == true) {
      getData().setTask(null);
      form.renderer.refresh();
    } else if ("userId".equals(property) == true) {
      getData().setUser(null);
      form.renderer.refresh();
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

  @Override
  public AbstractBasePage onSaveOrUpdate()
  {
    final TimesheetPrefData pref = getTimesheetPrefData();
    final TimesheetDO timesheet = getData();
    pref.appendRecentEntry(timesheet);
    pref.appendRecentTask(timesheet.getTaskId());
    if (StringUtils.isNotBlank(timesheet.getLocation()) == true) {
      pref.appendRecentLocation(timesheet.getLocation());
    }
    return null;
  }
  
  @Override
  public AbstractBasePage afterSaveOrUpdate()
  {
    if (form.saveAsTemplate == true) {
      final UserPrefEditPage userPrefEditPage = new UserPrefEditPage(UserPrefArea.TIMESHEET_TEMPLATE, getData());
      userPrefEditPage.setReturnToPage(this.returnToPage);
      return userPrefEditPage;
    }
    return null;
  }
  
  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
  }

  @Override
  protected String[] getBookmarkableSelectProperties()
  {
    return BOOKMARKABLE_SELECT_PROPERTIES;
  }

  @Override
  protected void onPreEdit()
  {
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
