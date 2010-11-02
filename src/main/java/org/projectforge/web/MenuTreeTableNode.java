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

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.projectforge.web.tree.TreeTableNode;

/**
 * Represents a single menu entry as part of the TreeTable.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MenuTreeTableNode extends TreeTableNode implements Serializable
{
  private static final long serialVersionUID = -2263519530942703140L;

  private MenuItemDef menuItemDef;

  private String url;

  private short orderNumber;

  private IModel<String> htmlSuffix;

  protected boolean hasNextSibling;

  protected MenuTreeTableNode()
  {
  }

  /**
   * If url starts with "secure" it will be recognized as a non-wicket page.
   * @param i18nKey
   * @param url
   * @param icon
   * @param orderNumber
   * @param params
   */
  protected MenuTreeTableNode(final MenuTreeTableNode parent, final MenuItemDef menuItemDef, final short orderNumber)
  {
    super(parent, orderNumber);
    this.menuItemDef = menuItemDef;
    this.orderNumber = orderNumber;
    if (menuItemDef.isWicketPage() == false) {
      this.url = "/secure/" + menuItemDef.getUrl();
    }
  }

  /**
   * Return a String representation of this object.
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer("MenuNode[label=");
    if (menuItemDef != null) {
      sb.append(menuItemDef.getI18nKey());
    }
    sb.append("]");
    return (sb.toString());
  }

  /** Should be overwrite by derived classes. */
  @Override
  public int compareTo(TreeTableNode obj)
  {
    MenuTreeTableNode node = (MenuTreeTableNode) obj;
    if (orderNumber < node.orderNumber) {
      return -1;
    } else if (orderNumber == node.orderNumber) {
      return 0;
    } else {
      return 1;
    }
  }

  public String[] getParams()
  {
    return menuItemDef.getParams();
  }

  public String getUrl()
  {
    return url;
  }

  public String getI18nKey()
  {
    return menuItemDef.getI18nKey();
  }

  public String getIcon()
  {
    return menuItemDef.getIcon();
  }

  public Class< ? extends Page> getPageClass()
  {
    return menuItemDef.getPageClass();
  }

  public boolean isWicketPage()
  {
    return menuItemDef.isWicketPage();
  }

  public boolean isNewWindow()
  {
    return menuItemDef.isNewWindow();
  }

  public short getOrderNumber()
  {
    return orderNumber;
  }

  public IModel<String> getHtmlSuffix()
  {
    return htmlSuffix;
  }

  public void setHtmlSuffix(IModel<String> htmlSuffix)
  {
    this.htmlSuffix = htmlSuffix;
  }
}
