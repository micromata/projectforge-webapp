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
import java.util.Collection;

import org.apache.wicket.model.IModel;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Represents a single menu entry.
 */
@Deprecated
public class MenuEntry implements Serializable
{
  private static final long serialVersionUID = 7961498640193169174L;

  protected Collection<MenuEntry> subMenuEntries;

  protected String[] params;

  protected String url;

  protected String label;

  protected String icon;

  protected String iconOpen;

  protected boolean newWindow = false;

  protected boolean opened = false;

  protected short orderNumber;

  protected IModel<String> htmlSuffix;

  protected MenuEntry parent;

  protected boolean visible = true;

  public String getIcon()
  {
    return icon;
  }

  public String getIconOpen()
  {
    return iconOpen;
  }

  /** Number is needed for the tree menu implementation of http://www.destroydrop.com/javascripts/tree/. */
  public short getOrderNumber()
  {
    return orderNumber;
  }

  void setOrderNumber(short orederNumber)
  {
    this.orderNumber = orederNumber;
  }

  public MenuEntry getParent()
  {
    return parent;
  }

  void setParent(MenuEntry parent)
  {
    this.parent = parent;
  }

  public boolean getHasParent()
  {
    return this.parent != null;
  }

  public MenuEntry(final MenuItemDef menuItem)
  {
    this.label = menuItem.getI18nKey();
    this.icon = menuItem.getIcon();
    this.iconOpen = menuItem.getIcon();
    this.newWindow = menuItem.isNewWindow();
    this.params = menuItem.getParams();
    if (menuItem.isWicketPage() == true) {
      this.url = WicketUtils.getBookmarkablePageUrl(menuItem.getPageClass(), params);

    } else {
      this.url = "secure/" + menuItem.getUrl();
    }
  }

  public void addMenuEntry(MenuEntry subMenuEntry)
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

  public boolean getHasSubMenuEntries()
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
    return newWindow;
  }

  /**
   * @return
   */
  public Collection<MenuEntry> getSubMenuEntries()
  {
    return subMenuEntries;
  }

  public String getLabel()
  {
    return label;
  }

  /**
   * @return
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * If not null then this html suffix will be shown direct after the menu entry. Used e. g. for displaying number of closed but not fully
   * invoiced orders after order book menu entry.
   * @return
   */
  public String getHtmlSuffixString()
  {
    return htmlSuffix != null ? htmlSuffix.getObject() : "";
  }

  public IModel<String> getHtmlSuffix()
  {
    return htmlSuffix;
  }

  public void setHtmlSuffix(IModel<String> htmlSuffix)
  {
    this.htmlSuffix = htmlSuffix;
  }

  public boolean isVisible()
  {
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }
}
