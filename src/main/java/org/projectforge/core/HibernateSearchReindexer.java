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

package org.projectforge.core;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.projectforge.common.DateHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.database.DatabaseDao;
import org.projectforge.mail.Mail;
import org.projectforge.mail.SendMail;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.micromata.hibernate.history.HistoryEntry;

public class HibernateSearchReindexer
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HibernateSearchReindexer.class);

  private static final String ERROR_MSG = "Error while re-indexing data base: found lock files while re-indexing data-base. "
      + "Try to run re-index manually in the web administration menu and if occured again, "
      + "shutdown ProjectForge, delete lock file(s) in hibernate-search sub directory and restart.";

  private Configuration configuration;

  private SendMail sendMail;

  private DatabaseDao databaseDao;

  private Date currentReindexRun = null;

  private HibernateTemplate hibernate;

  public void execute()
  {
    log.info("Re-index job started.");
    if (databaseDao == null) {
      log.error("Job not configured, aborting.");
      return;
    }
    final String result = rebuildDatabaseSearchIndices();
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

  public String rebuildDatabaseSearchIndices(final ReindexSettings settings, final Class< ? >... classes)
  {
    if (currentReindexRun != null) {
      final StringBuffer buf = new StringBuffer();
      if (classes != null && classes.length > 0) {
        boolean first = true;
        for (final Class< ? > cls : classes) {
          first = StringHelper.append(buf, first, cls.getName(), ", ");
        }
      }
      final String date = DateTimeFormatter.instance().getFormattedDateTime(currentReindexRun, Locale.ENGLISH, DateHelper.UTC);
      log.info("Re-indexing of '" + buf.toString() + "' cancelled due to another already running re-index job started at " + date + " (UTC):");
      return "Another re-index job is already running. The job was started at: " + date;
    }
    synchronized (this) {
      try {
        currentReindexRun = new Date();
        final StringBuffer buf = new StringBuffer();
        if (classes != null && classes.length > 0) {
          for (final Class< ? > cls : classes) {
            reindex(cls, settings, buf);
          }
        } else {
          // Re-index: HistoryEntry:
          reindex(HistoryEntry.class, settings, buf);
          // Re-index of all ProjectForge entities:
          for (final RegistryEntry entry : Registry.instance().getOrderedList()) {
            if (entry.getNestedDOClasses() != null) {
              for (final Class< ? > nestedDOClass : entry.getNestedDOClasses()) {
                reindex(nestedDOClass, settings, buf);
              }
            }
            reindex(entry.getDOClass(), settings, buf);
          }
        }
        return buf.toString();
      } finally {
        currentReindexRun = null;
      }
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked"})
  private void reindex(final Class< ? > clazz, final ReindexSettings settings, final StringBuffer buf)
  {
    // PF-378: Performance of run of full re-indexing the data-base is very slow for large data-bases
    // Single transactions needed, otherwise the full run will be very slow for large data-bases.
    final TransactionTemplate tx = new TransactionTemplate(new HibernateTransactionManager(hibernate.getSessionFactory()));
    tx.execute(new TransactionCallback() {
      // The call-back is needed, otherwise a lot of transactions are left open until last run is completed:
      public Object doInTransaction(final TransactionStatus status)
      {
        try {
          hibernate.execute(new HibernateCallback() {
            public Object doInHibernate(final Session session) throws HibernateException
            {
              databaseDao.reindex(clazz, settings, buf);
              status.setRollbackOnly();
              return null;
            }
          });
        } catch (final Exception ex) {
          buf.append(" (an error occured, see log file for further information.), ");
          log.error("While rebuilding data-base-search-index for '" + clazz.getName() + "': " + ex.getMessage(), ex);
        }
        return null;
      }
    });
  }

  public String rebuildDatabaseSearchIndices()
  {
    return rebuildDatabaseSearchIndices(new ReindexSettings());
  }

  public void setConfiguration(final Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setSendMail(final SendMail sendMail)
  {
    this.sendMail = sendMail;
  }

  public void setDatabaseDao(final DatabaseDao databaseDao)
  {
    this.databaseDao = databaseDao;
  }

  /**
   * @param hibernate the hibernate to set
   * @return this for chaining.
   */
  public void setHibernate(final HibernateTemplate hibernate)
  {
    this.hibernate = hibernate;
  }
}
