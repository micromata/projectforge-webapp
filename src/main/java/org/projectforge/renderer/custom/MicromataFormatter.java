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

package org.projectforge.renderer.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.renderer.CellHolder;
import org.projectforge.renderer.RenderType;
import org.projectforge.renderer.RowHolder;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.core.DummyPageContext;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.user.UserFormatter;


/**
 * @author Sebastian Hardt (s.hardt@micromata.de)
 * 
 */
public class MicromataFormatter extends Formatter
{

  private TaskFormatter taskFormatter;

  private UserFormatter userFormatter;

  private DateTimeFormatter dateTimeFormatter;

  private KostCache kostCache;

  private HtmlHelper htmlHelper;

  public Map<String, Object> getData(List<TimesheetDO> timeSheets, Integer taskId, HttpServletRequest request,
      HttpServletResponse response, TimesheetFilter actionFilter)
  {

    Map<String, Object> data = new HashMap<String, Object>();

    long durationSum = 0;

    for (TimesheetDO timesheet : timeSheets) {
      durationSum += timesheet.getDuration();
    }

    DummyPageContext pageContext = new DummyPageContext(request, response);
    List<RowHolder> list = new ArrayList<RowHolder>();
    for (TimesheetDO timesheet : timeSheets) {
      RowHolder row = new RowHolder();
      if (actionFilter.getUserId() != null) {
        Kost2DO kost2 = kostCache.getKost2(timesheet.getKost2Id());
        if (kost2 != null) {
          row.addCell(new CellHolder(KostFormatter.format(kost2)));
        } else {
          row.addCell(new CellHolder(""));
        }
      } else {
        row.addCell(new CellHolder(userFormatter.getFormattedUser(timesheet.getUser())));
      }
      row.addCell(new CellHolder(taskFormatter.getTaskPath(pageContext, timesheet.getTaskId(), taskId, false, true)));
      row.addCell(new CellHolder(dateTimeFormatter.getFormattedTimePeriod(timesheet.getTimePeriod(), RenderType.FOP, true)));
      row.addCell(new CellHolder(dateTimeFormatter.getFormattedDuration(timesheet.getTimePeriod())));
      row.addCell(new CellHolder(htmlHelper.formatXSLFOText(timesheet.getDescription(), true)));
      if (StringUtils.isNotBlank(timesheet.getLocation()) == true) {
        row.addCell(new CellHolder(htmlHelper.formatXSLFOText(timesheet.getLocation(), true)));
      } else {
        row.addCell(new CellHolder(""));
      }
      list.add(row);
    }

    data.put("list", list);
    data.put("title", getLocalizedString("timesheet.title.list"));
    data.put("systemDate", dateTimeFormatter.getFormattedDateTime(new Date()));
    data.put("searchStringLabel", getLocalizedString("searchString"));

    if (StringUtils.isNotEmpty(actionFilter.getSearchString()) == true) {
      data.put("searchString", htmlHelper.formatXSLFOText(actionFilter.getSearchString(), true));
    }

    data.put("timePeriodLabel", getLocalizedString("timePeriod"));

    data.put("startTime", dateTimeFormatter.getFormattedDate(actionFilter.getStartTime()));
    data.put("stopTime", dateTimeFormatter.getFormattedDate(actionFilter.getStopTime()));
    data.put("taskLabel", getLocalizedString("task"));

    if (taskId != null) {
      data.put("task", taskFormatter.getTaskPath(pageContext, taskId));
    }

    data.put("userLabel", getLocalizedString("timesheet.user"));

    if (actionFilter.getUserId() != null) {
      data.put("user", userFormatter.getFormattedUser(actionFilter.getUserId()));
    }
    data.put("totalDurationLabel", getLocalizedString("timesheet.totalDuration"));

    String str1 = dateTimeFormatter.getFormattedDuration(durationSum);
    String str2 = dateTimeFormatter.getFormattedDuration(durationSum, dateTimeFormatter.getDurationOfWorkingDay(), -1);
    data.put("totalDuration", str1);
    if (str1.equals(str2) == false) {
      data.put("totalHours", str2);
    }

    data.put("optionsLabel", getLocalizedString("label.options"));
    data.put("deletedLabel", getLocalizedString("deleted"));

    data.put("deleted", actionFilter.isDeleted());

    data.put("durationLabel", getLocalizedString("timesheet.duration"));
    data.put("descriptionLabel", getLocalizedString("description"));
    data.put("locationLabel", getLocalizedString("timesheet.location"));

    return data;
  }

  /**
   * @param taskFormatter the taskFormatter to set
   */
  public void setTaskFormatter(TaskFormatter taskFormatter)
  {
    this.taskFormatter = taskFormatter;
  }

  /**
   * @param userFormatter the userFormatter to set
   */
  public void setUserFormatter(UserFormatter userFormatter)
  {
    this.userFormatter = userFormatter;
  }

  /**
   * @param dateTimeFormatter the dateTimeFormatter to set
   */
  public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter)
  {
    this.dateTimeFormatter = dateTimeFormatter;
  }

  /**
   * @param htmlHelper the htmlHelper to set
   */
  public void setHtmlHelper(HtmlHelper htmlHelper)
  {
    this.htmlHelper = htmlHelper;
  }

  public void setKostCache(KostCache kostCache)
  {
    this.kostCache = kostCache;
  }
}
