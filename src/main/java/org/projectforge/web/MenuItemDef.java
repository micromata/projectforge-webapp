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

package org.projectforge.web;

import static org.projectforge.user.ProjectForgeGroup.ADMIN_GROUP;
import static org.projectforge.user.ProjectForgeGroup.CONTROLLING_GROUP;
import static org.projectforge.user.ProjectForgeGroup.FINANCE_GROUP;
import static org.projectforge.user.ProjectForgeGroup.ORGA_TEAM;
import static org.projectforge.user.UserRights.READONLY_PARTLYREADWRITE_READWRITE;
import static org.projectforge.user.UserRights.READONLY_READWRITE;

import org.apache.wicket.Page;
import org.projectforge.access.AccessChecker;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.fibu.EingangsrechnungDao;
import org.projectforge.fibu.EmployeeSalaryDao;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.RechnungDao;
import org.projectforge.fibu.datev.DatevImportDao;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.humanresources.HRPlanningDao;
import org.projectforge.orga.ContractDao;
import org.projectforge.orga.PostausgangDao;
import org.projectforge.orga.PosteingangDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.access.AccessListPage;
import org.projectforge.web.address.AddressListPage;
import org.projectforge.web.address.PhoneCallPage;
import org.projectforge.web.admin.AdminPage;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.admin.UpdatePage;
import org.projectforge.web.book.BookListPage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.core.ConfigurationListPage;
import org.projectforge.web.core.SearchPage;
import org.projectforge.web.fibu.AccountingRecordListPage;
import org.projectforge.web.fibu.AuftragListPage;
import org.projectforge.web.fibu.BankAccountListPage;
import org.projectforge.web.fibu.CustomerListPage;
import org.projectforge.web.fibu.EingangsrechnungListPage;
import org.projectforge.web.fibu.EmployeeListPage;
import org.projectforge.web.fibu.EmployeeSalaryListPage;
import org.projectforge.web.fibu.KontoListPage;
import org.projectforge.web.fibu.Kost1ListPage;
import org.projectforge.web.fibu.Kost2ArtListPage;
import org.projectforge.web.fibu.Kost2ListPage;
import org.projectforge.web.fibu.MonthlyEmployeeReportPage;
import org.projectforge.web.fibu.ProjektListPage;
import org.projectforge.web.fibu.RechnungListPage;
import org.projectforge.web.gantt.GanttChartListPage;
import org.projectforge.web.gwiki.GWikiContainerPage;
import org.projectforge.web.humanresources.HRListPage;
import org.projectforge.web.humanresources.HRPlanningListPage;
import org.projectforge.web.imagecropper.ImageCropperPage;
import org.projectforge.web.meb.MebListPage;
import org.projectforge.web.orga.ContractListPage;
import org.projectforge.web.orga.PostausgangListPage;
import org.projectforge.web.orga.PosteingangListPage;
import org.projectforge.web.scripting.ReportScriptingPage;
import org.projectforge.web.scripting.ScriptListPage;
import org.projectforge.web.statistics.PersonalStatisticsPage;
import org.projectforge.web.statistics.SystemStatisticsPage;
import org.projectforge.web.task.TaskTreePage;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.user.ChangePasswordPage;
import org.projectforge.web.user.GroupListPage;
import org.projectforge.web.user.MyAccountEditPage;
import org.projectforge.web.user.UserListPage;
import org.projectforge.web.user.UserPrefListPage;
import org.projectforge.web.wicket.WicketApplication;

public enum MenuItemDef
{
  // Super menus
  ADMINISTRATION("administration"), //
  COMMON("common"), //
  // *
  DOCUMENTATION("documentation"), //
  FIBU("fibu", //
      FINANCE_GROUP, ORGA_TEAM, CONTROLLING_GROUP), //
  KOST("fibu.kost", //
      FINANCE_GROUP, ORGA_TEAM, CONTROLLING_GROUP), //
  ORGA("orga", //
      FINANCE_GROUP, CONTROLLING_GROUP, ORGA_TEAM), //
  PROJECT_MANAGEMENT("projectmanagement"), //
  // *
  REPORTING("reporting", //
      FINANCE_GROUP, CONTROLLING_GROUP), //

