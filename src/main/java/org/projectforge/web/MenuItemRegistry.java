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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.projectforge.core.Configuration;
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
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.access.AccessListPage;
import org.projectforge.web.address.AddressListPage;
import org.projectforge.web.address.PhoneCallPage;
import org.projectforge.web.admin.AdminPage;
import org.projectforge.web.admin.SetupPage;
import org.projectforge.web.admin.SystemUpdatePage;
import org.projectforge.web.book.BookListPage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.core.ConfigurationListPage;
import org.projectforge.web.core.SearchPage;
import org.projectforge.web.doc.DocumentationPage;
import org.projectforge.web.fibu.AccountingRecordListPage;
import org.projectforge.web.fibu.AuftragListPage;
import org.projectforge.web.fibu.BankAccountListPage;
import org.projectforge.web.fibu.CustomerListPage;
import org.projectforge.web.fibu.DatevImportPage;
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
import org.projectforge.web.fibu.ReportObjectivesPage;
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

/**
 * The menu is build from the menu items which are registered in this registry. The order of the menu entries is defined by the order number
 * of the menu item definitions.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MenuItemRegistry
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MenuItemRegistry.class);

  private List<MenuItemDef> registry = new ArrayList<MenuItemDef>();

  private Configuration configuration = Configuration.getInstance();

  private static final MenuItemRegistry instance = new MenuItemRegistry();

  public static MenuItemRegistry instance()
  {
    return instance;
  }

  public MenuItemDef get(final String id)
  {
    for (final MenuItemDef entry : registry) {
      if (id.equals(entry.getId()) == true) {
        return entry;
      }
    }
    return null;
  }

  public MenuItemDef get(final MenuItemDefId id)
  {
    return get(id.getId());
  }

  public List<MenuItemDef> getMenuItemList()
  {
    return registry;
  }

  /**
   * Registers menu entry definition. It's important that a parent menu entry item definition is registered before its sub menu entry items.
   * @param menuItemDef
   * @return
   */
  public MenuItemDef register(final MenuItemDef menuItemDef)
  {
    final MenuEntryConfig root = configuration.getMenuConfig();
    if (root != null) {
      final MenuEntryConfig entry = root.findMenuEntry(menuItemDef); //
      if (entry != null) {
        if (entry.isVisible() != menuItemDef.isVisible()) {
          log.info("Menu item's visibility changed by config.xml for item '" + menuItemDef.getId() + "'.");
          menuItemDef.setVisible(entry.isVisible());
        }
      }
    }
    registry.add(menuItemDef);
    return menuItemDef;
  }

  private MenuItemDef register(final MenuItemDef parent, final MenuItemDefId defId, final int orderNumber,
      final ProjectForgeGroup... visibleForGroups)
  {
    return register(new MenuItemDef(parent, defId.getId(), orderNumber, defId.getI18nKey(), visibleForGroups));
  }

  private MenuItemDef register(final MenuItemDef parent, final MenuItemDefId defId, final int orderNumber,
      final Class< ? extends Page> pageClass, final UserRightId requiredRightId, final UserRightValue... requiredRightValues)
  {
    return register(new MenuItemDef(parent, defId.getId(), orderNumber, defId.getI18nKey(), pageClass, null, requiredRightId,
        requiredRightValues));
  }

  private MenuItemDef register(final MenuItemDef parent, final MenuItemDefId defId, final int orderNumber,
      final Class< ? extends Page> pageClass, final ProjectForgeGroup... visibleForGroups)
  {
    return register(parent, defId, orderNumber, pageClass, null, visibleForGroups);
  }

  private MenuItemDef register(final MenuItemDef parent, final MenuItemDefId defId, final int orderNumber,
      final Class< ? extends Page> pageClass, final String[] params, final ProjectForgeGroup... visibleForGroups)
  {
    return register(new MenuItemDef(parent, defId.getId(), orderNumber, defId.getI18nKey(), pageClass, params, visibleForGroups));
  }

  private MenuItemRegistry()
  {
    initialize(this);
  }

  // Needed as static method (because anonymous declared MenuItemDef are serialized).
  @SuppressWarnings("serial")
  private static void initialize(final MenuItemRegistry reg)
  {
    final Configuration configuration = Configuration.getInstance();
    // Super menus
    final MenuItemDef common = reg.register(null, MenuItemDefId.COMMON, 10);
    final MenuItemDef pm = reg.register(null, MenuItemDefId.PROJECT_MANAGEMENT, 20);
    final ProjectForgeGroup[] fibuGroups = new ProjectForgeGroup[] { FINANCE_GROUP, ORGA_TEAM, CONTROLLING_GROUP};
    final MenuItemDef fibu = reg.register(null, MenuItemDefId.FIBU, 30, fibuGroups);
    final MenuItemDef cost = reg.register(null, MenuItemDefId.COST, 40, FINANCE_GROUP, ORGA_TEAM, CONTROLLING_GROUP);
    final MenuItemDef reporting = reg.register(null, MenuItemDefId.REPORTING, 50, FINANCE_GROUP, CONTROLLING_GROUP);
    final MenuItemDef orga = reg.register(null, MenuItemDefId.ORGA, 60, FINANCE_GROUP, CONTROLLING_GROUP, ORGA_TEAM);
    final MenuItemDef admin = reg.register(null, MenuItemDefId.ADMINISTRATION, 70);
    final MenuItemDef misc = reg.register(null, MenuItemDefId.MISC, 100);

    // Menu entries
    // COMMON
    reg.register(common, MenuItemDefId.CALENDAR, 10, CalendarPage.class); // Visible for all.
    if (configuration.isBookManagementConfigured() == true) {
      reg.register(common, MenuItemDefId.BOOK_LIST, 20, BookListPage.class); // Visible for all.
    }
    if (configuration.isAddressManagementConfigured() == true) {
      reg.register(common, MenuItemDefId.ADDRESS_LIST, 30, AddressListPage.class); // Visible for all.
    }
    if (StringUtils.isNotEmpty(configuration.getTelephoneSystemUrl()) == true) {
      reg.register(common, MenuItemDefId.PHONE_CALL, 40, PhoneCallPage.class);
    }
    if (configuration.isMebConfigured() == true) {
      final MenuItemDef meb = new MenuItemDef(common, MenuItemDefId.MEB.getId(), 50, MenuItemDefId.MEB.getI18nKey(), MebListPage.class) {
        @Override
        protected void afterMenuEntryCreation(final MenuEntry createdMenuEntry, final MenuBuilderContext context)
        {
          createdMenuEntry.setNewCounterModel(new MenuNewCounterMeb());
        }
      };
      reg.register(meb);
    }
    reg.register(common, MenuItemDefId.SEARCH, 100, SearchPage.class);

    // PROJECT_MANAGEMENT
    reg.register(pm, MenuItemDefId.TASK_TREE, 10, TaskTreePage.class);
    reg.register(pm, MenuItemDefId.TIMESHEET_LIST, 20, TimesheetListPage.class);
    reg.register(pm, MenuItemDefId.MONTHLY_EMPLOYEE_REPORT, 30, MonthlyEmployeeReportPage.class);
    reg.register(pm, MenuItemDefId.PERSONAL_STATISTICS, 40, PersonalStatisticsPage.class);
    reg.register(pm, MenuItemDefId.HR_VIEW, 50, HRListPage.class, HRPlanningDao.USER_RIGHT_ID, READONLY_READWRITE);
    reg.register(pm, MenuItemDefId.HR_PLANNING_LIST, 60, HRPlanningListPage.class);
    reg.register(pm, MenuItemDefId.GANTT, 70, GanttChartListPage.class);
    // Order book 80 (if user isn't member of FIBU groups.
    // Projects 90 (if user isn't member of FIBU groups.

    // FIBU
    reg.register(fibu, MenuItemDefId.OUTGOING_INVOICE_LIST, 10, RechnungListPage.class, RechnungDao.USER_RIGHT_ID, READONLY_READWRITE);
    reg.register(fibu, MenuItemDefId.INCOMING_INVOICE_LIST, 20, EingangsrechnungListPage.class, EingangsrechnungDao.USER_RIGHT_ID,
        READONLY_READWRITE);
    reg.register(fibu, MenuItemDefId.BANK_ACCOUNT_LIST, 30, BankAccountListPage.class, FINANCE_GROUP, CONTROLLING_GROUP);
    reg.register(fibu, MenuItemDefId.CUSTOMER_LIST, 40, CustomerListPage.class, FINANCE_GROUP, CONTROLLING_GROUP);
    final MenuItemDef projects = new MenuItemDef(fibu, MenuItemDefId.PROJECT_LIST.getId(), 50, MenuItemDefId.PROJECT_LIST.getI18nKey(),
        ProjektListPage.class, ProjektDao.USER_RIGHT_ID, READONLY_READWRITE) {
      @Override
      protected void afterMenuEntryCreation(final MenuEntry createdMenuEntry, final MenuBuilderContext context)
      {
        if (context.getAccessChecker().isUserMemberOfGroup(fibuGroups) == false) {
          // Setting project management as parent because fibu isn't visible for this user:
          createdMenuEntry.setParent(context.getMenu(), pm.getId());
        }
      }
    };
    reg.register(projects);
    reg.register(fibu, MenuItemDefId.EMPLOYEE_LIST, 60, EmployeeListPage.class, FINANCE_GROUP, CONTROLLING_GROUP);
    reg.register(fibu, MenuItemDefId.EMPLOYEE_SALARY_LIST, 70, EmployeeSalaryListPage.class, EmployeeSalaryDao.USER_RIGHT_ID,
        READONLY_READWRITE);
    final MenuItemDef orderBook = new MenuItemDef(fibu, MenuItemDefId.ORDER_LIST.getId(), 80, MenuItemDefId.ORDER_LIST.getI18nKey(),
        AuftragListPage.class, AuftragDao.USER_RIGHT_ID, READONLY_PARTLYREADWRITE_READWRITE) {
      @Override
      protected void afterMenuEntryCreation(final MenuEntry createdMenuEntry, final MenuBuilderContext context)
      {
        if (context.getAccessChecker().isUserMemberOfGroup(fibuGroups) == true) {
          createdMenuEntry.setNewCounterModel(new MenuNewCounterOrder());
          createdMenuEntry.setNewCounterTooltip("menu.fibu.orderbook.htmlSuffixTooltip");
        } else {
          // Setting project management as parent because fibu isn't visible for this user:
          createdMenuEntry.setParent(context.getMenu(), pm.getId());
        }
      }
    };
    reg.register(orderBook);

    // COST
    reg.register(cost, MenuItemDefId.ACCOUNT_LIST, 10, KontoListPage.class, FINANCE_GROUP, CONTROLLING_GROUP);
    reg.register(cost, MenuItemDefId.COST1_LIST, 20, Kost1ListPage.class, Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE);
    reg.register(cost, MenuItemDefId.COST2_LIST, 30, Kost2ListPage.class, Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE);
    reg.register(cost, MenuItemDefId.COST2_TYPE_LIST, 40, Kost2ArtListPage.class, Kost2Dao.USER_RIGHT_ID, READONLY_READWRITE);

    // REPORTING
    reg.register(reporting, MenuItemDefId.ACCOUNTING_RECORD_LIST, 10, AccountingRecordListPage.class, FINANCE_GROUP, CONTROLLING_GROUP);
    reg.register(reporting, MenuItemDefId.REPORT_OBJECTIVES, 20, ReportObjectivesPage.class, FINANCE_GROUP, CONTROLLING_GROUP);
    reg.register(reporting, MenuItemDefId.SCRIPTING, 30, ReportScriptingPage.class, FINANCE_GROUP, CONTROLLING_GROUP);
    reg.register(reporting, MenuItemDefId.SCRIPT_LIST, 40, ScriptListPage.class, FINANCE_GROUP, CONTROLLING_GROUP);
    reg.register(reporting, MenuItemDefId.DATEV_IMPORT, 50, DatevImportPage.class, DatevImportDao.USER_RIGHT_ID, UserRightValue.TRUE);

    // ORGA
    reg.register(orga, MenuItemDefId.OUTBOX_LIST, 10, PostausgangListPage.class, PostausgangDao.USER_RIGHT_ID, READONLY_READWRITE);
    reg.register(orga, MenuItemDefId.INBOX_LIST, 20, PosteingangListPage.class, PosteingangDao.USER_RIGHT_ID, READONLY_READWRITE);
    if (CollectionUtils.isNotEmpty(configuration.getContractTypes()) == true) {
      reg.register(orga, MenuItemDefId.CONTRACTS, 30, ContractListPage.class, ContractDao.USER_RIGHT_ID, READONLY_READWRITE);
    }

    // ADMINISTRATION
    reg.register(admin, MenuItemDefId.MY_ACCOUNT, 10, MyAccountEditPage.class);
    reg.register(admin, MenuItemDefId.MY_PREFERENCES, 20, UserPrefListPage.class);
    reg.register(admin, MenuItemDefId.CHANGE_PASSWORD, 30, ChangePasswordPage.class); // Visible for all.
    reg.register(admin, MenuItemDefId.USER_LIST, 40, UserListPage.class);
    reg.register(admin, MenuItemDefId.GROUP_LIST, 50, GroupListPage.class); // Visible for all.
    reg.register(admin, MenuItemDefId.ACCESS_LIST, 60, AccessListPage.class); // Visible for all.
    reg.register(admin, MenuItemDefId.SYSTEM, 70, AdminPage.class, ADMIN_GROUP);
    reg.register(admin, MenuItemDefId.SYSTEM_UPDATE, 80, SystemUpdatePage.class, ADMIN_GROUP);
    reg.register(admin, MenuItemDefId.SYSTEM_STATISTICS, 90, SystemStatisticsPage.class);
    reg.register(admin, MenuItemDefId.CONFIGURATION, 100, ConfigurationListPage.class, ADMIN_GROUP);
    final MenuItemDef firstLogin = new MenuItemDef(admin, MenuItemDefId.SYSTEM_FIRST_LOGIN_SETUP_PAGE.getId(), 200,
        MenuItemDefId.SYSTEM_FIRST_LOGIN_SETUP_PAGE.getI18nKey(), SetupPage.class) {
      @Override
      protected boolean isVisible(final MenuBuilderContext context)
      {
        return LoginPage.FIRST_PSEUDO_SETUP_USER.equals(context.getLoggedInUser().getUsername());
      }
    };
    reg.register(firstLogin);

    // MISC
    final MenuItemDef imageCropper = new MenuItemDef(misc, MenuItemDefId.IMAGE_CROPPER.getId(), 100, MenuItemDefId.IMAGE_CROPPER
        .getI18nKey(), ImageCropperPage.class, new String[] { ImageCropperPage.PARAM_SHOW_UPLOAD_BUTTON, "false",
        ImageCropperPage.PARAM_ENABLE_WHITEBOARD_FILTER, "true"});
    imageCropper.setVisible(false); // invisible at default (because it's only functioning with valid ssl certificate).
    reg.register(imageCropper);
    if (WicketApplication.isDevelopmentModus() == true) {
      reg.register(misc, MenuItemDefId.GWIKI, 110, GWikiContainerPage.class);
    }
    reg.register(misc, MenuItemDefId.DOCUMENTATION, 200, DocumentationPage.class);
  }
}
