/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.WicketAjaxUtils;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class DefaultTreeTablePanel<T extends TreeTableNode> extends Panel implements TreeTablePanel
{
  private static final long serialVersionUID = 380527812338260483L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultTreeTablePanel.class);

  protected ISelectCallerPage caller;

  protected String selectProperty;

  protected WebMarkupContainer treeTableHead;

  protected WebMarkupContainer treeTableBody;

  protected RepeatingView rowRepeater;

  protected RepeatingView colHeadRepeater;

  private List<T> treeList;

  private TreeTable<T> treeTable;

  protected Integer highlightedRowId;

  protected boolean clickRows = true;

  public DefaultTreeTablePanel(final String id)
  {
    super(id);
  }

  public DefaultTreeTablePanel(final String id, final ISelectCallerPage caller, final String selectProperty)
  {
    super(id);
    this.caller = caller;
    this.selectProperty = selectProperty;
  }

  public void init()
  {
    treeTableHead = new WebMarkupContainer("thead");
    add(treeTableHead);
    initializeColumnHeads();
    treeTableBody = new WebMarkupContainer("tbody");
    treeTableBody.setOutputMarkupId(true);
    add(treeTableBody);
  }

  public void refresh()
  {
    treeList = null;
  }

  public void refreshTreeTable()
  {
    treeTable = null;
  }

  @Override
  protected void onBeforeRender()
  {
    if (treeList == null) {
      buildTree();
    }
    super.onBeforeRender();
  }

  private void buildTree()
  {
    createTreeRows();
  }

  protected abstract void initializeColumnHeads();

  protected void createTreeRows()
  {
    if (rowRepeater != null) {
      treeTableBody.remove(rowRepeater);
    }
    rowRepeater = new RepeatingView("rows");
    treeTableBody.add(rowRepeater);
    refresh(); // Force rebuilding.
    if (getTreeList() == null) {
      return;
    }
    int counter = 0;
    for (final T node : treeList) {
      final WebMarkupContainer row = createTreeRow(node);
      if (counter++ % 2 == 0) {
        row.add(new SimpleAttributeModifier("class", "even"));
      } else {
        row.add(new SimpleAttributeModifier("class", "odd"));
      }
    }
  }

  protected void addColumns(final RepeatingView colBodyRepeater, final String cssStyle, final T node)
  {

  }

  protected abstract TreeIconsActionPanel< ? extends TreeTableNode> createTreeIconsActionPanel(final T node);

  protected Label createColHead(final String i18nKey)
  {
    final Label colHead = new Label(colHeadRepeater.newChildId(), getString(i18nKey));
    return colHead;
  }

  private WebMarkupContainer createTreeRow(final T node)
  {
    final WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId(), new Model<TreeTableNode>(node));
    row.setOutputMarkupId(true);
    row.add(new SimpleAttributeModifier("class", "even"));
    if (clickRows == true) {
      WicketUtils.addRowClick(row);
    }
    rowRepeater.add(row);
    final RepeatingView colBodyRepeater = new RepeatingView("cols");
    row.add(colBodyRepeater);
    final String cssStyle = getCssStyle(node);

    // Column: browse icons
    final TreeIconsActionPanel< ? extends TreeTableNode> treeIconsActionPanel = createTreeIconsActionPanel(node);
    addColumn(row, treeIconsActionPanel, cssStyle);
    treeIconsActionPanel.init(this, node);
    treeIconsActionPanel.add(new AttributeAppendModifier("style", new Model<String>("white-space: nowrap;")));

    addColumns(colBodyRepeater, cssStyle, node);
    return row;
  }

  protected String getCssStyle(final T node)
  {
    return null;
  }

  protected void addColumn(final WebMarkupContainer parent, final Component component, final String cssStyle)
  {
    if (cssStyle != null) {
      component.add(new AttributeAppendModifier("style", new Model<String>(cssStyle)));
    }
    parent.add(component);
  }

  protected abstract List<T> buildTreeList();

  /**
   * Should be used in tree table view. The current tree will be returned for tree navigation.
   * @return
   */
  protected List<T> getTreeList()
  {
    if (treeList == null) {
      treeList = buildTreeList();
    }
    return treeList;
  }

  protected abstract TreeTable<T> buildTreeTable();

  public TreeTable<T> getTreeTable()
  {
    if (treeTable == null) {
      treeTable = buildTreeTable();
    }
    return treeTable;
  }

  public void setOpenNodes(Set<Serializable> openNodes)
  {
    if (getTreeTable() != null && openNodes != null) {
      getTreeTable().setOpenNodes(openNodes);
    }
  }

  public Set<Serializable> getOpenNodes()
  {
    return getTreeTable().getOpenNodes();
  }

  protected WebMarkupContainer getTreeRow(final Serializable hashId)
  {
    @SuppressWarnings("unchecked")
    final Iterator<WebMarkupContainer> it = (Iterator<WebMarkupContainer>) rowRepeater.iterator();
    while (it.hasNext() == true) {
      final WebMarkupContainer child = it.next();
      final TreeTableNode node = (TreeTableNode) child.getDefaultModelObject();
      if (node.getHashId().equals(hashId) == true) {
        return child;
      }
    }
    return null;
  }

  /**
   * Return the row after the row with the given id. If the row is the last row then null is returned.
   * @param hashId
   * @return
   */
  protected WebMarkupContainer getTreeRowAfter(final Serializable hashId)
  {
    final TreeTableNode node = getTreeTable().getElementAfter(getTreeList(), hashId);
    if (node == null) {
      return null;
    } else {
      return getTreeRow(node.getHashId());
    }
  }

  protected WebMarkupContainer removeTreeRow(final Serializable hashId)
  {
    final WebMarkupContainer row = getTreeRow(hashId);
    if (row != null) {
      treeTableBody.remove(row);
    }
    return row;
  }

  /**
   * Overload method should be return false if the call setEvent(AjaxRequestTarget, TreeTableEvent, TreeTableNode) should stop further
   * processing.
   * @param target
   * @param event
   * @param node
   * @return Always true.
   */
  protected boolean onSetEvent(final AjaxRequestTarget target, final TreeTableEvent event, final TreeTableNode node)
  {
    return true;
  }

  @SuppressWarnings("unchecked")
  public void setEvent(final AjaxRequestTarget target, final TreeTableEvent event, final TreeTableNode node)
  {
    if (onSetEvent(target, event, node) == false) {
      return;
    }
    if (treeTableBody == null) {
      log.error("Oups, treeTableContainer is null. Ignoring Ajax event.");
      return;
    }
    if (log.isDebugEnabled() == true) {
      log.debug("setEvent: node=" + node.getHashId() + ", event=" + event + ", nodeStatus=" + node.getNodeStatus());
    }
    final WebMarkupContainer currentRow = getTreeRow(node.getHashId());
    final AbstractLink link = (AbstractLink) currentRow.get("c1:icons:folder");
    if (event == TreeTableEvent.OPEN || event == TreeTableEvent.EXPLORE) {
      final StringBuffer prependJavascriptBuf = new StringBuffer();
      {
        // Add all childs
        final WebMarkupContainer row = getTreeRowAfter(node.getHashId());
        refresh(); // Force to rebuild tree list.
        for (final T child : getTreeTable().getDescendants(getTreeList(), (T) node)) {
          final WebMarkupContainer newRow = createTreeRow(child);
          if (row != null) {
            prependJavascriptBuf.append(WicketAjaxUtils.insertBefore(treeTableBody.getMarkupId(), row.getMarkupId(), "tr", newRow
                .getMarkupId()));
          } else {
            prependJavascriptBuf.append(WicketAjaxUtils.appendChild(treeTableBody.getMarkupId(), "tr", newRow.getMarkupId()));
          }
          target.addComponent(newRow);
        }
      }
      {
        // Replace opened-folder-icon by closed-folder-icon.
        replaceFolderImage(target, link, node, prependJavascriptBuf);
      }
      final String javaScript = prependJavascriptBuf.toString();
      if (javaScript.length() > 0) {
        target.prependJavascript(javaScript);
      }
      target.appendJavascript("updateEvenOdd();initTooltips();");
    } else {
      // Remove all childs
      final StringBuffer prependJavascriptBuf = new StringBuffer();
      final Iterator<WebMarkupContainer> it = (Iterator<WebMarkupContainer>) rowRepeater.iterator();
      final List<WebMarkupContainer> toRemove = new ArrayList<WebMarkupContainer>();
      while (it.hasNext() == true) {
        final WebMarkupContainer row = it.next();
        final TreeTableNode model = (TreeTableNode) row.getDefaultModelObject();
        if (node.isParentOf(model) == true) {
          prependJavascriptBuf.append(WicketAjaxUtils.removeChild(treeTableBody.getMarkupId(), row.getMarkupId()));
          toRemove.add(row);
        }
      }
      for (final WebMarkupContainer row : toRemove) {
        rowRepeater.remove(row);
      }
      {
        // Replace closed-folder-icon by opened-folder-icon.
        replaceFolderImage(target, link, node, prependJavascriptBuf);
      }
      final String javaScript = prependJavascriptBuf.toString();
      if (javaScript.length() > 0) {
        target.prependJavascript(javaScript);
      }
      target.appendJavascript("updateEvenOdd();");
    }
  }

  private void replaceFolderImage(final AjaxRequestTarget target, final AbstractLink link, final TreeTableNode node,
      final StringBuffer prependJavascriptBuf)
  {
    ContextImage oldImage = (ContextImage) link.get("folderImage");
    if (oldImage == null || oldImage.isVisible() == false) {
      oldImage = (ContextImage) link.get("folderOpenImage");
    }
    final ContextImage currentImage = TreeIconsActionPanel.getCurrentFolderImage(getResponse(), link, node);
    if (oldImage != currentImage) {
      prependJavascriptBuf.append(WicketAjaxUtils.replaceChild(link.getMarkupId(), oldImage.getMarkupId(), "img", currentImage
          .getMarkupId()));
      target.addComponent(currentImage);
    }
  }

  protected void onSetEventNode(final Serializable hashId)
  {
  }

  public void setEventNode(final Serializable hashId)
  {
    onSetEventNode(hashId);
    setHighlightedRowId((Integer) hashId);
  }

  public Serializable getEventNode()
  {
    return this.highlightedRowId;
  }
  
  public void setHighlightedRowId(Integer highlightedRowId)
  {
    this.highlightedRowId = highlightedRowId;
    getTreeTable().openNode(highlightedRowId); // Open path to highlighted (preselected) node.
    treeList = null; // Force rebuilding of the tree.
  }

  /**
   * @return true, if this page is called for selection by a caller otherwise false.
   */
  public boolean isSelectMode()
  {
    return this.caller != null;
  }
}
