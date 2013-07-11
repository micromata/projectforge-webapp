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
 * This Exception is thrown if any calculation needs an persistent WbsActivityDO, which can't be transient.
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public class IllegalTransientActivityException extends IllegalActivityStateException
{

  /**
   * 
   */
  private static final long serialVersionUID = -8556220806075402603L;

  public IllegalTransientActivityException(final WbsActivityDO causeActivity)
  {
    super(
        causeActivity,
        "plugins.chimney.errors.illegaltransientactivity",
        causeActivity != null && causeActivity.getWbsNode() != null ?
            causeActivity.getWbsNode().getTitle() : "-"
    );
  }

}
