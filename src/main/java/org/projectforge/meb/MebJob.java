/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import org.projectforge.core.Configuration;

/**
 * Can be called nightly for getting all MEB mails from the configured mail server for checking any missing MEB message. If no MEB mail
 * account is configured, the job does nothing.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MebJob
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MebJob.class);

  private MebMailClient mebMailClient;

  public void execute(final boolean importAllMails)
  {
    if (mebMailClient == null) {
      log.error("Job not configured, aborting.");
    }
    if (Configuration.getInstance().isMebMailAccountConfigured() == false) {
      return;
    }
    synchronized (mebMailClient) {
      log.info("MEB job started in '" + (importAllMails == true ? "read-all" : "read-recent") + "' mode.");
      int counter;
      if (importAllMails == true) {
        counter = mebMailClient.getNewMessages(false, false);
      } else {
        counter = mebMailClient.getNewMessages(true, true);
      }
      log.info("MEB job finished successfully, " + counter + " new messages imported.");
    }
  }

  public void setMebMailClient(MebMailClient mebMailClient)
  {
    this.mebMailClient = mebMailClient;
  }
}