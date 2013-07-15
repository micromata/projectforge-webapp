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
import javax.persistence.Transient;

import org.projectforge.plugins.chimney.utils.DerivedProgressCalculator;
import org.projectforge.plugins.chimney.utils.DerivedStatusCalculator;
import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;
import org.projectforge.plugins.chimney.wbs.visitors.WbsNodeChildValidationVisitor;
import org.projectforge.task.TaskStatus;

@Entity
@DiscriminatorValue("subtask")
public class SubtaskDO extends AbstractWbsNodeDO
{
  private static final long serialVersionUID = 2012789637926693915L;
  public static final SubtaskDO prototype = new SubtaskDO(); // convenience for database retrieval

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

  @Override
  @Transient
  public TaskStatus getStatus()
  {
    // TODO: Derive the status on the change of a workpackage and save it, instead of deriving it on each request
    final DerivedStatusCalculator statusProvider = new DerivedStatusCalculator();
    return statusProvider.deriveStatusFor(this);
  }

  @Override
  @Transient
  public int getProgress()
  {
    // TODO: Derive progress on change of a workpackage and save it, instead of deriving it on each request
    final DerivedProgressCalculator calculator = new DerivedProgressCalculator();
    return calculator.deriveProgressFor(this);
  }

}
