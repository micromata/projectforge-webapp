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

package org.projectforge.plugins.skillmatrix;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdateEntryImpl;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;

/**
 * Contains the initial data-base set-up script and later all update scripts if any data-base schema updates are required by any later
 * release of this skillmatrix plugin.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class SkillMatrixPluginUpdates
{
  static DatabaseUpdateDao dao;

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(SkillMatrixPlugin.ID_SKILL_RATING, "1.0.0", "2011-05-27", "Adds tables T_PLUGIN_SKILL and T_PLUGIN_SKILL_RATING.") {
      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        final Table skillTable = new Table(SkillDO.class);
        final Table skillRatingTable = new Table(SkillRatingDO.class);
        // Does the data-base table already exist?
        return dao.doesExist(skillTable) == true && dao.doesExist(skillRatingTable) == true ? UpdatePreCheckStatus.ALREADY_UPDATED
            : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        // Create initial data-base tables:
        final Table skillTable = new Table(SkillDO.class) //
        .addAttributes("id", "created", "lastUpdate", "deleted", "comment", "description");
        dao.createTable(skillTable);
        final Table skillRatingTable = new Table(SkillRatingDO.class) //
        .addAttributes("id", "created", "lastUpdate", "deleted", "comment", "description");
        dao.createTable(skillRatingTable);
        dao.createMissingIndices();
        return UpdateRunningStatus.DONE;
      }
    };
  }
}
