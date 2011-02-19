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

import org.apache.wicket.Page;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.wicket.WicketApplication;

public class MenuItemDef implements Serializable
{
  private static final long serialVersionUID = 6793153590139785117L;

  private String id;

  private String i18nKey;

  private MenuItemDef parent;

  private Class< ? extends Page> pageClass;

  private String url;

  private String[] params;

  private boolean newWindow;

  private ProjectForgeGroup[] visibleForGroups;

  private UserRightId requiredRightId;

  private UserRightValue[] requiredRightValues;

  private int orderNumber;

  /**
   * Overwrite this if you need special access checking.
   * @param context
   * @return true (at default).
   */
  protected boolean isVisible(final MenuBuilderContext context)
  {
    return true;
  }
  
  /**
   * @return null if not visible otherwise the created MenuEntry.
   */
  protected MenuEntry createMenuEntry(final Menu menu, final MenuBuilderContext context)
  {
    if (requiredRightId != null && hasRight(context.getAccessChecker(), context.getLoggedInUser()) == false) {
      return null;
    }
    if (isVisible(context) == false) {
      return null;
    }
    final ProjectForgeGroup[] visibleForGroups = getVisibleForGroups();
    if (visibleForGroups != null
        && visibleForGroups.length > 0
        && context.getAccessChecker().isUserMemberOfGroup(visibleForGroups) == false) {
      // Do nothing because menu is not visible for logged in user.
      return null;
    }
    final MenuEntry menuEntry = new MenuEntry(this);
    if (menuEntry != null) {
      afterMenuEntryCreation(menuEntry, context);
    }
    menuEntry.setMenu(menu);
    return menuEntry;
  }

  /**
   * Override this method if some modifications needed after a menu entry for an user's menu is created.
   * @param createdMenuEntry The fresh created menu entry (is never null).
   * @param context
   */
  protected void afterMenuEntryCreation(final MenuEntry createdMenuEntry, final MenuBuilderContext context)
  {
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey,
      final Class< ? extends Page> pageClass, final UserRightId requiredRightId, final UserRightValue... requiredRightValues)
  {
    this(parent, id, orderNumber, i18nKey, pageClass, null, requiredRightId, requiredRightValues);
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey,
      final Class< ? extends Page> pageClass, final String[] params, final UserRightId requiredRightId,
      final UserRightValue... requiredRightValues)
  {
    this.parent = parent;
    this.id = id;
    this.orderNumber = orderNumber;
    this.i18nKey = i18nKey;
    this.pageClass = pageClass;
    this.params = params;
    this.requiredRightId = requiredRightId;
    this.requiredRightValues = requiredRightValues;
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey,
      final Class< ? extends Page> pageClass, final ProjectForgeGroup... visibleForGroups)
  {
    this(parent, id, orderNumber, i18nKey, pageClass, null, visibleForGroups);
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey,
      final Class< ? extends Page> pageClass, final String[] params, final ProjectForgeGroup... visibleForGroups)
  {
    this.parent = parent;
    this.id = id;
    this.orderNumber = orderNumber;
    this.i18nKey = i18nKey;
    this.pageClass = pageClass;
    this.params = params;
    this.visibleForGroups = visibleForGroups;
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey, final String url,
      final ProjectForgeGroup... visibleForGroups)
  {
    this(parent, id, orderNumber, i18nKey, url, false, visibleForGroups);
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey, final String url,
      boolean newWindow, final ProjectForgeGroup... visibleForGroups)
  {
    this.parent = parent;
    this.id = id;
    this.orderNumber = orderNumber;
    this.i18nKey = i18nKey;
    this.url = url;
    this.newWindow = newWindow;
    this.visibleForGroups = visibleForGroups;
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey,
      final ProjectForgeGroup... visibleForGroups)
  {
    this.parent = parent;
    this.id = id;
    this.orderNumber = orderNumber;
    this.i18nKey = i18nKey;
    this.visibleForGroups = visibleForGroups;
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey, final String url, final UserRightId requiredRightId,
      final UserRightValue... requiredRightValues)
  {
    this(parent, id, orderNumber, i18nKey, url, false, requiredRightId, requiredRightValues);
  }

  public MenuItemDef(final MenuItemDef parent, final String id, final int orderNumber, final String i18nKey, final String url, boolean newWindow,
      final UserRightId requiredRightId, final UserRightValue... requiredRightValues)
  {
    this.parent = parent;
    this.id = id;
    this.orderNumber = orderNumber;
    this.i18nKey = i18nKey;
    this.url = url;
    this.newWindow = newWindow;
    this.requiredRightId = requiredRightId;
    this.requiredRightValues = requiredRightValues;
  }

  /**
   * @return parent menu item definition or null if this definition represents a top level menu item.
   */
  public MenuItemDef getParent()
  {
    return parent;
  }

  /**
   * @return Id used for html markup and for referencing in config.xml.
   */
  public String getId()
  {
    return id;
  }
  
  /**
   * Order number for sorting menu entries.
   * @return
   */
  public int getOrderNumber()
  {
    return orderNumber;
  }

  /**
   * @return Key used in the i18n resource bundle.
   */
  public String getI18nKey()
  {
    return i18nKey;
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

  public boolean hasUrl()
  {
    return url != null;
  }

  /**
   * @return true if this menu entry has a link (to a Wicket page or an url). Otherwise false (e. g. if this menu item def represents only a
   *         menu with sub menus).
   */
  public boolean isLink()
  {
    return isWicketPage() == true || hasUrl() == true;
  }

  /**
   * @return The url for non-Wicket pages (relative to "secure/") or the bookmarkable url for Wicket pages (relative to "wa/").
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
  
  @Override
  public String toString()
  {
    return id;
  }
}
