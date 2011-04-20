/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.projectforge.registry.Registry;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;

/**
 * Feed Servlet, which generates a 'text/calendar' output of the last four mounts.
 * Currenty relevant informations are date, start- and stop time and last but not
 * least the location of an event.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class CalendarFeed extends HttpServlet
{

  private static final long serialVersionUID = 1480433876190009435L;

  private static final int PERIOD_IN_MONTHS = 4;

  private PFUserDO user = null;

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
  {
    if (StringUtils.isBlank(req.getParameter("user")) || StringUtils.isBlank(req.getParameter("key"))) {
      resp.sendError(HttpStatus.SC_BAD_REQUEST);
      return;
    }

    final Calendar calendar = createCal(req.getParameter("user"), req.getParameter("key"));

    if (calendar == null) {
      resp.sendError(HttpStatus.SC_BAD_REQUEST);
      return;
    }

    resp.setContentType("text/calendar");
    final CalendarOutputter output = new CalendarOutputter();
    try {
      output.output(calendar, resp.getOutputStream());
    } catch (final ValidationException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * creates a calendar for the user, identified by his name and logged-in
   * cookie key.
   * 
   * @param userName
   * @param userKey
   * @return a calendar, null if authentication fails
   */
  public Calendar createCal(final String userName, final String userKey)
  {
    final UserDao userDao = (UserDao) Registry.instance().getDao(UserDao.class);
    final TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    user = userDao.getUserByStayLoggedInKey(userName, userKey);

    if (user == null) {
      return null;
    }

    PFUserContext.setUser(user);

    @SuppressWarnings("unused")
    final TimeZone timeZone = registry.getTimeZone(user.getTimeZone());

    // creating a new calendar
    final Calendar calendar = new Calendar();
    calendar.getProperties().add(
        new ProdId("-//" + user.getDisplayUsername() + "//ProjectForge//" + user.getLocale().toString().toUpperCase()));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);

    // adding events
    for (final VEvent event : getEvents()) {
      calendar.getComponents().add(event);
    }
    return calendar;
  }

  /**
   * builds the list of events
   * 
   * @return
   */
  private List<VEvent> getEvents()
  {
    final List<VEvent> events = new ArrayList<VEvent>();
    final TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    final TimeZone timezone = registry.getTimeZone(user.getTimeZone());
    final java.util.Calendar cal = java.util.Calendar.getInstance(timezone);

    // initializes timesheet filter
    final TimesheetFilter filter = new TimesheetFilter();
    filter.setUserId(user.getId());
    filter.setDeleted(false);
    filter.setStopTime(cal.getTime());

    // calculates the offset of the calendar
    final int offset = cal.get(java.util.Calendar.MONTH) - PERIOD_IN_MONTHS;

    if (offset < 0) {
      setCalDate(cal, cal.get(java.util.Calendar.YEAR) - 1, 12 + offset);
    } else {
      setCalDate(cal, cal.get(java.util.Calendar.YEAR), offset);
    }

    filter.setStartTime(cal.getTime());

    final TimesheetDao timesheetDao = (TimesheetDao) Registry.instance().getDao(TimesheetDao.class);
    final List<TimesheetDO> timesheetList = timesheetDao.getList(filter);

    // iterate over all timesheets and adds each event to the calendar
    for (final TimesheetDO timesheet : timesheetList) {

      final Date date = new Date(timesheet.getStartTime().getTime());
      cal.setTime(date);
      final DateTime startTime = getCalTime(timezone, cal);

      date.setTime(timesheet.getStopTime().getTime());
      cal.setTime(date);
      final DateTime stopTime = getCalTime(timezone, cal);

      final VEvent vEvent = new VEvent(startTime, stopTime, timesheet.getShortDescription());
      vEvent.getProperties().add(new Uid(startTime.toString()));
      vEvent.getProperties().add(new Location(timesheet.getLocation()));

      events.add(vEvent);
    }

    return events;
  }

  /**
   * 
   * @param timezone
   * @param cal
   * @return relevant DateTime for a calendar, using user timezone
   */
  private DateTime getCalTime(final TimeZone timezone, final java.util.Calendar cal)
  {
    final DateTime startTime = new DateTime(cal.getTime());
    startTime.setTimeZone(timezone);
    return startTime;
  }

  /**
   * sets the calendar to a special date. Used to calculate the year offset of an negative
   * time period. When the time period is set to 4 month and the current month is at the begin
   * of a year, the year-number must be decremented by one
   * 
   * @param cal
   * @param year
   * @param mounth
   */
  private void setCalDate(final java.util.Calendar cal, final int year, final int mounth)
  {
    cal.clear();
    cal.set(java.util.Calendar.YEAR, year);
    cal.set(java.util.Calendar.MONTH, mounth);
  }

}
