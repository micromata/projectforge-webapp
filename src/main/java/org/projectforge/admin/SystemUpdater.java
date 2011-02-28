/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.admin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.projectforge.core.UserException;
import org.projectforge.database.DatabaseUpdateDO;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;
import org.projectforge.scripting.GroovyExecutor;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.user.PFUserContext;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlObjectReader;

/**
 * Checks whether the data-base is up-to-date or not.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class SystemUpdater
{
  public static final String CORE_REGION_ID = "ProjectForge";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SystemUpdater.class);

  DatabaseUpdateDao databaseUpdateDao;

  // Used for update scripts
  private GroovyExecutor groovyExecutor;

  private SortedSet<UpdateEntry> updateEntries;

  private static SystemUpdater instance;

  static SystemUpdater instance()
  {
    return instance;
  }

  public void register(final UpdateEntry... updateEntries)
  {
    if (updateEntries == null) {
      return;
    }
    for (final UpdateEntry updateEntry : updateEntries) {
      getUpdateEntries().add(updateEntry);
    }
  }

  public void register(final Collection<UpdateEntry> updateEntries)
  {
    if (updateEntries == null) {
      return;
    }
    getUpdateEntries().addAll(updateEntries);
  }

  /**
   * Reads the update-scripts.xml file.
   */
  public void readUpdateFile()
  {
    final String filename = "updates/update-scripts.xml";
    final ClassLoader classLoader = this.getClass().getClassLoader();
    final InputStream is = classLoader.getResourceAsStream(filename);
    readUpdateFile(is);
  }

  /**
   * Reads the update script file from the given input stream.
   * @param is
   */
  @SuppressWarnings("unchecked")
  public void readUpdateFile(final InputStream is)
  {
    instance = this; // Used by scripts.
    final XmlObjectReader reader = new XmlObjectReader();
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(TreeSet.class, "projectforge-self-update");
    reader.setAliasMap(aliasMap);
    reader.initialize(UpdateEntryScript.class);
    String xml = null;
    try {
      xml = IOUtils.toString(is);
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new UserException("Unsupported update script format (see log files for details).");
    }
    updateEntries = (SortedSet<UpdateEntry>) reader.read(xml); // Read all scripts from xml.
    updateEntries.addAll(DatabaseCoreUpdates.getUpdateEntries());
  }

  /**
   * Runs the pre-check test of the first update entry in the list.
   * @return true if ALREADY_UPDATED, otherwise false.
   */
  public boolean isUpdated()
  {
    log.info("Checking for data-base updates...");
    for (final UpdateEntry updateEntry : getUpdateEntries()) {
      updateEntry.setPreCheckStatus(updateEntry.runPreCheck());
      if (updateEntry.getPreCheckStatus() != UpdatePreCheckStatus.ALREADY_UPDATED) {
        log
            .warn("*** Please note: The data-base perhaps has to be updated first before running the ProjectForge web app. Please login as administrator. Status '"
                + updateEntry.getPreCheckStatus()
                + "' for update entry '"
                + updateEntry.getRegionId()
                + "' "
                + updateEntry.getVersion());
        return false;
      }
    }
    log.info("No data-base updates found (OK).");
    return true;
  }

  /**
   * Runs all the pre checks of all update entries.
   */
  public void runAllPreChecks()
  {
    for (final UpdateEntry updateEntry : getUpdateEntries()) {
      updateEntry.setPreCheckStatus(updateEntry.runPreCheck());
    }
  }

  public SortedSet<UpdateEntry> getUpdateEntries()
  {
    synchronized (this) {
      if (updateEntries == null) {
        readUpdateFile();
      }
    }
    return updateEntries;
  }

  /**
   * Runs the update method of the given update entry.
   * @param updateScript
   */
  public void update(final UpdateEntry updateEntry)
  {
    updateEntry.setRunningStatus(updateEntry.runUpdate());
    final Table table = new Table(DatabaseUpdateDO.class);
    if (databaseUpdateDao.doesTableExist(table.getName()) == true) {
      databaseUpdateDao.insertInto(table.getName(), new String[] { "update_date", "region_id", "version", "execution_result",
          "executed_by_user_fk", "description"},
          new Object[] { new Date(), updateEntry.getRegionId(), String.valueOf(updateEntry.getVersion()), updateEntry.getRunningResult(),
              PFUserContext.getUserId(), updateEntry.getDescription()});
    } else {
      log.info("Data base table '" + table.getName() + "' doesn't (yet) exist. Can't register update (OK).");
    }
    updateEntry.setPreCheckStatus(updateEntry.runPreCheck());
    runAllPreChecks();
  }

  /**
   * Method used by UpdateScriptEntry for executing the groovy stuff.
   * @param updateScript
   * @return
   */
  UpdatePreCheckStatus runPreCheck(final UpdateEntryScript updateScript)
  {
    final GroovyResult result = execute(updateScript.getPreCheck());
    updateScript.setPreCheckResult(result);
    return (UpdatePreCheckStatus) result.getResult();
  }

  /**
   * Method used by UpdateScriptEntry for executing the groovy stuff.
   * @param updateScript
   * @return
   */
  UpdateRunningStatus runUpdate(final UpdateEntryScript updateScript)
  {
    log.info("Updating script " + updateScript.getVersion());
    runPreCheck(updateScript);
    if (UpdatePreCheckStatus.OK != updateScript.getPreCheckStatus()) {
      log.error("Pre-check failed. Aborting.");
      return UpdateRunningStatus.FAILED;
    }
    final GroovyResult result = execute(updateScript.getScript());
    updateScript.setRunningResult(result);
    if (result != null) {
      return (UpdateRunningStatus) result.getResult();
    }
    return UpdateRunningStatus.UNKNOWN;
  }

  GroovyResult execute(final String script)
  {
    final Map<String, Object> scriptVariables = new HashMap<String, Object>();
    scriptVariables.put("dao", databaseUpdateDao);
    scriptVariables.put("log", log);
    final StringBuffer buf = new StringBuffer();
    buf.append("import org.projectforge.admin.*;\n")//
        .append("import org.projectforge.database.*;\n\n")//
        .append(script);
    final GroovyResult groovyResult = groovyExecutor.execute(buf.toString(), scriptVariables);
    return groovyResult;
  }

  public void setDatabaseUpdateDao(final DatabaseUpdateDao databaseUpdateDao)
  {
    this.databaseUpdateDao = databaseUpdateDao;
  }

  public void setGroovyExecutor(GroovyExecutor groovyExecutor)
  {
    this.groovyExecutor = groovyExecutor;
  }
}
