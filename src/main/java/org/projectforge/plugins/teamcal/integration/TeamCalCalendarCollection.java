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

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalCalendarCollection implements Serializable
{
  private static final long serialVersionUID = -3111538883621120582L;

  private final Long ID;

  private String teamCalCalendarCollectionName;

  private HashMap<Integer, String> calendarMap;

  public TeamCalCalendarCollection() {
    ID = System.currentTimeMillis();
  }

  public TeamCalCalendarCollection(final TeamCalCalendarCollection oldCollection) {
    if (oldCollection.ID != null)
      ID = oldCollection.ID;
    else
      ID = System.currentTimeMillis();
    if (oldCollection.getTeamCalCalendarColletionName() != null)
      teamCalCalendarCollectionName = oldCollection.teamCalCalendarCollectionName;
    if (oldCollection.getCalendarMap() != null)
      calendarMap = new HashMap<Integer, String>(oldCollection.calendarMap);
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
   * @return the iD
   */
  public Long getID()
  {
    return ID;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ID == null) ? 0 : ID.hashCode());
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
    if (ID == null) {
      if (other.ID != null)
        return false;
    } else if (!ID.equals(other.ID))
      return false;
    return true;
  }
}
