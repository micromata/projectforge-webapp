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

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job should be scheduled every 10 minutes.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MebPollingJob implements Job
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MebPollingJob.class);

  private MebJob mebJob;


  public void execute(final JobExecutionContext context) throws JobExecutionException
  {
    //log.info("MEB polling job started.");
    if (mebJob == null) {
      wire(context);
    }
    try {
      mebJob.execute(false);
    } catch (final Throwable ex) {
      log.error("While executing hibernate search re-index job: " + ex.getMessage(), ex);
    }
    //log.info("MEB polling job finished.");
  }

  private void wire(final JobExecutionContext context)
  {
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
