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

package org.projectforge.scripting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.projectforge.AppVersion;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.core.ScriptingDao;
import org.projectforge.fibu.kost.reporting.ReportGeneratorList;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.task.ScriptingTaskTree;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ScriptDao extends BaseDao<ScriptDO>
{
  private TaskTree taskTree;

  private GroovyExecutor groovyExecutor;

  private GroovyResult groovyResult;

  public ScriptDao()
  {
    super(ScriptDO.class);
  }

  /**
   * Copy old script as script backup if modified.
   * @see org.projectforge.core.BaseDao#onChange(org.projectforge.core.ExtendedBaseDO, org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void onChange(final ScriptDO obj, final ScriptDO dbObj)
  {
    if (StringUtils.equals(dbObj.getScript(), obj.getScript()) == false) {
      obj.setScriptBackup(dbObj.getScript());
    }
  }

  /**
   * User must be member of group controlling or finance.
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final ScriptDO obj, final ScriptDO oldObj, final OperationType operationType, final boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(user, throwException, ProjectForgeGroup.CONTROLLING_GROUP, ProjectForgeGroup.FINANCE_GROUP);
  }

  @Override
  public ScriptDO newInstance()
  {
    return new ScriptDO();
  }

  public GroovyResult execute(final ScriptDO script, final List<ScriptParameter> parameters)
  {
    hasLoggedInUserSelectAccess(script, true);
    final ReportGeneratorList reportGeneratorList = new ReportGeneratorList();
    final Map<String, Object> scriptVariables = new HashMap<String, Object>();

    addScriptVariables(scriptVariables);
    addAliasForDeprecatedScriptVariables(scriptVariables);
    scriptVariables.put("reportList", reportGeneratorList);
    if (parameters != null) {
      for (final ScriptParameter param : parameters) {
        scriptVariables.put(param.getParameterName(), param.getValue());
      }
    }
    groovyResult = groovyExecutor.execute(new GroovyResult(), script.getScript(), scriptVariables);
    return groovyResult;
  }

  /**
   * Adds all registered dao's and other variables, such as appId, appVersion and task-tree. These variables are available in Groovy scripts
   * @param scriptVariables
   */
  public void addScriptVariables(final Map<String, Object> scriptVariables)
  {
    scriptVariables.put("appId", AppVersion.APP_ID);
    scriptVariables.put("appVersion", AppVersion.NUMBER);
    scriptVariables.put("appRelease", AppVersion.RELEASE_DATE);
    scriptVariables.put("reportList", null);
    scriptVariables.put("taskTree", new ScriptingTaskTree(taskTree));
    for (final RegistryEntry entry : Registry.instance().getOrderedList()) {
      final ScriptingDao<?> scriptingDao = entry.getScriptingDao();
      if (scriptingDao != null) {
        final String varName = StringUtils.uncapitalize(entry.getId());
        scriptVariables.put(varName + "Dao", scriptingDao);
      }
    }
  }

  /**
   * Some dao's are renamed, this methods adds the old names as aliases. Please note: addScriptVariables(Map) should be called before!
   * @param scriptVariables
   */
  public void addAliasForDeprecatedScriptVariables(final Map<String, Object> scriptVariables)
  {
    scriptVariables.put("auftragDao", scriptVariables.get(DaoRegistry.ORDERBOOK + "Dao"));
    scriptVariables.put("buchungssatzDao", scriptVariables.get(DaoRegistry.ACCOUNTING_RECORD + "Dao"));
    scriptVariables.put("eingangsrechnungDao", scriptVariables.get(DaoRegistry.INCOMING_INVOICE + "Dao"));
    scriptVariables.put("kost1Dao", scriptVariables.get(DaoRegistry.COST1 + "Dao"));
    scriptVariables.put("kost2ArtDao", scriptVariables.get(DaoRegistry.COST2_Type + "Dao"));
    scriptVariables.put("kost2Dao", scriptVariables.get(DaoRegistry.COST2 + "Dao"));
    scriptVariables.put("kostZuweisungDao", scriptVariables.get(DaoRegistry.COST_ASSIGNMENT + "Dao"));
    scriptVariables.put("kundeDao", scriptVariables.get(DaoRegistry.CUSTOMER + "Dao"));
    scriptVariables.put("projektDao", scriptVariables.get(DaoRegistry.PROJECT + "Dao"));
    scriptVariables.put("rechnungDao", scriptVariables.get(DaoRegistry.OUTGOING_INVOICE + "Dao"));
  }

  public void setTaskTree(final TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setGroovyExecutor(final GroovyExecutor groovyExecutor)
  {
    this.groovyExecutor = groovyExecutor;
  }
}
