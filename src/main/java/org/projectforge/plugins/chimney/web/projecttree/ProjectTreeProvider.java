/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeFilter;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;

public class ProjectTreeProvider implements ITreeProvider<AbstractWbsNodeDO>, Serializable
{

  private static final long serialVersionUID = 2502424385680573860L;

  // private final ISortState sortState = new SingleSortState();

  private final WicketWbsUtils wbsUtils;

  private final List<AbstractWbsNodeDO> rootList;

  private final WbsNodeFilter filter;

  public ProjectTreeProvider(final WicketWbsUtils wbsUtils, final WbsNodeFilter filter, final ProjectDO... roots)
  {
    this.wbsUtils = wbsUtils;
    this.filter = filter;
    rootList = new ArrayList<AbstractWbsNodeDO>(roots != null ? roots.length : 1);
    if (roots != null) {
      for (final ProjectDO r : roots) {
        rootList.add(r);
      }
    }
  }

  @Override
  public Iterator< ? extends AbstractWbsNodeDO> getChildren(final AbstractWbsNodeDO node)
  {
    return node.getFilteredChildren(filter).iterator();
  }

  @Override
  public Iterator< ? extends AbstractWbsNodeDO> getRoots()
  {
    return rootList.iterator();
  }

  @Override
  public boolean hasChildren(final AbstractWbsNodeDO node)
  {
    return !node.getFilteredChildren(filter).isEmpty();
  }

  @Override
  public IModel<AbstractWbsNodeDO> model(final AbstractWbsNodeDO node)
  {
    return wbsUtils.getModelFor(node);
  }

  @Override
  public void detach()
  {
    // nothing to do?
  }

  /**
   * Returns the predecessor in the filtered children of the passed node's parent or null if no predecessor exists.
   * @param node The node for which to get the predecessor for
   * @return Node's predecessor in the filtered list of it's parent or null
   */
  public AbstractWbsNodeDO getPredecessor(final AbstractWbsNodeDO node)
  {
    final AbstractWbsNodeDO parent = node.getParent();
    if (parent == null)
      return null;

    AbstractWbsNodeDO predecessor = null;
    final Iterator< ? extends AbstractWbsNodeDO> it = getChildren(parent);
    while (it.hasNext()) {
      final AbstractWbsNodeDO child = it.next();
      if (child.equals(node))
        return predecessor;
      predecessor = child;
    }
    return null;
  }

  /**
   * Returns the successor in the filtered children of the passed node's parent or null if no successor exists.
   * @param node The node for which to get the predecessor for
   * @return Node's successor in the filtered list of it's parent or null
   */
  public AbstractWbsNodeDO getSuccessor(final AbstractWbsNodeDO node)
  {
    final AbstractWbsNodeDO parent = node.getParent();
    if (parent == null)
      return null;

    AbstractWbsNodeDO predecessor = null;
    final Iterator< ? extends AbstractWbsNodeDO> it = getChildren(parent);
    while (it.hasNext()) {
      final AbstractWbsNodeDO child = it.next();
      if (predecessor != null && predecessor.equals(node))
        return child;
      predecessor = child;
    }
    return null;
  }
  /*
   * @Override public ISortState getSortState() { return sortState; }
   */

}
