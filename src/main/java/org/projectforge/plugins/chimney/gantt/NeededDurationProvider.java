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
 * Responsible for getting the needed duration for an activity. The needed duration is the duration an activity must have at least to be possible to be completed.
 * 
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public interface NeededDurationProvider
{

  /**
   * Returns the number of needed project days for the given activity
   * @param activity
   * @return the number of needed project days or null if not possible to tell
   */
  Integer neededProjectDays(WbsActivityDO activity);

}
