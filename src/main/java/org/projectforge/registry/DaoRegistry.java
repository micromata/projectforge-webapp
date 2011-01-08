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

package org.projectforge.registry;

import org.projectforge.access.AccessDao;
import org.projectforge.address.AddressDao;
import org.projectforge.book.BookDao;
import org.projectforge.core.BaseDao;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.fibu.EingangsrechnungDao;
import org.projectforge.fibu.EmployeeDao;
import org.projectforge.fibu.EmployeeSalaryDao;
import org.projectforge.fibu.KontoDao;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.RechnungDao;
import org.projectforge.fibu.kost.BuchungssatzDao;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.fibu.kost.Kost2ArtDao;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.fibu.kost.KostZuweisungDao;
import org.projectforge.task.TaskDao;
import org.projectforge.timesheet.TimesheetDao;
import org.projectforge.timesheet.TimesheetFilter;
import org.projectforge.user.GroupDao;
import org.projectforge.user.UserDao;

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

  private static boolean initialized = false;

  // *******************************************************************************
  // *** Please note: All dao's are added automatically to the scripting engine! ***
  // *** If you don't want so, please edit ScriptDao. ***
  // *******************************************************************************

  public static final String ACCESS = "access";

  public static final String ACCOUNT = "account";

  public static final String ACCOUNTING_RECORD = "accountingRecord";

  public static final String ADDRESS = "address";

  public static final String BOOK = "book";

  public static final String COST_ASSIGNMENT = "costAssignment";

  public static final String COST1 = "cost1";

  public static final String COST2 = "cost2";

  public static final String COST2_Type = "cost2Type";

  public static final String CUSTOMER = "customer";

  public static final String EMPLOYEE = "employee";

  public static final String EMPLOYEE_SALARY = "employeeSalary";

  public static final String GROUP = "group";

  public static final String INCOMING_INVOICE = "incomingInvoice";

  public static final String ORDERBOOK = "orderBook";

  public static final String OUTGOING_INVOICE = "outgoingInvoice";

  public static final String PROJECT = "project";

  public static final String TASK = "task";

  public static final String TIMESHEET = "timesheet";

  public static final String USER = "user";

  private AccessDao accessDao;

  private AddressDao addressDao;

  private AuftragDao auftragDao;

  private BuchungssatzDao buchungssatzDao;

  private BookDao bookDao;

  private EingangsrechnungDao eingangsrechnungDao;

  private EmployeeDao employeeDao;

  private EmployeeSalaryDao employeeSalaryDao;

  private GroupDao groupDao;

  private KontoDao kontoDao;

  private Kost1Dao kost1Dao;

  private Kost2ArtDao kost2ArtDao;

  private Kost2Dao kost2Dao;

  private KostZuweisungDao kostZuweisungDao;

  private KundeDao kundeDao;

  private RechnungDao rechnungDao;

  private ProjektDao projektDao;

  private TaskDao taskDao;

  private UserDao userDao;

  private TimesheetDao timesheetDao;

  /**
   * Registers all daos.
   */
  public synchronized void init()
  {
    if (initialized == true) {
      log.error("DaoRegistry is already initialized!");
      return;
    }
    register(ADDRESS, AddressDao.class, addressDao);
    register(TIMESHEET, TimesheetDao.class, timesheetDao).setSearchFilterClass(TimesheetFilter.class);
    register(TASK, TaskDao.class, taskDao);
    register(BOOK, BookDao.class, bookDao);
    register(OUTGOING_INVOICE, RechnungDao.class, rechnungDao, "fibu.rechnung");
    register(INCOMING_INVOICE, EingangsrechnungDao.class, eingangsrechnungDao, "fibu.eingangsrechnung");
    register(USER, UserDao.class, userDao);
    register(GROUP, GroupDao.class, groupDao);
    register(ACCESS, AccessDao.class, accessDao);
    register(ACCOUNTING_RECORD, BuchungssatzDao.class, buchungssatzDao, "fibu.buchungssatz");
    register(COST1, Kost1Dao.class, kost1Dao, "fibu.kost1");
    register(COST2, Kost2Dao.class, kost2Dao, "fibu.kost2");
    register(COST2_Type, Kost2ArtDao.class, kost2ArtDao, "fibu.kost2art");
    register(COST_ASSIGNMENT, KostZuweisungDao.class, kostZuweisungDao, "fibu.");
    register(ACCOUNT, KontoDao.class, kontoDao, "fibu.konto");
    register(CUSTOMER, KundeDao.class, kundeDao, "fibu.kunde");
    register(PROJECT, ProjektDao.class, projektDao, "fibu.projekt");
    register(ORDERBOOK, AuftragDao.class, auftragDao, "fibu.auftrag");
    register(EMPLOYEE, EmployeeDao.class, employeeDao, "fibu.employee");
    register(EMPLOYEE_SALARY, EmployeeDao.class, employeeSalaryDao, "fibu.employee.saraly");
    initialized = true;
  }

  public DaoRegistry()
  {
  }

  private RegistryEntry register(final String id, final Class< ? extends BaseDao< ? >> daoClassType, final BaseDao< ? > dao)
  {
    return register(id, daoClassType, dao, null);
  }

  private RegistryEntry register(final String id, final Class< ? extends BaseDao< ? >> daoClassType, final BaseDao< ? > dao,
      final String i18nPrefix)
  {
    if (dao == null) {
      log.error("Dao for '" + id + "' is null! Ignoring dao in registry.");
      return new RegistryEntry(null, null, null); // Create dummy.
    }
    final Registry registry = Registry.instance();
    final RegistryEntry entry = new RegistryEntry(id, daoClassType, dao, i18nPrefix);
    registry.register(id, entry);
    log.info("Dao '" + id + "' registerd.");
    return entry;
  }

  public void setAccessDao(AccessDao accessDao)
  {
    this.accessDao = accessDao;
  }

  public void setAddressDao(AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }

  public void setAuftragDao(AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
  }

  public void setBuchungssatzDao(BuchungssatzDao buchungssatzDao)
  {
    this.buchungssatzDao = buchungssatzDao;
  }

  public void setBookDao(BookDao bookDao)
  {
    this.bookDao = bookDao;
  }

  public void setEmployeeDao(EmployeeDao employeeDao)
  {
    this.employeeDao = employeeDao;
  }

  public void setEmployeeSalaryDao(EmployeeSalaryDao employeeSalaryDao)
  {
    this.employeeSalaryDao = employeeSalaryDao;
  }

  public void setEingangsrechnungDao(EingangsrechnungDao eingangsrechnungDao)
  {
    this.eingangsrechnungDao = eingangsrechnungDao;
  }

  public void setGroupDao(GroupDao groupDao)
  {
    this.groupDao = groupDao;
  }

  public void setKontoDao(KontoDao kontoDao)
  {
    this.kontoDao = kontoDao;
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

  public void setRechnungDao(RechnungDao rechnungDao)
  {
    this.rechnungDao = rechnungDao;
  }

  public void setProjektDao(ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }

  public void setTaskDao(TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setTimesheetDao(TimesheetDao timesheetDao)
  {
    this.timesheetDao = timesheetDao;
  }
}
