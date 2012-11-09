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

import org.apache.commons.lang.StringUtils;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalCalendarCollection implements Serializable
{
  private static final long serialVersionUID = -3111538883621120582L;

  private String teamCalCalendarCollectionName;

  private HashMap<Integer, String> calendarMap;

  public TeamCalCalendarCollection() {

  }

  public void setCalendarMap(final HashMap<Integer, String> calendarMap) {
    this.calendarMap =  calendarMap;
  }

  public HashMap<Integer, String> getCalendarMap() {
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
    } else if (equalHashMap(calendarMap, other.calendarMap) == false)
      return false;
    if (teamCalCalendarCollectionName == null) {
      if (other.teamCalCalendarCollectionName != null)
        return false;
    } else if (!teamCalCalendarCollectionName.equals(other.teamCalCalendarCollectionName))
      return false;
    return true;
  }

  private boolean equalHashMap(final HashMap<Integer, String> first, final HashMap<Integer, String> second)
  {
    if(first == null && second == null) {
      return true;
    }
    // only one of them is null -> false
    if(first == null || second == null) {
      return false;
    }
    for(final Integer key : first.keySet()) {
      if(second.get(key) == null) {
        return false;
      }
      if(StringUtils.equals(first.get(key), second.get(key)) == false) {
        return false;
      }
    }
    return true;
  }
}
