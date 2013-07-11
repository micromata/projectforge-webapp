/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.apache.commons.lang.Validate;
import org.apache.wicket.model.IModel;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeDao;
import org.projectforge.plugins.chimney.wbs.WbsNodeFilter;
import org.projectforge.plugins.chimney.web.DetachableChangeableDOModel;

/**
 * TreeNode Wrapper for WbsNodeDo for displaying a Tree in a wicket component.
 * Internally, it uses DetachableChangableDOModel to not save tree node objects in the session
 * and to prevent Hibernate LazyInitializationExceptions.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WbsTreeNode implements TreeNode, Serializable
{
  private static final long serialVersionUID = -1174364228350864123L;

  private final IModel<AbstractWbsNodeDO> nodeModel;
  private final WbsNodeDao wbsNodeDao;
  private transient boolean attached = false;

  private final WbsNodeFilter wbsNodeFilter;

  public WbsTreeNode(final AbstractWbsNodeDO node, final WbsNodeDao wbsNodeDao) {
    this(node, wbsNodeDao, null);
  }

  public WbsTreeNode(final AbstractWbsNodeDO node, final WbsNodeDao wbsNodeDao, final WbsNodeFilter wbsNodeFilter) {
    this(new DetachableChangeableDOModel<AbstractWbsNodeDO, WbsNodeDao>(node, wbsNodeDao), wbsNodeDao, wbsNodeFilter);
  }

  public WbsTreeNode(final IModel<AbstractWbsNodeDO> nodeModel, final WbsNodeDao wbsNodeDao) {
    this(nodeModel, wbsNodeDao, null);
  }

  public WbsTreeNode(final IModel<AbstractWbsNodeDO> nodeModel, final WbsNodeDao wbsNodeDao, final WbsNodeFilter wbsNodeFilter) {
    Validate.notNull(nodeModel);
    Validate.notNull(wbsNodeDao);
    this.wbsNodeDao = wbsNodeDao;
    this.nodeModel = nodeModel;
    this.wbsNodeFilter = wbsNodeFilter;
  }

  @Override
  public Enumeration<WbsTreeNode> children()
  {
    final AbstractWbsNodeDO node = getWbsNode();
    final Vector<WbsTreeNode> children = new Vector<WbsTreeNode>(node.childrenCount());
    for (int i=0; i<node.childrenCount(); i++) {
      if (isAllowed(node.getChild(i))) {
        children.add(new WbsTreeNode(node.getChild(i), wbsNodeDao, wbsNodeFilter));
      }
    }
    return children.elements();
  }

  private boolean isAllowed(final AbstractWbsNodeDO child)
  {
    if (wbsNodeFilter == null) {
      return true;
    }

    return wbsNodeFilter.isAllowed(child);
  }

  @Override
  public boolean getAllowsChildren()
  {
    return true;
  }

  @Override
  public TreeNode getChildAt(final int index)
  {
    if (wbsNodeFilter == null) {
      return new WbsTreeNode(getWbsNode().getChild(index), wbsNodeDao);
    } else {
      return new WbsTreeNode(getWbsNode().getFilteredChildren(wbsNodeFilter).get(index), wbsNodeDao, wbsNodeFilter);
    }
  }

  @Override
  public int getChildCount()
  {
    if (wbsNodeFilter == null) {
      return getWbsNode().childrenCount();
    } else {
      return getWbsNode().getFilteredChildren(wbsNodeFilter).size();
    }
  }

  @Override
  public int getIndex(final TreeNode childTreeNode)
  {
    if (childTreeNode instanceof WbsTreeNode) {
      final WbsTreeNode wbsChildNode = (WbsTreeNode)childTreeNode;
      final AbstractWbsNodeDO childNode = wbsChildNode.getWbsNode();
      final AbstractWbsNodeDO thisNode = getWbsNode();
      if (wbsNodeFilter == null) {
        return thisNode.getChildIndex(childNode);
      } else {
        thisNode.getFilteredChildren(wbsNodeFilter).indexOf(childNode);
      }
    }
    return -1;
  }

  @Override
  public TreeNode getParent()
  {
    final AbstractWbsNodeDO parent = getWbsNode().getParent();
    if (parent == null)
      return null;
    return new WbsTreeNode(parent, wbsNodeDao, wbsNodeFilter);
  }

  @Override
  public boolean isLeaf()
  {
    return !getWbsNode().hasChildren();
  }

  public AbstractWbsNodeDO getWbsNode() {
    return getWbsNodeModel().getObject();
  }

  public IModel<AbstractWbsNodeDO> getWbsNodeModel() {
    if (!attached) {
      // This a bug fix. For some reason the transient wbs object does not get detached
      // from DetachableDOModel in some cases after being serialized and unserialized.
      nodeModel.detach();
      attached = true;
    }
    return nodeModel;
  }

  @Override
  public String toString() {
    return getWbsNode().getTitle();
  }

}
