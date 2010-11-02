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

package org.projectforge.core;

import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.meb.MebJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job should be scheduled nightly.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class CronNightlyJob implements Job
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CronNightlyJob.class);

  private HibernateSearchReindexJob hibernateSearchReindexJob;

  private MebJob mebJob;
  
  private DatabaseUpdateDao databaseUpdateDao; 

  public void execute(final JobExecutionContext context) throws JobExecutionException
  {
    log.info("Nightly job started.");
    if (hibernateSearchReindexJob == null || mebJob == null) {
      wire(context);
    }
    try {
      hibernateSearchReindexJob.execute();
    } catch (final Throwable ex) {
      log.error("While executing hibernate search re-index job: " + ex.getMessage(), ex);
    }
    try {
      databaseUpdateDao.fixDBHistoryEntries();
    } catch (final Throwable ex) {
      log.error("While executing fix job for data base history entries: " + ex.getMessage(), ex);
    }
    try {
      mebJob.execute(true);
    } catch (final Throwable ex) {
      log.error("While executing MEB job: " + ex.getMessage(), ex);
    }
    log.info("Nightly job job finished.");
  }

  private void wire(final JobExecutionContext context)
  {
    hibernateSearchReindexJob = (HibernateSearchReindexJob) wire(context, "hibernateSearchReindexJob");
    databaseUpdateDao = (DatabaseUpdateDao) wire(context, "databaseUpdateDao");
    mebJob = (MebJob) wire(context, "mebJob");
  }

  private Object wire(final JobExecutionContext context, final String key)
  {
    final Object result = context.getMergedJobDataMap().get(key);
    if (result == null) {
      log.error("Mis-configuration of scheduler in applicationContext-web.xml: '" + key + "' is not availabe.");
    }
    return result;
  }
}
