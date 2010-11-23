/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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
import org.projectforge.web.admin.AdminPage;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.admin.UpdatePage;
import org.projectforge.web.book.BookListPage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.core.ConfigurationListPage;
import org.projectforge.web.fibu.AuftragListPage;
import org.projectforge.web.fibu.BankAccountListPage;
import org.projectforge.web.fibu.EingangsrechnungListPage;
import org.projectforge.web.fibu.Kost1ListPage;
import org.projectforge.web.fibu.Kost2ArtListPage;
import org.projectforge.web.fibu.Kost2ListPage;
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
import org.projectforge.web.scripting.ScriptListPage;
import org.projectforge.web.statistics.PersonalStatisticsPage;
import org.projectforge.web.statistics.SystemStatisticsPage;
import org.projectforge.web.task.TaskTreePage;
import org.projectforge.web.timesheet.TimesheetListPage;
import org.projectforge.web.user.GroupListPage;
import org.projectforge.web.user.MyAccountEditPage;
import org.projectforge.web.user.UserListPage;
import org.projectforge.web.user.UserPrefListPage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketApplication;

public enum MenuItemDef
{
  // Super menus
  ADMINISTRATION("administration", "bricks.png"), //
  COMMON("common", "cog.png"), //
  // *
  DEVELOPER_DOC("developer", "cog.png"), //
  // *
  DOCUMENTATION("documentation", "information.png"), //
  FIBU("fibu", "chart_line.png", //
      FINANCE_GROUP, ORGA_TEAM, CONTROLLING_GROUP), //
  KOST("fibu.kost", "coins.png", //
      FINANCE_GROUP, ORGA_TEAM, CONTROLLING_GROUP), //
  MISC("misc", "cog.png"), //
  ORGA("orga", "attach.png", //
      FINANCE_GROUP, CONTROLLING_GROUP, ORGA_TEAM), //
  PROJECT_MANAGEMENT("projectmanagement", "wand.png"), //
  // *
  REPORTING("reporting", "chart_line.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //

  // Menu entries
  ACCESS_LIST("accessList", AccessListPage.class, "lock.png"), // Visible for all.
  // *
  ADDRESS_LIST("addressList", AddressListPage.class, "vcard.png"), // Visible for all.
  // ADDRESS_LIST("addressList", "address/AddressList.action", "vcard.png"), // Visible for all.
  // *
  BANK_ACCOUNT_LIST("finance.bankAccounts", BankAccountListPage.class, "money.png",//
      FINANCE_GROUP, CONTROLLING_GROUP), //
  BOOK_LIST("bookList", BookListPage.class, "book_open.png"), // Visible for all.
  // *
  BUCHUNG_SATZ_LIST("fibu.buchungssaetze", "fibu/BuchungssatzList.action", "script.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  CALENDAR("calendar", CalendarPage.class, "calendar.png"), // Visible for all.
  // *
  CHANGE_PASSWORD("changePassword", "user/ChangePassword.action", "key.png"), // Visible for all.
  // *
  CONFIGURATION("configuration", ConfigurationListPage.class, "cog.png", //
      ADMIN_GROUP), //
  CONTRACTS("contracts", ContractListPage.class, "script.png", //
      ContractDao.USER_RIGHT_ID, READONLY_READWRITE), //
  DATEV_IMPORT("fibu.datevImport", "fibu/datevImport.action", "database_refresh.png", //
      DatevImportDao.USER_RIGHT_ID, UserRightValue.TRUE), //
  EINGANGS_RECHNUNG_LIST("fibu.eingangsrechnungen", EingangsrechnungListPage.class, "cart.png", //
      EingangsrechnungDao.USER_RIGHT_ID, READONLY_READWRITE), //
  EMPLOYEE_LIST("fibu.employees", "fibu/EmployeeList.action", "user.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  EMPLOYEE_SALARY_LIST("fibu.employeeSalaries", "fibu/EmployeeSalaryList.action", "money.png", //
      EmployeeSalaryDao.USER_RIGHT_ID, READONLY_READWRITE), //
  GANTT("gantt", GanttChartListPage.class, "gantt.png"), //
  GROUP_LIST("groupList", GroupListPage.class, "group.png"), // Visible for all.
  // *
  GWIKI("gwiki", GWikiContainerPage.class, "script_gear.png"), //
  // *
  HR_PLANNING_LIST("hrPlanningList", HRPlanningListPage.class, "group.png"), //
  HR_VIEW("hrList", HRListPage.class, "group.png", //
      HRPlanningDao.USER_RIGHT_ID, READONLY_READWRITE), //

  IMAGE_CROPPER("imageCropper", ImageCropperPage.class, "images.png", new String[] { ImageCropperPage.PARAM_SHOW_UPLOAD_BUTTON, "false",
      ImageCropperPage.PARAM_ENABLE_WHITEBOARD_FILTER, "true"}), //
  // *
  KONTO_LIST("fibu.konten", "fibu/KontoList.action", "money.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  KOST_ZUWEISUNG_LIST("fibu.kostZuweisungen", "fibu/KostZuweisungList.action", "coins.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  KOST1_LIST("fibu.kost1", Kost1ListPage.class, "coins.png", //
      Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE), //
  KOST2_LIST("fibu.kost2", Kost2ListPage.class, "coins.png", //
      Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE), //
  KOST2_ART_LIST("fibu.kost2arten", Kost2ArtListPage.class, "kost2art.png", //
      Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE), //
  LOGIN("login", "Login.action", "door_in.png"), //
  // *
  LOGOUT("logout", "Login.action?logout=true", "door_out.png"), //
  // *
  KUNDE_LIST("fibu.kunden", "fibu/KundeList.action", "customer.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  MEB("meb", MebListPage.class, "meb.png"), //
  MONTHLY_EMPLOYEE_REPORT("monthlyEmployeeReport", "fibu/MonthlyEmployeeReport.action", "report_user.png"), //
  // *
  MY_ACCOUNT("myAccount", MyAccountEditPage.class, "brick.png"), //
  // *
  MY_PREFERENCES("myPreferences", UserPrefListPage.class, "brick.png"), //
  // *
  ORDER_LIST("fibu.orderbook", AuftragListPage.class, "report_magnify.png", //
      AuftragDao.USER_RIGHT_ID, READONLY_PARTLYREADWRITE_READWRITE), //
  PHONE_CALL("phoneCall", "address/PhoneCall.action", "telephone.png"), //
  // *
  PERSONAL_STATISTICS("personalStatistics", PersonalStatisticsPage.class, "chart_line.png"), //
  // *
  POSTAUSGANG_LIST("orga.postausgang", PostausgangListPage.class, "mail_out.png", //
      PostausgangDao.USER_RIGHT_ID, READONLY_READWRITE), //
  POSTEINGANG_LIST("orga.posteingang", PosteingangListPage.class, "mail_open.png", //
      PosteingangDao.USER_RIGHT_ID, READONLY_READWRITE), //
  PROJEKT_LIST("fibu.projekte", ProjektListPage.class, "projekt.png", //
      ProjektDao.USER_RIGHT_ID, READONLY_READWRITE), //
  RECHNUNG_LIST("fibu.rechnungen", RechnungListPage.class, "calculator.png", //
      RechnungDao.USER_RIGHT_ID, READONLY_READWRITE), //
  REPORT_OBJECTIVES("fibu.reporting.reportObjectives", "fibu/ReportObjectives.action", "chart_line.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  REPORT_SCRIPTING("fibu.reporting.reportScripting", "fibu/ReportScripting.action", "chart_line.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  SCRIPT_LIST("fibu.scripting", ScriptListPage.class, "chart_line.png", //
      FINANCE_GROUP, CONTROLLING_GROUP), //
  SEARCH("search", "Search.action?reset=true", "zoom.png"), //
  SYSTEM("system", AdminPage.class, "cog.png", ADMIN_GROUP), //
  // *
  SYSTEM_FIRST_LOGIN_SETUP_PAGE("system.firstLoginSetup", SetupPage.class, "cog.png"), //
  SYSTEM_STATISTICS("systemStatistics", SystemStatisticsPage.class, "chart_line.png"), //
  SYSTEM_UPDATE("systemUpdate", UpdatePage.class, "cog.png", //
      ADMIN_GROUP), //
  TASK_TREE("taskTree", TaskTreePage.class, "chart_organisation.png"), //
  // *
  TIMESHEET_LIST("timesheetList", TimesheetListPage.class, "clock.png"), //
  // *
  USER_LIST("userList", UserListPage.class, "user.png"), //
  // *

  // Documentation
  PROJECTFORGE_DOC("doc.projectforge", "doc/ProjectForge.html", "projectforge.png", true), //
  USER_GUIDE("userGuide", "doc/Handbuch.html", "report_user.png", true), //
  // *
  FAQ("faq", "doc/FAQ.html", WebConstants.IMAGE_HELP, true), //
  FAQ_DE("faq", "doc/FAQ_de.html", WebConstants.IMAGE_HELP, true), //
  // *
  LICENSE("license", "LICENSE.txt", "script_lightning.png", true), //
  // *
  PROJECT_DOC("projectDocumentation", "site/index.html", "page.gif", true), //
  // *
  ADMIN_LOGBUCH("adminLogbuch", "doc/AdminLogbuch.html", "keyboard.png", true), //
  // *
  ADMIN_GUIDE("adminGuide", "doc/AdministrationGuide.html", "report_disk.png", true), //
  // *
  DEVELOPER_GUIDE("developerGuide", "doc/DeveloperGuide.html", "page_white_cup.png", true), //
  // *
  JAVA_DOC("javaDoc", "site/apidocs/index.html", "cup.png", true), //
  // *
  TEST_REPORTS("testReports", "site/surefire-report.html", "server_chart.png", true), //
  // *
  NEWS("news", "doc/News.html", "newspaper.png", true);
  // *

  private String id;

  private Class< ? extends Page> pageClass;

  private String url;

  private String[] params;

  private String icon;

  private boolean newWindow;

  private ProjectForgeGroup[] visibleForGroups;

  private UserRightId requiredRightId;

  private UserRightValue[] requiredRightValues;

  MenuItemDef(final String id, final Class< ? extends Page> pageClass, final String icon, final UserRightId requiredRightId,
      final UserRightValue... requiredRightValues)
  {
    this(id, pageClass, icon, null, requiredRightId, requiredRightValues);
  }

  MenuItemDef(final String id, final Class< ? extends Page> pageClass, final String icon, final String[] params,
      final UserRightId requiredRightId, final UserRightValue... requiredRightValues)
  {
    this.id = id;
    this.pageClass = pageClass;
    this.icon = icon;
    this.params = params;
    this.requiredRightId = requiredRightId;
    this.requiredRightValues = requiredRightValues;
  }

  MenuItemDef(final String id, final Class< ? extends Page> pageClass, final String icon, final ProjectForgeGroup... visibleForGroups)
  {
    this(id, pageClass, icon, null, visibleForGroups);
  }

  MenuItemDef(final String id, final Class< ? extends Page> pageClass, final String icon, final String[] params,
      final ProjectForgeGroup... visibleForGroups)
  {
    this.id = id;
    this.pageClass = pageClass;
    this.icon = icon;
    this.params = params;
    this.visibleForGroups = visibleForGroups;
  }

  MenuItemDef(final String id, final String url, final String icon, final ProjectForgeGroup... visibleForGroups)
  {
    this(id, url, icon, false, visibleForGroups);
  }

  MenuItemDef(final String id, final String url, final String icon, boolean newWindow, final ProjectForgeGroup... visibleForGroups)
  {
    this.id = id;
    this.url = url;
    this.icon = icon;
    this.newWindow = newWindow;
    this.visibleForGroups = visibleForGroups;
  }

  MenuItemDef(final String id, final String icon, final ProjectForgeGroup... visibleForGroups)
  {
    this.id = id;
    this.icon = icon;
    this.visibleForGroups = visibleForGroups;
  }

  MenuItemDef(final String id, final String url, final String icon, final UserRightId requiredRightId,
      final UserRightValue... requiredRightValues)
  {
    this(id, url, icon, false, requiredRightId, requiredRightValues);
  }

  MenuItemDef(final String id, final String url, final String icon, boolean newWindow, final UserRightId requiredRightId,
      final UserRightValue... requiredRightValues)
  {
    this.id = id;
    this.url = url;
    this.icon = icon;
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

  /**
   * @return The icon name (excluding the path).
   */
  public String getIcon()
  {
    return icon;
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
