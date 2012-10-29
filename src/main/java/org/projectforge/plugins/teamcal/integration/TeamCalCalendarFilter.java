/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
  private Map<Integer, String> calendarMap;

  /**
   * 
   */
  public TeamCalCalendarFilter()
  {
    super();
    this.calendarMap = new HashMap<Integer, String>();
  }

  public Set<Integer> getCalendarPk()
  {
    testSerializedMap();
    return calendarMap.keySet();
  }

  /**
   * 
   */
  private void testSerializedMap()
  {
    if (calendarMap == null) {
      calendarMap = new HashMap<Integer, String>();
    }
  }

  public void addCalendarPk(final Integer pk)
  {
    // default color
    updateCalendarColor(pk, DEFAULT_COLOR);
  }

  public void updateCalendarColor(final Integer pk, final String color)
  {
    if (StringUtils.isNotBlank(color)) {
      testSerializedMap();
      calendarMap.put(pk, color);
    }
  }

  public void removeCalendarPk(final Integer pk)
  {
    testSerializedMap();
    if (calendarMap.containsKey(pk)) {
      calendarMap.remove(pk);
    }
  }

  public String getColor(final Integer pk)
  {
    testSerializedMap();
    final String result = calendarMap.get(pk);
    if(StringUtils.isBlank(result)) {
      return DEFAULT_COLOR;
    }
    return result;
  }

  /**
   * @return
   */
  public List<TeamCalDO> calcAssignedtItems(final TeamCalDao dao)
  {
    final List<TeamCalDO> result = new LinkedList<TeamCalDO>();
    for (final Integer calendarId : getCalendarPk()) {
      result.add(dao.getById(calendarId));
    }
    return result;
  }

  public void resetFilter(final Collection<TeamCalDO> newCollection)
  {
    testSerializedMap();
    calendarMap.clear();
    for (final TeamCalDO calendar : newCollection) {
      addCalendarPk(calendar.getId());
    }
  }

  public void updateTeamCalendarFilter(final TeamCalCalendarFilter updatedFilter) {
    this.calendarMap = new HashMap<Integer, String>();
    for (final Integer key : updatedFilter.calendarMap.keySet()) {
      this.calendarMap.put(key, updatedFilter.calendarMap.get(key));
    }
    this.setSelectedCalendar(updatedFilter.getSelectedCalendar());
  }
}
