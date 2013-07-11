/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeFilter;

public class GanttActivityMapper
{

  private WbsActivityNavigator wbsActivityNavigator;
  private IScheduler scheduler;
  private final WbsNodeFilter phaseFilter;

  public GanttActivityMapper() {
    // initilize the filter to exclude PhaseDO which should not appear in gantt charts:
    final ArrayList<Class< ? extends AbstractWbsNodeDO>> exclusionList = new ArrayList<Class< ? extends AbstractWbsNodeDO>>();
    exclusionList.add(PhaseDO.class);
    phaseFilter = new WbsNodeFilter( exclusionList, true);
  }
  public void setWbsActivityNavigator(final WbsActivityNavigator wbsActivityNavigator)
  {
    this.wbsActivityNavigator = wbsActivityNavigator;
  }

  public WbsActivityNavigator getWbsActivityNavigator()
  {
    return wbsActivityNavigator;
  }

  public void setScheduler(final IScheduler scheduler)
  {
    this.scheduler = scheduler;
  }

  public IScheduler getScheduler()
  {
    return scheduler;
  }

  public ChimneyGanttActivity getResult(final WbsActivityDO subtree)
  {
    final GanttScheduledActivity scheduled = scheduler.getResult(subtree);
    return new ChimneyGanttActivity(subtree,  scheduled.getBegin(), scheduled.getEnd());
  }

  public List<ChimneyGanttActivity> map(final WbsActivityDO project)
  {
    final List<ChimneyGanttActivity> list = new LinkedList<ChimneyGanttActivity>();
    // exclude PhaseDO which should not appear in gantt charts:
    if (phaseFilter.isAllowed(project.getWbsNode())) {
      list.add(getResult(project));
    }
    for (final WbsActivityDO child: wbsActivityNavigator.getChildren(project)) {
      list.addAll(map(child));
    }
    return list;
  }
}
