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

import org.projectforge.calendar.ICal4JUtils;
import org.projectforge.core.ConfigXml;
import org.projectforge.mail.Mail;
import org.projectforge.mail.SendMail;
import org.projectforge.scripting.I18n;
import org.projectforge.user.PFUserContext;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class TeamEventMailer
{

  public final static String INVITATION = "invitation";
  public final static String UPDATE     = "update";
  public final static String REJECTION  = "rejection";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventMailer.class);

  public TeamEventMailer() {
  }

  public boolean send(final TeamEventDO event, final TeamEventDO orgEvent, final String mode) {
    if (mode.equals(INVITATION)) {
      return sendIcsFile(event);
    } else {
      log.error("Unsupported mode: " + mode + ". No email sent.");
      return false;
    }
  }

  private boolean sendIcsFile(final TeamEventDO event) {
    final String workdir ="tmp/";
    final String[] attachmentfiles = new String[1];
    attachmentfiles[0] = event.getUid() + ".ics";
    final String content = ICal4JUtils.getICal(event);
    if (writeFile(content, workdir, attachmentfiles[0] ) == false) {
      log.error("Can't write attachmentfile: " + attachmentfiles[0]);
      return false;
    }
    final Mail msg = new Mail();
    msg.setProjectForgeSubject(composeSubject(event));

    msg.setContentType(Mail.CONTENTTYPE_HTML);
    int failures = 0;
    final SendMail sendMail = new SendMail();
    sendMail.setConfigXml(ConfigXml.getInstance());
    for (final TeamEventAttendeeDO attendee : event.getAttendees()) {
      msg.setContent(composeHtmlContent(event, attendee.getId()));
      if (attendee.getUserId() == null) {
        msg.setTo(attendee.getUrl());
      } else {
        msg.setTo(attendee.getUser());
      }
      if (sendMail.send(msg, workdir, attachmentfiles) == false) {
        failures++;
      };
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

  private String composeSubject(final TeamEventDO event) {
    String s = PFUserContext.getUser().getFullname();
    s += " " + I18n.getString("plugins.teamcal.event.subject1");
    s += " „" + event.getSubject() + "“ ";
    s += I18n.getString("plugins.teamcal.event.subject2");
    return s;
  }

  private String composeHtmlContent(final TeamEventDO event, final Integer number) {
    String s= "<html><body><h2>" + composeSubject(event) +"</h2>";
    s += "<table>";
    s += "<tr>";
    s += "<td>" + I18n.getString("plugins.teamcal.event.event") + "</td>";
    s += "<td>" +  event.getStartDate() + " - " + event.getEndDate() + "</td>";
    s += "</tr>";
    s += "<tr>";
    s += "<td>" + I18n.getString("plugins.teamcal.event.location") + "</td>";
    s += "<td>" + event.getLocation() + "</td>";
    s += "</tr>";
    s += "<tr>";
    s += "<td>" + I18n.getString("plugins.teamcal.attendees") + "</td>";
    s += "<td>" + PFUserContext.getUser().getFullname() + " " + I18n.getString("plugins.teamcal.event.andyou") + "</td>";
    s += "</tr>";
    s += "<tr>";
    s += "<td></td>";
    s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Event/?e=" + event.getUid() + "&p=p" + number.toString() + "\">" + I18n.getString("plugins.teamcal.event.showreplies") + "</a></td>";
    s += "</tr>";
    s += "</table></br>";
    s += "<table>";
    s += "<tr>";
    s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=" + event.getUid() + "&p=p" + number.toString() + "&r=accept\">" + I18n.getString("plugins.teamcal.event.accept") + "</a></td>";
    s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=" + event.getUid() + "&p=p" + number.toString() + "&r=decline\">" + I18n.getString("plugins.teamcal.event.decline") + "</a></td>";
    s += "<td>" + "<a href=\"http://localhost:8080/ProjectForge/Calendar/Eventreply/?e=" + event.getUid() + "&p=p" + number.toString() + "&r=tentative\">" + I18n.getString("plugins.teamcal.event.tentative") + "</a></td>";
    s += "</tr>";
    s += "</table></br>";
    s += "</body>";
    s += "</html>";
    return s;
  }
}
