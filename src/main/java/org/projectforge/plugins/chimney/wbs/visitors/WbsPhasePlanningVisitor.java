/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2010, Micromata GmbH, Kai Reinhard
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

public class WbsPhasePlanningVisitor implements IWbsNodeVisitor
{

  private boolean isValid = false;

  public boolean isValid()
  {
    return isValid;
  }

  @Override
  public void visit(final MilestoneDO node)
  {
    isValid = true;
  }

  @Override
  public void visit(final ProjectDO node)
  {
    isValid = false;
  }

  @Override
  public void visit(final SubtaskDO node)
  {
    isValid = false;
  }

  @Override
  public void visit(final WorkpackageDO node)
  {
    isValid = false;
  }

  @Override
  public void visit(final AbstractWbsNodeDO node)
  {
    isValid = false;
  }

  @Override
  public void visit(final PhaseDO node)
  {
    isValid = true;
  }

}
