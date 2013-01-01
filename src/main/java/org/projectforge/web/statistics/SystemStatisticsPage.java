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

package org.projectforge.web.statistics;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.NumberFormatter;
import org.projectforge.database.HibernateUtils;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.springframework.jdbc.core.JdbcTemplate;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;

public class SystemStatisticsPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = 8587252641914110851L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SystemStatisticsPage.class);

  @SpringBean(name = "dataSource")
  private DataSource dataSource;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  public SystemStatisticsPage(final PageParameters parameters)
  {
    super(parameters);
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    body.add(new Label("totalNumberOfTimesheets", NumberFormatter.format(getTableCount(jdbc, TimesheetDO.class))));
    final long totalDuration = taskTree.getRootTaskNode().getDuration(taskTree, true);
    BigDecimal tatalPersonDays = new BigDecimal(totalDuration).divide(DateHelper.SECONDS_PER_WORKING_DAY, 2, BigDecimal.ROUND_HALF_UP);
    tatalPersonDays = NumberHelper.setDefaultScale(tatalPersonDays);
    body.add(new Label("totalNumberOfTimesheetDurations", NumberHelper.getNumberFractionFormat(getLocale(), tatalPersonDays.scale())
        .format(tatalPersonDays)));
    body.add(new Label("totalNumberOfUsers", NumberFormatter.format(getTableCount(jdbc, PFUserDO.class))));
    body.add(new Label("totalNumberOfTasks", NumberFormatter.format(getTableCount(jdbc, TaskDO.class))));
    final int totalNumberOfHistoryEntries = getTableCount(jdbc, HistoryEntry.class) + getTableCount(jdbc, PropertyDelta.class);
    body.add(new Label("totalNumberOfHistoryEntries", NumberFormatter.format(totalNumberOfHistoryEntries)));
  }

  private int getTableCount(final JdbcTemplate jdbc, final Class< ? > entity)
  {
    try {
      return jdbc.queryForInt("SELECT COUNT(*) FROM " + HibernateUtils.getDBTableName(entity));
    } catch (final Exception ex) {
      log.error(ex.getMessage(), ex);
      return 0;
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("system.statistics.title");
  }

}
