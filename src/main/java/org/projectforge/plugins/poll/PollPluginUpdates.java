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

package org.projectforge.plugins.poll;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdateEntryImpl;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;
import org.projectforge.plugins.poll.attendee.PollAttendeeDO;
import org.projectforge.plugins.poll.event.PollEventDO;
import org.projectforge.plugins.poll.result.PollResultDO;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollPluginUpdates
{
  static DatabaseUpdateDao dao;

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(PollPlugin.ID, "1.0.0", "2013-01-13", "Adds tables T_PLUGIN_POLL_*.") {

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        final Table pollTable = new Table(PollDO.class);
        final Table eventTable = new Table(PollEventDO.class);
        final Table attendeeTable = new Table(PollAttendeeDO.class);
        final Table resultTable = new Table(PollResultDO.class);
        // Does the data-base table already exist?
        return dao.doesExist(pollTable) == true
            && dao.doesExist(eventTable) == true
            && dao.doesExist(attendeeTable) == true
            && dao.doesExist(resultTable) == true ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        final Table pollTable = new Table(PollDO.class);
        final Table eventTable = new Table(PollEventDO.class);
        final Table attendeeTable = new Table(PollAttendeeDO.class);
        final Table resultTable = new Table(PollResultDO.class);
        // Create initial data-base table:
        if (dao.doesExist(pollTable) == false) {
          pollTable.addDefaultBaseDOAttributes().addAttributes("owner", "title", "location", "description", "active");
          dao.createTable(pollTable);
        }
        if (dao.doesExist(eventTable) == false) {
          eventTable.addDefaultBaseDOAttributes().addAttributes("poll", "startDate", "endDate");
          dao.createTable(eventTable);
        }
        if (dao.doesExist(attendeeTable) == false) {
          attendeeTable.addDefaultBaseDOAttributes().addAttributes("user", "email", "poll", "secureKey");
          dao.createTable(attendeeTable);
        }
        if (dao.doesExist(resultTable) == false) {
          resultTable.addDefaultBaseDOAttributes().addAttributes("pollEvent", "pollAttendee", "result");
          dao.createTable(resultTable);
        }
        dao.createMissingIndices();
        return UpdateRunningStatus.DONE;
      }
    };
  }
}
