/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.mail;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.InternalErrorException;
import org.projectforge.core.UserException;
import org.projectforge.scripting.GroovyEngine;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.HtmlHelper;

/**
 * Helper class for creating and transporting E-Mails. Groovy script is use-able for e-mail template mechanism.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SendMail
{
  public static final String STANDARD_SUBJECT_PREFIX = "[ProjectForge] ";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SendMail.class);

  private SendMailConfig sendMailConfig;

  private Properties properties;

  /**
   * Get the ProjectForge standard subject: "[ProjectForge] ..."
   * @param subject
   */
  public static String getProjectForgeSubject(String subject)
  {
    return STANDARD_SUBJECT_PREFIX + subject;
  }

  /**
   * @param composedMessage
   * @return true for successful sending, otherwise an exception will be thrown.
   * @throws UserException if to address is not given.
   * @throws InternalErrorException due to technical failures.
   */
  public boolean send(final Mail composedMessage)
  {
    final String to = composedMessage.getTo();
    if (to == null || to.trim().length() == 0) {
      log.error("No to address given. Sending of mail cancelled: " + composedMessage.toString());
      throw new UserException("mail.error.missingToAddress");
    }
    if (StringUtils.isBlank(sendMailConfig.getHost()) == true) {
      log.error("No e-mail host configured. E-Mail not sent: " + composedMessage.toString());
      return false;
    }
    log.info("Try to send email to " + to);
    // Get a Session object

    if (properties == null) {
      properties = new Properties();
      String protocol = sendMailConfig.getProtocol();
      properties.put("mail.from", sendMailConfig.getFrom());
      properties.put("mail.mime.charset", "UTF-8");
      properties.put("mail.transport.protocol", sendMailConfig.getProtocol());
      properties.put("mail." + protocol + ".host", sendMailConfig.getHost());
      properties.put("mail." + protocol + ".port", sendMailConfig.getPort());
      if (BooleanUtils.isTrue(sendMailConfig.getDebug()) == true) {
        properties.put("mail.debug", "true");
      }
    }
    new Thread() {
      @Override
      public void run()
      {
       sendIt(composedMessage);
      }
    }.start();
    return true;
  }
  
  private void sendIt(final Mail composedMessage) {
    final Session session = Session.getInstance(properties);
    Transport transport = null;
    try {
      MimeMessage message = new MimeMessage(session);
      if (composedMessage.getFrom() != null) {
        message.setFrom(new InternetAddress(composedMessage.getFrom()));
      } else {
        message.setFrom();
      }
      message.setRecipients(Message.RecipientType.TO, composedMessage.getTo());
      String subject;
      subject = composedMessage.getSubject();
      /*
       * try { subject = MimeUtility.encodeText(composedMessage.getSubject()); } catch (UnsupportedEncodingException ex) {
       * log.error("Exception encountered while encoding subject." + ex, ex); subject = composedMessage.getSubject(); }
       */
      message.setSubject(subject, sendMailConfig.getCharset());
      message.setSentDate(new Date());
      if (composedMessage.getContentType() != null) {
        message.setText(composedMessage.getContent(), composedMessage.getCharset(), composedMessage.getContentType());
      } else {
        message.setText(composedMessage.getContent(), sendMailConfig.getCharset());
      }
      message.saveChanges(); // don't forget this
      transport = session.getTransport();
      if (StringUtils.isNotEmpty(sendMailConfig.getUser()) == true) {
        transport.connect(sendMailConfig.getUser(), sendMailConfig.getPassword());
      } else {
        transport.connect();
      }
      transport.sendMessage(message, message.getAllRecipients());
    } catch (MessagingException ex) {
      log.error("While creating and sending message: " + composedMessage.toString(), ex);
      throw new InternalErrorException("mail.error.exception");
    } finally {
      if (transport != null) {
        try {
          transport.close();
        } catch (MessagingException ex) {
          log.error("While creating and sending message: " + composedMessage.toString(), ex);
          throw new InternalErrorException("mail.error.exception");
        }
      }
    }
    log.info("E-Mail successfully sent: " + composedMessage.toString());
  }

  /**
   * @param composedMessage
   * @param groovyTemplate
   * @param data
   * @param locale
   * @see GroovyEngine#executeTemplateFile(String)
   */
  public String renderGroovyTemplate(final Mail composedMessage, final String groovyTemplate, final Map<String, Object> data,
      final PFUserDO recipient)
  {
    final PFUserDO user = PFUserContext.getUser();
    data.put("createdLabel", PFUserContext.getLocalizedString("created"));
    data.put("loggedInUser", user);
    data.put("recipient", recipient);
    data.put("msg", composedMessage);
    log.debug("groovyTemplate=" + groovyTemplate);
    final GroovyEngine engine = new GroovyEngine(data, recipient.getLocale(), recipient.getTimeZoneObject());
    final String result = engine.executeTemplateFile(groovyTemplate);
    return result;
  }

  /**
   * Replaces new lines by &lt;br/&gt; and escapes html characters.
   * @param str
   * @see HtmlHelper#formatText(String, boolean)
   */
  public String formatHtml(final String str)
  {
    return HtmlHelper.formatText(str, true);
  }

  public void setConfigXml(final ConfigXml configXml)
  {
    this.sendMailConfig = configXml.getSendMailConfiguration();
  }
}
