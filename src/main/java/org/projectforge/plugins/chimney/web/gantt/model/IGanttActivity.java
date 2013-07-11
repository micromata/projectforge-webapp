/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.gantt.model;

import java.util.Iterator;

import org.joda.time.ReadableDateTime;
/**
 * this interface defines a gantt model: an abstract description of an activity which is visualized by a gantt charts
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IGanttActivity
{

  /**
   * @return a unique id for this activity (not null!)
   */
  public Integer getId();

  /**
   * @return the title of this activity (not null!)
   */
  public String getTitle();

  /**
   * @return work breakdown structure code or shortcut for the activity (not null!)
   */
  public String getWbsCode();

  /**
   * @return true, if this activity has a parent activity in the gantt model
   */
  public boolean hasParent();

  /**
   * retrieves the unique id of the parent activity.
   * This Method is not called if hasParent() (@see #hasParent) returns false.
   * @return unique id of the parent activity.
   */
  public Integer getParentId();

  /**
   * @return date, when this activity starts
   */
  public ReadableDateTime getBeginDate();
  /**
   * @return date, when this activity ends
   */
  public ReadableDateTime getEndDate();

  /**
   * @return percental progress of this activity (should be between 0 and 100)
   */
  public int getProgress();

  /**
   * @return visualization type of this activity (@see GanttActivityVisualizationType)
   */
  public GanttActivityVisualizationType getVisualizationType();

  /**
   * @return iterator of all predecessor dependencies of this activity (@see IGanttDependency)
   */
  public Iterator<IGanttDependency> predecessorDependencyIterator();

  /**
   * returns a link which can be used to link any web page to the activity
   * @return a link url corresponding to the activity
   */
  public String getLinkUrl();
}
