/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
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
 * Visitor interface for WbsNodes
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IWbsNodeVisitor extends IVisitor
{
  public void visit(MilestoneDO node);

  public void visit(ProjectDO node);

  public void visit(SubtaskDO node);

  public void visit(WorkpackageDO node);

  public void visit(AbstractWbsNodeDO node);

  public void visit(PhaseDO node);
}