  // Menu entries
  ACCESS_LIST("accessList", AccessListPage.class), // Visible for all.
  // *
  ADDRESS_LIST("addressList", AddressListPage.class), // Visible for all.
  // *
  BANK_ACCOUNT_LIST("finance.bankAccounts", BankAccountListPage.class,//
      FINANCE_GROUP, CONTROLLING_GROUP), //
  BOOK_LIST("bookList", BookListPage.class), // Visible for all.
  // *
  BUCHUNG_SATZ_LIST("fibu.buchungssaetze", AccountingRecordListPage.class, //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  CALENDAR("calendar", CalendarPage.class), // Visible for all.
  // *
  CHANGE_PASSWORD("changePassword", ChangePasswordPage.class), // Visible for all.
  // *
  CONFIGURATION("configuration", ConfigurationListPage.class, //
      ADMIN_GROUP), //
  CONTRACTS("contracts", ContractListPage.class, //
      ContractDao.USER_RIGHT_ID, READONLY_READWRITE), //
  DATEV_IMPORT("fibu.datevImport", "fibu/datevImport.action", //
      DatevImportDao.USER_RIGHT_ID, UserRightValue.TRUE), //
  EINGANGS_RECHNUNG_LIST("fibu.eingangsrechnungen", EingangsrechnungListPage.class, //
      EingangsrechnungDao.USER_RIGHT_ID, READONLY_READWRITE), //
  EMPLOYEE_LIST("fibu.employees", EmployeeListPage.class, //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  EMPLOYEE_SALARY_LIST("fibu.employeeSalaries", EmployeeSalaryListPage.class, //
      EmployeeSalaryDao.USER_RIGHT_ID, READONLY_READWRITE), //
  GANTT("gantt", GanttChartListPage.class), //
  GROUP_LIST("groupList", GroupListPage.class), // Visible for all.
  // *
  GWIKI("gwiki", GWikiContainerPage.class), //
  // *
  HR_PLANNING_LIST("hrPlanningList", HRPlanningListPage.class), //
  HR_VIEW("hrList", HRListPage.class, //
      HRPlanningDao.USER_RIGHT_ID, READONLY_READWRITE), //

  IMAGE_CROPPER("imageCropper", ImageCropperPage.class, new String[] { ImageCropperPage.PARAM_SHOW_UPLOAD_BUTTON, "false",
      ImageCropperPage.PARAM_ENABLE_WHITEBOARD_FILTER, "true"}), //
  // *
  KONTO_LIST("fibu.konten", KontoListPage.class, //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  // KOST_ZUWEISUNG_LIST("fibu.kostZuweisungen", "fibu/KostZuweisungList.action", //
  //    FINANCE_GROUP, CONTROLLING_GROUP), //
  KOST1_LIST("fibu.kost1", Kost1ListPage.class, //
      Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE), //
  KOST2_LIST("fibu.kost2", Kost2ListPage.class, //
      Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE), //
  KOST2_ART_LIST("fibu.kost2arten", Kost2ArtListPage.class, //
      Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE), //
  // *
  KUNDE_LIST("fibu.kunden", CustomerListPage.class, //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  MEB("meb", MebListPage.class), //
  MONTHLY_EMPLOYEE_REPORT("monthlyEmployeeReport", MonthlyEmployeeReportPage.class), //
  // *
  MY_ACCOUNT("myAccount", MyAccountEditPage.class), //
  // *
  MY_PREFERENCES("myPreferences", UserPrefListPage.class), //
  // *
  ORDER_LIST("fibu.orderbook", AuftragListPage.class, //
      AuftragDao.USER_RIGHT_ID, READONLY_PARTLYREADWRITE_READWRITE), //
  PHONE_CALL("phoneCall", PhoneCallPage.class), //
  // *
  PERSONAL_STATISTICS("personalStatistics", PersonalStatisticsPage.class), //
  // *
  POSTAUSGANG_LIST("orga.postausgang", PostausgangListPage.class, //
      PostausgangDao.USER_RIGHT_ID, READONLY_READWRITE), //
  POSTEINGANG_LIST("orga.posteingang", PosteingangListPage.class, //
      PosteingangDao.USER_RIGHT_ID, READONLY_READWRITE), //
  PROJEKT_LIST("fibu.projekte", ProjektListPage.class, //
      ProjektDao.USER_RIGHT_ID, READONLY_READWRITE), //
  RECHNUNG_LIST("fibu.rechnungen", RechnungListPage.class, //
      RechnungDao.USER_RIGHT_ID, READONLY_READWRITE), //
  REPORT_OBJECTIVES("fibu.reporting.reportObjectives", "fibu/ReportObjectives.action", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  REPORT_SCRIPTING("fibu.reporting.reportScripting", ReportScriptingPage.class, //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  SCRIPT_LIST("fibu.scripting", ScriptListPage.class, //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  SEARCH("search", SearchPage.class), //
  SYSTEM("system", AdminPage.class, ADMIN_GROUP), //
  // *
  SYSTEM_FIRST_LOGIN_SETUP_PAGE("system.firstLoginSetup", SetupPage.class), //
  SYSTEM_STATISTICS("systemStatistics", SystemStatisticsPage.class), //
  SYSTEM_UPDATE("systemUpdate", UpdatePage.class, //
      ADMIN_GROUP), //
  TASK_TREE("taskTree", TaskTreePage.class), //
  // *
  TIMESHEET_LIST("timesheetList", TimesheetListPage.class), //
  // *
  USER_LIST("userList", UserListPage.class), //
  // *

  // Documentation
  PROJECTFORGE_DOC("doc.projectforge", "doc/ProjectForge.html", true), //
  USER_GUIDE("userGuide", "doc/Handbuch.html", true), //
  // *
  FAQ("faq", "doc/FAQ.html", true), //
  FAQ_DE("faq", "doc/FAQ_de.html", true), //
  // *
  LICENSE("license", "LICENSE.txt", true), //
  // *
  PROJECT_DOC("projectDocumentation", "site/index.html", true), //
  // *
  ADMIN_LOGBUCH("adminLogbuch", "doc/AdminLogbuch.html", true), //
  // *
  ADMIN_GUIDE("adminGuide", "doc/AdministrationGuide.html", true), //
  // *
  DEVELOPER_GUIDE("developerGuide", "doc/DeveloperGuide.html", true), //
  // *
  JAVA_DOC("javaDoc", "site/apidocs/index.html", true), //
  // *
  TEST_REPORTS("testReports", "site/surefire-report.html", true), //
  // *
  NEWS("news", "doc/News.html", true);
  // *

  private String id;

  private Class< ? extends Page> pageClass;

  private String url;

  private String[] params;

  private boolean newWindow;

  private ProjectForgeGroup[] visibleForGroups;

  private UserRightId requiredRightId;

  private UserRightValue[] requiredRightValues;

  MenuItemDef(final String id, final Class< ? extends Page> pageClass, final UserRightId requiredRightId,
      final UserRightValue... requiredRightValues)
  {
    this(id, pageClass, null, requiredRightId, requiredRightValues);
  }

  MenuItemDef(final String id, final Class< ? extends Page> pageClass, final String[] params, final UserRightId requiredRightId,
      final UserRightValue... requiredRightValues)
  {
    this.id = id;
    this.pageClass = pageClass;
    this.params = params;
    this.requiredRightId = requiredRightId;
    this.requiredRightValues = requiredRightValues;
  }

  MenuItemDef(final String id, final Class< ? extends Page> pageClass, final ProjectForgeGroup... visibleForGroups)
  {
    this(id, pageClass, null, visibleForGroups);
  }

  MenuItemDef(final String id, final Class< ? extends Page> pageClass, final String[] params, final ProjectForgeGroup... visibleForGroups)
  {
    this.id = id;
    this.pageClass = pageClass;
    this.params = params;
    this.visibleForGroups = visibleForGroups;
  }

  MenuItemDef(final String id, final String url, final ProjectForgeGroup... visibleForGroups)
  {
    this(id, url, false, visibleForGroups);
  }

  MenuItemDef(final String id, final String url, boolean newWindow, final ProjectForgeGroup... visibleForGroups)
  {
    this.id = id;
    this.url = url;
    this.newWindow = newWindow;
    this.visibleForGroups = visibleForGroups;
  }

  MenuItemDef(final String id, final ProjectForgeGroup... visibleForGroups)
  {
    this.id = id;
    this.visibleForGroups = visibleForGroups;
  }

  MenuItemDef(final String id, final String url, final UserRightId requiredRightId, final UserRightValue... requiredRightValues)
  {
    this(id, url, false, requiredRightId, requiredRightValues);
  }

  MenuItemDef(final String id, final String url, boolean newWindow, final UserRightId requiredRightId,
      final UserRightValue... requiredRightValues)
  {
    this.id = id;
    this.url = url;
    this.newWindow = newWindow;
    this.requiredRightId = requiredRightId;
    this.requiredRightValues = requiredRightValues;
  }

  /**
   * @return Id used for i18n.
   */
  public String getId()
  {
    return id;
  }

  /**
   * @return Key used in the i18n resource bundle.
   */
  public String getI18nKey()
  {
    return "menu." + id;
  }

  /**
   * @return Wicket page or null for Stripes pages.
   */
  public Class< ? extends Page> getPageClass()
  {
    return pageClass;
  }

  /**
   * @return true, if pageClass (Wicket page) is given otherwise false.
   */
  public boolean isWicketPage()
  {
    return this.pageClass != null;
  }

  /**
   * @return The url for Stripes pages (relative to "secure/") or the bookmarkable url for Wicket pages (relative to "wa/").
   */
  public String getUrl()
  {
    if (url == null) {
      // Late binding: may be this enum class was instantiated before WicketApplication was initialized.
      this.url = WicketApplication.getBookmarkableMountPath(this.pageClass);
    }
    return url;
  }

  public String[] getParams()
  {
    return params;
  }

  public boolean isNewWindow()
  {
    return newWindow;
  }

  public ProjectForgeGroup[] getVisibleForGroups()
  {
    return visibleForGroups;
  }

  public UserRightId getRequiredRightId()
  {
    return requiredRightId;
  }

  public UserRightValue[] getRequiredRightValues()
  {
    return requiredRightValues;
  }

  public boolean hasRight(final AccessChecker accessChecker, final PFUserDO loggedInUser)
  {
    if (requiredRightId == null || requiredRightValues == null) {
      // Should not occur, for security reasons deny at default.
      return false;
    }
    if (accessChecker.hasRight(requiredRightId, false, requiredRightValues) == true) {
      return true;
    }
    return false;
  }
}
