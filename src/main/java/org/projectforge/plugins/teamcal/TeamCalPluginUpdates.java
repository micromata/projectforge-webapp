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

package org.projectforge.plugins.teamcal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdateEntryImpl;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.SchemaGenerator;
import org.projectforge.database.Table;
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
  private static final String VERSION_5_1 = "5.1";

  private static final String CURRENT_VERSION = VERSION_5_1;

  static DatabaseUpdateDao dao;

  final static Class< ? >[] doClasses = new Class< ? >[] { //
    TeamCalDO.class, TeamEventDO.class, TeamEventAttendeeDO.class};

  @SuppressWarnings("serial")
  public static List<UpdateEntry> getUpdateEntries()
  {
    final List<UpdateEntry> list = new ArrayList<UpdateEntry>();
    // /////////////////////////////////////////////////////////////////
    // 5.1
    // /////////////////////////////////////////////////////////////////
    list.add(new UpdateEntryImpl(TeamCalPlugin.ID, VERSION_5_1, "2013-04-25", "Increase length of T_PLUGIN_CALENDAR_EVENT.NOTE (255-4000)") {
      final Table eventTable = new Table(TeamEventDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        // Does the data-base table already exist?
        if (dao.isVersionUpdated(TeamCalPlugin.ID, CURRENT_VERSION) == true) {
          return UpdatePreCheckStatus.ALREADY_UPDATED;
        } else {
          return UpdatePreCheckStatus.READY_FOR_UPDATE;
        }
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        if (dao.isVersionUpdated(TeamCalPlugin.ID, CURRENT_VERSION) == false) {
          dao.alterTableColumnVarCharLength(eventTable.getName(), "note", 4000);
        }
        return UpdateRunningStatus.DONE;
      }
    });
    return list;
  }

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(TeamCalPlugin.ID, CURRENT_VERSION, "2013-04-25",
        "Adds tables T_PLUGIN_CALENDAR_* and parameter CALENDAR_DOMAIN.") {

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        // Does the data-base table already exist?
        if (dao.doesEntitiesExist(doClasses) == true && Configuration.getInstance().isCalendarDomainValid() == true) {
          return UpdatePreCheckStatus.ALREADY_UPDATED;
        } else {
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
       * @see org.projectforge.admin.UpdateEntry#afterUpdate()
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
    };
  }
}
