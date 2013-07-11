/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.gantt.model;

import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;

public class DefaultGanttActivityVisualizationTypeVisitor implements IWbsNodeVisitor
{
  private GanttActivityVisualizationType selectedType;

  public GanttActivityVisualizationType getVisualizationTypeFor(final AbstractWbsNodeDO node)
  {
    selectedType = GanttActivityVisualizationType.BAR;
    node.accept(this);
    return selectedType;
  };

  @Override
  public void visit(final MilestoneDO node)
  {
    selectedType = GanttActivityVisualizationType.MILESTONE;
  }

  @Override
  public void visit(final ProjectDO node)
  {
    selectedType = GanttActivityVisualizationType.SPAN;
  }

  @Override
  public void visit(final SubtaskDO node)
  {

    selectedType = node.childrenCount() > 0 ? GanttActivityVisualizationType.SPAN : GanttActivityVisualizationType.BAR;
  }

  @Override
  public void visit(final WorkpackageDO node)
  {
    selectedType = GanttActivityVisualizationType.BAR;
  }

  @Override
  public void visit(final AbstractWbsNodeDO node)
  {
    selectedType = GanttActivityVisualizationType.BAR;
  }

  @Override
  public void visit(final PhaseDO node)
  {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

}
