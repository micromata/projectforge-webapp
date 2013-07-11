/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import java.util.LinkedList;
import java.util.List;

import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class ResourceBasedNeededDurationProvider implements NeededDurationProvider
{
  private PlannedResourcesProvider plannedResourcesProvider;

  @Deprecated
  public ResourceBasedNeededDurationProvider() {}

  public ResourceBasedNeededDurationProvider(final PlannedResourcesProvider plannedResourcesProvider)
  {
    this.plannedResourcesProvider = plannedResourcesProvider;
  }

  @Override
  public Integer neededProjectDays(final WbsActivityDO activity)
  {
    final List<ReadablePeriod> findPlannedResources = plannedResourcesProvider.findPlannedResources(activity.getWbsNode());
    final Period maybeEffortEstimation = activity.getEffortEstimation();

    if (maybeEffortEstimation == null) {
      return null;
    }

    final int estimatedDays = maybeEffortEstimation.toStandardDays().getDays();

    if (findPlannedResources.size() < 1) {
      return estimatedDays;
    }

    final List<Integer> plannedDays = mapDays(findPlannedResources);
    final Integer maybeNeededDays = neededDays(estimatedDays, plannedDays);
    return maybeNeededDays != null ? maybeNeededDays : estimatedDays;
  }

  private List<Integer> mapDays(final List<ReadablePeriod> findPlannedResources)
  {
    final List<Integer> plannedDays = new LinkedList<Integer>();
    for (final ReadablePeriod p: findPlannedResources) {
      plannedDays.add(p.toPeriod().toStandardDays().getDays());
    }
    return plannedDays;
  }

  private Integer neededDays(final int estimatedDays, final List<Integer> plannedDays) {
    int neededDays = 0;
    int daysToGo = estimatedDays;
    List<Integer> plannedRest = new LinkedList<Integer>(plannedDays);

    while (daysToGo > 0) {
      final int toReduce = plannedRest.size();
      if (toReduce < 1) {
        break;
      }
      plannedRest = reduce(plannedRest);
      daysToGo -= toReduce;
      neededDays += 1;
    }

    return daysToGo > 0 ? null : neededDays;
  }

  private List<Integer> reduce(final List<Integer> plannedRest)
  {
    final List<Integer> reduced = new LinkedList<Integer>();
    for (final Integer i: plannedRest) {
      final int minusOne = i - 1;
      if (minusOne > 0) {
        reduced.add(minusOne);
      }
    }
    return reduced;
  }
}
