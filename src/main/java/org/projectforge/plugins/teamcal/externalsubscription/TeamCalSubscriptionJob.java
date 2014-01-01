/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.externalsubscription;

import org.projectforge.core.AbstractCronJob;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class TeamCalSubscriptionJob extends AbstractCronJob
{

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalSubscriptionJob.class);

  private static TeamCalDao teamCalDao;

  public void execute(final JobExecutionContext context) throws JobExecutionException
  {
    if (teamCalDao != null) {
      TeamEventExternalSubscriptionCache.instance().updateCache(teamCalDao);
    } else {
      log.error("TeamCalSubscriptionJob has no TeamCalDao set -> unable to update cache.");
    }
  }

  @Override
  protected void wire(final JobExecutionContext context)
  {
    // nothing to do here
  }

  public static void setTeamCalDao(final TeamCalDao teamCalDao)
  {
    TeamCalSubscriptionJob.teamCalDao = teamCalDao;
  }
}
