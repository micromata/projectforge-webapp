package org.projectforge.plugins.teamcal.externalsubscription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.projectforge.plugins.teamcal.event.TeamEventDO;

/**
 * Own abstraction of a RangeMap. You can add TeamEvents and access them through their start and end date.
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class SubscriptionHolder implements Serializable
{
  // one day in milliseconds
  private static final int ONE_DAY = 86400000; // 60*60*24*1000

  private List<TeamEventDO> eventList;

  private boolean sorted;

  public SubscriptionHolder()
  {
    eventList = new ArrayList<TeamEventDO>();
    sorted = false;
  }

  public void clear()
  {
    eventList.clear();
    sorted = false;
  }

  public void add(TeamEventDO value)
  {
    eventList.add(value);
    sorted = false;
  }

  public void sort()
  {
    Comparator<TeamEventDO> comparator = new Comparator<TeamEventDO>() {
      @Override
      public int compare(TeamEventDO o1, TeamEventDO o2)
      {
        if (o1 == null && o2 == null) {
          return 0;
        }
        if (o1 == null) {
          return -1;
        }
        if (o2 == null) {
          return 1;
        }
        return o1.getStartDate().compareTo(o2.getStartDate());
      }
    };
    Collections.sort(eventList, comparator);
    sorted = true;
  }

  public List<TeamEventDO> getResultList(Long startTime, Long endTime)
  {
    if (sorted == false) {
      sort();
    }
    List<TeamEventDO> result = new ArrayList<TeamEventDO>();
    for (TeamEventDO teamEventDo : eventList) {
      if (matches(teamEventDo, startTime, endTime) == true) {
        result.add(teamEventDo);
        // all our events are sorted, if we find a event which starts
        // after the end date, we can break this iteration
        if (teamEventDo.getEndDate().getTime() > endTime) {
          break;
        }
      }
    }
    // and return
    return result;
  }

  private boolean matches(TeamEventDO teamEventDo, Long startTime, Long endTime)
  {
    // Following period extension is needed due to all day events which are stored in UTC. The additional events in the result list not
    // matching the time period have to be removed by caller!
    startTime = startTime - ONE_DAY;
    endTime = endTime + ONE_DAY;

    // the following implementation is inspired by TeamEventDao with the following lines:

    // queryFilter.add(Restrictions.or(
    // (Restrictions.or(Restrictions.between("startDate", startDate, endDate), Restrictions.between("endDate", startDate, endDate))),
    // // get events whose duration overlap with chosen duration.
    // (Restrictions.and(Restrictions.le("startDate", startDate), Restrictions.ge("endDate", endDate)))));

    Long eventStartTime = teamEventDo.getStartDate().getTime();
    Long eventEndTime = teamEventDo.getEndDate().getTime();
    if (between(eventStartTime, startTime, endTime) || between(eventEndTime, startTime, endTime)) {
      return true;
    }
    if (eventStartTime <= startTime && eventEndTime >= endTime) {
      return true;
    }
    return false;
  }

  private boolean between(Long searchTime, Long startTime, Long endTime)
  {
    return searchTime >= startTime && searchTime <= endTime;
  }
}
