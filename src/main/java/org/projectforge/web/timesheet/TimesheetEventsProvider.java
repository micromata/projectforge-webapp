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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.EventNotFoundException;
import net.ftlines.wicket.fullcalendar.EventProvider;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.projectforge.calendar.TimePeriod;
import org.projectforge.common.StringHelper;
import org.projectforge.core.OrderDirection;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.task.TaskDO;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.calendar.CalendarFilter;

/**
 * Creates events for FullCalendar.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TimesheetEventsProvider implements EventProvider
{
  private static final long serialVersionUID = 2241430630558260146L;

  private final TimesheetDao timesheetDao;

  private final CalendarFilter calFilter;

  private final Map<Integer, Event> events = new HashMap<Integer, Event>();

  private long duration;

  private final Component parent;

  /**
   * @param parent For i18n.
   * @param timesheetDao
   * @param calFilter
   * @see Component#getString(String)
   */
  public TimesheetEventsProvider(final Component parent, final TimesheetDao timesheetDao, final CalendarFilter calFilter)
  {
    this.parent = parent;
    this.timesheetDao = timesheetDao;
    this.calFilter = calFilter;
  }

  @Override
  public Collection<Event> getEvents(final DateTime start, final DateTime end)
  {
    events.clear();
    final Integer userId = calFilter.getUserId();
    if (userId == null) {
      return events.values();
    }
    final TimesheetFilter filter = new TimesheetFilter();
    filter.setUserId(userId);
    filter.setStartTime(start.toDate());
    filter.setStopTime(end.toDate());
    filter.setOrderType(OrderDirection.ASC);
    final List<TimesheetDO> timesheets = timesheetDao.getList(filter);
    if (CollectionUtils.isEmpty(timesheets) == true) {
      return events.values();
    }
    boolean longFormat = false;
    if (Days.daysBetween(start, end).getDays() < 10) {
      // Week or day view:
      longFormat = true;
    }
    for (final TimesheetDO timesheet : timesheets) {
      final DateTime startTime = new DateTime(timesheet.getStartTime());
      final DateTime stopTime = new DateTime(timesheet.getStopTime());
      if (stopTime.isBefore(start) == true || startTime.isAfter(end) == true) {
        // Time sheet doesn't match time period start - end.
        continue;
      }
      final Event event = new Event();
      final Integer id = timesheet.getId();
      event.setId("" + id);
      event.setStart(startTime);
      event.setEnd(stopTime);
      final String title = getTitle(timesheet);
      if (longFormat == true) {
        // Week or day view:
        event.setTitle(title + "\n" + getToolTip(timesheet));
      } else {
        // Month view:
        event.setTitle(title);
      }
      events.put(id, event);
    }
    return events.values();
  }

  @Override
  public Event getEventForId(final String id) throws EventNotFoundException
  {
    final Integer idd = Integer.valueOf(id);
    final Event event = events.get(idd);
    if (event != null) {
      return event;
    }
    throw new EventNotFoundException("Event with id: " + id + " not found");
  }

  public String formatDuration(final long millis)
  {
    final int[] fields = TimePeriod.getDurationFields(millis, 8, 200);
    final StringBuffer buf = new StringBuffer();
    if (fields[0] > 0) {
      buf.append(fields[0]).append(getString("calendar.unit.day")).append(" ");
    }
    buf.append(fields[1]).append(":").append(StringHelper.format2DigitNumber(fields[2])).append(getString("calendar.unit.hour"));
    return buf.toString();
  }

  private String getTitle(final TimesheetDO timesheet)
  {
    final Kost2DO kost2 = timesheet.getKost2();
    final TaskDO task = timesheet.getTask();
    if (kost2 == null) {
      return (task != null && task.getTitle() != null) ? HtmlHelper.escapeXml(task.getTitle()) : "";
    }
    final StringBuffer buf = new StringBuffer();
    final StringBuffer b2 = new StringBuffer();
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
    buf.append(StringUtils.abbreviate(b2.toString(), 30));
    return buf.toString();
  }

  private String getToolTip(final TimesheetDO timesheet)
  {
    final String location = timesheet.getLocation();
    final String description = timesheet.getDescription();
    final TaskDO task = timesheet.getTask();
    final StringBuffer buf = new StringBuffer();
    if (StringUtils.isNotBlank(location) == true) {
      buf.append(location);
      if (StringUtils.isNotBlank(description) == true) {
        buf.append(": ");
      }
    }
    buf.append(StringUtils.defaultString(description));
    if (timesheet.getKost2() == null) {
      buf.append("; \n").append(task.getTitle());
    }
    return buf.toString();
  }

  private String getString(final String key)
  {
    return parent.getString(key);
  }
}