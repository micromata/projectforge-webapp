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

package org.projectforge.database;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.projectforge.access.AccessChecker;
import org.projectforge.continuousdb.SystemUpdater;
import org.projectforge.continuousdb.TableAttribute;
import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.continuousdb.UpdaterConfiguration;
import org.projectforge.continuousdb.hibernate.TableAttributeHookImpl;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MyDatabaseUpdater
{
  private UpdaterConfiguration configuration;

  private DataSource dataSource;

  private AccessChecker accessChecker;

  public MyDatabaseUpdater()
  {
  }

  private void initialize()
  {
    synchronized (this) {
      if (configuration != null) {
        return;
      }
      configuration = new UpdaterConfiguration();
      configuration.setDialect(HibernateUtils.getDialect()).setDataSource(dataSource);
      final MyDatabaseUpdateDao myDatabaseUpdateDao = new MyDatabaseUpdateDao(configuration);
      myDatabaseUpdateDao.setAccessChecker(accessChecker);
      configuration.setDatabaseUpdateDao(myDatabaseUpdateDao);
      TableAttribute.register(new TableAttributeHookImpl());

      final SortedSet<UpdateEntry> updateEntries = new TreeSet<UpdateEntry>();
      DatabaseCoreUpdates.dao = myDatabaseUpdateDao;
      updateEntries.addAll(DatabaseCoreUpdates.getUpdateEntries());
      getSystemUpdater().setUpdateEntries(updateEntries);
    }
  }

  public SystemUpdater getSystemUpdater()
  {
    initialize();
    return configuration.getSystemUpdater();
  }

  public MyDatabaseUpdateDao getDatabaseUpdateDao()
  {
    initialize();
    return (MyDatabaseUpdateDao) configuration.getDatabaseUpdateDao();
  }

  public void setAccessChecker(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  /**
   * @param dataSource the dataSource to set
   */
  public void setDataSource(final DataSource dataSource)
  {
    this.dataSource = dataSource;
  }
}
