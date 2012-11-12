/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.web.calendar.CalendarFilter;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalCalendarFilter extends CalendarFilter
{
  private static final long serialVersionUID = -8318037558891653348L;

  private static final String DEFAULT_COLOR = "#FAAF26";

  private List<TeamCalCalendarCollection> teamCalCalendarCollection;

  private TeamCalCalendarCollection currentCollection;

  public TeamCalCalendarFilter()
  {
    super();
    teamCalCalendarCollection = new ArrayList<TeamCalCalendarCollection>();
  }

  public TeamCalCalendarFilter(final TeamCalCalendarFilter filter)
  {
    super();
    if (this.teamCalCalendarCollection != null)
      this.teamCalCalendarCollection.clear();
    else
      this.teamCalCalendarCollection = new ArrayList<TeamCalCalendarCollection>();

    for (final TeamCalCalendarCollection tCCC : filter.teamCalCalendarCollection)
      this.teamCalCalendarCollection.add(tCCC);

    if (filter.getCurrentCollection() != null)
      this.currentCollection = new TeamCalCalendarCollection(filter.getCurrentCollection());
  }

  public Set<Integer> getCalendarPk(TeamCalCalendarCollection collection)
  {
    collection = testSerializedMap(collection);
    return collection.getCalendarMap().keySet();
  }

  /**
   * 
   */
  private TeamCalCalendarCollection testSerializedMap(TeamCalCalendarCollection collection)
  {
    if (collection == null) {
      collection = new TeamCalCalendarCollection();
    }
    if (collection.getCalendarMap() == null) {
      collection.setCalendarMap(new HashMap<Integer, String>());
    }
    return collection;
  }

  public void addCalendarPk(final Integer pk, final TeamCalCalendarCollection collection)
  {
    String color = DEFAULT_COLOR;
    long lastEntry = 0;

    // intelligent color choose
    for (final TeamCalCalendarCollection tCCC : teamCalCalendarCollection) {
      if (tCCC.getCalendarMap().containsKey(pk)) {
        // init
        if (lastEntry == 0){
          lastEntry = tCCC.getID();
        }

        // get color of last entry
        if (tCCC.getID() <= lastEntry) {
          lastEntry = tCCC.getID();
          color = tCCC.getCalendarMap().get(pk);
        }
      }
    }

    // default color
    updateCalendarColor(pk, color, collection);
  }

  public void updateCalendarColor(final Integer pk, final String color, TeamCalCalendarCollection collection)
  {
    if (StringUtils.isNotBlank(color)) {
      collection = testSerializedMap(collection);
      collection.getCalendarMap().put(pk, color);
    }
  }

  public void removeCalendarPk(final Integer pk, TeamCalCalendarCollection collection)
  {
    collection = testSerializedMap(collection);
    if (collection.getCalendarMap().containsKey(pk)) {
      collection.getCalendarMap().remove(pk);
    }
  }

  public String getColor(final Integer pk, TeamCalCalendarCollection collection)
  {
    collection = testSerializedMap(collection);
    final String result = collection.getCalendarMap().get(pk);
    if (StringUtils.isBlank(result)) {
      return DEFAULT_COLOR;
    }
    return result;
  }

  /**
   * @return
   */
  public List<TeamCalDO> calcAssignedtItems(final TeamCalDao dao, final TeamCalCalendarCollection collection)
  {
    final List<TeamCalDO> result = new LinkedList<TeamCalDO>();
    for (final Integer calendarId : getCalendarPk(collection)) {
      result.add(dao.getById(calendarId));
    }
    return result;
  }

  public void updateTeamCalendarFilter(final TeamCalCalendarFilter updatedFilter)
  {
    setSelectedCalendar(updatedFilter.getSelectedCalendar());
    if (updatedFilter.getCurrentCollection() != null) {
      this.currentCollection = new TeamCalCalendarCollection(updatedFilter.getCurrentCollection());
    }

    if (this.teamCalCalendarCollection != null)
      this.teamCalCalendarCollection.clear();
    else
      this.teamCalCalendarCollection = new ArrayList<TeamCalCalendarCollection>();

    for (final TeamCalCalendarCollection collection : updatedFilter.teamCalCalendarCollection) {
      this.teamCalCalendarCollection.add(new TeamCalCalendarCollection(collection));
    }
  }

  public void addTeamCalCalendarCollection(final TeamCalCalendarCollection collection)
  {
    teamCalCalendarCollection.add(collection);
  }

  /**
   * @return the currentCollection
   */
  public TeamCalCalendarCollection getCurrentCollection()
  {
    return currentCollection;
  }

  /**
   * @param currentCollection the currentCollection to set
   */
  public void setCurrentCollection(final TeamCalCalendarCollection currentCollection)
  {
    this.currentCollection = currentCollection;
  }

  /**
   * @return the teamCalCalendarCollection
   */
  public List<TeamCalCalendarCollection> getTeamCalCalendarCollection()
  {
    return teamCalCalendarCollection;
  }
}
