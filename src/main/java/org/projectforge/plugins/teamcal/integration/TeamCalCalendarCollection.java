/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalCalendarCollection implements Serializable
{
  private static final long serialVersionUID = -3111538883621120582L;

  private String teamCalCalendarCollectionName;

  private Map<Integer, String> calendarMap;

  private long creationTime;

  public TeamCalCalendarCollection()
  {
    calendarMap = new HashMap<Integer, String>();
    creationTime = System.currentTimeMillis();
  }

  public void setCalendarMap(final Map<Integer, String> calendarMap)
  {
    this.calendarMap = calendarMap;
  }

  public Map<Integer, String> getCalendarMap()
  {
    return calendarMap;
  }

  public void setTeamCalCalendarCollectionName(final String name)
  {
    this.teamCalCalendarCollectionName = name;
  }

  public String getTeamCalCalendarColletionName()
  {
    return teamCalCalendarCollectionName;
  }

  public void addNewCalendar(final Integer calendarPk, final String colorCode)
  {
    calendarMap.put(calendarPk, colorCode);
  }

  public void removeCalendar(final Integer calendarPk)
  {
    calendarMap.remove(calendarPk);
  }

  /**
   * @return the creationTime
   */
  public long getCreationTime()
  {
    return creationTime;
  }

  /**
   * @param creationTime the creationTime to set
   * @return this for chaining.
   */
  public void setCreationTime(final long creationTime)
  {
    this.creationTime = creationTime;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final TeamCalCalendarCollection other = (TeamCalCalendarCollection) obj;
    if (creationTime != other.creationTime)
      return false;
    return true;
  }

}
