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

package org.projectforge.plugins.teamcal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.continuousdb.SchemaGenerator;
import org.projectforge.continuousdb.Table;
import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.continuousdb.UpdateEntryImpl;
import org.projectforge.continuousdb.UpdatePreCheckStatus;
import org.projectforge.continuousdb.UpdateRunningStatus;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.database.MyDatabaseUpdateDao;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.event.TeamEventAttendeeDO;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.registry.Registry;
import org.projectforge.web.admin.SystemUpdatePage;
import org.projectforge.web.core.ConfigurationEditPage;
import org.projectforge.web.wicket.AbstractEditPage;

/**
 * Contains the initial data-base set-up script and later all update scripts if any data-base schema updates are required by any later
 * release of this to-do plugin.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TeamCalPluginUpdates
{
  static MyDatabaseUpdateDao dao;

  final static Class< ? >[] doClasses = new Class< ? >[] { //
    TeamCalDO.class, TeamEventDO.class, TeamEventAttendeeDO.class};

  final static String[] newAttributes51 = { "externalSubscription", "externalSubscriptionCalendarBinary", "externalSubscriptionHash",
    "externalSubscriptionUrl", "externalSubscriptionUpdateInterval"};

  @SuppressWarnings("serial")
  public static List<UpdateEntry> getUpdateEntries()
  {
    final List<UpdateEntry> list = new ArrayList<UpdateEntry>();
    // /////////////////////////////////////////////////////////////////
    // 5.1
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(
        TeamCalPlugin.ID,
        "5.1",
        "2013-04-25",
        "Increase length of T_PLUGIN_CALENDAR_EVENT.NOTE (255-4000), re-create T_PLUGIN_CALENDAR_EVENT_ATTENDEE, add external subscription features (T_EVENT.abo*).") {
      final Table eventTable = new Table(TeamEventDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        // Does the data-base table already exist?
        if (dao.doTableAttributesExist(TeamEventAttendeeDO.class, "commentOfAttendee") == true
            && dao.doTableAttributesExist(TeamCalDO.class, newAttributes51) == true) {
          return UpdatePreCheckStatus.ALREADY_UPDATED;
        } else {
          return UpdatePreCheckStatus.READY_FOR_UPDATE;
        }
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.doTableAttributesExist(TeamEventAttendeeDO.class, "commentOfAttendee") == false) {
          dao.alterTableColumnVarCharLength(eventTable.getName(), "note", 4000);
          dao.dropTable(new Table(TeamEventAttendeeDO.class).getName()); // Table wasn't in use yet.
          // TeamEventDO is only needed for generating OneToMany relation with attendee table:
          new SchemaGenerator(dao).add(TeamEventDO.class, TeamEventAttendeeDO.class).createSchema();
        }
        if (dao.doTableAttributesExist(TeamCalDO.class, newAttributes51) == false) {
          dao.addTableAttributes(TeamCalDO.class, newAttributes51);
        }
        return UpdateRunningStatus.DONE;
      }
    });
    return list;
  }

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(TeamCalPlugin.ID, "2013-04-25", "Adds tables T_PLUGIN_CALENDAR_* and parameter CALENDAR_DOMAIN.") {

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        // Does the data-base table already exist?
        // Check only the oldest table.
        if (dao.doEntitiesExist(TeamCalDO.class) == true) {
          return UpdatePreCheckStatus.ALREADY_UPDATED;
        } else {
          // The oldest table doesn't exist, therefore the plug-in has to initialized completely.
          return UpdatePreCheckStatus.READY_FOR_UPDATE;
        }
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        new SchemaGenerator(dao).add(doClasses).createSchema();
        dao.createMissingIndices();
        return UpdateRunningStatus.DONE;
      }

      /**
       * @see org.projectforge.continuousdb.UpdateEntry#afterUpdate()
       */
      @Override
      public void afterUpdate()
      {
        if (Configuration.getInstance().isCalendarDomainValid() == false) {
          // Force to edit configuration value 'calendar domain'.
          final ConfigurationDao configurationDao = Registry.instance().getDao(ConfigurationDao.class);
          configurationDao.checkAndUpdateDatabaseEntries();
          final ConfigurationDO configurationDO = configurationDao.getEntry(ConfigurationParam.CALENDAR_DOMAIN);
          final ConfigurationEditPage configurationEditPage = new ConfigurationEditPage(new PageParameters().add(
              AbstractEditPage.PARAMETER_KEY_ID, configurationDO.getId()));
          configurationEditPage.setReturnToPage(new SystemUpdatePage(new PageParameters()));
          throw new RestartResponseException(configurationEditPage);
        }
      }

      /**
       * @see org.projectforge.continuousdb.UpdateEntry#createMissingIndices()
       */
      @Override
      public int createMissingIndices()
      {
        int result = 0;
        if (dao.createIndex("idx_plugin_team_cal_time", "t_plugin_calendar_event", "calendar_fk, start_date, end_date") == true) {
          ++result;
        }
        if (dao.createIndex("idx_plugin_team_cal_start_date", "t_plugin_calendar_event", "calendar_fk, start_date") == true) {
          ++result;
        }
        if (dao.createIndex("idx_plugin_team_cal_end_date", "t_plugin_calendar_event", "calendar_fk, end_date") == true) {
          ++result;
        }
        return result;
      }
    };
  }
}
