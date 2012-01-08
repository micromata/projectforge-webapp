/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.task.TaskDO;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.web.HtmlHelper;

/**
 * For displaying timesheets in selectDate (calendar). Holds basic properties of TimesheetDO add some additional functionalities use-able
 * for more convenient use-age.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DisplayTimesheet implements Serializable
{
  private static final long serialVersionUID = 7956824126208303614L;

  private Integer id;

  private Date startTime;

  private Date stopTime;

  private String description;

  private String location;

  private String taskPath;

  private TaskDO task;

  private Kost2DO kost2;

  protected boolean startTimeLinkEnabled = true;

  protected boolean stopTimeLinkEnabled = true;

  /**
   * @param startTime
   * @param stopTime
   * @return
   * @see #isEmptyTimesheet()
   */
  public static DisplayTimesheet createBreak(Date startTime, Date stopTime)
  {
    DisplayTimesheet timesheet = new DisplayTimesheet();
    timesheet.startTime = startTime;
    timesheet.stopTime = stopTime;
    return timesheet;
  }

  private DisplayTimesheet()
  {

  }

  /**
   * Empty time sheets are automatically generated pause items for displaying in calendar.
   * @return
   * @see #createBreak(Date, Date)
   */
  public boolean isBreak()
  {
    return task == null;
  }

  public DisplayTimesheet(TimesheetDO timesheet)
  {
    this.startTime = timesheet.getStartTime();
    this.stopTime = timesheet.getStopTime();
    this.description = timesheet.getDescription();
    this.task = timesheet.getTask();
    this.kost2 = timesheet.getKost2();
    this.location = timesheet.getLocation();
    this.taskPath = timesheet.getTask().getTitle();
    this.id = timesheet.getId();
  }

  public Date getStartTime()
  {
    return startTime;
  }

  public Date getStopTime()
  {
    return stopTime;
  }

  public String getDescription()
  {
    return description;
  }

  public TaskDO getTask()
  {
    return task;
  }

  public Kost2DO getKost2()
  {
    return kost2;
  }

  public String getFormatted4Calendar()
  {
    if (kost2 == null) {
      return (task != null && task.getTitle() != null) ? HtmlHelper.escapeXml(task.getTitle()) : "";
    }
    StringBuffer buf = new StringBuffer();
    StringBuffer b2 = new StringBuffer();
    final ProjektDO projekt = kost2.getProjekt();
    if (projekt != null) {
      // final KundeDO kunde = projekt.getKunde();
      // if (kunde != null) {
      // if (StringUtils.isNotBlank(kunde.getIdentifier()) == true) {
      // b2.append(kunde.getIdentifier());
      // } else {
      // b2.append(kunde.getName());
      // }
      // b2.append(" - ");
      // }
      if (StringUtils.isNotBlank(projekt.getIdentifier()) == true) {
        b2.append(projekt.getIdentifier());
      } else {
        b2.append(projekt.getName());
      }
    } else {
      b2.append(kost2.getDescription());
    }
    buf.append(HtmlHelper.escapeXml(StringUtils.abbreviate(b2.toString(), 30)));
    return buf.toString();
  }

  public String getToolTip()
  {
    StringBuffer buf = new StringBuffer();
    if (StringUtils.isNotBlank(this.location) == true) {
      buf.append(this.location);
      if (StringUtils.isNotBlank(this.description) == true) {
        buf.append(": ");
      }
    }
    buf.append(StringUtils.defaultString(this.description));
    buf.append("; \n").append(this.taskPath);
    return buf.toString();
  }

  public Integer getId()
  {
    return id;
  }

  /**
   * Should be true, if the stop time of the previous time sheet for one user is before the start time of current time sheet (therefore the
   * user can insert a time sheet between both).
   */
  public boolean isStartTimeLinkEnabled()
  {
    return startTimeLinkEnabled;
  }

  public void setStartTimeLinkEnabled(boolean startTimeLinkEnabled)
  {
    this.startTimeLinkEnabled = startTimeLinkEnabled;
  }

  /**
   * Should be true, if the start time of the next time sheet for one user is after the stop time of current time sheet (therefore the user
   * can insert a time sheet between both).
   */
  public boolean isStopTimeLinkEnabled()
  {
    return stopTimeLinkEnabled;
  }

  public void setStopTimeLinkEnabled(boolean stopTimeLinkEnabled)
  {
    this.stopTimeLinkEnabled = stopTimeLinkEnabled;
  }
}
