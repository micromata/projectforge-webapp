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
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Represents a single menu entry.
 */
public class MenuEntry implements Serializable, Comparable<MenuEntry>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MenuEntry.class);

  private static final long serialVersionUID = 7961498640193169174L;

  protected SortedSet<MenuEntry> subMenuEntries;

  protected String url;

  protected boolean opened = false;

  protected IModel<Integer> newCounterModel;

  protected TotalNewCounterModel totalNewCounterModel;

  protected boolean totalNewCounterModelEvaluated;

  protected String newCounterTooltip;

  protected MenuEntry parent;

  protected Boolean visible;

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

  public void setNewCounterTooltip(String newCounterTooltip)
  {
    this.newCounterTooltip = newCounterTooltip;
  }

  public String getNewCounterTooltip()
  {
    return newCounterTooltip;
  }

  /**
   * Needed as marker for modified css.
   */
  public boolean isFirst()
  {
    return menu.isFirst(this);
  }

  public MenuEntry getParent()
  {
    return parent;
  }

  /**
   * @param menu Needed because MenuEntry is perhaps under construction and menu member isn't set yet.
   * @param id
   */
  public void setParent(final Menu menu, final String id)
  {
    final MenuEntry parentEntry = menu.findById(id);
    if (parentEntry == null) {
      log.error("Oups, menu entry '" + id + "' not found (ignoring setParent(...) of : " + getId());
    } else {
      setParent(parentEntry);
    }
  }

  void setParent(final MenuEntry parent)
  {
    this.parent = parent;
  }

  public boolean hasParent()
  {
    return this.parent != null;
  }

  /**
   * Root menu entry.
   */
  MenuEntry()
  {
  }

  public MenuEntry(final MenuItemDef menuItem)
  {
    this.menuItemDef = menuItem;
    if (menuItem.isWicketPage() == true) {
      this.url = WicketUtils.getBookmarkablePageUrl(menuItem.getPageClass(), menuItem.getParams());
    } else if (menuItem.getUrl() != null) {
      this.url = "../secure/" + menuItem.getUrl();
    }
  }

  public void setMenu(final Menu menu)
  {
    this.menu = menu;
    menu.addMenuEntry(this);
  }

  public void addMenuEntry(final MenuEntry subMenuEntry)
  {
    if (subMenuEntries == null) {
      subMenuEntries = new TreeSet<MenuEntry>();
    }
    subMenuEntries.add(subMenuEntry);
    subMenuEntry.setParent(this);
  }

  public MenuEntry findById(final String id)
  {
    if (menuItemDef != null && menuItemDef.getId().equals(id) == true) {
      return this;
    }
    if (this.subMenuEntries == null) {
      return null;
    }
    for (final MenuEntry subMenuEntry : this.subMenuEntries) {
      final MenuEntry found = subMenuEntry.findById(id);
      if (found != null) {
        return found;
      }
    }
    return null;
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

  public boolean isLink()
  {
    return menuItemDef.isWicketPage() == true || menuItemDef.hasUrl() == true;
  }

  /**
   * @return True or false if variable visible is set. True, if no sub menu entries exists in this entry has an link. If sub menu entries
   *         does exist then it's visible if any of the sub menu entries is visible. The variable visible is set automatically after the
   *         first call of this method.
   */
  public boolean isVisible()
  {
    if (visible != null) {
      return visible;
    }
    if (subMenuEntries == null || subMenuEntries.size() == 0) {
      visible = isLink();
    } else {
      for (final MenuEntry subMenuEntry : subMenuEntries) {
        if (subMenuEntry.isVisible() == true) {
          visible = true;
          break;
        }
      }
    }
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }

  public String getId()
  {
    return menuItemDef.getId();
  }

  @Override
  public int compareTo(final MenuEntry o)
  {
    if (menuItemDef.getOrderNumber() < o.menuItemDef.getOrderNumber()) {
      return -1;
    } else if (menuItemDef.getOrderNumber() > o.menuItemDef.getOrderNumber()) {
      return 1;
    }
    return menuItemDef.getI18nKey().compareTo(menuItemDef.getI18nKey());
  }
}
