/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

public class CyclicDependencyRelationException extends DependencyRelationException
{
  private final WbsActivityDO successor;
  private final WbsActivityDO predecessor;

  public CyclicDependencyRelationException(final WbsActivityDO predecessor, final WbsActivityDO successor)
  {
    super("plugins.chimney.errors.cyclicdependency");
    this.predecessor = predecessor;
    this.successor = successor;
  }

  @Override
  public String getMessage() {
    return String.format("Cyclic dependency between '%s' and '%s' detected", predecessor.toString(), successor.toString());
  }

  /**
   * 
   */
  private static final long serialVersionUID = -5080608098667373047L;

}
