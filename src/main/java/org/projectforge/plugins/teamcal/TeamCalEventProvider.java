/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.util.List;

import net.ftlines.wicket.fullcalendar.Event;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.projectforge.common.DateHelper;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.calendar.MyFullCalendarEventsProvider;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class TeamCalEventProvider extends MyFullCalendarEventsProvider
{

  private static final long serialVersionUID = -5609599079385073490L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEventProvider.class);

  //  private final TeamCalDao teamCalDao;

  @SpringBean(name = "teamEventDao")
  private final TeamEventDao teamEventDao;

  //  private long totalDuration;

  private Integer month;

  //  private DateTime firstDayOfMonth;

  private int days;

  // duration by day of month.
  private final long[] durationsPerDayOfMonth = new long[32];

  private final long[] durationsPerDayOfYear = new long[380];

  private final TeamEventFilter eventFilter;

  private final Integer teamCalId;

  /**
   * @param parent component for i18n
   */
  public TeamCalEventProvider(final Component parent, final TeamCalDao teamCalDao, final TeamEventDao teamEventDao, final Integer teamCalId)
  {
    super(parent);
    //    this.teamCalDao = teamCalDao;
    this.teamCalId = teamCalId;
    this.teamEventDao = teamEventDao;
    eventFilter = new TeamEventFilter();
  }

  /**
   * @see org.projectforge.web.calendar.MyFullCalendarEventsProvider#buildEvents(org.joda.time.DateTime, org.joda.time.DateTime)
   */
  @Override
  protected void buildEvents(final DateTime start, final DateTime end)
  {
    //    final Event event = new Event();
    //    event.setStart(start);
    //    event.setEnd(end);
    //    event.setAllDay(true);
    //    event.setTitle("Hallo");
    //    events.put(""+System.currentTimeMillis(), event);
    //    totalDuration = 0;
    for (int i = 0; i < durationsPerDayOfMonth.length; i++) {
      durationsPerDayOfMonth[i] = 0;
    }
    for (int i = 0; i < durationsPerDayOfYear.length; i++) {
      durationsPerDayOfYear[i] = 0;
    }
    eventFilter.setTeamCalId(teamCalId);
    eventFilter.setStartDate(start.toDate());
    eventFilter.setEndDate(end.toDate());
    final List<TeamEventDO> teamEvents = teamEventDao.getEventList(eventFilter);
    boolean longFormat = false;
    days = Days.daysBetween(start, end).getDays();
    if (days < 10) {
      // Week or day view:
      longFormat = true;
      month = null;
      //      firstDayOfMonth = null;
    } else {
      // Month view:
      final DateTime currentMonth = new DateTime(start.plusDays(10), PFUserContext.getDateTimeZone()); // Now we're definitely in the right
      // month.
      month = currentMonth.getMonthOfYear();
      //      firstDayOfMonth = currentMonth.withDayOfMonth(1);
    }
    if (CollectionUtils.isNotEmpty(teamEvents) == true) {
      for (final TeamEventDO teamEvent : teamEvents) {
        final DateTime startDate = new DateTime(teamEvent.getStartDate(), PFUserContext.getDateTimeZone());
        final DateTime endDate = new DateTime(teamEvent.getEndDate(), PFUserContext.getDateTimeZone());
        if (endDate.isBefore(start) == true || startDate.isAfter(end) == true) {
          // Time sheet doesn't match time period start - end.
          continue;
        }
        final long duration = teamEvent.getDuration();
        final Event event = new Event();
        event.setId("" + teamEvent.getId());
        event.setStart(startDate);
        event.setEnd(endDate);
        final String title = teamEvent.getSubject();
        if (longFormat == true) {
          // Week or day view:
          final DateTime dt = new DateTime(duration);
          String hour = dt.getHourOfDay()+"";
          String minute = dt.getMinuteOfHour()+"";
          if (dt.getHourOfDay() < 10)
            hour = "0" + dt.getHourOfDay();
          if (dt.getMinuteOfHour() < 10)
            minute = "0" + dt.getMinuteOfHour();
          event.setTitle(title + "\nNote: " + teamEvent.getNote() + "\nDauer: " + hour + ":" + minute);
          //getToolTip(teamEvent) + "\n" + formatDuration(duration, false));
        } else {
          // Month view:
          event.setTitle(title);
        }
        if (month != null && startDate.getMonthOfYear() != month && endDate.getMonthOfYear() != month) {
          // Display team events of other month as grey blue:
          event.setTextColor("#222222").setBackgroundColor("#ACD9E8").setColor("#ACD9E8");
        }
        events.put(teamEvent.getId() + "", event);
        if (month == null || startDate.getMonthOfYear() == month) {
          //          totalDuration += duration;
          addDurationOfDay(startDate.getDayOfMonth(), duration);
        }
        final int dayOfYear = startDate.getDayOfYear();
        addDurationOfDayOfYear(dayOfYear, duration);
      }
    }
    //    setStatistics(start, end);
  }

  /**
   * @param start
   */
  private void setStatistics(final DateTime start, final DateTime end)
  {
    // Show statistics: duration of every day is shown as all day event.
    DateTime day = start;
    int paranoiaCounter = 0;
    do {
      if (++paranoiaCounter > 1000) {
        log.error("Paranoia counter exceeded! Dear developer, please have a look at the implementation of buildEvents.");
        break;
      }
      final int dayOfMonth = day.getDayOfMonth();
      final int dayOfYear = day.getDayOfYear();
      final long duration = durationsPerDayOfMonth[dayOfMonth];
      final boolean firstDayOfWeek = day.getDayOfWeek() == PFUserContext.getJodaFirstDayOfWeek();
      if (firstDayOfWeek == false && duration == 0) {
        day = day.plusDays(1);
        continue;
      }
      final Event event = new Event().setAllDay(true);
      final String id = "s-" + (dayOfYear);
      event.setId(id);
      event.setStart(day);
      final String durationString = null; //formatDuration(duration, false);
      if (firstDayOfWeek == true) {
        // Show week of year at top of first day of week.
        long weekDuration = 0;
        for (short i = 0; i < 7; i++) {
          weekDuration += durationsPerDayOfYear[dayOfYear + i];
        }
        final StringBuffer buf = new StringBuffer();
        buf.append(getString("calendar.weekOfYearShortLabel")).append(DateHelper.getWeekOfYear(day));
        if (days > 1 && weekDuration > 0) {
          // Show total sum of durations over all time sheets of current week (only in week and month view).
          //                      buf.append(": ").append(formatDuration(weekDuration, false));
        }
        if (duration > 0) {
          buf.append(", ").append(durationString);
        }
        event.setTitle(buf.toString());
      } else {
        event.setTitle(durationString);
      }
      event.setTextColor("#666666").setBackgroundColor("#F9F9F9").setColor("#F9F9F9");
      event.setEditable(false);
      events.put(id, event);
      day = day.plusDays(1);
    }
    while (day.isAfter(end) == false);
  }

  private void addDurationOfDay(final int dayOfMonth, final long duration)
  {
    durationsPerDayOfMonth[dayOfMonth] += duration;
  }

  /**
   * @param dayOfMonth
   * @see DateTime#getDayOfMonth()
   */
  public long getDurationOfDay(final int dayOfMonth)
  {
    return durationsPerDayOfMonth[dayOfMonth];
  }

  private void addDurationOfDayOfYear(final int dayOfYear, final long duration)
  {
    durationsPerDayOfYear[dayOfYear] += duration;
  }

  /**
   * @param weekOfYear
   * @see DateTime#getDayOfMonth()
   */
  public long getDurationOfWeekOfYear(final int weekOfYear)
  {
    return durationsPerDayOfMonth[weekOfYear];
  }

}
