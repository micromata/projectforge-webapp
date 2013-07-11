/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

import org.joda.time.ReadablePeriod;

public interface IDependencyRelationReadOnly
{

  public IActivityReadOnly< ? > getPredecessor();

  public IActivityReadOnly< ? > getSuccessor();

  public DependencyRelationType getType();

  public ReadablePeriod getOffset();
}
