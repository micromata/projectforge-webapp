/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.resourceworkload;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.ReadableInstant;
import org.projectforge.fibu.EmployeeDO;
import org.projectforge.fibu.EmployeeDao;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.gantt.GanttScheduledActivity;
import org.projectforge.plugins.chimney.gantt.IScheduler;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.utils.date.StandardWorkdayNormalizer;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeUtils;
import org.projectforge.plugins.chimney.web.resourceworkload.model.IWorkloadData;

/**
 * This class takes an arbitrary list of resource assignments and calculates the planned workload sum in hours for each day in a given time interval
 * @see IWorkloadData
 * @author Sweeps <pf@byte-storm.com>
 */
public class ResourceAssignmentWorkload implements IWorkloadData
{


  private static final double MILLIS_TO_HOURS_FACTOR = 1.0/(1000.0*60.0*60.0);

  private final WbsActivityDao activityDao;
  private final EmployeeDao employeeDao;
  private final IScheduler scheduler;
  private final IEmployeeWorkdayProvider employeeAvailabilityProvider;
  private final HashSet<ProjectDO> affectedProjects;
  private final List<ResourceAssignmentDO> allResourceAssignements;
  //private static final Logger log = Logger.getLogger(ResourceAssignementWorkLoad.class);

  /**
   * constructor to create an IWorkloadData which calculates the planned workload for a given list of resource assignments.
   * @see IWorkloadData
   * @param allResourceAssignements
   * @param activityDao
   * @param scheduler
   * @param employeeDao
   * @param employeeAvailabilityProvider
   */
  public ResourceAssignmentWorkload(
      final List<ResourceAssignmentDO> allResourceAssignements, final WbsActivityDao activityDao,
      final IScheduler scheduler, final EmployeeDao employeeDao, final IEmployeeWorkdayProvider employeeAvailabilityProvider)
  {
    this.allResourceAssignements = allResourceAssignements;
    this.activityDao = activityDao;
    this.employeeDao = employeeDao;
    this.scheduler = scheduler;
    this.employeeAvailabilityProvider = employeeAvailabilityProvider;
    this.affectedProjects = new HashSet<ProjectDO>();
  }

  /**
   *returns the workload hours per day in the specified time interval  with respect to all given resource assignments
   *
   * @see org.projectforge.plugins.chimney.web.resourceworkload.model.IWorkloadData#getWorkloadHoursPerDay(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
   */
  @Override
  public double[] getWorkloadHoursPerDay(final DateMidnight beginDay, final DateMidnight endDay)
  {
    if (endDay.isBefore(beginDay)) {
      throw new IllegalArgumentException("negative interval: begin day must not be before end day.");
    }

    final int dayCount = (int)new Duration(beginDay, endDay).getStandardDays()+1;
    final double[] workloadHoursPerDay = new double[dayCount];

    for (int i=0; i < dayCount; ++i) {
      workloadHoursPerDay[i] = 0;
    }

    addAllResourceAssignementWorkloads(beginDay, endDay, allResourceAssignements, workloadHoursPerDay);

    return workloadHoursPerDay;
  }

  private void addAllResourceAssignementWorkloads(
      final DateMidnight beginDay, final DateMidnight endDay,
      final List<ResourceAssignmentDO> allResourceAssignements,
      final double[] workloadHoursPerDay) {

    AbstractWbsNodeDO wbsNode;
    WbsActivityDO wbsActivity;
    GanttScheduledActivity scheduledActivity;
    ProjectDO root;
    ResourceAssignmentDO resourceAssignement;
    for (final Iterator<ResourceAssignmentDO> rIt=allResourceAssignements.iterator(); rIt.hasNext(); ) {
      resourceAssignement = rIt.next();

      wbsNode = resourceAssignement.getWbsNode();
      // log.info(" assignement on "+wbsNode.getTitle()+"");
      root = WbsNodeUtils.getProject(wbsNode);
      if (!affectedProjects.contains(root)) {
        affectedProjects.add(root);
        wbsActivity = activityDao.getByOrCreateFor(root);
        scheduler.schedule(wbsActivity);
      }
      wbsActivity = activityDao.getByOrCreateFor(wbsNode);
      scheduledActivity = scheduler.getResult(wbsActivity);

      addResourceAssignmentWorkload(
          beginDay, endDay,
          scheduledActivity.getBegin(), scheduledActivity.getEnd(),
          resourceAssignement, workloadHoursPerDay
      );
    }

  }

