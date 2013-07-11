/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs.visitors;

import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;

/**
 * A visitor that, given any wbs node in a hierarchy, finds the ProjectDO that is always at the top of the wbs hierarchy.
 * @author Sweeps <pf@byte-storm.com>
 */
public class GetProjectRootVisitor implements IWbsNodeVisitor
{

  ProjectDO projectRoot;

  @Override
  public void visit(final MilestoneDO node)
  {
    if (node.getParent() != null)
      node.getParent().accept(this);
  }

  @Override
  public void visit(final ProjectDO node)
  {
    projectRoot = node;
  }

  @Override
  public void visit(final SubtaskDO node)
  {
    if (node.getParent() != null)
      node.getParent().accept(this);
  }

  @Override
  public void visit(final WorkpackageDO node)
  {
    if (node.getParent() != null)
      node.getParent().accept(this);
  }

  @Override
  public void visit(final AbstractWbsNodeDO node)
  {
    if (node.getParent() != null)
      node.getParent().accept(this);
  }

  @Override
  public void visit(final PhaseDO node)
  {
    if (node.getParent() != null)
      node.getParent().accept(this);
  }

  public ProjectDO getProjectRoot()
  {
    return projectRoot;
  }

}
