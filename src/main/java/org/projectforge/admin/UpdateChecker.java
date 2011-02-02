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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.projectforge.common.StringHelper;
import org.projectforge.core.UserException;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.scripting.GroovyExecutor;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlObjectReader;

/**
 * Checks wether the data-base is up-to-date or not.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class UpdateChecker
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateChecker.class);

  private DatabaseUpdateDao databaseUpdateDao;

  private GroovyExecutor groovyExecutor;

  private List<UpdateScript> updateScripts;

  public void readUpdateFile()
  {
    final String filename = "updates/update-scripts.xml";
    final ClassLoader classLoader = this.getClass().getClassLoader();
    final InputStream is = classLoader.getResourceAsStream(filename);
    readUpdateFile(is);
  }

  @SuppressWarnings("unchecked")
  public void readUpdateFile(final InputStream is)
  {
    final XmlObjectReader reader = new XmlObjectReader();
    reader.initialize(UpdateScript.class);
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(ArrayList.class, "projectforge-self-update");
    reader.setAliasMap(aliasMap);
    reader.initialize(UpdateScript.class);
    String xml = null;
    try {
      xml = IOUtils.toString(is);
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new UserException("Unsupported update script format (see log files for details).");
    }
    updateScripts = (List<UpdateScript>) reader.read(xml); // Read all scripts from xml.
    Collections.sort(updateScripts, new Comparator<UpdateScript>() {
      public int compare(UpdateScript o1, UpdateScript o2)
      {
        // Newest version first (descendant order):
        return StringHelper.compareTo(o2.getVersion(), o1.getVersion());
      }
    });
  }

  /**
   * Runs the pre-check test of the first scriptlet in the list.
   * @return true if ALREADY_UPDATED, otherwise false.
   */
  public boolean isUpdated()
  {
    if (updateScripts == null) {
      readUpdateFile();
    }
    final UpdateScript firstUpdateScript = updateScripts.get(0);
    runPreCheck(firstUpdateScript);
    final boolean result = firstUpdateScript.getPreCheckStatus() == UpdatePreCheckStatus.ALREADY_UPDATED;
    if (result == false) {
      log
          .warn("*** Please note: The data-base perhaps has to be updated first before running the ProjectForge web app. Please login as administrator. Status: "
              + firstUpdateScript.getPreCheckStatus());
    }
    return result;
  }

  public void runAllPreChecks()
  {
    for (final UpdateScript updateScript : updateScripts) {
      runPreCheck(updateScript);
    }
  }

  protected void runPreCheck(final UpdateScript updateScript)
  {
    final GroovyResult result = execute(updateScript.getPreCheck());
    updateScript.setPreCheckResult(result);
    updateScript.setPreCheckStatus(((UpdatePreCheckStatus) result.getResult()));
  }

  protected GroovyResult execute(final String script)
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
