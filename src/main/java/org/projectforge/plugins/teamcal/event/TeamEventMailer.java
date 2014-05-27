/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.projectforge.calendar.ICal4JUtils;
import org.projectforge.core.ConfigXml;
import org.projectforge.mail.Mail;
import org.projectforge.mail.SendMail;
import org.projectforge.registry.Registry;
import org.projectforge.scripting.I18n;
import org.projectforge.user.PFUserContext;

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
    private final Date lastEmail;
    private final HistoryEntry[] entries;
    private final List<HistoryEntry> list;
    public Marker(final HistoryEntry[] entries, final Date lastEmail) {
      this.entries = entries;
      this.lastEmail=lastEmail;
      list = new LinkedList<HistoryEntry>();
      for (int i=0; i < entries.length-1; i++) {
        if (entries[i].getTimestamp().after(lastEmail)) {
          list.add(entries[i]);
        }
      }
    }
    public boolean hasLocationChanged() {
      for (final HistoryEntry entry : list) {
        for (final PropertyDelta delta : entry.getDelta()) {
          if (StringUtils.contains(delta.getPropertyName(), "location") == true) {
            return true;
          }
        }
      }
      return false;
    }
    public boolean hasDateChanged() {
      for (final HistoryEntry entry : list) {
        for (final PropertyDelta delta : entry.getDelta()) {
          if (StringUtils.contains(delta.getPropertyName(), "startDate") == true ||
              StringUtils.contains(delta.getPropertyName(), "endDate") == true) {
            return true;
          }
        }
      }
      return false;
    }
    public boolean hasRecurrencyChanged() {
      for (final HistoryEntry entry : list) {
        for (final PropertyDelta delta : entry.getDelta()) {
          if (StringUtils.contains(delta.getPropertyName(), "recurrenceRule") == true) {
            return true;
          }
        }
      }
      return false;
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
      if (sendIcsFile(event, value.getType()) == false) {
        failures++;
      } else {
        // TODO: event.setLastEmail(now);
        final Timestamp t = new Timestamp(event.getLastUpdate().getTime());
        event.setLastEmail(t);
        teamEventDao.saveOrUpdate(event);
      }
    }
    return failures == 0 ? true : false;
  }

  private boolean sendIcsFile(final TeamEventDO event, final TeamEventMailType type) {
    final String workdir ="tmp/";
    final String[] attachmentfiles = new String[1];
    attachmentfiles[0] = event.getUid() + ".ics";
    final String content = ICal4JUtils.getICal(event);
    if (writeFile(content, workdir, attachmentfiles[0] ) == false) {
      log.error("Can't write attachmentfile: " + attachmentfiles[0]);
      return false;
    }
    final Mail msg = new Mail();
    msg.setProjectForgeSubject(composeSubject(event, type));

    msg.setContentType(Mail.CONTENTTYPE_HTML);
    int failures = 0;
    final SendMail sendMail = new SendMail();
    sendMail.setConfigXml(ConfigXml.getInstance());
    for (final TeamEventAttendeeDO attendee : event.getAttendees()) {
      msg.setContent(composeHtmlContent(event, attendee.getNumber(), type));
      if (attendee.getUserId() == null) {
        msg.setTo(attendee.getUrl());
      } else {
        msg.setTo(attendee.getUser());
        if (attendee.getUser().equals(PFUserContext.getUser()) == true) {
          continue;
        }
      }
      if (sendMail.send(msg, workdir, attachmentfiles) == false) {
        failures++;
      }
    }
    return failures == 0 ? true : false;
  }

  private boolean writeFile(final String content, final String workdir, final String file) {
    PrintWriter pWriter = null;
    boolean success = true;
    try {
      pWriter = new PrintWriter(new BufferedWriter(new FileWriter(workdir + file)));
      pWriter.println(content);
    } catch (final IOException ioe) {
      ioe.printStackTrace();
      success = false;
    } finally {
      if (pWriter != null){
        pWriter.flush();
        pWriter.close();
      }
    }
    return success;
  }

  private String composeHtmlContent(final TeamEventDO event, final Short number, final TeamEventMailType type) {

    final Marker marker = new Marker(teamEventDao.getHistoryEntries(event), event.getLastEmail());
    final StringBuffer buf = new StringBuffer();
    buf.append("<html><body><h2>").append(composeSubject(event, type)).append("</h2>");
    buf.append("<table>");
    buf.append(composeDate(event, type, marker));
    buf.append(composeRecurrence(event, number, type, marker));
    buf.append(composeLocation(event, type, marker));
    buf.append(composeAttendees(event, number, type));
    buf.append("</table>");
    buf.append(composeButtons(event, number, type));
    buf.append("</body></html>");
    final String s = buf.toString();
    return s;
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
        if (marker.hasDateChanged() == true) {
          buf.append("<td>").append(I18n.getString("plugins.teamcal.event.event.change")).append("</td>");
          break;
        }
      case REJECTION:
      case INVITATION:
        buf.append("<td>").append(I18n.getString("plugins.teamcal.event.event")).append("</td>");
    }
    buf.append("<td>").append(event.getStartDate()).append(" - ").append(event.getEndDate()).append("</td>");
    buf.append("</tr>");
    return buf.toString();
  }

  private String composeRecurrence(final TeamEventDO event, final Short number, final TeamEventMailType type, final Marker marker) {
    final StringBuffer buf = new StringBuffer();
    if (StringUtils.isNotBlank(event.getRecurrenceRule()) == true) {
      buf.append("<tr>");
      switch (type) {
        case UPDATE:
          if (marker.hasRecurrencyChanged() == true) {
            buf.append("<td>").append(I18n.getString("plugins.teamcal.event.event.changed")).append("</td>");
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
          if (marker.hasLocationChanged() ==true) {
            buf.append("<td>").append(I18n.getString("plugins.teamcal.event.location")).append(" changed").append("</td>");
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
      buf.append("<tr>");
      buf.append("<td></td>");
      buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Event/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("\">").append(I18n.getString("plugins.teamcal.event.showreplies")).append("</a></td>");
      buf.append("</tr>");
    }
    return buf.toString();
  }

  private String composeButtons(final TeamEventDO event, final Short number, final TeamEventMailType type) {
    final StringBuffer buf = new StringBuffer();
    buf.append("</br>");
    buf.append("<table>");
    buf.append("<tr>");
    buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("&r=accept\">").append(I18n.getString("plugins.teamcal.event.accept")).append("</a></td>");
    buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("&r=decline\">").append(I18n.getString("plugins.teamcal.event.decline")).append("</a></td>");
    buf.append("<td>").append("<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=").append(event.getUid()).append("&p=p").append(number.toString()).append("&r=tentative\">").append(I18n.getString("plugins.teamcal.event.tentative")).append("</a></td>");
    buf.append("</tr>");
    buf.append("</table></br>");
    return buf.toString();
  }

  //  private String composeContent(final TeamEventDO event, final Short number, final TeamEventMailType type) {
  //    if (type == TeamEventMailType.UPDATE) {
  //      return composeHtmlUpdateContent(event, number, type);
  //    }
  //    String s= "<html><body><h2>" + composeSubject(event, type) +"</h2>";
  //    s += "<table>";
  //    s += "<tr>";
  //    s += "<td>" + I18n.getString("plugins.teamcal.event.event") + "</td>";
  //    s += "<td>" +  event.getStartDate() + " - " + event.getEndDate() + "</td>";
  //    s += "</tr>";
  //
  //    if (StringUtils.isNotBlank(event.getLocation()) == true) {
  //      s += "<tr>";
  //      s += "<td>" + I18n.getString("plugins.teamcal.event.location") + "</td>";
  //      s += "<td>" + event.getLocation() + "</td>";
  //      s += "</tr>";
  //    }
  //    if (StringUtils.isNotBlank(event.getRecurrenceRule()) == true) {
  //      s += "<tr>";
  //      s += "<td></td>";
  //      s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Event/?e=" + event.getUid() + "&p=p" + number.toString() + "\">" + I18n.getString("plugins.teamcal.event.recurrence.show") + "</a></td>";
  //      s += "</tr>";
  //    }
  //    s += "<tr>";
  //    s += "<td>" + I18n.getString("plugins.teamcal.attendees") + "</td>";
  //    s += "<td>" + PFUserContext.getUser().getFullname() + " " + I18n.getString("plugins.teamcal.event.andyou") + "</td>";
  //    s += "</tr>";
  //    s += "<tr>";
  //    s += "<td></td>";
  //    s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Event/?e=" + event.getUid() + "&p=p" + number.toString() + "\">" + I18n.getString("plugins.teamcal.event.showreplies") + "</a></td>";
  //    s += "</tr>";
  //    s += "</table></br>";
  //    s += "<table>";
  //    s += "<tr>";
  //    s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=" + event.getUid() + "&p=p" + number.toString() + "&r=accept\">" + I18n.getString("plugins.teamcal.event.accept") + "</a></td>";
  //    s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=" + event.getUid() + "&p=p" + number.toString() + "&r=decline\">" + I18n.getString("plugins.teamcal.event.decline") + "</a></td>";
  //    s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=" + event.getUid() + "&p=p" + number.toString() + "&r=tentative\">" + I18n.getString("plugins.teamcal.event.tentative") + "</a></td>";
  //    s += "</tr>";
  //    s += "</table></br>";
  //    s += "</body>";
  //    s += "</html>";
  //    return s;
  //  }
  //
  //  private String composeHtmlUpdateContent(final TeamEventDO event, final Short number, final TeamEventMailType type) {
  //    final HistoryEntry[] entries = teamEventDao.getHistoryEntries(event);
  //
  //    class Marker {
  //      private final Date lastEmail;
  //      private final HistoryEntry[] entries;
  //      private final List<HistoryEntry> list;
  //      public Marker(final HistoryEntry[] entries, final Date lastEmail) {
  //        this.entries = entries;
  //        this.lastEmail=lastEmail;
  //        list = new LinkedList<HistoryEntry>();
  //        for (int i=0; i < entries.length-1; i++) {
  //          if (entries[i].getTimestamp().after(lastEmail)) {
  //            list.add(entries[i]);
  //          }
  //        }
  //      }
  //      public boolean hasLocationChanged() {
  //        for (final HistoryEntry entry : list) {
  //          for (final PropertyDelta delta : entry.getDelta()) {
  //            if (StringUtils.contains(delta.getPropertyName(), "location") == true) {
  //              return true;
  //            }
  //          }
  //        }
  //        return false;
  //      }
  //      public boolean hasDateChanged() {
  //        for (final HistoryEntry entry : list) {
  //          for (final PropertyDelta delta : entry.getDelta()) {
  //            if (StringUtils.contains(delta.getPropertyName(), "startDate") == true ||
  //                StringUtils.contains(delta.getPropertyName(), "endDate") == true) {
  //              return true;
  //            }
  //          }
  //        }
  //        return false;
  //      }
  //      public boolean hasRecurrencyChanged() {
  //        for (final HistoryEntry entry : list) {
  //          for (final PropertyDelta delta : entry.getDelta()) {
  //            if (StringUtils.contains(delta.getPropertyName(), "recurrenceRule") == true) {
  //              return true;
  //            }
  //          }
  //        }
  //        return false;
  //      }
  //    }
  //    /**
  //     * TODO
  //     * alle entries ermitteln, die nach event.getLastEmail() liegen
  //     */
  //    final Marker marker = new Marker(entries, event.getLastEmail());
  //    String s= "<html><body><h2>" + composeSubject(event, type) +"</h2>";
  //    s += "<table>";
  //    s += "<tr>";
  //    if (marker.hasDateChanged() == true) {
  //      s += "<td>Geändert.</td>";
  //    } else {
  //      s +="<td></td>";
  //    }
  //    s += "<td>" + I18n.getString("plugins.teamcal.event.event") + "</td>";
  //    s += "<td>" +  event.getStartDate() + " - " + event.getEndDate() + "</td>";
  //    s += "</tr>";
  //    if (StringUtils.isNotBlank(event.getLocation()) == true) {
  //      s += "<tr>";
  //      if (marker.hasLocationChanged() ==true) {
  //        s += "<td>Geändert.</td>";
  //      } else {
  //        s +="<td></td>";
  //      }
  //      s += "<td>" + I18n.getString("plugins.teamcal.event.location") + "</td>";
  //      s += "<td>" + event.getLocation() + "</td>";
  //      s += "</tr>";
  //    }
  //    s += "</table></br>";
  //    s += "</body>";
  //    s += "</html>";
  //    return s;
  //  }
}