  private void addResourceAssignmentWorkload(
      final DateMidnight beginDay, final DateMidnight endDay,
      final ReadableInstant scheduledAssignementBeginDate, final ReadableInstant scheduledAssignementEndDate,
      final ResourceAssignmentDO resourceAssignement, final double[] workloadHoursPerDay) {

    DateMidnight itBeginDay, itEndDay;

    if (scheduledAssignementBeginDate.isAfter(endDay) || scheduledAssignementEndDate.isBefore(beginDay)) {
      return;
    }

    if (scheduledAssignementBeginDate.isAfter(beginDay)) {
      itBeginDay = new DateMidnight(scheduledAssignementBeginDate);
    } else {
      itBeginDay = beginDay;
    }
    if (scheduledAssignementEndDate.isBefore(endDay)) {
      itEndDay = new DateMidnight(scheduledAssignementEndDate);;
    } else {
      itEndDay = endDay;
    }


    long plannedEffortMillis = 0;
    final Period plannedEffort = resourceAssignement.getPlannedEffort();
    if (plannedEffort!=null)
      plannedEffortMillis = StandardWorkdayNormalizer.toNormalizedDuration(plannedEffort).getMillis();

    if (plannedEffortMillis == 0)
      return; // no effort not needs to be added

    final EmployeeDO employee = employeeDao.getByUserId(resourceAssignement.getUser().getId());

    // log.info("plannedEffort: "+plannedEffortMillis+" on "+resourceAssignement+"");
    long plannedEffortPerWorkingDay;

    int workdayCountInScheduledActivity = 0;
    for (DateMidnight currentDay = new DateMidnight(scheduledAssignementBeginDate); !currentDay.isAfter(scheduledAssignementEndDate); currentDay = currentDay.plusDays(1)) {
      if (employeeAvailabilityProvider.isWorkdayFor(employee, currentDay)) {
        workdayCountInScheduledActivity++;
      }
    }
    if (workdayCountInScheduledActivity == 0) {
      throw new RessourceNotAvailableInScheduledTimeException(resourceAssignement, scheduledAssignementBeginDate, scheduledAssignementEndDate);

      /*
      // now an exception is thrown instead of the code in this comment, because the employee would need to work on non working days
      workingDayCountInScheduledNodeActivity = (int)(new Duration(scheduledAssignementBeginDate, scheduledAssignementEndDate).getStandardDays())+1;
      if (workingDayCountInScheduledNodeActivity <= 0)

        return; // no way to do work with no time
      plannedEffortPerWorkingDay = plannedEffortMillis/workingDayCountInScheduledNodeActivity;
      for (DateMidnight currentDay = itBeginDay; !currentDay.isAfter(itEndDay); currentDay = currentDay.plusDays(1)) {
        final int index = (int)(new Duration(beginDay, currentDay).getStandardDays());
        assert( index>=0 && index<workloadHoursPerDay.length );
        workloadHoursPerDay[index] += MILLIS_TO_HOURS_FACTOR * plannedEffortPerWorkingDay;
      }
      return;
       */
    }

    plannedEffortPerWorkingDay = plannedEffortMillis/workdayCountInScheduledActivity;
    //log.info("  add "+(MILLIS_TO_HOURS_FACTOR * plannedEffortPerWorkingDay)+" for each day from "+itBeginDay+" till "+ itEndDay);
    for (DateMidnight currentDay = itBeginDay; !currentDay.isAfter(itEndDay); currentDay = currentDay.plusDays(1)) {
      if (employeeAvailabilityProvider.isWorkdayFor(employee, currentDay)) {
        final int index = (int)(new Duration(beginDay, currentDay).getStandardDays());
        assert( index>=0 && index<workloadHoursPerDay.length );
        //log.info("   to index "+index+" (day: "+currentDay+")");

        workloadHoursPerDay[index] += MILLIS_TO_HOURS_FACTOR * plannedEffortPerWorkingDay;
      }
    }
  }
}
