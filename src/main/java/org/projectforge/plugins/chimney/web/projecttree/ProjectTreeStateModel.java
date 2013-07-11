/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.util.Set;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.ProviderSubset;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IDetachable;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

public class ProjectTreeStateModel extends AbstractReadOnlyModel<Set<AbstractWbsNodeDO>>
{
  private static final long serialVersionUID = 9161467331656964311L;

  private Set<AbstractWbsNodeDO> treeState;
  private ITreeProvider<AbstractWbsNodeDO> treeProvider;

  public ProjectTreeStateModel(final Set<AbstractWbsNodeDO> treeState)
  {
    super();
    this.treeState = treeState;
  }

  public ProjectTreeStateModel(final ITreeProvider<AbstractWbsNodeDO> treeProvider, final boolean expandAll)
  {
    super();
    this.treeProvider = treeProvider;
    if (expandAll)
      expandAll();
    else
      collapseAll();
  }

  public void collapseAll()
  {
    detach();
    treeState = new ProviderSubset<AbstractWbsNodeDO>(treeProvider);
  }

  public void expandAll()
  {
    detach();
    // Not implemented:
    //treeState = new InverseSet<AbstractWbsNodeDO>(new ProviderSubset<AbstractWbsNodeDO>(treeProvider));
  }

  @Override
  public Set<AbstractWbsNodeDO> getObject()
  {
    return treeState;
  }

  /**
   * Super class doesn't detach - would be nice though.
   */
  @Override
  public void detach()
  {
    if (treeState != null)
      ((IDetachable)treeState).detach();
  }

}
