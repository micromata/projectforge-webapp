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
import java.util.Iterator;
import java.util.List;

import org.projectforge.web.tree.TreeTable;
import org.projectforge.web.tree.TreeTableNode;

/**
 * The implementation of TreeTable for tasks. Used for browsing the tasks (tree view).
 */
public class MenuTreeTable extends TreeTable<MenuTreeTableNode> implements Serializable
{
  private static final long serialVersionUID = -8223045740442399796L;

  private boolean dirty = true;

  public MenuTreeTable()
  {
    this.root = new MenuTreeTableNode();
  }

  public TreeTableNode setOpenedStatusOfNode(final String eventKey, final Integer hashId)
  {
    return super.setOpenedStatusOfNode(eventKey, hashId);
  }

  public List<MenuTreeTableNode> getNodeList()
  {
    return super.getNodeList(null);
  }

  public boolean hasNextSibling(final MenuTreeTableNode node)
  {
    if (dirty == true) {
      evaluateSiblings(this.root);
      dirty = false;
    }
    return node.hasNextSibling;
  }

  private void evaluateSiblings(final MenuTreeTableNode node)
  {
    if (node.hasChilds() == true) {
      final Iterator<TreeTableNode> it = node.getChilds().iterator();
      while (it.hasNext() == true) {
        final MenuTreeTableNode child = (MenuTreeTableNode) it.next();
        child.hasNextSibling = (it.hasNext() == true);
        evaluateSiblings(child);
      }
    }
  }

  /**
   * Adds top level menu entry.
   */
  protected MenuTreeTableNode addNode(final MenuItemDef menuItemDef, short orderNumber)
  {
    return addNode(this.root, menuItemDef, orderNumber);
  }

  /**
   * @param loggedInUser Needs for checking visibility of menu.
   * @param menuItemDef
   * @param parent
   * @param orderNumber
   * @return
   */
  protected MenuTreeTableNode addNode(final MenuTreeTableNode parent, final MenuItemDef menuItemDef,
      final short orderNumber)
  {
    dirty = true;
    final MenuTreeTableNode node = new MenuTreeTableNode(parent, menuItemDef, orderNumber);
    addTreeTableNode(node);
    return node;
  }
}
