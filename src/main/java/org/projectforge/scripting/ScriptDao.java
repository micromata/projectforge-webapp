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
import org.projectforge.address.AddressDao;
import org.projectforge.common.LabelValueBean;
import org.projectforge.core.BaseDao;
import org.projectforge.core.ScriptingDao;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.EingangsrechnungDao;
import org.projectforge.fibu.EmployeeDao;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.EmployeeSalaryDao;
import org.projectforge.fibu.EmployeeScriptingDao;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungDao;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.BuchungssatzDao;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.fibu.kost.Kost1ScriptingDao;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2ArtDao;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.fibu.kost.KostZuweisungDO;
import org.projectforge.fibu.kost.KostZuweisungDao;
import org.projectforge.fibu.kost.reporting.ReportGeneratorList;
import org.projectforge.task.ScriptingTaskTree;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskTree;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ScriptDao extends BaseDao<ScriptDO>
{
  private AddressDao addressDao;

  private AuftragDao auftragDao;

  private BuchungssatzDao buchungssatzDao;

  private EingangsrechnungDao eingangsrechnungDao;

  private EmployeeDao employeeDao;

  private EmployeeSalaryDao employeeSalaryDao;

  private Kost1Dao kost1Dao;

  private Kost2ArtDao kost2ArtDao;

  private Kost2Dao kost2Dao;

  private KostZuweisungDao kostZuweisungDao;

  private KundeDao kundeDao;

  private ProjektDao projektDao;

  private RechnungDao rechnungDao;

  private TaskDao taskDao;

  private TaskTree taskTree;

  private TimesheetDao timesheetDao;

  private UserDao userDao;

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
    scriptVariables.put("addressDao", new ScriptingDao<AddressDO>(addressDao));
    scriptVariables.put("auftragDao", new ScriptingDao<AuftragDO>(auftragDao));
    scriptVariables.put("buchungssatzDao", new ScriptingDao<BuchungssatzDO>(buchungssatzDao));
    scriptVariables.put("eingangsrechnungDao", new ScriptingDao<EingangsrechnungDO>(eingangsrechnungDao));
    scriptVariables.put("employeeDao", new EmployeeScriptingDao(employeeDao));
    scriptVariables.put("employeeSalaryDao", new ScriptingDao<EmployeeSalaryDO>(employeeSalaryDao));
    scriptVariables.put("kost1Dao", new Kost1ScriptingDao(kost1Dao));
    scriptVariables.put("kost2Dao", new ScriptingDao<Kost2DO>(kost2Dao));
    scriptVariables.put("kost2ArtDao", new ScriptingDao<Kost2ArtDO>(kost2ArtDao));
    scriptVariables.put("kostZuweisungDao", new ScriptingDao<KostZuweisungDO>(kostZuweisungDao));
    scriptVariables.put("kundeDao", new ScriptingDao<KundeDO>(kundeDao));
    scriptVariables.put("projektDao", new ScriptingDao<ProjektDO>(projektDao));
    scriptVariables.put("rechnungDao", new ScriptingDao<RechnungDO>(rechnungDao));
    scriptVariables.put("reportList", null);
    scriptVariables.put("taskDao", new ScriptingDao<TaskDO>(taskDao));
    scriptVariables.put("taskTree", new ScriptingTaskTree(taskTree));
    scriptVariables.put("timesheetDao", new ScriptingDao<TimesheetDO>(timesheetDao));
    scriptVariables.put("userDao", new ScriptingDao<PFUserDO>(userDao));
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

  public void setAddressDao(AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }

  public void setAuftragDao(AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
  }

  public void setEingangsrechnungDao(EingangsrechnungDao eingangsrechnungDao)
  {
    this.eingangsrechnungDao = eingangsrechnungDao;
  }

  public void setBuchungssatzDao(BuchungssatzDao buchungssatzDao)
  {
    this.buchungssatzDao = buchungssatzDao;
  }

  public void setEmployeeDao(EmployeeDao employeeDao)
  {
    this.employeeDao = employeeDao;
  }

  public void setEmployeeSalaryDao(EmployeeSalaryDao employeeSalaryDao)
  {
    this.employeeSalaryDao = employeeSalaryDao;
  }

  public void setKost1Dao(Kost1Dao kost1Dao)
  {
    this.kost1Dao = kost1Dao;
  }

  public void setKost2ArtDao(Kost2ArtDao kost2ArtDao)
  {
    this.kost2ArtDao = kost2ArtDao;
  }

  public void setKost2Dao(Kost2Dao kost2Dao)
  {
    this.kost2Dao = kost2Dao;
  }

  public void setKostZuweisungDao(KostZuweisungDao kostZuweisungDao)
  {
    this.kostZuweisungDao = kostZuweisungDao;
  }

  public void setKundeDao(KundeDao kundeDao)
  {
    this.kundeDao = kundeDao;
  }

  public void setProjektDao(ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }

  public void setRechnungDao(RechnungDao rechnungDao)
  {
    this.rechnungDao = rechnungDao;
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setTaskDao(TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setTaskTree(TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setTimesheetDao(TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }

  public void setGroovyExecutor(GroovyExecutor groovyExecutor)
  {
    this.groovyExecutor = groovyExecutor;
  }
}
