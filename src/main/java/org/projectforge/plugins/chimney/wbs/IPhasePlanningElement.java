/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

/**
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public interface IPhasePlanningElement extends Comparable<IPhasePlanningElement>
{

  public String getTitle();
  public void setTitle(String title);

  public Integer getPlanningPosition();
  public void setPlanningPosition(Integer planningPosition);

}
