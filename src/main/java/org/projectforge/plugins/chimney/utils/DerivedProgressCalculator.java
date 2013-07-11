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

public class DerivedProgressCalculator
{

  public int deriveProgressFor(final AbstractWbsNodeDO node)
  {
    final List<Integer> workpackageProgresses = getWorkpackageProgresses(node);
    if (workpackageProgresses.isEmpty()) {
      return 0;
    } else {
      return (int) Math.floor(((double) sum(workpackageProgresses) / workpackageProgresses.size()));
    }
  }

  private int sum(final List<Integer> workpackageProgresses)
  {
    int sum = 0;
    for (final Integer i : workpackageProgresses) {
      sum += i;
    }
    return sum;
  }

  private List<Integer> getWorkpackageProgresses(final AbstractWbsNodeDO node)
  {
    final List<Integer> progresses = new LinkedList<Integer>();
    for (int i = 0; i < node.childrenCount(); i++) {
      final AbstractWbsNodeDO child = node.getChild(i);
      if (child instanceof WorkpackageDO) {
        progresses.add(child.getProgress());
      }
      progresses.addAll(getWorkpackageProgresses(child));
    }
    return progresses;
  }

}
