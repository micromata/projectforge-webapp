/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

public class SelfDependencyRelationException extends DependencyRelationException
{

  private final WbsActivityDO activity;

  public SelfDependencyRelationException(final WbsActivityDO activity)
  {
    super("plugins.chimney.errors.selfdependency");
    this.activity = activity;
  }

  @Override
  public String getMessage() {
    return String.format("Activity '%s' must not depend on itself", activity.toString());
  }

  /**
   * 
   */
  private static final long serialVersionUID = -3607551782489283665L;

}
