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

package org.projectforge.plugins.banking;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdateEntryImpl;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.SchemaGenerator;

/**
 * Contains the initial data-base set-up script and later all update scripts if any data-base schema updates are required by any later
 * release of this to-do plugin. <br/>
 * This is a part of the convenient auto update functionality of ProjectForge. You only have to insert update methods here for any further
 * release (with e. g. required data-base modifications). ProjectForge will do the rest.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class BankingPluginUpdates
{
  static DatabaseUpdateDao dao;

  final static Class< ? >[] doClasses = new Class< ? >[] { //
    BankAccountDO.class, BankAccountBalanceDO.class, BankAccountRecordDO.class};

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(BankingPlugin.BANK_ACCOUNT_ID, "1.0.0", "2012-01-21", "Adds tables T_PLUGIN_BANK_ACCOUNT_*.") {
      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        // Does the data-base table already exist?
        return dao.doesEntitiesExist(doClasses) == true ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.READY_FOR_UPDATE;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        // Create initial data-base table:
        new SchemaGenerator(dao).add(doClasses).createSchema();
        dao.createMissingIndices();
        return UpdateRunningStatus.DONE;
      }
    };
  }
}
