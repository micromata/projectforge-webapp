/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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
    return new UpdateEntryImpl(PollPlugin.ID, "1.0.0", "2012-05-09", "Adds tables T_PLUGIN_POLL_*.") {

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
          eventTable.addDefaultBaseDOAttributes().addAttributes("pollId", "startDate", "endDate");
          dao.createTable(eventTable);
        }
        if (dao.doesExist(attendeeTable) == false) {
          attendeeTable.addDefaultBaseDOAttributes().addAttributes("user", "email", "pollId", "secureKey");
          dao.createTable(attendeeTable);
        }
        if (dao.doesExist(resultTable) == false) {
          resultTable.addDefaultBaseDOAttributes().addAttributes("pollEventId", "pollAttendeeId", "result");
          dao.createTable(resultTable);
        }
        dao.createMissingIndices();
        return UpdateRunningStatus.DONE;
      }
    };
  }
}
