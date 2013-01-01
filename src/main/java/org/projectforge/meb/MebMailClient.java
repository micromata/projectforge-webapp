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

package org.projectforge.meb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import javax.mail.Flags;
import javax.mail.MessagingException;

import org.projectforge.core.ConfigXml;
import org.projectforge.mail.Mail;
import org.projectforge.mail.MailAccount;
import org.projectforge.mail.MailAccountConfig;
import org.projectforge.mail.MailFilter;

/**
 * Gets the messages from a mail account and assigns them to the MEB user's inboxes.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MebMailClient
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MebMailClient.class);

  private MebDao mebDao;

  /**
   * 
   * @param onlyRecentMails If true then only unseen mail will be got from the mail server and afterwards they will be set as seen.
   * @return Number of new imported messages.
   */
  public synchronized int getNewMessages(final boolean onlyRecentMails, final boolean markRecentMailsAsSeen)
  {
    int counter = 0;
    final MailFilter filter = new MailFilter();
    if (onlyRecentMails == true) {
      filter.setOnlyRecent(true);
    }
    final MailAccountConfig cfg = ConfigXml.getInstance().getMebMailAccount();
    if (cfg == null || cfg.getHostname() == null) {
      // No mail account configured.
      return 0;
    }
    final MailAccount mailAccount = new MailAccount(cfg);
    try {
      // If mark messages as seen is set then open mbox read-write.
      mailAccount.connect("INBOX", markRecentMailsAsSeen);
      final Mail[] mails = mailAccount.getMails(filter);
      if (mails != null) {
        for (final Mail mail : mails) {
          final MebEntryDO entry = new MebEntryDO();
          entry.setDate(mail.getDate());
          final String content = mail.getContent();
          final BufferedReader reader = new BufferedReader(new StringReader(content.trim()));
          try {
            StringBuffer buf = null;
            while (reader.ready() == true) {
              final String line = reader.readLine();
              if (line == null) {
                break;
              }
              if (line.startsWith("date=") == true) {
                if (line.length() > 5) {
                  final String dateString = line.substring(5);
                  final Date date = MebDao.parseDate(dateString);
                  entry.setDate(date);
                }
              } else if (line.startsWith("sender=") == true) {
                if (line.length() > 7) {
                  final String sender = line.substring(7);
                  entry.setSender(sender);
                }
              } else if (line.startsWith("msg=") == true) {
                if (line.length() > 4) {
                  final String msg = line.substring(4);
                  buf = new StringBuffer();
                  buf.append(msg);
                }
              } else if (buf != null) {
                buf.append(line);
              } else {
                entry.setSender(line); // First row is the sender.
                buf = new StringBuffer(); // The message follows.
              }
            }
            if (buf != null) {
              entry.setMessage(buf.toString().trim());
            }
          } catch (IOException ex) {
            log.fatal("Exception encountered " + ex, ex);
          }
          if (mebDao.checkAndAddEntry(entry, "MAIL") == true) {
            counter++;
          }
          if (markRecentMailsAsSeen == true) {
            try {
              mail.getMessage().setFlag(Flags.Flag.SEEN, true);
              //mail.getMessage().saveChanges();
            } catch (MessagingException ex) {
              log.error("Exception encountered while setting message flag SEEN as true: " + ex, ex);
            }
          }
          // log.info(mail);
        }
      }
      return counter;
    } finally {
      mailAccount.disconnect();
    }
  }

  public void setMebDao(MebDao mebDao)
  {
    this.mebDao = mebDao;
  }
}
