/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import org.projectforge.plugins.chimney.activities.WbsActivityDO;


/**
 * This Exception is thrown, if any activities have a cyclic dependency.
 * @author Sweeps <pf@byte-storm.com>
 */
public class CyclicDependencyException extends IllegalActivityStateException
{

  /**
   * 
   */
  private static final long serialVersionUID = 143303870068070546L;

  /**
   * @param causeActivity
   */
  public CyclicDependencyException(final WbsActivityDO causeActivity)
  {
    super(
        causeActivity,
        "plugins.chimney.errors.schedulercyclicdependency",
        causeActivity != null ?
            causeActivity.toString() : "-"
    );
    // TODO Auto-generated constructor stub
  }

}
