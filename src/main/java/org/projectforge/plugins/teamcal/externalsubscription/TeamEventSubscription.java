/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.externalsubscription;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventUtils;
import org.projectforge.web.calendar.CalendarFeed;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class TeamEventSubscription implements Serializable
{
  private static final long serialVersionUID = -9200146874015146227L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventSubscription.class);

  private final Integer teamCalId;

  private final SubscriptionHolder eventDurationAccess;

  private final List<TeamEventDO> recurrenceEvents;

  private final TeamCalDao teamCalDao;

  private String currentInitializedHash;

  private Long lastUpdated;

  private final HttpClient client;

  private static final Long TIME_IN_THE_PAST = 60L * 24 * 60 * 60 * 1000; // 60 days in millis in the past to subscribe

  public TeamEventSubscription(final TeamCalDao teamCalDao, final TeamCalDO teamCalDo)
  {
    this.teamCalDao = teamCalDao;
    this.teamCalId = teamCalDo.getId();
    eventDurationAccess = new SubscriptionHolder();
    recurrenceEvents = new ArrayList<TeamEventDO>();
    currentInitializedHash = null;
    lastUpdated = null;
    client = new HttpClient();
    init(teamCalDo);
  }

  private void init(final TeamCalDO teamCalDo)
  {
    String url = teamCalDo.getExternalSubscriptionUrl();
    if (teamCalDo.isExternalSubscription() == false || StringUtils.isEmpty(url) == true) {
      // No external subscription.
      return;
    }
    url = StringUtils.replace(url, "webcal", "http");
    // Shorten the url or avoiding logging of user credentials as part of the url
    final StringBuffer buf = new StringBuffer();
    boolean dotRead = false;
    for (int i = 0; i < url.length(); i++) {
      final char ch = url.charAt(i);
      if (dotRead == true && ch == '/') { // Slash after domain found
        // Shorten http://www.projectforge.org/cal/... -> http://www.projectforge.org
        buf.append("/...");
        break;
      } else if (ch == '?') {
        buf.append("?...");
        break;
      } else if (ch == '.') {
        dotRead = true;
      }
      buf.append(ch);
    }
    final String displayUrl = buf.toString();
    log.info("Getting subscribed calendar #" + teamCalDo.getId() + " from: " + displayUrl);
    final CalendarBuilder builder = new CalendarBuilder();
    byte[] bytes = null;
    try {

      // Create a method instance.
      final GetMethod method = new GetMethod(url);

      final int statusCode = client.executeMethod(method);

      if (statusCode != HttpStatus.SC_OK) {
        log.error("Unable to gather subscription calendar #"
            + teamCalDo.getId()
            + " information, using database from url '"
            + displayUrl
            + "'. Received statusCode: "
            + statusCode);
        return;
      }

      final MessageDigest md = MessageDigest.getInstance("MD5");

      // Read the response body.
      final InputStream stream = method.getResponseBodyAsStream();
      bytes = IOUtils.toByteArray(stream);

      final String md5 = calcHexHash(md.digest(bytes));
      if (StringUtils.equals(md5, teamCalDo.getExternalSubscriptionHash()) == false) {
        teamCalDo.setExternalSubscriptionHash(md5);
        teamCalDo.setExternalSubscriptionCalendarBinary(bytes);
        // internalUpdate is valid at this point, because we are calling this method in an async thread
        teamCalDao.internalUpdate(teamCalDo);
      }
    } catch (final Exception e) {
      bytes = teamCalDo.getExternalSubscriptionCalendarBinary();
      log.error("Unable to gather subscription calendar #"
          + teamCalDo.getId()
          + " information, using database from url '"
          + displayUrl
          + "': "
          + e.getMessage(), e);
    }
    if (bytes == null) {
      log.error("Unable to use database subscription calendar #" + teamCalDo.getId() + " information, quit from url '" + displayUrl + "'.");
      return;
    }
    if (currentInitializedHash != null && StringUtils.equals(currentInitializedHash, teamCalDo.getExternalSubscriptionHash()) == true) {
      // nothing to do here if the hashes are equal
      log.info("No modification of subscribed calendar #" + teamCalDo.getId() + " found from: " + displayUrl + " (OK, nothing to be done).");
      return;
    }
    try {
      final Date timeInPast = new Date(System.currentTimeMillis() - TIME_IN_THE_PAST);
      final Calendar calendar = builder.build(new ByteArrayInputStream(bytes));
      @SuppressWarnings("unchecked")
      final List<Component> list = calendar.getComponents(Component.VEVENT);
      final List<VEvent> vEvents = new ArrayList<VEvent>();
      for (final Component c : list) {
        final VEvent event = (VEvent) c;
        if (event.getSummary() != null && StringUtils.equals(event.getSummary().getValue(), CalendarFeed.SETUP_EVENT) == true) {
          // skip setup event!
          continue;
        }
        // skip only far gone events, if they have no recurrence
        if (event.getStartDate().getDate().before(timeInPast) && event.getProperty(Property.RRULE) == null) {
          continue;
        }
        vEvents.add(event);
      }
      // clear
      eventDurationAccess.clear();
      recurrenceEvents.clear();

      // the event id must (!) be negative and decrementing (different on each event)
      Integer startId = -1;
      for (final VEvent event : vEvents) {
        final TeamEventDO teamEvent = TeamEventUtils.createTeamEventDO(event);
        teamEvent.setId(startId);
        teamEvent.setCalendar(teamCalDo);

        if (teamEvent.hasRecurrence() == true) {
          // special treatment for recurrence events ..
          recurrenceEvents.add(teamEvent);
        } else {
          eventDurationAccess.add(teamEvent);
        }

        startId--;
      }
      lastUpdated = System.currentTimeMillis();
      currentInitializedHash = teamCalDo.getExternalSubscriptionHash();
      log.info("Subscribed calendar #" + teamCalDo.getId() + " successfully received from: " + displayUrl);
    } catch (final Exception e) {
      log.error("Unable to instantiate team event list for calendar #"
          + teamCalDo.getId()
          + " information, quit from url '"
          + displayUrl
          + "': "
          + e.getMessage(), e);
    }
  }

  /**
   * calculates hexadecimal representation of
   * @param md5
   * @return
   */
  private String calcHexHash(byte[] md5) {
    String result = null;
    if (md5 != null) {
      result = new BigInteger(1, md5).toString(16);
    }
    return result;
  }

  public List<TeamEventDO> getEvents(final Long startTime, final Long endTime)
  {
    Long perfStart = System.currentTimeMillis();
    List<TeamEventDO> result = eventDurationAccess.getResultList(startTime, endTime);
    Long perfDuration = System.currentTimeMillis() - perfStart;
    log.info("calculation of team events took " + perfDuration + " ms for " + result.size() + " events of " +eventDurationAccess.size() + " in total from calendar #"+teamCalId+".");
    return result;
  }

  public Integer getTeamCalId()
  {
    return teamCalId;
  }

  public Long getLastUpdated()
  {
    return lastUpdated;
  }

  public List<TeamEventDO> getRecurrenceEvents()
  {
    return recurrenceEvents;
  }
}
