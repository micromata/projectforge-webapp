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
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.projectforge.core.ConfigXml;
import org.projectforge.mail.Mail;
import org.projectforge.mail.SendMail;
import org.projectforge.registry.Registry;
import org.projectforge.scripting.I18n;
import org.projectforge.user.PFUserContext;

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

  public boolean send(final TeamEventDiff orgData) {
    int failures = 0;
    while (queue.isEmpty() == false) {
      final TeamEventMailValue value = queue.poll();
      final TeamEventDO event = teamEventDao.getById(value.getId());
      if (sendIcsFile(event, value.getType(), orgData) == false) {
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

  private boolean sendIcsFile(final TeamEventDO event, final TeamEventMailType type, final TeamEventDiff orgData) {
    int failures = 0;
    final String content = TeamEventUtils.getICalString(event, type);
    final Mail msg = new Mail();
    msg.setProjectForgeSubject(composeSubject(event, type));
    msg.setContentType(Mail.CONTENTTYPE_HTML);
    final SendMail sendMail = new SendMail();
    sendMail.setConfigXml(ConfigXml.getInstance());
    for (final TeamEventAttendeeDO attendee : event.getAttendees()) {
      msg.setContent(composeHtmlContent(event, attendee.getNumber(), type, orgData));
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

  private String composeHtmlContent(final TeamEventDO event, final Short number, final TeamEventMailType type, final TeamEventDiff orgData) {
    final StringBuffer buf = new StringBuffer();
    buf.append("<html><body><h2>").append(composeSubject(event, type)).append("</h2>");
    buf.append("<table>");
    buf.append(composeDate(event, type, orgData));
    buf.append(composeRecurrence(event, number, type, orgData));
    buf.append(composeLocation(event, type, orgData));
    buf.append(composeAttendees(event, number, type, orgData));
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

  private String composeDate(final TeamEventDO event, final TeamEventMailType type, final TeamEventDiff orgData) {
    final StringBuffer buf = new StringBuffer();
    buf.append("<tr>");
    switch (type) {
      case UPDATE:
        if (orgData.isDateChanged() == true) {
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

  private String composeRecurrence(final TeamEventDO event, final Short number, final TeamEventMailType type, final TeamEventDiff orgData) {
    final StringBuffer buf = new StringBuffer();
    if (StringUtils.isNotBlank(event.getRecurrenceRule()) == true) {
      buf.append("<tr>");
      switch (type) {
        case UPDATE:
          if (orgData.isRecurrenceChanged() == true) {
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

  private String composeLocation(final TeamEventDO event, final TeamEventMailType type, final TeamEventDiff orgData) {
    final StringBuffer buf = new StringBuffer();
    if (StringUtils.isNotBlank(event.getLocation()) == true) {
      buf.append("<tr>");
      switch (type) {
        case INVITATION:
        case REJECTION:
          buf.append("<td>").append(I18n.getString("plugins.teamcal.event.location")).append("</td>");
          break;
        case UPDATE:
          if (orgData.isLocationChanged() ==true) {
            buf.append("<td>").append(I18n.getString("plugins.teamcal.event.location.changed")).append("</td>");
          } else {
            buf.append("<td>").append(I18n.getString("plugins.teamcal.event.location")).append("</td>");
          }
      }
      buf.append("<td>").append(event.getLocation()).append("</td></tr>");
    }
    return buf.toString();
  }

  private String composeAttendees(final TeamEventDO event, final Short number, final TeamEventMailType type, final TeamEventDiff orgData) {
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
          if (orgData.isStatusChanged() == true) {
            for (final TeamEventAttendeeDO attendee: event.getAttendees()) {
              buf.append("<tr>");
              buf.append("<td></td>");
              if (attendee.getUserId() != null) {
                buf.append("<td>").append(attendee.getUser().getFullname());
              } else {
                buf.append("<td>").append(attendee.getUrl());
              }
              buf.append(" ").append(I18n.getString(attendee.getStatus().getI18nKey()));
              buf.append("</td>");
              buf.append("</tr>");
            }
          }
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
}
