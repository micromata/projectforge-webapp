/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.plugins.chimney;

import org.projectforge.continuousdb.DatabaseUpdateDao;
import org.projectforge.continuousdb.Table;
import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.continuousdb.UpdateEntryImpl;
import org.projectforge.continuousdb.UpdatePreCheckStatus;
import org.projectforge.continuousdb.UpdateRunningStatus;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

/**
 * Responsible for checking update status of the chimney plugin.
 * @see ChimneyPlugin
 * @author Sweeps <pf@byte-storm.com>
 * 
 */
public class ChimneyPluginUpdates
{
  static DatabaseUpdateDao dao;

  /**
   * The UpdateEntry return by this method can not perform any update. Instead it just gives the hint, that schemaUpdate must be set to
   * true, so hibernate can update the used tables, indexes, ... accordingly.
   */
  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(ChimneyPlugin.ID, "1.0.0", "2012-09-24",
        "Please set schemaUpdate to true or implement ChimneyPluginUpdates.getInitializationUpdateEntry() accordingly.") {

      @Override
      public UpdateRunningStatus runUpdate()
      {
        return UpdateRunningStatus.FAILED;
      }

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        final Class< ? >[] persistentEntities = { AbstractWbsNodeDO.class, WbsActivityDO.class, DependencyRelationDO.class,
            ResourceAssignmentDO.class};
        boolean ok = true;
        for (final Class< ? > clazz : persistentEntities) {
          final Table table = new Table(clazz);
          if (dao.doExist(table) == false) {
            ok = false;
          }
        }
        return ok ? UpdatePreCheckStatus.ALREADY_UPDATED : UpdatePreCheckStatus.FAILED;
      }
    };
  }
}
