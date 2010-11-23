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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.Model;
import org.projectforge.access.AccessChecker;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.fibu.AuftragDao;
import org.projectforge.fibu.datev.DatevImportDao;
import org.projectforge.meb.MebDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.meb.MenuMebSuffixModel;
import org.projectforge.web.wicket.WicketApplication;

public class MenuBuilder implements Serializable
{
  private static final long serialVersionUID = -924049082728488113L;

  public static final String NOT_YET_IMPLEMENTED = "Message.action?"
      + MessageAction.MSG_REQ_PARAM_NAME
      + "="
      + MessageAction.MSG_NOT_YET_IMPLEMENTED;

  private AccessChecker accessChecker;

  private AuftragDao auftragDao;

  private Configuration configuration;

  private MebDao mebDao;

  private MenuCache menuCache = new MenuCache();

  public static String getNewCounterForAsMenuEntrySuffix(final int counter)
  {
    return getNewCounterForAsMenuEntrySuffix(counter, null);
  }

  public static String getNewCounterForAsMenuEntrySuffix(final int counter, final String tooltip)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append(" <span style=\"background-color:red; color:white; font-weight:bold;font-size:110%;\");");
    if (tooltip != null) {
      buf.append(" title=\"").append(PFUserContext.getLocalizedString(tooltip)).append("\"");
    }
    buf.append(">&nbsp;").append(counter).append("&nbsp;</span>");
    return buf.toString();
  }

  public void setAccessChecker(AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  public void setAuftragDao(AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
  }

  public void setConfiguration(Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setMebDao(MebDao mebDao)
  {
    this.mebDao = mebDao;
  }

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
      root.addSubMenu(user, MenuItemDef.LOGIN);
    } else if (LoginAction.FIRST_PSEUDO_SETUP_USER.equals(user.getUsername()) == true) {
      root.addSubMenu(user, MenuItemDef.SYSTEM_FIRST_LOGIN_SETUP_PAGE);
    } else {
      final Locale locale = PFUserContext.getLocale();
      final boolean isGerman = locale != null && locale.toString().startsWith("de") == true;
      final Node common = root.addSubMenu(user, MenuItemDef.COMMON);
      common.addSubMenu(user, MenuItemDef.TASK_TREE);
      common.addSubMenu(user, MenuItemDef.TIMESHEET_LIST);
      common.addSubMenu(user, MenuItemDef.CALENDAR);
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
          final Model<String> htmlSuffixModel = new MenuOrderBookSuffixModel();
          orderBook.setHtmlSuffix(htmlSuffixModel);
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

      final Node misc = root.addSubMenu(user, MenuItemDef.MISC);
      if (configuration.getTaskIdValue(ConfigurationParam.DEFAULT_TASK_ID_4_BOOKS) != null) {
        misc.addSubMenu(user, MenuItemDef.BOOK_LIST);
      }
      if (configuration.getTaskIdValue(ConfigurationParam.DEFAULT_TASK_ID_4_ADDRESSES) != null) {
        misc.addSubMenu(user, MenuItemDef.ADDRESS_LIST);
      }
      if (StringUtils.isNotEmpty(configuration.getTelephoneSystemUrl()) == true) {
        misc.addSubMenu(user, MenuItemDef.PHONE_CALL);
      }
      misc.addSubMenu(user, MenuItemDef.IMAGE_CROPPER);
      if (configuration.isMebConfigured() == true) {
        final Node meb = misc.addSubMenu(user, MenuItemDef.MEB);
        if (meb != null) {
          Model<String> htmlSuffixModel = new MenuMebSuffixModel();
          meb.setHtmlSuffix(htmlSuffixModel);
        }
      }

      final Node doc = root.addSubMenu(user, MenuItemDef.DOCUMENTATION);
      if (doc != null) {
        doc.addSubMenu(user, MenuItemDef.NEWS);
        doc.addSubMenu(user, MenuItemDef.PROJECTFORGE_DOC);
        doc.addSubMenu(user, MenuItemDef.USER_GUIDE);
        if (isGerman == true) {
          doc.addSubMenu(user, MenuItemDef.FAQ_DE);
        } else {
          doc.addSubMenu(user, MenuItemDef.FAQ);
        }
        doc.addSubMenu(user, MenuItemDef.LICENSE);
        doc.addSubMenu(user, MenuItemDef.PROJECT_DOC);
        doc.addSubMenu(user, MenuItemDef.ADMIN_LOGBUCH);
        final Node dev = doc.addSubMenu(user, MenuItemDef.DEVELOPER_DOC);
        if (dev != null) {
          dev.addSubMenu(user, MenuItemDef.ADMIN_GUIDE);
          dev.addSubMenu(user, MenuItemDef.DEVELOPER_GUIDE);
          dev.addSubMenu(user, MenuItemDef.JAVA_DOC);
          dev.addSubMenu(user, MenuItemDef.TEST_REPORTS);
        }
      }
      if (WicketApplication.isDevelopmentModus() == true) {
        root.addSubMenu(user, MenuItemDef.GWIKI);
      }
      root.addSubMenu(user, MenuItemDef.SEARCH);
      root.addSubMenu(user, MenuItemDef.LOGOUT);
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

  public MenuTreeTable build(final PFUserDO user)
  {
    final Node root = buildMenuTree(user);
    final MenuTreeTable menu = new MenuTreeTable();
    build(menu, null, root, (short) 0);
    return menu;
  }

  private short build(final MenuTreeTable menu, final MenuTreeTableNode parentTreeTableNode, final Node parent, short orderNumber)
  {
    for (final Node node : parent.subMenues) {
      if (node.isVisible() == false) {
        continue;
      }
      final MenuTreeTableNode treeTableNode = addNode(menu, parentTreeTableNode, node.def, orderNumber++);
      if (treeTableNode == null) {
        continue;
      }
      if (node.htmlSuffix != null) {
        treeTableNode.setHtmlSuffix(node.htmlSuffix);
      }
      if (node.subMenues != null) {
        orderNumber = build(menu, treeTableNode, node, orderNumber);
      }
    }
    return orderNumber;
  }

  private MenuTreeTableNode addNode(final MenuTreeTable menu, final MenuTreeTableNode parent, final MenuItemDef menuItemDef,
      final short orderNumber)
  {
    if (parent != null) {
      return menu.addNode(parent, menuItemDef, orderNumber);
    } else {
      return menu.addNode(menuItemDef, orderNumber);
    }
  }

  private void build(final Menu menu, final MenuEntry parentEntry, final Node parent)
  {
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
      menuEntry.htmlSuffix = node.htmlSuffix;
      if (node.subMenues != null) {
        build(menu, menuEntry, node);
      }
    }
  }

  @Deprecated
  public Menu buildDTreeMenu(PFUserDO user)
  {
    final Node root = buildMenuTree(user);
    final Menu menu = new Menu();
    build(menu, null, root);
    // Order numbers are needed by the JavaScript menu:
    short orderNumber = 1;
    for (final MenuEntry menuEntry : menu.getMenuEntries()) {
      orderNumber = setOrderNumber(menuEntry, orderNumber);
    }
    return menu;
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

  private short setOrderNumber(MenuEntry menuEntry, short orderNumber)
  {
    menuEntry.setOrderNumber(orderNumber++);
    if (menuEntry.getHasSubMenuEntries() == true) {
      for (MenuEntry subMenuEntry : menuEntry.getSubMenuEntries()) {
        orderNumber = setOrderNumber(subMenuEntry, orderNumber);
      }
    }
    return orderNumber;
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

    Model<String> htmlSuffix;

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

    void setHtmlSuffix(final Model<String> htmlSuffix)
    {
      this.htmlSuffix = htmlSuffix;
    }

    boolean isVisible()
    {
      // All sub menues of this node are invisible therefore ignore this menu node.
      return isLeaf == true || (subMenues != null && subMenues.size() > 0);
    }
  }
}
