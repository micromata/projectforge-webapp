/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.gantt.model;

import org.projectforge.plugins.chimney.activities.DependencyRelationType;

/**
 * this interface is part of the gantt model and describes a dependency to a predecessor gantt activity
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IGanttDependency
{

  /**
   * @return unique id of the predecessor activity
   */
  public int getPredecessorId();

  /**
   * @return dependency type of this relation (@see org.projectforge.plugins.chimney.activities.DependencyRelationType)
   */
  public DependencyRelationType getDependencyRelationType();
}
