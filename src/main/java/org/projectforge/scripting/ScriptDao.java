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

package org.projectforge.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.projectforge.Version;
import org.projectforge.access.OperationType;
import org.projectforge.address.AddressDO;
import org.projectforge.common.LabelValueBean;
import org.projectforge.core.BaseDao;
import org.projectforge.core.ScriptingDao;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.EmployeeDao;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.EmployeeScriptingDao;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.fibu.kost.Kost1ScriptingDao;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.KostZuweisungDO;
import org.projectforge.fibu.kost.reporting.ReportGeneratorList;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.registry.Registry;
import org.projectforge.task.ScriptingTaskTree;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ScriptDao extends BaseDao<ScriptDO>
{
  private Registry registry;

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
  public boolean hasAccess(ScriptDO obj, ScriptDO oldObj, OperationType operationType, boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(throwException, ProjectForgeGroup.CONTROLLING_GROUP, ProjectForgeGroup.FINANCE_GROUP);
  }

  @Override
  public ScriptDO newInstance()
  {
    return new ScriptDO();
  }

  public GroovyResult execute(final ScriptDO script, List<ScriptParameter> parameters)
  {
    hasSelectAccess(script, true);
    ReportGeneratorList reportGeneratorList = new ReportGeneratorList();
    final Map<String, Object> scriptVariables = new HashMap<String, Object>();

    getScriptVariables(scriptVariables);
    scriptVariables.put("reportList", reportGeneratorList);
    if (parameters != null) {
      for (final ScriptParameter param : parameters) {
        scriptVariables.put(param.getParameterName(), param.getValue());
      }
    }
    groovyResult = groovyExecutor.execute(script.getScript(), scriptVariables);
    return groovyResult;
  }

  public List<LabelValueBean<String, Class< ? >>> getScriptVariables(final Map<String, Object> scriptVariables)
  {
    scriptVariables.put("appId", Version.APP_ID);
    scriptVariables.put("appVersion", Version.NUMBER);
    scriptVariables.put("appRelease", Version.RELEASE_DATE);
    scriptVariables.put("addressDao", new ScriptingDao<AddressDO>(registry.getDao(DaoRegistry.ADDRESS)));
    scriptVariables.put("auftragDao", new ScriptingDao<AuftragDO>(registry.getDao(DaoRegistry.ORDERBOOK)));
    scriptVariables.put("bookDao", new ScriptingDao<BuchungssatzDO>(registry.getDao(DaoRegistry.BOOK)));
    scriptVariables.put("buchungssatzDao", new ScriptingDao<BuchungssatzDO>(registry.getDao(DaoRegistry.BUCHUNGSSATZ)));
    scriptVariables.put("eingangsrechnungDao", new ScriptingDao<EingangsrechnungDO>(registry.getDao(DaoRegistry.EINGANGSRECHNUNG)));
    scriptVariables.put("employeeDao", new EmployeeScriptingDao((EmployeeDao) registry.getDao(DaoRegistry.EMPLOYEE)));
    scriptVariables.put("employeeSalaryDao", new ScriptingDao<EmployeeSalaryDO>(registry.getDao(DaoRegistry.EMPLOYEE_SALARY)));
    scriptVariables.put("kost1Dao", new Kost1ScriptingDao((Kost1Dao) registry.getDao(DaoRegistry.KOST1)));
    scriptVariables.put("kost2Dao", new ScriptingDao<Kost2DO>(registry.getDao(DaoRegistry.KOST2)));
    scriptVariables.put("kost2ArtDao", new ScriptingDao<Kost2ArtDO>(registry.getDao(DaoRegistry.KOST2_ART)));
    scriptVariables.put("kostZuweisungDao", new ScriptingDao<KostZuweisungDO>(registry.getDao(DaoRegistry.KOST_ZUWEISUNG)));
    scriptVariables.put("kundeDao", new ScriptingDao<KundeDO>(registry.getDao(DaoRegistry.KUNDE)));
    scriptVariables.put("projektDao", new ScriptingDao<ProjektDO>(registry.getDao(DaoRegistry.PROJEKT)));
    scriptVariables.put("rechnungDao", new ScriptingDao<RechnungDO>(registry.getDao(DaoRegistry.RECHNUNG)));
    scriptVariables.put("reportList", null);
    scriptVariables.put("taskDao", new ScriptingDao<TaskDO>(registry.getDao(DaoRegistry.TASK)));
    scriptVariables.put("taskTree", new ScriptingTaskTree(taskTree));
    scriptVariables.put("timesheetDao", new ScriptingDao<TimesheetDO>(registry.getDao(DaoRegistry.TIMESHEET)));
    scriptVariables.put("userDao", new ScriptingDao<PFUserDO>(registry.getDao(DaoRegistry.USER)));
    List<LabelValueBean<String, Class< ? >>> result = new ArrayList<LabelValueBean<String, Class< ? >>>();
    SortedSet<String> set = new TreeSet<String>();
    set.addAll(scriptVariables.keySet());
    for (String key : set) {
      Object obj = scriptVariables.get(key);
      Class< ? > clazz = null;
      if (obj != null) {
        clazz = obj.getClass();
      }
      LabelValueBean<String, Class< ? >> lv = new LabelValueBean<String, Class< ? >>(key, clazz);
      result.add(lv);
    }
    return result;
  }

  public void setRegistry(Registry registry)
  {
    this.registry = registry;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setGroovyExecutor(GroovyExecutor groovyExecutor)
  {
    this.groovyExecutor = groovyExecutor;
  }
}
