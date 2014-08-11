/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Comment;
import net.fortuna.ical4j.model.property.Contact;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.lang.StringUtils;
import org.projectforge.calendar.ICal4JUtils;
import org.projectforge.core.ConfigXml;
import org.projectforge.mail.Mail;
import org.projectforge.mail.SendMail;
import org.projectforge.registry.Registry;
import org.projectforge.scripting.I18n;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class TeamEventMailer
{

  private static TeamEventMailer instance = null;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventMailer.class);

  private final Queue<TeamEventMailValue> queue;

  private final TeamEventDao teamEventDao;

  class Marker {
    protected final Date lastEmail;
    protected final HistoryEntry[] entries;
    private final List<HistoryEntry> list;
    private boolean locationChanged;
    private boolean dateChanged;
    private boolean recurrenceChanged;

    public Marker(final HistoryEntry[] entries, final Date lastEmail) {
      this.entries = entries;
      this.lastEmail=lastEmail;
      list = new LinkedList<HistoryEntry>();
      for (int i=0; i < entries.length-1; i++) {
        if (entries[i].getTimestamp().getTime() > lastEmail.getTime()) {
          list.add(entries[i]);
        }
      }
      locationChanged = hasLocationChanged();
      dateChanged = hasDateChanged();
      recurrenceChanged = hasRecurrenceChanged();
    }
    private boolean hasLocationChanged() {
      for (final HistoryEntry entry : list) {
        for (final PropertyDelta delta : entry.getDelta()) {
          if (StringUtils.contains(delta.getPropertyName(), "location") == true) {
            return true;
          }
        }
      }
      return false;
    }
    private boolean hasDateChanged() {
      for (final HistoryEntry entry : list) {
        for (final PropertyDelta delta : entry.getDelta()) {
          if (StringUtils.contains(delta.getPropertyName(), "startDate") == true ||
              StringUtils.contains(delta.getPropertyName(), "endDate") == true ) {
            return true;
          }
        }
      }
      return false;
    }
    private boolean hasRecurrenceChanged() {
      for (final HistoryEntry entry : list) {
        for (final PropertyDelta delta : entry.getDelta()) {
          if (StringUtils.contains(delta.getPropertyName(), "recurrenceRule") == true) {
            return true;
          }
        }
      }
      return false;
    }
    public void computeChanges(final TeamEventDO event, final TeamEventDO orgEvent) {
      if (StringUtils.equals(event.getLocation(), orgEvent.getLocation()) == false) {
        locationChanged = true;
      }
      if (event.getStartDate() != null && orgEvent.getStartDate() != null &&
          event.getEndDate() != null && orgEvent.getEndDate() != null) {
        if (event.getStartDate().equals(orgEvent.getStartDate()) == false ||
            event.getEndDate().equals(orgEvent.getEndDate()) == false) {
          dateChanged = true;
        }
      }
      if (StringUtils.equals(event.getRecurrenceRule(), orgEvent.getRecurrenceRule()) == false) {
        recurrenceChanged = true;
      }
    }
    public boolean isLocationChanged()
    {
      return locationChanged;
    }
    public boolean isDateChanged()
    {
      return dateChanged;
    }
    public boolean isRecurrenceChanged()
    {
      return recurrenceChanged;
    }
  }

  private TeamEventMailer() {
    queue =  new LinkedList<TeamEventMailValue>();
    teamEventDao = Registry.instance().getDao(TeamEventDao.class);
  }

  public static TeamEventMailer getInstance() {
    if (instance == null) {
      instance = new TeamEventMailer();
    }
    return instance;
  }

  public Queue<TeamEventMailValue> getQueue()
  {
    return queue;
  }

  public boolean send() {
    int failures = 0;
    while (queue.isEmpty() == false) {
      final TeamEventMailValue value = queue.poll();
      final TeamEventDO event = teamEventDao.getById(value.getId());
      if (sendIcsFile(event, value.getType(), value.getOrgId()) == false) {
        failures++;
        log.error("Can't send all emails for TeamEvent: " + event.getSubject());
      } else {
        final Timestamp t = new Timestamp(event.getLastUpdate().getTime());
        event.setLastEmail(t);
        teamEventDao.saveOrUpdate(event);
      }
    }
    return failures == 0 ? true : false;
  }

  private boolean sendIcsFile(final TeamEventDO event, final TeamEventMailType type, final Integer orgId) {
    int failures = 0;
    final Marker marker = new Marker(teamEventDao.getHistoryEntries(event), event.getLastEmail());
    if (orgId != null) {
      final TeamEventDO orgEvent = teamEventDao.getById(orgId);
      marker.computeChanges(event, orgEvent);
    }
    final String content = getICal(event, type);
    final Mail msg = new Mail();
    msg.setProjectForgeSubject(composeSubject(event, type));
    msg.setContentType(Mail.CONTENTTYPE_HTML);
    final SendMail sendMail = new SendMail();
    sendMail.setConfigXml(ConfigXml.getInstance());
    for (final TeamEventAttendeeDO attendee : event.getAttendees()) {
      msg.setContent(composeHtmlContent(event, attendee.getNumber(), type, marker));
      if (attendee.getUserId() == null) {
        msg.setTo(attendee.getUrl());
      } else {
        msg.setTo(attendee.getUser());
        if (attendee.getUser().equals(PFUserContext.getUser()) == true) {
          continue;
        }
      }
      switch (type) {
        case INVITATION:
          if (sendMail.send(msg, content, event.getAttachments()) == false) {
            failures++;
          }
          break;
        case UPDATE:
        case REJECTION:
          if (sendMail.send(msg, content, null) == false) {
            failures++;
          }
          break;
      }

    }
    return failures == 0 ? true : false;
  }

  private String composeHtmlContent(final TeamEventDO event, final Short number, final TeamEventMailType type, final Marker marker) {
    final StringBuffer buf = new StringBuffer();
    buf.append("<html><body><h2>").append(composeSubject(event, type)).append("</h2>");
    buf.append("<table>");
    buf.append(composeDate(event, type, marker));
    buf.append(composeRecurrence(event, number, type, marker));
    buf.append(composeLocation(event, type, marker));
    buf.append(composeAttendees(event, number, type));
    buf.append(composeModifier(event, type));
    buf.append("</table>");
    buf.append(composeButtons(event, number, type));
    buf.append("</body></html>");
    return buf.toString();
  }

  private String composeSubject(final TeamEventDO event, final TeamEventMailType type) {
    final StringBuffer buf = new StringBuffer();
    switch (type) {
      case INVITATION: {
        buf.append(PFUserContext.getUser().getFullname());
        buf.append(" ").append(I18n.getString("plugins.teamcal.event.invitation1"));
        buf.append(" „").append(event.getSubject()).append("“ ");
        buf.append(I18n.getString("plugins.teamcal.event.invitation2"));
        return buf.toString();
      }
      case UPDATE: {
        buf.append(" „").append(event.getSubject()).append("“ ");
        buf.append(I18n.getString("plugins.teamcal.event.update"));
        return buf.toString();
      }
      case REJECTION: {
        buf.append(" „").append(event.getSubject()).append("“ ");
        buf.append(I18n.getString("plugins.teamcal.event.rejection"));
        return buf.toString();
      }
      default:
        buf.append("Unsupported TeamEventMailType: ").append(type);
        log.error(buf.toString());
        return buf.toString();
    }
  }

  private String composeDate(final TeamEventDO event, final TeamEventMailType type, final Marker marker) {
    final StringBuffer buf = new StringBuffer();
    buf.append("<tr>");
    switch (type) {
      case UPDATE:
        if (marker.isDateChanged() == true) {
          buf.append("<td>").append(I18n.getString("plugins.teamcal.event.changed")).append("</td>");
          break;
        }
      case REJECTION:
      case INVITATION:
        buf.append("<td>").append(I18n.getString("plugins.teamcal.event.event")).append("</td>");
    }
    final Date d1 = new Date(event.getStartDate().getTime());
    final Date d2 = new Date(event.getEndDate().getTime());
    final SimpleDateFormat df1 = new SimpleDateFormat("EEEEE, dd. MMMM yyyy, HH:mm");
    final SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
    df1.setTimeZone(PFUserContext.getTimeZone());
    df2.setTimeZone(PFUserContext.getTimeZone());
    final String s = df1.format(d1) + " Uhr - " + df2.format(d2) + " Uhr";
    buf.append("<td>").append(s).append("</td>");
    buf.append("</tr>");
    return buf.toString();
  }

  private String composeRecurrence(final TeamEventDO event, final Short number, final TeamEventMailType type, final Marker marker) {
    final StringBuffer buf = new StringBuffer();
    if (StringUtils.isNotBlank(event.getRecurrenceRule()) == true) {
      buf.append("<tr>");
      switch (type) {
        case UPDATE:
          if (marker.isRecurrenceChanged() == true) {
            buf.append("<td>").append(I18n.getString("plugins.teamcal.event.recurrence.changed")).append("</td>");
          } else {
            buf.append("<td></td>");
          }
          buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Event/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("\">").append(I18n.getString("plugins.teamcal.event.recurrence.show")).append("</a></td>");
          break;
        case INVITATION:
          buf.append("<td></td>");
          buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Event/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("\">").append(I18n.getString("plugins.teamcal.event.recurrence.show")).append("</a></td>");
          break;
        case REJECTION:
          buf.append("<td></td>");
          buf.append("<td>").append(I18n.getString("plugins.teamcal.event.recurrence.show")).append("</td>");
          break;
      }
      buf.append("</tr>");
    }
    return buf.toString();
  }

  private String composeLocation(final TeamEventDO event, final TeamEventMailType type, final Marker marker) {
    final StringBuffer buf = new StringBuffer();
    if (StringUtils.isNotBlank(event.getLocation()) == true) {
      buf.append("<tr>");
      switch (type) {
        case INVITATION:
        case REJECTION:
          buf.append("<td>").append(I18n.getString("plugins.teamcal.event.location")).append("</td>");
          break;
        case UPDATE:
          if (marker.isLocationChanged() ==true) {
            buf.append("<td>").append(I18n.getString("plugins.teamcal.event.location.changed")).append("</td>");
          } else {
            buf.append("<td>").append(I18n.getString("plugins.teamcal.event.location")).append("</td>");
          }
      }
      buf.append("<td>").append(event.getLocation()).append("</td></tr>");
    }
    return buf.toString();
  }

  private String composeAttendees(final TeamEventDO event, final Short number, final TeamEventMailType type) {
    final StringBuffer buf = new StringBuffer();
    if (event.getAttendees() != null || event.getAttendees().isEmpty() == false) {
      buf.append("<tr>");
      buf.append("<td>").append(I18n.getString("plugins.teamcal.attendees")).append("</td>");
      buf.append("<td>").append(PFUserContext.getUser().getFullname()).append(" ").append(I18n.getString("plugins.teamcal.event.andyou")).append("</td>");
      buf.append("</tr>");
      switch (type) {
        case REJECTION:
          break;
        case INVITATION:
        case UPDATE:
          buf.append("<tr>");
          buf.append("<td></td>");
          buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Event/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("\">").append(I18n.getString("plugins.teamcal.event.showreplies")).append("</a></td>");
          buf.append("</tr>");
      }
    }
    return buf.toString();
  }

  private String composeModifier(final TeamEventDO event, final TeamEventMailType type) {
    final StringBuffer buf = new StringBuffer();
    switch (type) {
      case INVITATION:
        break;
      case UPDATE:
        buf.append("<tr>");
        buf.append("<td>").append(I18n.getString("plugins.teamcal.event.changedby")).append("</td>");
        buf.append("<td>").append(PFUserContext.getUser().getFullname()).append("</td>");
        buf.append("</tr>");
        break;
      case REJECTION:
        buf.append("<tr>");
        buf.append("<td>").append(I18n.getString("plugins.teamcal.event.deletedby")).append("</td>");
        buf.append("<td>").append(PFUserContext.getUser().getFullname()).append("</td>");
        buf.append("</tr>");
    }
    return buf.toString();
  }

  private String composeButtons(final TeamEventDO event, final Short number, final TeamEventMailType type) {
    final StringBuffer buf = new StringBuffer();
    buf.append("</br>");
    switch(type) {
      case REJECTION:
        break;
      case INVITATION:
      case UPDATE:
        buf.append("<table>");
        buf.append("<tr>");
        buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("&r=accept\">").append(I18n.getString("plugins.teamcal.event.accept")).append("</a></td>");
        buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("&r=decline\">").append(I18n.getString("plugins.teamcal.event.decline")).append("</a></td>");
        buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("&r=tentative\">").append(I18n.getString("plugins.teamcal.event.tentative")).append("</a></td>");
        buf.append("</tr>");
        buf.append("</table></br>");
    }
    return buf.toString();
  }

  public static String getICal(final TeamEventDO teamEvent, final TeamEventMailType type) {
    final StringBuffer buf = new StringBuffer();
    final Calendar calendar = new Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    final TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    final VTimeZone tz = registry.getTimeZone(PFUserContext.getTimeZone().getID()).getVTimeZone();
    calendar.getComponents().add(tz);
    switch (type) {
      case INVITATION:
      case UPDATE:
        calendar.getProperties().add(Method.REQUEST);
        break;
      case REJECTION:
        calendar.getProperties().add(Method.CANCEL);
    }
    final VEvent vEvent = ICal4JUtils.createVEvent(teamEvent.getStartDate(), teamEvent.getEndDate(), teamEvent.getUid(), teamEvent.getSubject(), teamEvent.isAllDay());
    vEvent.getProperties().add(new Sequence(teamEvent.getSequence()));
    if (teamEvent.hasRecurrence() == true) {
      vEvent.getProperties().add(new RRule(teamEvent.getRecurrenceObject()));
    }
    if (StringUtils.isNotBlank(teamEvent.getLocation()) == true) {
      vEvent.getProperties().add(new Location(teamEvent.getLocation()));
    }
    if (StringUtils.isNotBlank(teamEvent.getNote()) == true) {
      vEvent.getProperties().add(new Comment(teamEvent.getNote()));
    }
    final PFUserDO user = PFUserContext.getUser();
    String s = user.getFullname();
    if (user.getOrganization() != null) {
      s += "\n" + user.getOrganization();
    }
    if (user.getPersonalPhoneIdentifiers() != null) {
      s += "\n" + user.getPersonalPhoneIdentifiers();
    }
    vEvent.getProperties().add(new Contact(s));
    try {
      if (StringUtils.isNotBlank(user.getEmail()) == true) {
        final ParameterList organizerParams = new ParameterList();
        organizerParams.add(new Cn(user.getFullname()));
        final Organizer organizer = new Organizer(organizerParams, "mailto:" + user.getEmail());
        vEvent.getProperties().add(organizer);
      }
    } catch (final Exception e) {
      log.error("Cant't build organizer " + e.getMessage());
    }
    if (teamEvent.getAttendees() != null) {
      for (final TeamEventAttendeeDO attendee : teamEvent.getAttendees() ) {
        final ParameterList attendeeParams = new ParameterList();
        if (attendee.getUser() != null) {
          try {
            attendeeParams.add(new Cn(attendee.getUser().getFullname()));
            //            attendeeParams.add(new SentBy(attendee.getUser().getEmail()));
            attendeeParams.add(new PartStat(attendee.getStatus().name()));
            if (attendee.getStatus().equals(TeamAttendeeStatus.NEEDS_ACTION) == true) {
              attendeeParams.add(Role.REQ_PARTICIPANT);
              attendeeParams.add(Rsvp.TRUE);
            }
            vEvent.getProperties().add(new Attendee(attendeeParams, "mailto:" + attendee.getUser().getEmail()));
          } catch (final Exception e) {
            log.error("Cant't build attendee " + e.getMessage());
          }
        } else {
          try {
            //            attendeeParams.add(new SentBy(attendee.getUrl()));
            attendeeParams.add(new PartStat(attendee.getStatus().name()));
            if (attendee.getStatus().equals(TeamAttendeeStatus.NEEDS_ACTION) == true) {
              attendeeParams.add(Role.REQ_PARTICIPANT);
              attendeeParams.add(Rsvp.TRUE);
            }
            vEvent.getProperties().add(new Attendee(attendeeParams, "mailto:" + attendee.getUrl()));
          }  catch (final Exception e) {
            log.error("Cant't build attendee " + e.getMessage());
          }
        }
      }
    }
    calendar.getComponents().add(vEvent);
    buf.append(calendar.toString());
    return buf.toString();
  }

}
