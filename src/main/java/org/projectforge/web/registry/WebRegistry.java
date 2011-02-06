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

package org.projectforge.web.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.html.WebPage;
import org.projectforge.core.BaseDao;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.registry.Registry;
import org.projectforge.web.LoginPage;
import org.projectforge.web.access.AccessEditPage;
import org.projectforge.web.access.AccessListPage;
import org.projectforge.web.address.AddressEditPage;
import org.projectforge.web.address.AddressListPage;
import org.projectforge.web.address.AddressMobileEditPage;
import org.projectforge.web.address.AddressMobileListPage;
import org.projectforge.web.address.AddressMobileViewPage;
import org.projectforge.web.address.AddressViewPage;
import org.projectforge.web.address.PhoneCallPage;
import org.projectforge.web.address.SendSmsPage;
import org.projectforge.web.admin.AdminPage;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.admin.SystemUpdatePage;
import org.projectforge.web.book.BookEditPage;
import org.projectforge.web.book.BookListPage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.core.ConfigurationListPage;
import org.projectforge.web.core.SearchPage;
import org.projectforge.web.doc.DocumentationPage;
import org.projectforge.web.doc.TutorialPage;
import org.projectforge.web.fibu.AccountingRecordEditPage;
import org.projectforge.web.fibu.AccountingRecordListPage;
import org.projectforge.web.fibu.AuftragEditPage;
import org.projectforge.web.fibu.AuftragListPage;
import org.projectforge.web.fibu.BankAccountEditPage;
import org.projectforge.web.fibu.BankAccountListPage;
import org.projectforge.web.fibu.CustomerEditPage;
import org.projectforge.web.fibu.CustomerListPage;
import org.projectforge.web.fibu.DatevImportPage;
import org.projectforge.web.fibu.EingangsrechnungEditPage;
import org.projectforge.web.fibu.EingangsrechnungListPage;
import org.projectforge.web.fibu.EmployeeEditPage;
import org.projectforge.web.fibu.EmployeeListPage;
import org.projectforge.web.fibu.EmployeeSalaryEditPage;
import org.projectforge.web.fibu.EmployeeSalaryListPage;
import org.projectforge.web.fibu.KontoEditPage;
import org.projectforge.web.fibu.KontoListPage;
import org.projectforge.web.fibu.Kost1EditPage;
import org.projectforge.web.fibu.Kost1ListPage;
import org.projectforge.web.fibu.Kost2ArtEditPage;
import org.projectforge.web.fibu.Kost2ArtListPage;
import org.projectforge.web.fibu.Kost2EditPage;
import org.projectforge.web.fibu.Kost2ListPage;
import org.projectforge.web.fibu.ProjektEditPage;
import org.projectforge.web.fibu.ProjektListPage;
import org.projectforge.web.fibu.RechnungEditPage;
import org.projectforge.web.fibu.RechnungListPage;
import org.projectforge.web.fibu.ReportObjectivesPage;
import org.projectforge.web.gantt.GanttChartEditPage;
import org.projectforge.web.gantt.GanttChartListPage;
import org.projectforge.web.gwiki.GWikiContainerPage;
import org.projectforge.web.humanresources.HRListPage;
import org.projectforge.web.humanresources.HRPlanningEditPage;
import org.projectforge.web.humanresources.HRPlanningListPage;
import org.projectforge.web.imagecropper.ImageCropperPage;
import org.projectforge.web.meb.MebEditPage;
import org.projectforge.web.meb.MebListPage;
import org.projectforge.web.mobile.LoginMobilePage;
import org.projectforge.web.mobile.MenuMobilePage;
import org.projectforge.web.orga.ContractEditPage;
import org.projectforge.web.orga.ContractListPage;
import org.projectforge.web.orga.PostausgangEditPage;
import org.projectforge.web.orga.PostausgangListPage;
import org.projectforge.web.orga.PosteingangEditPage;
import org.projectforge.web.orga.PosteingangListPage;
import org.projectforge.web.scripting.ScriptEditPage;
import org.projectforge.web.scripting.ScriptListPage;
import org.projectforge.web.statistics.PersonalStatisticsPage;
import org.projectforge.web.statistics.SystemStatisticsPage;
import org.projectforge.web.task.TaskEditPage;
import org.projectforge.web.task.TaskListPage;
import org.projectforge.web.task.TaskTreePage;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.user.ChangePasswordPage;
import org.projectforge.web.user.GroupEditPage;
import org.projectforge.web.user.GroupListPage;
import org.projectforge.web.user.MyAccountEditPage;
import org.projectforge.web.user.UserEditPage;
import org.projectforge.web.user.UserListPage;
import org.projectforge.web.user.UserPrefEditPage;
import org.projectforge.web.user.UserPrefListPage;
import org.projectforge.web.wicket.ErrorPage;
import org.projectforge.web.wicket.FeedbackPage;
import org.projectforge.web.wicket.IListPageColumnsCreator;

