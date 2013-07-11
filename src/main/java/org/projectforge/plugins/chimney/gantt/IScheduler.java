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
 * Interface for gantt schedulers.
 * 
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public interface IScheduler
{
  /**
   * <p>Call this method once on the root of the activity tree to let the scheduler run. To retrieve the scheduled result use {@link #getResult(WbsActivityDO)}.</p>
   * 
   * @param rootOfActivityTreeToSchedule Root of the activity tree (usually the activity corresponding to the project)
   */
  public void schedule(WbsActivityDO rootOfActivityTreeToSchedule);

  /**
   * <p>Use this method to retrieve the result of a scheduling for a child in the scheduled activity tree.</p>
   * <p>This should not trigger a schedule by itself, so you have to manually call {@link #schedule(WbsActivityDO)} beforehand.</p>
   * <p>This method should be implemented in a way so that the result can be retrieved in O(1).</p>
   * 
   * @param childInScheduledActivityTree Child in the already scheduled activity tree
   * @return Result of the schedule or null if the node is not in the tree
   */
  public GanttScheduledActivity getResult(WbsActivityDO childInScheduledActivityTree);
}
