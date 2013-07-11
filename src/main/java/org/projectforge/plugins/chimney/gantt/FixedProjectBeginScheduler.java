/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.DependencyRelationType;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.utils.date.DateCalculator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * <p>Use this class to schedule projects, which have only a fixed begin specified in the project activity. All other fixed begins or fixed ends on the activites are ignored.</p>
 * 
 * @author Sweeps &lt;pf@byte-storm.com&gt;
 * @see {@link FixedProjectBeginSchedulerTest} for implementation details
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class FixedProjectBeginScheduler implements IScheduler
{
  private final Map<WbsActivityDO, DateTime> begins = new HashMap<WbsActivityDO, DateTime>();
  private DateCalculator dayCalculator;
  private final Map<WbsActivityDO, DateTime> ends = new HashMap<WbsActivityDO, DateTime>();
  private final Set<WbsActivityDO> inSchedule = new HashSet<WbsActivityDO>();
  private NeededDurationProvider neededDurationProvider;
  private final Map<WbsActivityDO, GanttScheduledActivity> schedule = new HashMap<WbsActivityDO, GanttScheduledActivity>();
  private WbsActivityNavigator wbsActivityNavigator;

  /**
   * This constructor is not intended for use, but is needed for spring constructor injection to work properly. DO NOT DELETE.
   */
  @Deprecated
  public FixedProjectBeginScheduler() {}
  /**
   * Construct the scheduler with all its dependencies.
   * 
   * @param wbsActivityNavigator is used to navigate the activity tree
   * @param neededDurationProvider is used to get the minimum duration an activity must have
   * @param dateCalculator is used to calculate the begin and end dates of the activity. May be used to skip holidays and weekends.
   */
  public FixedProjectBeginScheduler(final WbsActivityNavigator wbsActivityNavigator, final NeededDurationProvider neededDurationProvider, final DateCalculator dateCalculator)
  {
    this.setNeededDurationProvider(neededDurationProvider);
    this.setWbsActivityNavigator(wbsActivityNavigator);
    this.setDayCalculator(dateCalculator);
  }

  /**
   * See {@link #FixedProjectBeginScheduler(WbsActivityNavigator, NeededDurationProvider, DateCalculator)} for use.
   */
  public DateCalculator getDayCalculator()
  {
    return dayCalculator;
  }

  /**
   * See {@link #FixedProjectBeginScheduler(WbsActivityNavigator, NeededDurationProvider, DateCalculator)} for use.
   */
  public NeededDurationProvider getNeededDurationProvider()
  {
    return neededDurationProvider;
  }

  /**
   * @see org.projectforge.plugins.chimney.gantt.IScheduler#getResult(org.projectforge.plugins.chimney.activities.WbsActivityDO)
   */
  @Override
  public GanttScheduledActivity getResult(final WbsActivityDO scheduledActivity)
  {
    return schedule.get(scheduledActivity);
  }

  /**
   * See {@link #FixedProjectBeginScheduler(WbsActivityNavigator, NeededDurationProvider, DateCalculator)} for use.
   */
  public WbsActivityNavigator getWbsActivityNavigator()
  {
    return wbsActivityNavigator;
  }

  /**
   * @see org.projectforge.plugins.chimney.gantt.IScheduler#schedule(org.projectforge.plugins.chimney.activities.WbsActivityDO)
   */
  @Override
  public void schedule(final WbsActivityDO wbsActivity)
  {
    checkScheduleArgument(wbsActivity);
    inSchedule.add(wbsActivity);
    final DateTime begin = wbsActivity.getFixedBeginDate();
    setScheduledBegin(wbsActivity, begin);
    scheduleChildren(wbsActivity);
    final DateTime end = findLatestChildEnd(wbsActivity, begin);
    setScheduledEnd(wbsActivity, end);
    store(wbsActivity, begin, end);
  }

  /**
   * See {@link #FixedProjectBeginScheduler(WbsActivityNavigator, NeededDurationProvider, DateCalculator)} for use.
   */
  public void setDayCalculator(final DateCalculator dayCalculator)
  {
    this.dayCalculator = dayCalculator;
  }

  /**
   * See {@link #FixedProjectBeginScheduler(WbsActivityNavigator, NeededDurationProvider, DateCalculator)} for use.
   */
  public void setNeededDurationProvider(final NeededDurationProvider neededDurationProvider)
  {
    this.neededDurationProvider = neededDurationProvider;
  }

  /**
   * See {@link #FixedProjectBeginScheduler(WbsActivityNavigator, NeededDurationProvider, DateCalculator)} for use.
   */
  public void setWbsActivityNavigator(final WbsActivityNavigator wbsActivityNavigator)
  {
    this.wbsActivityNavigator = wbsActivityNavigator;
  }

  private DateTime advanceDays(final DateTime date, final int days)
  {
    return dayCalculator.plus(date, days).toDateTime();
  }

  private Set<DateTime> beginToEndDependencyEnds(final WbsActivityDO wbsActivity)
  {
    final Set<DateTime> ends = new HashSet<DateTime>();

    for (final DependencyRelationDO depRel: filterNonDeleted(wbsActivity.getPredecessorRelations())) {
      if (depRel.getType() == DependencyRelationType.BEGIN_END) {
        final int daysOffset = depRel.getOffset().toStandardDays().getDays();
        ends.add(plus(findBegin(depRel.getPredecessor()), daysOffset));
      }
    }

    return ends;
  }

  private void checkScheduleArgument(final WbsActivityDO wbsActivity)
  {
    Validate.notNull(wbsActivity);

    if (wbsActivity.getId() == null) {
      throw new IllegalTransientActivityException(wbsActivity);
    }

    if (wbsActivity.getFixedBeginDate() == null) {
      throw new MissingFixedBeginDateException(wbsActivity);
    }
  }

  private boolean containsDeletedActivity(final DependencyRelationDO rel)
  {
    return isDeleted(rel.getPredecessor()) || isDeleted(rel.getSuccessor());
  }

  @SuppressWarnings("unused")
  private DateTime earliest(final DateTime... dateTimes) {
    final int length = dateTimes.length;

    if (length == 0) {
      return null;
    }

    DateTime earliest = null;

    for (int i = 0 ; i < length ; i++) {
      earliest = earliest(earliest, dateTimes[i]);
    }

    return earliest;
  }

  private DateTime earliest(final DateTime a, final DateTime b)
  {
    if (a == null && b == null) {
      return null;
    }

    if (a == null && b != null) {
      return b;
    }

    if (a != null && b == null) {
      return a;
    }

    if (b.isBefore(a)) {
      return b;
    } else {
      return a;
    }
  }

  private Collection<DateTime> endToEndDependencyEnds(final WbsActivityDO wbsActivity)
  {
    final Set<DateTime> ends = new HashSet<DateTime>();

    for (final DependencyRelationDO predRel: filterNonDeleted(wbsActivity.getPredecessorRelations())) {
      if (predRel.getType() == DependencyRelationType.END_END) {
        final int offsetDays = predRel.getOffset().toStandardDays().getDays();
        ends.add(regress(findEnd(predRel.getPredecessor()), offsetDays));
      }
    }

    return ends;
  }

  private Set<DependencyRelationDO> filterNonDeleted(final Set<DependencyRelationDO> predecessorRelations)
  {
    final Set<DependencyRelationDO> safeRelations = new HashSet<DependencyRelationDO>();

    for (final DependencyRelationDO rel: predecessorRelations) {
      if (containsDeletedActivity(rel)) {
        continue;
      }
      safeRelations.add(rel);
    }

    return safeRelations;
  }

  private DateTime findBegin(final WbsActivityDO wbsActivity)
  {
    final DateTime scheduledBegin = getScheduledBegin(wbsActivity);

    if (scheduledBegin != null) {
      return scheduledBegin;
    }

    final Set<DateTime> possibleBegins = new HashSet<DateTime>();
    final Set<DependencyRelationDO> predecessorRelations = filterNonDeleted(wbsActivity.getPredecessorRelations());

    for (final DependencyRelationDO predRel: predecessorRelations) {
      final WbsActivityDO predecessor = predRel.getPredecessor();
      final DependencyRelationType type = predRel.getType();
      final int offsetDays = predRel.getOffset().toStandardDays().getDays();

      switch (type) {
        case BEGIN_BEGIN:
          possibleBegins.add(advanceDays(findBegin(predecessor), offsetDays));
          break;

        case END_BEGIN:
          final DateTime end = findEnd(predecessor);
          if (isMilestone(predecessor) || isMilestone(wbsActivity)) {
            possibleBegins.add(advanceDays(end, offsetDays));
          } else {
            possibleBegins.add(advanceDays(nextDay(end), offsetDays));
          }
          break;
      }
    }

    final DateTime parentBegin = getScheduledBegin(wbsActivityNavigator.getParent(wbsActivity));
    possibleBegins.add(parentBegin);
    return latest(possibleBegins);
  }

  private DateTime findEnd(final WbsActivityDO wbsActivity)
  {
    final DateTime maybeEnd = getScheduledEnd(wbsActivity);
    DateTime end;

    if (maybeEnd == null) {
      subSchedule(wbsActivity);
      end = getScheduledEnd(wbsActivity);
    } else {
      end = maybeEnd;
    }

    end = getScheduledEnd(wbsActivity);
    return end;
  }

  private DateTime findEnd(final WbsActivityDO wbsActivity, final DateTime begin, final Integer neededProjectDays)
  {
    final DateTime minNeededEnd = plus(begin, neededProjectDays);
    final Collection<DateTime> possibleEnds = endToEndDependencyEnds(wbsActivity);
    possibleEnds.add(minNeededEnd);
    possibleEnds.addAll(beginToEndDependencyEnds(wbsActivity));
    final DateTime end = latest(possibleEnds);
    return end;
  }

  private DateTime findLatestChildEnd(final WbsActivityDO wbsActivity)
  {
    return findLatestChildEnd(wbsActivity, null);
  }

  private DateTime findLatestChildEnd(final WbsActivityDO wbsActivity, final DateTime fallback)
  {
    final List<WbsActivityDO> children = wbsActivityNavigator.getChildren(wbsActivity);

    if (children.isEmpty()) {
      return fallback;
    }

    DateTime latestEnd = fallback;

    for (final WbsActivityDO child: children) {
      latestEnd = latest(latestEnd, getScheduledEnd(child), findLatestChildEnd(child));
    }

    return latestEnd;
  }

  private DateTime getScheduledBegin(final WbsActivityDO wbsActivity)
  {
    return begins.get(wbsActivity);
  }

  private DateTime getScheduledEnd(final WbsActivityDO wbsActivity)
  {
    return ends.get(wbsActivity);
  }

  private boolean isDeleted(final WbsActivityDO predecessor)
  {
    return predecessor.getWbsNode().isDeleted();
  }

  private boolean isMilestone(final WbsActivityDO wbsActivity)
  {
    return Period.ZERO.equals(wbsActivity.getEffortEstimation());
  }

  private DateTime latest(final Collection<DateTime> dates)
  {
    final DateTime[] proto = new DateTime[dates.size()];
    return latest(dates.toArray(proto));
  }

  private DateTime latest(final DateTime... dateTimes) {
    final int length = dateTimes.length;

    if (length == 0) {
      return null;
    }

    DateTime latest = null;

    for (int i = 0 ; i < length ; i++) {
      latest = latest(latest, dateTimes[i]);
    }

    return latest;
  }

  private DateTime latest(final DateTime a, final DateTime b)
  {
    if (a == null && b == null) {
      return null;
    }

    if (a == null && b != null) {
      return b;
    }

    if (a != null && b == null) {
      return a;
    }

    if (b.isAfter(a)) {
      return b;
    } else {
      return a;
    }
  }

  @SuppressWarnings("unused")
  private DateTime minus(final DateTime date, final int days)
  {
    return dayCalculator.minus(date, days - 1).toDateTime();
  }

  private DateTime nextDay(final DateTime date)
  {
    return dayCalculator.plus(date, 1).toDateTime();
  }

  private DateTime plus(final DateTime date, final Integer projectDays)
  {
    return dayCalculator.plus(date, projectDays - 1).toDateTime();
  }

  private DateTime regress(final DateTime date, final int offsetDays)
  {
    return dayCalculator.minus(date, offsetDays).toDateTime();
  }

  private void scheduleChildren(final WbsActivityDO wbsActivity)
  {
    for (final WbsActivityDO child: wbsActivityNavigator.getChildren(wbsActivity)) {
      subSchedule(child);
    }
  }

  private void setScheduledBegin(final WbsActivityDO wbsActivity, final DateTime begin)
  {
    begins.put(wbsActivity, begin);
  }

  private void setScheduledEnd(final WbsActivityDO wbsActivity, final DateTime end)
  {
    ends.put(wbsActivity, end);
  }

  private void store(final WbsActivityDO wbsActivity, final DateTime begin, final DateTime end)
  {
    schedule.put(wbsActivity, new GanttScheduledActivity(begin, end));
    inSchedule.remove(wbsActivity);
  }

  private void subSchedule(final WbsActivityDO wbsActivity)
  {
    if (inSchedule.contains(wbsActivity)) {
      throw new CyclicDependencyException(wbsActivity);
    }

    inSchedule.add(wbsActivity);
    final DateTime begin = findBegin(wbsActivity);
    setScheduledBegin(wbsActivity, begin);
    scheduleChildren(wbsActivity);
    final Integer maybeNeededProjectDays = neededDurationProvider.neededProjectDays(wbsActivity);

    if (maybeNeededProjectDays != null && maybeNeededProjectDays != 0) {
      final DateTime end = findEnd(wbsActivity, begin, maybeNeededProjectDays);
      setScheduledEnd(wbsActivity, end);
      store(wbsActivity, begin, end);
      return;
    }

    final DateTime end = findLatestChildEnd(wbsActivity, begin);
    setScheduledEnd(wbsActivity, end);
    store(wbsActivity, begin, end);
  }
}
