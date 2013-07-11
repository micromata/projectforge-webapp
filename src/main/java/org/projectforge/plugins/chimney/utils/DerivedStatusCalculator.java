/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils;

import java.util.LinkedList;
import java.util.List;

import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.task.TaskStatus;

/**
 * <p>A helper class for deriving the node status from nodes further down in the hierarchy.
 * This implementation checks all workpackages in the subtree of a given node.</p>
 * <p>The following rules apply:</p>
 * <ul>
 *  <li>If no workpackages exist below the given node, the status is null.</li>
 *  <li>If the statuses of all workpackages below the given node are null, the status is null.</li>
 *  <li>If all workpackages below the given node are closed and no status is null, the status is closed.</li>
 *  <li>If all workpackages below the given node are not opened or null, the status not opened.</li>
 *  <li>The status is opened, otherwise</li>
 * </ul>
 * @author Sweeps <pf@byte-storm.com>
 */
public class DerivedStatusCalculator
{
  public TaskStatus deriveStatusFor(final AbstractWbsNodeDO node)
  {
    final List<TaskStatus> statusList = obtainWorkpackageChildrenStatuses(node);

    boolean allClosed = true;
    boolean allNotOpened = true;
    boolean allNull = true;
    boolean someNull = false;

    for (final TaskStatus status: statusList) {
      if (status == null)
        someNull = true;
      else switch (status) {
        case O:
          return TaskStatus.O;

        case C:
          allNotOpened = false;
          allNull = false;
          if (!allClosed)
            return TaskStatus.O;
          break;

        case N:
          allClosed = false;
          allNull = false;
          if (!allNotOpened)
            return TaskStatus.O;
          break;
      }
    }

    if (allNull)
      return null;

    if (allClosed)
      if (someNull)
        return TaskStatus.O;
      else
        return TaskStatus.C;


    if (allNotOpened)
      return TaskStatus.N;

    return TaskStatus.O;
  }

  private List<TaskStatus> obtainWorkpackageChildrenStatuses(final AbstractWbsNodeDO node)
  {
    final List<TaskStatus> statusList = new LinkedList<TaskStatus>();
    for (int i = 0 ; i < node.childrenCount() ; i++) {
      final AbstractWbsNodeDO child = node.getChild(i);
      if (child instanceof WorkpackageDO) {
        statusList.add(child.getStatus());
      }
      statusList.addAll(obtainWorkpackageChildrenStatuses(child));
    }
    return statusList;
  }
}
