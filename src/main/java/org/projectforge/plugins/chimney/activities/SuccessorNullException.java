/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

public class SuccessorNullException extends DependencyRelationException
{
  private static final long serialVersionUID = 388681832124111289L;

  public SuccessorNullException(final DependencyRelationDO dependency) {
    super("plugins.chimney.errors.successornull");
  }
}