/**
 * Registry for dao's. Here you can register additional daos and plugins (extensions of ProjectForge).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class WebRegistry
{
  public static final String BOOKMARK_LOGIN = "login";

  private static final String BOOKMARK_MOBILE_PREFIX = "m-";

  public static final String BOOKMARK_MOBILE_LOGIN = BOOKMARK_MOBILE_PREFIX + BOOKMARK_LOGIN;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WebRegistry.class);

  private static final WebRegistry instance = new WebRegistry();

  private Map<String, WebRegistryEntry> map = new HashMap<String, WebRegistryEntry>();

  private List<WebRegistryEntry> orderedList = new ArrayList<WebRegistryEntry>();

  private Map<String, Class< ? extends WebPage>> mountPages = new HashMap<String, Class< ? extends WebPage>>();

  public static WebRegistry instance()
  {
    return instance;
  }

  /**
   * Creates a new WebRegistryEntry and registers it. Id must be found in {@link Registry}.
   * @param id
   */
  public WebRegistryEntry register(final String id)
  {
    return register(new WebRegistryEntry(id));
  }

  /**
   * Creates a new WebRegistryEntry and registers it. Id must be found in {@link Registry}.
   * @param id
   */
  public WebRegistryEntry register(final String id, final Class< ? extends IListPageColumnsCreator< ? >> listPageColumnsCreatorClass)
  {
    return register(new WebRegistryEntry(id, listPageColumnsCreatorClass));
  }

  public WebRegistryEntry register(final WebRegistryEntry entry)
  {
    Validate.notNull(entry);
    map.put(entry.getId(), entry);
    orderedList.add(entry);
    return entry;
  }

  /**
   * Creates a new WebRegistryEntry and registers it. Id must be found in {@link Registry}.
   * @param id
   */
  public WebRegistryEntry register(final String id, final boolean insertBefore, final WebRegistryEntry entry)
  {
    return register(new WebRegistryEntry(id), insertBefore, entry);
  }

  /**
   * Creates a new WebRegistryEntry and registers it. Id must be found in {@link Registry}.
   * @param id
   */
  public WebRegistryEntry register(final String id, final Class< ? extends IListPageColumnsCreator< ? >> listPageColumnsCreatorClass,
      final boolean insertBefore, final WebRegistryEntry entry)
  {
    return register(new WebRegistryEntry(id, listPageColumnsCreatorClass), insertBefore, entry);
  }

  public WebRegistryEntry register(final WebRegistryEntry existingEntry, final boolean insertBefore, final WebRegistryEntry entry)
  {
    Validate.notNull(existingEntry);
    Validate.notNull(entry);
    map.put(entry.getId(), entry);
    int idx = orderedList.indexOf(existingEntry);
    if (idx < 0) {
      log.error("Registry entry '" + existingEntry.getId() + "' not found. Appending the given entry to the list.");
      orderedList.add(entry);
    } else if (insertBefore == true) {
      orderedList.add(idx, entry);
    } else {
      orderedList.add(idx + 1, entry);
    }
    return entry;
  }

  public WebRegistryEntry getEntry(final String id)
  {
    return map.get(id);
  }

  public List<WebRegistryEntry> getOrderedList()
  {
    return orderedList;
  }

  public BaseDao< ? > getDao(final String id)
  {
    final WebRegistryEntry entry = getEntry(id);
    return entry != null ? entry.getDao() : null;
  }

  public WebRegistry addMountPage(final String mountPage, final Class< ? extends WebPage> pageClass)
  {
    this.mountPages.put(mountPage, pageClass);
    return this;
  }

  public WebRegistry addMountPages(final String mountPageBasename, final Class< ? extends WebPage> pageListClass,
      final Class< ? extends WebPage> pageEditClass)
  {
    addMountPage(mountPageBasename + "List", pageListClass);
    addMountPage(mountPageBasename + "Edit", pageEditClass);
    return this;
  }

  public Map<String, Class< ? extends WebPage>> getMountPages()
  {
    return mountPages;
  }

  private WebRegistry()
  {
    register(DaoRegistry.ADDRESS, AddressListPage.class);
    addMountPages(DaoRegistry.ADDRESS, AddressListPage.class, AddressEditPage.class);
    addMountPage(DaoRegistry.ADDRESS + "View", AddressViewPage.class);
    register(DaoRegistry.TIMESHEET, TimesheetListPage.class);
    addMountPages(DaoRegistry.TIMESHEET, TimesheetListPage.class, TimesheetEditPage.class);
    register(DaoRegistry.TASK, TaskListPage.class);
    addMountPages(DaoRegistry.TASK, TaskListPage.class, TaskEditPage.class);
    register(DaoRegistry.BOOK, BookListPage.class);
    addMountPages(DaoRegistry.BOOK, BookListPage.class, BookEditPage.class);
    register(DaoRegistry.OUTGOING_INVOICE, RechnungListPage.class);
    addMountPages(DaoRegistry.OUTGOING_INVOICE, RechnungListPage.class, RechnungEditPage.class);
    register(DaoRegistry.INCOMING_INVOICE, EingangsrechnungListPage.class);
    addMountPages(DaoRegistry.INCOMING_INVOICE, EingangsrechnungListPage.class, EingangsrechnungEditPage.class);
    register(DaoRegistry.USER, UserListPage.class);
    addMountPages(DaoRegistry.USER, UserListPage.class, UserEditPage.class);
    register(DaoRegistry.GROUP, GroupListPage.class);
    addMountPages(DaoRegistry.GROUP, GroupListPage.class, GroupEditPage.class);
    register(DaoRegistry.ACCESS, AccessListPage.class);
    addMountPages(DaoRegistry.ACCESS, AccessListPage.class, AccessEditPage.class);
    register(DaoRegistry.ACCOUNTING_RECORD, AccountingRecordListPage.class);
    addMountPages(DaoRegistry.ACCOUNTING_RECORD, AccountingRecordListPage.class, AccountingRecordEditPage.class);
    register(DaoRegistry.COST1, Kost1ListPage.class);
    addMountPages(DaoRegistry.COST1, Kost1ListPage.class, Kost1EditPage.class);
    register(DaoRegistry.COST2, Kost2ListPage.class);
    addMountPages(DaoRegistry.COST2, Kost2ListPage.class, Kost2EditPage.class);
    register(DaoRegistry.COST2_Type, Kost2ArtListPage.class);
    addMountPages(DaoRegistry.COST2_Type, Kost2ArtListPage.class, Kost2ArtEditPage.class);
    register(DaoRegistry.ACCOUNT, KontoListPage.class);
    addMountPages(DaoRegistry.ACCOUNT, KontoListPage.class, KontoEditPage.class);
    register(DaoRegistry.CUSTOMER);// TODO: , KundeListPage.class);
    addMountPages(DaoRegistry.CUSTOMER, CustomerListPage.class, CustomerEditPage.class);
    register(DaoRegistry.PROJECT, ProjektListPage.class);
    addMountPages(DaoRegistry.PROJECT, ProjektListPage.class, ProjektEditPage.class);
    // register(DaoRegistry.ORDERBOOK, AuftragListPage.class);
    addMountPages(DaoRegistry.ORDERBOOK, AuftragListPage.class, AuftragEditPage.class);

    addMountPages("bankAccount", BankAccountListPage.class, BankAccountEditPage.class);
    addMountPages("contract", ContractListPage.class, ContractEditPage.class);
    addMountPages("employee", EmployeeListPage.class, EmployeeEditPage.class);
    addMountPages("employeeSalary", EmployeeSalaryListPage.class, EmployeeSalaryEditPage.class);
    addMountPages("ganttChart", GanttChartListPage.class, GanttChartEditPage.class);
    addMountPages("hrPlanning", HRPlanningListPage.class, HRPlanningEditPage.class);
    addMountPage("hrList", HRListPage.class);
    addMountPages("meb", MebListPage.class, MebEditPage.class);
    addMountPages("postausgang", PostausgangListPage.class, PostausgangEditPage.class);
    addMountPages("posteingang", PosteingangListPage.class, PosteingangEditPage.class);
    addMountPages("script", ScriptListPage.class, ScriptEditPage.class);
    addMountPages("userPref", UserPrefListPage.class, UserPrefEditPage.class);

    addMountPage("admin", AdminPage.class);
    addMountPage("imageCropper", ImageCropperPage.class);
    addMountPage("calendar", CalendarPage.class);
    addMountPage("changePassword", ChangePasswordPage.class);
    addMountPage("configuration", ConfigurationListPage.class);
    addMountPage("datevImport", DatevImportPage.class);
    addMountPage("doc", DocumentationPage.class);
    addMountPage("error", ErrorPage.class);
    addMountPage("feedback", FeedbackPage.class);
    addMountPage("gwikiContainer", GWikiContainerPage.class);
    addMountPage(BOOKMARK_LOGIN, LoginPage.class);
    addMountPage("myAccount", MyAccountEditPage.class);
    addMountPage("personalStatistics", PersonalStatisticsPage.class);
    addMountPage("phoneCall", PhoneCallPage.class);
    addMountPage("reportObjectives", ReportObjectivesPage.class);
    addMountPage("search", SearchPage.class);
    addMountPage("sendSms", SendSmsPage.class);
    addMountPage("setup", SetupPage.class);
    addMountPage("systemStatistics", SystemStatisticsPage.class);
    addMountPage("systemUpdate", SystemUpdatePage.class);
    addMountPage("taskTree", TaskTreePage.class);
    addMountPage("tutorial", TutorialPage.class);

    addMountPages(BOOKMARK_MOBILE_PREFIX + "address", AddressMobileListPage.class, AddressMobileEditPage.class);

    addMountPage(BOOKMARK_MOBILE_LOGIN, LoginMobilePage.class);
    addMountPage(BOOKMARK_MOBILE_PREFIX + "menu", MenuMobilePage.class);
    addMountPage(BOOKMARK_MOBILE_PREFIX + "addressView", AddressMobileViewPage.class);
  }
}
