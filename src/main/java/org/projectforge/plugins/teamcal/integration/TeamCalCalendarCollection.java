/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalCalendarCollection implements Serializable
{
  private static final long serialVersionUID = -3111538883621120582L;

  private Long id;

  private String teamCalCalendarCollectionName;

  private final HashSet<Integer> visibleList;

  private HashMap<Integer, String> calendarMap;

  public TeamCalCalendarCollection()
  {
    id = System.currentTimeMillis();
    visibleList = new HashSet<Integer>();
  }

  public TeamCalCalendarCollection(final TeamCalCalendarCollection oldCollection)
  {
    this();
    if (oldCollection.id != null) {
      id = oldCollection.id;
    }

    if (oldCollection.getTeamCalCalendarColletionName() != null) {
      teamCalCalendarCollectionName = oldCollection.teamCalCalendarCollectionName;
    }

    if (oldCollection.getCalendarMap() != null) {
      calendarMap = new HashMap<Integer, String>(oldCollection.calendarMap);
    }

    if (oldCollection.visibleList != null) {
      visibleList.addAll(oldCollection.visibleList);
    }

  }

  public void setCalendarMap(final HashMap<Integer, String> calendarMap)
  {
    this.calendarMap = calendarMap;
  }

  public HashMap<Integer, String> getCalendarMap()
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
    visibleList.add(calendarPk);
  }

  public void removeCalendar(final Integer calendarPk)
  {
    calendarMap.remove(calendarPk);
    visibleList.remove(calendarPk);
  }

  /**
   * @return the iD
   */
  public Long getID()
  {
    return id;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  public Set<Integer> getTeamCalsVisibleList()
  {
    return visibleList;
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
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}
