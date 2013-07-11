/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

public class PredecessorNullException extends DependencyRelationException
{
  private static final long serialVersionUID = 5198421244403798986L;

  public PredecessorNullException(final DependencyRelationDO dependency) {
    super("plugins.chimney.errors.predecessornull");
  }
}
