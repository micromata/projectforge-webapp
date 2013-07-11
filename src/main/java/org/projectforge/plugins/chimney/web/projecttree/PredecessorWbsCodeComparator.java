/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.util.Comparator;

import org.projectforge.plugins.chimney.activities.DependencyRelationDO;

/**
 * A comparator that perfoms character-based comparisons of wbs codes of wbs nodes.
 * It is used to sort predeccessor dependencies by the target's wbs code.
 * @author Sweeps <pf@byte-storm.com>
 */
public class PredecessorWbsCodeComparator implements Comparator<DependencyRelationDO>
{

  @Override
  public int compare(final DependencyRelationDO a, final DependencyRelationDO b)
  {
    if (a == null && b == null)
      return 0;
    if (b == null || b.getPredecessor() == null || b.getPredecessor().getWbsNode() == null || b.getPredecessor().getWbsNode().getWbsCode() == null)
      return -1;
    if (a == null || a.getPredecessor() == null || a.getPredecessor().getWbsNode() == null || a.getPredecessor().getWbsNode().getWbsCode() == null)
      return 1;

    final String aWbsCode = a.getPredecessor().getWbsNode().getWbsCode();
    final String bWbsCode = b.getPredecessor().getWbsNode().getWbsCode();
    return aWbsCode.compareToIgnoreCase(bWbsCode);
  }

}
