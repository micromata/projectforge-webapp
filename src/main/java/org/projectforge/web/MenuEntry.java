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
import java.util.Collection;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Represents a single menu entry.
 */
public class MenuEntry implements Serializable
{
  private static final long serialVersionUID = 7961498640193169174L;

  protected Collection<MenuEntry> subMenuEntries;

  protected String url;

  protected boolean opened = false;

  protected IModel<Integer> newCounterModel;

  protected TotalNewCounterModel totalNewCounterModel;

  protected boolean totalNewCounterModelEvaluated;

  protected String newCounterTooltip;

  protected MenuEntry parent;

  protected boolean visible = true;

  protected MenuItemDef menuItemDef;

  protected Menu menu;

  public IModel<Integer> getNewCounterModel()
  {
    if (hasSubMenuEntries() == false) {
      return newCounterModel;
    }
    if (totalNewCounterModelEvaluated == true) {
      return totalNewCounterModel;
    }
    for (final MenuEntry subEntry : subMenuEntries) {
      final IModel<Integer> subSumModel = subEntry.getNewCounterModel();
      if (subSumModel == null) {
        continue;
      }
      if (totalNewCounterModel == null) {
        totalNewCounterModel = new TotalNewCounterModel();
      }
      totalNewCounterModel.add(subSumModel);
    }
    totalNewCounterModelEvaluated = true;
    return totalNewCounterModel;
  }

  public String getNewCounterTooltip()
  {
    return newCounterTooltip;
  }

  public MenuEntry findMenu(final Class< ? extends Page> pageClass)
  {
    if (menuItemDef.getPageClass() != null && menuItemDef.getPageClass().isAssignableFrom(pageClass) == true) {
      return this;
    }
    if (subMenuEntries != null) {
      for (final MenuEntry subMenuEntry : subMenuEntries) {
        final MenuEntry found = subMenuEntry.findMenu(pageClass);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  public void setSelected()
  {
    menu.selectedMenu = this;
  }

  public boolean isSelected()
  {
    if (this == menu.selectedMenu) {
      return true;
    }
    if (subMenuEntries != null) {
      for (final MenuEntry subMenuEntry : subMenuEntries) {
        if (subMenuEntry.isSelected() == true) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isFirst()
  {
    return menu.isFirst(this);
  }

  public MenuEntry getParent()
  {
    return parent;
  }

  void setParent(final MenuEntry parent)
  {
    this.parent = parent;
  }

  public boolean hasParent()
  {
    return this.parent != null;
  }

  public MenuEntry(final MenuItemDef menuItem, final Menu menu)
  {
    this.menuItemDef = menuItem;
    this.menu = menu;
    if (menuItem.isWicketPage() == true) {
      this.url = WicketUtils.getBookmarkablePageUrl(menuItem.getPageClass(), menuItem.getParams());
    } else if (menuItem.getUrl() != null) {
      this.url = "secure/" + menuItem.getUrl();
    }
  }

  public void addMenuEntry(final MenuEntry subMenuEntry)
  {
    if (subMenuEntries == null) {
      subMenuEntries = new ArrayList<MenuEntry>();
    }
    subMenuEntries.add(subMenuEntry);
    subMenuEntry.setParent(this);
  }

  public boolean hasSubMenuEntries()
  {
    return (this.subMenuEntries != null && subMenuEntries.size() > 0);
  }

  public boolean isOpened()
  {
    return this.opened;
  }

  /**
   * Should the link open a separate window (named 'pforge2')?
   * @return
   */
  public boolean isNewWindow()
  {
    return menuItemDef.isNewWindow();
  }

  public boolean isWicketPage()
  {
    return menuItemDef.isWicketPage();
  }

  public Class< ? extends Page> getPageClass()
  {
    return menuItemDef.getPageClass();
  }

  /**
   * @return
   */
  public Collection<MenuEntry> getSubMenuEntries()
  {
    return subMenuEntries;
  }

  public String getI18nKey()
  {
    return menuItemDef.getI18nKey();
  }

  /**
   * @return
   */
  public String getUrl()
  {
    return url;
  }

  public String[] getParams()
  {
    return menuItemDef.getParams();
  }

  public void setNewCounterModel(final IModel<Integer> newCounterModel)
  {
    this.newCounterModel = newCounterModel;
  }

  public boolean isVisible()
  {
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }

  public String getId()
  {
    return menuItemDef.name();
  }
}
