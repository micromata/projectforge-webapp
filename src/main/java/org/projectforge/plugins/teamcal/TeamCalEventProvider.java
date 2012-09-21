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
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.calendar.MyFullCalendarEventsProvider;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class TeamCalEventProvider extends MyFullCalendarEventsProvider
{

  private static final long serialVersionUID = -5609599079385073490L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEventProvider.class);

  private UserGroupCache userGroupCache;

  @SpringBean(name = "teamEventDao")
  private final TeamEventDao teamEventDao;

  private Integer month;

  private int days;

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
          // Event doesn't match time period start - end.
          continue;
        }
        final long duration = teamEvent.getDuration();
        final Event event = new Event();
        event.setId("" + teamEvent.getId());
        event.setStart(startDate);
        event.setEnd(endDate);
        if (teamEvent.isAllDay())
          event.setAllDay(true);
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

          final TeamCalRight right = new TeamCalRight();
          final PFUserDO user = PFUserContext.getUser();
          String durationString = "";
          if (right.isOwner(user, teamEvent.getCalendar()) == true
              || right.hasAccessGroup(teamEvent.getCalendar().getFullAccessGroup(), userGroupCache, user) == true) {
            if (event.isAllDay() == false)
              durationString = "\n"+ getString("plugins.teamevent.duration") + ": " + hour + ":" + minute;
            event.setTitle(getString("plugins.teamevent.subject") + ": " + title + "\n"+ getString("plugins.teamevent.note") + ": " + teamEvent.getNote() + durationString);
          } else {
            if (right.hasAccessGroup(teamEvent.getCalendar().getReadOnlyAccessGroup(), userGroupCache, user) == true) {
              if (event.isAllDay() == false)
                durationString = "\n"+ getString("plugins.teamevent.duration") + ": " + hour + ":" + minute;
              event.setTitle(getString("plugins.teamevent.subject") + ": " + title + "\n"+ getString("plugins.teamevent.note") + ": " + teamEvent.getNote() + durationString);
              event.setEditable(false);
            } else {
              // for minimal access
              event.setTitle("");
              event.setEditable(false);
            }
          }
        } else {
          // Month view:
          event.setTitle(title);
        }
        if (month != null && startDate.getMonthOfYear() != month && endDate.getMonthOfYear() != month) {
          // Display team events of other month as grey blue:
          event.setTextColor("#222222").setBackgroundColor("#ACD9E8").setColor("#ACD9E8");
        }
        events.put(teamEvent.getId() + "", event);
      }
    }
  }

  public void setUserGroupCache(final UserGroupCache userGroupCache) {
    this.userGroupCache = userGroupCache;
  }

  /**
   * @return the log
   */
  public static org.apache.log4j.Logger getLog()
  {
    return log;
  }
}
