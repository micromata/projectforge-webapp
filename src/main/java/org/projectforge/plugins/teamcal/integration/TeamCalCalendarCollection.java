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

  public TeamCalCalendarCollection() {

  }

  public TeamCalCalendarCollection(final String teamCalCalendarCollectionName, final Map<Integer, String> calendarMap) {
    this.teamCalCalendarCollectionName = teamCalCalendarCollectionName;
    this.calendarMap = calendarMap;
  }

  public void setCalendarMap(final Map<Integer, String> calendarMap) {
    this.calendarMap = calendarMap;
  }

  public Map<Integer, String> getCalendarMap() {
    return calendarMap;
  }

  public void setTeamCalCalendarCollectionName(final String name) {
    this.teamCalCalendarCollectionName = name;
  }

  public String getTeamCalCalendarColletionName() {
    return teamCalCalendarCollectionName;
  }

  public void addNewCalendar(final Integer calendarPk, final String colorCode) {
    calendarMap.put(calendarPk, colorCode);
  }

  public void removeCalendar(final Integer calendarPk) {
    calendarMap.remove(calendarPk);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((calendarMap == null) ? 0 : calendarMap.hashCode());
    result = prime * result + ((teamCalCalendarCollectionName == null) ? 0 : teamCalCalendarCollectionName.hashCode());
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
    if (calendarMap == null) {
      if (other.calendarMap != null)
        return false;
    } else if (!calendarMap.equals(other.calendarMap))
      return false;
    if (teamCalCalendarCollectionName == null) {
      if (other.teamCalCalendarCollectionName != null)
        return false;
    } else if (!teamCalCalendarCollectionName.equals(other.teamCalCalendarCollectionName))
      return false;
    return true;
  }

}
