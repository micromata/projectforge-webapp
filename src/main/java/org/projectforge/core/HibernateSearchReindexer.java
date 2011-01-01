/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.core;

import org.apache.commons.lang.StringUtils;
import org.projectforge.database.DatabaseDao;
import org.projectforge.mail.Mail;
import org.projectforge.mail.SendMail;

public class HibernateSearchReindexer
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HibernateSearchReindexer.class);

  private static final String ERROR_MSG = "Error while re-indexing data base: found lock files while re-indexing data-base. "
      + "Try to run re-index manually in the web administration menu and if occured again, "
      + "shutdown ProjectForge, delete lock file(s) in hibernate-search sub directory and restart.";

  private Configuration configuration;

  private SendMail sendMail;

  private DatabaseDao databaseDao;

  public void execute()
  {
    log.info("Re-index job started.");
    if (databaseDao == null) {
      log.error("Job not configured, aborting.");
      return;
    }
    final String result = databaseDao.rebuildDatabaseSearchIndices();
    if (result.contains("*") == true) {
      log.fatal(ERROR_MSG);
      final String recipients = configuration.getStringValue(ConfigurationParam.SYSTEM_ADMIN_E_MAIL);
      if (StringUtils.isNotBlank(recipients) == true) {
        log.info("Try to inform administrator about re-indexing error.");
        final Mail msg = new Mail();
        msg.setTo(recipients);
        msg.setProjectForgeSubject("Error while re-indexing ProjectForge data-base.");
        msg.setContent(ERROR_MSG + "\n\nResult:\n" + result);
        msg.setContentType(Mail.CONTENTTYPE_TEXT);
        sendMail.send(msg);
      }
    }
    log.info("Re-index job finished successfully.");
  }

  public void setConfiguration(Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setSendMail(SendMail sendMail)
  {
    this.sendMail = sendMail;
  }

  public void setDatabaseDao(DatabaseDao databaseDao)
  {
    this.databaseDao = databaseDao;
  }
}
