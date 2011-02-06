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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.core.Configuration;
import org.projectforge.fibu.datev.DatevImportDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.wicket.WicketApplication;

public class MenuBuilder implements Serializable
{
  private static final long serialVersionUID = -924049082728488113L;

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "configuration")
  private Configuration configuration;

  public void setAccessChecker(AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  public void setConfiguration(Configuration configuration)
  {
    this.configuration = configuration;
  }

  private MenuCache menuCache = new MenuCache();

  public void expireMenu(final Integer userId)
  {
    menuCache.removeMenu(userId);
  }

  public void refreshAllMenus()
  {
    menuCache.setExpired();
  }

  private Node buildMenuTree(final PFUserDO user)
  {
    final Node root = new Node();
    if (user == null) {
      return root;
    }
    if (LoginPage.FIRST_PSEUDO_SETUP_USER.equals(user.getUsername()) == true) {
      final Node common = root.addSubMenu(user, MenuItemDef.COMMON);
      common.addSubMenu(user, MenuItemDef.SYSTEM_FIRST_LOGIN_SETUP_PAGE);
      return root;
    }
    final Node common = root.addSubMenu(user, MenuItemDef.COMMON);
    if (common != null) {
      common.addSubMenu(user, MenuItemDef.CALENDAR);
      common.addSubMenu(user, MenuItemDef.SEARCH);
      if (configuration.isBookManagementConfigured() == true) {
        common.addSubMenu(user, MenuItemDef.BOOK_LIST);
      }
      if (configuration.isAddressManagementConfigured() == true) {
        common.addSubMenu(user, MenuItemDef.ADDRESS_LIST);
      }
      if (StringUtils.isNotEmpty(configuration.getTelephoneSystemUrl()) == true) {
        common.addSubMenu(user, MenuItemDef.PHONE_CALL);
      }
      common.addSubMenu(user, MenuItemDef.IMAGE_CROPPER);
      if (configuration.isMebConfigured() == true) {
        final Node meb = common.addSubMenu(user, MenuItemDef.MEB);
        if (meb != null) {
          meb.setNewCounterModel(new MenuNewCounterMeb());
        }
      }
      if (WicketApplication.isDevelopmentModus() == true) {
        common.addSubMenu(user, MenuItemDef.GWIKI);
      }
      common.addSubMenu(user, MenuItemDef.DOCUMENTATION);
    }
    final Node fibu = root.addSubMenu(user, MenuItemDef.FIBU);
    boolean projectMenuEntryExists = false;
    boolean orderBookMenuEntryExists = false;
    if (fibu != null) {
      fibu.addSubMenu(user, MenuItemDef.RECHNUNG_LIST);
      fibu.addSubMenu(user, MenuItemDef.EINGANGS_RECHNUNG_LIST);
      fibu.addSubMenu(user, MenuItemDef.BANK_ACCOUNT_LIST);
      fibu.addSubMenu(user, MenuItemDef.KUNDE_LIST);
      if (fibu.addSubMenu(user, MenuItemDef.PROJEKT_LIST) != null) {
        projectMenuEntryExists = true;
      }
      fibu.addSubMenu(user, MenuItemDef.EMPLOYEE_LIST);
      fibu.addSubMenu(user, MenuItemDef.EMPLOYEE_SALARY_LIST);
      boolean partlyReadwrite = accessChecker.hasRight(UserRightId.PM_ORDER_BOOK, UserRightValue.PARTLYREADWRITE);
      final Node orderBook = partlyReadwrite ? null : fibu.addSubMenu(user, MenuItemDef.ORDER_LIST);
      if (orderBook != null) {
        orderBookMenuEntryExists = true;
        orderBook.setNewCounterModel(new MenuNewCounterOrder());
        orderBook.setNewCounterTooltip("menu.fibu.orderbook.htmlSuffixTooltip");
      }
    }
    final Node kost = root.addSubMenu(user, MenuItemDef.KOST);
    if (kost != null) {
      kost.addSubMenu(user, MenuItemDef.KONTO_LIST);
      kost.addSubMenu(user, MenuItemDef.KOST1_LIST);
      kost.addSubMenu(user, MenuItemDef.KOST2_LIST);
      kost.addSubMenu(user, MenuItemDef.KOST2_ART_LIST);
    }
    final Node reporting = root.addSubMenu(user, MenuItemDef.REPORTING);
    if (reporting != null) {
      reporting.addSubMenu(user, MenuItemDef.BUCHUNG_SATZ_LIST);
      reporting.addSubMenu(user, MenuItemDef.REPORT_OBJECTIVES);
      reporting.addSubMenu(user, MenuItemDef.REPORT_SCRIPTING);
      reporting.addSubMenu(user, MenuItemDef.SCRIPT_LIST);
      if (DatevImportDao.hasRight(accessChecker) == true) {
        reporting.addSubMenu(user, MenuItemDef.DATEV_IMPORT);
      }
    }
    final Node orga = root.addSubMenu(user, MenuItemDef.ORGA);
    if (orga != null) {
      orga.addSubMenu(user, MenuItemDef.POSTEINGANG_LIST);
      orga.addSubMenu(user, MenuItemDef.POSTAUSGANG_LIST);
      if (CollectionUtils.isNotEmpty(configuration.getContractTypes()) == true) {
        orga.addSubMenu(user, MenuItemDef.CONTRACTS);
      }
    }
    final Node admin = root.addSubMenu(user, MenuItemDef.ADMINISTRATION);
    if (admin != null) {
      admin.addSubMenu(user, MenuItemDef.MY_ACCOUNT);
      admin.addSubMenu(user, MenuItemDef.MY_PREFERENCES);
      admin.addSubMenu(user, MenuItemDef.CHANGE_PASSWORD);
      admin.addSubMenu(user, MenuItemDef.USER_LIST);
      admin.addSubMenu(user, MenuItemDef.GROUP_LIST);
      admin.addSubMenu(user, MenuItemDef.ACCESS_LIST);
      admin.addSubMenu(user, MenuItemDef.SYSTEM);
      admin.addSubMenu(user, MenuItemDef.SYSTEM_UPDATE);
      admin.addSubMenu(user, MenuItemDef.SYSTEM_STATISTICS);
      admin.addSubMenu(user, MenuItemDef.CONFIGURATION);
    }
    final Node projectMgmnt = root.addSubMenu(user, MenuItemDef.PROJECT_MANAGEMENT);
    if (projectMgmnt != null) {
      projectMgmnt.addSubMenu(user, MenuItemDef.TASK_TREE);
      projectMgmnt.addSubMenu(user, MenuItemDef.TIMESHEET_LIST);
      projectMgmnt.addSubMenu(user, MenuItemDef.MONTHLY_EMPLOYEE_REPORT);
      projectMgmnt.addSubMenu(user, MenuItemDef.PERSONAL_STATISTICS);
      projectMgmnt.addSubMenu(user, MenuItemDef.HR_VIEW);
      projectMgmnt.addSubMenu(user, MenuItemDef.HR_PLANNING_LIST);
      if (projectMenuEntryExists == false) {
        projectMgmnt.addSubMenu(user, MenuItemDef.PROJEKT_LIST);
      }
      if (orderBookMenuEntryExists == false) {
        projectMgmnt.addSubMenu(user, MenuItemDef.ORDER_LIST);
      }
      projectMgmnt.addSubMenu(user, MenuItemDef.GANTT);
    }
    return root;
  }

  public Menu getMenu(final PFUserDO user)
  {
    Menu menu = null;
    if (user != null) {
      menu = menuCache.getMenu(user.getId());
      if (menu != null) {
        return menu;
      }
    }
    final Node root = buildMenuTree(user);
    menu = new Menu();
    build(menu, null, root);
    if (user != null) {
      menuCache.putMenu(user.getId(), menu);
    }
    return menu;
  }

  private void build(final Menu menu, final MenuEntry parentEntry, final Node parent)
  {
    if (parent.subMenues == null) {
      // Only if user is not logged in.
      return;
    }
    for (final Node node : parent.subMenues) {
      if (node.isVisible() == false) {
        continue;
      }
      final MenuEntry menuEntry;
      if (parentEntry == null) {
        menuEntry = addMenuEntry(menu, node.def);
      } else {
        menuEntry = addMenuEntry(menu, parentEntry, node.def);
      }
      if (menuEntry == null) {
        continue;
      }
      menuEntry.newCounterModel = node.newCounterModel;
      menuEntry.newCounterTooltip = node.newCounterTooltip;
      if (node.subMenues != null) {
        build(menu, menuEntry, node);
      }
    }
  }

  private MenuEntry addMenuEntry(final Menu menu, final MenuItemDef menuItemDef)
  {
    return addMenuEntry(menu, null, menuItemDef);
  }

  private MenuEntry addMenuEntry(final Menu menu, final MenuEntry parent, final MenuItemDef menuItemDef)
  {
    if (parent == null) {
      return menu.addMenuEntry(menuItemDef);
    } else {
      return menu.addMenuEntry(parent, menuItemDef);
    }
  }

  private MenuEntryConfig findMenuEntryConfig(final MenuItemDef menuItemDef)
  {
    final MenuEntryConfig root = configuration.getMenuConfig();
    if (root == null) {
      return null;
    }
    final MenuEntryConfig entry = root.findMenuEntry(menuItemDef);
    return entry;
  }

  class Node
  {
    Node parent;

    List<Node> subMenues;

    MenuItemDef def;

    Model<Integer> newCounterModel;

    String newCounterTooltip;

    boolean isLeaf = true;

    Node()
    {
    }

    Node(final MenuItemDef def)
    {
      this.def = def;
    }

    Node addSubMenu(final PFUserDO loggedInUser, final MenuItemDef def)
    {
      final MenuEntryConfig menu = findMenuEntryConfig(def);
      if (menu != null && menu.isVisible() == false) {
        return null;
      }
      isLeaf = false;
      if (def.getRequiredRightId() != null && def.hasRight(accessChecker, loggedInUser) == false) {
        return null;
      }
      final ProjectForgeGroup[] visibleForGroups = def.getVisibleForGroups();
      if (visibleForGroups != null && visibleForGroups.length > 0 && accessChecker.isUserMemberOfGroup(visibleForGroups) == false) {
        // Do nothing because menu is not visible for logged in user.
        return null;
      }
      if (subMenues == null) {
        subMenues = new ArrayList<Node>();
      }
      final Node subMenu = new Node(def);
      subMenu.parent = this;
      subMenues.add(subMenu);
      return subMenu;
    }

    public void setNewCounterTooltip(String newCounterTooltip)
    {
      this.newCounterTooltip = newCounterTooltip;
    }

    void setNewCounterModel(Model<Integer> newCounterModel)
    {
      this.newCounterModel = newCounterModel;
    }

    boolean isVisible()
    {
      // All sub menues of this node are invisible therefore ignore this menu node.
      return isLeaf == true || (subMenues != null && subMenues.size() > 0);
    }
  }
}
