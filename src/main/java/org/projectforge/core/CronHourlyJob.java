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
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job should be scheduled hourly.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class CronHourlyJob extends AbstractCronJob
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CronHourlyJob.class);

  private DatabaseUpdateDao databaseUpdateDao;

  public void execute(final JobExecutionContext context) throws JobExecutionException
  {
    log.info("Hourly job started.");
    if (databaseUpdateDao == null) {
      wire(context);
    }
    if (databaseUpdateDao == null) {
      log.fatal("Job not configured, aborting.");
      return;
    }
    try {
      final int numberOfFixedEntries = databaseUpdateDao.internalFixDBHistoryEntries();
      if (numberOfFixedEntries > 0) {
        log.info("Data-base-fix-history-entries job finished successfully: " + numberOfFixedEntries + " entries fixed.");
      }
    } catch (final Throwable ex) {
      log.error("While executing fix job for data base history entries: " + ex.getMessage(), ex);
    }
    log.info("Hourly job job finished.");
  }

  protected void wire(final JobExecutionContext context)
  {
    databaseUpdateDao = (DatabaseUpdateDao) wire(context, "databaseUpdateDao");
  }
}
