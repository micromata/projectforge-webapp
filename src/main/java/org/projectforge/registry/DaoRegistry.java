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

package org.projectforge.registry;

import javax.sql.DataSource;

import org.projectforge.access.AccessDao;
import org.projectforge.address.AddressDao;
import org.projectforge.book.BookDao;
import org.projectforge.core.BaseDao;
import org.projectforge.core.ConfigurationDao;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.EingangsrechnungDao;
import org.projectforge.fibu.EingangsrechnungsPositionDO;
import org.projectforge.fibu.EmployeeDao;
import org.projectforge.fibu.EmployeeSalaryDao;
import org.projectforge.fibu.EmployeeScriptingDao;
import org.projectforge.fibu.KontoDao;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.RechnungDao;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.fibu.kost.BuchungssatzDao;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.fibu.kost.Kost1ScriptingDao;
import org.projectforge.fibu.kost.Kost2ArtDao;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.fibu.kost.KostZuweisungDao;
import org.projectforge.gantt.GanttChartDao;
import org.projectforge.humanresources.HRPlanningDao;
import org.projectforge.humanresources.HRPlanningEntryDO;
import org.projectforge.meb.MebDao;
import org.projectforge.orga.ContractDao;
import org.projectforge.orga.PostausgangDao;
import org.projectforge.orga.PosteingangDao;
import org.projectforge.scripting.ScriptDao;
import org.projectforge.task.TaskDao;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.user.GroupDao;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserPrefDao;
import org.projectforge.user.UserRightDao;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Helper object which stores all dao objects and put them into the registry. <br/>
 * <b>Please note:</b><br/>
 * All dao's are added automatically to the scripting engine! If you don't want so, please edit ScriptDao.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DaoRegistry
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DaoRegistry.class);

  private static DaoRegistry instance;

  // *******************************************************************************
  // *** Please note: All dao's are added automatically to the scripting engine! ***
  // *** If you don't want so, please edit ScriptDao. ***
  // *******************************************************************************

  public static final String ACCESS = "access";

  public static final String ACCOUNT = "account";

  public static final String ACCOUNTING_RECORD = "accountingRecord";

  public static final String ADDRESS = "address";

  public static final String BOOK = "book";

  public static final String CONFIGURATION = "configuration";

  public static final String CONTRACT = "contract";

  public static final String COST_ASSIGNMENT = "costAssignment";

  public static final String COST1 = "cost1";

  public static final String COST2 = "cost2";

  public static final String COST2_Type = "cost2Type";

  public static final String CUSTOMER = "customer";

  public static final String EMPLOYEE = "employee";

  public static final String EMPLOYEE_SALARY = "employeeSalary";

  public static final String GANTT = "gantt";

  public static final String GROUP = "group";

  public static final String HR_LIST = "hrList";

  public static final String HR_PLANNING = "hrPlanning";

  public static final String INCOMING_INVOICE = "incomingInvoice";

  public static final String INCOMING_MAIL = "incomingMail";

  public static final String MEB = "meb";

  public static final String ORDERBOOK = "orderBook";

  public static final String OUTGOING_INVOICE = "outgoingInvoice";

  public static final String OUTGOING_MAIL = "outgoingMail";

  public static final String PROJECT = "project";

  public static final String SCRIPT = "script";

  public static final String TASK = "task";

  public static final String TIMESHEET = "timesheet";

  public static final String USER = "user";

  public static final String USER_RIGHT = "userRight";

  public static final String USER_PREF = "userPref";

  private AccessDao accessDao;

  private AddressDao addressDao;

  private AuftragDao auftragDao;

  private BuchungssatzDao buchungssatzDao;

  private BookDao bookDao;

  private ConfigurationDao configurationDao;

  private ContractDao contractDao;

  private DataSource dataSource;

  private EingangsrechnungDao eingangsrechnungDao;

  private EmployeeDao employeeDao;

  private EmployeeSalaryDao employeeSalaryDao;

  private GanttChartDao ganttChartDao;

  private GroupDao groupDao;

  private HibernateTemplate hibernateTemplate;

  private HRPlanningDao hrPlanningDao;

  private KontoDao kontoDao;

  private Kost1Dao kost1Dao;

  private Kost2ArtDao kost2ArtDao;

  private Kost2Dao kost2Dao;

  private KostZuweisungDao kostZuweisungDao;

  private KundeDao kundeDao;

  private MebDao mebDao;

  private PostausgangDao postausgangDao;

  private PosteingangDao posteingangDao;

  private RechnungDao rechnungDao;

  private ProjektDao projektDao;

  private ScriptDao scriptDao;

  private TaskDao taskDao;

  private TimesheetDao timesheetDao;

  private UserDao userDao;

  private UserPrefDao userPrefDao;

  private UserRightDao userRightDao;

  /**
   * Registers all daos.
   */
  @SuppressWarnings("unchecked")
  public synchronized void init()
  {
    if (instance != null) {
      log.error("DaoRegistry is already initialized!");
      return;
    }
    register(CONFIGURATION, ConfigurationDao.class, configurationDao, "administration.configuration").setSearchable(false);
    register(USER, UserDao.class, userDao, "user");
    Registry.instance().setUserGroupCache(userDao.getUserGroupCache());
    register(GROUP, GroupDao.class, groupDao, "group");
    register(TASK, TaskDao.class, taskDao, "task"); // needs PFUserDO
    Registry.instance().setTaskTree(taskDao.getTaskTree());
    register(ACCESS, AccessDao.class, accessDao, "access");

    register(ADDRESS, AddressDao.class, addressDao, "address");
    register(TIMESHEET, TimesheetDao.class, timesheetDao, "timesheet") //
    .setSearchFilterClass(TimesheetFilter.class);
    register(BOOK, BookDao.class, bookDao, "book");

    register(CUSTOMER, KundeDao.class, kundeDao, "fibu.kunde");
    register(PROJECT, ProjektDao.class, projektDao, "fibu.projekt"); // Needs customer

    register(COST1, Kost1Dao.class, kost1Dao, "fibu.kost1").setScriptingDao(new Kost1ScriptingDao(kost1Dao));
    register(COST2_Type, Kost2ArtDao.class, kost2ArtDao, "fibu.kost2art");
    register(COST2, Kost2Dao.class, kost2Dao, "fibu.kost2"); // Needs kost2Art and project
    register(COST_ASSIGNMENT, KostZuweisungDao.class, kostZuweisungDao, "fibu.") // Needs kost, invoices, employee salaries
    .setFullTextSearchSupport(false).setSearchable(false);

    register(ORDERBOOK, AuftragDao.class, auftragDao, "fibu.auftrag") // Needs customer, project
    .setNestedDOClasses(AuftragsPositionDO.class);
    register(OUTGOING_INVOICE, RechnungDao.class, rechnungDao, "fibu.rechnung") // Needs customer, project
    .setNestedDOClasses(RechnungsPositionDO.class);
    register(INCOMING_INVOICE, EingangsrechnungDao.class, eingangsrechnungDao, "fibu.eingangsrechnung") //
    .setNestedDOClasses(EingangsrechnungsPositionDO.class);
    register(ACCOUNTING_RECORD, BuchungssatzDao.class, buchungssatzDao, "fibu.buchungssatz").setSearchable(false); // Need account, cost1
    // and cost2.
    register(ACCOUNT, KontoDao.class, kontoDao, "fibu.konto");
    Registry.instance().setKontoCache(kontoDao.getKontoCache());
    register(EMPLOYEE, EmployeeDao.class, employeeDao, "fibu.employee").setScriptingDao(new EmployeeScriptingDao(employeeDao));
    register(EMPLOYEE_SALARY, EmployeeDao.class, employeeSalaryDao, "fibu.employee.salary").setSearchable(false);

    register(CONTRACT, ContractDao.class, contractDao, "legalAffaires.contract");
    register(OUTGOING_MAIL, PostausgangDao.class, postausgangDao, "orga.postausgang");
    register(INCOMING_MAIL, PosteingangDao.class, posteingangDao, "orga.posteingang");

    register(GANTT, GanttChartDao.class, ganttChartDao, "gantt");
    register(HR_PLANNING, HRPlanningDao.class, hrPlanningDao, "hr.planning") //
    .setNestedDOClasses(HRPlanningEntryDO.class).setSearchable(false);

    register(MEB, MebDao.class, mebDao, "meb");
    register(SCRIPT, ScriptDao.class, scriptDao, "scripting").setSearchable(false);
    register(USER_PREF, UserPrefDao.class, userPrefDao).setSearchable(false);
    register(USER_RIGHT, UserRightDao.class, userRightDao).setSearchable(false);
    Registry.instance().setDataSource(dataSource);
    Registry.instance().setHibernateTemplate(hibernateTemplate);
    instance = this;
  }

  public DaoRegistry()
  {
  }

  private RegistryEntry register(final String id, final Class< ? extends BaseDao< ? >> daoClassType, final BaseDao< ? > dao)
  {
    return register(id, daoClassType, dao, null);
  }

  /**
   * Registers a new dao, which is available
   * @param id
   * @param daoClassType
   * @param dao
   * @param i18nPrefix
   * @return
   */
  public RegistryEntry register(final String id, final Class< ? extends BaseDao< ? >> daoClassType, final BaseDao< ? > dao,
      final String i18nPrefix)
  {
    if (dao == null) {
      log.error("Dao for '" + id + "' is null! Ignoring dao in registry.");
      return new RegistryEntry(null, null, null); // Create dummy.
    }
    final Registry registry = Registry.instance();
    final RegistryEntry entry = new RegistryEntry(id, daoClassType, dao, i18nPrefix);
    registry.register(entry);
    log.info("Dao '" + id + "' registerd.");
    return entry;
  }

  public void setAccessDao(final AccessDao accessDao)
  {
    this.accessDao = accessDao;
  }

  public void setAddressDao(final AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }

  public void setAuftragDao(final AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
  }

  public void setBuchungssatzDao(final BuchungssatzDao buchungssatzDao)
  {
    this.buchungssatzDao = buchungssatzDao;
  }

  public void setBookDao(final BookDao bookDao)
  {
    this.bookDao = bookDao;
  }

  public void setConfigurationDao(final ConfigurationDao configurationDao)
  {
    this.configurationDao = configurationDao;
  }

  public void setContractDao(final ContractDao contractDao)
  {
    this.contractDao = contractDao;
  }

  public void setDataSource(final DataSource dataSource)
  {
    this.dataSource = dataSource;
  }

  public void setEmployeeDao(final EmployeeDao employeeDao)
  {
    this.employeeDao = employeeDao;
  }

  public void setEmployeeSalaryDao(final EmployeeSalaryDao employeeSalaryDao)
  {
    this.employeeSalaryDao = employeeSalaryDao;
  }

  public void setEingangsrechnungDao(final EingangsrechnungDao eingangsrechnungDao)
  {
    this.eingangsrechnungDao = eingangsrechnungDao;
  }

  public void setGanttChartDao(final GanttChartDao ganttChartDao)
  {
    this.ganttChartDao = ganttChartDao;
  }

  public void setGroupDao(final GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setHibernateTemplate(final HibernateTemplate hibernateTemplate)
  {
    this.hibernateTemplate = hibernateTemplate;
  }

  public void setHRPlanningDao(final HRPlanningDao hrPlanningDao)
  {
    this.hrPlanningDao = hrPlanningDao;
  }

  public void setKontoDao(final KontoDao kontoDao)
  {
    this.kontoDao = kontoDao;
  }

  public void setKost1Dao(final Kost1Dao kost1Dao)
  {
    this.kost1Dao = kost1Dao;
  }

  public void setKost2ArtDao(final Kost2ArtDao kost2ArtDao)
  {
    this.kost2ArtDao = kost2ArtDao;
  }

  public void setKost2Dao(final Kost2Dao kost2Dao)
  {
    this.kost2Dao = kost2Dao;
  }

  public void setKostZuweisungDao(final KostZuweisungDao kostZuweisungDao)
  {
    this.kostZuweisungDao = kostZuweisungDao;
  }

  public void setKundeDao(final KundeDao kundeDao)
  {
    this.kundeDao = kundeDao;
  }

  public void setMebDao(final MebDao mebDao)
  {
    this.mebDao = mebDao;
  }

  public void setPostausgangDao(final PostausgangDao postausgangDao)
  {
    this.postausgangDao = postausgangDao;
  }

  public void setPosteingangDao(final PosteingangDao posteingangDao)
  {
    this.posteingangDao = posteingangDao;
  }

  public void setRechnungDao(final RechnungDao rechnungDao)
  {
    this.rechnungDao = rechnungDao;
  }

  public void setProjektDao(final ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }

  public void setScriptDao(final ScriptDao scriptDao)
  {
    this.scriptDao = scriptDao;
  }

  public void setTaskDao(final TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setTimesheetDao(final TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setUserPrefDao(final UserPrefDao userPrefDao)
  {
    this.userPrefDao = userPrefDao;
  }

  public void setUserRightDao(final UserRightDao userRightDao)
  {
    this.userRightDao = userRightDao;
  }
}
