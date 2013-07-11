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
@DiscriminatorValue("Workpackage")
public class WorkpackageDO extends AbstractWbsNodeDO
{
  private static final long serialVersionUID = -8883710265963990727L;
  public static final WorkpackageDO prototype = new WorkpackageDO(); // convenience for database retrieval

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

}
