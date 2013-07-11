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
 * This Exception is thrown, if an WbsActivityDO has no fixed begin date,
 * which is needed for the current purpose.
 * If any algorithm needs a fixed begin date of an WbsActivityDO, but it is not set,
 * then this algorithm should throw this type of Exception.
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public class MissingFixedBeginDateException extends IllegalActivityStateException
{

  /**
   * 
   */
  private static final long serialVersionUID = -3800102602811123932L;

  public MissingFixedBeginDateException(final WbsActivityDO causeActivity)
  {
    super(
        causeActivity,
        "plugins.chimney.errors.missingfixedbegindate",
        causeActivity != null && causeActivity.getWbsNode() != null ?
            causeActivity.getWbsNode().getTitle() : "-"
    );
  }



}
