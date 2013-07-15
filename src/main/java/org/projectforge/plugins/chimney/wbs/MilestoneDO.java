/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;
import org.projectforge.plugins.chimney.wbs.visitors.WbsNodeChildValidationVisitor;

@Entity
@DiscriminatorValue("milestone")
public class MilestoneDO extends AbstractWbsNodeDO
{
  private static final long serialVersionUID = -948071470598592248L;

  public static final MilestoneDO prototype = new MilestoneDO(); // convenience for database retrieval

  @Override
  public void accept(final IWbsNodeVisitor visitor)
  {
    visitor.visit(this);
  }

  @Override
  protected boolean childIsOfValidType(final AbstractWbsNodeDO newNode)
  {
    final WbsNodeChildValidationVisitor visitor = new WbsNodeChildValidationVisitor(this);
    newNode.accept(visitor);
    return visitor.isValidChild();
  }

  /**
   * Phase will not be set. Milestones are not assigned to phases.
   * */
  @Override
  public void setPhase(final PhaseDO phaseDO)
  {
  }

}
